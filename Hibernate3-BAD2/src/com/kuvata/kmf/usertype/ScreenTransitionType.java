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
public class ScreenTransitionType extends PersistentStringEnum 
{	
	public static final ScreenTransitionType OFF = new ScreenTransitionType("Off","Off");	
	public static final ScreenTransitionType BETWEEN_ASSETS = new ScreenTransitionType("Between Assets","Between Assets");
	public static final ScreenTransitionType BETWEEN_LAYOUTS = new ScreenTransitionType("Between Layouts","Between Layouts");
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(OFF.toString(), OFF);
		INSTANCES.put(BETWEEN_ASSETS.toString(), BETWEEN_ASSETS);
		INSTANCES.put(BETWEEN_LAYOUTS.toString(), BETWEEN_LAYOUTS);
	}
	/**
	 * 
	 *
	 */
	public ScreenTransitionType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected ScreenTransitionType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	
	public String getName()
	{
		return this.name;
	}	

	/**
	 * 
	 * @return
	 */
	public static List getScreenTransitionTypes()
	{
		List l = new LinkedList();		
		for( Iterator i = INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param contentSchedulerStatusTypeName
	 * @return
	 */
	public static ScreenTransitionType getScreenTransitionType(String name)
	{
		return (ScreenTransitionType) INSTANCES.get( name  );
	}
}
