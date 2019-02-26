package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.asset.AdServer;
import com.kuvata.kmf.asset.Audio;
import com.kuvata.kmf.asset.AuxInput;
import com.kuvata.kmf.asset.DeviceVolume;
import com.kuvata.kmf.asset.Flash;
import com.kuvata.kmf.asset.Html;
import com.kuvata.kmf.asset.Image;
import com.kuvata.kmf.asset.PowerOff;
import com.kuvata.kmf.asset.StreamingClient;
import com.kuvata.kmf.asset.TargetedAsset;
import com.kuvata.kmf.asset.Ticker;
import com.kuvata.kmf.asset.Url;
import com.kuvata.kmf.asset.Video;
import com.kuvata.kmf.asset.Webapp;
import com.kuvata.kmf.asset.Xml;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * @author Lazlo Mita
 * Created on July 26, 2018
 * Copyright 2018, Inception Signage, Inc.
 */
public class AssetType extends PersistentStringEnum 
{	
	public static final AssetType VIDEO = new AssetType("Video", Video.class.getName(), false, false, false);
	public static final AssetType AUDIO = new AssetType("Audio", Audio.class.getName(), false, false, false);
	public static final AssetType IMAGE = new AssetType("Image", Image.class.getName(), false, false, false);
	public static final AssetType FLASH = new AssetType("Flash Animated", Flash.class.getName(), false, false, false);	
	public static final AssetType WEBAPP = new AssetType("Web Application", Webapp.class.getName(), false, false, false);	
	public static final AssetType HTML = new AssetType("HTML", Html.class.getName(), false, false, false);		
	public static final AssetType URL = new AssetType("URL", Url.class.getName(), false, false, false);
	public static final AssetType TICKER = new AssetType("Ticker", Ticker.class.getName(), false, false, false);	
	public static final AssetType XML = new AssetType("XML", Xml.class.getName(), false, false, false);
	public static final AssetType STREAMING_CLIENT = new AssetType("Live Video", StreamingClient.class.getName(), false, false, false);	
	
	// MCM Assets
	public static final AssetType AUX_INPUT = new AssetType("Auxiliary Input", AuxInput.class.getName(), false, false, true);
	public static final AssetType POWER_OFF = new AssetType("Power Off", PowerOff.class.getName(), false, false, true);
	
	// Control Assets
	public static final AssetType DEVICE_VOLUME = new AssetType("Device Volume", DeviceVolume.class.getName(), true, false, false);
	
	// Dynamic Assets
	public static final AssetType TARGETED_ASSET = new AssetType("Targeted Asset", TargetedAsset.class.getName(), false, true, false);
	public static final AssetType AD_SERVER = new AssetType("Ad Server", AdServer.class.getName(), false, true, false);
	
	public static final Map<String, AssetType> INSTANCES = new HashMap<String, AssetType>();
	private boolean isControlAsset;
	private boolean isDynamicAsset;
	private boolean isMcmAsset;
		    
	static
	{
		INSTANCES.put(AUDIO.toString(), AUDIO);
		INSTANCES.put(FLASH.toString(), FLASH);
		INSTANCES.put(HTML.toString(), HTML);	
		INSTANCES.put(IMAGE.toString(), IMAGE);
		INSTANCES.put(VIDEO.toString(), VIDEO);				
		INSTANCES.put(WEBAPP.toString(), WEBAPP);
		INSTANCES.put(URL.toString(), URL);
		INSTANCES.put(TICKER.toString(), TICKER);
		INSTANCES.put(TARGETED_ASSET.toString(), TARGETED_ASSET);
		INSTANCES.put(AUX_INPUT.toString(), AUX_INPUT);
		INSTANCES.put(POWER_OFF.toString(), POWER_OFF);
		INSTANCES.put(STREAMING_CLIENT.toString(), STREAMING_CLIENT);
		INSTANCES.put(AD_SERVER.toString(), AD_SERVER);
		INSTANCES.put(DEVICE_VOLUME.toString(), DEVICE_VOLUME);
		
		// Intentionally omitting XML -- not currently used
		//INSTANCES.put(XML.toString(), XML);
		//INSTANCES.put(STREAMING_SERVER.toString(), STREAMING_SERVER);
	}
	
	public AssetType() {}

	public AssetType(String name, String persistentValue) {
		super(name, persistentValue);
		this.name = name;
		this.isControlAsset = false;
		this.isDynamicAsset = false;
		this.isMcmAsset = false;
	}
	
