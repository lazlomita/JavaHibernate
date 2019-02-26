/*
 * Created on Oct 29, 2004
 */
package com.kuvata.kmf.asset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.AssetType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.util.Files;

/**
 * @author jrandesi
 */
public class Webapp extends Asset implements IWebapp
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.WebappPresentation";

	private String fileloc;
	private Long adler32;
	private Long filesize;
	private String startPage;
	private Integer overlayOpacity;
	private String originalFilename;
	private Boolean setTransparentBg;
	private static String createAssetPage = "createAssetWebapp";
	private static String assetPropertiesPage = "assetPropertiesWebapp";
	
	public Webapp()
	{}
	
	public String getPresentationType()
	{
	    return PRESENTATION_TYPE;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getCreateAssetPage()
	{
		return Webapp.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Webapp.assetPropertiesPage; 
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.WEBAPP;
	}	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension) throws FileNotFoundException, IOException
	{		
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getPreviewPath()
	{
		if( previewPath == null )
		{
			previewPath = "";
			if( this.getFileloc() != null ) {
				if( this.getFileloc().indexOf(".") >= 0 ){
					String fileExt = this.getFileloc().substring(this.getFileloc().lastIndexOf("."));
					if( this.getFileloc().indexOf("/") >= 0 ){
						String fileName = this.getFileloc().substring( this.getFileloc().lastIndexOf("/") + 1, this.getFileloc().lastIndexOf(".") );
						fileName += fileExt;
						previewPath = "/"+ Constants.SCHEMAS +
								"/"+ SchemaDirectory.getSchema().getSchemaName() +		
								"/"+ Constants.ASSETS_DIR +
								"/"+ AssetType.WEBAPP.getPartialPersistentValue() +
								"/"+ fileName;	
					}
				}
			}
		}		
		return previewPath;		
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getThumbnailPath()
	{
		if( thumbnailPath == null )
		{
			thumbnailPath = "/"+ Constants.APP_NAME +"/images/icons/web_application.gif";
		}
		return thumbnailPath;			
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{		
		return "<img src=\""+ getThumbnailPath() +"\">";			
	}	
	
	/**
	 * Implements the parent's abstract method. Removes the referenced file
	 * associated with this asset if any, and calls Entity.delete() to remove
	 * this object from the database.
	 */
	public void delete() throws HibernateException
	{		
		if( this.getFileloc() != null )
		{
			File f = new File( this.getFileloc() );
			if( f.exists() )
			{
				f.delete();
			}
		}
		super.delete();
	}	
		
	/**
	 * Implements the parent's abstract method
	 * Determines if this asset's referenced file exists on disk
	 */
	public boolean getReferencedFileExists()
	{
		return Asset.referencedFileExists( this.getFileloc() );
	}	
	
	public static Webapp create(String assetName, Float length, String startPage, Displayarea da, Layout l, boolean useFileLocFromServer, 
			String fileLocation, Integer overlayOpacity, Boolean setTransparentBg, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Webapp a = new Webapp();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setFileloc( "" );			
		a.setStartPage( startPage );
		a.setFileloc( fileLocation );
		a.setOverlayOpacity(overlayOpacity);
		a.setSetTransparentBg(setTransparentBg);
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			a.setOriginalFilename(originalFilename);
		}else if(fileLocation != null && fileLocation.length() > 0){
			fileLocation = fileLocation.replaceAll("\\\\", "/");
			a.setOriginalFilename(fileLocation.substring(fileLocation.lastIndexOf("/") + 1));
		}
		
		Long assetId = a.save();
					
		// If we're using a fileloc on the server
		if( useFileLocFromServer )
		{
			// Create a physical link to the file
			a.uploadLargeFile( fileLocation, waitForFileUpload );
		}				
		return a;
	}
	
	public void update(String assetName, Float length, String startPage, Displayarea da, Layout l,
			boolean useFileLocFromServer, String fileLocation, Integer overlayOpacity, Boolean setTransparentBg, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = this.getAssetPresentation();
		
		// If the default length has changed
		boolean lengthChanged = false;
		if( length != null && ap.getLength().equals( length ) == false ) {
			ap.setLength( length );
			lengthChanged = true;
		}			
		this.makeDirty( lengthChanged, false );	
					
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.update();
		
		// If we're not using a fileloc from the server
		if( useFileLocFromServer == false )
		{
			// If the fileloc property has changed (it was not left empty) 
			if( fileLocation.length() > 0 && this.getFileloc().equals( fileLocation ) == false)
			{				
				String fileLoc = "/temp" + getFileExtension(fileLocation);
				this.setFileloc( fileLoc );
				this.createThumbnail( Asset.MAX_DIMENSION );
								
			}				
		}
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			this.setOriginalFilename(originalFilename);
		}else if(fileLocation != null && fileLocation.length() > 0){
			fileLocation = fileLocation.replaceAll("\\\\", "/");
			this.setOriginalFilename(fileLocation.substring(fileLocation.lastIndexOf("/") + 1));
		}
		
		this.setAssetName( assetName );			
		this.setAssetPresentation( ap );											
		this.setStartPage( startPage );
		this.setOverlayOpacity(overlayOpacity);
		this.setSetTransparentBg(setTransparentBg);
		this.update();
		
		// If we're using a fileloc from the server
		if( useFileLocFromServer )
		{
			// If the fileloc property has changed -- re-upload the file
			if( this.getFileloc() == null || this.getFileloc().equals( fileLocation ) == false)
			{
				if( fileLocation != null && fileLocation.length() > 0 )
				{
					// Retrieve the asset file
					this.uploadLargeFile( fileLocation, waitForFileUpload );
				}
			}
		}
	}
	
	/**
	 * Copies this asset and assigns the given new asset name.
	 * 
	 * @param newAssetName
	 * @return
	 */
	public Long copy(String newAssetName) throws ClassNotFoundException, InterruptedException
	{				
		// Create a new asset object
		Webapp newAsset = new Webapp();		
		newAsset.setAssetName( newAssetName );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setStartPage( this.getStartPage() );
		newAsset.setOverlayOpacity( this.getOverlayOpacity() );
		newAsset.setSetTransparentBg( this.getSetTransparentBg() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		
		// Copy the referenced file and set the associated properties
		newAsset.uploadLargeFile( this.getFileloc(), true );			
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}	
	
	/**
	 * @return Returns the fileloc.
	 */
	public String getFileloc() {
		return fileloc;
	}
	/**
	 * @param fileloc The fileloc to set.
	 */
	public void setFileloc(String fileloc) {
		this.fileloc = fileloc;
	}
	/**
	 * @return Returns the webappId.
	 */
	public Long getWebappId() {
		return this.getAssetId();
	}
	/**
	 * @param webappId The webappId to set.
	 */
	public void setWebappId(Long webappId) {
		this.setAssetId( webappId );
	}
	/**
	 * @return Returns the adler32.
	 */
	public Long getAdler32() {
		return adler32;
	}
	/**
	 * @param adler32 The adler32 to set.
	 */
	public void setAdler32(Long adler32) {
		this.adler32 = adler32;
	}
	/**
	 * @return Returns the startPage.
	 */
	public String getStartPage() {
		return startPage;
	}
	/**
	 * @param startPage The startPage to set.
	 */
	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}
	/**
	 * @return Returns the filesize.
	 */
	public Long getFilesize() {
		return filesize;
	}
	/**
	 * @param filesize The filesize to set.
	 */
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	public Integer getOverlayOpacity() {
		return overlayOpacity;
	}

	public void setOverlayOpacity(Integer overlayOpacity) {
		this.overlayOpacity = overlayOpacity;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public Boolean getSetTransparentBg() {
		return setTransparentBg;
	}

	public void setSetTransparentBg(Boolean setTransparentBg) {
		this.setTransparentBg = setTransparentBg;
	}
}