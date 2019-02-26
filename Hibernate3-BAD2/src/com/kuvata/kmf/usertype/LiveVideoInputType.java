package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class LiveVideoInputType extends PersistentStringEnum 
{	
	public static final LiveVideoInputType TUNER = new LiveVideoInputType("Tuner", "0");
	public static final LiveVideoInputType SVIDEO = new LiveVideoInputType("S-Video", "1");
	public static final LiveVideoInputType COMPOSITE = new LiveVideoInputType("Composite", "2");
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(TUNER.toString(), TUNER);
		INSTANCES.put(SVIDEO.toString(), SVIDEO);
		INSTANCES.put(COMPOSITE.toString(), COMPOSITE);
	}
	/**
	 * 
	 *
	 */
	public LiveVideoInputType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected LiveVideoInputType(String name, String persistentValue) {
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
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getLiveVideoInputTypes()
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
	 * @param name
	 * @return
	 */
	public static LiveVideoInputType getLiveVideoInputType(String persistentValue)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			LiveVideoInputType inputType = (LiveVideoInputType)i.next();
			if( inputType.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return inputType;
			}
		}
		return null;
	}	
}
