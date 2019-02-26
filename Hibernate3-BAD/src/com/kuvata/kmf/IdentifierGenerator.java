/*
 * Created on Aug 25, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.type.Type;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public class IdentifierGenerator implements org.hibernate.id.IdentifierGenerator, Configurable
{
	private static final String CLASS_NAME= "className";
	private String className;
	
	/**
	 * 
	 */
	public void configure(Type type, Properties params, Dialect d) throws MappingException 
	{			
		this.className = params.getProperty( CLASS_NAME );		
	}
	
	/**
	 * 
	 */
	public Serializable generate(SessionImplementor session, Object obj) throws HibernateException 
	{			
		return Entity.makeEntityId( this.className );		
	}	
}
