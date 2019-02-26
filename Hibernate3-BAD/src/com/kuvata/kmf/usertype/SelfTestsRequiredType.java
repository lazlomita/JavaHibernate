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
public class SelfTestsRequiredType extends PersistentStringEnum 
{	
	public static final SelfTestsRequiredType YES = new SelfTestsRequiredType("Yes", "Yes");
	public static final SelfTestsRequiredType NO = new SelfTestsRequiredType("No", "No");
	public static final SelfTestsRequiredType MANUAL = new SelfTestsRequiredType("Manual", "Manual");	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(YES.toString(), YES);
		INSTANCES.put(NO.toString(), NO);
		INSTANCES.put(MANUAL.toString(), MANUAL);
	}
	/**
	 * 
	 *
	 */
	public SelfTestsRequiredType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected SelfTestsRequiredType(String name, String persistentValue) {
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
}
