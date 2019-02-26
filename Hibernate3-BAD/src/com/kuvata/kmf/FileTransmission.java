package com.kuvata.kmf;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.DispatcherConstants;
import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;
import parkmedia.usertype.FileTransmissionStatus;
import parkmedia.usertype.FileTransmissionType;

import com.kuvata.ErrorLogger;
import com.kuvata.kmf.util.Files;
import com.kuvata.kmf.util.Reformat;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.XPath;

public class FileTransmission extends Entity { 

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( FileTransmission.class );
	private Long fileTransmissionId;
	private Device device;
	private String filename;
	private Long filesize;
	private FileTransmissionStatus status;
	private FileTransmissionType type;
	private Date dt;	
	private Date transferDt;
	
	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
	
	/*
	 * The fileTransmissionSync HashMap contains the ids of devices and is used
	 * by the handleFileExstsFileUpload and handUpdateFileTransmissionServerCommand
	 * methods to ensure that they are operating on only one device at a time
	 */
	private static HashMap<Long,Long> fileTransmissionSync = new HashMap<Long, Long>();
	
	/**
	 * 
	 *
	 */
	public FileTransmission()
	{		
	}

	/**
	 * 
	 * @param p
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static FileTransmission getFileTransmission(Device device, String filename) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		FileTransmission ft = (FileTransmission)session.createCriteria(FileTransmission.class)
				.add( Expression.eq("device.deviceId", device.getDeviceId()) )		
				.add( Expression.eq("filename", filename).ignoreCase() )				
				.uniqueResult();							
		return ft;		
	}
	
	public static List<FileTransmission> getFileTransmissionsForCancellation(Device device, Long assetId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(FileTransmission.class)
				.add( Expression.eq("device.deviceId", device.getDeviceId()) )		
				.add( Expression.like("filename", "%/" + assetId + "-%").ignoreCase() )				
				.list();	
	}
	
	/**
	 * Called from DispatcherSoalBindingImpl.logFileUploadComplete().
	 * Updates the database with device presentations found in the given file_exists.xml file
	 * @param advancedPropertiesFile
	 */
	public static void handleFileExistsFileUpload(File zipFile)
	{
		boolean deleteZipFile = true;
		File fileExistsFile = null;
		try
		{				
			// First, unzip the zip file (ignoring directory structure) into the logs directory
			String unzipPath = zipFile.getAbsolutePath().substring( 0, zipFile.getAbsolutePath().lastIndexOf("/") );
			String extractedFileName = Files.unzip( zipFile.getAbsolutePath(), unzipPath, false );
			if( extractedFileName != null && extractedFileName.length() > 0 ){
				String fileExistsPath = unzipPath +"/"+ extractedFileName;
				fileExistsFile = new File( fileExistsPath );
				if( fileExistsFile.exists() )
				{
					Document doc = new Document( fileExistsFile );
					Element root = doc.getElement( new XPath("//"+ DispatcherConstants.FILE_EXISTS_ROOT_ELEMENT ) );
					
					// Get the mac address out of the root element
					String macAddr = root.getAttribute( DispatcherConstants.MAC_ADDRESS_ATTRIBUTE );
					if( macAddr != null && macAddr.length() > 0 )
					{	
						Device device = Device.getDeviceByMacAddr( macAddr );
						if( device != null )
						{
							Long lockObj = null;
							if( fileTransmissionSync.containsKey( device.getDeviceId() ) )
							{
								lockObj = fileTransmissionSync.get( device.getDeviceId() );
							}
							else
							{
								lockObj = new Long( device.getDeviceId() );
								fileTransmissionSync.put( lockObj, lockObj );
							}
	
							synchronized (lockObj)
							{
								HashSet<String> existingFileTransmissions = new HashSet<String>();				
								existingFileTransmissions.addAll( FileTransmission.getFileTransmissionFilenames( device ) );
							
								// Log a message and create a row for each file that was reported from the device that does not exist in the file_transmission table
								Elements es = root.getElements();
								HashMap<String, Date> xmlCache = new HashMap<String, Date>();
								while( es.hasMoreElements() )
								{				
									Element e = (Element)es.next();
									
									// If there is not already a fileTransmission object for this file 
									String name = e.getAttribute( DispatcherConstants.NAME_ATTRIBUTE ).toLowerCase();
									String transferDtStr = e.getAttribute( DispatcherConstants.TRANSFER_DT_ATTRIBUTE ).toLowerCase();
									Date transferDt = transferDtStr != null && transferDtStr.length() > 0 ? DATE_TIME_FORMAT.parse(transferDtStr) : null;
									xmlCache.put(name, transferDt);
									if( existingFileTransmissions.contains( name ) == false )
									{
										// Log the message
										logger.info("The following file: "+ name +" exists on this device: "+ device.getDeviceId() +" but does not exist in the file_transmission table");
										FileTransmissionType fileTransmissionType = FileTransmissionType.getFileTransmissionType( e.getAttribute( DispatcherConstants.TYPE_ATTRIBUTE ) );
										FileTransmission.create( device, name, FileTransmissionStatus.EXISTS, fileTransmissionType, new Date(), transferDt, false );
										existingFileTransmissions.add( name );
									}								
								}	
		
								// Log a message and delete the row for each row in the file_transmission table that was not included in the report from the device
								boolean updateBytesToDownload = false;
								List<FileTransmission> fts = FileTransmission.getFileTransmissions( device );
								for( Iterator<FileTransmission> i=fts.iterator(); i.hasNext(); )
								{
									FileTransmission ft = i.next();
									if( xmlCache.containsKey(ft.getFilename()) == false ){
										// If this file does not need to be sent to the device
										if(ft.status.isTransmissionNeeded() == false){
											logger.info("The following row in the file_transmission table: "+ ft.getFilename() +" was not included in the list of files from this device: "+ device.getDeviceId());
											ft.delete();
											i.remove();
											updateBytesToDownload = true;
										}
									}
									// Now that we have determined that the file exists in both the file_transmission table on the server and exists on the device
									else
									{
										// If the status of this row in the file_transmission table is not "Exists"
										if( ft.getStatus() == null || ft.getStatus().getPersistentValue().equalsIgnoreCase( FileTransmissionStatus.EXISTS.getPersistentValue() ) == false )
										{
											// Get the transfer date
											Date transferDt = xmlCache.get(ft.getFilename());
											if(transferDt != null){
												ft.setTransferDt(transferDt);
											}
											
											// Set it to "Exists"
											ft.setStatus( FileTransmissionStatus.EXISTS );
											ft.update();
											updateBytesToDownload = true;
										}
									}
								}
								
								// If we determined that bytesToDownload needs to be updated for this device -- update it
								if( updateBytesToDownload ){
									device.setBytesToDownload( FileTransmission.getBytesToDownload( device ) );
								}
														
								// Update the lastFileExistsDt property for this device
								device.setLastFileExistsDt( new Date() );
								device.update();
							}
						}
						else
						{
							logger.info("Could not locate a device with the given mac address: "+ macAddr +". Unable to continue.");
						}
					}
					else
					{
						logger.info("Could not locate macAddr attribute in "+ DispatcherConstants.FILE_EXISTS_FILENAME_PREFIX +". Unable to continue.");
					}					
				}
				else
				{
					logger.info("Could not locate specified file: "+ fileExistsPath +". Unable to continue.");
				}		
			}
		}
		catch(Exception e)
		{
			if(e instanceof java.io.IOException){
				deleteZipFile = false;
				ErrorLogger.logError("Could not parse file under: " + zipFile + ". Saving off the zip file for review.");
			}else{
				logger.error( e );
			}
		}
		finally
		{
			// Delete the zip file		
			if( zipFile != null && zipFile.isDirectory() == false && deleteZipFile){
				zipFile.delete();
			}
			// Delete the file_exists.xml file			
			if( fileExistsFile != null && fileExistsFile.isDirectory() == false ){
				fileExistsFile.delete();
			}
		}
	}	
	
