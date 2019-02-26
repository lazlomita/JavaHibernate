package com.kuvata.kmf;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import com.kuvata.kmf.usertype.DeviceCommandType;
import com.kuvata.kmf.util.Reformat;


/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class DeviceScript extends Entity 
{
	private static Logger logger = Logger.getLogger(DeviceScript.class);
	private Long deviceScriptId;
	private Device device;	
	private String script;	
	
	/**
	 * 
	 *
	 */
	public DeviceScript()
	{		
	}
	
	/**
	 * 
	 * @param deviceScriptId
	 * @return
	 * @throws HibernateException
	 */
	public static DeviceScript getDeviceScript(Long deviceScriptId) throws HibernateException
	{
		return (DeviceScript)Entity.load(DeviceScript.class, deviceScriptId);		
	}
	
	/**
	 * Creates a new DeviceScript object
	 * @param device
	 * @param script
	 */
	public static void create(Device device, String script)
	{
		DeviceScript deviceScript = new DeviceScript();							
		deviceScript.setDevice( device );
		deviceScript.setScript( script );		
		deviceScript.save();			
	}
	
	/**
	 * 1. Creates a new DeviceScript object
	 * 2. Adds a getFile device command for the given script
	 * 
	 * @param device
	 * @param scriptPath
	 * @throws InterruptedException
	 */
	public static void sendScript(Device device, String scriptPath) throws InterruptedException
	{
		// TODO: Should we check to see if a DeviceScript object already exists?		
		// Create a new DeviceScript object
		DeviceScript.create( device, scriptPath );
		
		// Add a getFile device command for the given script
		device.addDeviceCommand( DeviceCommandType.GET_FILE, scriptPath, false );
	}
	
	/**
	 * Returns a collection of DeviceScript objects associated with the given device and scriptPath.
	 * 
	 * @param device
	 * @param scriptPath
	 * @return
	 */
	public static List<DeviceScript> getDeviceScripts(Device device, String scriptPath)
	{
		Session session = HibernateSession.currentSession();		
		return session.createQuery("from DeviceScript ds where ds.device.deviceId=? and ds.script=?")
					.setParameter(0, device.getDeviceId())
					.setParameter(1, scriptPath)					
					.list();  		
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getDeviceScriptId();
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof DeviceScript) ) result = false;
		
		DeviceScript de = (DeviceScript) other;		
		if(this.hashCode() == de.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "DeviceScript".hashCode();		
		result = Reformat.getSafeHash( this.getDevice().getDeviceId(), result, 13 );
		result = Reformat.getSafeHash( this.getScript(), result, 13 );		
		return result;
	}

	/**
	 * @return the deviceScriptId
	 */
	public Long getDeviceScriptId() {
		return deviceScriptId;
	}

	/**
	 * @param deviceScriptId the deviceScriptId to set
	 */
	public void setDeviceScriptId(Long deviceScriptId) {
		this.deviceScriptId = deviceScriptId;
	}

	/**
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}	
}
