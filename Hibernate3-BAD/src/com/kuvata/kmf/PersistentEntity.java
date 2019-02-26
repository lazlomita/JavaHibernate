package com.kuvata.kmf;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

public class PersistentEntity {

	public Long save(){
		Session session = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		Long newId = null;
		try{
			newId = (Long)session.save( this );			
			if( HibernateSession.getBulkmode() == false ){
					HibernateSession.commitTransaction();
			}
		} catch (HibernateException e) {
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true ){
				HibernateSession.rollbackBulkmode();
			}else{
				HibernateSession.rollbackTransaction();
			}
			throw e;
		}
		return newId;
	}
	
	public void update() throws HibernateException{
		Session session = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		try {
			session.update( this );		
			
			if( HibernateSession.getBulkmode() == false ){	
				HibernateSession.commitTransaction();
			}					
		} catch (HibernateException e) {			
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true ){
				HibernateSession.rollbackBulkmode();
			}else{
				HibernateSession.rollbackTransaction();
			}
			throw e;
		}			
	}
	
	public static PersistentEntity load(Class c, Long entityId) throws HibernateException{
		Session session = HibernateSession.currentSession();						
		PersistentEntity result = (PersistentEntity)session.createCriteria( c )
			.add( Expression.eq( "id", entityId ) )
			.uniqueResult();		
		return result; 			
	}
	
	public void delete(){
		Session session = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		try {
			session.delete( this );		
			
			if( HibernateSession.getBulkmode() == false ){	
				HibernateSession.commitTransaction();
			}					
		} catch (HibernateException e) {			
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true ){
				HibernateSession.rollbackBulkmode();
			}else{
				HibernateSession.rollbackTransaction();
			}
			throw e;
		}
	}
}
