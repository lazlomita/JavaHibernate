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
import com.kuvata.kmf.usertype.LiveVideoChannelType;
import com.kuvata.kmf.usertype.LiveVideoInputType;
import com.kuvata.kmf.usertype.LiveVideoType;

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
public class StreamingClient extends Asset implements IStreamingClient
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.StreamingClientPresentation";

	private static String createAssetPage = "createAssetStreamingClient";
	private static String assetPropertiesPage = "assetPropertiesStreamingClient";
	private String streamingUrl;
	private LiveVideoType liveVideoType;
	private LiveVideoInputType liveVideoInput;
	private LiveVideoChannelType liveVideoChannelType;
	private String liveVideoChannel;
	private Boolean suppressAudio;
	
	public StreamingClient()
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
		return StreamingClient.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return StreamingClient.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.STREAMING_CLIENT;
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
	public static StreamingClient create(String assetName, Float length, LiveVideoType liveVideoType, String streamingUrl, LiveVideoInputType liveVideoInput,
			LiveVideoChannelType liveVideoChannelType, String liveVideoChannel, Boolean suppressAudio, Displayarea da, Layout l)
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		StreamingClient a = new StreamingClient();		
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setLiveVideoType( liveVideoType );
		a.setStreamingUrl( streamingUrl );
		a.setLiveVideoInput( liveVideoInput );
		a.setLiveVideoChannelType( liveVideoChannelType );
		a.setLiveVideoChannel( liveVideoChannel );
		a.setSuppressAudio( suppressAudio );
		a.save();		
		return a;
	}
	
	public void update(String assetName, Float length, LiveVideoType liveVideoType, String streamingUrl, LiveVideoInputType liveVideoInput,
			LiveVideoChannelType liveVideoChannelType, String liveVideoChannel, Boolean suppressAudio, Displayarea da, Layout l) 
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
		this.setLiveVideoType( liveVideoType );
		this.setStreamingUrl( streamingUrl );		
		this.setLiveVideoInput( liveVideoInput );
		this.setLiveVideoChannelType( liveVideoChannelType );
		this.setLiveVideoChannel( liveVideoChannel );
		this.setSuppressAudio( suppressAudio );
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
		StreamingClient newAsset = new StreamingClient();		
		newAsset.setAssetName( newAssetName );
		newAsset.setStreamingUrl( this.getStreamingUrl() );
		newAsset.setLiveVideoType( this.getLiveVideoType() );
		newAsset.setLiveVideoInput( this.getLiveVideoInput() );
		newAsset.setLiveVideoChannelType( this.getLiveVideoChannelType() );
		newAsset.setLiveVideoChannel( this.getLiveVideoChannel() ); 
		newAsset.setSuppressAudio( this.getSuppressAudio() );
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

	/**
	 * @return Returns the liveVideoType.
	 */
	public LiveVideoType getLiveVideoType() {
		return liveVideoType;
	}
	

	/**
	 * @param liveVideoType The liveVideoType to set.
	 */
	public void setLiveVideoType(LiveVideoType liveVideoType) {
		this.liveVideoType = liveVideoType;
	}

	/**
	 * @return Returns the liveVideoBroadcastType.
	 */
	public LiveVideoChannelType getLiveVideoChannelType() {
		return liveVideoChannelType;
	}
	

	/**
	 * @param liveVideoBroadcastType The liveVideoBroadcastType to set.
	 */
	public void setLiveVideoChannelType(
			LiveVideoChannelType liveVideoBroadcastType) {
		this.liveVideoChannelType = liveVideoBroadcastType;
	}
	

	/**
	 * @return Returns the liveVideoChannel.
	 */
	public String getLiveVideoChannel() {
		return liveVideoChannel;
	}
	

	/**
	 * @param liveVideoChannel The liveVideoChannel to set.
	 */
	public void setLiveVideoChannel(String liveVideoChannel) {
		this.liveVideoChannel = liveVideoChannel;
	}
	

	/**
	 * @return Returns the liveVideoInput.
	 */
	public LiveVideoInputType getLiveVideoInput() {
		return liveVideoInput;
	}
	

	/**
	 * @param liveVideoInput The liveVideoInput to set.
	 */
	public void setLiveVideoInput(LiveVideoInputType liveVideoInput) {
		this.liveVideoInput = liveVideoInput;
	}

	/**
	 * @return the suppressAudio
	 */
	public Boolean getSuppressAudio() {
		return suppressAudio;
	}

	/**
	 * @param suppressAudio the suppressAudio to set
	 */
	public void setSuppressAudio(Boolean suppressAudio) {
		this.suppressAudio = suppressAudio;
	}
	
	
		
	
}
