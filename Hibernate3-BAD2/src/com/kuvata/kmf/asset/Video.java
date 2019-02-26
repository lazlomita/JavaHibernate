/*
 * Created on Oct 29, 2004
 */
package com.kuvata.kmf.asset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.apache.log4j.Logger;
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
public class Video extends Asset implements IVideo
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.VideoPresentation";
	private static Logger logger = Logger.getLogger(Video.class);
	private Integer width;
	private Integer height;
	private Float length;
	private String fileloc;
	private Long adler32;
	private Long filesize;
	private Boolean displayEmbeddedSubtitles;
	private Boolean suppressAudio;
	private Boolean anamorphicWidescreen;
	private String originalFilename;
	private Integer overlayOpacity;
	private String videoCodec;
	private Integer normalizationBeginAvg;
	private Boolean framesync;
	private static String createAssetPage = "createAssetVideo";
	private static String assetPropertiesPage = "assetPropertiesVideo";
	
	public Video()
	{}
	
	public String getPresentationType()
	{
	    return PRESENTATION_TYPE;
	}

	/**
	 * Implements the parent's abstract method. Used to determine which page to
	 * display in the create asset wizard for this asset type.
	 */
	public String getCreateAssetPage()
	{
		return Video.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine which page to
	 * display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Video.assetPropertiesPage; 
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.VIDEO;
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension) throws FileNotFoundException, IOException
	{		
/*		String videoFilePath = this.getFileloc();
		if(videoFilePath.indexOf("file://") < 0)
		{
			videoFilePath = "file://"+ videoFilePath;
		}
		long frameToCapture = Constants.FRAME_TO_CAPTURE;
		String imageFilePath = this.getRelativePath( true );
		FrameAccess.captureImage(videoFilePath, frameToCapture, imageFilePath);*/
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getPreviewPath() throws HibernateException
	{
		if( previewPath == null ){
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
								"/"+ AssetType.VIDEO.getPartialPersistentValue() +
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
			thumbnailPath =  "/"+ Constants.APP_NAME +"/images/icons/video.gif";	
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
	 * Implements the parent's abstract method Determines if this asset's
	 * referenced file exists on disk
	 */
	public boolean getReferencedFileExists()
	{
		return Asset.referencedFileExists( this.getFileloc() );
	}	
	
	public static Video create(String assetName, Float defaultLength,  Displayarea da, Layout l, boolean useFileLocFromServer, 
			String fileLocation, Boolean displayEmbeddedSubtitles, Boolean suppressAudio, Boolean anamorphicWidescreen, Boolean framesync, boolean waitForFileUpload, 
			Integer overlayOpacity, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( defaultLength );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type
		Video a = new Video();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setWidth( new Integer( Constants.INTRINSIC_LENGTH_PLACEHOLDER ) );
		a.setHeight( new Integer( Constants.INTRINSIC_LENGTH_PLACEHOLDER ) );
		a.setLength( new Float( Constants.INTRINSIC_LENGTH_PLACEHOLDER ) );	
		a.setFileloc( fileLocation );
		a.setDisplayEmbeddedSubtitles( displayEmbeddedSubtitles );
		a.setAnamorphicWidescreen(anamorphicWidescreen);
		a.setSuppressAudio( suppressAudio );
		a.setOverlayOpacity(overlayOpacity);
		a.setFramesync(framesync);
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			a.setOriginalFilename(originalFilename);
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
		
		return a;
	}
	
	public void update(String assetName, Float defaultLength, Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, 
			Boolean displayEmbeddedSubtitles, Boolean suppressAudio, Boolean anamorphicWidescreen, Boolean framesync, boolean waitForFileUpload, Integer overlayOpacity, 
			String originalFilename, boolean evaluateDirtyEntity) throws IOException, InterruptedException
	{
		AssetPresentation ap = this.getAssetPresentation();
		
		// If the default length has changed
		boolean lengthChanged = false;
		if( defaultLength != null && ap.getLength().equals( defaultLength ) == false ) {
			ap.setLength( defaultLength );
			lengthChanged = true;
		}			
		
		if (evaluateDirtyEntity)
			this.makeDirty( lengthChanged, false );	
		
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.update();
		
		// If we're not using a fileloc from the server
		if( useFileLocFromServer == false )
		{
			// If the fileloc property has changed (it was not left empty)
			if( (fileLocation.length() > 0 && (this.getFileloc() != null && this.getFileloc().equals( fileLocation ) == false)) || this.getFileloc() == null)
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
		this.setDisplayEmbeddedSubtitles( displayEmbeddedSubtitles );
		this.setSuppressAudio( suppressAudio );
		this.setAnamorphicWidescreen(anamorphicWidescreen);
		this.setOverlayOpacity(overlayOpacity);
		this.setFramesync(framesync);
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
		Video newAsset = new Video();		
		newAsset.setAssetName( newAssetName );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setWidth( this.getWidth() );
		newAsset.setHeight( this.getHeight() );
		newAsset.setLength( this.getLength() );
		newAsset.setDisplayEmbeddedSubtitles( this.getDisplayEmbeddedSubtitles() );
		newAsset.setSuppressAudio( this.getSuppressAudio() );
		newAsset.setFramesync( this.getFramesync() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are
		// going to copy them
		newAsset.save( false );
		
		// Copy the referenced file and set the associated properties
		newAsset.uploadLargeFile( this.getFileloc(), true );			
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}		
	
	/**
	 * Attempts to retrieve the intrinsic properties (height/width/length) of
	 * this video file and sets the corresponding properties of this object.
	 * 
	 */
	public void setIntrinsicProperties()
	{
		MediaInfo mediaInfo = MediaInfo.getMediaInfo( this );
		if(mediaInfo != null){
			
			// Update all presentations if needed
			updatePresentations(mediaInfo.getLength(), this.getLength());
			
			this.setLength( mediaInfo.getLength() );	
			this.setHeight( mediaInfo.getHeight() );
			this.setWidth( mediaInfo.getWidth() );
			this.setVideoCodec( mediaInfo.getVideoCodec() );
			this.setNormalizationBeginAvg(mediaInfo.getNormalizationBeginAvg());
			
			// If we failed to calculate the length
			if(mediaInfo.getLength().floatValue() == -1){
				this.setFileloc(null);
			}
	
			// If the length property of this asset's assetPresentation is "15.001", re-set it
			if( this.getAssetPresentation().getLength().equals( new Float(Constants.PLAY_LENGTH_PLACEHOLDER) ) && mediaInfo.getLength().equals( new Float(Constants.INTRINSIC_LENGTH_PLACEHOLDER) ) == false ){			
				this.getAssetPresentation().setLength( mediaInfo.getLength() );
				this.getAssetPresentation().update();
			}
		}else{
			this.setLength(0f);
			this.setHeight(0);
			this.setWidth(0);
			this.setVideoCodec(null);
			this.setNormalizationBeginAvg(null);
		}
		this.update();
	}
	
	/**
	 * @return Returns the length.
	 */
	public Float getLength() {
		return length;
	}
	/**
	 * @param length
	 *            The length to set.
	 */
	public void setLength(Float length) {
		this.length = length;
	}
	/**
	 * @return Returns the videoId.
	 */
	public Long getVideoId() {
		return this.getAssetId();
	}
	/**
	 * @param videoId
	 *            The videoId to set.
	 */
	public void setVideoId(Long videoId) {
		this.setAssetId( videoId );
	}
	/**
	 * @return Returns the fileloc.
	 */
	public String getFileloc() {
		return fileloc;
	}
	/**
	 * @param fileloc
	 *            The fileloc to set.
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
	 * @param height
	 *            The height to set.
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
	 * @param width
	 *            The width to set.
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
	 * @param adler32
	 *            The adler32 to set.
	 */
	public void setAdler32(Long adler32) {
		this.adler32 = adler32;
	}
	/**
	 * @return Returns the filesize.
	 */
	public Long getFilesize() {
		return filesize;
	}
	/**
	 * @param filesize
	 *            The filesize to set.
	 */
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	/**
	 * @return Returns the displayEmbeddedSubtitles.
	 */
	public synchronized Boolean getDisplayEmbeddedSubtitles() {
		return displayEmbeddedSubtitles;
	}
	

	/**
	 * @param displayEmbeddedSubtitles
	 *            The displayEmbeddedSubtitles to set.
	 */
	public synchronized void setDisplayEmbeddedSubtitles(
			Boolean displayEmbeddedSubtitles) {
		this.displayEmbeddedSubtitles = displayEmbeddedSubtitles;
	}
	/**
	 * @return the suppressAudio
	 */
	public Boolean getSuppressAudio() {
		return suppressAudio;
	}

	/**
	 * @param suppressAudio
	 *            the suppressAudio to set
	 */
	public void setSuppressAudio(Boolean suppressAudio) {
		this.suppressAudio = suppressAudio;
	}

	public Boolean getAnamorphicWidescreen() {
		return anamorphicWidescreen;
	}

	public void setAnamorphicWidescreen(Boolean anamorphicWidescreen) {
		this.anamorphicWidescreen = anamorphicWidescreen;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public Integer getOverlayOpacity() {
		return overlayOpacity;
	}

	public void setOverlayOpacity(Integer overlayOpacity) {
		this.overlayOpacity = overlayOpacity;
	}

	public String getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public Integer getNormalizationBeginAvg() {
		return normalizationBeginAvg;
	}

	public void setNormalizationBeginAvg(Integer normalizationBeginAvg) {
		this.normalizationBeginAvg = normalizationBeginAvg;
	}

	public Boolean getFramesync() {
		return framesync;
	}

	public void setFramesync(Boolean framesync) {
		this.framesync = framesync;
	}
}
