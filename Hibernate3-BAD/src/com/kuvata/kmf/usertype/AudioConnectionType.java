package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.usertype.PersistentStringEnum;

public class AudioConnectionType extends PersistentStringEnum 
{	
	public static final AudioConnectionType STEREO_JACK = new AudioConnectionType("3.5 mm Jack","3.5 mm Jack");
	public static final AudioConnectionType HDMI = new AudioConnectionType("HDMI","HDMI");	
	//public static final AudioConnectionType USB = new AudioConnectionType("USB","USB");
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(STEREO_JACK.toString(), STEREO_JACK);
		INSTANCES.put(HDMI.toString(), HDMI);
		//INSTANCES.put(USB.toString(), USB);
	}
	/**
	 * 
	 *
	 */
	public AudioConnectionType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected AudioConnectionType(String name, String persistentValue) {
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
	public static List getAudioConnectionTypes()
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
	public static AudioConnectionType getAudioConnectionTypeByName(String name){
		return (AudioConnectionType) INSTANCES.get( name );
	}	
	/**
	 * 
	 * @param displayOrientationTypeName
	 * @return
	 */
	public static AudioConnectionType getAudioConnectionType(String value)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); ){
			AudioConnectionType act = (AudioConnectionType)i.next();
			if( act.getPersistentValue().equalsIgnoreCase( value ) ){
				return act;
			}
		}
		return null;
	}	
}
