package com.kuvata.kmf.asset;

import com.kuvata.kmf.IAsset;

public interface IImage extends IAsset
{
	public Integer getWidth();
	public Integer getHeight();
	public String getFileloc();
	public Long getAdler32();	
	public Long getFilesize();	
	public Boolean getFitToSize();
	public String getOriginalFilename();
}
