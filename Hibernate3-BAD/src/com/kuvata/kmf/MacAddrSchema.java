package com.kuvata.kmf;

import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.KMFLogger;



/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class MacAddrSchema extends KmfEntity {
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( MacAddrSchema.class );	
	private Long macAddrSchemaId;
	private String macAddr;
	private Schema schema;
	
	/**
	 * 
	 * @throws Exception
	 */
	public MacAddrSchema()
	{		
	}
	
	public Long getEntityId(){
		return this.macAddrSchemaId;
	}
	
	/**
	 * Attempt to retrieve a MacAddrSchema object with the given macAddr
	 * @param macAddr
	 * @return
	 */
	public static MacAddrSchema getMacAddrSchema(String macAddr)
	{
		Session session = HibernateSession.currentSession();
		MacAddrSchema macAddrSchema = (MacAddrSchema)session.createCriteria(MacAddrSchema.class)
			.add( Expression.eq("macAddr", macAddr) )							
			.uniqueResult();
		return macAddrSchema;
	}
	
	public static void create(String macAddr, String schemaName)
	{
		// Locate the schema associated with the given schemaName
		Schema schema = Schema.getSchema( schemaName );
		if( schema != null )
		{
			MacAddrSchema macAddrSchema = new MacAddrSchema();
			macAddrSchema.setMacAddr( macAddr );
			macAddrSchema.setSchema( schema );
			macAddrSchema.save();
		}else{
			logger.info("Could not create MacAddrSchema. Unable to locate schema with given name: "+ schemaName);
		}
	}
	
	/**
	 * Removes any MacAddrSchema objects with the given macAddr
	 * @param macAddr
	 */
	public static void delete(String macAddr, String schemaName)
	{
		// Locate the schema associated with the given schemaName
		Schema schema = Schema.getSchema( schemaName );
		if( schema != null )
		{
			MacAddrSchema macAddrSchema = MacAddrSchema.getMacAddrSchema( macAddr, schema );
			if( macAddrSchema != null ){
				macAddrSchema.delete();
			}
		}else{
			logger.info("Could not delete MacAddrSchema. Unable to locate schema with given name: "+ schemaName);
		}
	}
	
	private static MacAddrSchema getMacAddrSchema(String macAddr, Schema schema)
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT macAddrSchema FROM MacAddrSchema as macAddrSchema "
			+ "WHERE macAddrSchema.schema.schemaId = :schemaId "
			+ "AND macAddrSchema.macAddr = :macAddr";		
		return (MacAddrSchema)session.createQuery( hql )
			.setParameter("schemaId", schema.getSchemaId())
			.setParameter("macAddr", macAddr)
			.uniqueResult();
	}

	/**
	 * @return Returns the macAddr.
	 */
	public String getMacAddr() {
		return macAddr;
	}
	

	/**
	 * @param macAddr The macAddr to set.
	 */
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	

	/**
	 * @return Returns the macAddrSchemaId.
	 */
	public Long getMacAddrSchemaId() {
		return macAddrSchemaId;
	}
	

	/**
	 * @param macAddrSchemaId The macAddrSchemaId to set.
	 */
	public void setMacAddrSchemaId(Long macAddrSchemaId) {
		this.macAddrSchemaId = macAddrSchemaId;
	}
	

	/**
	 * @return Returns the schema.
	 */
	public Schema getSchema() {
		return schema;
	}
	

	/**
	 * @param schema The schema to set.
	 */
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
	
	

}
