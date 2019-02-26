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
public class SegmentSearchType extends PersistentStringEnum 
{	
	public static final SegmentSearchType SEGMENT_NAME = new SegmentSearchType("Schedule Name", "segment_name");	
	public static final SegmentSearchType SEGMENT_ID = new SegmentSearchType("Schedule ID", "segment_id");	
	public static final LinkedHashMap<String, SegmentSearchType> INSTANCES = new LinkedHashMap<String, SegmentSearchType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(SEGMENT_NAME.toString(), SEGMENT_NAME);
		INSTANCES.put(SEGMENT_ID.toString(), SEGMENT_ID);		
	}
	/**
	 * 
	 *
	 */
	public SegmentSearchType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SegmentSearchType(String name, String persistentValue) {
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
	public static SegmentSearchType getSearchType(String persistentValue)
	{
		for( Iterator<SegmentSearchType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			SegmentSearchType et = (SegmentSearchType)i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
	
	public static LinkedList<SegmentSearchType> getSearchTypes()
	{
		LinkedList<SegmentSearchType> l = new LinkedList<SegmentSearchType>();
		for(Iterator<SegmentSearchType> i = SegmentSearchType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add( i.next() );
		}		
		return l;
	}	
}
