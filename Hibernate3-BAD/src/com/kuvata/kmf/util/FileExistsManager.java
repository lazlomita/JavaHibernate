package com.kuvata.kmf.util;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;
import parkmedia.usertype.DeviceCommandType;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class FileExistsManager extends Thread {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( FileExistsManager.class );
	private String schemaName;
	private int fileExistsUploadFrequency;
	
	public FileExistsManager(String schemaName, int fileExistsUploadFrequency)
	{
		this.schemaName = schemaName;
		this.fileExistsUploadFrequency = fileExistsUploadFrequency;
	}
	
	/**
	 * Launches the thread that will send a CREATE_FILE_EXISTS device command
	 * to each device on a calculated interval. This will in turn cause the
	 * device to send up it's definitive list of files to the server. 
	 *
	 */
	public static void doFileExistsManagement()
	{
		try
		{
			// Make sure the schemas have already been loaded
			if( SchemaDirectory.schemas != null ) 
			{
				// For each schema that has been loaded into the schemas hashmap			
				for (Iterator i = SchemaDirectory.schemas.keySet().iterator(); i.hasNext(); )
				{
					String schemaName = (String)i.next();

					// Skip the BASE_SCHEMA
					if( schemaName.equals( Constants.BASE_SCHEMA ) == false )
					{
						// Attempt to retrieve the fileExistsUploadFrequency property
						String fileExistsUploadFrequency = KuvataConfig.getPropertyValue( "Dispatcher.fileExistsUploadFrequency", true );
						if( fileExistsUploadFrequency != null && fileExistsUploadFrequency.length() > 0 )
						{
							logger.info("Launching file exists manager thread for for "+ schemaName +" schema.");
							FileExistsManager fileExistsManager = new FileExistsManager( schemaName, new Integer( fileExistsUploadFrequency ).intValue() );
							fileExistsManager.start();							
						}
						else{
							logger.info("Could not launch file exists manager because the fileExistsUploadFrequency could not be found.");
						}						
					}			
				}	
			} else {
				logger.info("Could not launch the FileExistsManager because no schemas have been loaded.");
			}
		}
		catch(Exception e) {
			logger.error("Unexpected error occurred in FileExistsManager.", e);
		} finally {
			try{
				HibernateSession.closeSession();
			}catch( HibernateException e)
			{ e.printStackTrace(); }
		}				
	}
	
	/**
	 * Sends each device a CREATE_FILE_EXISTS device command on an interval calculated as follows:
	 * Calculate the amount of time to sleep based on:
	 * the  number of times per day we want to receive the file_exists file from the device and
	 * the number of devices that will be uploading the file.
	 * This is done in order ensure that all devices do not upload thier file_exists file at the same time,
	 * but instead are equally spread through each day.
	 */
	public void run()
	{
		SchemaDirectory.setup(this.schemaName, this.getClass().getName());
		Session session = HibernateSession.currentSession();		
		
		// Keep this thread alive
		while( true )
		{
			/*
			 * Get a list of devices ordered by lastFileExistsDt ascending.
			 * First, get a list of all devices that do not yet have a lastFileExistsDt.
			 * Then, get a list of all devices that do have a lastFileExistsDt, ordered by
			 * lastFileExistsDt ascending and add the results together.
			 */
			String hql = "SELECT device "
				+ "FROM Device device "
				+ "WHERE device.lastFileExistsDt IS NULL";
			List l = session.createQuery( hql ).list();			
			hql = "SELECT device "
				+ "FROM Device device "
				+ "WHERE device.lastFileExistsDt IS NOT NULL "
				+ "ORDER BY device.lastFileExistsDt";
			List l2 = session.createQuery( hql ).list();
			l.addAll( l2 );
			for( Iterator i=l.iterator(); i.hasNext(); )
			{
				try 
				{					
					Device device = (Device)i.next();
					logger.info("Sending createFileExists device command to "+ device.getDeviceName());
					device.addDeviceCommand( DeviceCommandType.CREATE_FILE_EXISTS, "", false );
					
					logger.info("Sending uploadDeviceProperties device command to "+ device.getDeviceName());
					device.addDeviceCommand( DeviceCommandType.UPLOAD_DEVICE_PROPERTIES, "", false );
					
					// Calculate the amount of time to sleep based on:
					// the  number of times per day we want to receive the file_exists file from the device and
					// the number of devices that will be uploading the file.
					// This is done in order ensure that all devices do not upload thier file_exists file at the same time,
					// but instead are equally spread through each day.
					long sleepTime = 86400 / this.fileExistsUploadFrequency / l.size();
					logger.info("Sleeping for: "+ sleepTime +" seconds in FileExistsManager.");
					Thread.sleep( sleepTime * 1000 );
				} 
				catch (Exception e) {
					logger.error( e );
				} 
			}
		}
	}

}
