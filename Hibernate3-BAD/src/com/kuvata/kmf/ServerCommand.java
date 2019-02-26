package com.kuvata.kmf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.DispatcherConstants;
import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;
import parkmedia.device.configurator.DeviceProperty;
import parkmedia.usertype.EventType;
import parkmedia.usertype.ServerCommandType;
import parkmedia.usertype.StatusType;
import parkmedia.usertype.SubStatusType;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.kuvata.ErrorLogger;
import com.kuvata.kmf.util.Files;

public class ServerCommand extends PersistentEntity 
{ 
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( ServerCommand.class );
	private Long serverCommandId;
	private Long deviceServerCommandId;
	private Device device;
	private ServerCommandType command;
	private StatusType status;
	private SubStatusType subStatus;
	private String parameters;
	private Date createDt;
	private Date lastModifiedDt;
	
	// Flag used to allow debug duration logging
	private static boolean debugMode = false;
	
	static{
		try{
			String durationLines = KuvataConfig.getPropertyValue("Application.durationLines");
			if(durationLines != null && durationLines.length() > 0){
				debugMode = Boolean.parseBoolean(durationLines);
			}
		}catch(Exception e){}
	}
	
	/**
	 * 
	 *
	 */
	public ServerCommand()
	{		
	}
	
	public Long getEntityId() {
		return this.serverCommandId;
	}
	
	/**
	 * Returns a ServerCommand for the given deviceId and deviceServerCommandId
	 * @param deviceId
	 * @param deviceServerCommandId
	 * @return
	 */
	public static ServerCommand getServerCommand(Long deviceId, Long deviceServerCommandId)
	{
		Session session = HibernateSession.currentSession();				
		return (ServerCommand)session.createCriteria( ServerCommand.class )
			.add( Expression.eq("device.deviceId", deviceId) )		
			.add( Expression.eq("deviceServerCommandId", deviceServerCommandId) )
			.uniqueResult();
	}
	
	public static void create(Long deviceServerCommandId, Device device, ServerCommandType command, String parameters)
	{
		// If a row does not already exist for this server command
		ServerCommand serverCommand = ServerCommand.getServerCommand( device.getDeviceId(), deviceServerCommandId );		
		if( serverCommand == null )
		{										
			// Create a new row in the server_command table
			ServerCommand sc = new ServerCommand();
			sc.setDeviceServerCommandId( deviceServerCommandId );
			sc.setDevice( device );
			sc.setCommand( command );
			sc.setParameters( parameters );
			sc.setStatus( StatusType.PENDING );
			sc.setCreateDt( new Date() );
			sc.save();
		}else{
			logger.info("Could not create server command because one already exists with the given deviceServerCommandId: "+ deviceServerCommandId);
		}
	}
	
