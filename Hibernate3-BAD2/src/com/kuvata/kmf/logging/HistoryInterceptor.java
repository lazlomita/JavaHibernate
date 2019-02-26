/*
 * Created on Sep 27, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.type.Type;

import com.kuvata.kmf.AssetPresentation;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public class HistoryInterceptor implements Interceptor {

	private Session session;
	private Integer userId;
	private String program;
	private String userName;
	private Set<Historizable> inserts = new HashSet<Historizable>();
	private HashMap<Integer, Object[]> updates = new HashMap<Integer, Object[]>();
	private HashMap<String, HistorizedCollection> collectionUpdates = new HashMap<String, HistorizedCollection>();
	private Set<Historizable> deletes = new HashSet<Historizable>();
	private static final ArrayList<String> insertsToIgnore = new ArrayList<String>();
	private static final ArrayList<String> deletesToIgnore = new ArrayList<String>();
	
	static
	{
		// Populate the collection of inserts and deletes to ignore
		insertsToIgnore.add( AssetPresentation.class.getName() );
		deletesToIgnore.add( AssetPresentation.class.getName() );
	}
	
	/**
	 * Create a new HistoryInterceptor, recording the changes under the given program and userName
	 * @param program
	 * @param userName
	 */
	public HistoryInterceptor(String program, String userName) 
	{
		this.program = program;
		this.userName = userName;
	}		

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.Interceptor#onCollectionUpdate(java.lang.Object, java.io.Serializable)
	 */
	public void onCollectionUpdate(Object collection, Serializable key)
	{
		if( collection instanceof HistorizablePersistentBag || collection instanceof HistorizablePersistentSet )
		{
			// If the collection is either a ... it will be a persistent collection
			PersistentCollection persistentCollection = (PersistentCollection)collection;						
			String fullKey = key +"_"+ persistentCollection.getRole();
			HistorizedCollection historizedCollection = new HistorizedCollection( persistentCollection, key.toString(), collectionUpdates.get( fullKey ) );				
			collectionUpdates.put( fullKey,  historizedCollection );
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.hibernate.Interceptor#postFlush(java.util.Iterator)
	 */
    public void postFlush(Iterator iterator) 
	{
        try 
		{   	
			// Log the insert events 
			for (Iterator<Historizable> it = inserts.iterator(); it.hasNext();) 
			{
				Historizable entity = it.next();
				HistoryEntry.logEvent( HistoryEntry.ACTION_CREATE, entity, this.program, this.userName );
			}
			
			// Log the update events
			for (Iterator<Object[]> it = updates.values().iterator(); it.hasNext();) 
			{
				Object[] obj = it.next();			
				Historizable entity = (Historizable) obj[0];
				Object[] newValues = (Object[]) obj[1];
				Object[] oldValues = (Object[]) obj[2];
				Object[] propertyName = (Object[]) obj[3];
				HistoryEntry.logEvent( HistoryEntry.ACTION_UPDATE, entity, newValues, oldValues, propertyName, this.program, this.userName );
			}
			
			// Log the delete events
			for (Iterator<Historizable> it = deletes.iterator(); it.hasNext();) 
			{
				Historizable entity = it.next();
				HistoryEntry.logEvent( HistoryEntry.ACTION_DELETE, entity, this.program, this.userName );
			}
			
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
			inserts.clear();
			updates.clear();
			deletes.clear();
        } 
    } 
    
    /*
     * (non-Javadoc)
     * @see org.hibernate.Interceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    public boolean onSave(Object object, Serializable serializable, Object[] obj2, String[] str, Type[] type) 
    { 
		if( object instanceof Historizable ) 
		{
			// If the class associated with this historizable is not in our collection of insertsToIgnore
			Historizable historizable = (Historizable)object;
			if( insertsToIgnore.contains( historizable.getClass().getName() ) == false )
			{
				// Save it to the collection of inserts to log
				inserts.add( historizable );		             
			}
        } 
        return false; 
    } 
    
    /*
     * (non-Javadoc)
     * @see org.hibernate.Interceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    public boolean onFlushDirty(Object object, Serializable serializable, Object[] newValues, Object[] oldValues, String[] properties, Type[] type) 
	{ 	
		if (object instanceof Historizable) 
		{ 			
			Object[] obj = new Object[4];
			obj[0] = (Historizable) object;
			obj[1] = newValues;
			obj[2] = oldValues;			
			obj[3] = properties;	
			
			// Since we cannot rely on using the hashcode of an object array,
			// sum the hashcode of each property within the newValues array (ignoring collections).
			int hashCode = object.hashCode();
			for( int i=0; i<newValues.length; i++)
			{
				Object newOne = (Object)newValues[i];
				if (newOne instanceof PersistentCollection) {
					continue;
				}				
				hashCode += newOne != null ? newOne.hashCode() : 0;				
			}
			updates.put( hashCode, obj );		             
        } 
        return false; 
    } 
    
    /*
     * (non-Javadoc)
     * @see org.hibernate.Interceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    public void onDelete(Object object, Serializable serializable, Object[] state, String[] properties, Type[] type) 
	{ 
		if (object instanceof Historizable) 
		{ 			
			// If the class associated with this historizable is not in our collection of deletesToIgnore
			Historizable historizable = (Historizable)object;
			if( deletesToIgnore.contains( historizable.getClass().getName() ) == false )
			{
				// Save it to the collection of deletes to log
				deletes.add( historizable );		             
			}					             
        } 
    } 
	
    /*
     * (non-Javadoc)
     * @see org.hibernate.Interceptor#beforeTransactionCompletion(org.hibernate.Transaction)
     */
	public void beforeTransactionCompletion(Transaction arg0)
	{
        try 
		{			
			// Log the update events
			for( HistorizedCollection historizedCollection : collectionUpdates.values() ) 
			{
				// Each historizedCollection should either have newValue OR oldValue populated,
				// which will tell us whether a collection was added to, or removed from.
				String newValue = historizedCollection.getNewValue();
				String oldValue = historizedCollection.getOldValue();
				if( newValue.length() > 0 ){
					HistoryEntry.logEvent("Added to", historizedCollection, newValue, oldValue, historizedCollection.getPropertyName(), this.program, this.userName);	
				}else if( oldValue.length() > 0 ){
					HistoryEntry.logEvent("Removed from", historizedCollection, newValue, oldValue, historizedCollection.getPropertyName(), this.program, this.userName);					
				}				
			}			
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
        	collectionUpdates.clear();
        } 
	}
	
	/**
	 * 
	 * @param session
	 */
	public void setSession(Session session) {
		this.session=session;
	}	
	
	/*
	 * No custom implementation
	 */
	public String onPrepareStatement(String sql){
		return sql;
	}
	public String getEntityName(Object arg0){
		return null;
	}		
	public void onCollectionRemove(Object collection, Serializable key){
	}
	public void onCollectionRecreate(Object collection, Serializable key){
	}
	public void beforeTransactionBegin(Transaction arg0){		
	}
	public void afterTransactionBegin(Transaction arg0){		
	}
	public void afterTransactionCompletion(Transaction arg0){		
	}	
	public Object getEntity(String arg0, Serializable arg1){
		return null;
	}
	public int[] findDirty(Object obj, Serializable id, Object[] newValues, Object[] oldValues, String[] properties, Type[] types) {
	  return null;
	}
	public boolean onLoad(Object obj, Serializable id, Object[] values, String[] properties, Type[] types) throws CallbackException {
	  return false;
	}	
	public Object instantiate(String arg0, EntityMode arg1, Serializable arg2) throws CallbackException {
	  return null;
	}
	public Boolean isTransient(Object arg0) {
	  return null;
	}
	public void preFlush(Iterator it) throws CallbackException {
	}		
}


