/*
 * Created on Nov 15, 2004
 */
package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * 
 *
 */
public class FileTransmissionType extends PersistentStringEnum 
{	
	public static final FileTransmissionType PRESENTATION = new FileTransmissionType("Presentation","Presentation");	
	public static final FileTransmissionType CONTENT_SCHEDULE = new FileTransmissionType("Content Schedule","Content Schedule");
	public static final FileTransmissionType DEVICE_RELEASE = new FileTransmissionType("Device Release","Device Release");
	public static final FileTransmissionType DEVICE_SCRIPT = new FileTransmissionType("Device Script","Device Script");
	public static final FileTransmissionType FEED = new FileTransmissionType("Feed","Feed");
	public static final FileTransmissionType OS_IMAGE_PART = new FileTransmissionType("OS Image Part","OS Image Part");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(PRESENTATION.toString(), PRESENTATION);
		INSTANCES.put(CONTENT_SCHEDULE.toString(), CONTENT_SCHEDULE);
		INSTANCES.put(DEVICE_RELEASE.toString(), DEVICE_RELEASE);
		INSTANCES.put(FEED.toString(), FEED);
		INSTANCES.put(DEVICE_SCRIPT.toString(), DEVICE_SCRIPT);
		INSTANCES.put(OS_IMAGE_PART.toString(), OS_IMAGE_PART);
	}
	/**
	 * 
	 *
	 */
	public FileTransmissionType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected FileTransmissionType(String name, String persistentValue) {
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
	public static FileTransmissionType getFileTransmissionType(String name)
	{
		return (FileTransmissionType) INSTANCES.get( name );
	}	
}
