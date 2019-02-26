package com.kuvata.kmf.usertype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.kuvata.kmf.usertype.PersistentStringEnum;



/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class TickerPresentationStyle extends PersistentStringEnum 
{	
	private static Logger logger = Logger.getLogger(TickerPresentationStyle.class);
	public static final TickerPresentationStyle SCROLLING = new TickerPresentationStyle("Scrolling", "scrolling");
	public static final TickerPresentationStyle PAGING = new TickerPresentationStyle("Paging", "paging");		
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(SCROLLING.toString(), SCROLLING);
		INSTANCES.put(PAGING.toString(), PAGING);
	}
	/**
	 * 
	 *
	 */
	public TickerPresentationStyle() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected TickerPresentationStyle(String name, String persistentValue) {
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
	public static ArrayList getTickerPresentationStyles()
	{
		boolean isOpenGL = true;
		
		ArrayList result = new ArrayList();
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); ) 
		{
			TickerPresentationStyle tps = (TickerPresentationStyle)i.next();
			
			// Only add the PAGING presentation style if the property is set
			if( tps.getPersistentValue().equalsIgnoreCase( TickerPresentationStyle.PAGING.getPersistentValue() ) ){
				if( isOpenGL ){
					result.add( tps );
				}
			}else{
				result.add( tps );
			}
		}
		
		// Sort the list in alphabetical order
		Collections.sort( result );		
		return result;
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static TickerPresentationStyle getTickerPresentationStyle(String persistentValue)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			TickerPresentationStyle tpr = (TickerPresentationStyle)i.next();
			if( tpr.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return tpr;
			}
		}
		return null;
	}	
}
