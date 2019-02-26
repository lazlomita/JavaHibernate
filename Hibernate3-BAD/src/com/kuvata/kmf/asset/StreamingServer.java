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
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;

/**
 * @author anaber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StreamingServer extends Asset implements IStreamingServer
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.StreamingServerPresentation";

	private static String createAssetPage = "createAssetStreamingServer";
	private static String assetPropertiesPage = "assetPropertiesStreamingServer";
	private String streamingUrl;
	
	public StreamingServer()
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
		return StreamingServer.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return StreamingServer.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		//return AssetType.STREAMING_SERVER;
		return null;
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
		// No preview for power off asset type
		return "";
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getThumbnailPath()
	{
		// Do not show preview icon
		return "";			
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{		
		return "<img src=\""+ getThumbnailPath() +"\">";			
	}	
	
	/**
	 * Implements the parent's abstract method. 
	 */
	public void delete() throws HibernateException
	{
		super.delete();
	}	
	
	/**
	 * Implements the parent's abstract method
	 * Since this asset type does not have a referenced file, return true
	 */
	public boolean getReferencedFileExists()
	{
		return true;
	}
	
	public static StreamingServer create(String assetName, Float length, String streamingUrl, Displayarea da, Layout l)
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		StreamingServer a = new StreamingServer();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );			
		a.setStreamingUrl( streamingUrl );
		a.save();
		
		return a;
	}
	
	public void update(String assetName, Float length, String streamingUrl, Displayarea da, Layout l) 
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
		this.setStreamingUrl( streamingUrl );
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
		StreamingServer newAsset = new StreamingServer();		
		newAsset.setAssetName( newAssetName );
		newAsset.setStreamingUrl( this.getStreamingUrl() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		newAsset.copyPermissionEntries( this );
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}
	
	/**
	 * @return Returns the streamingUrl.
	 */
	public String getStreamingUrl() {
		return streamingUrl;
	}
	

	/**
	 * @param streamingUrl The streamingUrl to set.
	 */
	public void setStreamingUrl(String streamingUrl) {
		this.streamingUrl = streamingUrl;
	}
		
	
}
