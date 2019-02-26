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
public class InterruptionToleranceUnit extends PersistentStringEnum 
{	
	public static final InterruptionToleranceUnit SECONDS = new InterruptionToleranceUnit("seconds", "seconds");
	public static final InterruptionToleranceUnit MINUTES = new InterruptionToleranceUnit("minutes", "minutes");
	public static final InterruptionToleranceUnit HOURS = new InterruptionToleranceUnit("hours", "hours");	
	public static final Map INSTANCES = new HashMap();	
	
	static{
		INSTANCES.put(SECONDS.toString(), SECONDS);
		INSTANCES.put(MINUTES.toString(), MINUTES);
		INSTANCES.put(HOURS.toString(), HOURS);				
	}
	
	public InterruptionToleranceUnit() {}
	
	protected InterruptionToleranceUnit(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString(){
		return this.name;
	}
	
	public String getInterruptionToleranceUnitName(){
		return this.name;
	}
	
	public String getValue(){
		return this.getPersistentValue();
	}
	
	public static List getInterruptionToleranceUnits()
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
	public static InterruptionToleranceUnit getInterruptionToleranceUnit(String interruptionToleranceUnitName)
	{
		return (InterruptionToleranceUnit) INSTANCES.get( interruptionToleranceUnitName );
	}	
}
