package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class FramesyncType extends PersistentStringEnum 
{	
	public static final FramesyncType OFF = new FramesyncType("Off", "Off");
	public static final FramesyncType LOCAL_MASTER = new FramesyncType( "Local Master", "Local Master");
	public static final FramesyncType NETWORK_MASTER = new FramesyncType( "Network Master", "Network Master");
	public static final FramesyncType SLAVE = new FramesyncType("Slave", "Slave");
	public static final Map INSTANCES = new LinkedHashMap();
	
	static{
		INSTANCES.put(OFF.toString(), OFF);
		INSTANCES.put(LOCAL_MASTER.toString(), LOCAL_MASTER);
		INSTANCES.put(NETWORK_MASTER.toString(), NETWORK_MASTER);
		INSTANCES.put(SLAVE.toString(), SLAVE);
	}
	
	public FramesyncType() {}
	
	protected FramesyncType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public static List getFramesyncTypes()
	{
		List l = new LinkedList();
		for( Iterator i=FramesyncType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		return l;
	}	
	
	public static FramesyncType getFramesyncType(String name) {
		return (FramesyncType) INSTANCES.get( name );
	}	
}
