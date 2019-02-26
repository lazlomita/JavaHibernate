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
public class FileTransmissionPriorityType extends PersistentStringEnum 
{	
	public static final FileTransmissionPriorityType HIGH = new FileTransmissionPriorityType("High","2");	
	public static final FileTransmissionPriorityType NORMAL = new FileTransmissionPriorityType("Normal","1");
	public static final FileTransmissionPriorityType LOW = new FileTransmissionPriorityType("Low","0");	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(HIGH.toString(), HIGH);
		INSTANCES.put(NORMAL.toString(), NORMAL);
		INSTANCES.put(LOW.toString(), LOW);	
	}
	/**
	 * 
	 *
	 */
	public FileTransmissionPriorityType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected FileTransmissionPriorityType(String name, String persistentValue) {
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
	 * @param contentScheduleEventTypeName
	 * @return
	 */
	public static FileTransmissionPriorityType getFileTransmissionPriorityType(String name)
	{
		return (FileTransmissionPriorityType) INSTANCES.get( name );
	}	
}
