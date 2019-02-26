/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.asset.StreamingServer;
import com.kuvata.kmf.util.Reformat;
/**
 * 
 * @author anaber
 *
 */
public class StreamingServerPresentation extends Presentation {
	
	public String streamingUrl;
	
	public StreamingServerPresentation()
	{
	    SUBDIRECTORY = "streamingserver";	    
	}
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public StreamingServerPresentation(StreamingServer streaming, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(streaming);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public StreamingServerPresentation(StreamingServer streaming) throws HibernateException, IOException
	{
	    this();	    
	    this.setPresenterType(PresenterType.STREAMING_SERVER);
	    this.setAssetId( streaming.getAssetId() );
		this.setStreamingUrl( streaming.getStreamingUrl() );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "StreamingServerPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getStreamingUrl().hashCode(),result,5);
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
}
