package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class SegmentType extends PersistentStringEnum 
{	
	public static final SegmentType CONTENT = new SegmentType("Content", "Content");
	public static final SegmentType CONTROL = new SegmentType("Control", "Control");
	public static final Map INSTANCES = new HashMap();
	
	static{
		INSTANCES.put(CONTENT.toString(), CONTENT);
		INSTANCES.put(CONTROL.toString(), CONTROL);
	}
	
	public SegmentType() {}
	
	protected SegmentType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List getSegmentTypes()
	{
		List l = new LinkedList();
		for( Iterator i=SegmentType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}	
	
	public static SegmentType getSegmentType(String name) {
		return (SegmentType) INSTANCES.get( name );
	}	
}
