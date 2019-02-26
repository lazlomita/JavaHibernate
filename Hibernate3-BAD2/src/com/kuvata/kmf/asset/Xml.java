/*
 * Created on Jan 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.asset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.kuvata.kmf.usertype.AssetType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.util.Files;

/**
 * @author anaber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Xml extends Asset implements IXml
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.XmlPresentation";

	private String fileloc;
	private Long adler32;
	private Long filesize;
	private static String createAssetPage = "createAssetXml";
	private static String assetPropertiesPage = "assetPropertiesXml";
	
	public Xml()
	{}	
	
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
		return Xml.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return Xml.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.XML;
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
		// Currently do not have preview for xml
		return "";
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getThumbnailPath()
	{
		// Do not show preview icon for xml
		return "";
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{		
		return "<img src=\""+ getThumbnailPath() +"\">";			
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
	 * Implements the parent's abstract method
	 * Determines if this asset's referenced file exists on disk
	 */
	public boolean getReferencedFileExists()
	{
		return Asset.referencedFileExists( this.getFileloc() );
	}	
	
	public static Xml create(String assetName, Float length, 
			Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, boolean waitForFileUpload) throws IOException, InterruptedException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		Xml a = new Xml();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );
		a.setFileloc( fileLocation );
		Long assetId = a.save();
					
		// If we're using a fileloc on the server
		String fileLoc = null;
		if( useFileLocFromServer )
		{
			// Create a physical link to the file
			fileLoc = a.uploadLargeFile( fileLocation, waitForFileUpload );
			
			// First evict, then re-get the asset in case it was changed in the uploadLargeFile thread
			Session session = HibernateSession.currentSession();
			session.evict( a );
			a = (Xml)Asset.getAsset( a.getAssetId() );			
		}			
		return a;
	}
	
	public void update(String assetName, Float length, 
			Displayarea da, Layout l, boolean useFileLocFromServer, String fileLocation, boolean waitForFileUpload) throws IOException, InterruptedException
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
		
		// If we're using a fileloc from the server
		if( useFileLocFromServer )
		{
			// If the fileloc property has changed -- re-upload the file
			if( this.getFileloc() == null || this.getFileloc().equals( fileLocation ) == false)
			{
				if( fileLocation != null && fileLocation.length() > 0 )
				{
					/*
					 * uploadLargeFile() spawns a thread which may have updated this object in another hibernate session.
					 * Sometimes this thread is finished running by the time we get here, but sometimes it's not
					 * (in the case where the update is happening from the user interface).
					 * We need to be careful about how we use this object in this session after this call
					 * because there is another thread updating the same database row outside of this object's 
					 * identity scope.
					 */
					String fileLoc = this.uploadLargeFile( fileLocation, waitForFileUpload );
					this.setFileloc( fileLoc );
				}
			}
		}				
		this.setAssetName( assetName );		
		this.setAssetPresentation( ap );
		this.setAdler32( Asset.calculateAdler32( this.getFileloc() ) );
		this.update();
	}	
	
	/**
	 * Copies this asset and assigns the given new asset name.
	 * 
	 * @param newAssetName
	 * @return
	 */
	public Long copy(String newAssetName) throws ClassNotFoundException, InterruptedException
	{				
		// Create a new asset object
		Xml newAsset = new Xml();		
		newAsset.setAssetName( newAssetName );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		
		// Copy the referenced file and set the associated properties
		newAsset.uploadLargeFile( this.getFileloc(), true );			
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
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
	 * @return Returns the xmlpageId.
	 */
	public Long getXmlpageId() {
		return this.getAssetId();
	}
	/**
	 * @param htmlpageId The xmlpageId to set.
	 */
	public void setXmlpageId(Long xmlpageId) {
		this.setAssetId(xmlpageId);
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
	 * @return Returns the filesize.
	 */
	public Long getFilesize() {
		return filesize;
	}
	/**
	 * @param filesize The filesize to set.
	 */
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}
}
