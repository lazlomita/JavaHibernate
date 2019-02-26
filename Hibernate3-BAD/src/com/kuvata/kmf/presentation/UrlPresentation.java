/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.asset.Html;
import com.kuvata.kmf.asset.Url;
import com.kuvata.kmf.util.Reformat;
/**
 * 
 * @author anaber
 *
 */
public class UrlPresentation extends Presentation {
	private String url;
	public Integer opacity;
	public Boolean setTransparentBg;
	public Boolean html5Hwaccel;
	public UrlPresentation()
	{
	    SUBDIRECTORY = "url";	    
	}
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public UrlPresentation(Url u, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(u);
	    this.setPresenterType(presenterType);
	}
	
	public UrlPresentation(Url u) throws HibernateException, IOException
	{
	    this();
	    this.setUrl(u.getUrl());
	    this.setPresenterType(PresenterType.URL);
	    this.setAssetId( u.getAssetId() );
	    this.setOpacity(u.getOverlayOpacity());
	    this.setSetTransparentBg(u.getSetTransparentBg());
	    this.setHtml5Hwaccel(u.getHtml5Hwaccel());
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "UrlPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getUrl().hashCode(),result,5);
		result = Reformat.getSafeHash(this.getOpacity(),result,7);
		result = Reformat.getSafeHash(this.getSetTransparentBg(),result,11);
		result = Reformat.getSafeHash(this.getHtml5Hwaccel(),result,13);
		return result < 0 ? -result : result;
	}
	
	/**
	 * This constructor is called from the ContentScheduler.serializeAsset().
	 * We want to parse the url presentation's url to determine if we need to
	 * dynamically bind any query string parameters to the 
	 * given device's metadata.
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public UrlPresentation(Url origUrl, Device device) throws HibernateException, IOException
	{
	    this();
		
		// Make a copy of this html presentation so as to not modify the original object
		Url url = new Url();
		url.setUrl(origUrl.getUrl());
	    url.setAssetId(origUrl.getAssetId() );
	    url.setOverlayOpacity(origUrl.getOverlayOpacity());
	    url.setSetTransparentBg(origUrl.getSetTransparentBg());
	    url.setHtml5Hwaccel(origUrl.getHtml5Hwaccel());

	    /*
		 * Bind any metadata parameters that are found in the url
		 * Split the start page to locate any metadata identifiers -- i.e. {Device.zipCode}
		 */
		String newUrl = this.appendMetadataParameters( url.getUrl(), device );

		// Update the start page of the html object to be used in serialization
		url.setUrl( newUrl );					

		// Load the html presentation using the updated startpage
		loadUrlPresentation( url );
	}		
	
	private void loadUrlPresentation(Url u) throws IOException
	{
	    this.setUrl(u.getUrl());
	    this.setPresenterType(PresenterType.URL);
	    this.setAssetId( u.getAssetId() );
	    this.setOpacity(u.getOverlayOpacity());
	    this.setSetTransparentBg(u.getSetTransparentBg());
	    this.setHtml5Hwaccel(u.getHtml5Hwaccel());
	}	
	
	/**
	 * Implements parent's abstract method
	 */
	public ReferencedFile getReferencedFile()
	{
		return null;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getOpacity() {
		return opacity;
	}
	public void setOpacity(Integer opacity) {
		this.opacity = opacity;
	}
	public Boolean getSetTransparentBg() {
		return setTransparentBg;
	}
	public void setSetTransparentBg(Boolean setTransparentBg) {
		this.setTransparentBg = setTransparentBg;
	}
	public Boolean getHtml5Hwaccel() {
		return html5Hwaccel;
	}
	public void setHtml5Hwaccel(Boolean html5Hwaccel) {
		this.html5Hwaccel = html5Hwaccel;
	}
}
