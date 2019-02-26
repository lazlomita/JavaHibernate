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

import parkmedia.KMFLogger;
import parkmedia.usertype.AssetType;
import parkmedia.usertype.PlaylistImportStatus;
import parkmedia.usertype.PlaylistImporterColumnType;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.IAsset;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.PlaylistAsset;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.permissions.ActionType;
import com.kuvata.kmf.permissions.FilterManager;

public class PlaylistImporter {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( PlaylistImporter.class );	
	private StringBuffer status = null;
	private String controlFilepath = null;		
	private String layoutName = null;
	private String displayareaName = null;
	private String length = null;	
	private Long playlistId = null;
	private Playlist playlist = null;	
	private Layout defaultLayout = null;
	private Displayarea defaultDisplayarea = null;
	private boolean appendAssetsToPlaylist = false;
	private PlaylistImportStatus importStatus = PlaylistImportStatus.SUCCESS;	
	private ArrayList metadataFields = new ArrayList();
		
	/**
	 * @param playlistName
	 * @param contentRotationName
	 * @param layoutName
	 * @param displayareaName
	 * @param length
	 * @param appendAssetsToPlaylist
	 * @param controlFilePath
	 */
	public static void doImport(Long playlistId, String layoutName, String displayareaName, String length, boolean appendAssetsToPlaylist, String controlFilePath)
	{
		PlaylistImporter playlistImporter = new PlaylistImporter( playlistId, layoutName, displayareaName, length, appendAssetsToPlaylist, controlFilePath );
		
		// If we initialized successfully
		if( playlistImporter.init() )
		{
			// Perform the import
			playlistImporter.doPlaylistImport();
		}else{
			if( playlistImporter.playlist != null ){
				int numPlaylistImports = playlistImporter.playlist.getPlaylistImports().size();
				playlistImporter.playlist.setCsvImportDate( new Date() );
				playlistImporter.playlist.setCsvImportStatus( PlaylistImportStatus.FAILED );
				playlistImporter.playlist.setCsvImportDetail( Hibernate.createClob(playlistImporter.status.toString()) );
				playlistImporter.playlist.setType(Constants.STATIC);
				playlistImporter.playlist.update();
				playlistImporter.playlist.getPlaylistImports().add( playlistImporter );
				playlistImporter.playlist.update();
			}
		}
	}
	
	public PlaylistImporter(Long playlistId, String layoutName, String displayareaName, String length, boolean appendAssetsToPlaylist, String controlFilepath)
	{
		this.playlistId = playlistId;		
		this.layoutName = layoutName;
		this.displayareaName = displayareaName;
		this.length = length;
		this.appendAssetsToPlaylist = appendAssetsToPlaylist;				
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
		
		playlist = Playlist.getPlaylist( this.playlistId );
		if( playlist == null )
		{
			logger.info("Could not locate playlist with the given playlist id: "+ this.playlistId +". Exiting.");
			return false;
		}

		// If something other than the "Default" layout was passed in, attempt to locate the layout
		if( this.layoutName.equalsIgnoreCase( Constants.DEFAULT ) == false )
		{
			List layouts = Layout.getLayouts( this.layoutName );
			if( layouts.size() == 0 ){
				appendStatusMessage("Could not locate layout: "+ this.layoutName +". Exiting.");
				return false;
			}else if( layouts.size() > 1 ){
				appendStatusMessage("Located more than one layout named: "+ this.layoutName +". Exiting.");
				return false;
			}else if( layouts.size() == 1 ){
				defaultLayout = (Layout)layouts.get(0);
			}
		}
		
		// If something other than the "Default" displayarea was passed in, attempt to locate the displayarea
		if( this.displayareaName.equalsIgnoreCase( Constants.DEFAULT ) == false )
		{
			List displayareas = Displayarea.getDisplayareas( this.displayareaName );
			if( displayareas.size() == 0 ) {
				appendStatusMessage("Could not locate displayarea: "+ this.displayareaName +". Exiting.");
				return false;
			}
			else if( displayareas.size() > 1 )
			{
				// Look for the displayarea that is part of the specified layout
				// There should only ever be one displayarea with the same name that is part of a given layout
				for( Iterator i=displayareas.iterator(); i.hasNext(); ){
					Displayarea da = (Displayarea)i.next();
					if( defaultLayout != null && defaultLayout.containsDisplayarea( da ) ){
						defaultDisplayarea = da;
					}
				}														
			}else if( displayareas.size() == 1 ){
				defaultDisplayarea = (Displayarea)displayareas.get(0);
			}		
		}
		return result;
	}
	
	private void doPlaylistImport()
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
					// If we're replacing the assets in this playlist
					if( this.appendAssetsToPlaylist == false )
					{
						playlist.deletePlaylistAssets();
					}
					
					String[] rawDataLine = lcsvp.getLine(); 						
					while( rawDataLine != null )
					{
						handleCSVLine( lcsvp );	
						rawDataLine = lcsvp.getLine(); 
					}
					
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
						
