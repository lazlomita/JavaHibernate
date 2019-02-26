/*
 * Created on Jul 19, 2004
 * Copyright 2004, Oooo.TV, Inc.
 */
package com.kuvata.kmf;

import java.io.File;
import java.util.HashMap;

import org.hibernate.HibernateException;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;


import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.XPath;

public class SchemaDirectory {
	
	public static final String KUVATA_SCHEMA = "kuvata";
	public static HashMap<String, SchemaInstance> schemas = null;
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( SchemaDirectory.class );
	private static final ThreadLocal<String> schemaName = new ThreadLocal<String>();
	private static ThreadLocal<String> program = new ThreadLocal<String>();
	private static boolean recordhistory = true;
	private static boolean initializedKuvataSchema = false;
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void loadSchemas() throws HibernateException
	{		
		try
		{			
			/*
			 * If the schemas have not yet been initialized
			 * NOTE: We are checking for null schemas here outside of the synchronized block so that the 
			 * majority of the time we will not have to introduce a synchronization
			 */
			if(SchemaDirectory.schemas == null || SchemaDirectory.schemas.size() == 0 )
			{
				// Synchronize the loading of schemas in order to ensure that loadSchemas() only occurs once per JVM
				synchronized( SchemaDirectory.class )
				{
					/*
					 * Since we are not synchronizing the initial check on SchemaDirectory.schemas (above),
					 * it it possible that when we get here, schemas is not null, so we will check again
					 */
					if(SchemaDirectory.schemas == null || SchemaDirectory.schemas.size() == 0 )
					{
						HashMap<String, SchemaInstance> schemas = new HashMap<String, SchemaInstance>();
						Document doc = new Document(new File(KuvataConfig.getSchemaDirectoryCfgPath()) );		
						Elements es = doc.getElements(new XPath("//schemas/schema"));
						
						while(es.hasMoreElements())
						{
							Element e = es.next();
							String strSchemaName = e.getAttribute("name");
							String configFile = e.getAttribute("hibernate-cfg");
							
							SchemaInstance s = new SchemaInstance(strSchemaName, configFile);
							s.setRecordHistory(recordhistory);
							schemas.put( strSchemaName, s );				
						}
						SchemaDirectory.schemas = schemas;		
					}
				}
			}
		}
		catch(Exception e){
			logger.error( e );
		}
	}
	
	// This is a helper method to allow initialization of kuvata schema only once per JVM
	// This method should only be used on the device side
	public static void initUninitializedSchema(String programName){
		// If we have not initialized the kuvata schema yet
		if(initializedKuvataSchema == false){
			// This must be a command line program since we haven't yet initialized the kuvata schema
			initialize(KUVATA_SCHEMA, programName, null, false, true);
		}else{
			// Set the schema name since this is a thread local variable and needs to be set for every new thread
			SchemaDirectory.schemaName.set( KUVATA_SCHEMA );
		}
	}
	
	public static void initialize(String schemaName, String programName, String appUser, boolean recordHistory, boolean isCommandLine)
	{
		if( isCommandLine ){
			if(KMFLogger.isCommandLine == false){
				logger.info("isCommandLine flag is being flipped to true by " + programName);
			}
			KMFLogger.isCommandLine = true;
			KuvataConfig.setSchemaDirectoryCfgFile( KuvataConfig.getPropertyValue("CommandlineSchemaDirectoryCfgFile") );
		}
		SchemaDirectory.setRecordHistory( recordHistory );
		SchemaDirectory.schemaName.set( schemaName );
		SchemaDirectory.program.set(programName);
		
		try {
            String setupClass = KuvataConfig.getPropertyValue("Application.setupClass");
            if( setupClass != null ) {
                Class loadSetupClass = Class.forName(setupClass);
            }					
		} catch(KmfException kmfe) {
			//logger.debug("Could not retrieve property \"Application.setupClass\". Continuing.");
		} catch(ClassNotFoundException e){
			logger.error( e );
		}
		
		// If an appUser was passed in, attempt to create a KmfSession object
		if( appUser != null ) {
			KmfSession.setKmfSession( KmfSession.create( appUser ) ) ;	
		}else{
			/*
			 * Explicitly clear out the kmfSession thread local variable
			 * This is required because even though the thread may be released back to the thread pool,
			 * the ThreadLocal variables associated with that thread may not be cleared out.
			 */
			KmfSession.setKmfSession( null );
		}
		
		// Set the flag if we just initialized the kuvata schema
		if(schemaName != null && schemaName.equals(KUVATA_SCHEMA)){
			initializedKuvataSchema = true;
			
			// Due to an unknown bug with Entity Classes, we can't dynamically load entity classes while creating an object (mostly ones with foreign keys).
			// We will load entity classes here to prevent the above.
			EntityClass.loadEntityClasses();
		}
	}	
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static String getSchemaBaseDirectory() throws HibernateException
	{		
		return KuvataConfig.getKuvataHome() +
				"/"+ Constants.SCHEMAS +
				"/"+ SchemaDirectory.getSchema().getSchemaName();													
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static SchemaInstance getSchema() throws HibernateException
	{		
		/*
		 * If the schemas have not yet been initialized
		 */
		if(SchemaDirectory.schemas == null || SchemaDirectory.schemas.size() == 0 ){
			loadSchemas();
		}
		
		SchemaInstance s = (SchemaInstance) SchemaDirectory.schemas.get( schemaName.get() );
		if( s == null ){
			throw new KmfException("schema does not exist: "+schemaName.get());
		}
		return s;
	}
	
	public static void setup(String schemaName, String programName){
		SchemaDirectory.schemaName.set( schemaName );
		SchemaDirectory.program.set(programName);
	}
	
	public static String getSchemaName()
	{
		return (String)SchemaDirectory.schemaName.get();	
	}
	
	/**
	 * @return Returns the program.
	 */
	public static String getProgram() {
		return (String)program.get();
	}
	
	/**
	 * @param recordHistory The recordHistory to set.
	 */
	public static void setRecordHistory(boolean recordHistory) {
		recordhistory = recordHistory;
	}

}
