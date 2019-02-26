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
public class LiveVideoChannelType extends PersistentStringEnum 
{	
	public static final LiveVideoChannelType BROADCAST = new LiveVideoChannelType("Broadcast", "Broadcast");
	public static final LiveVideoChannelType CABLE = new LiveVideoChannelType("Cable", "Cable");		
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(BROADCAST.toString(), BROADCAST);
		INSTANCES.put(CABLE.toString(), CABLE);
	}
	/**
	 * 
	 *
	 */
	public LiveVideoChannelType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected LiveVideoChannelType(String name, String persistentValue) {
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
	public static List getLiveVideoChannelTypes()
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
	public static LiveVideoChannelType getLiveVideoChannelType(String name)
	{
		return (LiveVideoChannelType) INSTANCES.get( name );
	}	
}
