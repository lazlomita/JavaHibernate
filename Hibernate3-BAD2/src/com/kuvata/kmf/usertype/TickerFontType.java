package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class TickerFontType extends PersistentStringEnum {
	public static final TickerFontType DEFAULT_FONT = new TickerFontType("Default Font", "Default Font");
	public static final TickerFontType ARIAL = new TickerFontType("Arial", "Arial");
	public static final TickerFontType HELVETICA = new TickerFontType("Helvetica", "Helvetica");
	public static final TickerFontType TAHOMA = new TickerFontType("Tahoma", "Tahoma");
	public static final TickerFontType VERDANA = new TickerFontType("Verdana", "Verdana");
	
	public static final Map<String, TickerFontType> INSTANCES = new HashMap<String, TickerFontType>();
	
	/**
	 * 
	 */    
	static
	{
		INSTANCES.put(DEFAULT_FONT.toString(), DEFAULT_FONT);
		INSTANCES.put(ARIAL.toString(), ARIAL);
		INSTANCES.put(HELVETICA.toString(), HELVETICA);
		INSTANCES.put(TAHOMA.toString(), TAHOMA);
		INSTANCES.put(VERDANA.toString(), VERDANA);
	}
	
	public TickerFontType() {}
	
	protected TickerFontType(String name, String persistentValue) {
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
	public static List<TickerFontType> getTickerFontTypes()
	{
		List<TickerFontType> l = new LinkedList<TickerFontType>();
		Iterator<TickerFontType> i = INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);
		
		return l;
	}	
	
	public static TickerFontType getTickerFont(String name)
	{
		for( Iterator<TickerFontType> i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			TickerFontType t = (TickerFontType)i.next();
			if( t.getName().equalsIgnoreCase( name ) ){
				return t;
			}
		}
		return null;
	}
}
