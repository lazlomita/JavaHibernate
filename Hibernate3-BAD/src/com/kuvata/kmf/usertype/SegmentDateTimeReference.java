package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on November 29, 2017
 * Copyright 2017, Inception Signage, Inc.
 * 
 * @author Lazlo Mita
 */
public class SegmentDateTimeReference 
{	
	public static final SegmentDateTimeReference DEVICE_LOCAL_TIME = new SegmentDateTimeReference("Device local time");
	public static final SegmentDateTimeReference SERVER_ABSOLUTE_TIME = new SegmentDateTimeReference("Server absolute time");
	public static final Map INSTANCES = new HashMap();
	
	private String value;
	
	static{
		INSTANCES.put(DEVICE_LOCAL_TIME.toString(), DEVICE_LOCAL_TIME);
		INSTANCES.put(SERVER_ABSOLUTE_TIME.toString(), SERVER_ABSOLUTE_TIME);	
	}
	
	public SegmentDateTimeReference() {}
	
	protected SegmentDateTimeReference(String value) {
		this.value = value;
	}
	
	public String toString(){
		return this.value;
	}
	
	public static List getSegmentDateTimeReference(){
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
	
	public static SegmentDateTimeReference getSegmentDateTimeReference(String segmentDateTimeReference){
		return (SegmentDateTimeReference) INSTANCES.get( segmentDateTimeReference );
	}	
}