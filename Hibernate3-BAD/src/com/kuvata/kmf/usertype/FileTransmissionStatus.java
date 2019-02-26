/*
 * Created on Nov 15, 2004
 */
package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * 
 * @author anaber
 *
 */
public class FileTransmissionStatus extends PersistentStringEnum 
{	
	public static final FileTransmissionStatus CANCELLED = new FileTransmissionStatus("Cancelled","Cancelled", false, 4);
	public static final FileTransmissionStatus EXISTS = new FileTransmissionStatus("Exists","Exists", false, 4);	
	public static final FileTransmissionStatus IN_PROGRESS = new FileTransmissionStatus("In Progress","In Progress", true, 3);
	public static final FileTransmissionStatus NEEDED = new FileTransmissionStatus("Needed","Needed", true, 2);
	public static final FileTransmissionStatus NEEDED_FOR_FUTURE = new FileTransmissionStatus("Needed For Future","Needed For Future", true, 1);
	public static final Map<String, FileTransmissionStatus> INSTANCES = new HashMap<String, FileTransmissionStatus>();
	private int priority;
	private boolean transmissionNeeded;
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(EXISTS.toString(), EXISTS);
		INSTANCES.put(IN_PROGRESS.toString(), IN_PROGRESS);
		INSTANCES.put(NEEDED.toString(), NEEDED);
		INSTANCES.put(NEEDED_FOR_FUTURE.toString(), NEEDED_FOR_FUTURE);	
		INSTANCES.put(CANCELLED.toString(), CANCELLED);	
	}
	/**
	 * 
	 *
	 */
	public FileTransmissionStatus() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected FileTransmissionStatus(String name, String persistentValue, boolean transmissionNeeded, int priority) {
		super(name, persistentValue);
		this.priority = priority;
		this.transmissionNeeded = transmissionNeeded;
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
	 * @param contentScheduleEventTypeName
	 * @return
	 */
	public static FileTransmissionStatus getDevicePresentationStatus(String name)
	{
		return (FileTransmissionStatus) INSTANCES.get( name );
	}
	public int getPriority() {
		return priority;
	}
	public boolean isTransmissionNeeded() {
		return transmissionNeeded;
	}	
}
