package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class ContentUpdateType extends PersistentStringEnum 
{
	public static final ContentUpdateType NETWORK = new ContentUpdateType("Network", "Network");
	public static final ContentUpdateType DVD_USB = new ContentUpdateType("DVD/USB", "DVD/USB");
	public static final ContentUpdateType NO_UPDATES = new ContentUpdateType("No Updates", "No Updates");
	
	public static final Map INSTANCES = new LinkedHashMap();
			    
	static
	{
		INSTANCES.put(NETWORK.toString(), NETWORK);
		INSTANCES.put(DVD_USB.toString(), DVD_USB);
		INSTANCES.put(NO_UPDATES.toString(), NO_UPDATES);
	}
	
	public ContentUpdateType() {}

	public ContentUpdateType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public static ContentUpdateType getContentUpdateType(String contentUpdateTypePersistentValue)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			ContentUpdateType contentUpdateType = (ContentUpdateType)i.next();
			if( contentUpdateType.getPersistentValue().equalsIgnoreCase( contentUpdateTypePersistentValue ) ){
				return contentUpdateType;
			}
		}
		return null;
	}		
	
	public static List getContentUpdateTypes()
	{
		List l = new LinkedList();
		Iterator i = ContentUpdateType.INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		return l;
	}	
}
