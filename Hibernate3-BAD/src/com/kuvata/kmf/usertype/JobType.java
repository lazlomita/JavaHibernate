package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


public class JobType extends PersistentStringEnum 
{	
	public static final JobType ASSET_FILE_UPDATE = new JobType("AssetFileUpdate", "AssetFileUpdate");
	public static final JobType DELETE_DEVICE = new JobType("DeleteDevice", "DeleteDevice");
	
	public static final Map INSTANCES = new HashMap();
			    
	static{
		INSTANCES.put(ASSET_FILE_UPDATE.toString(), ASSET_FILE_UPDATE);
		INSTANCES.put(DELETE_DEVICE.toString(), DELETE_DEVICE);
	}

	public JobType(){}
	
	public JobType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString(){
		return this.name;
	}
	
	public String getJobName(){
		return this.name;
	}

	public static JobType getJobType(String jobTypePersistentValue){
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); ){
			JobType jobType = (JobType)i.next();
			if( jobType.getPersistentValue().equalsIgnoreCase( jobTypePersistentValue ) ){
				return jobType;
			}
		}
		return null;
	}		
	
	public static List getJobTypes(){
		List l = new LinkedList();
		Iterator i = JobType.INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}	
}
