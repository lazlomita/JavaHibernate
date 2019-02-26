/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import parkmedia.DispatcherConstants;
import parkmedia.KuvataConfig;
import parkmedia.device.configurator.DeviceProperty;
import com.kuvata.kmf.usertype.PresenterType;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.asset.Html;
import com.kuvata.kmf.util.Files;
import com.kuvata.kmf.util.Reformat;
/**
 * @author anaber
 *
 */
public class HtmlPresentation extends Presentation {
	
	private static Logger logger = Logger.getLogger(HtmlPresentation.class);
	private String startPage;
	public ReferencedFile referencedFile;
	public Integer opacity;
	public Boolean setTransparentBg;
	public Boolean html5Hwaccel;
	/**
	 * 
	 *
	 */
	public HtmlPresentation()
	{
	    SUBDIRECTORY = "html";	    
	}
	/**
	 * 
	 * @param h
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public HtmlPresentation(Html h, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(h);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param h
	 * @throws HibernateException
	 * @throws IOException
	 */
	public HtmlPresentation(Html h) throws HibernateException, IOException
	{
	    this();
		loadHtmlPresentation( h );			    
	}
	
	/**
	 * This constructor is called from the ContentScheduler.serializeAsset().
	 * We want to parse the html presentation's startPage to determine if we need to
	 * dynamically bind any of the startPage query string parameters to the 
	 * given device's metadata.
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public HtmlPresentation(Html origHtml, Device device) throws HibernateException, IOException
	{
	    this();
		
		// Make a copy of this html presentation so as to not modify the original object
		Html html = new Html();
		html.setAssetId( origHtml.getAssetId() );		
		html.setStartPage( origHtml.getStartPage() );
		html.setAdler32( origHtml.getAdler32() );
		html.setFileloc( origHtml.getFileloc() );
		html.setOverlayOpacity( origHtml.getOverlayOpacity() );
		html.setSetTransparentBg( origHtml.getSetTransparentBg() );
		html.setHtml5Hwaccel( origHtml.getHtml5Hwaccel() );
					
		/*
		 * Bind any metadata parameters that are found in the webapp start page
		 * Split the start page to locate any metadata identifiers -- i.e. {Device.zipCode}
		 */
		String newStartPage = this.appendMetadataParameters( html.getStartPage(), device );

		// Update the start page of the html object to be used in serialization
		html.setStartPage( newStartPage );					

		// Load the html presentation using the updated startpage
		loadHtmlPresentation( html );
	}		
	
	private void loadHtmlPresentation(Html html) throws IOException
	{
	    this.setPresenterType(PresenterType.HTML);
	    this.setAssetId( html.getAssetId() );
		this.setStartPage( html.getStartPage() );
		this.setOpacity(html.getOverlayOpacity());
		this.setSetTransparentBg(html.getSetTransparentBg());
		this.setHtml5Hwaccel(html.getHtml5Hwaccel());
		
	    String extension = getFileExtension( html.getFileloc() );
	    String newFileloc = SUBDIRECTORY + "/" +html.getHtmlpageId()+"-html-"+html.getAdler32() + extension;
	    ReferencedFile zipFile = new ReferencedFile( newFileloc );
		zipFile.createFrom( html.getFileloc() );	    
	    this.setReferencedFile( zipFile );			
	}	
	
	public static void deploy(String presentationFullPath)
	{
		// If this is a .zip file
		if( presentationFullPath.endsWith(".zip") )
		{
			// Strip out the .zip from the file name
			String filename = presentationFullPath.substring( presentationFullPath.lastIndexOf("/") + 1 );
			filename = filename.substring(0, filename.lastIndexOf("."));
			
			// Build the path to the HtmlRender folder
			String htmlDeploymentPath = KuvataConfig.getPropertyValue( DispatcherConstants.HTML_DEPLOYMENT_DIR );
			if( htmlDeploymentPath.endsWith("/") == false ) {
				htmlDeploymentPath += "/";
			}
			htmlDeploymentPath = htmlDeploymentPath + filename;
			
			// Create the folder under the HtmlRenderer webapp
			// and unzip the .zip file into the HtmlRenderer webapp subfolder if it does not alredy exist
			File f = new File( htmlDeploymentPath );
			if( f.exists() == false ) {
				logger.info("Creating html renderable folder: "+ htmlDeploymentPath);
				f.mkdir();				
				Files.unzip( presentationFullPath, htmlDeploymentPath, true );
				
				// Do this only on a linux device
				if(DeviceProperty.getCurrentPlatform().equals(DeviceProperty.PLATFORM_LINUX)){
					// chown the newly created html directory so that the apache user can write to the directory
					File htmlDir = new File( htmlDeploymentPath );
					if( htmlDir.exists() ) {
						String cmd = "chown -R apache:root "+ htmlDeploymentPath;						
						logger.info("Setting ownership of html deployment dir: "+ cmd);
						try {
							Process proc = Runtime.getRuntime().exec( cmd );
							proc.waitFor();
						} catch (Exception e) {
							logger.error( e );
						}
					}
					htmlDir = null;
				}
			}
			f = null;
		}
	}
	
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "HtmlPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getReferencedFile().getFileloc().hashCode(),result,5);
		result = Reformat.getSafeHash(this.getStartPage().hashCode(),result,7);
		result = Reformat.getSafeHash(this.getOpacity(),result,11);
		result = Reformat.getSafeHash(this.getSetTransparentBg(),result,13);
		result = Reformat.getSafeHash(this.getHtml5Hwaccel(),result,17);
		return result < 0 ? -result : result;
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
	 * @return Returns the startPage.
	 */
	public String getStartPage() {
		return startPage;
	}
	/**
	 * @param startPage The startPage to set.
	 */
	public void setStartPage(String startPage) {
		this.startPage = startPage;
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
