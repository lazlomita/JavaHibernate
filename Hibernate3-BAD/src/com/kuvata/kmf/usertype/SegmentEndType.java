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
public class SegmentEndType extends PersistentStringEnum 
{	
	public static final SegmentEndType END_AFTER = new SegmentEndType("End After", "End After");
	public static final SegmentEndType END_AFTER_NUM_ASSETS = new SegmentEndType("End After Num Assets", "End After Num Assets");
	public static final SegmentEndType WHEN_FINISHED = new SegmentEndType("When Finished", "When Finished");	
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(END_AFTER.toString(), END_AFTER);
		INSTANCES.put(END_AFTER_NUM_ASSETS.toString(), END_AFTER_NUM_ASSETS);		
		INSTANCES.put(WHEN_FINISHED.toString(), WHEN_FINISHED);		
	}
	/**
	 * 
	 *
	 */
	public SegmentEndType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SegmentEndType(String name, String persistentValue) {
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
	public String getSegmentEndTypeName()
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
	public static SegmentEndType getSegmentEndType(String segmentEndTypeName)
	{
		return (SegmentEndType) INSTANCES.get( segmentEndTypeName );
	}	
}
