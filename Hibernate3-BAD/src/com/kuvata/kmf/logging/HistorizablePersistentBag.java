package com.kuvata.kmf.logging;

import java.util.Collection;

import org.hibernate.collection.PersistentBag;
import org.hibernate.engine.SessionImplementor;

public class HistorizablePersistentBag extends PersistentBag {

	public HistorizablePersistentBag(SessionImplementor session){
		super( session );
	}
	public HistorizablePersistentBag(SessionImplementor session, Collection collection){
		super( session, collection );
	}
}