	public static void handleUpdateFileTransmissionServerCommand(HashMap<String, String> params, Device device) throws ParseException
	{
		Long lockObj = null;
		if( fileTransmissionSync.containsKey( device.getDeviceId() ) )
		{
			lockObj = fileTransmissionSync.get( device.getDeviceId() );
		}
		else
		{
			lockObj = new Long( device.getDeviceId() );
			fileTransmissionSync.put( lockObj, lockObj );
		}

		synchronized (lockObj)
		{
			String filename = params.get( DispatcherConstants.NAME_ATTRIBUTE );
			String strFileTransmissionType = params.get( DispatcherConstants.TYPE_ATTRIBUTE );
			String strLastModified = params.get( DispatcherConstants.TIMESTAMP_ATTRIBUTE );
			String strTransferDate = params.get( DispatcherConstants.TRANSFER_DT_ATTRIBUTE );
			String strDelete = params.get( DispatcherConstants.DELETE_ATTRIBUTE );
			FileTransmissionType fileTransmissionType = null;
			if( strFileTransmissionType != null && strFileTransmissionType.length() > 0 ){
				fileTransmissionType = FileTransmissionType.getFileTransmissionType( strFileTransmissionType );
			}
			Date lastModified = null;
			if( strLastModified != null && strLastModified.length() > 0 ){
				lastModified = DATE_TIME_FORMAT.parse( strLastModified );	
			}	
			Date transferDt = null;
			if( strTransferDate != null && strTransferDate.length() > 0 ){
				transferDt = DATE_TIME_FORMAT.parse( strTransferDate );	
			}
			
			// If the delete flag was passed in
			if( strDelete != null && strDelete.equalsIgnoreCase( Boolean.TRUE.toString() ) )
			{
				// Delete the row from the file_transmission table
				FileTransmission fileTransmission = FileTransmission.getFileTransmission( device, filename );
				if( fileTransmission != null )
				{
					logger.info("Deleting file_transmission for device: "+ device.getDeviceId() +" - filename: "+ filename );
					fileTransmission.delete();
				}else{
					logger.info("Could not locate corresponding row in file_transmission table for device: "+ device.getDeviceId() +" - filename: "+ filename );
				}
			}
			else
			{
				// Create or update the existing file_transmission record
				FileTransmission.createOrUpdate( device, filename, FileTransmissionStatus.EXISTS, fileTransmissionType, lastModified, transferDt, true );
			}		
		}
	}
	
