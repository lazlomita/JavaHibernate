package com.kuvata.kmf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import com.kuvata.kmf.usertype.AssetSearchType;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.DefaultAssetAffinityType;
import com.kuvata.kmf.usertype.DirtyType;
import com.kuvata.kmf.usertype.FileTransmissionStatus;
import com.kuvata.kmf.usertype.FileTransmissionType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.asset.Audio;
import com.kuvata.kmf.asset.Flash;
import com.kuvata.kmf.asset.Html;
import com.kuvata.kmf.asset.Image;
import com.kuvata.kmf.asset.Video;
import com.kuvata.kmf.asset.Webapp;
import com.kuvata.kmf.asset.Xml;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.billing.CampaignAsset;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.util.Files;
import com.kuvata.kmf.util.HibernateUtil;
import com.kuvata.kmf.util.Reformat;

/**
 * Class representing Assets
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 */
public abstract class Asset extends Entity implements IAsset, Historizable
{
	private static Logger logger = Logger.getLogger(Asset.class);
	private Long assetId;
	private String assetName;		
	private AssetPresentation assetPresentation;
	private Set pairedAssets = new HashSet();
	private Set assetSegmentParts = new HashSet();
	private Set contentRotationAssets = new HashSet();
	private Set playlistAssets = new HashSet();		
	private Set assetExclusions = new HashSet();
	private Date startDate;
	private Date endDate;
	private IAsset defaultAsset;
	private String adserverIdentifier;
	protected String previewPath;
	protected String thumbnailPath;
	private Date lastModified;
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAYABLE);
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
	public static final String THUMB_EXT = "_tbn";
	public static final int MAX_DIMENSION = 200;
	public static final int CHUNK_SIZE = 1024 * 32;	
	private static final String[] historyFields = {"All"};
	private static final String START_UPLOAD = "startUpload";
	private static final String ASSET_ID = "assetId";
	private static final String ASSET_NAME = "assetName";
	private static final String LOCAL_FULLPATH = "localFullPath";	
	private static final String RELATIVE_PATH = "relativePath";
	
	public abstract String getPresentationType();
	public abstract String getCreateAssetPage();
	public abstract String getAssetPropertiesPage();
	public abstract AssetType getAssetType();	
	public abstract String getPreviewPath() throws HibernateException;
	public abstract String getThumbnailPath() throws HibernateException;
	public abstract String renderHTML();
	public abstract boolean getReferencedFileExists();
	public abstract Long copy( String newAssetName ) throws ClassNotFoundException, InterruptedException;
	
	/**
	 *The Asset method 
	 *
	 */
	public Asset()
	{		
	}
	
	public static Asset convert(IAsset iAsset)
	{
		return (Asset)HibernateUtil.convert( iAsset );
	}
	
	public Long save(){
		this.setLastModified(new Date());
		return super.save();
	}
	
	public Long save(boolean createPermissionEntries){
		this.setLastModified(new Date());
		return super.save(createPermissionEntries);
	}
	
	public void update(){
		this.setLastModified(new Date());
		super.update();
	}
	
	/**
	 * Delete method to delete an asset
	 */
	public void delete() throws HibernateException
	{
		// Remove any asset affinities associated with this asset
		Device.removeAssetAffinity( this );
		
		// Remove any expiration attachments
		if(this.isDefaultAssetForAnotherAsset()){
			for(Asset a : this.getAssetsUsingThisDefaultAsset()){
				a.removeDefaultAssetInfo();
			}
		}
		
		// Remove this asset from all campaigns
		CampaignAsset.deleteCampaignAssets(this);
		
		// Delete this asset's asset presentation
		this.getAssetPresentation().delete();
		
		// Delete all pairings to this asset
		for(PairedAsset pa : PairedAsset.getPairedAssetFromAsset(this.assetId)){
			pa.delete();
		}
		
		// Delete all non-Exists file_transmission rows
		String hql = "DELETE FROM FileTransmission WHERE type = '" + FileTransmissionType.PRESENTATION.getPersistentValue() + "' AND filename LIKE '%/" + this.getAssetId() + "-%' AND status != '" + FileTransmissionStatus.EXISTS.getPersistentValue() + "'";
		HibernateSession.currentSession().createQuery(hql).executeUpdate();
		
		super.delete();
	}
	
	/**
	 * 
	 * @return
	 */
	public List getHistoryFields()
	{	
		List l = new ArrayList();
		for (int i=0; i<historyFields.length; i++)
		{
			l.add(historyFields[i].toLowerCase());
		}
		return l;
	}
		
	/**
	 * 
	 * @param assetType
	 * @return
	 * @throws HibernateException
	 */
	public static List getAssets(AssetType assetType) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT asset "
			+ "FROM "+ assetType.getPersistentValue() +" as asset "				
			+ "ORDER BY UPPER(asset.assetName)"; 		
		return session.createQuery( hql ).list();		
	}
	
	public static Asset getAdServerAsset(String adserverIdentifier) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT asset "
			+ "FROM Asset as asset WHERE asset.adserverIdentifier = :adserverIdentifier";
		return (Asset)session.createQuery( hql ).setParameter("adserverIdentifier", adserverIdentifier).uniqueResult();
	}
	
	/**
	 * 
	 * @param assetId
	 * @return
	 * @throws HibernateException
	 */	
	public static List getAssets(List assetIds) throws HibernateException
	{		
		/*
		 * We cannot call Entity.load() here because the object
		 * sometimes comes back as a proxy, and cannot be cast to an entity
		 */
		ArrayList returnList = new ArrayList();
		List l = Entity.load(Asset.class, assetIds);
		for(Iterator i=l.iterator();i.hasNext();){
			Object o = i.next();
			if( o != null ) {
				IAsset iAsset = (IAsset) o;
				if( iAsset != null ) {
					returnList.add((Asset)HibernateUtil.convert( iAsset ));		
				}			
			}
		}
		return returnList;
	}
	
	public static List getAssetNames(List assetIds) throws HibernateException
	{		
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT asset.assetName "
			+ "FROM Asset as asset WHERE asset.assetId IN (:assetIds)"				
			+ "ORDER BY UPPER(asset.assetName)"; 		
		return session.createQuery( hql ).setParameterList("assetIds", assetIds).list();	
	}
	
	/**
	 * Returns true if an asset with the given name already exists in the database
	 * 
	 * @param assetName
	 * @return
	 */
	public static boolean assetExists(String assetName, AssetType assetType) throws HibernateException, ClassNotFoundException
	{					
		// Attempt to retrieve the asset with the given name
		Session session = HibernateSession.currentSession();		
		List l = session.createCriteria( Class.forName( assetType.getPersistentValue() ) )
					.add( Expression.eq("assetName", assetName).ignoreCase() )
					.list();
		return l.size() > 0 ? true : false;		
	}	
	
	/**
	 * Attempts to retrieve the asset with the given name
	 * @param assetName
	 * @return
	 */
	public static List getAssets(String assetName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createCriteria(IAsset.class)
					.add( Expression.eq("assetName", assetName).ignoreCase() )				
					.list();
		return l;		
	}
	
	/**
	 * Attempts to retrieve the asset with the given name
	 * @param assetName
	 * @return
	 */
	public static List getAssets(String assetName, AssetType at) throws HibernateException, ClassNotFoundException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createCriteria( Class.forName( at.getPersistentValue() ) )
					.add( Expression.eq("assetName", assetName).ignoreCase() )				
					.list();
		return l;		
	}	
			
	/**
	 * Returns all assets that are set to given dirty status
	 * @return 
	 */
	public static List getDirtyAssets(DirtyType dirtyType) throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT a "
					+ "FROM Asset a, Dirty d "
					+ "WHERE a.assetId = d.dirtyEntityId "
					+ "AND d.dirtyType = '"+ dirtyType.getPersistentValue() + "'";		
		List l = session.createQuery( hql ).list(); 
		return l; 	
	}	
	
	/**
	 * @return Returns the playlistAssets in the given order
	 */
	public static int getAssetsCount(AssetType at) throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(a) from "+ at.getPersistentValue() +" as a";
		Iterator i = session.createQuery(hql).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}	
	/**
	 * 
	 * @param assetId
	 * @return
	 * @throws HibernateException
	 */	
	public static Asset getAsset(Long assetId) throws HibernateException
	{		
		/*
		 * We cannot call Entity.load() here because the object
		 * sometimes comes back as a proxy, and cannot be cast to an entity
		 */
		Asset result = null;
		Session session = HibernateSession.currentSession();
		Object o = session.createCriteria( Asset.class )
			.add( Restrictions.eq( "id", new Long(assetId) ) )
			.uniqueResult();			
		if( o != null ) {
			IAsset iAsset = (IAsset)o;
			if( iAsset != null ) {
				result = (Asset)HibernateUtil.convert( iAsset );		
			}			
		}
		return result;
	}
	
	public boolean isDefaultAssetForAnotherAsset()
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT COUNT(a) "
					+ "FROM Asset a "
					+ "WHERE a.defaultAsset.assetId = :assetId";
		Long count = (Long)(session.createQuery( hql ).setParameter("assetId", this.getAssetId()).iterate().next());
		
		return count > 0; 	
	}
	
	public List<Asset> getAssetsUsingThisDefaultAsset() 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT a "
					+ "FROM Asset a "
					+ "WHERE a.defaultAsset.assetId = :assetId";		
		return session.createQuery( hql ).setParameter("assetId", this.getAssetId()).list(); 	
	}
	
	/**
	 * 
	 * @param at
	 * @return
	 * @throws HibernateException
	 */
	public static String getAssetDirectory(AssetType at) throws HibernateException
	{
		String result = null;
		result = Asset.getAssetDirectory();
		if( at != null )
		{
			result = result +"/"+ at.getPersistentValue().substring( at.getPersistentValue().lastIndexOf(".") + 1 ).toLowerCase();	
		}
		return result;
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static String getAssetDirectory() throws HibernateException
	{		
		return SchemaDirectory.getSchemaBaseDirectory()+"/"+ Constants.ASSETS_DIR;	
	}
	
	/**
	 * Returns true or false depending on whether or not the given fileloc
	 * exists on disk.
	 * 
	 * @param fileloc
	 * @return
	 */
	public static boolean referencedFileExists(String fileloc){
		boolean result = false;
		if( fileloc != null && fileloc.length() > 0 ){
			File f = new File( fileloc );
			if( f.isFile() && f.exists() ){
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAssetId();
	}
	/**
	 * 
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();		
		if(includeLeaves == true)
		{	
			// Build the string for each device			
			result.append("[");					
			result.append("{id:"+ this.getAssetId() +"}, \""+ this.getAssetName() + "\", null, null, type_asset");	
			result.append("],\n");
		}
		return result.toString();
	}
	
	public void removeDefaultAssetInfo(){
		this.setStartDate(null);
		this.setEndDate(null);
		this.setDefaultAsset(null);
		this.update();
		
		this.makeDirty(false, true);
	}
	
	/**
	 * Make this entity dirty and cascade to devices if necessary.
	 * 
	 * @param lengthChanged
	 * @throws HibernateException
	 */
	public void makeDirty(boolean lengthChanged, boolean expirationChanged) throws HibernateException
	{
		// If there is a dirty object for this object		
		Dirty d = Dirty.getDirty( this.getAssetId() );		
		if(d != null)
		{
			if( lengthChanged == true)
			{
				d.setDirtyType( DirtyType.ASSET_LENGTH_CHANGED );
			}
			else if (expirationChanged){
				d.setDirtyType(DirtyType.ASSET_EXPIRATION_CHANGED);
			}
			else
			{
				// Only update if we've found a dirty object that does not 
				// have a "higher degree of dirty-ness" status than what we are updating to
				if(d.getDirtyType().equals(DirtyType.ASSET_LENGTH_CHANGED) == false && d.getDirtyType().equals(DirtyType.ASSET_EXPIRATION_CHANGED) == false)
				{
					d.setDirtyType( DirtyType.ASSET_LENGTH_UNCHANGED );
				}							
			}	
			d.update();
		}
		else
		{
			// Create a new dirty object
			d = new Dirty();			
			d.setDirtyEntityId( this.getAssetId() );
			if( lengthChanged == true ) {
				d.setDirtyType( DirtyType.ASSET_LENGTH_CHANGED );
			} else if (expirationChanged){
				d.setDirtyType(DirtyType.ASSET_EXPIRATION_CHANGED);
			} else {
				d.setDirtyType( DirtyType.ASSET_LENGTH_UNCHANGED );
			}		
			d.save();
		}				
		
		// If we have changed the length of an asset
		if( lengthChanged )
		{
			// Make dirty any devices that is using this asset
			Device.makeDirty( this );
		}
		
		// Make dirty any segments that this asset is a member of
		Segment.makeDirty( this );
	}	
	/**
	 * 
	 * @param fullFilePath
	 * @return
	 */
	public static Long calculateAdler32(String fullFilePath)
	{	
		Long result = null;		
		try
		{
			File f = new File( fullFilePath );
			FileInputStream in = new FileInputStream(f);
	
			Checksum fileCheck = new Adler32();
			CheckedInputStream checkedIn = new CheckedInputStream(in, fileCheck);
				
			byte[] buf = new byte[CHUNK_SIZE];
			int length = 0;		
			while( (length = checkedIn.read(buf)) > 0 )
			{			
			}
			checkedIn.close();
			in.close();
			
			result = Long.valueOf( fileCheck.getValue() );
		} catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return result;
	}
	/**
	 * Appends the given adler32 to the given fullFilePath.
	 * 
	 * @param fullFilePath
	 * @return
	 */
	public static String buildFileloc(String fullFilePath, Long adler32)
	{	
		String result = fullFilePath.indexOf(".") > 0 ? fullFilePath.substring( 0, fullFilePath.lastIndexOf(".") ) : fullFilePath;
		result += "-"+ adler32.toString() + (fullFilePath.indexOf(".") > 0 ? fullFilePath.substring( fullFilePath.lastIndexOf(".") ) : "");
		return result;
	}	

	/**
	 * 	Remove this asset from any playlists that contain this asset 
	 *  and re-order the playlist assets for that playlist
	 *
	 */
	public void removeFromPlaylists() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		HibernateSession.startBulkmode();
		
		// We need to get a list of assetPresentation ids before we delete the playlist asset
		// We cant delete from assetPresentation before deleting the playlist asset as the playlist asset
		// refers to the asset presentation and throws a hibernate exception if we try to do so.
		String hql = "SELECT pa.assetPresentation.assetPresentationId FROM PlaylistAsset pa WHERE pa.asset.assetId = "+this.getAssetId();
		List assetPresentationIds = session.createQuery(hql).list();
		
		// Delete all PlaylistAsset objects that are associated with this asset
		hql = "DELETE PlaylistAsset "
			+ "WHERE asset_id = "+ this.getAssetId();
		session.createQuery(hql).executeUpdate();
		
		if(assetPresentationIds.size() > 0){
			// Delete the asset presentation for the playlist assets associated to this asset
			hql = "DELETE AssetPresentation WHERE  asset_presentation_id IN (:ids)";
			session.createQuery( hql ).setParameterList("ids", assetPresentationIds).executeUpdate();
		}
		HibernateSession.stopBulkmode();
		
		// NOTE: We are intentionally not re-ordering the assets of each playlist that 
		// was affected for performance reasons		
	}
	
	/**
	 * Although this method is named "uploadLargeFile", we are not currently
	 * uploading files of type video or audio. The current implementation of this 
	 * method will create a physical link to the file location specified on the form.
	 * 
	 * @param assetId
	 * @param f
	 * @return
	 */
	public String uploadLargeFile(String origFilePath, boolean waitForFileUpload) throws HibernateException, InterruptedException
	{       				
        // Extract the file extension from the file
		String newFilePath = "";
		if( origFilePath != null && origFilePath.indexOf(".") > 0 )
		{
	        String fileExt = origFilePath.substring( origFilePath.lastIndexOf(".") ).toLowerCase();        
	        newFilePath = Asset.getAssetDirectory( this.getAssetType() ) +"/"+ this.getAssetId().toString() + fileExt;
			newFilePath = newFilePath.replaceAll("\\\\", "/");
			
	        // Make sure the original file exists
	        File file = new File( origFilePath );
	        if(file.exists() == true)
	        {				
	        	// Copy the file
				// Peform the actual file copy in a separate thread
				// so that the the interface does not wait
				FileCopyThread t = this.new FileCopyThread(){
					public void run(){
						try{
							
							SchemaDirectory.initialize( this.getSchemaName(), "Asset - UploadLargeFile", this.getAppUserId(), false, false );
							
							// Copy the file
							Files.copyFile( this.getOrigFilePath(), this.getNewFilePath(), true );
							
							// Attempt to get a reference to the new file
							File newFile = new File( this.getNewFilePath() );
							long newFilesize = 0;
							Long adler32 = new Long(0);
							String fileloc = this.getNewFilePath();
							if( newFile != null ){							
								newFilesize = newFile.length();
								
								// Calculate the adler32 value for this asset and use it to build the filename
								adler32 = Asset.calculateAdler32( this.getNewFilePath() );
								fileloc = Asset.buildFileloc( this.getNewFilePath(), adler32 );					
								
								// Rename the file using its adler32
								Files.renameTo( newFile, new File(fileloc) );
							}
							
							// Calculate adler32 and create the thumbnail image after copying the new file
							Asset a = Asset.getAsset( this.getAssetId() );							
							if( a instanceof Audio ){
								Audio audio = (Audio)a;								
								audio.setFileloc( fileloc );
								audio.setAdler32( adler32 );
								audio.setFilesize( newFilesize );
								audio.createThumbnail( Asset.MAX_DIMENSION );
								audio.setIntrinsicProperties();
								audio.update();
							}else if( a instanceof Flash ){
								Flash flash = (Flash)a;
								flash.setFileloc( fileloc );
								flash.setAdler32( adler32 );
								flash.setFilesize( newFilesize );
								flash.createThumbnail( Asset.MAX_DIMENSION );
								flash.update();
							}else if( a instanceof Html ){
								Html html = (Html)a;
								html.setFileloc( fileloc );
								html.setAdler32( adler32 );
								html.setFilesize( newFilesize );
								html.createThumbnail( Asset.MAX_DIMENSION );
								html.update();
							}else if( a instanceof Xml ){
								Xml xml = (Xml)a;
								xml.setFileloc( fileloc );
								xml.setAdler32( adler32 );
								xml.setFilesize( newFilesize );
								xml.createThumbnail( Asset.MAX_DIMENSION );
								xml.update();
							}else if( a instanceof Image ){
								Image image = (Image)a;
								image.setFileloc( fileloc );
								image.setAdler32( adler32 );
								image.createThumbnail( Asset.MAX_DIMENSION );
								image.setFilesize( newFilesize );
								image.setIntrinsicProperties();
								image.update();
							}else if( a instanceof Video ){
								Video video = (Video)a;
								video.setFileloc( fileloc );
								video.setAdler32( adler32 );
								video.setFilesize( newFilesize );								
								video.createThumbnail( Asset.MAX_DIMENSION );
								video.setIntrinsicProperties();
								video.update();
							}else if( a instanceof Webapp ){
								Webapp webapp = (Webapp)a;
								webapp.setFileloc( fileloc );
								webapp.setAdler32( adler32 );
								webapp.setFilesize( newFilesize );
								webapp.createThumbnail( Asset.MAX_DIMENSION );
								webapp.update();
							}
						}
						catch(Exception e)
						{
							logger.error("An unexpected error occurred in FileCopyThread.", e );
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
				};		
				t.setAssetId( this.getAssetId() );
				t.setOrigFilePath( origFilePath );
				t.setNewFilePath( newFilePath );
				t.setSchemaName( SchemaDirectory.getSchema().getSchemaName() );				

				
				// Do not allow this thread to die, even if the parent thread finishes
				t.setDaemon( true );
				t.start();		
				
				// If the flag was set to wait for the file to finish uploading -- wait
				if( waitForFileUpload ){
					t.join();
				}
	        }       
	        else {
	        	logger.info("Unable to upload file. File not found: "+ origFilePath);
	        }
		}else{
			logger.info("Unable to upload large file. Invalid file path: "+ origFilePath);
		}
        return newFilePath;
	}		
	
	

	
	
	/**
	 * Builds the hql to retrieve assets according to the given search criteria.
	 *  
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param getCount
	 * @return
	 */
	public static String buildSearchHql(AttrDefinition attrDefinition, AssetSearchType assetSearchType, String assetSearchString, String[] selectedAssetTypes, String selectedSearchOption, String searchString, 
			String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber, AssetType assetTypeToExclude, Long assetIdToExclude, String orderBy,
			boolean getCount, boolean getAssetOnly, boolean addWildcards, boolean includeControlAssetTypes, boolean excludeExpiredAssets)
	{
		String hql = "";
		
		// Trim input boxes
		assetSearchString = assetSearchString != null && assetSearchString.length() > 0 ? assetSearchString.trim() : assetSearchString;
		searchString = searchString != null && searchString.length() > 0 ? searchString.trim() : searchString;
		
		// If the assetName search string was left blank, use wildcard
		if( assetSearchString == null || assetSearchString.trim().length() == 0 ){
			assetSearchString = "%";
		}
		
		// If this is a list of asset names
		String assetNamesList = "";
		String assetIdsList = assetSearchString;
		if(assetSearchString.contains("~")){
			if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_NAME.getPersistentValue() ) ){
				for(String s : assetSearchString.split("~")){
					assetNamesList += assetNamesList.length() > 0 ? ",'" + s.trim() + "'" : "'" + s.trim() + "'";
				}
			}else if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_ID.getPersistentValue() ) ){
				assetIdsList = "";
				for(String s : assetSearchString.split("~")){
					assetIdsList += assetIdsList.length() > 0 ? "," + s.trim() : s.trim();
				}
			}
		}
		// Imply *
		else if(addWildcards){
			if(assetSearchString.startsWith("*") == false){
				assetSearchString = "*" + assetSearchString;
			}
			if(assetSearchString.endsWith("*") == false){
				assetSearchString = assetSearchString + "*";
			}
			if(searchString != null && searchString.length() > 0){
				if(searchString.startsWith("*") == false){
					searchString = "*" + searchString;
				}
				if(searchString.endsWith("*") == false){
					searchString = searchString + "*";
				}
			}
		}
		
		// Convert any "*" to "%" for wildcard searches
		assetSearchString = assetSearchString.replaceAll("\\*", "\\%");	
		assetSearchString = Reformat.oraesc(assetSearchString);		
		
		// If the orderBy clause was not passed in -- use default 
		if( orderBy == null || orderBy.length() <= 0 ) {
			orderBy = "UPPER(asset.assetName)";	
		}
				
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(asset) ";
		}else if( getAssetOnly == true ){
			hql = "SELECT asset ";
		}
						
		// If the "All Asset Types" string was passed in
		String assetTypes = "";
		if( selectedAssetTypes.length == 1 && (selectedAssetTypes[0].equalsIgnoreCase( Constants.ALL_ASSET_TYPES ) || selectedAssetTypes[0].equalsIgnoreCase("-1")) )
		{
			// Build the string of asset types
			for( Iterator<AssetType> i=AssetType.getAssetTypes(false, includeControlAssetTypes).iterator(); i.hasNext(); )
			{
				// Do not include the assetTypeToExclude (if defined)
				AssetType assetType = i.next();
				if( assetTypeToExclude == null || assetType.getPersistentValue().equalsIgnoreCase( assetTypeToExclude.getPersistentValue() ) == false )
				{
					if( assetTypes.length() > 0 ){
						assetTypes += ", ";					
					}				
					String assetTypeClassName = assetType.getPersistentValue().substring( assetType.getPersistentValue().lastIndexOf(".") + 1 );
					assetTypes += "'"+ assetTypeClassName +"'";	
				}
			}
		}
		else
		{
			// Build the string of asset types		
			for( int i=0; i<selectedAssetTypes.length; i++ )
			{
				if( assetTypes.length() > 0 ){
					assetTypes += ", ";					
				}
				String assetTypeClassName = selectedAssetTypes[i].substring( selectedAssetTypes[i].lastIndexOf(".") + 1 ); 
				assetTypes += "'"+ assetTypeClassName +"'";
			}			
		}

		/*
		 * If the "No Metadata", or "Date Modified" option was selected, exclude the metadata from the search criteria 
		 */
		boolean excludeMetadataCriteria = false;
		if( selectedSearchOption.equalsIgnoreCase( Constants.NO_METADATA ) || selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ))
		{
			excludeMetadataCriteria = true;
		}
		else
		{
			/*
			 * Search by this attr definition
			 */
			AttrDefinition ad = attrDefinition != null ? attrDefinition : AttrDefinition.getAttrDefinition( new Long( selectedSearchOption ) );
			if( ad != null)
			{				
				// If this is a String attr
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If this is a multi-select
					if( ad.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) )
					{
						// If no items were selected
						if( selectedSearchOptions== null || selectedSearchOptions.length == 0 )
						{
							// Exclude the attrDefinition criteria							
							excludeMetadataCriteria = true;
						}
						else
						{														
							// Get all assets that have a StringAttr with the given criteria
							hql += "FROM Asset as asset "
								+ "WHERE asset.class IN ("+ assetTypes +") "
								+ "AND asset.assetId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" ";
							
							String searchOptionList = "";
							for(String s : selectedSearchOptions){
								searchOptionList += searchOptionList.length() > 0 ? ",?" : "?";
							}
							hql += " AND attr.value IN ("+ searchOptionList +") ) ";
							
							// Search according to the specified assetSearchType
							if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_NAME.getPersistentValue() ) ){
								if(assetNamesList != null && assetNamesList.length() > 0){
									hql += "AND asset.assetName IN (" + assetNamesList + ") ";
								}else{
									hql += "AND UPPER(asset.assetName) LIKE UPPER('"+ assetSearchString +"') ";
								}
							}else if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_ID.getPersistentValue() ) ){
								hql += "AND asset.assetId IN ("+ assetIdsList +") ";
							}
							
							// Exclude assets
							if(assetIdToExclude != null){
								hql += "AND asset.assetId != " + assetIdToExclude + " ";
							}
			
							if (excludeExpiredAssets) {
								hql += "AND (asset.endDate = null OR asset.endDate >= ?) ";
							}
							
							hql += "ORDER BY "+ orderBy;									
						}																		
					}
					else
					{						
						// If the searchString was left blank 
						if( searchString == null || searchString.trim().length() == 0 )
						{
							// Exclude the attrDefinition criteria					
							excludeMetadataCriteria = true;						
						}
						else
						{
							// Convert any "*" to "%" for wildcard searches		
							searchString = searchString.replaceAll("\\*", "\\%");
							searchString = Reformat.oraesc(searchString);											
							
							// Get all assets that have a StringAttr with the given criteria
							hql += "FROM Asset as asset "
								+ "WHERE asset.class IN ("+ assetTypes +") "								
								+ "AND asset.assetId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER('"+ searchString +"') ) ";
							
							// Search according to the specified assetSearchType
							if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_NAME.getPersistentValue() ) ){
								if(assetNamesList != null && assetNamesList.length() > 0){
									hql += "AND asset.assetName IN (" + assetNamesList + ") ";
								}else{
									hql += "AND UPPER(asset.assetName) LIKE UPPER('"+ assetSearchString +"') ";
								}
							}else if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_ID.getPersistentValue() ) ){
								hql += "AND asset.assetId IN ("+ assetIdsList +") ";
							}
							
							// Exclude assets
							if(assetIdToExclude != null){
								hql += "AND asset.assetId != " + assetIdToExclude + " ";
							}
							
							if (excludeExpiredAssets) {
								hql += "AND (asset.endDate = null OR asset.endDate >= ?) ";
							}
							
							hql += "ORDER BY "+ orderBy;								
						}										
					}
				}
				// If this is a Date attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// If both the min and max dates were left blank
					if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a DateAttr.value between the two dates
						hql += "FROM Asset as asset "
							+ "WHERE asset.class IN ("+ assetTypes +") "
							+ "AND asset.assetId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						
							// Search according to the specified assetSearchType
							if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_NAME.getPersistentValue() ) ){
								if(assetNamesList != null && assetNamesList.length() > 0){
									hql += "AND asset.assetName IN (" + assetNamesList + ") ";
								}else{
									hql += "AND UPPER(asset.assetName) LIKE UPPER('"+ assetSearchString +"') ";
								}
							}else if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_ID.getPersistentValue() ) ){
								hql += "AND asset.assetId IN ("+ assetIdsList +") ";
							}
							
							// Exclude assets
							if(assetIdToExclude != null){
								hql += "AND asset.assetId != " + assetIdToExclude + " ";
							}
							
							if (excludeExpiredAssets) {
								hql += "AND (asset.endDate = null OR asset.endDate >= ?) ";
							}
							
							hql += "ORDER BY "+ orderBy;					
					}
				}
				// If this is a Number attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If both the min and max numbers were left blank
					if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a DateAttr.value between the two dates
						hql += "FROM Asset as asset "
							+ "WHERE asset.class IN ("+ assetTypes +") "
							+ "AND asset.assetId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						
						// Search according to the specified assetSearchType
						if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_NAME.getPersistentValue() ) ){
							if(assetNamesList != null && assetNamesList.length() > 0){
								hql += "AND asset.assetName IN (" + assetNamesList + ") ";
							}else{
								hql += "AND UPPER(asset.assetName) LIKE UPPER('"+ assetSearchString +"') ";
							}
						}else if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_ID.getPersistentValue() ) ){
							hql += "AND asset.assetId IN ("+ assetIdsList +") ";
						}
						
						// Exclude assets
						if(assetIdToExclude != null){
							hql += "AND asset.assetId != " + assetIdToExclude + " ";
						}
						
						if (excludeExpiredAssets) {
							hql += "AND (asset.endDate = null OR asset.endDate >= ?) ";
						}
						
						hql += "ORDER BY "+ orderBy;							
					}
				}				
			}
			// If we did not find a valid AttrDefinition -- attempt to search for any StringAttr
			else
			{
				// Convert any "*" to "%" for wildcard searches		
				searchString = searchString.replaceAll("\\*", "\\%");
				searchString = Reformat.oraesc(searchString);	
				
				// Get all assets that have a StringAttr with the given criteria
				hql += "FROM Asset as asset "	
					+ "WHERE asset.assetId IN "
					+ 	"(SELECT attr.ownerId "
					+	" FROM StringAttr attr "				
					+	" WHERE UPPER(attr.value) LIKE UPPER('"+ searchString +"')) ";
				
				// Exclude assets
				if(assetIdToExclude != null){
					hql += "AND asset.assetId != " + assetIdToExclude + " ";
				}
				
				if (excludeExpiredAssets) {
					hql += "AND (asset.endDate = null OR asset.endDate >= ?) ";
				}
				
				hql += "ORDER BY "+ orderBy;
			}
		}
	
		// If we're not excluding the metadata criteria in the query
		if( excludeMetadataCriteria )
		{
			hql += "FROM Asset as asset "
				+ "WHERE asset.class IN ("+ assetTypes +") ";
				
			// Search according to the specified assetSearchType
			if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_NAME.getPersistentValue() ) ){
				if(assetNamesList != null && assetNamesList.length() > 0){
					hql += "AND asset.assetName IN (" + assetNamesList + ") ";
				}else{
					hql += "AND UPPER(asset.assetName) LIKE UPPER('"+ assetSearchString +"') ";
				}
			}else if( assetSearchType.getPersistentValue().equalsIgnoreCase( AssetSearchType.ASSET_ID.getPersistentValue() ) ){
				hql += "AND asset.assetId IN ("+ assetIdsList +") ";
			}			
			
			// If we're filtering by last modified date
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
			{
				hql += " AND asset.lastModified >= ? "
					+  " AND asset.lastModified <= ? ";				
			}

			// Exclude assets
			if(assetIdToExclude != null){
				hql += "AND asset.assetId != " + assetIdToExclude + " ";
			}
			
			if (excludeExpiredAssets) {
				hql += "AND (asset.endDate = null OR asset.endDate >= ?) ";
			}
			
			hql += "ORDER BY "+ orderBy;	
		}
		return hql;
	}	
	
	/**
	 * Copies the paired assets of the this asset to the given asset presentation.
	 * Does not copy paired displayareas if the paired displayarea is not associated
	 * with the layout of the given asset presentation.
	 * 
	 * @param selectedAsset
	 * @param ap
	 */
	public void copyPairedAssets(AssetPresentation ap)
	{
		// Copy the paired assets from the selected asset's
		for( Iterator i=this.getAssetPresentation().getPairedDisplayareas().iterator(); i.hasNext(); )
		{
			PairedDisplayarea pda = (PairedDisplayarea)i.next();
			
			// Make sure this paired displayarea exists in the asset presentation's layout
			boolean displayareaExists = false;
			for(Iterator j=ap.getLayout().getLayoutDisplayareas().iterator(); j.hasNext(); )
			{
				LayoutDisplayarea lda = (LayoutDisplayarea)j.next();
				if( lda.getDisplayarea().getDisplayareaId().equals( pda.getDisplayarea().getDisplayareaId() ) ) {
					displayareaExists = true;
					break;
				}
			}
			
			// Only copy this paired displayarea if this displayarea exists in the asset presentation's layout
			if( displayareaExists ){
				PairedDisplayarea newPda = new PairedDisplayarea();
				newPda.setAssetPresentation( ap );
				newPda.setDisplayarea( pda.getDisplayarea() );
				newPda.save();
				for( Iterator j=pda.getPairedAssets().iterator(); j.hasNext(); )
				{
					PairedAsset pairedAsset = (PairedAsset)j.next();
					PairedAsset newPairedAsset = new PairedAsset();
					newPairedAsset.setPairedDisplayarea( newPda );
					newPairedAsset.setAsset( pairedAsset.getAsset() );
					newPairedAsset.setLength( pairedAsset.getLength() );
					newPairedAsset.setSeqNum( pairedAsset.getSeqNum() );
					newPairedAsset.save();
				}
			}
		}	
	}
	
	private void updatePlaylistAssetsWithActualLength(Float actualLength, Float oldActualLength){
		Session session = HibernateSession.currentSession();
		String hql = "UPDATE AssetPresentation ap SET ap.length = :actualLength "
					+"WHERE ap.assetPresentationId IN "
					+"( SELECT pa.assetPresentation.assetPresentationId FROM PlaylistAsset as pa "
					+ "WHERE pa.asset.assetId = :assetId AND (pa.assetPresentation.length = :playLengthPlaceholder OR pa.assetPresentation.length = :oldActualLength))";
		int count = session.createQuery(hql).setParameter("actualLength", actualLength).setParameter("assetId", this.getAssetId()).
		setParameter("playLengthPlaceholder", Float.parseFloat(Constants.PLAY_LENGTH_PLACEHOLDER)).setParameter("oldActualLength", oldActualLength).executeUpdate();
		
		// If we did end up changing some asset presentations on the playlist, dirty the playlists
		if(count > 0){
			hql = "SELECT pa.playlist FROM PlaylistAsset as pa WHERE pa.asset.assetId = :assetId AND pa.assetPresentation.length = :actualLength";
			List<Playlist> l = session.createQuery(hql).setParameter("assetId", this.getAssetId()).setParameter("actualLength", actualLength).list();
			for(Playlist p : l){
				p.makeDirty();
			}
		}
	}
	
	private void updateContentRotationAssetsWithActualLength(Float actualLength, Float oldActualLength){
		Session session = HibernateSession.currentSession();
		String hql = "UPDATE ContentRotationAsset cra SET cra.length = :actualLength "
					+"WHERE cra.asset.assetId = :assetId AND (cra.length = :playLengthPlaceholder OR cra.length = :oldActualLength)";
		int count = session.createQuery(hql).setParameter("actualLength", actualLength).setParameter("assetId", this.getAssetId()).
		setParameter("playLengthPlaceholder", Float.parseFloat(Constants.PLAY_LENGTH_PLACEHOLDER)).setParameter("oldActualLength", oldActualLength).executeUpdate();
		
		// If we did end up changing some asset presentations on the playlist, dirty the playlists
		if(count > 0){
			hql = "SELECT cra.contentRotation FROM ContentRotationAsset as cra WHERE cra.asset.assetId = :assetId AND cra.length = :actualLength";
			List<ContentRotation> l = session.createQuery(hql).setParameter("assetId", this.getAssetId()).setParameter("actualLength", actualLength).list();
			for(ContentRotation cr : l){
				cr.makeDirty();
			}
		}
	}

	/*
	 * This method is used to propagate asset (video or audio) length changes to the respective
	 * presentations upon calculation of their actual length
	 */
	public void updatePresentations(Float actualLength, Float oldActualLength){
		
		// If actual length is other than -1
		if(actualLength.equals( new Float(Constants.INTRINSIC_LENGTH_PLACEHOLDER) ) == false){
			
			// If the length property of this asset's assetPresentation is "15.001" or the old actual length, re-set it
			if( this.getAssetPresentation().getLength().equals( new Float(Constants.PLAY_LENGTH_PLACEHOLDER) ) || this.getAssetPresentation().getLength().equals(oldActualLength)){
				this.getAssetPresentation().setLength( actualLength );
				this.getAssetPresentation().update();
				
				// Update all presentations with new length
				updatePlaylistAssetsWithActualLength(actualLength, oldActualLength);
				updateContentRotationAssetsWithActualLength(actualLength, oldActualLength);
			}
		}
	}
	
	/**
	 * 
	 * @param metadataField
	 * @return
	 * @throws Exception
	 */
	public String getCustomMetadata(String metadataField) throws Exception
	{		
		return getMetadataInfo( Asset.class.getName(), metadataField );
	}

	protected String getFileExtension(String filename)
    {
	    String extension = "";
	    int dot = filename.lastIndexOf(".");
	    if( dot>0 && dot<filename.length() ){
	        extension = filename.substring(dot).toLowerCase();
	    }
	    return extension;
    }
	
	public String getLastModifiedFormatted() {
		String result = "";
		if( this.getLastModified() != null ){
			result = EntityInstance.dateFormat.format( this.getLastModified() );
		}
		return result;
	}
	
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		
		Asset a = null;
		if( other instanceof IAsset )
		{
			a = Asset.convert( (IAsset)other );
		}else if( other instanceof Asset ){
			a = (Asset)other;
		}
		
		if( a != null )
		{
			if(this.hashCode() == a.hashCode()){
				result =  true;	
			}
		}else{
			result = false;
		}
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "Asset".hashCode();
		result = Reformat.getSafeHash( this.getAssetId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getAssetName(), result, 11 );
		return result < 0 ? -result : result;		
	}
	
	public int eTag()
	{
		int result = "Asset".hashCode();
		result = Reformat.getSafeHash( this.getAssetId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getAssetName(), result, 5 );
		result = Reformat.getSafeHash( this.getAssetType().getPersistentValue(), result, 7 );
		result = Reformat.getSafeHash( this.getHeight(), result, 11 );
		result = Reformat.getSafeHash( this.getWidth(), result, 13 );
		if(this.getAssetPresentation() != null){
			result = Reformat.getSafeHash( this.getAssetPresentation().getLayout().getLayoutId().toString(), result, 17 );
			result = Reformat.getSafeHash( this.getAssetPresentation().getDisplayarea().getDisplayareaId(), result, 19 );
			result = Reformat.getSafeHash( this.getAssetPresentation().getLength(), result, 23 );
		}
		return result < 0 ? -result : result;		
	}
	/**
	 * 
	 * @return
	 */
	public Set getPairedAssetsSorted()
	{
		return null;
	}

	/**
	 * @return Returns the assetId.
	 */
	public Long getAssetId() {
		return assetId;
	}

	/**
	 * @param assetId The assetId to set.
	 */
	public void setAssetId(Long assetId) {
		this.assetId = assetId;
	}

	/**
	 * @return Returns the assetName.
	 */
	public String getAssetName() {
		return assetName;
	}

	/**
	 * @param assetName The assetName to set.
	 */
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	/**
	 * @return Returns the assetPresentation.
	 */
	public AssetPresentation getAssetPresentation() {
		return assetPresentation;
	}

	/**
	 * @param assetPresentation The assetPresentation to set.
	 */
	public void setAssetPresentation(AssetPresentation assetPresentation) {
		this.assetPresentation = assetPresentation;
	}

	/**
	 * @return Returns the assetSegmentParts.
	 */
	public Set getAssetSegmentParts() {
		return assetSegmentParts;
	}

	/**
	 * @param assetSegmentParts The assetSegmentParts to set.
	 */
	public void setAssetSegmentParts(Set assetSegmentParts) {
		this.assetSegmentParts = assetSegmentParts;
	}

	/**
	 * @return Returns the contentRotationAssets.
	 */
	public Set getContentRotationAssets() {
		return contentRotationAssets;
	}

	/**
	 * @param contentRotationAssets The contentRotationAssets to set.
	 */
	public void setContentRotationAssets(Set contentRotationAssets) {
		this.contentRotationAssets = contentRotationAssets;
	}

	/**
	 * @return Returns the pairedAssets.
	 */
	public Set getPairedAssets() {
		return pairedAssets;
	}

	/**
	 * @param pairedAssets The pairedAssets to set.
	 */
	public void setPairedAssets(Set pairedAssets) {
		this.pairedAssets = pairedAssets;
	}

	/**
	 * @return Returns the playlistAssets.
	 */
	public Set getPlaylistAssets() {
		return playlistAssets;
	}

	/**
	 * @param playlistAssets The playlistAssets to set.
	 */
	public void setPlaylistAssets(Set playlistAssets) {
		this.playlistAssets = playlistAssets;
	}
	
	/**
	 * @return Returns the assetExclusions.
	 */
	public Set getAssetExclusions() {
		return assetExclusions;
	}
	
	/**
	 * @param assetExclusions The assetExclusions to set.
	 */
	public void setAssetExclusions(Set assetExclusions) {
		this.assetExclusions = assetExclusions;
	}

	/**
	 * Should not have to implement these methods!!
	 * Restriction on Hibernate's polymorphic subclass implementation,
	 * where the superclass proxy (IAsset) must implement all subclass methods.
	 */
	public Integer getWidth() {
		return null;
	}
	public Integer getHeight() {
		return null;
	}	
	public Float getLength() {
		return null;
	}
	public String getFileloc() {
		return null;
	}
	public Long getAdler32() {
		return null;	
	}
	public DefaultAssetAffinityType getDefaultAssetAffinityType() {
		return null;
	}
	public Set<TargetedAssetMember> getTargetedAssetMembers() {
		return null;
	}
		
	
	private class FileCopyThread extends Thread {
		private String origFilePath;
		private String newFilePath;
		private Long assetId;
		private String schemaName;
		private String appUserId;
		
		/**
		 * @return Returns the newFilePath.
		 */
		public String getNewFilePath() {
			return newFilePath;
		}		
		/**
		 * @param newFilePath The newFilePath to set.
		 */
		public void setNewFilePath(String newFilePath) {
			this.newFilePath = newFilePath;
		}		
		/**
		 * @return Returns the origFilePath.
		 */
		public String getOrigFilePath() {
			return origFilePath;
		}		
		/**
		 * @param origFilePath The origFilePath to set.
		 */
		public void setOrigFilePath(String origFilePath) {
			this.origFilePath = origFilePath;
		}
		/**
		 * @return Returns the asset.
		 */
		public Long getAssetId() {
			return assetId;
		}
		
		/**
		 * @param asset The asset to set.
		 */
		public void setAssetId(Long assetId) {
			this.assetId = assetId;
		}
		/**
		 * @return Returns the schemaName.
		 */
		public String getSchemaName() {
			return schemaName;
		}
		
		/**
		 * @param schemaName The schemaName to set.
		 */
		public void setSchemaName(String schemaName) {
			this.schemaName = schemaName;
		}
		/**
		 * @return Returns the appUserId.
		 */
		public String getAppUserId() {
			return appUserId;
		}
		
		/**
		 * @param appUserId The appUserId to set.
		 */
		public void setAppUserId(String appUserId) {
			this.appUserId = appUserId;
		}			
		
	}
	public String getAdserverIdentifier() {
		return adserverIdentifier;
	}
	public void setAdserverIdentifier(String adserverIdentifier) {
		this.adserverIdentifier = adserverIdentifier;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public IAsset getDefaultAsset() {
		return defaultAsset;
	}
	public void setDefaultAsset(IAsset defaultAsset) {
		this.defaultAsset = defaultAsset;
	}
}
