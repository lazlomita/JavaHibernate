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
public class Audio extends Asset implements IAudio
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.AudioPresentation";

	private Float length;
	private String fileloc;
	private Long adler32;
	private Long filesize;
	private String originalFilename;
	private Integer normalizationBeginAvg;
	private static String createAssetPage = "createAssetAudio";
	private static String assetPropertiesPage = "assetPropertiesAudio";
	
	/**
	 * 
	 *
	 */
	public Audio()
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
		return Audio.createAssetPage; 
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Audio.assetPropertiesPage; 
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.AUDIO;
	}
	
	public static Audio create(String assetName, Float defaultLength, Displayarea da, Layout l, boolean useFileLocFromServer, 
			String fileLocation, boolean waitForFileUpload, String originalFilename) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( defaultLength );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Audio a = new Audio();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setFileloc( "" );			
		a.setLength( new Float( Constants.INTRINSIC_LENGTH_PLACEHOLDER ));	
		a.setFileloc( fileLocation );
		
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
			boolean waitForFileUpload, String originalFilename, boolean evaluateDirtyEntity) throws IOException, InterruptedException
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
			if( (this.getFileloc() == null) || (fileLocation.length() > 0 && this.getFileloc().equals( fileLocation ) == false))
			{
				String fileLoc = "/temp" + getFileExtension(fileLocation);
				this.setFileloc( fileLoc );
				this.createThumbnail( Asset.MAX_DIMENSION );
							
			}	
		}		
		this.setAssetName( assetName );			
		this.setAssetPresentation( ap );
		
		// Set original filename
		if(originalFilename != null && originalFilename.length() > 0){
			this.setOriginalFilename(originalFilename);
		}else if(fileLocation != null && fileLocation.length() > 0){
			fileLocation = fileLocation.replaceAll("\\\\", "/");
			this.setOriginalFilename(fileLocation.substring(fileLocation.lastIndexOf("/") + 1));
		}
		
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
		Audio newAsset = new Audio();		
		newAsset.setAssetName( newAssetName );
		newAsset.setLength( this.getLength() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
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
	 * Attempts to retrieve the intrinsic properties (length)
	 * of this audio file and sets the corresponding properties of this object.
	 *
	 */
	public void setIntrinsicProperties()
	{
		MediaInfo mediaInfo = MediaInfo.getMediaInfo( this );
		if(mediaInfo != null){
			// Update all presentations if needed
			updatePresentations(mediaInfo.getLength(), this.getLength());
			
			this.setLength( mediaInfo.getLength() );
			this.setNormalizationBeginAvg(mediaInfo.getNormalizationBeginAvg());
			
			// If we failed to calculate the length
			if(mediaInfo.getLength().floatValue() == -1){
				this.setFileloc(null);
			}
		}else{
			this.setLength(0f);
			this.setNormalizationBeginAvg(null);
		}
		this.update();
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension) throws FileNotFoundException, IOException
	{				
	}
	
	/*
	 * Implements the parent's abstract method
	 * Determines if this asset's referenced file exists on disk
	 */
	public boolean getReferencedFileExists()
	{
		return Asset.referencedFileExists( this.getFileloc() );
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
								"/"+ SchemaDirectory.getSchema().getSchemaName() +		
								"/"+ Constants.ASSETS_DIR +
								"/"+ AssetType.AUDIO.getPartialPersistentValue() +
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
			thumbnailPath =  "/"+ Constants.APP_NAME +"/images/icons/audio.gif";	
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
	 * @return Returns the length.
	 */
	public Float getLength() {
		return length;
	}
	/**
	 * @param length The length to set.
	 */
	public void setLength(Float length) {
		this.length = length;
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
	public Long getAudioId(){
		return this.getAssetId();
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

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public Integer getNormalizationBeginAvg() {
		return normalizationBeginAvg;
	}

	public void setNormalizationBeginAvg(Integer normalizationBeginAvg) {
		this.normalizationBeginAvg = normalizationBeginAvg;
	}
}
