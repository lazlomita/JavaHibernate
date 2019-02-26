package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Apr 7, 2016
 * 
 * @author Wilfredo Vargas
 */
public class SegmentColumnType extends PersistentStringEnum 
{
	public static final SegmentColumnType SEGMENT_ID = new SegmentColumnType("segment_id", "segment_id");
	public static final SegmentColumnType NAME = new SegmentColumnType("name", "name");
	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(SEGMENT_ID.toString(), SEGMENT_ID);
		INSTANCES.put(NAME.toString(), NAME);
	}
	/**
	 * 
	 *
	 */
	public SegmentColumnType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SegmentColumnType(String name, String persistentValue) {
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
	public static SegmentColumnType getPlaylistImporterColumnType(String name)
	{
		return (SegmentColumnType) INSTANCES.get( name );
	}	
}
