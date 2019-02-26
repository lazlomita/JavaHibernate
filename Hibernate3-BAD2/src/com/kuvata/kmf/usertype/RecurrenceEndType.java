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
public class RecurrenceEndType extends PersistentStringEnum 
{	
	public static final RecurrenceEndType END_AFTER = new RecurrenceEndType("End After", "End After");
	public static final RecurrenceEndType NO_END = new RecurrenceEndType("No End", "No End");
	public static final RecurrenceEndType ON_DATE = new RecurrenceEndType("On Date", "On Date");
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(END_AFTER.toString(), END_AFTER);
		INSTANCES.put(NO_END.toString(), NO_END);
		INSTANCES.put(ON_DATE.toString(), ON_DATE);
	}
	/**
	 * 
	 *
	 */
	public RecurrenceEndType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected RecurrenceEndType(String name, String persistentValue) {
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
	public String getRecurrenceEndTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getRecurrenceEndTypes()
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
	 * @param recurrenceEndTypeName
	 * @return
	 */
	public static RecurrenceEndType getRecurrenceEndType(String recurrenceEndTypeName)
	{
		return (RecurrenceEndType) INSTANCES.get( recurrenceEndTypeName );
	}	
}
