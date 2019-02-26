/*
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.sql.Clob;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * 
 * @author Jeff Randesi
 */
public class DeviceIngesterStatus extends Entity {

	private Long deviceIngesterStatusId;
	private Date dt;
	private Clob status;
	private static final int MAX_RECENT_DEVICE_INGESTERS = 10;

	public DeviceIngesterStatus() 
	{		
	}
	
	public Long getEntityId()
	{
		return this.getDeviceIngesterStatusId();
	}
	
	/**
	 * Returns a DeviceIngesterStatus object with the given id
	 * @param deviceIngesterStatusId
	 * @return
	 * @throws HibernateException
	 */
	public static DeviceIngesterStatus getDeviceIngesterStatus(Long deviceIngesterStatusId) throws HibernateException
	{
		return (DeviceIngesterStatus)Entity.load(DeviceIngesterStatus.class, deviceIngesterStatusId);		
	}
	
	/**
	 * Retrieve the most recent deviceIngesterStatus object
	 * @return
	 * @throws HibernateException
	 */
	public static DeviceIngesterStatus getLastDeviceIngesterStatus() throws HibernateException
	{
		DeviceIngesterStatus result = null;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT dis FROM DeviceIngesterStatus as dis "
			+"WHERE dis.dt = "
			+"(SELECT MAX(dis2.dt) FROM DeviceIngesterStatus as dis2)";			
		Query q = session.createQuery( hql ); 
		q.setMaxResults(1);		
		List l = q.list();
		if( l != null && l.size() > 0 ) {
			result = (DeviceIngesterStatus)l.get(0);			
		}
		return result;		
	}
	
	/**
	 * Retrieve the 10 most recent device ingester status objects
	 * @return
	 * @throws HibernateException
	 */
	public static List<DeviceIngesterStatus> getMostRecentDeviceIngesterStatuses() throws HibernateException
	{
		DeviceIngesterStatus result = null;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT dis FROM DeviceIngesterStatus as dis "
			+"ORDER BY dis.dt DESC";
		Query q = session.createQuery( hql ).setMaxResults( MAX_RECENT_DEVICE_INGESTERS ); 				
		return q.list();		
	}

	

	/**
	 * @return the deviceIngesterStatusId
	 */
	public Long getDeviceIngesterStatusId() {
		return deviceIngesterStatusId;
	}

	/**
	 * @param deviceIngesterStatusId the deviceIngesterStatusId to set
	 */
	public void setDeviceIngesterStatusId(Long deviceIngesterStatusId) {
		this.deviceIngesterStatusId = deviceIngesterStatusId;
	}

	/**
	 * @return Returns the dt.
	 */
	public Date getDt() {
		return dt;
	}
	

	/**
	 * @param dt The dt to set.
	 */
	public void setDt(Date dt) {
		this.dt = dt;
	}

	/**
	 * @return the status
	 */
	public Clob getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Clob status) {
		this.status = status;
	}

	
	

}
