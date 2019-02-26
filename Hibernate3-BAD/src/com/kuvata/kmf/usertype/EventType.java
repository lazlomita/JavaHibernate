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
public class EventType extends PersistentStringEnum 
{	
	public static final EventType HEARTBEAT_STARTUP = new EventType("Heartbeat Startup", "heartbeat_startup");
	public static final EventType PRESENTER_STARTUP = new EventType("Presenter Startup", "presenter_startup");
	public static final EventType ANTIVIRUS_SCAN = new EventType("AntiVirus Scan", "antivirus_scan");
	public static final EventType DEVICE_SCRIPT_INSTALL = new EventType("Device Script Install", "device_script_install");
	public static final EventType DEVICE_RELEASE_INSTALL = new EventType("Device Release Install", "device_release_install");
	public static final EventType HTTPD_FAILURE = new EventType("Httpd Failure", "httpd_failure");
	public static final EventType CACHE_MANAGER_RESTART = new EventType("Cache Manager Restart", "Cache Manager Restart");
	public static final EventType NO_VIDEO = new EventType("No Video", "No Video");
	public static final EventType PRESENTER_TERMINATED = new EventType("Presenter Terminated", "presenter_terminated");
	public static final EventType DB_RECOVERED = new EventType("Database Recovered", "db_recovered");
	public static final EventType KUVATA_RECOVERED = new EventType("Kuvata Recovered", "kuvata_recovered");
	public static final Map<String, EventType> INSTANCES = new HashMap<String, EventType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(HEARTBEAT_STARTUP.toString(), HEARTBEAT_STARTUP);
		INSTANCES.put(PRESENTER_STARTUP.toString(), PRESENTER_STARTUP);
		INSTANCES.put(ANTIVIRUS_SCAN.toString(), ANTIVIRUS_SCAN);
		INSTANCES.put(DEVICE_SCRIPT_INSTALL.toString(), DEVICE_SCRIPT_INSTALL);
		INSTANCES.put(DEVICE_RELEASE_INSTALL.toString(), DEVICE_RELEASE_INSTALL);
		INSTANCES.put(HTTPD_FAILURE.toString(), HTTPD_FAILURE);
		INSTANCES.put(CACHE_MANAGER_RESTART.toString(), CACHE_MANAGER_RESTART);
		INSTANCES.put(NO_VIDEO.toString(), NO_VIDEO);
		INSTANCES.put(PRESENTER_TERMINATED.toString(), PRESENTER_TERMINATED);
		INSTANCES.put(DB_RECOVERED.toString(), DB_RECOVERED);
		INSTANCES.put(KUVATA_RECOVERED.toString(), KUVATA_RECOVERED);
	}
	/**
	 * 
	 *
	 */
	public EventType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected EventType(String name, String persistentValue) {
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
	 * Returns the EventType associated with the given persistentValue
	 * @param persistentValue
	 * @return
	 */
	public static EventType getEventType(String persistentValue)
	{
		for( Iterator<EventType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			EventType et = i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
}
