package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class DirtyType extends PersistentStringEnum 
{	
	public static final DirtyType CONTENT_SCHEDULER_COULD_NOT_RUN = new DirtyType("ContentSchedulerCouldNotRun", "ContentSchedulerCouldNotRun");
	public static final DirtyType CONTENT_SCHEDULER_AUTO = new DirtyType("ContentSchedulerAuto", "ContentSchedulerAuto");
	public static final DirtyType CONTENT_SCHEDULER = new DirtyType("ContentScheduler", "ContentScheduler");
	public static final DirtyType ASSET_LENGTH_CHANGED = new DirtyType("Asset: length changed", "Asset: length changed");
	public static final DirtyType ASSET_LENGTH_UNCHANGED = new DirtyType("Asset: length unchanged", "Asset: length unchanged");
	public static final DirtyType ASSET_EXPIRATION_CHANGED = new DirtyType("Asset: expiration changed", "Asset: expiration changed");
	public static final DirtyType PLAYLIST = new DirtyType("Playlist", "Playlist");
	public static final DirtyType CONTENT_ROTATION = new DirtyType("Content Rotation", "Content Rotation");
	public static final DirtyType SEGMENT = new DirtyType("Segment", "Segment");
	public static final DirtyType DEVICE = new DirtyType("Device", "Device");
	public static final DirtyType ASSET_INGESTER = new DirtyType("AssetIngester", "Asset Ingester");
	public static final DirtyType DEVICE_INGESTER = new DirtyType("DeviceIngester", "Device Ingester");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(CONTENT_SCHEDULER_COULD_NOT_RUN.toString(), CONTENT_SCHEDULER_COULD_NOT_RUN);
		INSTANCES.put(CONTENT_SCHEDULER_AUTO.toString(), CONTENT_SCHEDULER_AUTO);
		INSTANCES.put(CONTENT_SCHEDULER.toString(), CONTENT_SCHEDULER);
		INSTANCES.put(ASSET_LENGTH_CHANGED.toString(), ASSET_LENGTH_CHANGED);
		INSTANCES.put(ASSET_LENGTH_UNCHANGED.toString(), ASSET_LENGTH_UNCHANGED);
		INSTANCES.put(ASSET_EXPIRATION_CHANGED.toString(), ASSET_EXPIRATION_CHANGED);
		INSTANCES.put(PLAYLIST.toString(), PLAYLIST);
		INSTANCES.put(CONTENT_ROTATION.toString(), CONTENT_ROTATION);		
		INSTANCES.put(SEGMENT.toString(), SEGMENT);
		INSTANCES.put(DEVICE.toString(), DEVICE);
	}
	/**
	 * 
	 *
	 */
	public DirtyType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DirtyType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public String getAssetTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static DirtyType getDirtyType(String dirtyTypeName)
	{
		return (DirtyType) INSTANCES.get( dirtyTypeName );
	}		
}
