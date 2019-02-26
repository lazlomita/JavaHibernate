package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Nov 3, 2015
 * 
 * @author Wilfredo Vargas
 */
public class PlaylistIngesterColumnType extends PersistentStringEnum 
{
	public static final PlaylistIngesterColumnType PLAYLIST_ID = new PlaylistIngesterColumnType("playlist_id", "playlist_id");
	public static final PlaylistIngesterColumnType NAME = new PlaylistIngesterColumnType("name", "name");
	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(PLAYLIST_ID.toString(), PLAYLIST_ID);
		INSTANCES.put(NAME.toString(), NAME);
	}
	/**
	 * 
	 *
	 */
	public PlaylistIngesterColumnType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PlaylistIngesterColumnType(String name, String persistentValue) {
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
	 * @param dirtyTypeName
	 * @return
	 */
	public static PlaylistIngesterColumnType getPlaylistImporterColumnType(String name)
	{
		return (PlaylistIngesterColumnType) INSTANCES.get( name );
	}	
}
