package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Apr 6, 2016
 * 
 * @author Wilfredo Vargas
 */
public class ContentRotationColumnType extends PersistentStringEnum 
{
	public static final ContentRotationColumnType CONTENTROTATION_ID = new ContentRotationColumnType("contentrotation_id", "contentrotation_id");
	public static final ContentRotationColumnType NAME = new ContentRotationColumnType("name", "name");
	
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(CONTENTROTATION_ID.toString(), CONTENTROTATION_ID);
		INSTANCES.put(NAME.toString(), NAME);
	}
	/**
	 * 
	 *
	 */
	public ContentRotationColumnType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected ContentRotationColumnType(String name, String persistentValue) {
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
	public static ContentRotationColumnType getPlaylistImporterColumnType(String name)
	{
		return (ContentRotationColumnType) INSTANCES.get( name );
	}	
}
