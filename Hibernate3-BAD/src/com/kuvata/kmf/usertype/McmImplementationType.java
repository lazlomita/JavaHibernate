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
public class McmImplementationType extends PersistentStringEnum 
{	
	public static final McmImplementationType LG = new McmImplementationType("LG", "LG");
	public static final McmImplementationType SAMSUNG = new McmImplementationType("Samsung", "Samsung");
	public static final McmImplementationType NEC = new McmImplementationType("NEC", "NEC");
	public static final McmImplementationType SHARP = new McmImplementationType("Sharp", "Sharp");
	public static final McmImplementationType SONY = new McmImplementationType("Sony", "Sony");
	public static final McmImplementationType PANASONIC = new McmImplementationType("Panasonic", "Panasonic");
	public static final McmImplementationType PHILIPS = new McmImplementationType("Philips", "Philips");
	public static final McmImplementationType PLANAR = new McmImplementationType("Planar", "Planar");
	public static final McmImplementationType CUSTOM1 = new McmImplementationType("Custom 1", "Custom 1");
	public static final McmImplementationType GENERIC_MONITOR_ASCII = new McmImplementationType("Generic Monitor (ASCII)", "Generic Monitor (ASCII)");
	public static final McmImplementationType BROWN_INNOVATIONS = new McmImplementationType("Brown Innovations", "BrownInnovations");
	public static final McmImplementationType AV_SWITCH = new McmImplementationType("A/V Switch", "A/V Switch");
	public static final McmImplementationType POWER_SWITCH = new McmImplementationType("Power Switch", "Power Switch");
	public static final McmImplementationType VIDEO_SWITCH = new McmImplementationType("Video Switch", "Video Switch");
	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(LG.toString(), LG);
		INSTANCES.put(SAMSUNG.toString(), SAMSUNG);
		INSTANCES.put(NEC.toString(), NEC);
		INSTANCES.put(SHARP.toString(), SHARP);
		INSTANCES.put(SONY.toString(), SONY);
		INSTANCES.put(PANASONIC.toString(), PANASONIC);
		INSTANCES.put(PHILIPS.toString(), PHILIPS);
		INSTANCES.put(PLANAR.toString(), PLANAR);
		INSTANCES.put(CUSTOM1.toString(), CUSTOM1);
		INSTANCES.put(GENERIC_MONITOR_ASCII.toString(), GENERIC_MONITOR_ASCII);
		INSTANCES.put(BROWN_INNOVATIONS.toString(), BROWN_INNOVATIONS);
		INSTANCES.put(AV_SWITCH.toString(), AV_SWITCH);
		INSTANCES.put(POWER_SWITCH.toString(), POWER_SWITCH);
		INSTANCES.put(VIDEO_SWITCH.toString(), VIDEO_SWITCH);
	}
	/**
	 * 
	 *
	 */
	public McmImplementationType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected McmImplementationType(String name, String persistentValue) {
		super(name, persistentValue);
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
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List getMcmImplementationTypes()
	{
		List l = new LinkedList();
		for( Iterator i=McmImplementationType.INSTANCES.values().iterator(); i.hasNext(); ) {			
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
	public static McmImplementationType getMcmImplementationTypeByName(String name)
	{
		return (McmImplementationType) INSTANCES.get( name );
	}	
	
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static McmImplementationType getMcmImplementationType(String persistentValue) {
		for( Iterator i = McmImplementationType.INSTANCES.values().iterator(); i.hasNext(); )
		{
			McmImplementationType type = (McmImplementationType)i.next();
			if( type.getPersistentValue().equalsIgnoreCase( persistentValue ) ) {
				return type;
			}
		}
		return null;
	}	
}
