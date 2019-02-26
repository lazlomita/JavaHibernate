package com.kuvata.kmf;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.KMFLogger;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * This is a copy of the Entity class. We do not want KMF classes to extend Entity.
 * 
 * @author Jeff Randesi
 */
public abstract class KmfEntity {
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( KmfEntity.class );
	public abstract Long getEntityId();
	
	/**
	 * Constructor
	 */
	public KmfEntity()
	{		
	}
	
	/**
	 * Method used by KMF's custom IdentifierGenerator class to generate
	 * a unique identifier based on the entity_seq sequence.
	 * 
	 * For each instance of a KMF entity, this method creates a new record
	 * in the ENTITY_INSTANCE table and returns the newly created entity id.
	 * 
	 * @param className
	 * @return
	 * @throws HibernateException
	 */
	public static Long makeEntityId(String className) throws HibernateException
	{
		return Entity.makeEntityId( className );		
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Long save() throws HibernateException
	{		
		Session session = HibernateSession.currentSession();	
		HibernateSession.beginTransaction();
		Long newId = null;
		try {			
			newId = (Long)session.save( this );								
			HibernateSession.commitTransaction();
		} catch (HibernateException e) {			
			// Rollback the transaction if an error occurred
			HibernateSession.rollbackTransaction();
			logger.error( e );
		}
		return newId;
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void update() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		HibernateSession.beginTransaction();
		try {			
			session.update( this );			
			HibernateSession.commitTransaction();	
		} catch (HibernateException e) {			
			// Rollback the transaction if an error occurred
			HibernateSession.rollbackTransaction();
			logger.error( e );
		}			
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public void delete() throws HibernateException
	{		
		delete(this);		
	}
	
	/**
	 * 
	 * @param e
	 * @throws HibernateException
	 */
	private void delete(KmfEntity e) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		HibernateSession.beginTransaction();
		try 
		{				
			// Delete this entity
			session.delete( e );
			HibernateSession.commitTransaction();
		} catch (HibernateException ex) {			
			// Rollback the transaction if an error occurred
			HibernateSession.rollbackTransaction();
			logger.error( ex );
		}			
	}
	
	/**
	 * Returns an Entity object with the given entityId
	 * 
	 * @param entityId
	 * @return
	 * @throws Exception
	 */
	public static KmfEntity load(Class c, Long entityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();						
		KmfEntity result = (KmfEntity)session.createCriteria( c )
			.add( Expression.eq( "id", entityId ) )
			.uniqueResult();		
		return result; 			
	}
}
