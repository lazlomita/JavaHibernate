package com.kuvata.kmf.asset;

import java.sql.Clob;

import com.kuvata.kmf.usertype.TickerFontType;
import com.kuvata.kmf.usertype.TickerStyleType;
import com.kuvata.kmf.usertype.TickerType;

import com.kuvata.kmf.IAsset;


public interface ITicker extends IAsset
{
	public Clob getTickerText();
	public TickerFontType getTickerFont();
	public TickerStyleType getTickerStyle();
	public String getTickerColor();
	public String getTickerBackgroundColor();
	public String getTickerFontSize();
	public String getFileloc();
	public Long getAdler32();
	public TickerType getTickerType();
}
