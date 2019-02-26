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
public class DefaultAssetAffinityType extends PersistentStringEnum 
{	
	public static final DefaultAssetAffinityType RANDOM = new DefaultAssetAffinityType("Choose Randomly", "Random");
	public static final DefaultAssetAffinityType FIRST_IN_LIST = new DefaultAssetAffinityType("Choose first asset in list", "First In List");
	public static final DefaultAssetAffinityType SKIP_ASSET = new DefaultAssetAffinityType("Skip Asset", "Skip Asset");
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(RANDOM.toString(), RANDOM);
		INSTANCES.put(FIRST_IN_LIST.toString(), FIRST_IN_LIST);
		INSTANCES.put(SKIP_ASSET.toString(), SKIP_ASSET);
	}
	/**
	 * 
	 *
	 */
	public DefaultAssetAffinityType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DefaultAssetAffinityType(String name, String persistentValue) {
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
	public static List getSegmentEndTypes()
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
	 * @param segmentEndTypeName
	 * @return
	 */
	public static DefaultAssetAffinityType getDefaultAssetAffinityType(String type)
	{
		return (DefaultAssetAffinityType) INSTANCES.get( type );
	}
	
	public static DefaultAssetAffinityType getDefaultAssetAffinityTypeByPersistentValue(String persistentValue)
	{				
		for( Iterator<DefaultAssetAffinityType> i = DefaultAssetAffinityType.INSTANCES.values().iterator(); i.hasNext(); ){
			DefaultAssetAffinityType ps = i.next();
			if( ps.getPersistentValue().equals( persistentValue) ){
				return ps;
			}
		}
		return null;
	}
}
