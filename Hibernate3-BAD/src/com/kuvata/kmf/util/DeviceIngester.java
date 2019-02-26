package com.kuvata.kmf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;
import parkmedia.usertype.AssetIngesterColumnType;
import parkmedia.usertype.DeviceIngesterColumnType;
import parkmedia.usertype.DirtyType;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.DeviceIngesterStatus;
import com.kuvata.kmf.Dirty;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmm.services.MediaManageable;
import com.kuvata.kmm.services.MediaManageableServiceLocator;

public class DeviceIngester {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( DeviceIngester.class );	
	private StringBuffer status = null;
	private String mediaManagerServiceUrl = null;
	private DeviceIngesterStatus dis = null;
	private AppUser appUser = null;		
	private boolean ignoreDuplicates = false;
	private ArrayList metadataFields = new ArrayList();
	
	public DeviceIngester(AppUser appUser, String duplicatesMode, String controlFilepath)
	{
		// Create the deviceIngesterStatus object so we can update its status throughout the process
		this.dis = new DeviceIngesterStatus();
		this.dis.setDt( new Date() );		
		this.dis.save();
		appendStatusMessage("Device Ingester output:\n\n", true);
		
		// Now that we've created the device ingester status object, update the dirty object 
		// so we know to stop displaying the "initializing" message in DeviceIngesterAction
		// This really should only return one record only, but iterate through nevertheless
		List dirtyDeviceIngesters = Dirty.getDirtyEntities( DirtyType.DEVICE_INGESTER );
		for( Iterator i=dirtyDeviceIngesters.iterator(); i.hasNext(); )
		{
			Dirty d = (Dirty)i.next();
			d.setDirtyEntityId( dis.getDeviceIngesterStatusId() );
			d.update();
		}
		
		if( duplicatesMode.equalsIgnoreCase("ignore") ){
			this.ignoreDuplicates = true;	
		}
		this.appUser = appUser;		
				
		try
		{
			// Retrieve the url to the MediaManagerService
			this.mediaManagerServiceUrl = KuvataConfig.getMediaManagerServiceUrl();

			// Perform the device ingestion
			doDeviceIngestion( controlFilepath );
		}
		catch(Exception e){			
			logger.error("An unexcepted error occurred in DeviceIngester. Exiting.");
		}	
		finally
		{
			logger.info( status.toString() );
			this.dis.setStatus( Hibernate.createClob( status.toString() ) );
			this.dis.update();
		}
	}

	private void doDeviceIngestion( String controlFilepath )
	{		
		try
		{
			// Make sure the control file exists
			File controlFile = new File( controlFilepath );
			if( controlFile.exists() )
			{
				// Parse the csv file (NOTE: Excel specific!?!)
				InputStream is = new FileInputStream( controlFile );
				LabeledCSVParser lcsvp = new LabeledCSVParser( new ExcelCSVParser( is, ',' ) );

				// First, make sure the required columns were included
				if( verifyColumnHeaders( lcsvp ) )
				{
					// Attempt to login the user and instantiate the http session
					if( validateSession( this.appUser.getName(), this.appUser.getPassword()) )
					{
						// Build the headers line
						String[] rawDataLine = lcsvp.getLabels();
						StringBuffer sb = new StringBuffer();
						for( int i=0; i<rawDataLine.length; i++ ){
							if( rawDataLine[i] != null ){
								sb.append( rawDataLine[i].trim() );	
							}
							
							if(i+1 < rawDataLine.length){
								sb.append( ", " );
							}
						}
						
						appendStatusMessage( "     HEADERS: " + sb.toString() + "\n", true );
						
						rawDataLine = lcsvp.getLine(); 						
						while( rawDataLine != null )
						{
							handleCSVLine( lcsvp, rawDataLine );	
							rawDataLine = lcsvp.getLine(); 
						}							
					}
					else
					{
						appendStatusMessage("Could not validate user: "+ this.appUser.getName() +"/"+ this.appUser.getPassword() +". Exiting.", true);
					}
				}			
			}
			else
			{
				appendStatusMessage("Could not locate control file: "+ controlFilepath +". Exiting.", true);
			}	
			appendStatusMessage("\nFinished.\n", true);
		}
		catch(Exception e)
		{
			appendStatusMessage("An unexpected error occurred in DeviceIngester.doDeviceIngestion(): "+ e.toString(), true);
			logger.error("An unexpected error occurred in DeviceIngester.doDeviceIngestion(): "+ e.toString(), e);
		}
	}
	
