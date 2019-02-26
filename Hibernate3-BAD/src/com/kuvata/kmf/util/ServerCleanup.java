/*
 * Created on Mar 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.DispatcherConstants;
import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.device.configurator.DeviceProperty;
import parkmedia.usertype.JobType;
import parkmedia.usertype.StatusType;

import com.kuvata.StreamGobbler;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HeartbeatEvent;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Job;
import com.kuvata.kmf.PlaybackEvent;
import com.kuvata.kmf.PlaybackEventSummary;
import com.kuvata.kmf.Schema;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.ServerCommand;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

/**
 * @author jrandesi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServerCleanup extends TimerTask
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( ServerCleanup.class );
	private static String memoryAllocationMin = "64m";	// Default initial memory allocation 
	private static String memoryAllocationMax = "256m";	// Default maximum memory allocation
	private static final int DELETE_BATCH_SIZE = 2000000;
	private static final int DAYS_BEFORE_PROCESSING_OCCURS = 1;
	
	public ServerCleanup()
	{		
	}
	
	/**
	 * Launches the ServerCleanup java program in its own JVM.
	 */
	public void run()
	{
		try
		{
			// Define the memory allocation range for the java process
			try {
				memoryAllocationMin = KuvataConfig.getPropertyValue("ContentScheduler.memoryMin");
			} catch(KmfException e) {
				logger.info("Could not location property ContentScheduler.memoryMin. Using default: "+ memoryAllocationMin);
			}
			try {
				memoryAllocationMax = KuvataConfig.getPropertyValue("ContentScheduler.memoryMax");
			} catch(KmfException e) {
				logger.info("Could not location property ContentScheduler.memoryMax. Using default: "+ memoryAllocationMax);
			}
			
			/*
			 * Build and execute the java command to execute the server clean up
			 */			
			String cmd = "java -cp "+ KuvataConfig.getPropertyValue("classpath") +" -Xms"+ memoryAllocationMin +" -Xmx"+ memoryAllocationMax +" "+ ServerCleanup.class.getName();
			logger.info("Running server cleanup: "+ cmd);
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec( cmd );
			
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERR");            			
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUT");		
			errorGobbler.start();
			outputGobbler.start();			
			p = null;
			rt = null;	
		}
		catch(Exception e)
		{
			logger.error("Unexpected error occurred in ServerCleanup.", e);
		}			
	}
	
	private void runServerCleanup()
	{
		try
		{
			// Make sure the schemas have already been loaded
			if( SchemaDirectory.schemas == null ) {
				logger.info("Loading schemas in ServerCleanup.");
				SchemaDirectory.loadSchemas();
			}			
			
			// For each schema that has been loaded into the schemas hashmap			
			for (Iterator i = SchemaDirectory.schemas.keySet().iterator(); i.hasNext(); )
			{
				String schemaName = String.valueOf( i.next() );

				// Skip the BASE_SCHEMA
				if( schemaName.equals( Constants.BASE_SCHEMA ) == false )
				{
					logger.info("Performing server cleanup for "+ schemaName +" schema.");
					SchemaDirectory.setup(schemaName, this.getClass().getName());
					doServerCleanup();
					logger.info("Finished performing server cleanup for "+ schemaName +".");
					
					Schema schema = Schema.getSchema(schemaName);
					schema.setLastServerCleanupDt(new Date());
					schema.update();
					
					HibernateSession.closeSession();
				}			
			}		

		}
		catch(Exception e)
		{
			logger.error("Unexpected error occurred in ServerCleanup.", e);
		}		
		finally
		{
			try{
				HibernateSession.closeSession();
			}catch( HibernateException e)
			{ e.printStackTrace(); }
		}			
	}
	
	public void doServerCleanup() throws Exception
	{
		int daysBeforeCleanUp = getDaysBeforeCleanUp();
		int daysBeforeCleanUpLow = getDaysBeforeCleanUpLow();
		int daysBeforePlaybackEventCleanUp = getDaysBeforePlaybackEventCleanUp();
		int daysBeforePlaybackEventSummaryCleanUp = getDaysBeforePlaybackEventSummaryCleanUp();
		
		// Locate the presentations directory					
		String presentationsDir = KuvataConfig.getKuvataHome() +"/"+ Constants.SCHEMAS +"/"+ SchemaDirectory.getSchema().getSchemaName() +"/"+ Constants.PRESENTATIONS;
		String assetsDir = KuvataConfig.getKuvataHome() +"/"+ Constants.SCHEMAS +"/"+ SchemaDirectory.getSchema().getSchemaName() +"/"+ Constants.ASSETS_DIR;
		File f = new File( presentationsDir );
		if( f.exists() == false ) {
			logger.info("Could not locate presentations directory: \""+ presentationsDir +"\". Exiting ServerCleanup.");
			return;
		}
		if( f.canWrite() == false ) {
			logger.info("Could not write to presentations directory: \""+ presentationsDir +"\". Exiting ServerCleanup.");
			return;
		}
		
		// Subtract x number of days to get the usage date to test against
		Calendar minUsageDate = Calendar.getInstance();
		minUsageDate.add(Calendar.DAY_OF_MONTH, - daysBeforeCleanUp );
		
		// Subtract 1 day to get the usage date to test against
		Calendar minProcessDate = Calendar.getInstance();
		minProcessDate.add(Calendar.DAY_OF_MONTH, - DAYS_BEFORE_PROCESSING_OCCURS );		
		
		// Locate "old" presentation files and delete them
		logger.info("Cleaning up presentations: "+ presentationsDir);
		deletePresentations( presentationsDir, minUsageDate.getTime() );
		
		// Locate "old" asset files and delete them
		logger.info("Cleaning up assets: "+ assetsDir);		
		deleteAssets( assetsDir, minUsageDate.getTime() );		
		
		// Locate "old" schedule files and delete them		
		logger.info("Cleaning up schedules");
		deleteSchedules( daysBeforeCleanUp );
		
		// Clean up the content schedule event table
		logger.info("Cleaning up content_schedule_event table");
		contentScheduleEventCleanUp( daysBeforeCleanUp );
		
		// Clean up the heartbeat event table
		logger.info("Cleaning up heartbeat_event table");
		heartbeatEventCleanUp( daysBeforeCleanUp );
		
		// Clean up device command table
		logger.info("Cleaning device_command table");
		deviceCommandCleanUp( daysBeforeCleanUp );
		
		// Clean up server command table
		logger.info("Cleaning server_command table");
		serverCommandCleanUp( daysBeforeCleanUpLow );		
		
		// Clean up mcm_history table
		logger.info("Cleaning up mcm_history table");
		mcmHistoryCleanUp( daysBeforeCleanUp );
		
		// Clean up mcm_history table
		logger.info("Cleaning up self_test_history table");
		selfTestHistoryCleanUp( daysBeforeCleanUp );
		
		// Clean up content_scheduler_status table
		logger.info("Cleaning up content_scheduler_status table");
		contentSchedulerStatusCleanUp( daysBeforeCleanUp );		
		
		// Clean up the playback_event event table
		logger.info("Cleaning up playback_event table");
		playbackEventCleanUp( daysBeforePlaybackEventCleanUp );			
		
		// Clean up the playback_event event table
		logger.info("Cleaning up playback_event_summary table");
		playbackEventSummaryCleanUp( daysBeforePlaybackEventSummaryCleanUp );
		
		// Locate "old" uploaded log files and process or delete them
		logger.info("Processing uploaded files");
		processUploadedFiles( minProcessDate.getTime() );
		
		// Clean up the file_transmissions table
		logger.info("Cleaning up file_transmission table");
		fileTransmissionCleanUp( daysBeforeCleanUp );
		
		// Clean up the content_schedule table
		logger.info("Cleaning up content_schedule table");
		contentScheduleCleanUp( daysBeforeCleanUp );
		
		// Make sure the isLastHeartbeat records are sane
		logger.info("Cleaning up is_last_heartbeat records");
		cleanUpIsLastHeartbeatRecords();	
		
		// Delete any devices that are marked for deletion
		logger.info("Deleting devices marked for deletion");
		deleteDevices();	
	}

	private void deletePresentations(String directoryItem, Date minUsageDate)
	{
		try
		{
			File file = new File(directoryItem);
			if( file.isDirectory() ) 
			{
				String list[] = file.list();
				for (int i = 0; i < list.length; i++)
				{
					deletePresentations(directoryItem + File.separatorChar + list[i], minUsageDate);
				}
			}		
			else if( file.isFile() )
			{									
				// Get the file extension
			    String extension = "";
			    int dot = file.getAbsolutePath().lastIndexOf(".");
			    if( dot>0 && dot<file.getAbsolutePath().length() )
			    {
			        extension = file.getAbsolutePath().substring(dot);
			    }
			    
			    // If this is an xml file
			    if( extension.equalsIgnoreCase(".xml") )
			    {			
					// If the lastModifiedDate is prior to our minUsageDate
					if( file.lastModified() < minUsageDate.getTime() )
					{
						// Delete any ReferencedFiles and the presentation file itself
						deletePresentation( file, minUsageDate );
					}	
			    }
			}
			file = null;
		}
		catch(Exception e){
			logger.error( e );
		}					
	}
	
	/**
	 * Deletes the ReferencedFiles found in the given presentation file
	 * Delete the presentation.xml file itself
	 * 
	 * @param presentationFile
	 */
	private void deletePresentation(File presentationFile, Date minUsageDate) throws HibernateException		
	{
		try {
			// Parse the presentation xml file and locate any ReferencedFiles
			Document d = new Document( presentationFile );
			Elements es = d.getElements( new XPath("//object[@class=\""+ Constants.REFERENCED_FILE_CLASS +"\"]"));
			while(es.hasMoreElements())
			{
				Element objectElement = es.next();
				String referencedFileRelativePath = objectElement.getElement( new XPath("void/string") ).getTextString();
				
				// Append the presentations directory to the relative path			
				String referencedFileloc = KuvataConfig.getKuvataHome() + 
										"/"+ Constants.SCHEMAS +					
										"/"+ SchemaDirectory.getSchema().getSchemaName() +
										"/"+ Constants.PRESENTATIONS +
										"/"+ referencedFileRelativePath;
				referencedFileloc = referencedFileloc.replaceAll("\\\\", "/");
				
				// If this ReferencedFile exists, delete it
				File referencedFile = new File( referencedFileloc );
				if( referencedFile.exists() == true )
				{
					// If the lastModifiedDate is prior to our minUsageDate
					if( referencedFile.lastModified() < minUsageDate.getTime() )
					{
						referencedFile.delete();
					}
				}	
				referencedFile = null;
			}
			d = null;
		} catch (ParseException e) {
			// Even though this isn't ideal, we can ignore this error
		}
		
		// Delete the presentation file itself
		presentationFile.delete();	
		presentationFile = null;
	}
	
	private void deleteAssets(String directoryItem, Date minUsageDate)
	{
		try
		{
			File file = new File(directoryItem);
			if( file.isDirectory() ) 
			{
				String list[] = file.list();
				for (int i = 0; i < list.length; i++)
				{
					// Skip the thumbs directory
					if( list[i].equalsIgnoreCase("thumbs") == false ){
						deleteAssets(directoryItem + File.separatorChar + list[i], minUsageDate);
					}
				}
			}		
			else if( file.isFile() )
			{									
				// Get the file extension
			    String extension = "";
			    int dot = file.getAbsolutePath().lastIndexOf(".");
			    if( dot>0 && dot<file.getAbsolutePath().length() )
			    {
			        extension = file.getAbsolutePath().substring(dot);
			    }
			    
				// If the lastModifiedDate is prior to our minUsageDate
				if( file.lastModified() < minUsageDate.getTime() )
				{
					// Parse the assetId out of the filename
					String assetId = "";
					if( file.getAbsolutePath().indexOf(".") > 0 ){
						assetId = file.getAbsolutePath().substring( 0, file.getAbsolutePath().lastIndexOf(".") );
						if( assetId.indexOf("/") >= 0 ){
							assetId = assetId.substring( assetId.lastIndexOf("/") + 1 );
						}
						if( assetId.indexOf("-") > 0 ){
							assetId = assetId.substring( 0, assetId.lastIndexOf("-") );
							
							// If there is a "temp" file in this folder -- delete it
							// This could have happened if an exception occurred during file upload
							if( assetId.startsWith("temp") ){
								logger.info("Invalid asset file: "+ file.getAbsolutePath() +". Deleting file.");
								file.delete();
							}
							else
							{
								// Attempt to locate an asset with the given assetId
								Asset asset = Asset.getAsset( new Long( assetId ) );
								
								// If this file is not the currently referenced file
								if( asset == null || asset.getFileloc() == null || asset.getFileloc().equalsIgnoreCase( file.getAbsolutePath() ) == false )
								{
									// Delete the asset file
									logger.info("Deleting \"old\" asset file: "+ file.getAbsolutePath());
									file.delete();
									
									// Attempt to locate the "thumbs" version of this file
									String thumbnailFilename = file.getName().substring( 0, file.getName().lastIndexOf("." ) );
									thumbnailFilename += Asset.THUMB_EXT + file.getName().substring( file.getName().lastIndexOf(".") );
									String thumbnailPath = file.getAbsolutePath().substring( 0, file.getAbsolutePath().lastIndexOf("/") + 1 )+ Constants.THUMBS_DIR +"/"+ thumbnailFilename;
									
									logger.info("Does thumb exist: "+ thumbnailPath );
									File thumbFile = new File( thumbnailPath );
									if( thumbFile.exists() ){
										logger.info("Deleting \"old\" thumbnail file: "+ thumbFile.getAbsolutePath());
										thumbFile.delete();
									}								
								}								
							}
						}
					}
					
				}	
			}
			file = null;
		}
		catch(Exception e){
			logger.error( e );
		}		
	}	
	
	private void deleteSchedules(int daysBeforeCleanUp)
	{
		try
		{
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
	    	String schedulesDir = KuvataConfig.getKuvataHome() + 
								"/"+ Constants.SCHEMAS +					
								"/"+ SchemaDirectory.getSchema().getSchemaName() +
								"/"+ Constants.SCHEDULES;
	    	
	    	File f = new File( schedulesDir );       
			File[] files = f.listFiles();
			for(int i=0; i<files.length; i++)
			{
				// If this is a file and not a directory
				if( files[i].isFile() )
				{
					// If this is a valid schedule file
					if( files[i].getName().endsWith(".xml") && files[i].getName().indexOf("-") > 0 )
					{					
						// Parse the date out of the filename
						String datePart = files[i].getName();
						if( datePart.indexOf("-") > 0 ){
							datePart = datePart.substring( datePart.indexOf("-") + 1 );
							if( datePart.indexOf("-") > 0 ){
								datePart = datePart.substring( 0, datePart.lastIndexOf("-") );    				
					        	Date scheduleDate = dateTimeFormat.parse( datePart );
					        	
					        	// Subtract x number of days to get the usage date to test against
								Calendar minUsageDate = Calendar.getInstance();
								minUsageDate.add(Calendar.DAY_OF_MONTH, - (new Integer(daysBeforeCleanUp).intValue()) );
								
								// If the scheduleDate is prior to our minUsageDate
								if( scheduleDate.getTime() < minUsageDate.getTime().getTime() )
								{
									// Delete the file
									files[i].delete();
								}
							}
						}
					}else{
						// Invalid file -- remove it from the schedules dir
						logger.info("Invalid schedule file: "+ files[i].getName() +". Deleting file." );
						files[i].delete();
					}
				}
			}    
		}
		catch(Exception e){
			logger.error( e );
		}					
	}
	
	/**
	 * Delete "old" rows from the content schedule event table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void contentScheduleEventCleanUp(int daysBeforeCleanUp)
	{
		try{		
			String deleteSql = "DELETE FROM content_schedule_event WHERE start_datetime < ?";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}				
	}
	
	/**
	 * Delete "old" rows from the heartbeat_event table,
	 * however do not delete any rows where is_last_heartbeat = 1 
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void heartbeatEventCleanUp(int daysBeforeCleanUp)
	{
		try{
			String deleteSql = "DELETE FROM heartbeat_event WHERE dt < ? and is_last_heartbeat != 1";				
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );		
		}
		catch(Exception e){
			logger.error( e );
		}		
	}	
	
	/**
	 * Delete "old" rows from the device_command table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void deviceCommandCleanUp(int daysBeforeCleanUp)
	{
		try{
			String deleteSql = "DELETE FROM device_command "
				+ "WHERE create_dt < ? "
				+ "AND status IN ('"+ StatusType.SUCCESS +"','"+ StatusType.FAILED +"','"+ StatusType.CANCELLED +"')";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
			
			deleteSql = "DELETE FROM device_command "
				+ "WHERE create_dt < ? "
				+ "AND command NOT IN ('propertyChange', 'installDeviceRelease', 'installScript')";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}					
	}	
	
	/**
	 * Delete "old" rows from the server_command table
	 * that have a status of either Success, Failed, or Cancelled
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void serverCommandCleanUp(int daysBeforeCleanUp)
	{
		try{							 
			String deleteSql = "DELETE FROM server_command "
				+"WHERE create_dt < ? "
			+ "AND status IN ('"+ StatusType.SUCCESS +"','"+ StatusType.FAILED +"','"+ StatusType.CANCELLED +"')";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}					
	}		
	
	/**
	 * Delete "old" rows from the mcm_history table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void mcmHistoryCleanUp(int daysBeforeCleanUp)
	{
		try{	
			String deleteSql = "DELETE FROM mcm_history "
				+"WHERE timestamp < ? and is_last_mcm_history != 1";		
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );		
		}
		catch(Exception e){
			logger.error( e );
		}				
	}
	
	/**
	 * Delete "old" rows from the self_test_history table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void selfTestHistoryCleanUp(int daysBeforeCleanUp)
	{
		try{	
			String deleteSql = "DELETE FROM self_test_history WHERE dt < ?";		
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );		
		}
		catch(Exception e){
			logger.error( e );
		}				
	}
	
	/**
	 * Deletes any "old" files that were uploaded
	 * 
	 * @param minUsageDate
	 * @throws HibernateException
	 * @throws ParseException
	 */
	private void processUploadedFiles(Date minUsageDate)
	{
		try
		{
	    	String mcmDir = KuvataConfig.getKuvataHome() + "/"+ Constants.SCHEMAS + "/"+ SchemaDirectory.getSchema().getSchemaName() +"/"+ DispatcherConstants.UPLOADS_DIRECTORY;
			File f = new File( mcmDir );       
			File[] files = f.listFiles();
			for(int i=0; i<files.length; i++)
			{
				// If this is a file and not a directory
				if( files[i].isFile() )
				{							
					// If the lastModifiedDate is prior to our minUsageDate
					if( files[i].lastModified() < minUsageDate.getTime() )
					{
						// If this is an advanced_properties or screenshot file -- delete it
						// NOTE: As of 3.2, advanced_properties.xml files are no longer being uploaded.
						// Leaving this block for backward compatibility.
						if( files[i].getName().indexOf( DeviceProperty.ADVANCED_PROPERTIES_FILENAME ) >= 0 
								|| files[i].getName().indexOf( DispatcherConstants.SCREENSHOT_FILENAME ) >= 0 )
						{
							// Delete the file
							files[i].delete();
						}
						else
						{
							// Process the uploaded file
							ServerCommand.processUploadFile( files[i] );						
						}
					}	
			    }
			}
		}
		catch(Exception e){
			logger.error( e );
		}					
	}		
	
	/**
	 * Delete "old" rows from the content_scheduler_status table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void contentSchedulerStatusCleanUp(int daysBeforeCleanUp)
	{
		try{	
			String deleteSql = "DELETE FROM content_scheduler_status WHERE dt < ?";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}					
	}			
	
	/**
	 * Delete "old" rows from the playback_event table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void playbackEventCleanUp(int daysBeforeDatabaseCleanUp)
	{
		try{
			Schema schema = Schema.getSchema(SchemaDirectory.getSchemaName());
			long lastAggregatedId = schema.getLastAggregatedId() != null ? schema.getLastAggregatedId() : 0;
			String deleteSql = "DELETE FROM playback_event WHERE start_datetime < ? AND playback_event_id <= " + lastAggregatedId;
			doBulkDbDelete( daysBeforeDatabaseCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}					
	}	
	
	/**
	 * Delete "old" rows from the playback_event_summary table
	 *
	 */
	private void playbackEventSummaryCleanUp(int daysBeforeDatabaseCleanUp)
	{
		try{	
			String deleteSql = "DELETE FROM playback_event_summary WHERE start_datetime < ?";
			doBulkDbDelete( daysBeforeDatabaseCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}
	}	
	
	/**
	 * Delete "old" rows from the file_transmission table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void fileTransmissionCleanUp(int daysBeforeCleanUp)
	{
		try{
			String deleteSql = "DELETE FROM cs_file_transmission WHERE file_transmission_id IN (SELECT file_transmission_id FROM file_transmission WHERE dt < ? AND status != 'Exists')";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
			
			deleteSql = "DELETE FROM file_transmission WHERE dt < ? AND status != 'Exists'";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}					
	}
	
	/**
	 * Delete "old" rows from the content_schedule table
	 * 
	 * @param daysBeforeContentScheduleEventCleanUp
	 */
	private void contentScheduleCleanUp(int daysBeforeCleanUp)
	{
		try{
			String deleteSql = "DELETE FROM cs_file_transmission WHERE content_schedule_id IN (SELECT content_schedule_id FROM content_schedule WHERE server_end_dt < ?)";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
			
			deleteSql = "DELETE FROM content_schedule WHERE server_end_dt < ?";
			doBulkDbDelete( daysBeforeCleanUp, deleteSql );
		}
		catch(Exception e){
			logger.error( e );
		}					
	}
	
	private void doBulkDbDelete(int daysBeforeCleanUp, String deleteSql) throws SQLException
	{
	   	// Subtract x number of days to get the clean up date
		Calendar calCleanUpDate = Calendar.getInstance();
		calCleanUpDate.add(Calendar.DAY_OF_MONTH, - (daysBeforeCleanUp) );
		java.sql.Date cleanUpDate = new java.sql.Date( calCleanUpDate.getTimeInMillis() );
		
		// Keep going until our delete SQL does not return any records
		Session session = HibernateSession.currentSession();	
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();

		// It is necessary to set this flag in order to avoid the "You cannot commit with autocommit set!" error
		conn.setAutoCommit( false );
		while( true )
		{
			// Delete rows using bound parameters
			String sql = deleteSql +" and rownum <= "+ DELETE_BATCH_SIZE;
				
			// Build the prepared statement and delete
			logger.info("Cleaning up db: "+ sql );				
			PreparedStatement stmt = conn.prepareStatement( sql );
			stmt.setDate( 1, cleanUpDate );						
			int numRowsDeleted = stmt.executeUpdate();
			stmt.close();
			conn.commit();

			// If the delete statement did not delete any rows -- stop trying to delete
			if( numRowsDeleted == 0 ){
				break;
			}
		}
		session.flush();
		session.clear();	
		conn.close();
	}
	
	private void cleanUpIsLastHeartbeatRecords() throws Exception
	{
		// Query the heartbeat_event table for any devices that have more than one is_last_heartbeat row
		Session session = HibernateSession.currentSession();
		String hql = "SELECT he.deviceId FROM HeartbeatEvent he "
			+"WHERE he.isLastHeartbeat = 1 "
			+"GROUP BY he.deviceId "
			+"HAVING COUNT(he) > 1";						
		Query q = session.createQuery( hql );
		
		// For each device that has more than one row with is_last_heartbeat = 1  
		HibernateSession.beginTransaction();
		for( Iterator i=q.list().iterator(); i.hasNext(); )
		{
			// Select all rows for this device with is_last_heartbeat = 1 
			// except for the most recent one
			Long deviceId = (Long)i.next();
			String hql2 = "SELECT he FROM HeartbeatEvent he "
				+ "WHERE he.isLastHeartbeat = 1 "
				+ "AND he.deviceId = :deviceId "
				+ "AND he.heartbeatEventId != "
				+ "(SELECT MAX(heartbeatEvent.heartbeatEventId) FROM HeartbeatEvent heartbeatEvent "
				+ "WHERE heartbeatEvent.isLastHeartbeat = 1 "
				+ "AND heartbeatEvent.deviceId = :deviceId)";
			Query q2 = session.createQuery( hql2 );
			q2.setParameter( "deviceId", deviceId );			
			for( Iterator j=q2.list().iterator(); j.hasNext(); )
			{
				HeartbeatEvent he = (HeartbeatEvent)j.next();
				he.setIsLastHeartbeat( Boolean.FALSE );
				logger.info("Cleaning up isLastHeartbeat record: "+ he.getHeartbeatEventId());
				session.update( he );
			}			
		}
		HibernateSession.commitTransaction();
	}
	
	/**
	 * Deletes any devices that are marked for deletion
	 */
	private void deleteDevices(){
		// Get a list of all devices that are marked for deletion
		List<Device> devicesToDelete = Device.getDevicesToDelete();
		for( Iterator<Device> i = devicesToDelete.iterator(); i.hasNext(); ){
			Device deviceToDelete = i.next();
			
			// Create a job to delete this device from the job server
			Job.createJob(JobType.DELETE_DEVICE, deviceToDelete.getDeviceId().toString(), null);
		}
	}
	
	/**
	 * Attempts to retrieve the Dispatcher.daysBeforeCleanUp. If not found, return a default value of 14 days.
	 * @return
	 */
	public static int getDaysBeforeCleanUp()
	{
		int daysBeforeCleanUp = 14; // Default
		try	{				
			// Retrieve the daysBeforeCleanUp property
			daysBeforeCleanUp = new Integer(KuvataConfig.getPropertyValue("Dispatcher.daysBeforeCleanUp")).intValue();
		}
		catch(KmfException kmfe)		{
			logger.info("Could not locate property \"Dispatcher.daysBeforeCleanUp\". Using default value: "+ daysBeforeCleanUp);
		}
		return daysBeforeCleanUp;
	}
	
	/**
	 * Attempts to retrieve the Dispatcher.daysBeforeCleanUp.low. If not found, return a default value of 2 days.
	 * @return
	 */
	public static int getDaysBeforeCleanUpLow()
	{
		int daysBeforeCleanUpLow = 2; // Default
		try	{				
			// Retrieve the daysBeforeCleanUp property
			daysBeforeCleanUpLow = new Integer(KuvataConfig.getPropertyValue("Dispatcher.daysBeforeCleanUp.low")).intValue();
		}
		catch(KmfException kmfe)		{
			logger.info("Could not locate property \"Dispatcher.daysBeforeCleanUp.low\". Using default value: "+ daysBeforeCleanUpLow);
		}
		return daysBeforeCleanUpLow;
	}
	
	/**
	 * Attempts to retrieve the Dispatcher.daysBeforePlaybackEventCleanUp. If not found, return a default value of 7 days.
	 * @return
	 */
	public static int getDaysBeforePlaybackEventCleanUp()
	{
		int daysBeforePlaybackEventCleanUp = 7; // Default
		try	{				
			// Retrieve the daysBeforeContentScheduleEventCleanUp property
			daysBeforePlaybackEventCleanUp = new Integer(KuvataConfig.getPropertyValue("Dispatcher.daysBeforePlaybackEventCleanUp")).intValue();
		}
		catch(KmfException kmfe)		{
			logger.info("Could not locate property \"Dispatcher.daysBeforePlaybackEventCleanUp\". Using default value: "+ daysBeforePlaybackEventCleanUp);
		}
		return daysBeforePlaybackEventCleanUp;
	}
	
	/**
	 * Attempts to retrieve the Dispatcher.daysBeforePlaybackEventSummaryCleanUp. If not found, return a default value of 40 days.
	 * @return
	 */
	public static int getDaysBeforePlaybackEventSummaryCleanUp()
	{
		int daysBeforePlaybackEventSummaryCleanUp = 65; // Default
		try	{				
			// Retrieve the daysBeforeContentScheduleEventCleanUp property
			daysBeforePlaybackEventSummaryCleanUp = new Integer(KuvataConfig.getPropertyValue("Dispatcher.daysBeforePlaybackEventSummaryCleanUp")).intValue();
		}
		catch(KmfException kmfe)		{
			logger.info("Could not locate property \"Dispatcher.daysBeforePlaybackEventCleanUp\". Using default value: "+ daysBeforePlaybackEventSummaryCleanUp);
		}
		return daysBeforePlaybackEventSummaryCleanUp;
	}
	
	public static int getDaysBeforeCleanUpForCurrentReportsSourceTable()
	{
		// Determine which table to generate the reports off of
		String reportsSourceTable = Constants.REPORTS_SOURCE_TABLE_DEFAULT;
		try{
			reportsSourceTable = KuvataConfig.getPropertyValue( Constants.REPORTS_SOURCE_TABLE );
		}catch(KmfException e){
			logger.info("Could not locate property: "+ Constants.REPORTS_SOURCE_TABLE +". Using default: "+ Constants.REPORTS_SOURCE_TABLE_DEFAULT);
		}
		
		if(reportsSourceTable.equalsIgnoreCase(PlaybackEvent.class.getSimpleName())){
			return getDaysBeforePlaybackEventCleanUp();
		}else if(reportsSourceTable.equalsIgnoreCase(PlaybackEventSummary.class.getSimpleName())){
			return getDaysBeforePlaybackEventSummaryCleanUp();
		}else{
			return getDaysBeforeCleanUp();
		}
	}
	
    /**
     * For testing purposes only 
     */
    public static void main(String[] args)
    {
		// Load the schemas
		SchemaDirectory.initialize("kuvata", "Server Cleanup", null, false, true );
    	new ServerCleanup().runServerCleanup();
    }	
}
