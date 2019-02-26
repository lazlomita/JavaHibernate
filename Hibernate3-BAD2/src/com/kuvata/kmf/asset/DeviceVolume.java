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
public class DeviceVolume extends Asset implements IDeviceVolume
{
	public static final String PRESENTATION_TYPE = "com.kuvata.kmf.presentation.DeviceVolumePresentation";

	private static String createAssetPage = "createAssetDeviceVolume";
	private static String assetPropertiesPage = "assetPropertiesDeviceVolume";
	
	private Integer volume;
	
	public DeviceVolume()
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
		return DeviceVolume.createAssetPage;
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return DeviceVolume.assetPropertiesPage; 
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.DEVICE_VOLUME;
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
		// No preview for power off asset type
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
	
	public static DeviceVolume create(String assetName, Integer volume, Displayarea da, Layout l)
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( 0f );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.save();
		
		// Create a new object of the given type			
		DeviceVolume a = new DeviceVolume();			
		a.setAssetName( assetName );
		a.setVolume(volume);
		a.setAssetPresentation( ap );			
		a.save();
		
		return a;
	}
	
	public void update(String assetName, Integer volume, Displayarea da, Layout l) 
	{
		AssetPresentation ap = this.getAssetPresentation();
		ap.setLength(0f);
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.update();
		
		this.setVolume(volume);
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
		DeviceVolume newAsset = new DeviceVolume();		
		newAsset.setAssetName( newAssetName );	
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
				
		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}		
}
