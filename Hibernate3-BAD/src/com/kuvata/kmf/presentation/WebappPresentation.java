/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import parkmedia.DispatcherConstants;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.asset.Webapp;
import com.kuvata.kmf.util.Files;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * @author anaber
 *
 */
public class WebappPresentation extends Presentation {
	
	private static Logger logger = Logger.getLogger(WebappPresentation.class);
	public  static final String WEBAPP_PREFIX = "webapp-";
	public ReferencedFile referencedFile;	
	private String startPage;
	public Integer opacity;
	public Boolean setTransparentBg;
	
	public WebappPresentation()
	{
	    SUBDIRECTORY = "webapp";	    
	}
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public WebappPresentation(Webapp webapp, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(webapp);
	    this.setPresenterType(presenterType);
	}
	
	/**
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public WebappPresentation(Webapp webapp) throws HibernateException, IOException
	{
	    this();	    
		loadWebappPresentation( webapp );
	}

	/**
	 * This constructor is called from the ContentScheduler.serializeAsset().
	 * We want to parse the webapp's startPage to determine if we need to
	 * dynamically bind any of the startPage query string parameters to the 
	 * given device's metadata.
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public WebappPresentation(Webapp origWebapp, Device device) throws HibernateException, IOException
	{
	    this();
		
		// Make a copy of this webapp so as to not modify the original object
		Webapp webapp = new Webapp();
		webapp.setAssetId( origWebapp.getAssetId() );		
		webapp.setStartPage( origWebapp.getStartPage() );
		webapp.setAdler32( origWebapp.getAdler32() );
		webapp.setFileloc( origWebapp.getFileloc() );
		webapp.setOverlayOpacity( origWebapp.getOverlayOpacity() );
		webapp.setSetTransparentBg( origWebapp.getSetTransparentBg() );
					
		/*
		 * Bind any metadata parameters that are found in the webapp start page
		 * Split the start page to locate any metadata identifiers -- i.e. {Device.zipCode}
		 */
		String newStartPage = this.appendMetadataParameters( webapp.getStartPage(), device );

		// Update the start page of the webapp object to be used in serialization
		webapp.setStartPage( newStartPage );					

		// Load the webapp presentation using the updated startpage
		loadWebappPresentation( webapp );
	}	
	
	private void loadWebappPresentation(Webapp webapp) throws IOException
	{
	    this.setPresenterType(PresenterType.WEBAPP);
	    this.setAssetId( webapp.getAssetId() );
		this.setStartPage( webapp.getStartPage() );
		this.setOpacity( webapp.getOverlayOpacity() );
		this.setSetTransparentBg(webapp.getSetTransparentBg());
		
	    String extension = getFileExtension( webapp.getFileloc() );
	    String newFileloc = SUBDIRECTORY + "/" +webapp.getWebappId()+"-webapp-"+webapp.getAdler32() + extension;
	    ReferencedFile webappFile = new ReferencedFile( newFileloc );
		webappFile.createFrom( webapp.getFileloc() );	    
	    this.setReferencedFile( webappFile );			
	}
	
	/**
	 * Returns the path of the directory (name of the referenced file) to be used when constructing the 
	 * URL to the webapp start page.
	 * @return
	 */
	public String getWebappDirectoryName(boolean removeFileExtension)
	{
		String result = "";
		if( this.getReferencedFile() != null && this.getReferencedFile().getFileloc() != null ) 
		{
			// Get the name of the .war file without any path
			result = this.getReferencedFile().getFileloc().substring( this.getReferencedFile().getFileloc().lastIndexOf("/") + 1 );
			
			// Add a prefix to the webapp name because JBoss does not allow webapps to start with a number
			result = WEBAPP_PREFIX + result;
			
			// Remove the file extension to get the path as required by JBoss
			if( removeFileExtension ){
				result = result.substring( 0, result.lastIndexOf(".") );
			}			
		}
		return result;
	}
	
	/**
	 * Builds the unique name of the war file and copies it to the webappDeployment directory.
	 *
	 */
	public static void deploy(String presentationFullPath)
	{		
		if(presentationFullPath.endsWith(".war")){
			String webappDeploymentFullPath = KuvataConfig.getPropertyValue( DispatcherConstants.WEBAPP_DEPLOYMENT_DIR );
			if( webappDeploymentFullPath.endsWith("/") == false ) {
				webappDeploymentFullPath = webappDeploymentFullPath +"/";
			}
			webappDeploymentFullPath += WEBAPP_PREFIX + presentationFullPath.substring(presentationFullPath.lastIndexOf("/") + 1);
			
			// Copy the war file into the jboss deployment directory
			logger.info("copying war file "+ presentationFullPath + " to "+ webappDeploymentFullPath);
			Files.copyFile( presentationFullPath, webappDeploymentFullPath );
		}
		
	}	
	
	public String getDeploymentFullPath()
	{
		String webappDeploymentFullPath = KuvataConfig.getPropertyValue( DispatcherConstants.WEBAPP_DEPLOYMENT_DIR );
		if( webappDeploymentFullPath.endsWith("/") == false ) {
			webappDeploymentFullPath = webappDeploymentFullPath +"/";
		}
		webappDeploymentFullPath += this.getWebappDirectoryName( false );
		return webappDeploymentFullPath;
	}
	
	/**
	 * Returns the name of the directory (name of the referenced file) to be used when constructing the 
	 * URL to the webapp start page.
	 * @return
	 */
	public String getWebappDirectoryName()
	{
		return getWebappDirectoryName( true );
	}
	
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "WebappPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId(),result,3);
		if( this.getReferencedFile() != null ){
			result = Reformat.getSafeHash(this.getReferencedFile().getFileloc(),result,5);
		}		
		result = Reformat.getSafeHash(this.getStartPage(),result,7);
		result = Reformat.getSafeHash(this.getOpacity(),result,11);
		result = Reformat.getSafeHash(this.getSetTransparentBg(),result,13);
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
}
