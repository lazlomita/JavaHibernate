package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class StatusType extends PersistentStringEnum 
{	
	public static final StatusType HOLD = new StatusType("Hold","Hold", 0);
	public static final StatusType QUEUED = new StatusType("Queued","Queued", 1);
	public static final StatusType IN_PROGRESS = new StatusType("In Progress","In Progress", 2);
	public static final StatusType PENDING = new StatusType("Pending","Pending", 3);	
	public static final StatusType SUCCESS = new StatusType("Success","Success", 4);
	public static final StatusType FAILED = new StatusType("Failed","Failed", 4);
	public static final StatusType CANCELLED = new StatusType("Cancelled","Cancelled", 4);
	
	public static final Map<String, StatusType> INSTANCES = new HashMap<String, StatusType>();
	private int priority = 0;
	
	/**
	 * 
	 */
	static
	{
		INSTANCES.put(HOLD.toString(), HOLD);
		INSTANCES.put(QUEUED.toString(), QUEUED);
		INSTANCES.put(PENDING.toString(), PENDING);
		INSTANCES.put(SUCCESS.toString(), SUCCESS);
		INSTANCES.put(FAILED.toString(), FAILED);
		INSTANCES.put(PENDING.toString(), PENDING);
		INSTANCES.put(CANCELLED.toString(), CANCELLED);
	}
	/**
	 * 
	 *
	 */
	public StatusType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected StatusType(String name, String persistentValue, int priority) {
		super(name, persistentValue);
		this.priority = priority;
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	
	// Used for cross-compilation
	public boolean equals(String s){
		return this.getPersistentValue().equals(s);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getStatusTypeName()
	{
		return this.name;
	}
	
	/**
	 * Returns true if the given status is of a higher priority that "this" status object.
	 * Otherwise, returns false.
	 * @param status
	 * @return
	 */
	public boolean isGreaterThanOrEqual(StatusType status)
	{
		if( this.getPriority() >= status.getPriority() ){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 
	 * @return
	 */
	public static List<StatusType> getStatusTypes()
	{
		List<StatusType> l = new LinkedList<StatusType>();		
		for(Iterator<StatusType> i = INSTANCES.values().iterator(); i.hasNext();){
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
	public static StatusType getStatusType(String statusTypeName)
	{
		return (StatusType) INSTANCES.get( statusTypeName  );
	}
	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}	
	
	
}
