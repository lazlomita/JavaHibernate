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
public class PlaylistSearchType extends PersistentStringEnum 
{	
	public static final PlaylistSearchType PLAYLIST_NAME = new PlaylistSearchType("Playlist Name", "playlist_name");	
	public static final PlaylistSearchType PLAYLIST_ID = new PlaylistSearchType("Playlist ID", "playlist_id");	
	public static final LinkedHashMap<String, PlaylistSearchType> INSTANCES = new LinkedHashMap<String, PlaylistSearchType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(PLAYLIST_NAME.toString(), PLAYLIST_NAME);
		INSTANCES.put(PLAYLIST_ID.toString(), PLAYLIST_ID);		
	}
	/**
	 * 
	 *
	 */
	public PlaylistSearchType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PlaylistSearchType(String name, String persistentValue) {
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
	public static PlaylistSearchType getSearchType(String persistentValue)
	{
		for( Iterator<PlaylistSearchType> i=INSTANCES.values().iterator(); i.hasNext(); ){
			PlaylistSearchType et = (PlaylistSearchType)i.next();
			if( et.getPersistentValue().equalsIgnoreCase( persistentValue ) ){
				return et;
			}
		}
		return null;
	}		
	
	public static LinkedList<PlaylistSearchType> getSearchTypes()
	{
		LinkedList<PlaylistSearchType> l = new LinkedList<PlaylistSearchType>();
		for(Iterator<PlaylistSearchType> i = PlaylistSearchType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add( i.next() );
		}		
		return l;
	}	
}
