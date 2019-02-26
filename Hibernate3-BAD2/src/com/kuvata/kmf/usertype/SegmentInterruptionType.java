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
public class SegmentInterruptionType extends PersistentStringEnum 
{	
	public static final SegmentInterruptionType INTERRUPTION_POINT = new SegmentInterruptionType("at point", "at point");
	public static final SegmentInterruptionType AIRING_POINT = new SegmentInterruptionType("airing point", "airing point");	
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(INTERRUPTION_POINT.toString(), INTERRUPTION_POINT);
		INSTANCES.put(AIRING_POINT.toString(), AIRING_POINT);		
	}
	/**
	 * 
	 *
	 */
	public SegmentInterruptionType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SegmentInterruptionType(String name, String persistentValue) {
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
	public String getSegmentInterruptionTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getSegmentInterruptionTypes()
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
	 * @param segmentInterruptionTypeName
	 * @return
	 */
	public static SegmentInterruptionType getSegmentInterruptionType(String segmentInterruptionTypeName)
	{
		return (SegmentInterruptionType) INSTANCES.get( segmentInterruptionTypeName );
	}	
}
