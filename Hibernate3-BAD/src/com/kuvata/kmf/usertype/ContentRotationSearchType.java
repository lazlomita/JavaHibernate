package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class ContentRotationSearchType extends PersistentStringEnum 
{	
	public static final ContentRotationSearchType CONTENT_ROTATION_NAME = new ContentRotationSearchType("Content Rotation Name", "content_rotation_name");	
	public static final ContentRotationSearchType CONTENT_ROTATION_ID = new ContentRotationSearchType("Content Rotation ID", "content_rotation_id");	
	public static final LinkedHashMap<String, ContentRotationSearchType> INSTANCES = new LinkedHashMap<String, ContentRotationSearchType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(CONTENT_ROTATION_NAME.toString(), CONTENT_ROTATION_NAME);
		INSTANCES.put(CONTENT_ROTATION_ID.toString(), CONTENT_ROTATION_ID);		
	}
	/**
	 * 
	 *
	 */
	public ContentRotationSearchType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected ContentRotationSearchType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns the EventType associated with the given persistentValue
	 * @param persistentValue
	 * @return
	 */
	public static ContentRotationSearchType getSearchType(String persistentValue)
	{
		for( Iterator<ContentRotationSearchType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			ContentRotationSearchType et = (ContentRotationSearchType)i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
	
	public static LinkedList<ContentRotationSearchType> getSearchTypes()
	{
		LinkedList<ContentRotationSearchType> l = new LinkedList<ContentRotationSearchType>();
		for(Iterator<ContentRotationSearchType> i = ContentRotationSearchType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add( i.next() );
		}		
		return l;
	}	
}
