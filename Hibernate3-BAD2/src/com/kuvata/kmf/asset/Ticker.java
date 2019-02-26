/*
 * Created on Oct 29, 2004
 */
package com.kuvata.kmf.asset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Clob;
import java.util.ArrayList;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.TickerFontType;
import com.kuvata.kmf.usertype.TickerPresentationStyle;
import com.kuvata.kmf.usertype.TickerStyleType;
import com.kuvata.kmf.usertype.TickerType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.util.Reformat;

/**
 * @author jrandesi
 */
public class Ticker extends Asset implements ITicker
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.TickerPresentation";

	private String fileloc;
	private Long adler32;
	private Clob tickerText;
	private TickerFontType tickerFont;
	private TickerStyleType tickerStyle;
	private String tickerColor;
	private String tickerBackgroundColor;
	private String tickerFontSize;
	private String rssUrl;
	private Long rssPollFrequency;
	private Boolean rssReadTitle;
	private Boolean rssReadDescription;
	private TickerType tickerType;
	private Integer overlayOpacity;
	private TickerPresentationStyle tickerPresentationStyle;
	private static String createAssetPage = "createAssetTicker";
	private static String assetPropertiesPage = "assetPropertiesTicker";	
	
	public Ticker()
	{
	}
	
	public ITicker getInterface()
	{
		System.out.println("in getInteface()");
		return (ITicker)this;
	}

	public String getPresentationType()
	{
	    return PRESENTATION_TYPE;
	}

	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getCreateAssetPage()
	{
		return Ticker.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Ticker.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.TICKER;
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension) throws FileNotFoundException, IOException
	{		
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getPreviewPath()
	{
		if( previewPath == null )
		{
			previewPath = "/"+ Constants.APP_NAME +"/renderEntityAction.action?entityId="+ this.getAssetId().toString();
		}
		return previewPath;
	}

	/**
	 * Implements the parent's abstract method.
	 */
	public String getThumbnailPath()
	{
		if( thumbnailPath == null )
		{
			thumbnailPath = "/"+ Constants.APP_NAME +"/images/icons/html.gif";
		}
		return thumbnailPath;
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{			
		// Build the html according to the properties of the ticker
		StringBuffer result = new StringBuffer();		
		String textDecoration = "";
		String tickerText = Reformat.convertClobToString( this.tickerText, true );
		
		// Defaults
		if( this.tickerFontSize == null || this.tickerFontSize.length() == 0 ){
			this.tickerFontSize = "24";
		}
		
		if( this.tickerType != null && this.tickerType.getPersistentValue().equalsIgnoreCase( TickerType.RSS.getPersistentValue() ) ){
			tickerText = "{RSS feed}";
		}
		
		// Replace any new line characters with <BR>
		tickerText = tickerText.replaceAll("\\\n", "<br>");
					
		// Vertically center the ticker text
		if( this.tickerStyle != null){
			
			// If this ticker needs to be underlined, it must be specified as text-decoration
			if( this.tickerStyle.getPersistentValue().equalsIgnoreCase("u") ) {
				textDecoration = " ; text-decoration:underline";
			}
			result.append("<" + this.tickerStyle.getPersistentValue() + ">");
			result.append("<span style=\"height:auto; font-family:"+ this.tickerFont.getPersistentValue() +"; font-size:" + this.tickerFontSize + "; color:" + this.tickerColor + textDecoration +"\" nowrap>" + tickerText + "</span>");
			result.append("</" + this.tickerStyle.getPersistentValue() + ">");			
		}	
		else
		{
			result.append("<span style=\"font-family:"+ this.tickerFont.getPersistentValue() +"; font-size:" + this.tickerFontSize + "; color:" + this.tickerColor + "\">" + tickerText + "</span>");
		}
		return result.toString();					
	}	
	
	/**
	 * Implements the parent's abstract method
	 * Determines if this asset's referenced file exists on disk
	 */
	public boolean getReferencedFileExists()
	{
		return Asset.referencedFileExists( this.getFileloc() );
	}	
	
	/**
	 * Get the content of the ticker file
	 * 
	 * @param t
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getTickerHTML() throws FileNotFoundException, IOException
	{
		StringBuffer sb = new StringBuffer();
		File f = new File( this.getFileloc() );
		if( f.exists() )
		{
			// Read the contents of this ticker's referenced file into a string
		    BufferedReader in = new BufferedReader( new FileReader( this.getFileloc() ) );		    
		    String str = null;
		    while ((str = in.readLine()) != null) {
		    	sb.append( str );
		    }
		    in.close();
		}
	    return sb.toString();
	}	
	
		
			
	
	public static Ticker create(String assetName, Clob tickerText, String tickerColor, TickerFontType tickerFont, 
			TickerStyleType tickerStyle, String tickerBackgroundColor, String tickerFontSize, Displayarea da, Layout l, String playLength, TickerType tickerType, 
			TickerPresentationStyle tickerPresentationStyle, String rssUrl, Long rssPollFrequency, Boolean rssReadTitle, Boolean rssReadDescription, Integer overlayOpacity) throws HibernateException, IOException
	{		
		// Defaults
		if( tickerFont == null){
			tickerFont = TickerFontType.DEFAULT_FONT;
		}
		if( tickerFontSize == null || tickerFontSize.length() == 0 ){
			tickerFontSize = Constants.TICKER_FONT_SIZE_DEFAULT;
		}		
		if( tickerStyle == null){
			tickerStyle = TickerStyleType.NORMAL;
		}
		if( tickerColor == null || tickerColor.length() <= 0 ){
			tickerColor = Constants.TICKER_COLOR_DEFAULT;
		}
		if( tickerBackgroundColor == null || tickerBackgroundColor.length() == 0 ){
			tickerBackgroundColor = Constants.TICKER_BACKGROUND_COLOR_DEFAULT;
		}
		if( tickerPresentationStyle == null ){
			tickerPresentationStyle = TickerPresentationStyle.SCROLLING;
		}
		if( overlayOpacity == null ){
			overlayOpacity = 100;
		}
		
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( new Float(playLength) );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new ticker object			
		Ticker ticker = new Ticker();			
		ticker.setAssetName( assetName );
		ticker.setAssetPresentation( ap );
		ticker.setTickerColor( tickerColor );
		ticker.setTickerText( tickerText );
		ticker.setTickerFont( tickerFont );
		ticker.setTickerStyle( tickerStyle );
		ticker.setTickerBackgroundColor( tickerBackgroundColor );
		ticker.setTickerFontSize( tickerFontSize );
		ticker.setTickerType( tickerType );
		ticker.setRssUrl( rssUrl != null ? rssUrl.trim() : rssUrl );
		ticker.setRssPollFrequency( rssPollFrequency );
		ticker.setRssReadTitle( rssReadTitle );
		ticker.setRssReadDescription( rssReadDescription );		
		ticker.setTickerPresentationStyle( tickerPresentationStyle );
		ticker.setOverlayOpacity(overlayOpacity);
		ticker.save();
					
		// Build the HTML file
		String fileLoc = ticker.createTickerFile( ticker.renderHTML() );			
		ticker.setFileloc( fileLoc );						
		ticker.createThumbnail( Asset.MAX_DIMENSION );			
		ticker.setAdler32( Asset.calculateAdler32( fileLoc ) );
		ticker.update();
		return ticker;
	}
	
	public void update(String assetName, Float length, String url, Displayarea da, Layout l, Clob tickerText, String tickerColor, TickerFontType tickerFont,
			TickerStyleType tickerStyle, String tickerBackgroundColor, String tickerFontSize, TickerType tickerType, TickerPresentationStyle tickerPresentationStyle, String rssUrl,
			Long rssPollFrequency, Boolean rssReadTitle, Boolean rssReadDescription, Integer overlayOpacity) throws IOException
	{	
		AssetPresentation ap = this.getAssetPresentation();
		
		// If the default length has changed
		boolean lengthChanged = false;
		if( length != null && ap.getLength().equals( length ) == false ) {
			ap.setLength( length );
			lengthChanged = true;
		}			
		this.makeDirty( lengthChanged, false );	
		
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.update();
					
		// If any of the properties of the ticker have changed
		boolean rebuildTickerHtml = false;
		if( this.getTickerText() != null && this.getTickerText().equals( tickerText ) == false 
				|| (this.getTickerColor() != null && this.getTickerColor().equals( tickerColor ) == false)
				|| (this.getTickerFont() != null && this.getTickerFont().equals( tickerFont ) == false)
				|| (this.getTickerStyle() != null && this.getTickerStyle().equals( tickerStyle ) == false) 
				|| (this.getTickerBackgroundColor() != null && this.getTickerBackgroundColor().equals( tickerBackgroundColor ) == false)
				|| (this.getTickerFontSize() != null && this.getTickerFontSize().equals( tickerFontSize ) == false) )
		{
			// Set the flag to re-build the HTML file
			rebuildTickerHtml = true;			
		}			
				
		this.setAssetName( assetName );	
		this.setTickerColor( tickerColor );
		this.setTickerText( tickerText );
		this.setTickerFont( tickerFont );
		this.setTickerStyle( tickerStyle );	
		this.setTickerBackgroundColor( tickerBackgroundColor );
		this.setTickerFontSize( tickerFontSize );
		this.setAssetPresentation( ap );
		this.setTickerType( tickerType );
		this.setRssUrl( rssUrl != null ? rssUrl.trim() : rssUrl );
		this.setRssPollFrequency( rssPollFrequency );
		this.setRssReadTitle( rssReadTitle );
		this.setRssReadDescription( rssReadDescription );
		this.setTickerPresentationStyle( tickerPresentationStyle );
		this.setOverlayOpacity(overlayOpacity);
		
		// If any of the properties have changed, we need to rebuild the ticker file
		if( rebuildTickerHtml ){
			String tickerContents = this.renderHTML();
			String fileLoc = this.createTickerFile( tickerContents );
			this.setFileloc( fileLoc );
		}
		
		this.setAdler32( Asset.calculateAdler32( this.getFileloc() ) );
		this.update();	
	}		
			
	
	/**
	 * Generates an HTML file containing the given fileContents and saves
	 * it to the given filePath.
	 * 
	 * @param fileContents	Contents of the HTML file 
	 * @param filePath		Path in which to save the HTML file
	 * @return				Returns the path to the newly created HTML file
	 */
	public String createTickerFile(String fileContents) throws HibernateException, IOException
	{	
		// Build the file path
		String filePath = Asset.getAssetDirectory( this.getAssetType() ) +"/"+ this.getAssetId().toString() + ".html";	
		
		// Create an HTML file with the specified contents
		BufferedWriter out = null;
		out = new BufferedWriter(new FileWriter(filePath));
		out.write( "<HTML>");
		out.write("<BODY>");
		out.write(fileContents);
		out.write("</BODY>");
		out.write("</HTML>");			
		out.close();	
		return filePath;
	}	
	
	/**
	 * Copies this asset and assigns the given new asset name.
	 * 
	 * @param newAssetName
	 * @return
	 */
	public Long copy(String newAssetName) throws ClassNotFoundException
	{				
		// Create a new asset object
		Ticker newAsset = new Ticker();		
		newAsset.setAssetName( newAssetName );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setFileloc( this.getFileloc() );
		newAsset.setAdler32( this.getAdler32() );
		newAsset.setTickerText( this.getTickerText() );
		newAsset.setTickerFont( this.getTickerFont() );
		newAsset.setTickerStyle( this.getTickerStyle() );
		newAsset.setTickerColor( this.getTickerColor() );
		newAsset.setTickerBackgroundColor( this.getTickerBackgroundColor() );
		newAsset.setTickerFontSize( this.getTickerFontSize() );
		newAsset.setRssUrl( this.getRssUrl() );
		newAsset.setRssPollFrequency( this.getRssPollFrequency() );
		newAsset.setRssReadTitle( this.getRssReadTitle() );
		newAsset.setRssReadDescription( this.getRssReadDescription() );
		newAsset.setTickerType( this.getTickerType() );
		newAsset.setTickerPresentationStyle( this.getTickerPresentationStyle() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}
		
	
	/**
	 * Implements the parent's abstract method. Removes the referenced file
	 * associated with this asset if any, and calls Entity.delete() to remove
	 * this object from the database.
	 */
	public void delete() throws HibernateException
	{		
		if( this.getFileloc() != null )
		{
			File f = new File( this.getFileloc() );
			if( f.exists() )
			{
				f.delete();
			}
		}
		super.delete();
	}	
		
	/**
	 * @return Returns the fileloc.
	 */
	public String getFileloc() {
		return fileloc;
	}
	/**
	 * @param fileloc The fileloc to set.
	 */
	public void setFileloc(String fileloc) {
		this.fileloc = fileloc;
	}
	/**
	 * @return Returns the adler32.
	 */
	public Long getAdler32() {
		return adler32;
	}
	/**
	 * @param adler32 The adler32 to set.
	 */
	public void setAdler32(Long adler32) {
		this.adler32 = adler32;
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
	public TickerFontType getTickerFont() {
		return tickerFont;
	}
	/**
	 * @param tickerFont The tickerFont to set.
	 */
	public void setTickerFont(TickerFontType tickerFont) {
		this.tickerFont = tickerFont;
	}
	/**
	 * @return Returns the tickerStyle.
	 */
	public TickerStyleType getTickerStyle() {
		return tickerStyle;
	}
	/**
	 * @param tickerStyle The tickerStyle to set.
	 */
	public void setTickerStyle(TickerStyleType tickerStyle) {
		this.tickerStyle = tickerStyle;
	}
	/**
	 * @return Returns the tickerText.
	 */
	public Clob getTickerText() {
		return tickerText;
	}
	/**
	 * @param tickerText The tickerText to set.
	 */
	public void setTickerText(Clob tickerText) {
		this.tickerText = tickerText;
	}	
	/**
	 * @return Returns the tickerBackgroundColor.
	 */
	public String getTickerBackgroundColor() {
		return tickerBackgroundColor;
	}
	
	/**
	 * @param tickerBackgroundColor The tickerBackgroundColor to set.
	 */
	public void setTickerBackgroundColor(String tickerBackgroundColor) {
		this.tickerBackgroundColor = tickerBackgroundColor;
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
	 * @param assetPropertiesPage The assetPropertiesPage to set.
	 */
	public static void setAssetPropertiesPage(String assetPropertiesPage) {
		Ticker.assetPropertiesPage = assetPropertiesPage;
	}
	/**
	 * @param createAssetPage The createAssetPage to set.
	 */
	public static void setCreateAssetPage(String createAssetPage) {
		Ticker.createAssetPage = createAssetPage;
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
	 * @return Returns the rssPollFrequency.
	 */
	public Long getRssPollFrequency() {
		return rssPollFrequency;
	}
	

	/**
	 * @param rssPollFrequency The rssPollFrequency to set.
	 */
	public void setRssPollFrequency(Long rssPollFrequency) {
		this.rssPollFrequency = rssPollFrequency;
	}
	

	/**
	 * @return Returns the rssReadDescription.
	 */
	public Boolean getRssReadDescription() {
		return rssReadDescription;
	}
	

	/**
	 * @param rssReadDescription The rssReadDescription to set.
	 */
	public void setRssReadDescription(Boolean rssReadDescription) {
		this.rssReadDescription = rssReadDescription;
	}
	

	/**
	 * @return Returns the rssReadTitle.
	 */
	public Boolean getRssReadTitle() {
		return rssReadTitle;
	}
	

	/**
	 * @param rssReadTitle The rssReadTitle to set.
	 */
	public void setRssReadTitle(Boolean rssReadTitle) {
		this.rssReadTitle = rssReadTitle;
	}

	/**
	 * @return Returns the tickerType.
	 */
	public TickerType getTickerType() {
		return tickerType;
	}
	

	/**
	 * @param tickerType The tickerType to set.
	 */
	public void setTickerType(TickerType tickerType) {
		this.tickerType = tickerType;
	}

	/**
	 * @return Returns the tickerPresentationStyle.
	 */
	public TickerPresentationStyle getTickerPresentationStyle() {
		return tickerPresentationStyle;
	}
	

	/**
	 * @param tickerPresentationStyle The tickerPresentationStyle to set.
	 */
	public void setTickerPresentationStyle(
			TickerPresentationStyle tickerPresentationStyle) {
		this.tickerPresentationStyle = tickerPresentationStyle;
	}

	public Integer getOverlayOpacity() {
		return overlayOpacity;
	}

	public void setOverlayOpacity(Integer overlayOpacity) {
		this.overlayOpacity = overlayOpacity;
	}
}
