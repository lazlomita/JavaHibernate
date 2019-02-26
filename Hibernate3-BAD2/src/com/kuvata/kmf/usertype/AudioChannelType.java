package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class AudioChannelType extends PersistentStringEnum 
{	
	public static final AudioChannelType ALL = new AudioChannelType("All", "All");
	public static final AudioChannelType LEFT = new AudioChannelType("Left", "Left");
	public static final AudioChannelType RIGHT = new AudioChannelType( "Right", "Right");
	public static final AudioChannelType NONE = new AudioChannelType( "None", "None");
	public static final Map INSTANCES = new HashMap();
	
	static{
		INSTANCES.put(ALL.toString(), ALL);
		INSTANCES.put(LEFT.toString(), LEFT);
		INSTANCES.put(RIGHT.toString(), RIGHT);
		INSTANCES.put(NONE.toString(), NONE);
	}
	
	public AudioChannelType() {}
	
	protected AudioChannelType(String name, String persistentValue) {
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
	public static List getAudioChannelTypes()
	{
		List l = new LinkedList();
		for( Iterator i=AudioChannelType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}	
	
	public static AudioChannelType getAudioChannelType(String name) {
		return (AudioChannelType) INSTANCES.get( name );
	}	
}
