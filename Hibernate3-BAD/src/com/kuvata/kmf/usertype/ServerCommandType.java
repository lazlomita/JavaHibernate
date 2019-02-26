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
public class ServerCommandType extends PersistentStringEnum 
{	
	public static final ServerCommandType UPDATE_DEVICE_COMMAND_STATUS = new ServerCommandType("updateDeviceCommandStatus", "updateDeviceCommandStatus");
	public static final ServerCommandType LOG_FILE_UPLOAD_COMPLETE = new ServerCommandType("logFileUploadComplete", "logFileUploadComplete");
	public static final ServerCommandType LOG_EVENT = new ServerCommandType("logEvent", "logEvent");
	public static final ServerCommandType UPDATE_FILE_TRANSMISSION = new ServerCommandType("updateFileTransmission", "updateFileTransmission");
	public static final ServerCommandType UPDATE_DEVICE_PROPERTIES = new ServerCommandType("updateDeviceProperties", "updateDeviceProperties");
	public static final ServerCommandType CREATE_MCM_HISTORY_ENTRY = new ServerCommandType("createMcmHistoryEntry", "createMcmHistoryEntry");
	public static final ServerCommandType CANCEL_CS = new ServerCommandType("cancelCS", "cancelCS");
	
	public static final Map<String, ServerCommandType> INSTANCES = new HashMap<String, ServerCommandType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(UPDATE_DEVICE_COMMAND_STATUS.toString(), UPDATE_DEVICE_COMMAND_STATUS);
		INSTANCES.put(LOG_FILE_UPLOAD_COMPLETE.toString(), LOG_FILE_UPLOAD_COMPLETE);
		INSTANCES.put(LOG_EVENT.toString(), LOG_EVENT);
		INSTANCES.put(UPDATE_FILE_TRANSMISSION.toString(), UPDATE_FILE_TRANSMISSION);
		INSTANCES.put(UPDATE_DEVICE_PROPERTIES.toString(), UPDATE_DEVICE_PROPERTIES);
		INSTANCES.put(CREATE_MCM_HISTORY_ENTRY.toString(), CREATE_MCM_HISTORY_ENTRY);
		INSTANCES.put(CANCEL_CS.toString(), CANCEL_CS);
	}
	/**
	 * 
	 *
	 */
	public ServerCommandType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public ServerCommandType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	
	// Used for cross-compilation
	public boolean equals(String s){
		return this.getPersistentValue().equals(s);
	}
	
	/**
	 * 
	 * @param serverCommandName
	 * @return
	 */
	public static ServerCommandType getServerCommandType(String serverCommandName)
	{
		return (ServerCommandType) INSTANCES.get( serverCommandName );
	}	
}
