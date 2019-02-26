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
 * This class is relied upon by other layers (i.e. the Promo Only layer), so even though
 * there are currently no ContentSchedulerHookTypes for the core product, 
 * this class must remain in the base code for compilation reasons.
 * 
 * @author jrandesi
 */
public class ContentSchedulerHookType extends PersistentStringEnum 
{			
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */
	static
	{
	}
	/**
	 * 
	 *
	 */
	public ContentSchedulerHookType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public ContentSchedulerHookType(String name, String persistentValue) {
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
	public String getContentSchedulerHookTypeName()
	{
		return this.name;
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getContentSchedulerHookTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param persistentValue
	 * @return
	 */
	public static ContentSchedulerHookType getContentSchedulerHookTypeByPersistentValue(String persistentValue)
	{		
		Iterator i = ContentSchedulerHookType.INSTANCES.values().iterator();
		while(i.hasNext())
		{
			ContentSchedulerHookType ps = (ContentSchedulerHookType)i.next();
			if( ps.getPersistentValue().equals( persistentValue) ) {
				return ps;
			}
		}
		return null;
	}		
	/**
	 * 
	 * @param contentSchedulerStatusTypeName
	 * @return
	 */
	public static ContentSchedulerHookType getContentSchedulerHookType(String name)
	{
		return (ContentSchedulerHookType) INSTANCES.get( name );
	}	
}
