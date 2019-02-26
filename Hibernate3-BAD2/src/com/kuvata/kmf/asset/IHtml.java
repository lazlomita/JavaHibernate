package com.kuvata.kmf.asset;

import com.kuvata.kmf.IAsset;

public interface IHtml extends IAsset
{
	public String getFileloc();
	public Long getAdler32();	
	public Long getFilesize();
	public String getOriginalFilename();
	public Boolean getSetTransparentBg();
}
