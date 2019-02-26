/*
 * Created on Jan 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.asset;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.AssetType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;

/**
 * @author anaber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Url extends Asset implements IUrl 
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.UrlPresentation";

	private String url;
	private Integer overlayOpacity;
	private Boolean setTransparentBg;
	private Boolean html5Hwaccel;
	private static String createAssetPage = "createAssetUrl";
	private static String assetPropertiesPage = "assetPropertiesUrl";
	
	public Url()
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
		return Url.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Url.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.URL;
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
	
	public static Url create(String assetName, Float length, String url, Displayarea da, Layout l, Integer overlayOpacity, Boolean setTransparentBg, Boolean html5Hwaccel) throws IOException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Url a = new Url();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setUrl( url );
		a.setOverlayOpacity(overlayOpacity);
		a.setSetTransparentBg(setTransparentBg);
		a.setHtml5Hwaccel(html5Hwaccel);
		a.save();
		
		return a;
	}
	
	public void update(String assetName, Float length, String url, Displayarea da, Layout l, Integer overlayOpacity, Boolean setTransparentBg, Boolean html5Hwaccel) throws IOException
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
			
		this.setAssetName( assetName );		
		this.setAssetPresentation( ap );
		this.setUrl( url );
		this.setOverlayOpacity(overlayOpacity);
		this.setSetTransparentBg(setTransparentBg);
		this.setHtml5Hwaccel(html5Hwaccel);
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
		Url newAsset = new Url();		
		newAsset.setAssetName( newAssetName );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setUrl( this.getUrl() );
		newAsset.setOverlayOpacity( this.getOverlayOpacity() );
		newAsset.setSetTransparentBg( this.getSetTransparentBg() );
		newAsset.setHtml5Hwaccel( this.getHtml5Hwaccel() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
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

	public Integer getOverlayOpacity() {
		return overlayOpacity;
	}

	public void setOverlayOpacity(Integer overlayOpacity) {
		this.overlayOpacity = overlayOpacity;
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