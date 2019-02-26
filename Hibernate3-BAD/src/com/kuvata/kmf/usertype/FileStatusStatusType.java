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
public class FileStatusStatusType extends PersistentStringEnum 
{		
	public static final FileStatusStatusType EXISTS = new FileStatusStatusType("Exists","Exists");
	public static final FileStatusStatusType IN_PROGRESS = new FileStatusStatusType("In Progress", "In Progress");
	public static final FileStatusStatusType DOWNLOADING = new FileStatusStatusType("Downloading", "Downloading");
	public static final FileStatusStatusType FAILED = new FileStatusStatusType("Failed", "Failed");	
	public static final FileStatusStatusType CANCELED = new FileStatusStatusType("Canceled", "Canceled");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{	
		INSTANCES.put(EXISTS.toString(), EXISTS);
		INSTANCES.put(IN_PROGRESS.toString(), IN_PROGRESS);
		INSTANCES.put(DOWNLOADING.toString(), DOWNLOADING);
		INSTANCES.put(FAILED.toString(), FAILED);		
		INSTANCES.put(CANCELED.toString(), CANCELED);
	}
	/**
	 * 
	 *
	 */
	public FileStatusStatusType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected FileStatusStatusType(String name, String persistentValue) {
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
	 * @param dirtyTypeName
	 * @return
	 */
	public static FileStatusStatusType getFileStatusStatusType(String name)
	{
		return (FileStatusStatusType) INSTANCES.get( name );
	}	
}
