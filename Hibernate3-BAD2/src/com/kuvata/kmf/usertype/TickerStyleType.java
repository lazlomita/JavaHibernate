package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class TickerStyleType extends PersistentStringEnum {

	public static final TickerStyleType NORMAL = new TickerStyleType("Normal", "");
	public static final TickerStyleType BOLD = new TickerStyleType("Bold", "b");
	public static final TickerStyleType ITALIC = new TickerStyleType("Italic", "i");
	public static final Map<String, TickerStyleType> INSTANCES = new HashMap<String, TickerStyleType>();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(NORMAL.toString(), NORMAL);
		INSTANCES.put(BOLD.toString(), BOLD);
		INSTANCES.put(ITALIC.toString(), ITALIC);
	}
	
	public TickerStyleType() {}
	
	protected TickerStyleType(String name, String persistentValue) {
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
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * 
	 */
	public static List<TickerStyleType> getTickerStyleTypes()
	{
		List<TickerStyleType> l = new LinkedList<TickerStyleType>();
		Iterator<TickerStyleType> i = INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);
		
		return l;
	}	
	
	public static TickerStyleType getTickerStyle(String name)
	{
		for( Iterator<TickerStyleType> i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			TickerStyleType tpr = (TickerStyleType)i.next();
			if( tpr.getName().equalsIgnoreCase( name ) ){
				return tpr;
			}
		}
		return null;
	}
	
	public static TickerStyleType getTickerStyleFromPersistentValue(String value)
	{
		for( Iterator<TickerStyleType> i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			TickerStyleType tpr = (TickerStyleType)i.next();
			if( tpr.getPersistentValue().equalsIgnoreCase( value ) ){
				return tpr;
			}
		}
		return null;
	}
}
