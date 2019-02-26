package com.kuvata.kmf.asset;

import com.kuvata.kmf.IAsset;

public interface IWebapp extends IAsset
{
	public String getFileloc();
	public Long getAdler32();	
	public Long getFilesize();
	public String getStartPage();
	public String getOriginalFilename();
	public Boolean getSetTransparentBg();
}
