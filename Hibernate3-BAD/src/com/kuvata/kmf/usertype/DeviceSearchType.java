package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class DeviceSearchType extends PersistentStringEnum 
{	
	// Primary search types
	public static final DeviceSearchType DEVICE_NAME = new DeviceSearchType("Device Name", "device_name");
	public static final DeviceSearchType MAC_ADDRESS = new DeviceSearchType("MAC Address", "mac_address");
	public static final DeviceSearchType DEVICE_ID = new DeviceSearchType("Device ID", "device_id");
	public static final DeviceSearchType IP_ADDRESS = new DeviceSearchType("IP Address", "ip_address");
	public static final DeviceSearchType VPN_IP_ADDRESS = new DeviceSearchType("VPN IP Address", "vpn_ip_address");
	
	// Secondary search types. We will not include the following in the instances collection.
	public static final DeviceSearchType VERSION = new DeviceSearchType("Version", "version");
	public static final DeviceSearchType LICENSE_STATUS = new DeviceSearchType("License Status", "license_status");
	
	public static final LinkedHashMap<String, DeviceSearchType> INSTANCES = new LinkedHashMap<String, DeviceSearchType>();
	
	/**
	 * 
	 */	    
	static{
		INSTANCES.put(DEVICE_ID.toString(), DEVICE_ID);
		INSTANCES.put(DEVICE_NAME.toString(), DEVICE_NAME);
		INSTANCES.put(IP_ADDRESS.toString(), IP_ADDRESS);
		INSTANCES.put(MAC_ADDRESS.toString(), MAC_ADDRESS);		
		INSTANCES.put(VPN_IP_ADDRESS.toString(), VPN_IP_ADDRESS);
	}
	/**
	 * 
	 *
	 */
	public DeviceSearchType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DeviceSearchType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns the EventType associated with the given persistentValue
	 * @param persistentValue
	 * @return
	 */
	public static DeviceSearchType getDeviceSearchType(String persistentValue)
	{
		for( Iterator<DeviceSearchType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			DeviceSearchType et = (DeviceSearchType)i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
	
	public static LinkedList<DeviceSearchType> getDeviceSearchTypes()
	{
		LinkedList<DeviceSearchType> l = new LinkedList<DeviceSearchType>();
		for(Iterator<DeviceSearchType> i = DeviceSearchType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add( i.next() );
		}		
		return l;
	}	
}
