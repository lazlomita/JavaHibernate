package com.kuvata.kmf.asset;

import parkmedia.usertype.LiveVideoChannelType;
import parkmedia.usertype.LiveVideoInputType;
import parkmedia.usertype.LiveVideoType;

import com.kuvata.kmf.IAsset;

public interface IStreamingClient extends IAsset
{
	public String getStreamingUrl();
	public LiveVideoType getLiveVideoType();
	public LiveVideoInputType getLiveVideoInput();
	public LiveVideoChannelType getLiveVideoChannelType();
	public String getLiveVideoChannel();
	public Boolean getSuppressAudio();
}
