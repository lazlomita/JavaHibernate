package com.kuvata.kmf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.kuvata.kmf.util.Reformat;


/**
 * 
 * 
 * Persistent class for table ENTITY_INSTANCE.
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class EntityInstance {

	private Long entityId;
	private Long appUserId;
	private Date lastModified;
	private EntityClass entityClass;	
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		
	/**
	 * Constructor
	 */
	public EntityInstance()
	{		
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof EntityInstance) ) result = false;
		
		EntityInstance ei = (EntityInstance) other;		
		if(this.hashCode() == ei.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "EntityInstance".hashCode();
		result = Reformat.getSafeHash( this.getEntityId(), result, 13 );
		result = Reformat.getSafeHash( this.getEntityClass().getEntityClassId(), result, 13 );
		return result;
	}	
	
	/**
	 * Retrieves a persistent EntityInstance object for the given entityId
	 * 
	 * @param entityId
	 * @return
	 * @throws HibernateException
	 */
	public static EntityInstance getEntityInstance(Long entityId) throws HibernateException
	{ 		
		Session session = HibernateSession.currentSession();		
		Iterator iter = session.createQuery("from EntityInstance ei where ei.id=?").setParameter(0, entityId).iterate(); 
		EntityInstance result = iter.hasNext() ? (EntityInstance) iter.next() : null;
		Hibernate.close( iter );
		return result;
	}
	
	/**
	 * @return Returns the formattedTimestamp.
	 */
	public String getLastModifiedFormatted() {
		String result = "";
		if( this.getLastModified() != null ){
			result = dateFormat.format( this.getLastModified() );
		}
		return result;
	}

	/**
	 * @return Returns the entityClass.
	 */
	public EntityClass getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass The entityClass to set.
	 */
	public void setEntityClass(EntityClass entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return Returns the entityId.
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * @param entityId The entityId to set.
	 */
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}
	/**
	 * @return Returns the appUserId.
	 */
	public Long getAppUserId() {
		return appUserId;
	}
	
	/**
	 * @param appUserId The appUserId to set.
	 */
	public void setAppUserId(Long appUserId) {
		this.appUserId = appUserId;
	}
	/**
	 * @return Returns the lastModified.
	 */
	public Date getLastModified() {
		return lastModified;
	}
	
	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
}
