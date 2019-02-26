package com.kuvata.kmf.logging;

import java.util.Set;

import org.hibernate.collection.PersistentSet;
import org.hibernate.engine.SessionImplementor;

public class HistorizablePersistentSet extends PersistentSet {

	public HistorizablePersistentSet(SessionImplementor session){
		super( session );
	}
	public HistorizablePersistentSet(SessionImplementor session, Set set){
		super( session, set );
	}
}
