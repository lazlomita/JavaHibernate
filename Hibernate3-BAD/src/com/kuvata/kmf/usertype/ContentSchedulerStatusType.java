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
 * @author anaber
 *
 */
public class ContentSchedulerStatusType extends PersistentStringEnum 
{	
	public static final ContentSchedulerStatusType SUCCESS = new ContentSchedulerStatusType("Success","Success");
	public static final ContentSchedulerStatusType FAILED = new ContentSchedulerStatusType("Failed","Failed");	
	public static final ContentSchedulerStatusType IN_PROGRESS = new ContentSchedulerStatusType("In Progress","In Progress");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(SUCCESS.toString(), SUCCESS);
		INSTANCES.put(FAILED.toString(), FAILED);
		INSTANCES.put(IN_PROGRESS.toString(), IN_PROGRESS);
	}
	/**
	 * 
	 *
	 */
	public ContentSchedulerStatusType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected ContentSchedulerStatusType(String name, String persistentValue) {
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
	 * @return
	 */
	public String getContentSchedulerStatusTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getContentSchedulerStatusTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext())
		{
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
	public static ContentSchedulerStatusType getContentSchedulerStatusType(String contentSchedulerStatusTypeName)
	{
		return (ContentSchedulerStatusType) INSTANCES.get( contentSchedulerStatusTypeName );
	}	
}
