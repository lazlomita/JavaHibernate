package com.kuvata.kmf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.AssetIngesterColumnType;
import parkmedia.usertype.DirtyType;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.kuvata.StreamGobbler;
import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetIngesterStatus;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Dirty;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmm.services.MediaManageable;
import com.kuvata.kmm.services.MediaManageableServiceLocator;
import com.kuvata.kmm.services.MediaManagerServiceSoapBindingStub;

public class AssetIngester {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( AssetIngester.class );		
	private StringBuffer status = null;
	private String mediaManagerServiceUrl = null;
	private AssetIngesterStatus ais = null;
	private AppUser appUser = null;		
	private boolean ignoreDuplicates = false;
	private ArrayList metadataFields = new ArrayList();
	private String controlFilepath;
	private static String memoryAllocationMin = "64m";	// Default initial memory allocation 
	private static String memoryAllocationMax = "256m";	// Default maximum memory allocation
	
	public AssetIngester(AppUser appUser, String duplicatesMode, String controlFilepath)
	{
		// Create the assetIngesterStatus object so we can update its status throughout the process
		this.ais = new AssetIngesterStatus();
		this.ais.setDt( new Date() );		
		this.ais.save();
		appendStatusMessage("Asset Ingester output:\n\n", true);
		
		// Now that we've created the asset ingester status object, update the dirty object 
		// so we know to stop displaying the "initializing" message in AssetIngesterAction
		// This really should only return one record only, but iterate through nevertheless
		List dirtyAssetIngesters = Dirty.getDirtyEntities( DirtyType.ASSET_INGESTER );
		for( Iterator i=dirtyAssetIngesters.iterator(); i.hasNext(); )
		{
			Dirty d = (Dirty)i.next();
			d.setDirtyEntityId( ais.getAssetIngesterStatusId() );
			d.update();
		}
		
		if( duplicatesMode.equalsIgnoreCase("ignore") ){
			this.ignoreDuplicates = true;	
		}
		
		this.appUser = appUser;
		this.controlFilepath = controlFilepath.replaceAll("\\\\", "/");;
				
		try
		{
			// Retrieve the url to the MediaManagerService
			this.mediaManagerServiceUrl = KuvataConfig.getMediaManagerServiceUrl();

			// Perform the asset ingestion
			doAssetIngestion();
		}
		catch(Exception e){			
			logger.error("An unexpected error occurred in AssetIngester. Exiting.");
		}	
		finally
		{
			logger.info( status.toString() );
			this.ais.setStatus( Hibernate.createClob( status.toString() ) );
			this.ais.update();
		}
	}

	private void doAssetIngestion()
	{		
		try
		{
			LinkedList<String> errors = new LinkedList<String>();
			LinkedList<String> warnings = new LinkedList<String>();
			
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
							handleCSVLine( lcsvp, rawDataLine, errors, warnings );	
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
			
			if(errors.size() > 0){
				appendStatusMessage("\nERRORS SUMMARY:\n", false);
				for(String error : errors){
					appendStatusMessage(error, false);
				}
				appendStatusMessage("", true);
			}
			
			if(warnings.size() > 0){
				appendStatusMessage("\nWARNINGS SUMMARY:\n", false);
				for(String warning : warnings){
					appendStatusMessage(warning, false);
				}
				appendStatusMessage("", true);
			}
		}
		catch(Exception e)
		{
			appendStatusMessage("An unexpected error occurred in AssetIngester.doAssetIngestion(): "+ e.toString(), true);
			logger.error("An unexpected error occurred in AssetIngester.doAssetIngestion(): "+ e.toString(), e);
		}
	}
	
