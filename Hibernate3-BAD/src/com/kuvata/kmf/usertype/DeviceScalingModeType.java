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
public class DeviceScalingModeType extends PersistentStringEnum 
{	
	public static final DeviceScalingModeType STRETCH = new DeviceScalingModeType("Stretch", "Stretch");
	public static final DeviceScalingModeType LETTER_BOX_PILLAR_BOX = new DeviceScalingModeType("Letterbox/Pillarbox", "Letterbox/Pillarbox");
	public static final DeviceScalingModeType NONE = new DeviceScalingModeType("None", "None");
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(STRETCH.toString(), STRETCH);
		INSTANCES.put(LETTER_BOX_PILLAR_BOX.toString(), LETTER_BOX_PILLAR_BOX);
		INSTANCES.put(NONE.toString(), NONE);
	}
	/**
	 * 
	 *
	 */
	public DeviceScalingModeType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DeviceScalingModeType(String name, String persistentValue) {
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
	
	public static List getDeviceScalingModeTypes()
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
	 * @param persistentValue
	 * @return
	 */
	public static DeviceScalingModeType getDeviceScalingModeTypeByPersistentValue(String persistentValue)
	{				
		for( Iterator i = DeviceScalingModeType.INSTANCES.values().iterator(); i.hasNext(); ){
			DeviceScalingModeType dat = (DeviceScalingModeType)i.next();
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
	public static DeviceScalingModeType getDeviceScalingModeType(String name)
	{
		return (DeviceScalingModeType) INSTANCES.get( name );
	}	
}
