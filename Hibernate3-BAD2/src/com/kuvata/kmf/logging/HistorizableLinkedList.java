package com.kuvata.kmf.logging;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

public class HistorizableLinkedList<E> extends LinkedList<E> implements Historizable, UserCollectionType  {

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
		return ((HistorizablePersistentBag)collection).contains(obj);
	}
	
	// could be common for all collection implementations.
	public Iterator<E> getElementsIterator(Object collection) {
		return ((HistorizablePersistentBag)collection).iterator();
	}
	
	// common for list-like collections.
	public Object indexOf(Object collection, Object obj) {
		return ((HistorizablePersistentBag)collection).indexOf( obj );
	}
	// factory method for certain collection type.
	public Object instantiate() {
		return new LinkedList<E>();
	}
	
	public Object instantiate(int arg0) { 
		return new LinkedList<E>(); 
	} 	
	
	// standard wrapper for collection type.
	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
		// Use hibernate's built in persistent set implementation wrapper
		return new HistorizablePersistentBag(session);
	}
	
	public Object replaceElements(Object collectionA, Object collectionB, CollectionPersister arg2, Object arg3, Map arg4, SessionImplementor arg5) throws HibernateException { 
		LinkedList<E> listA = (LinkedList<E>) collectionA; 
		LinkedList<E> listB = (LinkedList<E>) collectionB; 
		listB.clear(); 
		listB.addAll(listA); 
		return listB; 
	} 	
	
	// standard wrapper for collection type.
	public PersistentCollection wrap(SessionImplementor session, Object colllection) {
		// Use hibernate's built in persistent set implementation wrapper.
		return new HistorizablePersistentBag(session, (LinkedList<E>)colllection);
	}
	
}
