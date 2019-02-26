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
public class LiveVideoType extends PersistentStringEnum 
{	
	public static final LiveVideoType STREAMING_URL = new LiveVideoType("Streaming URL", "Streaming URL");
	public static final LiveVideoType HDMIIN = new LiveVideoType("HDMI-IN", "HDMI-IN");
	public static final Map INSTANCES = new HashMap();	
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(STREAMING_URL.toString(), STREAMING_URL);
		INSTANCES.put(HDMIIN.toString(), HDMIIN);
	}
	/**
	 * 
	 *
	 */
	public LiveVideoType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected LiveVideoType(String name, String persistentValue) {
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
	public static LiveVideoType getLiveVideoType(String name)
	{
		return (LiveVideoType) INSTANCES.get( name );
	}	
}
