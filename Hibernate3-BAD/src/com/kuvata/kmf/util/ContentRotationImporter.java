package com.kuvata.kmf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import parkmedia.KMFLogger;
import parkmedia.usertype.PlaylistImportStatus;
import parkmedia.usertype.PlaylistImporterColumnType;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.ContentRotation;
import com.kuvata.kmf.ContentRotationAsset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.IAsset;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.permissions.ActionType;
import com.kuvata.kmf.permissions.FilterManager;

public class ContentRotationImporter {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( ContentRotationImporter.class );	
	private StringBuffer status = null;
	private String controlFilepath = null;	
	private Long contentRotationId = null;		
	private String length = null;
	private ContentRotation contentRotation = null;		
	private boolean appendAssets = false;
	private PlaylistImportStatus importStatus = PlaylistImportStatus.SUCCESS;
		
	/**
	 * @param playlistName
	 * @param contentRotationName
	 * @param layoutName
	 * @param displayareaName
	 * @param length
	 * @param appendAssetsToPlaylist
	 * @param controlFilePath
	 */
	public static void doImport(Long contentRotationId, String length, boolean appendAssets, String controlFilePath)
	{
		ContentRotationImporter contentRotationImporter = new ContentRotationImporter( contentRotationId, length, appendAssets, controlFilePath );
		
		// If we initialized sucessfully
		if( contentRotationImporter.init() )
		{
			// Peform the import
			contentRotationImporter.doContentRotationImport();
		}else{
			if( contentRotationImporter.contentRotation != null ){
				contentRotationImporter.contentRotation .setCsvImportDate( new Date() );
				contentRotationImporter.contentRotation .setCsvImportStatus( PlaylistImportStatus.FAILED );
				contentRotationImporter.contentRotation .setCsvImportDetail( Hibernate.createClob( contentRotationImporter.status.toString() ) );
				contentRotationImporter.contentRotation .update();
			}
		}
	}
	
	public ContentRotationImporter(Long contentRotationId, String length, boolean appendAssets, String controlFilepath)
	{
		this.contentRotationId = contentRotationId;		
		this.length = length;
		this.appendAssets = appendAssets;				
		this.controlFilepath = controlFilepath;
	}
	
	/**
	 * Make sure a valid playlistName, layoutName, and displayareaName have been passed in
	 * 
	 * @return
	 */
	private boolean init()
	{
		boolean result = true;
		
		// If we're importing into a contentRotation
		contentRotation = ContentRotation.getContentRotation( this.contentRotationId );
		if( contentRotation == null )
		{
			logger.info("Could not locate content rotation: "+ this.contentRotationId +". Exiting.");
			return false;
		}		
		return result;
	}
	
	private void doContentRotationImport()
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
					// If we're replacing the assets in this content rotation
					if( this.appendAssets == false )
					{
						contentRotation.deleteContentRotationAssets();
					}
					
