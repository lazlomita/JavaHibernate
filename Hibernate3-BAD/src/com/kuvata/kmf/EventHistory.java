/*
 * Created on Sep 27, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import parkmedia.KMFLogger;
import parkmedia.usertype.EventType;


/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public class EventHistory extends PersistentEntity
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( EventHistory.class );
	private static final int MAX_EVENT_HISTORY = 7; // days
	private Long eventHistoryId;
	private EventType eventType;
	private Long deviceId;
	private Date eventDt;
	private String eventDetails;

	/**
	 * 
	 *
	 */
	public EventHistory() 
	{
	}
	
	/**
	 * Creates a row in the event_history table based on the given parameters
	 * 
	 * @param eventType
	 * @param eventDt
	 * @param deviceId
	 */
	public static void create(EventType eventType, Date eventDt, Long deviceId, String eventDetails)
	{
		EventHistory eventHistory = new EventHistory();
		eventHistory.setEventType( eventType );
		eventHistory.setEventDt( eventDt );
		eventHistory.setDeviceId( deviceId );
		eventHistory.setEventDetails(eventDetails);
		eventHistory.save();
	}
	
	public static List<EventHistory> getEventHistory(Device device)
	{				
		String hql = "SELECT eventHistory FROM EventHistory eventHistory "
			+ "WHERE eventHistory.deviceId = :deviceId "
			+ "AND eventHistory.eventDt >= :eventDt "
			+ "ORDER BY eventHistory.eventDt DESC";
		
		// Get all events for the past 7 days
		Calendar c = Calendar.getInstance();
		c.set( Calendar.HOUR_OF_DAY, 0);
		c.add( Calendar.DATE, -MAX_EVENT_HISTORY);
		
		Session session = HibernateSession.currentSession();
		List<EventHistory> l = session.createQuery( hql )
			.setParameter("deviceId", device.getDeviceId())
			.setParameter("eventDt", c.getTime())
			.list();		
		return l;
	}
				
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getEventHistoryId();
	}

	/**
	 * @return the eventHistoryId
	 */
	public Long getEventHistoryId() {
		return eventHistoryId;
	}

	/**
	 * @param eventHistoryId the eventHistoryId to set
	 */
	public void setEventHistoryId(Long eventHistoryId) {
		this.eventHistoryId = eventHistoryId;
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the deviceId
	 */
	public Long getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the eventDt
	 */
	public Date getEventDt() {
		return eventDt;
	}

	/**
	 * @param eventDt the eventDt to set
	 */
	public void setEventDt(Date eventDt) {
		this.eventDt = eventDt;
	}

	public String getEventDetails() {
		return eventDetails;
	}

	public void setEventDetails(String eventDetails) {
		this.eventDetails = eventDetails;
	}	
	
	
	
}
