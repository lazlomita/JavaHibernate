package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class VariableLengthType extends PersistentStringEnum 
{	
	public static final VariableLengthType VARIABLE = new VariableLengthType("variable", "variable");	
	public static final VariableLengthType FIXED = new VariableLengthType("fixed", "fixed");
	public static final VariableLengthType DEFAULT = new VariableLengthType("default", "default");
	public static final LinkedHashMap<String, VariableLengthType> INSTANCES = new LinkedHashMap<String, VariableLengthType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(VARIABLE.toString(), VARIABLE);
		INSTANCES.put(FIXED.toString(), FIXED);
		INSTANCES.put(DEFAULT.toString(), DEFAULT);
	}
	/**
	 * 
	 *
	 */
	public VariableLengthType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected VariableLengthType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns the VariableLengthType associated with the given persistentValue
	 * @param persistentValue
	 * @return
	 */
	public static VariableLengthType getVariableLengthType(String persistentValue)
	{
		for( Iterator<VariableLengthType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			VariableLengthType et = (VariableLengthType)i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
}
