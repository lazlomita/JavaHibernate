package com.kuvata.kmf;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import parkmedia.KMFLogger;


/**
 * Controls the Hibernate Session
 * 
 * @author Jeff Randesi
 */
public class HibernateSession{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( HibernateSession.class );	
	private static ThreadLocal session = new ThreadLocal(); 
	private static ThreadLocal bulkmode = new ThreadLocal();
	private static ThreadLocal txCount = new ThreadLocal();
	private static ThreadLocal tx = new ThreadLocal();
	
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static Session currentSession() throws HibernateException
	{		
		if(session.get() == null)
		{			
			Session s = SchemaDirectory.getSchema().getHibernateSession();
			session.set( s );
			bulkmode.set( new Boolean( false ) );			
		}				
		return (Session) session.get();
	}

	/**
	 * 
	 * @throws HibernateException
	 */
	public static void closeSession() throws HibernateException
	{
		Session s = (Session) session.get();
		if (s != null) 
		{
			try{
				if( getBulkmode() == true ){
					stopBulkmode();
				}
			}catch(Exception e){
				logger.error( e );
			}finally{
				s.close();
				session.set( null );
				bulkmode.set( null );	
			}			
		}
	}
	
	public static boolean getBulkmode(){
		Boolean b = (Boolean) bulkmode.get();
		return b.booleanValue();
	}
	
	public static Transaction getTx(){
		return (Transaction)tx.get();
	}
	
	public static Integer getTxCount(){
		return (Integer)txCount.get();
	}
	
	public static void setTxCount(Integer value){
		txCount.set(value);
	}
	
	public static void startBulkmode()
	{
		HibernateSession.currentSession();
		if( getBulkmode() == false )
		{
			bulkmode.set( new Boolean(true) );
			HibernateSession.beginTransaction();;						
		}		
	}
	
	public static void stopBulkmode()
	{
		HibernateSession.currentSession();
		if( getBulkmode() == true )
		{
			bulkmode.set( new Boolean(false) );
			HibernateSession.commitTransaction();
		}		
	}
	
	public static void rollbackBulkmode()
	{
		HibernateSession.currentSession();
		if( getBulkmode() == true )
		{
			bulkmode.set( new Boolean(false) );
			HibernateSession.rollbackTransaction();
		}		
			
	}
	
	// Start the transaction
	public static void beginTransaction(){
		// If the transaction is null or has already been commited
		if(txCount.get() == null || (Integer)txCount.get() <= 0){
			// Start a new transaction
			tx.set(HibernateSession.currentSession().beginTransaction());
			txCount.set(new Integer(1));
		}else{
			// Increment transaction count
			txCount.set( (Integer)txCount.get() + 1);
		}
	}

	// Commit the transaction
	public static void commitTransaction(boolean bubbleException) throws HibernateException{
		HibernateException exceptionToThrow = null;
		
		// If the transaction count is 1
		if(txCount.get() != null && (Integer)txCount.get() == 1){
			// Commit
			if(tx.get() != null){
				try {
					((Transaction)tx.get()).commit();
				} catch (HibernateException e) {
					if(bubbleException == false){
						logger.error("An unexpected error occurred while committing the transaction: "+ e.getMessage(), e);
						HibernateSession.rollbackTransaction();
					}else{
						// We are not going to roll back this transaction since the only case
						// where bubble exceptions is true is when we start a new transaction.
						exceptionToThrow = e;
						
						// If this fixes the connection leak issue, great. otherwise get rid of the rollback.
						HibernateSession.rollbackTransaction();
					}
				}
			}				
			txCount.set(new Integer(0));
		}else{
			// Decrement transaction count
			txCount.set( (Integer)txCount.get() - 1);
		}
		
		if(exceptionToThrow != null){
			throw exceptionToThrow;
		}
	}
	
	public static void commitTransaction(){
		try {
			commitTransaction(false);
		} catch (Exception e) {
			// We don't really expect an exception here
			logger.error("An unexpected error occurred while committing the transaction: "+ e.getMessage(), e);
		}
	}
	
	// Rollback the transaction
	public static void rollbackTransaction(){
		if(txCount.get() != null && (Integer)txCount.get() > 0){
			if(tx.get() != null){
				((Transaction)tx.get()).rollback();
			}
			txCount.set(new Integer(0));
		}
	}
	
	/**
	 * Helper method to clear the hibernate cache. Admitedly it is strange that
	 * we would execute a transaction.commit() here, but have not found
	 * a more reliable way to get the desired behavior.
	 * 
	 * We have tried the following without success:
	 * HibernateSession.currentSession.clear();
	 * HibernateSession.currentSession.flush();
	 * SchemaDirectory.getSchema().getSessionFactory().evictQueries();
	 * SchemaDirectory.getSchema().getSessionFactory().evict( DeviceCommand.class );
	 */
	public static void clearCache()
	{		
		Session session = HibernateSession.currentSession();
		HibernateSession.beginTransaction();
		HibernateSession.commitTransaction();
		HibernateSession.closeSession();
		session = HibernateSession.currentSession();
	}
	
	/**
	 * Continue executing the given hql update statement in batches of 1000
	 * until there are no more "ids" to run for. 
	 * NOTE: we are expecting an hql string containing an IN clause, with a 
	 * bound parameter named "ids". 
	 * @param hql
	 * @param ids
	 */
	public static void executeBulkUpdate(String hql, List idsToUpdate)
	{
		Session sess = HibernateSession.currentSession();
		Query q = sess.createQuery(hql);
		
		// Make a copy of the ids so that manipulating it does not affect other processing
		List ids = new ArrayList(idsToUpdate);
		int toIndex = 0;
		while( ids.size() > 0 )
		{
			if( ids.size() > Constants.MAX_NUMBER_EXPRESSIONS_IN_LIST ){
				toIndex = Constants.MAX_NUMBER_EXPRESSIONS_IN_LIST;				
			}else{
				toIndex = ids.size();
			}	
			try {
				// NOTE: We are expecting the given hql statement to have a bound parameter named "ids"
				q.setParameterList("ids", ids.subList( 0, toIndex ));
				q.executeUpdate();
			} catch (HibernateException e) {
				logger.error( e );
			} finally 
			{
				// Make sure we're always removing from the list -- even if the update statement fails 
				ids.subList( 0, toIndex ).clear();
			}
		}				
	}
	
	
}
