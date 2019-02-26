/*
 * Created on Feb 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.usertype.TickerFontType;
import com.kuvata.kmf.usertype.TickerPresentationStyle;
import com.kuvata.kmf.usertype.TickerStyleType;
import com.kuvata.kmf.usertype.TickerType;
import com.kuvata.kmf.asset.Ticker;
import com.kuvata.kmf.util.Reformat;

/**
 * @author jrourke
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class TickerPresentation extends Presentation {
	
	private static Logger logger = Logger.getLogger(TickerPresentation.class);
	public ReferencedFile referencedFile;
	public String tickerBackgroundColor;
	public String tickerColor;	
	public String tickerFont;
	public String tickerFontSize;
	public String tickerStyle;
	public String tickerText;	
	public String rssUrl;
	public Long rssPollFrequency;		
	public String tickerType;
	public String tickerPresentationStyle;
	public boolean rssReadTitle;			
	public boolean rssReadDescription;
	public Integer opacity;
	
	/**
	 * 
	 *
	 */
	public TickerPresentation()
	{
	    SUBDIRECTORY = "ticker";	    
	}
	/**
	 * 
	 * @param t
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public TickerPresentation(Ticker t, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(t);				
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param ticker
	 * @throws HibernateException
	 * @throws IOException
	 */
	public TickerPresentation(Ticker ticker) throws HibernateException, IOException
	{
	    this();	    		
	    this.setPresenterType(PresenterType.TICKER);
	    this.setAssetId( ticker.getAssetId() );
		this.setTickerText( ticker.getTickerText() != null ? Reformat.convertClobToString( ticker.getTickerText(), true ) : "" );
		this.setTickerBackgroundColor( ticker.getTickerBackgroundColor() );
		this.setTickerColor( ticker.getTickerColor() );
		this.setTickerFontSize( ticker.getTickerFontSize() );
		this.setRssUrl( ticker.getRssUrl() );
		this.setRssPollFrequency( ticker.getRssPollFrequency() );
		this.setOpacity(ticker.getOverlayOpacity());
		if( ticker.getRssReadTitle() != null ){
			this.setRssReadTitle( ticker.getRssReadTitle() );
		}else{
			this.setRssReadTitle( false );
		}
		if( ticker.getRssReadDescription() != null ){
			this.setRssReadDescription( ticker.getRssReadDescription() );
		}else{
			this.setRssReadDescription( false );
		}
		if( ticker.getTickerFont() != null ){
			this.setTickerFont( ticker.getTickerFont().getPersistentValue() );
		}else{
			this.setTickerFont( TickerFontType.DEFAULT_FONT.getPersistentValue() ); 	// Default
		}
		if( ticker.getTickerPresentationStyle() != null ){
			this.setTickerPresentationStyle( ticker.getTickerPresentationStyle().getPersistentValue() );
		}else{
			this.setTickerPresentationStyle( TickerPresentationStyle.SCROLLING.getPersistentValue() ); 	// Default
		}
		if( ticker.getTickerType() != null ){
			this.setTickerType( ticker.getTickerType().getPersistentValue() );
		}else{
			this.setTickerType( TickerType.TEXT.getPersistentValue() );		// Default
		}
		if( ticker.getTickerStyle() != null){
			this.setTickerStyle( ticker.getTickerStyle().getPersistentValue() );
		}else{
			this.setTickerStyle( TickerStyleType.NORMAL.getPersistentValue() );
		}
	    String extension = getFileExtension(ticker.getFileloc());
	    String newFileloc = SUBDIRECTORY + "/" +ticker.getAssetId()+"-ticker-"+ticker.getAdler32() + extension;
	    ReferencedFile imageFile = new ReferencedFile( newFileloc );
	    imageFile.createFrom( ticker.getFileloc() );	    
	    this.setReferencedFile( imageFile );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = this.getClass().getName().hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getReferencedFile().getFileloc().hashCode(),result,5);
		result = Reformat.getSafeHash(this.getTickerBackgroundColor(),result,7);
		result = Reformat.getSafeHash(this.getTickerColor(),result,11);
		result = Reformat.getSafeHash(this.getTickerFont(),result,13);
		result = Reformat.getSafeHash(this.getTickerFontSize(),result,17);
		result = Reformat.getSafeHash(this.getTickerStyle(),result,19);
		result = Reformat.getSafeHash(this.getTickerText(),result,23);
		result = Reformat.getSafeHash(this.getRssUrl(),result,29);
		result = Reformat.getSafeHash(this.getRssPollFrequency(),result,31);
		result = Reformat.getSafeHash(this.isRssReadTitle(),result,37);
		result = Reformat.getSafeHash(this.isRssReadDescription(),result,41);
		result = Reformat.getSafeHash(this.getTickerPresentationStyle(),result,43);
		result = Reformat.getSafeHash(this.getTickerType(),result,47);
		result = Reformat.getSafeHash(this.getOpacity(),result,53);
		return result < 0 ? -result : result;		
	}

	
	/**
	 * @return Returns the tickerColor.
	 */
	public String getTickerBackgroundColor() {
		return tickerBackgroundColor;
	}	
	/**
	 * @param tickerColor The tickerColor to set.
	 */
	public void setTickerBackgroundColor(String tickerColor) {
		this.tickerBackgroundColor = tickerColor;
	}	
	/**
	 * @return Returns the referencedFile.
	 */
	public ReferencedFile getReferencedFile() {
		return referencedFile;
	}
	/**
	 * @param referencedFile The referencedFile to set.
	 */
	public void setReferencedFile(ReferencedFile referencedFile) {
		this.referencedFile = referencedFile;
	}
	/**
	 * @return Returns the pollFrequency.
	 */
	public Long getRssPollFrequency() {
		return rssPollFrequency;
	}
	
	/**
	 * @param pollFrequency The pollFrequency to set.
	 */
	public void setRssPollFrequency(Long pollFrequency) {
		this.rssPollFrequency = pollFrequency;
	}
	
	/**
	 * @return Returns the rssUrl.
	 */
	public String getRssUrl() {
		return rssUrl;
	}
	
	/**
	 * @param rssUrl The rssUrl to set.
	 */
	public void setRssUrl(String rssUrl) {
		this.rssUrl = rssUrl;
	}
	/**
	 * @return Returns the readRssDescription.
	 */
	public boolean isRssReadDescription() {
		return rssReadDescription;
	}
	
	/**
	 * @param readRssDescription The readRssDescription to set.
	 */
	public void setRssReadDescription(boolean readRssDescription) {
		this.rssReadDescription = readRssDescription;
	}
	
	/**
	 * @return Returns the readRssTitle.
	 */
	public boolean isRssReadTitle() {
		return rssReadTitle;
	}
	
	/**
	 * @param readRssTitle The readRssTitle to set.
	 */
	public void setRssReadTitle(boolean readRssTitle) {
		this.rssReadTitle = readRssTitle;
	}
	
	/**
	 * @return Returns the tickerColor.
	 */
	public String getTickerColor() {
		return tickerColor;
	}
	
	/**
	 * @param tickerColor The tickerColor to set.
	 */
	public void setTickerColor(String tickerColor) {
		this.tickerColor = tickerColor;
	}
	
	/**
	 * @return Returns the tickerFont.
	 */
	public String getTickerFont() {
		return tickerFont;
	}
	
	/**
	 * @param tickerFont The tickerFont to set.
	 */
	public void setTickerFont(String tickerFont) {
		this.tickerFont = tickerFont;
	}
	
	/**
	 * @return Returns the tickerFontSize.
	 */
	public String getTickerFontSize() {
		return tickerFontSize;
	}
	
	/**
	 * @param tickerFontSize The tickerFontSize to set.
	 */
	public void setTickerFontSize(String tickerFontSize) {
		this.tickerFontSize = tickerFontSize;
	}
	
	/**
	 * @return Returns the tickerStyle.
	 */
	public String getTickerStyle() {
		return tickerStyle;
	}
	
	/**
	 * @param tickerStyle The tickerStyle to set.
	 */
	public void setTickerStyle(String tickerStyle) {
		this.tickerStyle = tickerStyle;
	}
	
	/**
	 * @return Returns the tickerText.
	 */
	public String getTickerText() {
		return tickerText;
	}
	
	/**
	 * @param tickerText The tickerText to set.
	 */
	public void setTickerText(String tickerText) {
		this.tickerText = tickerText;
	}
	/**
	 * @return Returns the tickerPresentationStyle.
	 */
	public String getTickerPresentationStyle() {
		return tickerPresentationStyle;
	}
	
	/**
	 * @param tickerPresentationStyle The tickerPresentationStyle to set.
	 */
	public void setTickerPresentationStyle(String tickerPresentationStyle) {
		this.tickerPresentationStyle = tickerPresentationStyle;
	}
	/**
	 * @return Returns the tickerType.
	 */
	public String getTickerType() {
		return tickerType;
	}
	
	/**
	 * @param tickerType The tickerType to set.
	 */
	public void setTickerType(String tickerType) {
		this.tickerType = tickerType;
	}
	public Integer getOpacity() {
		return opacity;
	}
	public void setOpacity(Integer opacity) {
		this.opacity = opacity;
	}
}