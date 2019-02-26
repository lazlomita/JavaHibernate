package com.kuvata.kmf;

import java.util.Iterator;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import com.kuvata.kmf.logging.HistorizableCollectionMember;

/**
 *
 * 
 * @author Jeff Randesi
 * Created on Sep. 15, 2005
 * Copyright 2005, Kuvata, Inc.
 */
public class AlertUser extends Entity implements HistorizableCollectionMember
{
	private Long alertUserId;
	private Alert alert;	
	private Long userId;	
		
	/**
	 * 
	 *
	 */
	public AlertUser()
	{		
	}	
		
	/**
	 * Removes all records that are associated with the given userId
	 * @param userId
	 */
	public static void removeUser(Long userId)
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(AlertUser.class)
					.add( Expression.eq("userId", userId) )
					.list();
		for( Iterator i = l.iterator(); i.hasNext(); )
		{
			AlertUser au = (AlertUser)i.next();
			au.delete();
			i.remove();
		}
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAlertUserId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getEntityId();
	}		
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getUserId().toString();
	}	
	/**
	 * @return Returns the alert.
	 */
	public Alert getAlert() {
		return alert;
	}
	/**
	 * @param alert The alert to set.
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
	}
	/**
	 * @return Returns the alertUserId.
	 */
	public Long getAlertUserId() {
		return alertUserId;
	}
	/**
	 * @param alertUserId The alertUserId to set.
	 */
	public void setAlertUserId(Long alertUserId) {
		this.alertUserId = alertUserId;
	}
	/**
	 * @return Returns the userId.
	 */
	public Long getUserId() {
		return userId;
	}
	/**
	 * @param userId The userId to set.
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