					HibernateSession.startBulkmode();
					String[] rawDataLine = lcsvp.getLine(); 						
					while( rawDataLine != null )
					{
						handleCSVLine( lcsvp );	
						rawDataLine = lcsvp.getLine(); 
					}
					HibernateSession.stopBulkmode();
				}else{
					importStatus = PlaylistImportStatus.FAILED;
				}
			}
			else
			{
				appendStatusMessage("Could not locate control file: "+ controlFilepath +". Exiting.");
				importStatus = PlaylistImportStatus.FAILED;
			}	
			appendStatusMessage("\nFinished.");
						
			contentRotation.setCsvImportDate( new Date() );
			contentRotation.setCsvImportStatus( importStatus );
			contentRotation.setCsvImportDetail( Hibernate.createClob( this.status.toString() ) );
			
			// Make sure this content rotation is static -- update it if not			
			if( contentRotation.getHql() != null && contentRotation.getHql().length() > 0 ) {
				contentRotation.setHql( null );			
			}				
			contentRotation.update();
		}
		catch(Exception e)
		{
			appendStatusMessage("An unexpected error occurred in ContentRotationImporter.doPlaylistImport(): "+ e.toString());
			logger.error("An unexpected error occurred in ContentRotationImporter.doContentRotationImport(): "+ e.toString(), e);
		}
	}
	
	private void handleCSVLine(LabeledCSVParser csvInputLine) throws ServiceException, RemoteException
	{				
		String assetName = csvInputLine.getValueByLabel( PlaylistImporterColumnType.NAME.toString() );				
		String csvLength = csvInputLine.getValueByLabel( PlaylistImporterColumnType.LENGTH.toString() );

		// Attempt to find an asset with the given name
		List assets = Asset.getAssets( assetName );
		
		// If we did not find any assets with the given name
		if( assets.size() == 0 )
		{
			appendStatusMessage( "WARNING: Unable to locate asset: "+ assetName +"\n" );
			this.importStatus = PlaylistImportStatus.WARNING;			
		}		
		// If we found one and only one assets with the given name
		else if( assets.size() == 1 )
		{			
			String length = null;	
			String defaultLength = null;
			IAsset iAsset = (IAsset)assets.get(0);
			Asset asset = Asset.convert( iAsset );			
			
			// If the "Default" length was passed in, use the asset presentation's length
			if( this.length.equalsIgnoreCase( Constants.DEFAULT ) ){
				defaultLength = asset.getAssetPresentation().getLength().toString(); 
			}else{
				defaultLength = this.length;
			}			
						
			// If a length was specified in the csv file
			if( csvLength != null && csvLength.length() > 0 )
			{
				// Make sure it is a valid float
				try {
					Float testFloat = Float.parseFloat( csvLength );
					length = testFloat.toString();
				} catch (NumberFormatException e) {
					appendStatusMessage("WARNING: Invalid length specified: "+ csvLength +". Using default: "+ defaultLength +"\n");
					this.importStatus = PlaylistImportStatus.WARNING;					
					length = defaultLength;
				}
			} else {				
				length = defaultLength;	
			}

			// Create the content rotation asset
			ContentRotationAsset.create( contentRotation, asset, length, asset.getAssetPresentation().getVariableLength(), true );			
		}
		// If we found more than one asset with the given name
		else if( assets.size() > 1 )
		{
			appendStatusMessage("WARNING: More than one asset with the name, \""+ assetName +"\", already exists. Ignoring record.\n");
		}
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
		int nameColumn = lcsvp.getLabelIdx( PlaylistImporterColumnType.NAME.toString() );
		if( nameColumn < 0 ) {
			appendStatusMessage("ERROR: Column \""+ PlaylistImporterColumnType.NAME.toString() +"\" not found. Exiting.\n");
		}	
		else
		{
			// Display warning messages if "other" columns are found
			String[] labels = lcsvp.getLabels();
			for( int i=0; i<labels.length; i++ )
			{
				String label = labels[i];
				if( label.length() > 0 ){
					PlaylistImporterColumnType columnType = PlaylistImporterColumnType.getPlaylistImporterColumnType( label.toLowerCase() );
					if( columnType == null )
					{
						appendStatusMessage("WARNING: The following column is unknown and will be ignored: "+ label +"\n");
					}
				}
			}
			result = true;
		}
		return result;
	}
	
    public synchronized void appendStatusMessage(String msg) throws HibernateException
    {
    	if( this.status == null ) {
			this.status = new StringBuffer();
    	}
		logger.info( msg );
		this.status.append( msg );
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if( args.length != 6 ){			
			logger.error("Usage: java ContentRotationImporter username password contentRotationId length[Default] appendAssetsToContentRotation[true|false] controlFilepath");			
		}
		else
		{		
			String username = args[0];
			String password = args[1];
			String contentRotationId = args[2];			
			String length = args[3];
			String appendAssetsToContentRotation = args[4];
			String controlFilepath = args[5];
								
			try 
			{			
				// Authenticate the username/password
				SchemaDirectory.initialize( Constants.BASE_SCHEMA, "Content Rotation Importer", null, false, true );				
				AppUser appUser = AppUser.getAppUser( username, password );
				if( appUser != null )
				{
					// Switch to the schema associated with this appuser
					String schema = appUser.getSchema().getSchemaName();
					HibernateSession.closeSession();
					SchemaDirectory.initialize( schema, "Content Rotation Importer", appUser.getAppUserId().toString(), true, true );
					boolean appendAssets = false;
					if( appendAssetsToContentRotation.equalsIgnoreCase( Constants.TRUE ) ){
						appendAssets = true;
					}			
					
					// Enable permissions specific to this action
					FilterManager.enableFilters( ActionType.CONTENT_ROTATION_IMPORTER );
					ContentRotationImporter.doImport(new Long( contentRotationId ), length, appendAssets, controlFilepath);
					logger.info("Finished Content Rotation Importer");
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
					HibernateSession.closeSession();
				} catch(HibernateException he) {			
					he.printStackTrace();
				}
			}	
		}

	}

}
