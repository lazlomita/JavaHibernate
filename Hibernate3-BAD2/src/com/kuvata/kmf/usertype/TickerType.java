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
public class TickerType extends PersistentStringEnum 
{	
	public static final TickerType TEXT = new TickerType("Text", "text");
	public static final TickerType RSS = new TickerType("RSS", "rss");		
	public static final Map INSTANCES = new HashMap();	
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(TEXT.toString(), TEXT);
		INSTANCES.put(RSS.toString(), RSS);
	}
	/**
	 * 
	 *
	 */
	public TickerType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected TickerType(String name, String persistentValue) {
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
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getTickerTypes()
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
	 * @param name
	 * @return
	 */
	public static TickerType getTickerType(String name)
	{
		return (TickerType) INSTANCES.get( name );
	}	
}