	public AssetType(String name, String persistentValue, boolean isControlAsset, boolean isDynamicAsset, boolean isMcmAsset) {
		super(name, persistentValue);
		this.isControlAsset = isControlAsset;
		this.isDynamicAsset = isDynamicAsset;
		this.isMcmAsset = isMcmAsset;
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getAssetTypeName()
	{
		return this.name;
	}
	
	/**
	 * This method returns the everything beyond the last period in the persistent value
	 * 
	 * @return
	 */
	public String getPartialPersistentValue(boolean toLowerCase)
	{
		String result = null;		
		if( this.getPersistentValue().lastIndexOf(".") > 0 ) {
			result = this.getPersistentValue().substring( this.getPersistentValue().lastIndexOf(".") + 1 );
			
			// If the flag was passed in to lowercase the result
			if( toLowerCase ) result = result.toLowerCase();
		}
		return result;
	}
	
	public String getPartialPersistentValue()
	{
		return getPartialPersistentValue( true );
	}
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List<AssetType> getAssetTypes(boolean includeAllAssetTypes, boolean includeControlAssetTypes)
	{
		List<AssetType> l = new LinkedList<AssetType>( AssetType.INSTANCES.values() );		
		
		// Sort the list in alphabetical order
		BeanPropertyComparator comparator = new BeanPropertyComparator( "name" );		
		Collections.sort( l, comparator );					
		
		if(includeControlAssetTypes == false){
			l.remove(AssetType.DEVICE_VOLUME);
		}
		
		return l;
	}
	
	/**
	 * Returns a list of only those asset that are not "control" assets and are not "dynamic" assets
	 * @return
	 */
	public static List<AssetType> getNonSpecialAssetTypes()
	{
		List<AssetType> l = new LinkedList<AssetType>();		
		for( Iterator<AssetType> i = AssetType.INSTANCES.values().iterator(); i.hasNext(); ){
			AssetType at = (AssetType)i.next();
			
			// Only add "non-control" assets
			if( at.isControlAsset() == false && at.isDynamicAsset() == false && at.isMcmAsset() == false ) {
				l.add( at );	
			}			
		}
		
		// Sort the list in alphabetical order
		BeanPropertyComparator comparator = new BeanPropertyComparator( "name" );		
		Collections.sort( l, comparator );			
		return l;
	}	
	
	/**
	 * Returns a list of control assets only
	 * @return
	 */
	public static List<AssetType> getControlAssetTypes()
	{
		List<AssetType> l = new LinkedList<AssetType>();		
		for( Iterator<AssetType> i = AssetType.INSTANCES.values().iterator(); i.hasNext(); ){
			AssetType at = i.next();
			
			// Only add "control" assets
			if( at.isControlAsset() ) {
				l.add( at );	
			}			
		}
		
		// Sort the list in alphabetical order
		BeanPropertyComparator comparator = new BeanPropertyComparator( "name" );		
		Collections.sort( l, comparator );			
		return l;
	}
	
	/**
	 * Returns a list of mcm assets only
	 * @return
	 */
	public static List<AssetType> getMcmAssetTypes()
	{
		List<AssetType> l = new LinkedList<AssetType>();		
		for( Iterator<AssetType> i = AssetType.INSTANCES.values().iterator(); i.hasNext(); ){
			AssetType at = i.next();
			
			// Only add "mcm" assets
			if( at.isMcmAsset() ) {
				l.add( at );	
			}			
		}
		
		// Sort the list in alphabetical order
		BeanPropertyComparator comparator = new BeanPropertyComparator( "name" );		
		Collections.sort( l, comparator );			
		return l;
	}
	
	/**
	 * Returns a list of dynamic assets only
	 * @return
	 */
	public static List<AssetType> getDynamicAssetTypes()
	{
		List<AssetType> l = new LinkedList<AssetType>();		
		for( Iterator<AssetType> i = AssetType.INSTANCES.values().iterator(); i.hasNext(); ){
			AssetType at = i.next();
			
			// Only add "dynamic" assets
			if( at.isDynamicAsset() ) {
				l.add( at );	
			}			
		}
		
		// Sort the list in alphabetical order
		BeanPropertyComparator comparator = new BeanPropertyComparator( "name" );		
		Collections.sort( l, comparator );			
		return l;
	}		
	
		
	/**
	 * Perform a case-insensitive search for an asset type with the given assetTypeName.
	 * @param assetTypeName
	 * @return
	 */
	public static AssetType getAssetType(String assetTypeName)
	{
		for( Iterator<String> i=INSTANCES.keySet().iterator(); i.hasNext(); )
		{
			String strAssetTypeName = i.next();
			if( strAssetTypeName.equalsIgnoreCase( assetTypeName ) ){
				return (AssetType) INSTANCES.get( strAssetTypeName );
			}
		}
		return null;		
	}	
	
	/**
	 * 
	 * @param assetTypePersistentValue
	 * @return
	 */
	public static AssetType getAssetTypeByPersistentValue(String assetTypePersistentValue)
	{
		for( Iterator<AssetType> i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			AssetType assetType = i.next();
			if( assetType.getPersistentValue().equalsIgnoreCase( assetTypePersistentValue ) ){
				return assetType;
			}
		}
		return null;
	}			
	
	/**
	 * @return Returns the isControlAsset.
	 */
	public boolean isControlAsset() {
		return isControlAsset;
	}
	/**
	 * @param isControlAsset The isControlAsset to set.
	 */
	public void setControlAsset(boolean isControlAsset) {
		this.isControlAsset = isControlAsset;
	}

	/**
	 * @return the isDynamicAsset
	 */
	public boolean isDynamicAsset() {
		return isDynamicAsset;
	}

	/**
	 * @param isDynamicAsset the isDynamicAsset to set
	 */
	public void setDynamicAsset(boolean isDynamicAsset) {
		this.isDynamicAsset = isDynamicAsset;
	}

	public boolean isMcmAsset() {
		return isMcmAsset;
	}

	public void setMcmAsset(boolean isMcmAsset) {
		this.isMcmAsset = isMcmAsset;
	}
	
}
