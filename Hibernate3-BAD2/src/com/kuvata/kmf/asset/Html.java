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

/**
 * @author jrandesi
 */
public class Html extends Asset implements IHtml
{
	/**
	 * 
	 */
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.HtmlPresentation";

	private String fileloc;
	private Long adler32;
	private Long filesize;
	private String startPage;
	private Integer overlayOpacity;
	private String originalFilename;
	private Boolean setTransparentBg;
	private Boolean html5Hwaccel;
	private static String createAssetPage = "createAssetHtml";
	private static String assetPropertiesPage = "assetPropertiesHtml";
	
	/**
	 * 
	 *
	 */
	public Html()
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
		return Html.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Html.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.HTML;
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
	public String getPreviewPath() throws HibernateException
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
								"/"+ Constants.ASSETS_DIR +
								"/"+ AssetType.HTML.getPartialPersistentValue() +
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
	public String getThumbnailPath() throws HibernateException
	{
		if( thumbnailPath == null )
		{
			thumbnailPath = "/"+ Constants.APP_NAME +"/images/icons/html.gif";
		}
		return thumbnailPath;		 		
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{
		String result = "";
		try {
			result = "<img src=\""+ getThumbnailPath() +"\">";
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return result;		
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
	
	public static Html create(String assetName, Float length, 
			String startPage, Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, Integer overlayOpacity,
			Boolean setTransparentBg, Boolean html5Hwaccel, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Html a = new Html();			
		a.setAssetName( assetName );
		a.setStartPage( startPage );
		a.setAssetPresentation( ap );
		a.setFileloc( fileLocation );
		a.setOverlayOpacity(overlayOpacity);
		a.setSetTransparentBg(setTransparentBg);
		a.setHtml5Hwaccel(html5Hwaccel);
		
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
	
	public Html update(String assetName, Float length, String startPage, 
			Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, Integer overlayOpacity,
			Boolean setTransparentBg, Boolean html5Hwaccel, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		// If we're not using a fileloc from the server
		if( useFileLocFromServer == false )
		{
			if(fileLocation != null && fileLocation.length() > 0){
				String fileLoc = "/temp" + getFileExtension(fileLocation);
				this.setFileloc( fileLoc );				
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
		}else if(fileLocation != null && fileLocation.length() > 0){
			fileLocation = fileLocation.replaceAll("\\\\", "/");
			this.setOriginalFilename(fileLocation.substring(fileLocation.lastIndexOf("/") + 1));
		}
		
		this.setAssetName( assetName );		
		this.setStartPage( startPage );
		this.setAssetPresentation( ap );
		this.setOverlayOpacity(overlayOpacity);
		this.setSetTransparentBg(setTransparentBg);
		this.setHtml5Hwaccel(html5Hwaccel);
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
		Html newAsset = new Html();		
		newAsset.setAssetName( newAssetName );		
		newAsset.setStartPage( this.getStartPage() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setOverlayOpacity( this.getOverlayOpacity() );
		newAsset.setSetTransparentBg( this.getSetTransparentBg() );
		newAsset.setHtml5Hwaccel( this.getHtml5Hwaccel() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		
		// Copy the referenced file and set the associated properties
		newAsset.uploadLargeFile( this.getFileloc(), true );			
		
		// Copy any metadata associated with this asset
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
	 * @return Returns the htmlpageId.
	 */
	public Long getHtmlpageId() {
		return this.getAssetId();
	}
	/**
	 * @param htmlpageId The htmlpageId to set.
	 */
	public void setHtmlpageId(Long htmlpageId) {
		this.setAssetId(htmlpageId);
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

	public Boolean getHtml5Hwaccel() {
		return html5Hwaccel;
	}

	public void setHtml5Hwaccel(Boolean html5Hwaccel) {
		this.html5Hwaccel = html5Hwaccel;
	}
}