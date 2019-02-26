/*
 * Created on Oct 29, 2004
 */
package com.kuvata.kmf.asset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.hibernate.HibernateException;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.util.Files;
import com.kuvata.kmm.KMMServlet;


/**
 * @author jrandesi
 */
public class Image extends Asset implements IImage
{
	private static Logger logger = Logger.getLogger(Image.class);
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.ImagePresentation";

	private Integer width;
	private Integer height;
	private String fileloc;
	private Long adler32;
	private Long filesize;
	public Boolean fitToSize;
	private Integer overlayOpacity;
	private String originalFilename;
	private static String createAssetPage = "createAssetImage";
	private static String assetPropertiesPage = "assetPropertiesImage";

	public Image()
	{}
		
	/**
	 * @return Returns the length.
	 */
	public Float getLength() {
		return null;
	}
	
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
		return Image.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Image.assetPropertiesPage; 
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.IMAGE;
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension)
	{				
		// Create the name of the file to save
		if( this.getFileloc() != null && this.getFileloc().length() > 0 && this.getFileloc().indexOf(".") > 0 ){
			String fileExt = this.getFileloc().substring(this.getFileloc().lastIndexOf("."));		
			String thumbFileName = this.getFileloc().substring( this.getFileloc().lastIndexOf("/"), this.getFileloc().lastIndexOf(".") );		
			thumbFileName += Asset.THUMB_EXT + fileExt;
			String thumbFilePath = this.getFileloc().substring( 0, this.getFileloc().lastIndexOf("/") );
			thumbFilePath += "/"+ Constants.THUMBS_DIR + thumbFileName;
			KMMServlet.createThumbnail( maxDimension, this.getFileloc(), thumbFilePath );
		}
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{
		StringBuffer sb = new StringBuffer();
		try {	
			sb.append("<a href=\""+ this.getPreviewPath() +"\" target=\"blank\">");
			sb.append(this.getAssetName() +": ");
			sb.append("<img src=\""+ this.getThumbnailPath() +"\" alt=\""+ this.getAssetName() +"\" border=\"0\" height=\"17px\" align=\"absbottom\">");
			sb.append("</a>");						
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return sb.toString();		
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
	 * Returns the relative path to either the actual or thumbnail asset
	 */
	public String getPreviewPath() throws HibernateException
	{
		if( previewPath == null  )
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
								"/"+ AssetType.IMAGE.getPartialPersistentValue() +
								"/"+ fileName;	
					}
				}
			}
		} 
		return previewPath;
	}
	
	/**
	 * Returns the relative path to either the actual or thumbnail asset
	 */
	public String getThumbnailPath() throws HibernateException
	{
		// Attempt to locate the thumbnail file
		if( this.getFileloc() != null && this.getFileloc().indexOf(".") > 0 ) {
			String fileExt = this.getFileloc().substring(this.getFileloc().lastIndexOf("."));
			String fileName = this.getFileloc().substring( this.getFileloc().lastIndexOf("/") + 1, this.getFileloc().lastIndexOf(".") );		
			fileName += Asset.THUMB_EXT + fileExt;
			thumbnailPath = "/"+ Constants.SCHEMAS +					
					"/"+ SchemaDirectory.getSchema().getSchemaName() +
					"/"+ Constants.ASSETS_DIR + 
					"/"+ AssetType.IMAGE.getPartialPersistentValue() +
					"/"+ Constants.THUMBS_DIR + 
					"/"+ fileName;
			
			// If the thumbnail file does not exist
			String thumbnailFullPath = KuvataConfig.getKuvataHome() + thumbnailPath;
			File thumbnailFile = new File( thumbnailFullPath );
			if( thumbnailFile.exists() == false )
			{
				// Use the full preview path -- we'll shrink it in the html
				thumbnailPath = getPreviewPath();
			}			
		} else {
			thumbnailPath = "";
		}		
		return thumbnailPath;
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
	
	public static Image create(HttpServletRequest request, HttpServletResponse response, String assetName, Float length, Displayarea da, Layout l, boolean useFileLocFromServer, 
			String fileLocation, FormFile formFile, Integer overlayOpacity, Boolean fitToSize, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Image a = new Image();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setFileloc( "" );				
		a.setFitToSize( fitToSize );
		a.setOverlayOpacity(overlayOpacity);
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			a.setOriginalFilename(originalFilename);
		}else if(formFile != null){
			a.setOriginalFilename(formFile.getFileName());
		}else if(fileLocation != null && fileLocation.length() > 0){
			fileLocation = fileLocation.replaceAll("\\\\", "/");
			a.setOriginalFilename(fileLocation.substring(fileLocation.lastIndexOf("/") + 1));
		}
		
		a.save();
		
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
	
	public Image update(HttpServletRequest request, HttpServletResponse response, String assetName, Float length,
			Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, FormFile formFile, Integer overlayOpacity,
			Boolean fitToSize, boolean waitForFileUpload, String originalFilename, boolean evaluateDirtyEntity) throws IOException, InterruptedException
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
		
		// markDirtyEntity must be false for the first update (after create an asset)
		if (evaluateDirtyEntity)
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
		this.setFitToSize( fitToSize );
		this.setIntrinsicProperties();
		this.setOverlayOpacity(overlayOpacity);
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
		Image newAsset = new Image();		
		newAsset.setAssetName( newAssetName );		
		newAsset.setWidth( this.getWidth() );
		newAsset.setHeight( this.getHeight() );
		newAsset.setFitToSize( this.getFitToSize() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setOverlayOpacity( this.getOverlayOpacity() );
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
	 * Attempts to retrieve the intrinsic properties (height/width)
	 * of this image file and sets the corresponding properties of this object.
	 *
	 */
	public void setIntrinsicProperties()
	{
		try {
			if(this.getFileloc() != null){
				File f = new File( this.getFileloc() );
				if( f.exists() ){
					BufferedImage image = ImageIO.read( f );
					if( image != null )
					{
						this.setWidth( image.getWidth() );
						this.setHeight( image.getHeight() );
						this.update();
					}
				}
			}
		} catch (Exception e) {
			logger.error( e );
		}
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
	 * @return Returns the imageId.
	 */
	public Long getImageId() {
		return this.getAssetId();
	}
	/**
	 * @param imageId The imageId to set.
	 */
	public void setImageId(Long imageId) {
		this.setAssetId(imageId);
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
	
}