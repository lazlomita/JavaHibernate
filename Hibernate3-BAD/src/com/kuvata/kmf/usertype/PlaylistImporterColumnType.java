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
public class PlaylistImporterColumnType extends PersistentStringEnum 
{
	public static final PlaylistImporterColumnType ASSET_ID = new PlaylistImporterColumnType("asset_id", "asset_id");
	public static final PlaylistImporterColumnType NAME = new PlaylistImporterColumnType("name", "name");
	public static final PlaylistImporterColumnType ASSET_TYPE = new PlaylistImporterColumnType("asset_type", "asset_type");
	public static final PlaylistImporterColumnType LENGTH = new PlaylistImporterColumnType("length", "length");
	public static final PlaylistImporterColumnType LAYOUT = new PlaylistImporterColumnType("layout", "layout");
	public static final PlaylistImporterColumnType DISPLAYAREA = new PlaylistImporterColumnType("displayarea", "displayarea");	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(ASSET_ID.toString(), ASSET_ID);
		INSTANCES.put(NAME.toString(), NAME);
		INSTANCES.put(ASSET_TYPE.toString(), ASSET_TYPE);
		INSTANCES.put(LENGTH.toString(), LENGTH);
		INSTANCES.put(LAYOUT.toString(), LAYOUT);
		INSTANCES.put(DISPLAYAREA.toString(), DISPLAYAREA);		
	}
	/**
	 * 
	 *
	 */
	public PlaylistImporterColumnType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PlaylistImporterColumnType(String name, String persistentValue) {
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
	public String getName()
	{
		return this.name;
	}
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static PlaylistImporterColumnType getPlaylistImporterColumnType(String name)
	{
		return (PlaylistImporterColumnType) INSTANCES.get( name );
	}	
}
