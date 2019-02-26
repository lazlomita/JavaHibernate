package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class DeviceCommandType extends PersistentStringEnum 
{	
	public static final DeviceCommandType GET_CONTENT_SCHEDULE = new DeviceCommandType("getContentSchedule", "getContentSchedule", true);
	public static final DeviceCommandType GET_PRESENTATION = new DeviceCommandType("getPresentation", "getPresentation", true);	
	public static final DeviceCommandType MCM_COMMAND = new DeviceCommandType("mcmCommand", "mcmCommand", false);
	public static final DeviceCommandType PROPERTY_CHANGE_COMMAND = new DeviceCommandType("propertyChange", "propertyChange", false);
	public static final DeviceCommandType GET_FILE = new DeviceCommandType("getFile", "getFile", true);
	public static final DeviceCommandType CREATE_FILE_EXISTS = new DeviceCommandType("createFileExists", "createFileExists", false);
	public static final DeviceCommandType RUN_SELF_TESTS = new DeviceCommandType("runSelfTests", "runSelfTests", false);
	public static final DeviceCommandType UPLOAD_SCREENSHOT = new DeviceCommandType("uploadScreenshot", "uploadScreenshot", false);
	public static final DeviceCommandType UPLOAD_DEVICE_PROPERTIES = new DeviceCommandType("uploadDeviceProperties", "uploadDeviceProperties", false);
	public static final DeviceCommandType INSTALL_DEVICE_RELEASE = new DeviceCommandType("installDeviceRelease", "installDeviceRelease", false);
	public static final DeviceCommandType INSTALL_SCRIPT = new DeviceCommandType("installScript", "installScript", false);
	public static final DeviceCommandType BACKUP_NETWORK_PROPERTIES = new DeviceCommandType("backupNetworkProperties", "backupNetworkProperties", false);
	
	public static final Map<String, DeviceCommandType> INSTANCES = new HashMap<String, DeviceCommandType>();
	private boolean isFileTransferCommand;
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(GET_CONTENT_SCHEDULE.toString(), GET_CONTENT_SCHEDULE);
		INSTANCES.put(GET_PRESENTATION.toString(), GET_PRESENTATION);
		INSTANCES.put(MCM_COMMAND.toString(), MCM_COMMAND);		
		INSTANCES.put(PROPERTY_CHANGE_COMMAND.toString(), PROPERTY_CHANGE_COMMAND);		
		INSTANCES.put(GET_FILE.toString(), GET_FILE);
		INSTANCES.put(CREATE_FILE_EXISTS.toString(), CREATE_FILE_EXISTS);
		INSTANCES.put(RUN_SELF_TESTS.toString(), RUN_SELF_TESTS);	
		INSTANCES.put(UPLOAD_SCREENSHOT.toString(), UPLOAD_SCREENSHOT);
		INSTANCES.put(UPLOAD_DEVICE_PROPERTIES.toString(), UPLOAD_DEVICE_PROPERTIES);
		INSTANCES.put(INSTALL_DEVICE_RELEASE.toString(), INSTALL_DEVICE_RELEASE);
		INSTANCES.put(INSTALL_SCRIPT.toString(), INSTALL_SCRIPT);
		INSTANCES.put(BACKUP_NETWORK_PROPERTIES.toString(), BACKUP_NETWORK_PROPERTIES);
	}
	/**
	 * 
	 *
	 */
	public DeviceCommandType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public DeviceCommandType(String name, String persistentValue, boolean isFileTransferCommand) {
		super(name, persistentValue);
		this.isFileTransferCommand = isFileTransferCommand;
	}
	
	public String toString(){
		return this.getPersistentValue();
	}
	
	public String getValue(){
		return this.getPersistentValue();
	}
	
	// Used for cross-compilation
	public boolean equals(String s){
		return this.getPersistentValue().equals(s);
	}
	
	/**
	 * 
	 * @param deviceCommandName
	 * @return
	 */
	public static DeviceCommandType getDeviceCommandType(String deviceCommandName)
	{
		return (DeviceCommandType) INSTANCES.get( deviceCommandName );
	}
	/**
	 * @return Returns the isFileTransferCommand.
	 */
	public boolean isFileTransferCommand() {
		return isFileTransferCommand;
	}
	
	/**
	 * @param isFileTransferCommand The isFileTransferCommand to set.
	 */
	public void setFileTransferCommand(boolean isFileTransferCommand) {
		this.isFileTransferCommand = isFileTransferCommand;
	}
		
	
}
