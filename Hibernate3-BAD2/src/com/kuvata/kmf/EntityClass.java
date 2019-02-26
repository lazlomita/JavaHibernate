package com.kuvata.kmf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;



/**
 * 
 * 
 * Persistent class for table ENTITY_CLASS.
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class EntityClass {

	private Long entityClassId;
	private String className;
	private Set attrDefinitions = new HashSet();
	private Set entityInstances = new HashSet();
	private static HashMap<String, EntityClass> entityClasses = null;
	
	/**
	 * Constructor  
	 */
	public EntityClass()
	{				
	}
	/**
	 * 
	 * @param className
	 */
	public EntityClass(String className)
	{
		this.className = className;
	}
	
	/**
	 * Attempts to retrieve an EntityClass object with the given className
	 * @return	Returns a persistent EntityClass if found or null
	 */
	public static EntityClass getEntityClass(String className) throws HibernateException
	{
		// Return the entity class object associated with the given className
		return entityClasses.get( className );		
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static synchronized void loadEntityClasses()
	{
		// If another thread was waiting outside the sync block while one was inside,
		// we need to make sure that we don't re-do the following
		if(entityClasses == null){
			// We want to execute the following code only on the server side.
			// We will determine this by looking up the heartbeat interval in kuvata.properties
			String heartbeatInterval = null;
			
			// If we are on the server
			if(heartbeatInterval == null){
				/*
				 * Load up the entityClasses hash map with all entries in the entity_class table
				 * This will allow us to avoid making a trip to the database everytime we 
				 * need to locate an EntityClass by className
				 */
				try {
					Session session = HibernateSession.currentSession();			
					List<EntityClass> l = session.createQuery("FROM EntityClass").list();
					entityClasses = new HashMap<String, EntityClass>();
					for( EntityClass entityClass : l )
					{
						entityClasses.put( entityClass.getClassName(), entityClass );
					}
				} catch (Exception e) {
					// We will ignore this error since we could be on a device.
				}
			}else{
				// Set this property on the devices to avoid future calls to this method from this JVM.
				entityClasses = new HashMap<String, EntityClass>();
			}
		}
	}
	
	public static synchronized void reloadEntityClasses(){
		/*
		 * Load up the entityClasses hash map with all entries in the entity_class table
		 * This will allow us to avoid making a trip to the database everytime we 
		 * need to locate an EntityClass by className
		 */
		try {
			Session session = HibernateSession.currentSession();			
			List<EntityClass> l = session.createQuery("FROM EntityClass").list();
			entityClasses = new HashMap<String, EntityClass>();
			for( EntityClass entityClass : l )
			{
				entityClasses.put( entityClass.getClassName(), entityClass );
			}
		} catch (Exception e) {
			// We will ignore this error since we could be on a device.
		}
	}
	
	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return Returns the entityClassId.
	 */
	public Long getEntityClassId() {
		return entityClassId;
	}

	/**
	 * @param entityClassId The entityClassId to set.
	 */
	public void setEntityClassId(Long entityClassId) {
		this.entityClassId = entityClassId;
	}

	/**
	 * @return Returns the attrDefinitions.
	 */
	public Set getAttrDefinitions() {
		return attrDefinitions;
	}

	/**
	 * @param attrDefinitions The attrDefinitions to set.
	 */
	public void setAttrDefinitions(Set attrDefinitions) {
		this.attrDefinitions = attrDefinitions;
	}

	/**
	 * @return Returns the entityInstances.
	 */
	public Set getEntityInstances() {
		return entityInstances;
	}

	/**
	 * @param entityInstances The entityInstances to set.
	 */
	public void setEntityInstances(Set entityInstances) {
		this.entityInstances = entityInstances;
	}
}
