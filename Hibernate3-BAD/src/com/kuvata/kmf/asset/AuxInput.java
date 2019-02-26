/*
 * Created on Jan 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.asset;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Layout;

/**
 * @author anaber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AuxInput extends Asset implements IAuxInput
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.AuxInputPresentation";

	private static String createAssetPage = "createAssetAuxInput";
	private static String assetPropertiesPage = "assetPropertiesAuxInput";
	
	public AuxInput()
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
		return AuxInput.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return AuxInput.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.AUX_INPUT;
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
		// No preview for AuxInput asset type
		return "";
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getThumbnailPath()
	{
		// Do not show preview icon
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
	 * Implements the parent's abstract method. 
	 */
	public void delete() throws HibernateException
	{
		super.delete();
	}	
	
	/**
	 * Implements the parent's abstract method
	 * Since this asset type does not have a referenced file, return true
	 */
	public boolean getReferencedFileExists()
	{
		return true;
	}
	
	public static AuxInput create(String assetName, Float length, Displayarea da, Layout l)
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		AuxInput a = new AuxInput();			
		a.setAssetName( assetName );
		a.setAssetPresentation( ap );			
		a.save();
		
		return a;
	}
	
	public void update(String assetName, Float length, Displayarea da, Layout l) 
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
			
		this.setAssetName( assetName );		
		this.setAssetPresentation( ap );
		this.update();
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
		AuxInput newAsset = new AuxInput();		
		newAsset.setAssetName( newAssetName );	
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		newAsset.copyPermissionEntries( this );
		
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}		
	
}