	public static synchronized FileTransmission createOrUpdate(Device device, String filename, FileTransmissionStatus status, FileTransmissionType type, Date dt, Date transferDt, boolean updateBytesToDownload)
	{
		// If a file transmission already exists for this device/presentation
		FileTransmission result = null;
		FileTransmission fileTransmission = FileTransmission.getFileTransmission( device, filename );
		if( fileTransmission != null )
		{
			// If the existing priority is less than the priority we're about to set
			if(fileTransmission.getStatus().getPriority() < status.getPriority())
			{
				// Update the file transmission record
				fileTransmission.setStatus( status );
				fileTransmission.setType( type );
				fileTransmission.setDt( dt );
				
				// If a transferDt was passed in -- update it -- otherwise ignore it
				if( transferDt != null ){
					fileTransmission.setTransferDt( transferDt );
				}
				fileTransmission.update();
				 
				// Update the bytesToDownload property of the device
				if( updateBytesToDownload ){
					device.updateBytesToDownload();
				}
				
				// Return the fileTransmissions in an ArrayList
				result = fileTransmission;
			}
		}
		else
		{
			// Create it
			logger.info("Could not locate file_transmission record for device: "+ (device != null ? device.getDeviceId() : "") +" - filename: "+ filename +". Creating record.");
			FileTransmission newFt = create( device, filename, status, type, dt, transferDt, updateBytesToDownload ); 
			result = newFt;
		}
		return result;
	}
	
