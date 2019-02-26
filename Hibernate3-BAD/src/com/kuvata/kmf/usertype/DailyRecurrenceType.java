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
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class DailyRecurrenceType extends PersistentStringEnum 
{	
	public static final DailyRecurrenceType HOURLY = new DailyRecurrenceType("Hourly", "Hourly");
	public static final DailyRecurrenceType WEEKDAY = new DailyRecurrenceType("Weekday", "Weekday");
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(HOURLY.toString(), HOURLY);
		INSTANCES.put(WEEKDAY.toString(), WEEKDAY);		
	}
	/**
	 * 
	 *
	 */
	public DailyRecurrenceType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DailyRecurrenceType(String name, String persistentValue) {
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
	public String getDailyRecurrenceTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getDailyRecurrenceTypes()
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
	 * @param dailyRecurrenceTypeName
	 * @return
	 */
	public static DailyRecurrenceType getDailyRecurrenceType(String dailyRecurrenceTypeName)
	{
		return (DailyRecurrenceType) INSTANCES.get( dailyRecurrenceTypeName );
	}	
}
