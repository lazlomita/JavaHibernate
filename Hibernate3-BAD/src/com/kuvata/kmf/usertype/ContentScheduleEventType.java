/*
 * Created on Nov 15, 2004
 */
package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * 
 * @author anaber
 *
 */
public class ContentScheduleEventType extends PersistentStringEnum 
{	
	public static final ContentScheduleEventType SEGMENT_BLOCK = new ContentScheduleEventType("SegmentBlock","SegmentBlock");
	public static final ContentScheduleEventType PLAYLIST = new ContentScheduleEventType("Playlist","Playlist");
	public static final ContentScheduleEventType ASSET = new ContentScheduleEventType("Asset","Asset");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(SEGMENT_BLOCK.toString(), SEGMENT_BLOCK);
		INSTANCES.put(PLAYLIST.toString(), PLAYLIST);
		INSTANCES.put(ASSET.toString(), ASSET);
	}
	/**
	 * 
	 *
	 */
	public ContentScheduleEventType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected ContentScheduleEventType(String name, String persistentValue) {
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
	public String getContentScheduleEventTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getContentScheduleEventTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param contentScheduleEventTypeName
	 * @return
	 */
	public static ContentScheduleEventType getAssetType(String contentScheduleEventTypeName)
	{
		return (ContentScheduleEventType) INSTANCES.get( contentScheduleEventTypeName );
	}	
}
