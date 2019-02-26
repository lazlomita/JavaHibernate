package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.attr.DateAttr;
import com.kuvata.kmf.attr.NumberAttr;
import com.kuvata.kmf.attr.StringAttr;
import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class AttrType extends PersistentStringEnum 
{	
	public static final AttrType STRING = new AttrType("Text", StringAttr.class.getName());
	public static final AttrType NUMBER = new AttrType("Number", NumberAttr.class.getName());
	public static final AttrType DATE = new AttrType("Date", DateAttr.class.getName());
	
	public static final Map INSTANCES = new HashMap();
			    
	static
	{
		INSTANCES.put(STRING.toString(), STRING);
		INSTANCES.put(NUMBER.toString(), NUMBER);
		INSTANCES.put(DATE.toString(), DATE);
	}
	
	public AttrType() {}

	public AttrType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public String getAttrTypeName()
	{
		return this.name;
	}

	/**
	 * 
	 * @param displayOrientationTypeName
	 * @return
	 */
	public static AttrType getAttrType(String attrTypePersistentValue)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			AttrType attrType = (AttrType)i.next();
			if( attrType.getPersistentValue().equalsIgnoreCase( attrTypePersistentValue ) ){
				return attrType;
			}
		}
		return null;
	}		
	
	/**
	 * Returns a list of all attr types
	 * @return
	 */
	public static List getAttrTypes()
	{
		List l = new LinkedList();
		Iterator i = AttrType.INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}	
}
