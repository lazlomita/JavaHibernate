package com.kuvata.kmf.asset;

import com.kuvata.kmf.usertype.LiveVideoChannelType;
import com.kuvata.kmf.usertype.LiveVideoInputType;
import com.kuvata.kmf.usertype.LiveVideoType;

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
