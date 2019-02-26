package com.kuvata.kmf.asset;

import com.kuvata.kmf.IAsset;

public interface IVideo extends IAsset
{
	public Integer getWidth();
	public Integer getHeight();
	public Float getLength();
	public String getFileloc();
	public Long getAdler32();	
	public Long getFilesize();
	public Boolean getDisplayEmbeddedSubtitles();
	public Boolean getSuppressAudio();
	public String getOriginalFilename();
}
