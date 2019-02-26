package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class PeripheralType extends PersistentStringEnum 
{	
	public static final PeripheralType DISPLAY = new PeripheralType("Display", "Display");
	public static final PeripheralType AUDIO = new PeripheralType("Audio", "Audio");
	public static final PeripheralType AV_SWITCH = new PeripheralType("A/V Switch", "A/V Switch");
	public static final PeripheralType POWER_SWITCH = new PeripheralType("Power Switch", "Power Switch");
	public static final PeripheralType VIDEO_SWITCH = new PeripheralType("Video Switch", "Video Switch");
	public static final PeripheralType NON_MONITORED_DISPLAY = new PeripheralType("Non-Monitored Display", "Non-Monitored Display");
	public static final Map<String, PeripheralType> INSTANCES = new HashMap<String, PeripheralType>();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(DISPLAY.toString(), DISPLAY);
		INSTANCES.put(AUDIO.toString(), AUDIO);
		INSTANCES.put(AV_SWITCH.toString(), AV_SWITCH);
		INSTANCES.put(POWER_SWITCH.toString(), POWER_SWITCH);
		INSTANCES.put(VIDEO_SWITCH.toString(), VIDEO_SWITCH);
		INSTANCES.put(NON_MONITORED_DISPLAY.toString(), NON_MONITORED_DISPLAY);
	}
	/**
	 * 
	 *
	 */
	public PeripheralType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PeripheralType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public static PeripheralType getPeripheralType(String persistentValue) {
		for( Iterator<PeripheralType> i = PeripheralType.INSTANCES.values().iterator(); i.hasNext(); ){
			PeripheralType type = i.next();
			if( type.getPersistentValue().equalsIgnoreCase( persistentValue ) ) {
				return type;
			}
		}
		return null;
	}	
	
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	public String getName(){
		return this.name;
	}	
}
