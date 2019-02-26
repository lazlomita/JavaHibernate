package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


public class RecurrenceType extends PersistentStringEnum 
{	
	public static final RecurrenceType MONTHLY = new RecurrenceType("Monthly", "Monthly");
	public static final RecurrenceType WEEKLY = new RecurrenceType("Weekly", "Weekly");
	public static final RecurrenceType DAILY = new RecurrenceType("Daily", "Daily");
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(MONTHLY.toString(), MONTHLY);
		INSTANCES.put(WEEKLY.toString(), WEEKLY);
		INSTANCES.put(DAILY.toString(), DAILY);	
	}
	/**
	 * 
	 *
	 */
	public RecurrenceType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected RecurrenceType(String name, String persistentValue) {
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
	public String getRecurrenceTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getRecurrenceTypes()
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
	 * @param recurrenceTypeName
	 * @return
	 */
	public static RecurrenceType getRecurrenceType(String recurrenceTypeName)
	{
		return (RecurrenceType) INSTANCES.get( recurrenceTypeName );
	}	
}
