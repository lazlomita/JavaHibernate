/*
 * Created on Nov 15, 2004
 */
package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * 
 * @author anaber
 *
 */
public class DisplayRotationType extends PersistentStringEnum 
{	
	public static final DisplayRotationType VERTICAL_LEFT = new DisplayRotationType("Vertical (Left)","left");
	public static final DisplayRotationType VERTICAL_RIGHT = new DisplayRotationType("Vertical (Right)","right");	
	public static final DisplayRotationType HORIZONTAL = new DisplayRotationType("Horizontal","normal");
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(VERTICAL_LEFT.toString(), VERTICAL_LEFT);
		INSTANCES.put(VERTICAL_RIGHT.toString(), VERTICAL_RIGHT);
		INSTANCES.put(HORIZONTAL.toString(), HORIZONTAL);
	}
	/**
	 * 
	 *
	 */
	public DisplayRotationType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DisplayRotationType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	public String getName()	{
		return this.name;
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
	public static List getDisplayRotationTypes()
	{
		List l = new LinkedList();		
		for( Iterator i = INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		BeanPropertyComparator comparator1 = new BeanPropertyComparator("name");
		Collections.sort( l, comparator1 );		
		return l;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static DisplayRotationType getDisplayRotationTypeByName(String name){
		return (DisplayRotationType) INSTANCES.get( name );
	}	
	/**
	 * 
	 * @param displayOrientationTypeName
	 * @return
	 */
	public static DisplayRotationType getDisplayRotationType(String displayRotationPersistentValue)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); ){
			DisplayRotationType drt = (DisplayRotationType)i.next();
			if( drt.getPersistentValue().equalsIgnoreCase( displayRotationPersistentValue ) ){
				return drt;
			}
		}
		return null;
	}	
}
