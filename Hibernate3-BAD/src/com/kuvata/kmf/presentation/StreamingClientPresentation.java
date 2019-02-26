/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.LiveVideoChannelType;
import com.kuvata.kmf.usertype.LiveVideoInputType;
import com.kuvata.kmf.usertype.LiveVideoType;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.asset.StreamingClient;
import com.kuvata.kmf.util.Reformat;
/**
 * 
 * @author jrandesi
 *
 */
public class StreamingClientPresentation extends Presentation {
	
	public String streamingUrl;
	public String liveVideoType;
	public String liveVideoInput;
	public String liveVideoChannelType;
	public String liveVideoChannel;
	public Boolean suppressAudio = Boolean.FALSE;
	
	public StreamingClientPresentation()
	{
	    SUBDIRECTORY = "streamingclient";	    
	}
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public StreamingClientPresentation(StreamingClient streaming, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(streaming);
	    this.setPresenterType(presenterType);
	    this.setSuppressAudio( streaming.getSuppressAudio() != null ? streaming.getSuppressAudio() : Boolean.FALSE );
	}
	/**
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public StreamingClientPresentation(StreamingClient streamingClient) throws HibernateException, IOException
	{
	    this();	    
	    this.setPresenterType(PresenterType.STREAMING_CLIENT);
	    this.setAssetId( streamingClient.getAssetId() );
		this.setStreamingUrl( streamingClient.getStreamingUrl() );
		if( streamingClient.getLiveVideoType() != null ){
			this.setLiveVideoType( streamingClient.getLiveVideoType().getPersistentValue() );
		}else{
			this.setLiveVideoType( LiveVideoType.STREAMING_URL.getPersistentValue() );
		}
		if( streamingClient.getLiveVideoChannelType() != null ){
			this.setLiveVideoChannelType( streamingClient.getLiveVideoChannelType().getPersistentValue() );
		}else{
			this.setLiveVideoChannelType( LiveVideoChannelType.BROADCAST.getPersistentValue() );
		}		
		if( streamingClient.getLiveVideoInput() != null ){
			this.setLiveVideoInput( streamingClient.getLiveVideoInput().getPersistentValue() );
		}else{
			this.setLiveVideoInput( LiveVideoInputType.TUNER.getPersistentValue() );
		}	
		this.setLiveVideoChannel( streamingClient.getLiveVideoChannel() );
		this.setSuppressAudio( streamingClient.getSuppressAudio() != null ? streamingClient.getSuppressAudio() : Boolean.FALSE );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "StreamingClientPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId(),result,3);
		result = Reformat.getSafeHash(this.getStreamingUrl(),result,5);
		result = Reformat.getSafeHash(this.getLiveVideoType(),result,7);
		result = Reformat.getSafeHash(this.getLiveVideoChannelType(),result,9);
		result = Reformat.getSafeHash(this.getLiveVideoInput(),result,13);
		result = Reformat.getSafeHash(this.getLiveVideoChannel(),result,17);
		result = Reformat.getSafeHash(this.getSuppressAudio(),result,19);
		if( result < 0 ) {
		    return -result;
		} else {
		    return result;
		}
	}
	
	/**
	 * Implements parent's abstract method
	 */
	public ReferencedFile getReferencedFile()
	{
		return null;
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
	 * @return Returns the liveVideoBroadcastType.
	 */
	public String getLiveVideoChannelType() {
		return liveVideoChannelType;
	}
	
	/**
	 * @param liveVideoBroadcastType The liveVideoBroadcastType to set.
	 */
	public void setLiveVideoChannelType(String liveVideoBroadcastType) {
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
	public String getLiveVideoInput() {
		return liveVideoInput;
	}
	
	/**
	 * @param liveVideoInput The liveVideoInput to set.
	 */
	public void setLiveVideoInput(String liveVideoInput) {
		this.liveVideoInput = liveVideoInput;
	}
	
	/**
	 * @return Returns the liveVideoType.
	 */
	public String getLiveVideoType() {
		return liveVideoType;
	}
	
	/**
	 * @param liveVideoType The liveVideoType to set.
	 */
	public void setLiveVideoType(String liveVideoType) {
		this.liveVideoType = liveVideoType;
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
