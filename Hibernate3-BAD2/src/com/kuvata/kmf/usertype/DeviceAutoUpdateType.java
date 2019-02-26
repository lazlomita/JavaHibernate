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
public class DeviceAutoUpdateType extends PersistentStringEnum 
{	
	public static final DeviceAutoUpdateType OFF = new DeviceAutoUpdateType("Off", "Off");
	public static final DeviceAutoUpdateType ON = new DeviceAutoUpdateType("On", "On");
	public static final DeviceAutoUpdateType ON_PLUS_BETA = new DeviceAutoUpdateType("On + Beta", "On Plus Beta");
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(OFF.toString(), OFF);
		INSTANCES.put(ON.toString(), ON);
		INSTANCES.put(ON_PLUS_BETA.toString(), ON_PLUS_BETA);
	}
	/**
	 * 
	 *
	 */
	public DeviceAutoUpdateType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DeviceAutoUpdateType(String name, String persistentValue) {
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
	 * @param persistentValue
	 * @return
	 */
	public static DeviceAutoUpdateType getDeviceAutoUpdateTypeByPersistentValue(String persistentValue)
	{				
		for( Iterator i = DeviceAutoUpdateType.INSTANCES.values().iterator(); i.hasNext(); )
		{
			DeviceAutoUpdateType dat = (DeviceAutoUpdateType)i.next();
			if( dat.getPersistentValue().equalsIgnoreCase( persistentValue) ) {
				return dat;
			}
		}
		return null;
	}	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static DeviceAutoUpdateType getDeviceAutoUpdateType(String name){
		return (DeviceAutoUpdateType) INSTANCES.get( name );
	}	
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List getDeviceAutoUpdateTypes()
	{
		List l = new LinkedList();
		for( Iterator i=DeviceAutoUpdateType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}		
}
