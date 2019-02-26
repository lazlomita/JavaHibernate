/*
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.sql.Clob;
import java.util.Date;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import com.kuvata.dispatcher.scheduling.ContentSchedulerArg;
import com.kuvata.kmf.usertype.ContentSchedulerStatusType;
import com.kuvata.kmf.util.Reformat;

/**
 * Represents the app_user table in the BASE_SCHEMA.
 * 
 * @author Jeff Randesi
 */
public class ContentSchedulerStatus extends Entity {

	private Long contentSchedulerStatusId;
	private Date dt;
	private Clob args;
	private Clob status;
	private ContentSchedulerStatusType contentSchedulerStatus;
	private ContentUpdate contentUpdate;

	public ContentSchedulerStatus() 
	{		
	}
	
	public Long getEntityId()
	{
		return this.getContentSchedulerStatusId();
	}
	
	/**
	 * Returns a ContentSchedulerStatus object with the given id
	 * @param contentSchedulerStatusId
	 * @return
	 * @throws HibernateException
	 */
	public static ContentSchedulerStatus getContentSchedulerStatus(Long contentSchedulerStatusId) throws HibernateException
	{
		return (ContentSchedulerStatus)Entity.load(ContentSchedulerStatus.class, contentSchedulerStatusId);		
	}
	
	/**
	 * Retrieve the most recent content scheduler status object
	 * @return
	 * @throws HibernateException
	 */
	public static ContentSchedulerStatus getLastContentSchedulerStatus() throws HibernateException
	{
		ContentSchedulerStatus result = null;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT css FROM ContentSchedulerStatus as css "
			+"WHERE css.dt = "
			+"(SELECT MAX(css2.dt) FROM ContentSchedulerStatus as css2)";			
		Query q = session.createQuery( hql ); 
		q.setMaxResults(1);		
		List l = q.list();
		if( l != null && l.size() > 0 )
		{
			result = (ContentSchedulerStatus)l.get(0);			
		}
		return result;		
	}
	
	/**
	 * Retrieve the 10 most recent content scheduler status objects
	 * @return
	 * @throws HibernateException
	 */
	public static List getMostRecentContentSchedulerStatuses() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(css) FROM ContentSchedulerStatus as css WHERE dt > sysdate - 1";
		Long rowCount = (Long)session.createQuery(hql).iterate().next();
		
		// Return at least 10 rows
		if(rowCount >= 10){
			hql = "SELECT css FROM ContentSchedulerStatus as css WHERE dt > sysdate - 1 "
				+"ORDER BY css.contentSchedulerStatusId DESC";
			return session.createQuery( hql ).list();
		}else{
			hql = "SELECT css FROM ContentSchedulerStatus as css "
				+"ORDER BY css.contentSchedulerStatusId DESC";
			return session.createQuery( hql ).setMaxResults(10).list();
		}
	}
	
	/**
	 * Convert the serialized arguments into an array ContentSchedulerArg objects
	 * @return
	 */
	public ContentSchedulerArg[] convertArgs()
	{
		ContentSchedulerArg[] contentSchedulerArgs = null;
		if( this.getArgs() != null )
		{
			String stringArgs = Reformat.convertClobToString( this.getArgs(), true );
			ByteArrayInputStream bs = new ByteArrayInputStream( stringArgs.getBytes() );						
	        XMLDecoder decoder = new XMLDecoder( bs );
	        contentSchedulerArgs = (ContentSchedulerArg[]) decoder.readObject();
	        decoder.close();
		}
		return contentSchedulerArgs;
	}
	
	/**
	 * @return Returns the contentSchedulerStatusId.
	 */
	public Long getContentSchedulerStatusId() {
		return contentSchedulerStatusId;
	}
	/**
	 * @param contentSchedulerStatusId The contentSchedulerStatusId to set.
	 */
	public void setContentSchedulerStatusId(Long contentSchedulerStatusId) {
		this.contentSchedulerStatusId = contentSchedulerStatusId;
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
	 * @return the args
	 */
	public Clob getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(Clob args) {
		this.args = args;
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

	/**
	 * @return the contentSchedulerStatus
	 */
	public ContentSchedulerStatusType getContentSchedulerStatus() {
		return contentSchedulerStatus;
	}

	/**
	 * @param contentSchedulerStatus the contentSchedulerStatus to set
	 */
	public void setContentSchedulerStatus(
			ContentSchedulerStatusType contentSchedulerStatus) {
		this.contentSchedulerStatus = contentSchedulerStatus;
	}

	public ContentUpdate getContentUpdate() {
		return contentUpdate;
	}

	public void setContentUpdate(ContentUpdate contentUpdate) {
		this.contentUpdate = contentUpdate;
	}	
}