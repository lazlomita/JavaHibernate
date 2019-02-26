/*
 * Created on Sep 27, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.logging;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public interface HistorizableChildEntity extends Historizable {
	
	/*
	 * This is the entityId that will be used when saving to the history_entry table.
	 * 
	 */
	public Long getHistoryEntityId();
	
	/*
	 * This is the entityName that will be used when saving to the history_entry table.
	 * Most often it is the name of one of the underlying entities within the collection member's class. 
	 * (For example, DeviceSchedule's implementation of getEntityName() is this.getDevice().getDeviceName())
	 */
	public String getEntityName();

}
