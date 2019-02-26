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
public class AssetIntervalUnitType extends PersistentStringEnum 
{	
	public static final AssetIntervalUnitType MINUTES = new AssetIntervalUnitType("minutes", "minutes");
	public static final AssetIntervalUnitType HOURS = new AssetIntervalUnitType("hours", "hours");	
	public static final AssetIntervalUnitType SECONDS = new AssetIntervalUnitType("seconds", "seconds");
	public static final AssetIntervalUnitType ASSETS = new AssetIntervalUnitType("assets", "assets");
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(MINUTES.toString(), MINUTES);
		INSTANCES.put(HOURS.toString(), HOURS);
		INSTANCES.put(SECONDS.toString(), SECONDS);
		INSTANCES.put(ASSETS.toString(), ASSETS);
	}
	/**
	 * 
	 *
	 */
	public AssetIntervalUnitType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected AssetIntervalUnitType(String name, String persistentValue) {
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
	public static List getAssetIntervalUnits()
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
	 * @param name
	 * @return
	 */
	public static AssetIntervalUnitType getAssetIntervalUnit(String name)
	{
		return (AssetIntervalUnitType) INSTANCES.get( name );
	}	
}
