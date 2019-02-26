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
public class FileStatusType extends PersistentStringEnum 
{	
	public static final FileStatusType PRESENTATION = new FileStatusType("Presentation", "Presentation");
	public static final FileStatusType CONTENT_SCHEDULE = new FileStatusType("Content Schedule", "Content Schedule");
	public static final FileStatusType DEVICE_RELEASE = new FileStatusType("Device Release", "Device Release");
	public static final FileStatusType DEVICE_SCRIPT = new FileStatusType("Device Script", "Device Script");
	public static final FileStatusType UPLOAD = new FileStatusType("Upload", "Upload");
	public static final FileStatusType OS_IMAGE_PART = new FileStatusType("OS Image Part","OS Image Part");
	public static final FileStatusType FEED = new FileStatusType("Feed","Feed");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(PRESENTATION.toString(), PRESENTATION);
		INSTANCES.put(CONTENT_SCHEDULE.toString(), CONTENT_SCHEDULE);
		INSTANCES.put(DEVICE_RELEASE.toString(), DEVICE_RELEASE);
		INSTANCES.put(DEVICE_SCRIPT.toString(), DEVICE_SCRIPT);
		INSTANCES.put(UPLOAD.toString(), UPLOAD);
		INSTANCES.put(OS_IMAGE_PART.toString(), OS_IMAGE_PART);
		INSTANCES.put(FEED.toString(), FEED);
	}
	/**
	 * 
	 *
	 */
	public FileStatusType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected FileStatusType(String name, String persistentValue) {
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
	
	// Used for cross-compilation
	public String getValue(){
		return this.getPersistentValue();
	}
	
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static FileStatusType getFileStatusTypeType(String name)
	{
		return (FileStatusType) INSTANCES.get( name );
	}	
}
