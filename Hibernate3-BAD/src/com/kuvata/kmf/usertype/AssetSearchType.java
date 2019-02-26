package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class AssetSearchType extends PersistentStringEnum 
{	
	public static final AssetSearchType ASSET_NAME = new AssetSearchType("Asset Name", "asset_name");	
	public static final AssetSearchType ASSET_ID = new AssetSearchType("Asset ID", "asset_id");	
	public static final LinkedHashMap<String, AssetSearchType> INSTANCES = new LinkedHashMap<String, AssetSearchType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(ASSET_NAME.toString(), ASSET_NAME);
		INSTANCES.put(ASSET_ID.toString(), ASSET_ID);		
	}
	/**
	 * 
	 *
	 */
	public AssetSearchType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected AssetSearchType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns the EventType associated with the given persistentValue
	 * @param persistentValue
	 * @return
	 */
	public static AssetSearchType getAssetSearchType(String persistentValue)
	{
		for( Iterator<AssetSearchType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			AssetSearchType et = (AssetSearchType)i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
	
	public static LinkedList<AssetSearchType> getAssetSearchTypes()
	{
		LinkedList<AssetSearchType> l = new LinkedList<AssetSearchType>();
		for(Iterator<AssetSearchType> i = AssetSearchType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add( i.next() );
		}		
		return l;
	}	
}
