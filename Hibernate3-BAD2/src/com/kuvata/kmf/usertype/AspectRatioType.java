package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class AspectRatioType extends PersistentStringEnum 
{	
	public static final AspectRatioType ASPECT_RATIO_4x3 = new AspectRatioType("4:3", "4:3");
	public static final AspectRatioType ASPECT_RATIO_16x9 = new AspectRatioType("16:9", "16:9");
	public static final AspectRatioType ASPECT_RATIO_CUSTOM = new AspectRatioType( Constants.CUSTOM_DISPLAY_NAME, Constants.CUSTOM_DISPLAY_NAME);
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(ASPECT_RATIO_4x3.toString(), ASPECT_RATIO_4x3);
		INSTANCES.put(ASPECT_RATIO_16x9.toString(), ASPECT_RATIO_16x9);
		INSTANCES.put(ASPECT_RATIO_CUSTOM.toString(), ASPECT_RATIO_CUSTOM);
	}
	/**
	 * 
	 *
	 */
	public AspectRatioType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected AspectRatioType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List getAspectRatioTypes()
	{
		List l = new LinkedList();
		for( Iterator i=AspectRatioType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}	
	
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static AspectRatioType getAspectRatioType(String name) {
		return (AspectRatioType) INSTANCES.get( name );
	}	
}
