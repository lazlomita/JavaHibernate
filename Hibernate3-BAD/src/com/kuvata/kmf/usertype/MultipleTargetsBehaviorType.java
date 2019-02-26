package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class MultipleTargetsBehaviorType extends PersistentStringEnum 
{	
	public static final MultipleTargetsBehaviorType RANDOM = new MultipleTargetsBehaviorType("Choose Randomly", "Random");
	public static final MultipleTargetsBehaviorType SEQUENTIAL = new MultipleTargetsBehaviorType("Play in order", "In Order");	
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(RANDOM.toString(), RANDOM);
		INSTANCES.put(SEQUENTIAL.toString(), SEQUENTIAL);		
	}
	/**
	 * 
	 *
	 */
	public MultipleTargetsBehaviorType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected MultipleTargetsBehaviorType(String name, String persistentValue) {
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
	 * @param segmentEndTypeName
	 * @return
	 */
	public static MultipleTargetsBehaviorType getMultpleTargetsBehaviorType(String type)
	{
		return (MultipleTargetsBehaviorType) INSTANCES.get( type );
	}	
	
	/**
	 * 
	 * @param persistentValue
	 * @return
	 */
	public static MultipleTargetsBehaviorType getMultpleTargetsBehaviorTypeByPersistentValue(String persistentValue)
	{				
		for( Iterator<MultipleTargetsBehaviorType> i = MultipleTargetsBehaviorType.INSTANCES.values().iterator(); i.hasNext(); ){
			MultipleTargetsBehaviorType ps = i.next();
			if( ps.getPersistentValue().equals( persistentValue) ){
				return ps;
			}
		}
		return null;
	}	
}