	private void handleCSVLine(LabeledCSVParser csvInputLine, String[] rawDataLine, LinkedList<String> errors, LinkedList<String> warnings) throws ServiceException, RemoteException
	{		
		String assetType = csvInputLine.getValueByLabel( AssetIngesterColumnType.ASSET_TYPE.toString() );
		String name = csvInputLine.getValueByLabel( AssetIngesterColumnType.NAME.toString() );
		String asset = csvInputLine.getValueByLabel( AssetIngesterColumnType.ASSET.toString() );
		String height = csvInputLine.getValueByLabel( AssetIngesterColumnType.HEIGHT.toString() );
		String width = csvInputLine.getValueByLabel( AssetIngesterColumnType.WIDTH.toString() );
		String defaultLayout = csvInputLine.getValueByLabel( AssetIngesterColumnType.DEFAULT_LAYOUT.toString() );
		String defaultDisplayarea = csvInputLine.getValueByLabel( AssetIngesterColumnType.DEFAULT_DISPLAYAREA.toString() );
		String defaultLength = csvInputLine.getValueByLabel( AssetIngesterColumnType.DEFAULT_LENGTH.toString() );
		String startPage = csvInputLine.getValueByLabel( AssetIngesterColumnType.START_PAGE.toString() );		
		String authUsername = csvInputLine.getValueByLabel( AssetIngesterColumnType.AUTH_USERNAME.toString() );
		String authPassword = csvInputLine.getValueByLabel( AssetIngesterColumnType.AUTH_PASSWORD.toString() );
		String fitToSize = csvInputLine.getValueByLabel( AssetIngesterColumnType.FIT_TO_SIZE.toString() );
		String transparentBackground = csvInputLine.getValueByLabel( AssetIngesterColumnType.TRANSPARENT_BACKGROUND.toString() );
		String pairedName = csvInputLine.getValueByLabel( AssetIngesterColumnType.PAIRED_NAME.toString() );
		String pairedAssetType = csvInputLine.getValueByLabel( AssetIngesterColumnType.PAIRED_ASSET_TYPE.toString() );
		String pairedDisplayarea = csvInputLine.getValueByLabel( AssetIngesterColumnType.PAIRED_DISPLAYAREA.toString() );
		String pairedLength = csvInputLine.getValueByLabel( AssetIngesterColumnType.PAIRED_LENGTH.toString() );
		String playAssetAudio = csvInputLine.getValueByLabel( AssetIngesterColumnType.PLAY_ASSET_AUDIO.toString() );
		String memberName = csvInputLine.getValueByLabel( AssetIngesterColumnType.MEMBER_NAME.toString() );
		String memberAssetType = csvInputLine.getValueByLabel( AssetIngesterColumnType.MEMBER_ASSET_TYPE.toString() );
		String memberDevices = csvInputLine.getValueByLabel( AssetIngesterColumnType.MEMBER_DEVICES.toString() );
		String dynamicAssetLength = csvInputLine.getValueByLabel( AssetIngesterColumnType.DYNAMIC_ASSET_LENGTH.toString() );
		String noTargetingRule = csvInputLine.getValueByLabel( AssetIngesterColumnType.NO_TARGETING_RULE.toString() );
		String multipleTargetingRule = csvInputLine.getValueByLabel( AssetIngesterColumnType.MULTIPLE_TARGETING_RULE.toString() );
		String roles = csvInputLine.getValueByLabel( AssetIngesterColumnType.ROLES.toString() );
		String pairingRule = csvInputLine.getValueByLabel( AssetIngesterColumnType.PAIRING_RULE.toString() );
		String startDate = csvInputLine.getValueByLabel( AssetIngesterColumnType.START_DATE.toString() );
		String endDate = csvInputLine.getValueByLabel( AssetIngesterColumnType.END_DATE.toString() );
		String defaultAsset = csvInputLine.getValueByLabel( AssetIngesterColumnType.DEFAULT_ASSET.toString() );
		String defaultAssetType = csvInputLine.getValueByLabel( AssetIngesterColumnType.DEFAULT_ASSET_TYPE.toString() );
		String dupAssets = csvInputLine.getValueByLabel( AssetIngesterColumnType.DUPLICATE_ASSETS.toString() );
		String campaign = csvInputLine.getValueByLabel( AssetIngesterColumnType.CAMPAIGN.toString() );
		String anamorphicWidescreen = csvInputLine.getValueByLabel( AssetIngesterColumnType.ANAMORPHIC_WIDESCREEN.toString() );
		String newName = csvInputLine.getValueByLabel( AssetIngesterColumnType.NEW_NAME.toString() );
		String removeAsset = csvInputLine.getValueByLabel( AssetIngesterColumnType.REMOVE_ASSET.toString() );
		String opacity = csvInputLine.getValueByLabel( AssetIngesterColumnType.OPACITY.toString() );
		String displaySubtitles = csvInputLine.getValueByLabel( AssetIngesterColumnType.DISPLAY_SUBTITLES.toString() );
		String framesync = csvInputLine.getValueByLabel( AssetIngesterColumnType.FRAMESYNC.toString() );
		String html5Hwaccel = csvInputLine.getValueByLabel( AssetIngesterColumnType.HTML5_HARDWARE_ACCELERATION.toString() );
		
		assetType = assetType != null ? assetType.trim() : null;
		name = name != null ? name.trim() : null;
		asset = asset != null ? asset.trim() : null;
		height = height != null ? height.trim() : null;
		width = width != null ? width.trim() : null;
		defaultLayout = defaultLayout != null ? defaultLayout.trim() : null;
		defaultDisplayarea = defaultDisplayarea != null ? defaultDisplayarea.trim() : null;
		defaultDisplayarea = defaultDisplayarea != null ? defaultDisplayarea.trim() : null;
		defaultLength = defaultLength != null ? defaultLength.trim() : null;
		startPage = startPage != null ? startPage.trim() : null;
		authUsername = authUsername != null ? authUsername.trim() : null;
		authPassword = authPassword != null ? authPassword.trim() : null;
		fitToSize = fitToSize != null ? fitToSize.trim() : null;
		transparentBackground = transparentBackground != null ? transparentBackground.trim() : null;
		pairedName = pairedName != null ? pairedName.trim() : null;
		pairedAssetType = pairedAssetType != null ? pairedAssetType.trim() : null;
		pairedDisplayarea = pairedDisplayarea != null ? pairedDisplayarea.trim() : null;
		pairedLength = pairedLength != null ? pairedLength.trim() : null;
		playAssetAudio = playAssetAudio != null ? playAssetAudio.trim() : null;
		memberName = memberName != null ? memberName.trim() : null;
		memberAssetType = memberAssetType != null ? memberAssetType.trim() : null;
		memberDevices = memberDevices != null ? memberDevices.trim() : null;
		dynamicAssetLength = dynamicAssetLength != null ? dynamicAssetLength.trim() : null;
		noTargetingRule = noTargetingRule != null ? noTargetingRule.trim() : null;
		multipleTargetingRule = multipleTargetingRule != null ? multipleTargetingRule.trim() : null;
		roles = roles != null ? roles.trim() : null;
		pairingRule = pairingRule != null ? pairingRule.trim() : null;
		startDate = startDate != null ? startDate.trim() : null;
		endDate = endDate != null ? endDate.trim() : null;
		defaultAsset = defaultAsset != null ? defaultAsset.trim() : null;
		defaultAssetType = defaultAssetType != null ? defaultAssetType.trim() : null;
		dupAssets = dupAssets != null ? dupAssets.trim() : null;
		campaign = campaign != null ? campaign.trim() : null;
		anamorphicWidescreen = anamorphicWidescreen != null ? anamorphicWidescreen.trim() : null;
		newName = newName != null ? newName.trim() : null;
		removeAsset = removeAsset != null ? removeAsset.trim() : null;
		opacity = opacity != null ? opacity.trim() : null;
		displaySubtitles = displaySubtitles != null ? displaySubtitles.trim() : null;
		framesync = framesync != null ? framesync.trim() : null;
		html5Hwaccel = html5Hwaccel != null ? html5Hwaccel.trim() : null;
		
		boolean ignoreDuplicates = dupAssets != null && dupAssets.equalsIgnoreCase("update") ? false : dupAssets != null && dupAssets.equalsIgnoreCase("ignore") ? true : this.ignoreDuplicates;
		
		// Make sure to update the asset location if it is local to the csv
		if(asset != null && asset.length() > 0 && !asset.contains(":")){
			// Remove the automatically ingested directory from the path
			if(controlFilepath.contains(AutoAssetIngest.AUTOMATICALLY_INGESTED_SUB_DIRECTORY)){
				controlFilepath = controlFilepath.replace(AutoAssetIngest.AUTOMATICALLY_INGESTED_SUB_DIRECTORY, "");
			}
			asset = "file://" + controlFilepath.substring(0, controlFilepath.lastIndexOf("/") + 1) + asset;
		}
		
		// Build the string array for each of our "valid" metadata fields
		String[][] metadata = new String[ metadataFields.size() ][2];
		int counter = 0;
		for( Iterator i=metadataFields.iterator(); i.hasNext(); )
		{
			String metadataLabel = (String)i.next();
			metadata[ counter ][0] = metadataLabel;
			String value = csvInputLine.getValueByLabel( metadataLabel );
			value = value != null && value.trim().length() > 0 ? value.trim() : null;
			metadata[ counter++ ][1] = value;
		}
		
		// Execute the web service request to create or update the asset
		MediaManageable mediaManageable = MediaManageableServiceLocator.getMediaManager( this.mediaManagerServiceUrl );
		((MediaManagerServiceSoapBindingStub)mediaManageable).setTimeout(7200000);
		String response;
		// If remove asset is set to "true"
		if(Boolean.parseBoolean(removeAsset)){
			// Execute the web service request to create or update the asset
		    response = mediaManageable.deleteAssetByName(name);						
		} else {
			// Execute the web service request to create or update the asset
			response = mediaManageable.createOrUpdateAsset( null, assetType, name, asset, height, width, null, 
					defaultLayout, defaultDisplayarea, defaultLength, startPage, fitToSize, transparentBackground,
					pairedName, pairedAssetType, pairedDisplayarea, pairedLength, playAssetAudio,
					memberName, memberAssetType, memberDevices, dynamicAssetLength, noTargetingRule, multipleTargetingRule,
					roles, authUsername, authPassword, pairingRule, startDate, endDate, defaultAsset, defaultAssetType, campaign,
					anamorphicWidescreen, framesync, html5Hwaccel, newName, removeAsset, opacity, displaySubtitles, 
					null, null, null, null, null, null, null, null, null, null, null, null, metadata, ignoreDuplicates );
		}
		
		// Rebuild the rawDataLine
		StringBuffer sb = new StringBuffer();
		for( int i=0; i<rawDataLine.length; i++ ){
			if (sb.length()> 0)
				sb.append( "," );
			
			if( rawDataLine[i] != null ){
				sb.append( rawDataLine[i].trim() );	
			}			
		}
		
		// If we did not receive any warning or error messages from the web service -- show the success message
		if( response != null && response.startsWith("assetId=")){
			response = Constants.SUCCESS.toUpperCase();
		}else{
			String message = response + "\n" + "     RAW DATA: " + sb.toString() + "\n";
			if(response.toLowerCase().contains("error")){
				errors.add(message);
			}else if(response.toLowerCase().contains("warning")){
				warnings.add(message);
			}
		}
		
		appendStatusMessage( response +"\n", false );
		appendStatusMessage( "     RAW DATA: " + sb.toString() + "\n", true );
	}
	