	private void handleCSVLine(LabeledCSVParser csvInputLine, String[] rawDataLine) throws ServiceException, RemoteException
	{		
		String name = csvInputLine.getValueByLabel( DeviceIngesterColumnType.NAME.toString() );
		String macAddress = csvInputLine.getValueByLabel( DeviceIngesterColumnType.MAC_ADDRESS.toString() );
		String csHorizon = csvInputLine.getValueByLabel( DeviceIngesterColumnType.CS_HORIZON.toString() );
		String csLength = csvInputLine.getValueByLabel( DeviceIngesterColumnType.CS_LENGTH.toString() );
		String deliveryMode = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DELIVERY_MODE.toString() );
		String licenseStatus = csvInputLine.getValueByLabel( DeviceIngesterColumnType.LICENSE_STATUS.toString() );
		String applyAlerts = csvInputLine.getValueByLabel( DeviceIngesterColumnType.APPLY_ALERTS.toString() );
		String autoUpdate = csvInputLine.getValueByLabel( DeviceIngesterColumnType.AUTO_UPDATE.toString() );
		String display = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DISPLAY.toString() );
		String outputMode = csvInputLine.getValueByLabel( DeviceIngesterColumnType.OUTPUT_MODE.toString() );
		String resolution = csvInputLine.getValueByLabel( DeviceIngesterColumnType.RESOLUTION.toString() );
		String orientation = csvInputLine.getValueByLabel( DeviceIngesterColumnType.ORIENTATION.toString() );
		String scalingMode = csvInputLine.getValueByLabel( DeviceIngesterColumnType.SCALING_MODE.toString() );
		String timezone = csvInputLine.getValueByLabel( DeviceIngesterColumnType.TIMEZONE.toString() );
		String edgeServer = csvInputLine.getValueByLabel( DeviceIngesterColumnType.EDGE_SERVER.toString() );
		String activeMirrorSource = csvInputLine.getValueByLabel( DeviceIngesterColumnType.ACTIVE_MIRROR_SOURCE.toString() );
		String mirrorSourceDevices = csvInputLine.getValueByLabel( DeviceIngesterColumnType.MIRROR_SOURCE_DEVICES.toString() );
		String heartbeatInterval = csvInputLine.getValueByLabel( DeviceIngesterColumnType.HEARTBEAT_INTERVAL.toString() );
		String maxFileStorage = csvInputLine.getValueByLabel( DeviceIngesterColumnType.MAX_FILE_STORAGE.toString() );
		String memoryThreshold = csvInputLine.getValueByLabel( DeviceIngesterColumnType.MEMORY_THRESHOLD.toString() );
		String iowaitThreshold = csvInputLine.getValueByLabel( DeviceIngesterColumnType.IOWAIT_THRESHOLD.toString() );
		String cpuThreshold = csvInputLine.getValueByLabel( DeviceIngesterColumnType.CPU_THRESHOLD.toString() );
		String loadThreshold = csvInputLine.getValueByLabel( DeviceIngesterColumnType.LOAD_THRESHOLD.toString() );		
		String screenshotUploadTime = csvInputLine.getValueByLabel( DeviceIngesterColumnType.SCREENSHOT_UPLOAD_TIME.toString() );
		String volume = csvInputLine.getValueByLabel( DeviceIngesterColumnType.VOLUME.toString() );
		String lcdPin = csvInputLine.getValueByLabel( DeviceIngesterColumnType.LCD_PIN.toString() );
		String lcdBranding = csvInputLine.getValueByLabel( DeviceIngesterColumnType.LCD_BRANDING.toString() );
		String dhcpEnabled = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DHCP_ENABLED.toString() );				
		String ipAddress = csvInputLine.getValueByLabel( DeviceIngesterColumnType.IP_ADDRESS.toString() );
		String connectAddress = csvInputLine.getValueByLabel( DeviceIngesterColumnType.CONNECT_ADDRESS.toString() );
		String netmask = csvInputLine.getValueByLabel( DeviceIngesterColumnType.NETMASK.toString() );
		String gateway = csvInputLine.getValueByLabel( DeviceIngesterColumnType.GATEWAY.toString() );
		String dnsServer = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DNS_SERVER.toString() );
		String networkInterface = csvInputLine.getValueByLabel( DeviceIngesterColumnType.NETWORK_INTERFACE.toString() );				
		String filesyncStartTime = csvInputLine.getValueByLabel( DeviceIngesterColumnType.FILESYNC_START_TIME.toString() );
		String filesyncEndTime = csvInputLine.getValueByLabel( DeviceIngesterColumnType.FILESYNC_END_TIME.toString() );
		String createDefaultMcm = csvInputLine.getValueByLabel( DeviceIngesterColumnType.CREATE_DEFAULT_MCM.toString() );
		String templateDevice = csvInputLine.getValueByLabel( DeviceIngesterColumnType.TEMPLATE_DEVICE.toString() );
		String roles = csvInputLine.getValueByLabel( DeviceIngesterColumnType.ROLES.toString() );
		String deviceGroups = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DEVICE_GROUPS.toString() );
		String dispatcherServers = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DISPATCHER_SERVERS.toString() );
		String redirectGateway = csvInputLine.getValueByLabel( DeviceIngesterColumnType.REDIRECT_GATEWAY.toString() );
		String xOffset = csvInputLine.getValueByLabel( DeviceIngesterColumnType.X_OFFSET.toString() );
		String yOffset = csvInputLine.getValueByLabel( DeviceIngesterColumnType.Y_OFFSET.toString() );
		String zoom = csvInputLine.getValueByLabel( DeviceIngesterColumnType.ZOOM.toString() );
		String antivirusScan = csvInputLine.getValueByLabel( DeviceIngesterColumnType.ANTIVIRUS_SCAN_TIME.toString() );
		String downloadPriority = csvInputLine.getValueByLabel( DeviceIngesterColumnType.DOWNLOAD_PRIORITY.toString() );
		String audioNormalization = csvInputLine.getValueByLabel( DeviceIngesterColumnType.AUDIO_NORMALIZATION.toString() );
		String segments = csvInputLine.getValueByLabel( DeviceIngesterColumnType.SEGMENTS.toString() );
		String venueName = csvInputLine.getValueByLabel( DeviceIngesterColumnType.VENUE_NAME.toString() );
		String alphaCompositing = csvInputLine.getValueByLabel( DeviceIngesterColumnType.ALPHA_COMPOSITING.toString() );
		String bandwidthLimit = csvInputLine.getValueByLabel( DeviceIngesterColumnType.BANDWIDTH_LIMIT.toString() );
		String audioConnection = csvInputLine.getValueByLabel( DeviceIngesterColumnType.AUDIO_CONNECTION.toString() );
		String framesync = csvInputLine.getValueByLabel( DeviceIngesterColumnType.FRAMESYNC.toString() );
		String removeDevice = csvInputLine.getValueByLabel( DeviceIngesterColumnType.REMOVE_DEVICE.toString() );
		String type2VideoPlayback = csvInputLine.getValueByLabel( DeviceIngesterColumnType.TYPE2_VIDEO_PLAYER.toString() );
		String useChrome = csvInputLine.getValueByLabel( DeviceIngesterColumnType.USE_CHROME.toString() );
		String chromeDisableGpu = csvInputLine.getValueByLabel( DeviceIngesterColumnType.CHROME_DISABLE_GPU.toString() );

		name = name != null ? name.trim() : null;
		macAddress = macAddress != null ? macAddress.trim() : null;
		csHorizon = csHorizon != null ? csHorizon.trim() : null;
		csLength = csLength != null ? csLength.trim() : null;
		deliveryMode = deliveryMode != null ? deliveryMode.trim() : null;
		licenseStatus = licenseStatus != null ? licenseStatus.trim() : null;
		applyAlerts = applyAlerts != null ? applyAlerts.trim() : null;
		autoUpdate = autoUpdate != null ? autoUpdate.trim() : null;		
		display = display != null ? display.trim() : null;
		outputMode = outputMode != null ? outputMode.trim() : null;
		resolution = resolution != null ? resolution.trim() : null;
		orientation = orientation != null ? orientation.trim() : null;
		scalingMode = scalingMode != null ? scalingMode.trim() : null;		
		timezone = timezone != null ? timezone.trim() : null;
		edgeServer = edgeServer != null ? edgeServer.trim() : null;
		activeMirrorSource = activeMirrorSource != null ? activeMirrorSource.trim() : null;
		mirrorSourceDevices = mirrorSourceDevices != null ? mirrorSourceDevices.trim() : null;		
		heartbeatInterval = heartbeatInterval != null ? heartbeatInterval.trim() : null;
		maxFileStorage = maxFileStorage != null ? maxFileStorage.trim() : null;		
		memoryThreshold = memoryThreshold != null ? memoryThreshold.trim() : null;		
		iowaitThreshold = iowaitThreshold != null ? iowaitThreshold.trim() : null;
		cpuThreshold = cpuThreshold != null ? cpuThreshold.trim() : null;
		loadThreshold = loadThreshold != null ? loadThreshold.trim() : null;
		screenshotUploadTime = screenshotUploadTime != null ? screenshotUploadTime.trim() : null;
		volume = volume != null ? volume.trim() : null;		
		lcdPin = lcdPin != null ? lcdPin.trim() : null;
		lcdBranding = lcdBranding != null ? lcdBranding.trim() : null;		
		dhcpEnabled = dhcpEnabled != null ? dhcpEnabled.trim() : null;
		ipAddress = ipAddress != null ? ipAddress.trim() : null;
		connectAddress = connectAddress != null ? connectAddress.trim() : null;
		netmask = netmask != null ? netmask.trim() : null;
		gateway = gateway != null ? gateway.trim() : null;
		dnsServer = dnsServer != null ? dnsServer.trim() : null;				
		networkInterface = networkInterface != null ? networkInterface.trim() : null;		
		filesyncStartTime = filesyncStartTime != null ? filesyncStartTime.trim() : null;
		filesyncEndTime = filesyncEndTime != null ? filesyncEndTime.trim() : null;
		createDefaultMcm = createDefaultMcm != null ? createDefaultMcm.trim() : null;
		templateDevice = templateDevice != null ? templateDevice.trim() : null;
		roles = roles != null ? roles.trim() : null;
		deviceGroups = deviceGroups != null ? deviceGroups.trim() : null;
		dispatcherServers = dispatcherServers != null ? dispatcherServers.trim() : null;
		redirectGateway = redirectGateway != null ? redirectGateway.trim() : null;
		xOffset = xOffset != null ? xOffset.trim() : null;
		yOffset = yOffset != null ? yOffset.trim() : null;
		zoom = zoom != null ? zoom.trim() : null;
		antivirusScan = antivirusScan != null ? antivirusScan.trim() : null;
		downloadPriority = downloadPriority != null ? downloadPriority.trim() : null;
		audioNormalization = audioNormalization != null ? audioNormalization.trim() : null;
		venueName = venueName != null ? venueName.trim() : null;
		alphaCompositing = alphaCompositing != null ? alphaCompositing.trim() : null;
		bandwidthLimit = bandwidthLimit != null ? bandwidthLimit.trim() : null;
		audioConnection = audioConnection != null ? audioConnection.trim() : null;
		framesync = framesync != null ? framesync.trim() : null;
		removeDevice = removeDevice != null ? removeDevice.trim() : null;
		type2VideoPlayback = type2VideoPlayback != null ? type2VideoPlayback.trim() : null;
		useChrome = useChrome != null ? useChrome.trim() : null;
		chromeDisableGpu = chromeDisableGpu != null ? chromeDisableGpu.trim() : null;
		
		// Build the string array for each of our "valid" metadata fields
		String[][] metadata = new String[ metadataFields.size() ][2];
		int counter = 0;
		for( Iterator<String> i=metadataFields.iterator(); i.hasNext(); )
		{
			String metadataLabel = i.next();
			metadata[ counter ][0] = metadataLabel;
			String value = csvInputLine.getValueByLabel( metadataLabel );
			value = value != null && value.trim().length() > 0 ? value.trim() : null;
			metadata[ counter++ ][1] = value;
		}
		
		// Execute the web service request to create or update the device
		MediaManageable mediaManageable = MediaManageableServiceLocator.getMediaManager( this.mediaManagerServiceUrl );
		String response;
		// If remove device is set to "true"
		if(Boolean.parseBoolean(removeDevice)){
			// Execute the web service request to create or update the device
		    response = mediaManageable.deleteDeviceByName(name);						
		} else {
			// Execute the web service request to create or update the device
		    response = mediaManageable.createOrUpdateDevice( name, macAddress, csLength, csHorizon, licenseStatus, deliveryMode, applyAlerts, autoUpdate,
		    		display, outputMode, resolution, orientation, scalingMode, timezone, edgeServer, redirectGateway, activeMirrorSource,
		    		mirrorSourceDevices, heartbeatInterval, maxFileStorage, memoryThreshold, iowaitThreshold, cpuThreshold, loadThreshold,
		    		screenshotUploadTime, volume, lcdPin, lcdBranding, dhcpEnabled, ipAddress, connectAddress, netmask, gateway, dnsServer,
		    		networkInterface, dispatcherServers, filesyncStartTime, filesyncEndTime, xOffset, yOffset, zoom, 
		    		templateDevice, createDefaultMcm, antivirusScan, downloadPriority, audioNormalization, roles, deviceGroups, segments, venueName, alphaCompositing,
		    		bandwidthLimit, audioConnection, framesync, type2VideoPlayback, useChrome, chromeDisableGpu, metadata, this.ignoreDuplicates );
		}

		
		// If we did not receive any warning or error messages from the web service -- show the success message
		if( response == null || response.length() == 0 ){
			response = Constants.SUCCESS.toUpperCase();
		}
		
		// Rebuild the rawDataLine
		StringBuffer sb = new StringBuffer();
		for( int i=0; i<rawDataLine.length; i++ ){
			if( rawDataLine[i] != null ){
				sb.append( rawDataLine[i].trim() );	
			}			
			sb.append( "," );
		}
		appendStatusMessage( response +"\n", false );
		appendStatusMessage( "     RAW DATA: "+ sb.toString() +"\n", true );
	}
	
	/**
	 * Verify that the required columns are present. Display warning messages
	 * for additional column headers that will be ignored.
	 * @param lcsvp
	 * @return
	 */
	private boolean verifyColumnHeaders(LabeledCSVParser lcsvp) throws IOException, ClassNotFoundException
	{
		// Display warning messages if "other" columns are found
		String[] labels = lcsvp.getLabels();
		for( int i=0; i<labels.length; i++ )
		{
			String label = labels[i];
			DeviceIngesterColumnType columnType = DeviceIngesterColumnType.getDeviceIngesterColumnType( label );
			if( columnType == null )
			{
				/*
				 * Attempt to locate this column as an device metadata field.
				 * If this label starts with the "METADATA:" prefix, chop it off
				 */
				String attrName = label.toUpperCase(); 
				if( attrName.indexOf( Constants.METADATA_PREFIX ) >= 0 ){
					attrName = attrName.substring( attrName.indexOf( Constants.METADATA_PREFIX ) + Constants.METADATA_PREFIX.length() ).trim();
				}
				
				// If we found an attrDefinition with the given name
				AttrDefinition ad = AttrDefinition.getAttributeDefinition( Device.class.getName(), attrName );
				if( ad != null )
				{
					// Add it to our list of metadata fields
					metadataFields.add( label );
				}				
				else
				{
					appendStatusMessage("WARNING: The following column is unknown and will be ignored: "+ label +"\n", true);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Calls the loginUser web service method to instantiate the http session
	 * that will be used for subsequent web service calls.
	 *  
	 * @param username
	 * @param password
	 */
	private boolean validateSession(String username, String password) throws Exception
	{
		// Unencrypt the password here because the loginUser web service method is expecting an unencrypted password
		TripleDES decrypter = new TripleDES();
		String unencryptedPassword = decrypter.decryptFromString( password );
		
		MediaManageable mediaManageable = MediaManageableServiceLocator.getMediaManager( this.mediaManagerServiceUrl );		
    	String response = mediaManageable.loginUser( username, unencryptedPassword );
		if( response.equalsIgnoreCase( Constants.TRUE ) ){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Set the DeviceIngester dirty status 
	 */
	public synchronized static void makeDirty() throws HibernateException
	{		
		// Create a new dirty object
		Dirty d = new Dirty();					
		d.setDirtyType( DirtyType.DEVICE_INGESTER );		
		d.save();
	}
	
	/**
	 * Removes all dirty objects of type DeviceIngester 
	 */
	public synchronized static void makeNotDirty() throws HibernateException
	{		
		List<Dirty> deviceIngesterDirties = Dirty.getDirtyEntities( DirtyType.DEVICE_INGESTER );	
		for( Iterator<Dirty> i=deviceIngesterDirties.iterator(); i.hasNext(); ){
			i.next().delete();
		}
	}	
	
	/**
	 * Returns true of false depending whether or not there is a 
	 * dirty object of type DeviceIngester for this object.
	 * 
	 * @return 
	 */
	public static boolean isDirty() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();
		String hql = "SELECT d "
					+ "FROM Dirty d "
					+ "WHERE d.dirtyType = '"+ DirtyType.DEVICE_INGESTER.getPersistentValue() + "'";
		Iterator<Dirty> i = session.createQuery( hql ).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}		
	
    public synchronized void appendStatusMessage(String msg, boolean saveMessage) throws HibernateException
    {
    	if( this.status == null ){
			this.status = new StringBuffer();
    	}
		this.status.append( msg );
		
		// If the flag to persist the message to the db was passed in
		if( saveMessage && this.dis != null )
		{			
			this.dis.setStatus( Hibernate.createClob( this.status.toString() ) );
			this.dis.update();
		}
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if( args.length != 5 ){			
			logger.error("Usage: java DeviceIngester username password duplicatesMode[ignore|modify] controlFilepath wasRunFromInterface[true|false]");
			logger.error("Args passed in: "+ args.length);
			for(int i=0; i<args.length; i++) {
				System.out.println("arg"+ i +"="+ args[i]);
			}
		}
		else
		{		
			String username = args[0];
			String password = args[1];
			String duplicatesMode = args[2];
			String controlFilepath = args[3];
			String wasRunFromInterface = args[4];
			
			try 
			{				
				SchemaDirectory.initialize( Constants.BASE_SCHEMA, "Device Ingester", null, false, true );
				
				// Authenticate the username/password
				AppUser appUser = AppUser.getAppUser( username, password );
				if( appUser != null )
				{
					// Switch to the schema associated with this appuser
					String schema = appUser.getSchema().getSchemaName();
					HibernateSession.closeSession();
					SchemaDirectory.initialize( schema, "Device Ingester", appUser.getAppUserId().toString(), true, true );
					new DeviceIngester( appUser, duplicatesMode, controlFilepath );
					logger.info("Finished Device Ingester");
				}
				else
				{
					logger.info("Unable to login user: "+ username +"/"+ password +". Exiting.");
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally
			{
				try {
					// If this program was launched from the interface, clear the dirty flags
					if( wasRunFromInterface != null && wasRunFromInterface.equalsIgnoreCase( Constants.TRUE ) )
					{
						// If we successfully switched to a database schema other than the BASE_SCHEMA -- clear the dirty flag
						if( SchemaDirectory.getSchemaName() != null && SchemaDirectory.getSchemaName().equalsIgnoreCase( Constants.BASE_SCHEMA ) == false ){
							DeviceIngester.makeNotDirty();
						}
					}
				} catch(Exception e) {			
					e.printStackTrace();
				}
				
				try {						
					HibernateSession.closeSession();
				} catch(HibernateException he) {			
					he.printStackTrace();
				}
			}	
		}

	}

}
