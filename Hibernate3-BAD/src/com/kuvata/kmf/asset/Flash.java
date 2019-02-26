/*
 * Created on Oct 29, 2004
 */
package com.kuvata.kmf.asset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.upload.FormFile;
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
public class Flash extends Asset implements IFlash
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.FlashPresentation";

	private Integer width;
	private Integer height;
	private String fileloc;
	private Long adler32;
	private Long filesize;
	public Boolean fitToSize;
	private Integer overlayOpacity;
	private String originalFilename;
	private Boolean setTransparentBg;
	private static String createAssetPage = "createAssetFlash";
	private static String assetPropertiesPage = "assetPropertiesFlash";
	/**
	 * 
	 *
	 */
	public Flash()
	{}		
		
	/**
	 * 
	 */
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
		return Flash.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Flash.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.FLASH;
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension) throws FileNotFoundException, IOException
	{		
	}
	
	/**
	 * Implements the parent's abstract method
	 * Determines if this asset's referenced file exists on disk
	 */
	public boolean getReferencedFileExists()
	{
		return Asset.referencedFileExists( this.getFileloc() );
	}
	
	/**	
	 * Returns the relative path to the flash file
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
								"/"+ AssetType.FLASH.getPartialPersistentValue() +
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
			thumbnailPath = "/"+ Constants.APP_NAME +"/images/flash_tbn.gif";	
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
	
	public static Flash create(HttpServletRequest request, HttpServletResponse response, String assetName, Float length, 
			Integer width, Integer height, Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, FormFile formFile, String flashBackgroundColor, Boolean setTransparentBg,
			Integer overlayOpacity, Boolean fitToSize, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Flash a = new Flash();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setFileloc( "" );			
		a.setWidth( width );
		a.setHeight( height);
		a.setFitToSize( fitToSize );
		a.setFileloc( fileLocation );
		a.setOverlayOpacity(overlayOpacity);
		a.setSetTransparentBg(setTransparentBg);
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			a.setOriginalFilename(originalFilename);
		}else if(formFile != null){
			a.setOriginalFilename(formFile.getFileName());
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
		else
		{
			setAttributesForUpload( request, a.getAssetId().toString(), a.getAssetName(), fileLocation );
		}				
		return a;
	}
	
	public Flash update(HttpServletRequest request, HttpServletResponse response, String assetName, Float length, Integer width, Integer height,
			Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, FormFile formFile, String flashBackgroundColor, Boolean setTransparentBg,
			Integer overlayOpacity, Boolean fitToSize, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		// If we're not using a fileloc from the server
		if( useFileLocFromServer == false )
		{
			if(fileLocation != null && fileLocation.length() > 0){
				String fileLoc = "/temp" + getFileExtension(fileLocation);
				this.setFileloc( fileLoc );
				
				// Set the attribute to initiate the upload of the audio asset				
				setAttributesForUpload( request, this.getAssetId().toString(), assetName, fileLocation );
			}
		}
		
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
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			this.setOriginalFilename(originalFilename);
		}else if(formFile != null){
			this.setOriginalFilename(formFile.getFileName());
		}else if(fileLocation != null && fileLocation.length() > 0){
			fileLocation = fileLocation.replaceAll("\\\\", "/");
			this.setOriginalFilename(fileLocation.substring(fileLocation.lastIndexOf("/") + 1));
		}
		
		this.setAssetName( assetName );
		this.setWidth( width );
		this.setHeight( height );
		this.setFitToSize( fitToSize );
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
		return this;
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
		Flash newAsset = new Flash();		
		newAsset.setAssetName( newAssetName );
		newAsset.setWidth( this.getWidth() );
		newAsset.setHeight( this.getHeight() );
		newAsset.setFitToSize( this.getFitToSize() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setOverlayOpacity( this.getOverlayOpacity() );
		newAsset.setSetTransparentBg( this.getSetTransparentBg() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		newAsset.copyPermissionEntries( this );
		
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
	 * @return Returns the height.
	 */
	public Integer getHeight() {
		return height;
	}
	/**
	 * @param height The height to set.
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}
	/**
	 * @return Returns the width.
	 */
	public Integer getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(Integer width) {
		this.width = width;
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
	 * @return Returns the fitToSize.
	 */
	public Boolean getFitToSize() {
		return fitToSize;
	}
	

	/**
	 * @param fitToSize The fitToSize to set.
	 */
	public void setFitToSize(Boolean fitToSize) {
		this.fitToSize = fitToSize;
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