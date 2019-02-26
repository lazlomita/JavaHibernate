/*
 * Created on Nov 15, 2004
 */
package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;
/**
 * 
 * @author anaber
 *
 */
public class ContentRotationImportType extends PersistentStringEnum 
{	
	public static final ContentRotationImportType APPEND = new ContentRotationImportType("Append","Append");
	public static final ContentRotationImportType REPLACE = new ContentRotationImportType("Replace","Replace");		
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(APPEND.toString(), APPEND);
		INSTANCES.put(REPLACE.toString(), REPLACE);
	}
	/**
	 * 
	 *
	 */
	public ContentRotationImportType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected ContentRotationImportType(String name, String persistentValue) {
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
	 * @param contentSchedulerStatusTypeName
	 * @return
	 */
	public static ContentRotationImportType getContentRotationImportType(String name)
	{
		return (ContentRotationImportType) INSTANCES.get( name  );
	}	
}
