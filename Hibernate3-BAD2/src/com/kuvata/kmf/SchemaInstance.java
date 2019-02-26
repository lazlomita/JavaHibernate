/*
 * Created on Jul 19, 2004
 * Copyright 2004, Oooo.TV, Inc.
 */
package com.kuvata.kmf;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jmx.StatisticsService;

import com.kuvata.kmf.logging.HistoryInterceptor;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public class SchemaInstance {

	private SessionFactory sessionFactory;
	private String schemaName;
	private boolean recordHistory;
	/**
	 * 
	 * @param schemaName
	 * @param configFilePath
	 * @throws HibernateException
	 */
	public SchemaInstance(String schemaName, String configFilePath) throws HibernateException
	{		
		// Don't get from JNDI, use a static SessionFactory
		if (sessionFactory == null) 
		{		       
			// Load the given hibernate configuration file
			sessionFactory = new Configuration().configure(new File( configFilePath )).buildSessionFactory();
			
			/*
			 * Register the hibernate StatisticsServer as an MBean
			 * if an MBean server is available
			 */
			try 
			{
				// If we can get a reference to an MBean server
				ArrayList<MBeanServer> list = MBeanServerFactory.findMBeanServer( null );			
				if( list != null && list.size() > 0 )
				{
					// Look for the server named "jboss"
					for( Iterator<MBeanServer> i=list.iterator(); i.hasNext(); )
					{
						MBeanServer server = i.next();
						if( server != null && server.getDefaultDomain() != null && server.getDefaultDomain().equalsIgnoreCase("jboss") )
						{
							// Build the MBean name based on the configFilePath
							String mbeanName = "Hibernate:type=statistics,application="+ configFilePath.substring( configFilePath.lastIndexOf("/") + 1);
							System.out.println("Registering MBean: "+ mbeanName);
							ObjectName on = new ObjectName( mbeanName );
							StatisticsService mBean = new StatisticsService();
							mBean.setSessionFactory( sessionFactory );
							server.registerMBean(mBean, on);		
							break;
						}						
					}					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		this.setSchemaName( schemaName );
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Session getHibernateSession() throws HibernateException
	{
		Session s;
		if (recordHistory == true)
		{
			String username = "";
			HistoryInterceptor interceptor = new HistoryInterceptor( SchemaDirectory.getProgram(), username );
			s = sessionFactory.openSession( interceptor );			
			interceptor.setSession( s );
		}
		else
		{		
			s = sessionFactory.openSession();
		}
		return s;
	}
	
	/**
	 * @return Returns the schemaName.
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName The schemaName to set.
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return Returns the sessionFactory.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param recordHistory The recordHistory to set.
	 */
	public void setRecordHistory(boolean recordHistory) {
		this.recordHistory = recordHistory;
	}

	/*
	 * @return path to the playback_events directory
	 */
	 public String getPlaybackEventsDir()
	 {
		 return "/schemas/" + SchemaDirectory.getSchema().getSchemaName() + "/playback_events";
	 }
	 
	 /*
	  * @return path to the uploads directory
	  */
	 public String getUploadsDir()
	 {
		 return "/schemas/" + SchemaDirectory.getSchema().getSchemaName() + "/uploads";
	 }
	 
	 public String getHwInfoDir()
	 {
		 return "/schemas/" + SchemaDirectory.getSchema().getSchemaName() + "/hardware_infos";
	 }
}
