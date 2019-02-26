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
 * 
 * @author anaber
 *
 */
public class PlaylistImportStatus extends PersistentStringEnum 
{	
	public static final PlaylistImportStatus SUCCESS = new PlaylistImportStatus("Success","Success");
	public static final PlaylistImportStatus FAILED = new PlaylistImportStatus("Failed","Failed");	
	public static final PlaylistImportStatus WARNING = new PlaylistImportStatus("See details.","Warning");	
	public static final PlaylistImportStatus IN_PROGRESS = new PlaylistImportStatus("In Progress","In Progress");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(SUCCESS.toString(), SUCCESS);
		INSTANCES.put(FAILED.toString(), FAILED);
		INSTANCES.put(IN_PROGRESS.toString(), IN_PROGRESS);
	}
	/**
	 * 
	 *
	 */
	public PlaylistImportStatus() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PlaylistImportStatus(String name, String persistentValue) {
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
	public static List getPlaylistImportStatuses()
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
	public static PlaylistImportStatus getPlaylistImportStatus(String name)
	{
		return (PlaylistImportStatus) INSTANCES.get( name  );
	}	
}