	public static List<ServerCommand> getUnexecutedServerCommands()
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT sc "
			+ "FROM ServerCommand as sc "	
			+ "WHERE sc.status='"+ StatusType.PENDING.toString() +"' "
			+ "ORDER BY sc.deviceServerCommandId";
		return session.createQuery( hql ).setMaxResults( Constants.SERVER_COMMAND_MAX_RESULTS ).list();				
	}
	
	public static Long getUnexecutedServerCommandCount()
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(*) "
			+ "FROM ServerCommand as sc "
			+ "WHERE sc.status='"+ StatusType.PENDING.toString() + "'";
		return (Long)session.createQuery( hql ).list().get(0);
	}
	
	/**
	 * Returns the id of the highest pending server command issued by the given device
	 * @param device
	 * @return
	 */
	public static long getHighestProcessedDeviceServerCommandId(Device device)
	{
		long result = -1l;
			if( device != null ){
				Session session = HibernateSession.currentSession();
				String hql = "SELECT MAX(sc.deviceServerCommandId) "
					+ "FROM ServerCommand as sc "	
					+ "WHERE sc.status ='"+ StatusType.SUCCESS.toString() +"' "
					+ "AND sc.device.deviceId = ?";
				Iterator<Long> i = session.createQuery( hql ).setParameter( 0, device.getDeviceId() ).iterate();
				Long maxDeviceServerCommandId = i.next();
				Hibernate.close( i );
				if( maxDeviceServerCommandId != null ){
					result = maxDeviceServerCommandId.longValue();
				}
			}
		return result;
	}
	
	public static void processUploadFile(File f) throws Exception
	{
		if( f.exists() )
		{
			// Get the staging folder under the uploads directory		
			String uploadsDir = SchemaDirectory.getSchema().getUploadsDir();
			String filename = f.getName();
			String stagingPath = uploadsDir +"/"+ DispatcherConstants.STAGING_DIRECTORY +"/"+ filename;
			File stagingFile = new File( stagingPath );
			boolean success = Files.renameTo( f, stagingFile );
			logger.info("moving log file from "+ f.getAbsolutePath() + " to "+ stagingPath);

			if( success )
			{									
				// If we've just uploaded the playback_events.zip file
				if( filename.indexOf( Constants.PLAYBACK_EVENTS_FILENAME ) >= 0 )
				{
					Object[] identifier = debugLog("START T:", null);
					String playbackEventsDir = SchemaDirectory.getSchema().getPlaybackEventsDir();
					Files.makeCheckDirectory( playbackEventsDir );
					Files.renameTo( stagingFile, new File( playbackEventsDir + "/" +  stagingFile.getName() ) );
					debugLog("END T:", identifier);
				}
				// If we've just uploaded the file_exists.zip file
				else if( filename.indexOf( DispatcherConstants.FILE_EXISTS_FILENAME_PREFIX ) >= 0 ){
					Object[] identifier = debugLog("START U:", null);
					FileTransmission.handleFileExistsFileUpload( stagingFile );
					debugLog("END U:", identifier);
				}		
				// If we've just uploaded the self_tests.zip file
				else if( filename.indexOf( DispatcherConstants.SELF_TESTS_FILENAME ) >= 0 ){
					Object[] identifier = debugLog("START V:", null);
					SelfTestHistory.handleSelfTestsFileUpload( stagingFile );
					debugLog("END V:", identifier);
				}	
				// If we've just uploaded a screenshot file
				else if( filename.indexOf( DispatcherConstants.SCREENSHOT_FILENAME ) >= 0 ){
					Object[] identifier = debugLog("START W:", null);
					Device.handleScreenshotFileUpload( stagingFile );
					debugLog("END W:", identifier);
				}	
				/*
				 * NOTE: As of 3.2, these conditions are no longer necessary, however we 
				 * need to keep them in for backward compatibility.
				 */
				// If we've just uploaded the advanced_properties.zip file		
				else if( filename.indexOf( DeviceProperty.ADVANCED_PROPERTIES_FILENAME ) >= 0 ){
					Object[] identifier = debugLog("START X:", null);
					Device.handleAdvancedPropertiesFileUpload( stagingFile );
					debugLog("END X:", identifier);
				}	
				// If we've just uploaded an mcm file
				else if( filename.indexOf( DispatcherConstants.MCM_FILENAME ) >= 0 ){
					Object[] identifier = debugLog("START Y:", null);
					McmHistoryEntry.createMcmHistoryEntry( stagingFile );
					debugLog("END Y:", identifier);
				}
				// If we've just uploaded a hardware info file
				else if( filename.indexOf( DispatcherConstants.HARDWARE_INFO_FILENAME ) >= 0 ){
					Object[] identifier = debugLog("START Z:", null);
					File hwInfo = new File(SchemaDirectory.getSchema().getHwInfoDir() + "/" + filename);
					Files.renameTo( stagingFile, hwInfo );
					handleHardwareInfoFiles();
					debugLog("END Z:", identifier);
				}
			}
			// If the file was not renamed successfully -- delete it	
			else if( f.isDirectory() == false )
			{
				f.delete();
			}				
		}		
	}
	
	/**
	 * Creates a row in the event_history table based on the given parameters
	 * @param params
	 * @param device
	 * @throws ParseException
	 */
	public static void handleLogEventServerCommand(HashMap<String, String> params, Device device) throws ParseException
	{		
		SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		String strEventType = params.get( Constants.EVENT_TYPE );
		String strEventDt = params.get( Constants.EVENT_DT );
		EventType eventType = EventType.getEventType( strEventType );
		String strEventDetails = params.get( Constants.EVENT_DETAILS );
		Date eventDt = dateFormat.parse( strEventDt );
		if( device != null )
		{
			// Create a row in event_history table
			EventHistory.create( eventType, eventDt, device.getDeviceId(), strEventDetails );			
		}else{
			logger.info("Could not create event_history record because the given device is null.");
		}	
	}
	
	private static void handleHardwareInfoFiles(){
		
		// Get the hardware info directory
		File dir = new File(SchemaDirectory.getSchema().getHwInfoDir());
		
		// For each file in this directory
		for(File f : dir.listFiles()){
			
			// If this is a hardware info file
			if( f.exists() && f.getName().startsWith(DispatcherConstants.HARDWARE_INFO_FILENAME))
			{
				Long deviceId = null;
				try {
					// Get the deviceId from the filename
					deviceId = Long.parseLong(f.getName().substring(f.getName().indexOf("-") + 1));
					Device device = Device.getDevice(deviceId);
					
					// Parse the csv file (NOTE: Excel specific!?!)
					InputStream is = new FileInputStream( f );
					LabeledCSVParser lcsvp = new LabeledCSVParser( new ExcelCSVParser( is, ',' ) );
					
					// Column names
					String[] columnNames = new String[]{"SysMake","SysModel","BoardMfg","BoardModel","Encfs","DriveSize","BootLoc","BootSize","RootLoc","RootSize",
							"SwapLoc","SwapSize","ContentLoc","ContentSize","VideoCard","AudioChipset","SysRAM","ProcInfo","CoreCount","EthernetChipset","USBType","ttyS0","ttyS1","ttyS2","ttyS3"};
					
					// Verify that the required columns are present
					boolean headersPresent = true;
					for(String columnName : columnNames){
						int columnIndex = lcsvp.getLabelIdx( columnName );
						if(columnIndex < 0){
							logger.error("Could not locate column " + columnName + " in hardware info file: " + f.getAbsolutePath() + ". Ignoring file.");
							headersPresent = false;
							break;
						}
					}
					
					// If all columns are present
					if(headersPresent){
						String[] columnValues = new String[columnNames.length];
						String[] rawDataLine = lcsvp.getLine();
						if( rawDataLine != null )
						{
							// Get all column values from the file
							for(int i=0; i<columnNames.length; i++){
								String value = lcsvp.getValueByLabel( columnNames[i] );
								if(value != null){
									columnValues[i] = value.trim();
								}
							}
							
							// We have all data to create the hardware info row
							if(device != null){
								DeviceHardwareInfo oldDhi = DeviceHardwareInfo.getDeviceHardwareInfo(device);
								DeviceHardwareInfo dhi = DeviceHardwareInfo.create(columnValues, device);
								
								// If we successfully created the row
								if(dhi != null){
									// Delete this file
									f.delete();
									
									// Delete the old hardware info
									if(oldDhi != null){
										oldDhi.delete();
									}
								}
							}else{
								logger.error("Could not locate device with id: " + deviceId + ". Ignoring hardware info file.");
							}
						}
					}
				} catch (Exception e) {
					ErrorLogger.logError( "Error parsing hardware info file for device: " + deviceId );
				}
			}
		}
	}
	
	private static Object[] debugLog(String message, Object[] o){
		if(debugMode){
			// Begin method
			if(o == null){
				Random rand = new Random();
				Date d = new Date();
				String identifier = d.getTime() +"-"+ rand.nextInt(100000);
				o = new Object[] { identifier, d };
				logger.debug(message + (String)o[0]);
			}
			// End method
			else{
				logger.debug(message + (String)o[0] + " DURATION="+ (System.currentTimeMillis() - ((Date)o[1]).getTime())/1000);
			}
		}
		return o;
	}
	
	/**
	 * @return the serverCommandId
	 */
	public Long getServerCommandId() {
		return serverCommandId;
	}

	/**
	 * @param serverCommandId the serverCommandId to set
	 */
	public void setServerCommandId(Long serverCommandId) {
		this.serverCommandId = serverCommandId;
	}

	/**
	 * @return the status
	 */
	public StatusType getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(StatusType status) {
		this.status = status;
	}

	/**
	 * @return the subStatus
	 */
	public SubStatusType getSubStatus() {
		return subStatus;
	}

	/**
	 * @param subStatus the subStatus to set
	 */
	public void setSubStatus(SubStatusType subStatus) {
		this.subStatus = subStatus;
	}

	/**
	 * @return the parameters
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the createDt
	 */
	public Date getCreateDt() {
		return createDt;
	}

	/**
	 * @param createDt the createDt to set
	 */
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}

	/**
	 * @return the lastModifiedDt
	 */
	public Date getLastModifiedDt() {
		return lastModifiedDt;
	}

	/**
	 * @param lastModifiedDt the lastModifiedDt to set
	 */
	public void setLastModifiedDt(Date lastModifiedDt) {
		this.lastModifiedDt = lastModifiedDt;
	}

	/**
	 * @return the command
	 */
	public ServerCommandType getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(ServerCommandType command) {
		this.command = command;
	}

	/**
	 * @return the deviceServerCommandId
	 */
	public Long getDeviceServerCommandId() {
		return deviceServerCommandId;
	}

	/**
	 * @param deviceServerCommandId the deviceServerCommandId to set
	 */
	public void setDeviceServerCommandId(Long deviceServerCommandId) {
		this.deviceServerCommandId = deviceServerCommandId;
	}

	/**
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(Device device) {
		this.device = device;
	}
	
}