	public static FileTransmission create(Device device, String filename, FileTransmissionStatus status, FileTransmissionType type, Date dt, Date transferDt, boolean updateBytesToDownload)
	{
		FileTransmission fileTransmission = new FileTransmission();
		fileTransmission.setDevice( device );
		fileTransmission.setFilename( filename );
		fileTransmission.setStatus( status );
		fileTransmission.setType( type );
		fileTransmission.setDt( dt );
		
		// If a transferDt was passed in -- update it -- otherwise ignore it
		if( transferDt != null ){
			fileTransmission.setTransferDt( transferDt );
		}		
		
		// Attempt to get the size of this file
		Long filesize = 0L;
		String filepath = KuvataConfig.getKuvataHome() +"/"+ Constants.SCHEMAS +"/"+ SchemaDirectory.getSchema().getSchemaName() +"/"+ filename;		
		File f = new File( filepath );
		if( f.exists() ){
			filesize = Long.valueOf( f.length() );
		}
		fileTransmission.setFilesize( filesize );
		fileTransmission.save();
		
		// Update the bytesToDownload property of the device
		if( updateBytesToDownload ){
			device.updateBytesToDownload();
		}
		return fileTransmission;
	}
	
	/**
	 * Returns a list of fileTransmission objects for this device
	 * @return 
	 */
	public static List<FileTransmission> getFileTransmissions(Device device) throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT fileTransmission "
					+ "FROM FileTransmission fileTransmission "
					+ "WHERE fileTransmission.device.deviceId = :deviceId "					
					+ "ORDER BY fileTransmission.filename";
		return session.createQuery( hql ).setParameter("deviceId", device.getDeviceId()).list();  	
	}	
	
	/**
	 * Returns a list of fileTransmission filenames for this device
	 * @return 
	 */
	public static List<String> getFileTransmissionFilenames(Device device) throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT LOWER(fileTransmission.filename) "
					+ "FROM FileTransmission fileTransmission "
					+ "WHERE fileTransmission.device.deviceId = :deviceId "					
					+ "ORDER BY fileTransmission.filename";
		return session.createQuery( hql ).setParameter("deviceId", device.getDeviceId()).list();  	
	}		
	
	/**
	 * Returns the SUM total filesize of fileTransmission objects for this device where status is either
	 * NEEDED, NEEDED_FOR_FUTURE, or IN_PROGRESS.
	 * @return 
	 */
	public static Long getBytesToDownload(Device device) throws HibernateException 
	{			
		Long result = 0L;
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT SUM(fileTransmission.filesize) "
					+ "FROM FileTransmission fileTransmission "
					+ "WHERE fileTransmission.device.deviceId = :deviceId "		
					+ "AND (fileTransmission.status = '"+ FileTransmissionStatus.NEEDED.getPersistentValue() + "' "
					+ "OR fileTransmission.status = '"+ FileTransmissionStatus.NEEDED_FOR_FUTURE.getPersistentValue() + "' "
					+ "OR fileTransmission.status = '"+ FileTransmissionStatus.IN_PROGRESS.getPersistentValue() + "')";
		Iterator i = session.createQuery( hql ).setParameter("deviceId", device.getDeviceId()).iterate();
		if( i.hasNext() ){
			Long temp1 = (Long)i.next();
			if(temp1 != null){
				result = temp1.longValue();	
			}			
		}			
		Hibernate.close( i );
		return result;  	
	}	
	
	/**
	 * Returns all fileTransmission objects for this device where status is either
	 * NEEDED, NEEDED_FOR_FUTURE, or IN_PROGRESS.
	 * @return 
	 */
	public static List<FileTransmission> getFileTransmissionsToDownload(Device device) throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT fileTransmission "
					+ "FROM FileTransmission fileTransmission "
					+ "WHERE fileTransmission.device.deviceId = :deviceId "		
					+ "AND (fileTransmission.status = '"+ FileTransmissionStatus.NEEDED.getPersistentValue() + "' "
					+ "OR fileTransmission.status = '"+ FileTransmissionStatus.NEEDED_FOR_FUTURE.getPersistentValue() + "' "
					+ "OR fileTransmission.status = '"+ FileTransmissionStatus.IN_PROGRESS.getPersistentValue() + "') "
					+ "ORDER BY fileTransmission.dt";
		return session.createQuery( hql ).setParameter("deviceId", device.getDeviceId()).list();
	}		
	
	public static List<FileTransmission> getFutureFileTransmissionsToDownload(Device device) throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT fileTransmission "
					+ "FROM FileTransmission fileTransmission "
					+ "WHERE fileTransmission.device.deviceId = :deviceId "		
					+ "AND fileTransmission.status = '"+ FileTransmissionStatus.NEEDED_FOR_FUTURE.getPersistentValue() + "' "
					+ "ORDER BY fileTransmission.dt";
		return session.createQuery( hql ).setParameter("deviceId", device.getDeviceId()).list();
	}
	
	/**
	 * Returns a list of fileTransmission objects that have a status of either "Needed" or "Needed For Future",
	 * or have an old "In Progress" status.
	 * @return 
	 */
	public static List<FileTransmission> getFileTransmissionsForMulticast(long minId, long maxId) throws HibernateException 
	{
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT fileTransmission "
					+ "FROM FileTransmission fileTransmission "
					+ "WHERE (fileTransmission.status = '"+ FileTransmissionStatus.NEEDED.getPersistentValue() + "' "
					+ "OR fileTransmission.status = '"+ FileTransmissionStatus.NEEDED_FOR_FUTURE.getPersistentValue() + "' "
					+ "OR fileTransmission.status = '"+ FileTransmissionStatus.IN_PROGRESS.getPersistentValue() + "') "
					+ "AND fileTransmission.device.multicastNetwork IS NOT NULL "
					+ "AND fileTransmission.fileTransmissionId > :minId "
					+ "AND fileTransmission.fileTransmissionId <= :maxId "
					+ "ORDER BY fileTransmission.filename";
		return session.createQuery( hql ).setParameter("minId", minId).setParameter("maxId", maxId).list();
	}
	
	public static Long getMaxFileTransmissionId(){
		Long result = 0l;
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT MAX(fileTransmission.fileTransmissionId) FROM FileTransmission fileTransmission";
		Iterator i = session.createQuery( hql ).iterate();
		if(i.hasNext()){
			result = (Long)i.next();
		}
		return result;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof FileTransmission) ) result = false;
		
		FileTransmission c = (FileTransmission) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}	
	/**
	 * Return hashCode for unique Displayarea
	 */
	public int hashCode()
	{
		int result = FileTransmission.class.getName().hashCode();
		result = Reformat.getSafeHash( this.fileTransmissionId, result, 3 );
		result = Reformat.getSafeHash( this.device.getDeviceId(), result, 5 );
		result = Reformat.getSafeHash( this.dt, result, 7 );		
		return result;
	}

	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getFileTransmissionId();
	}
	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}
	
	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}
	
	/**
	 * @return Returns the fileTransmissionId.
	 */
	public Long getFileTransmissionId() {
		return fileTransmissionId;
	}
	
	/**
	 * @param fileTransmissionId The fileTransmissionId to set.
	 */
	public void setFileTransmissionId(Long fileTransmissionId) {
		this.fileTransmissionId = fileTransmissionId;
	}
	
	/**
	 * @return Returns the dt.
	 */
	public Date getDt() {
		return dt;
	}
	
	/**
	 * @param dt The dt to set.
	 */
	public void setDt(Date dt) {
		this.dt = dt;
	}
	
	/**
	 * @return Returns the presentation.
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param presentation The presentation to set.
	 */
	public void setFilename(String presentation) {
		this.filename = presentation;
	}

	/**
	 * @return Returns the status.
	 */
	public FileTransmissionStatus getStatus() {
		return status;
	}
	

	/**
	 * @param status The status to set.
	 */
	public void setStatus(FileTransmissionStatus status) {
		this.status = status;
	}

	/**
	 * @return Returns the type.
	 */
	public FileTransmissionType getType() {
		return type;
	}
	

	/**
	 * @param type The type to set.
	 */
	public void setType(FileTransmissionType type) {
		this.type = type;
	}

	/**
	 * @return the filesize
	 */
	public Long getFilesize() {
		return filesize;
	}

	/**
	 * @param filesize the filesize to set
	 */
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	/**
	 * @return the transferDt
	 */
	public Date getTransferDt() {
		return transferDt;
	}

	/**
	 * @param transferDt the transferDt to set
	 */
	public void setTransferDt(Date transferDt) {
		this.transferDt = transferDt;
	}
	
}
