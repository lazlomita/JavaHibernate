package com.kuvata.kmf.logging;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

public class HistorizableLinkedSet<E> extends LinkedHashSet<E> implements Historizable, UserCollectionType  {

	// This class implements historizable so that we know to save history for this collection,
	// however we will never use the getters when logging history -- we will be logging the history
	// of the historizable entities within this collection
	public Long getEntityId(){
		return null;
	}
	public String getEntityName(){
		return null;
	}	
 
	// could be common for all collection implementations.
	public boolean contains(Object collection, Object obj) {
		return ((HistorizablePersistentSet)collection).contains(obj);
	}
	
	// could be common for all collection implementations.
	public Iterator<E> getElementsIterator(Object collection) {
		return ((HistorizablePersistentSet)collection).iterator();
	}
	
	// common for list-like collections.
	public Object indexOf(Object collection, Object obj) {
		return null;
	}
	// factory method for certain collection type.
	public Object instantiate() {
		return new LinkedHashSet<E>();
	}
	
	public Object instantiate(int arg0) { 
		return new LinkedHashSet<E>(); 
	} 	
	
	// standard wrapper for collection type.
	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
		// Use hibernate's built in persistent set implementation wrapper
		return new HistorizablePersistentSet(session);
	}
	
	public Object replaceElements(Object collectionA, Object collectionB, CollectionPersister arg2, Object arg3, Map arg4, SessionImplementor arg5) throws HibernateException { 
		LinkedHashSet<E> listA = (LinkedHashSet<E>) collectionA; 
		LinkedHashSet<E> listB = (LinkedHashSet<E>) collectionB; 
		listB.clear(); 
		listB.addAll(listA); 
		return listB; 
	} 	
	
	// standard wrapper for collection type.
	public PersistentCollection wrap(SessionImplementor session, Object colllection) {
		// Use hibernate's built in persistent set implementation wrapper.
		return new HistorizablePersistentSet(session, (LinkedHashSet<E>)colllection);
	}
	
}
