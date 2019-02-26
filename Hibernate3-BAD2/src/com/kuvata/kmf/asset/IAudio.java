package com.kuvata.kmf.asset;

import com.kuvata.kmf.IAsset;

public interface IAudio extends IAsset
{
	public Float getLength();	
	public String getFileloc();
	public Long getAdler32();	
	public Long getFilesize();
	public String getOriginalFilename();
}
