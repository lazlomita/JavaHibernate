/*
 * Created on Nov 15, 2004
 */
package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * 
 * @author Jeff Randesi
 *
 */
public class SubStatusType extends PersistentStringEnum 
{	
	public static final SubStatusType DOWNLOADING = new SubStatusType("Downloading","Downloading");
	public static final SubStatusType UPLOADING = new SubStatusType("Uploading","Uploading");
	public static final SubStatusType BUILDING = new SubStatusType("Building","Building");
	public static final SubStatusType RUNNING = new SubStatusType("Running","Running");
	public static final SubStatusType ACTIVATING = new SubStatusType("Activating","Activating");
	public static final Map<String, SubStatusType> INSTANCES = new HashMap<String, SubStatusType>();
	/**
	 * 
	 */
	static
	{		
		INSTANCES.put(DOWNLOADING.toString(), DOWNLOADING);
		INSTANCES.put(UPLOADING.toString(), UPLOADING);
		INSTANCES.put(BUILDING.toString(), BUILDING);
		INSTANCES.put(RUNNING.toString(), RUNNING);
		INSTANCES.put(ACTIVATING.toString(), ACTIVATING);		
	}
	/**
	 * 
	 *
	 */
	public SubStatusType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SubStatusType(String name, String persistentValue) {
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
	 * @return
	 */
	public String getDeviceCommandStatusTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List<SubStatusType> getDeviceCommandStatusTypes()
	{
		List<SubStatusType> l = new LinkedList<SubStatusType>();		
		for(Iterator<SubStatusType> i = INSTANCES.values().iterator(); i.hasNext(); ){
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param contentSchedulerStatusTypeName
	 * @return
	 */
	public static SubStatusType getDeviceCommandSubStatusType(String deviceCommandStatusTypeName)
	{
		return (SubStatusType) INSTANCES.get( deviceCommandStatusTypeName  );
	}	
}
