package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class SegmentEndAfterUnit extends PersistentStringEnum 
{	
	public static final SegmentEndAfterUnit MINUTES = new SegmentEndAfterUnit("minutes", "minutes");
	public static final SegmentEndAfterUnit HOURS = new SegmentEndAfterUnit("hours", "hours");	
	public static final SegmentEndAfterUnit SECONDS = new SegmentEndAfterUnit("seconds", "seconds");	
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(MINUTES.toString(), MINUTES);
		INSTANCES.put(HOURS.toString(), HOURS);
		INSTANCES.put(SECONDS.toString(), SECONDS);
	}
	/**
	 * 
	 *
	 */
	public SegmentEndAfterUnit() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SegmentEndAfterUnit(String name, String persistentValue) {
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
	public String getSegmentEndAfterUnitName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getSegmentEndAfterUnits()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext())
		{
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);
		
		return l;
	}
	/**
	 * 
	 * @param segmentEndAfterUnitName
	 * @return
	 */
	public static SegmentEndAfterUnit getSegmentEndAfterUnit(String segmentEndAfterUnitName)
	{
		return (SegmentEndAfterUnit) INSTANCES.get( segmentEndAfterUnitName );
	}	
}
