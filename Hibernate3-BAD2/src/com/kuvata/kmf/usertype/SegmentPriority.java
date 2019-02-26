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
public class SegmentPriority extends PersistentStringEnum 
{	
	public static final SegmentPriority HIGHEST = new SegmentPriority("Highest", "Highest");
	public static final SegmentPriority HIGH = new SegmentPriority("High", "High");
	public static final SegmentPriority NORMAL = new SegmentPriority("Normal", "Normal");
	public static final SegmentPriority LOW = new SegmentPriority("Low", "Low");
	public static final SegmentPriority LOWEST = new SegmentPriority("Lowest", "Lowest");	
	
	/**
	 * 
	 */
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(HIGHEST.toString(), HIGHEST);
	    INSTANCES.put(HIGH.toString(), HIGH);
		INSTANCES.put(NORMAL.toString(), NORMAL);
		INSTANCES.put(LOW.toString(), LOW);
		INSTANCES.put(LOWEST.toString(), LOWEST);
	}
	/**
	 * 
	 *
	 */
	public SegmentPriority() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SegmentPriority(String name, String persistentValue) {
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
	public String getSegmentPriorityName()
	{
		return this.name;
	}
	
	private int getScore()
	{
		if( this.name == "Highest" ) return 5;
		else if( this.name == "High" ) return 4;
		else if( this.name == "Normal" ) return 3;
		else if( this.name == "Low" ) return 2;
		else return 1;
	}
	
	/**
	 * 
	 * @param compareTo
	 * @return
	 */
	public boolean higherThan(SegmentPriority compareTo)
	{
		if( this.getScore() > compareTo.getScore() ) return true;
		else return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getSegmentPriorities()
	{
		List l = new LinkedList();
		l.add( SegmentPriority.HIGHEST );
		l.add( SegmentPriority.HIGH );
		l.add( SegmentPriority.NORMAL );
		l.add( SegmentPriority.LOW );
		l.add( SegmentPriority.LOWEST );
		return l;
	}
	/**
	 * 
	 * @param segmentPriorityName
	 * @return
	 */
	public static SegmentPriority getSegmentPriority(String segmentPriorityName)
	{
		return (SegmentPriority) INSTANCES.get( segmentPriorityName );
	}	
}
