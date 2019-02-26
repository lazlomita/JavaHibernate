/*
 * Created on Jan 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.asset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.PlaylistOrderType;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.ContentRotation;
import com.kuvata.kmf.ContentRotationAsset;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;

/**
 * @author jrandesi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AdServer extends Asset implements IAdServer
{
	private String url;
	private ContentRotation defaultContentRotation;
	private static String createAssetPage = "createAssetAdServer";
	private static String assetPropertiesPage = "assetPropertiesAdServer";
    public static final int AD_SERVER_MAX_TRIES = 1;
    public static final int AD_SERVER_SLEEP_INTERVAL_DEVICE = 30000;
    public static final int AD_SERVER_SLEEP_INTERVAL_SERVER = 5000;
	
    // Default content
    private List<ContentRotationAsset> contentRotationAssets;
    private ListIterator<ContentRotationAsset> contentRotationAssetsIterator;
    
	public AdServer(){}
	
	public String getPresentationType()
	{
	    return "";
	}

	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getCreateAssetPage()
	{
		return AdServer.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return AdServer.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.AD_SERVER;
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
			// Return the url
			previewPath = this.getUrl();
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
	 * Implements the parent's abstract method
	 * Since this asset type does not have a referenced file, return true
	 */
	public boolean getReferencedFileExists()
	{
		return true;
	}
	
	/**
	 * Implements the parent's abstract method. 
	 */
	public void delete() throws HibernateException
	{
		super.delete();
	}	
	
	public static AdServer create(String assetName, Float length, String url, Boolean variableLength, Displayarea da, Layout l, ContentRotation defaultContentRotation) throws IOException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.setVariableLength( variableLength );
		ap.save();
		
		// Create a new object of the given type			
		AdServer a = new AdServer();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setUrl( url );
		a.setDefaultContentRotation(defaultContentRotation);
		a.save();
		
		return a;
	}
	
	public void update(String assetName, Float length, String url, Boolean variableLength, Displayarea da, Layout l, ContentRotation defaultContentRotation) throws IOException
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
		ap.setVariableLength( variableLength );
		ap.update();
			
		this.setAssetName( assetName );		
		this.setAssetPresentation( ap );
		this.setUrl( url );
		this.setDefaultContentRotation(defaultContentRotation);
		this.update();
	}		
	
	/**
	 * Copies this asset and assigns the given new asset name.
	 * 
	 * @param newAssetName
	 * @return
	 */
	public Long copy(String newAssetName) throws ClassNotFoundException
	{				
		// Create a new asset object
		AdServer newAsset = new AdServer();		
		newAsset.setAssetName( newAssetName );
		newAsset.setUrl( this.getUrl() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		
		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );		
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}
	
	public ContentRotationAsset getNextDefaultContentAsset(){
		ContentRotationAsset asset = null;
		if(defaultContentRotation != null){
			// If its the first request for a default asset
			if(contentRotationAssets == null && defaultContentRotation.getContentRotationAssets().size() > 0){
				contentRotationAssets = new LinkedList(defaultContentRotation.getContentRotationAssets());
				
				// Randomize if necessary
				if(defaultContentRotation.getContentRotationOrder() != null && defaultContentRotation.getContentRotationOrder().equals(PlaylistOrderType.RANDOM)){
					Collections.shuffle(contentRotationAssets);
				}
				
				// Set the iterator
				contentRotationAssetsIterator = contentRotationAssets.listIterator();
			}
			
			// Server the default asset
			if(contentRotationAssetsIterator != null){
				if(contentRotationAssetsIterator.hasNext() == false){
					contentRotationAssetsIterator = contentRotationAssets.listIterator();
				}
				
				asset = contentRotationAssetsIterator.next();
			}
		}
		return asset;
	}
		
	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	public ContentRotation getDefaultContentRotation() {
		return defaultContentRotation;
	}

	public void setDefaultContentRotation(ContentRotation defaultContentRotation) {
		this.defaultContentRotation = defaultContentRotation;
	}
}
