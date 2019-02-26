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

import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * NOTE: This class is currently being used by both Playlist and Content Rotation.
 * If we decide to support different orderTypes for Playlists vs. Content Rotations, 
 * we could create separate usertype classes at that point.
 * @author jrandesi
 *
 */
public class PlaylistOrderType extends PersistentStringEnum 
{	
	public static final PlaylistOrderType SEQUENTIAL = new PlaylistOrderType("Sequential","Sequential");	
	public static final PlaylistOrderType RANDOM = new PlaylistOrderType("Random","Random");		
	public static final Map INSTANCES = new HashMap();
	
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(SEQUENTIAL.toString(), SEQUENTIAL);
		INSTANCES.put(RANDOM.toString(), RANDOM);
	}
	/**
	 * 
	 *
	 */
	public PlaylistOrderType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PlaylistOrderType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	
	public String getName()
	{
		return this.name;
	}	

	/**
	 * 
	 * @return
	 */
	public static List getPlaylistOrderTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param contentSchedulerStatusTypeName
	 * @return
	 */
	public static PlaylistOrderType getPlaylistOrderType(String name)
	{
		return (PlaylistOrderType) INSTANCES.get( name  );
	}
}