	/**
	 * Verify that the required columns are present. Display warning messages
	 * for additional column headers that will be ignored.
	 * @param lcsvp
	 * @return
	 */
	private boolean verifyColumnHeaders(LabeledCSVParser lcsvp) throws IOException, ClassNotFoundException
	{
		boolean result = false;
		
		// Verify that the required columns are present
		int assetTypeColumn = lcsvp.getLabelIdx( AssetIngesterColumnType.ASSET_TYPE.toString() );
		int nameColumn = lcsvp.getLabelIdx( AssetIngesterColumnType.NAME.toString() );
		if( assetTypeColumn < 0 ) {
			appendStatusMessage("ERROR: Column \""+ AssetIngesterColumnType.ASSET_TYPE.toString() +"\" not found. Exiting.\n", true);
		}else if( nameColumn < 0 ) {
			appendStatusMessage("ERROR: Column \""+ AssetIngesterColumnType.NAME.toString() +"\" not found. Exiting.\n", true);
		}else
		{
			// Display warning messages if "other" columns are found
			String[] labels = lcsvp.getLabels();
			for( int i=0; i<labels.length; i++ )
			{
				String label = labels[i];
				AssetIngesterColumnType columnType = AssetIngesterColumnType.getAssetIngesterColumnType( label );
				if( columnType == null )
				{
					/*
					 * Attempt to locate this column as an asset metadata field.
					 * If this label starts with the "METADATA:" prefix, chop it off
					 */
					String attrName = label.toUpperCase(); 
					if( attrName.indexOf( Constants.METADATA_PREFIX ) >= 0 ){
						attrName = attrName.substring( attrName.indexOf( Constants.METADATA_PREFIX ) + Constants.METADATA_PREFIX.length()).trim();
					}
					
					// If we found an attrDefinition with the given name
					AttrDefinition ad = AttrDefinition.getAttributeDefinition( Asset.class.getName(), attrName );
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
			result = true;
		}
		return result;
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
	 * Set the AssetIngester dirty status 
	 */
	public synchronized static void makeDirty() throws HibernateException
	{		
		// Create a new dirty object
		Dirty d = new Dirty();					
		d.setDirtyType( DirtyType.ASSET_INGESTER );		
		d.save();
	}
	
	/**
	 * Removes all dirty objects of type AssetIngester 
	 */
	public synchronized static void makeNotDirty() throws HibernateException
	{		
		List assetIngesterDirties = Dirty.getDirtyEntities( DirtyType.ASSET_INGESTER );	
		for( Iterator i=assetIngesterDirties.iterator(); i.hasNext(); )
		{
			Dirty d = (Dirty)i.next();
			d.delete();
		}
	}	
	
	/**
	 * Returns true of false depending whether or not there is a 
	 * dirty object of type AssetIngester for this object.
	 * 
	 * @return 
	 */
	public static boolean isDirty() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();
		String hql = "SELECT d "
					+ "FROM Dirty d "
					+ "WHERE d.dirtyType = '"+ DirtyType.ASSET_INGESTER.getPersistentValue() + "'";
		Iterator i = session.createQuery( hql ).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}		
	
    public synchronized void appendStatusMessage(String msg, boolean saveMessage) throws HibernateException
    {
    	if( this.status == null )
    	{
			this.status = new StringBuffer();
    	}
		this.status.append( msg );
		
		// If the flag to persist the message to the db was passed in
		if( saveMessage && this.ais != null )
		{			
			this.ais.setStatus( Hibernate.createClob( this.status.toString() ) );
			this.ais.update();
		}
    }
    
	/**
	 * Executes the assetIngester command line program in a separate JVM.
	 * 
	 * @param username
	 * @param password
	 * @param duplicatesMode
	 * @param controlFilePath
	 * @throws IOException
	 */
	public static String runAssetIngester(String username, String password, String duplicatesMode, String controlFilePath) throws IOException
	{		
		String result = "";
		
		// Make sure the asset ingester is not already running
		List assetIngesterDirties = Dirty.getDirtyEntities( DirtyType.ASSET_INGESTER );	
		if( assetIngesterDirties.size() == 0 )
		{
			try
			{
				// Set the dirty flag
				AssetIngester.makeDirty();
				
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
				 * Build and execute the java command to execute the content scheduler
				 */			
				String cmd = "java -cp "+ KuvataConfig.getPropertyValue("classpath") +" -Xms"+ memoryAllocationMin +" -Xmx"+ memoryAllocationMax +" "
					+ AssetIngester.class.getName() +" "+ username +" "+ password +" "+ duplicatesMode +" "+ controlFilePath +" "+ Constants.TRUE;
				logger.info("Running asset ingester: "+ cmd);			
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
				// Clear the dirty flag in case of an error
				AssetIngester.makeNotDirty();
			}
			result = Constants.ASSET_INGESTER_INITIALIZING_MESSAGE;
		}
		else
		{
			result = "The Asset Ingester was unable to run because a previous instance is already running.";
		}
		return result;
	}    
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if( args.length != 5 ){			
			logger.error("Usage: java AssetIngester username password duplicatesMode[ignore|modify] controlFilepath wasRunFromInterface[true|false]");
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
				SchemaDirectory.initialize( Constants.BASE_SCHEMA, "Asset Ingester", null, false, true );
				
				// Authenticate the username/password
				AppUser appUser = AppUser.getAppUser( username, password );
				if( appUser != null )
				{
					// Switch to the schema associated with this appuser
					String schema = appUser.getSchema().getSchemaName();
					HibernateSession.closeSession();
					SchemaDirectory.initialize( schema, "Asset Ingester", appUser.getAppUserId().toString(), true, true );
					new AssetIngester( appUser, duplicatesMode, controlFilepath );
					logger.info("Finished Asset Ingester");
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
					if( wasRunFromInterface != null && wasRunFromInterface.equalsIgnoreCase( Constants.TRUE ) ){
						AssetIngester.makeNotDirty();
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