			playlist.setCsvImportDate( new Date() );
			playlist.setCsvImportStatus( importStatus );
			playlist.setCsvImportDetail( Hibernate.createClob(this.status.toString()) );
			playlist.makeDirty();
			playlist.update();
			playlist.resetPlaylistDisplayareas();
			playlist.updateLength();
		}
		catch(Exception e)
		{
			appendStatusMessage("An unexpected error occurred in PlaylistImporter.doPlaylistImport(): "+ e.toString());
			logger.error("An unexpected error occurred in PlaylistImporter.doPlaylistImport(): "+ e.toString(), e);
		}
	}
	
	private void handleCSVLine(LabeledCSVParser csvInputLine) throws ServiceException, RemoteException, ClassNotFoundException
	{
		String assetId = csvInputLine.getValueByLabel( PlaylistImporterColumnType.ASSET_ID.toString() );
		String assetName = csvInputLine.getValueByLabel( PlaylistImporterColumnType.NAME.toString() );
		String assetType = csvInputLine.getValueByLabel( PlaylistImporterColumnType.ASSET_TYPE.toString() );
		String layoutName = csvInputLine.getValueByLabel( PlaylistImporterColumnType.LAYOUT.toString() );
		String displayareaName = csvInputLine.getValueByLabel( PlaylistImporterColumnType.DISPLAYAREA.toString() );
		String csvLength = csvInputLine.getValueByLabel( PlaylistImporterColumnType.LENGTH.toString() );

		// If this is not a valid assetType
		Long assetIdLng = assetId != null && assetId.length() > 0 ? Long.parseLong(assetId) : null;
		AssetType at = assetType != null && assetType.length() > 0 ? AssetType.getAssetType( assetType ) : null;
		if( assetIdLng != null || at != null )
		{
			Asset assetFromId = null;
			if(assetIdLng != null){
				assetFromId = Asset.getAsset(assetIdLng);
				if(assetFromId != null && ((assetName != null && assetName.length() > 0) || at != null)){
					if(assetName != null && assetName.length() > 0 && assetFromId.getAssetName().equals(assetName) == false){
						appendStatusMessage("ERROR: Couldn't match asset_id \"" + assetId + "\" to asset_name \"" + assetName + "\". Ignoring line.\n");
						return;
					}else if(at != null && assetFromId.getAssetType().equals(at) == false){
						appendStatusMessage("ERROR: Couldn't match asset_id \"" + assetId + "\" to asset_type \"" + assetType + "\". Ignoring line.\n");
						return;
					}
				}else if(assetFromId == null){
					appendStatusMessage("ERROR: Couldn't locate an asset with asset_id \"" + assetId + "\". Ignoring line.\n");
					return;
				}
			}
			
			// Attempt to find an asset with the given name
			List assets = assetFromId == null ? Asset.getAssets( assetName, at ) : null;
			
			// If we found one and only one assets with the given name
			if( assetFromId != null || assets.size() == 1 )
			{
				Layout layout = null;
				Displayarea displayarea = null;
				String length = null;
				String defaultLength = null;
				
				Asset asset = assetFromId;
				if(assetFromId == null){
					IAsset iAsset = (IAsset)assets.get(0);
					asset = Asset.convert( iAsset );
				}
				
				// If we've gotten this far and the defaultLayout is null, it means we're using the "Default" layout
				if( defaultLayout == null || this.layoutName.equalsIgnoreCase(Constants.DEFAULT)){
					defaultLayout = asset.getAssetPresentation().getLayout(); 
				}
								
				// If we've gotten this far and the defaultDisplayarea is null, it means we're using the "Default" displayarea
				if( defaultDisplayarea == null || this.displayareaName.equalsIgnoreCase(Constants.DEFAULT)){
					defaultDisplayarea = asset.getAssetPresentation().getDisplayarea(); 
				}
				
				// If the "Default" length was passed in, use the asset presentation's length
				if( this.length.equalsIgnoreCase( Constants.DEFAULT ) ){
					defaultLength = asset.getAssetPresentation().getLength().toString(); 
				}else{
					defaultLength = this.length;
				}			
							
				// If a layout was specified in the csv file
				if( layoutName != null && layoutName.length() > 0 )
				{
					// If a layout does not exist with the given name, use the default layout
					List layouts = Layout.getLayouts( layoutName );
					if( layouts.size() == 0 ) {
						layout = defaultLayout;
						appendStatusMessage("WARNING: Unable to locate layout: "+ layoutName +". Using default: "+ layout.getLayoutName() +"\n");
						this.importStatus = PlaylistImportStatus.WARNING;
					}else if( layouts.size() > 1 ) {
						layout = defaultLayout;
						appendStatusMessage("WARNING: Located more than one layout named: "+ layoutName +". Using default: "+ layout.getLayoutName() +"\n");
						this.importStatus = PlaylistImportStatus.WARNING;
					}else if( layouts.size() == 1 ){
						layout = (Layout)layouts.get(0);
					}
				}else{
					layout = defaultLayout;
				}
				
				// If a displayarea was specified in the csv file
				if( displayareaName != null && displayareaName.length() > 0 )
				{
					// If a displayarea does not exist with the given name, use the default displayarea
					List displayareas = Displayarea.getDisplayareas( displayareaName );
					if( displayareas.size() == 0 )
					{
						displayarea = defaultDisplayarea;
						appendStatusMessage("WARNING: Unable to locate displayarea: "+ displayareaName +". Using default: "+ displayarea.getDisplayareaName() +"\n");
						this.importStatus = PlaylistImportStatus.WARNING;					
					}else if( displayareas.size() > 1 )
					{
						// Look for the displayarea that is part of the specified layout
						// There should only ever be one displayarea with the same name that is part of a given layout
						for( Iterator i=displayareas.iterator(); i.hasNext(); ){
							Displayarea da = (Displayarea)i.next();
							if( layout != null && layout.containsDisplayarea( da ) ){
								displayarea = da;
							}
						}														
					}else if( displayareas.size() == 1 ){
						displayarea = (Displayarea)displayareas.get(0);
					}
				}else{
					displayarea = defaultDisplayarea;
				}
				
				// If the displayarea is not part of the layout
				if( layout.containsDisplayarea( displayarea ) == false ) {
					appendStatusMessage("WARNING: The displayarea \""+ displayarea.getDisplayareaName() +"\" is not part of the layout \""+ layout.getLayoutName() +"\".\n");
					this.importStatus = PlaylistImportStatus.WARNING;				
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
				
				// If the asset type is not allowed in the displayarea
				if( displayarea.isValidAssetType( asset.getAssetType() ) == false ){
					appendStatusMessage("WARNING: The asset type, \""+ asset.getAssetType().getAssetTypeName() +"\", associated with this asset is not allowed in this displayarea \""+ displayarea.getDisplayareaName() +"\".\n");
					this.importStatus = PlaylistImportStatus.WARNING;				
				}

				// Create the playlist asset
				PlaylistAsset.create( playlist, asset, length, layout, displayarea, asset.getAssetPresentation().getVariableLength(), false, true, true, false );			
			}
			// If we did not find any assets with the given name
			else if( assets.size() == 0 )
			{
				appendStatusMessage( "WARNING: Unable to locate asset: "+ assetName +"\n" );
				this.importStatus = PlaylistImportStatus.WARNING;			
			}
			// If we found more than one asset with the given name
			else if( assets.size() > 1 )
			{
				appendStatusMessage("WARNING: More than one asset with the name, \""+ assetName +"\", already exists. Ignoring record.\n");
			}			
		}
		else
		{
			appendStatusMessage( "WARNING: Unknown asset_type: "+ assetType +". Ignoring Record.\n" );
			this.importStatus = PlaylistImportStatus.WARNING;
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
		int assetIdColumn = lcsvp.getLabelIdx( PlaylistImporterColumnType.ASSET_ID.toString() );
		int nameColumn = lcsvp.getLabelIdx( PlaylistImporterColumnType.NAME.toString() );
		int assetTypeColumn = lcsvp.getLabelIdx( PlaylistImporterColumnType.ASSET_TYPE.toString() );
		if( assetIdColumn < 0 && nameColumn < 0 ) {
			appendStatusMessage("ERROR: Column \""+ PlaylistImporterColumnType.NAME.toString() +"\" not found. Exiting.\n");
		}
		else if( assetIdColumn < 0 && assetTypeColumn < 0 ) {
			appendStatusMessage("ERROR: Column \""+ PlaylistImporterColumnType.ASSET_TYPE.toString() +"\" not found. Exiting.\n");
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
		
		if( args.length != 8 ){			
			logger.error("Usage: java PlaylistImporter username password playlistId layoutName[Default] displayareaName[Default] length[Default] appendAssetsToPlaylist[true|false] controlFilepath");			
		}
		else
		{		
			String username = args[0];
			String password = args[1];
			String playlistId = args[2];
			String layoutName = args[3];
			String displayareaName = args[4];
			String length = args[5];
			String appendAssetsToPlaylist = args[6];
			String controlFilepath = args[7];
								
			try 
			{				
				// Authenticate the username/password
				SchemaDirectory.initialize( Constants.BASE_SCHEMA, "Playlist Importer", null, false, true );				
				AppUser appUser = AppUser.getAppUser( username, password );
				if( appUser != null )
				{
					// Switch to the schema associated with this appuser
					String schema = appUser.getSchema().getSchemaName();
					HibernateSession.closeSession();
					SchemaDirectory.initialize( schema, "Playlist Importer", appUser.getAppUserId().toString(), true, true );
					boolean appendAssets = false;
					if( appendAssetsToPlaylist.equalsIgnoreCase( Constants.TRUE ) ){
						appendAssets = true;
					}				
					
					// Enable permissions specific to this action
					FilterManager.enableFilters( ActionType.PLAYLIST_IMPORTER );
					PlaylistImporter.doImport(new Long( playlistId ), layoutName, displayareaName, length, appendAssets, controlFilepath);
					logger.info("Finished Playlist Importer");
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
