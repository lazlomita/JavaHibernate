package com.kuvata.kmf;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import parkmedia.KMFLogger;

import com.kuvata.kmm.KMMRequestProcessor;


/**
 * Created on Jul 8, 2004
 * Last update on March 5, 2018
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 * @author Lazlo Mita Delgadillo
 */
public class Schema extends KmfEntity {
	
	private Long schemaId;
	private String schemaName;	
	private String rsyncUsername;
	private String rsyncPassword;
	private String rsyncPort;
	private String sshServer;
	private String sshPort;
	private String servicePort;	
	private Boolean useDeviceProperties;
	private Long templateDeviceId;
	private Integer maxRsyncProcesses;
	private Long lastAggregatedId;
	private Date lastAggregatedDate;
	private Integer aggregationProgress;
	private Date lastServerCleanupDt;
	private Boolean useSSHTerminal;
	
	/**
	 * 	 * @throws Exception
	 */
	public Schema() throws Exception
	{		
	}
	
	public Long getEntityId(){
		return this.schemaId;
	}	
	
	/**
	 * Returns the schema object associated with the given schema
	 * @param schemaName
	 * @return
	 * @throws HibernateException
	 */
	public static Schema getSchema(String schemaName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		Schema schema = (Schema)session.createQuery("from Schema s where s.schemaName=?")
									.setParameter(0, schemaName).uniqueResult();						
		return schema; 		
	}		

	/**
	 * @return Returns the schemaId.
	 */
	public Long getSchemaId() {
		return schemaId;
	}

	/**
	 * @param schema The schema to set.
	 */
	public void setSchemaId(Long schemaId) {
		this.schemaId = schemaId;
	}

	/**
	 * @return Returns the schema.
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schema The schema to set.
	 */
	public void setSchemaName(String schema) {
		this.schemaName = schema;
	}


	/**
	 * @return Returns the templateDeviceId.
	 */
	public Long getTemplateDeviceId() {
		return templateDeviceId;
	}
	

	/**
	 * @param templateDeviceId The templateDeviceId to set.
	 */
	public void setTemplateDeviceId(Long templateDeviceId) {
		this.templateDeviceId = templateDeviceId;
	}
	

	/**
	 * @return Returns the rsyncPassword.
	 */
	public String getRsyncPassword() {
		return rsyncPassword;
	}
	

	/**
	 * @param rsyncPassword The rsyncPassword to set.
	 */
	public void setRsyncPassword(String rsyncPassword) {
		this.rsyncPassword = rsyncPassword;
	}
	

	/**
	 * @return Returns the rsyncPort.
	 */
	public String getRsyncPort() {
		return rsyncPort;
	}
	

	/**
	 * @param rsyncPort The rsyncPort to set.
	 */
	public void setRsyncPort(String rsyncPort) {
		this.rsyncPort = rsyncPort;
	}
	

	/**
	 * @return Returns the rsyncUsername.
	 */
	public String getRsyncUsername() {
		return rsyncUsername;
	}
	

	/**
	 * @param rsyncUsername The rsyncUsername to set.
	 */
	public void setRsyncUsername(String rsyncUsername) {
		this.rsyncUsername = rsyncUsername;
	}
	

	/**
	 * @return Returns the servicePort.
	 */
	public String getServicePort() {
		return servicePort;
	}
	

	/**
	 * @param servicePort The servicePort to set.
	 */
	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}
	
	/**
	 * @return Returns the useDeviceProperties.
	 */
	public Boolean getUseDeviceProperties() {
		return useDeviceProperties;
	}	

	/**
	 * @param useDeviceProperties The useDeviceProperties to set.
	 */
	public void setUseDeviceProperties(Boolean useDeviceProperties) {
		this.useDeviceProperties = useDeviceProperties;
	}

	public String getSshPort() {
		return sshPort;
	}

	public void setSshPort(String sshPort) {
		this.sshPort = sshPort;
	}

	public String getSshServer() {
		return sshServer;
	}

	public void setSshServer(String sshServer) {
		this.sshServer = sshServer;
	}

	/**
	 * @return the maxRsyncProcesses
	 */
	public synchronized Integer getMaxRsyncProcesses() {
		return maxRsyncProcesses;
	}

	/**
	 * @param maxRsyncProcesses the maxRsyncProcesses to set
	 */
	public synchronized void setMaxRsyncProcesses(Integer maxRsyncProcesses) {
		this.maxRsyncProcesses = maxRsyncProcesses;
	}

	public Date getLastAggregatedDate() {
		return lastAggregatedDate;
	}

	public void setLastAggregatedDate(Date lastAggregatedDate) {
		this.lastAggregatedDate = lastAggregatedDate;
	}

	public Integer getAggregationProgress() {
		return aggregationProgress;
	}

	public void setAggregationProgress(Integer aggregationProgress) {
		this.aggregationProgress = aggregationProgress;
	}

	public Long getLastAggregatedId() {
		return lastAggregatedId;
	}

	public void setLastAggregatedId(Long lastAggregatedId) {
		this.lastAggregatedId = lastAggregatedId;
	}

	public Date getLastServerCleanupDt() {
		return lastServerCleanupDt;
	}

	public void setLastServerCleanupDt(Date lastServerCleanupDt) {
		this.lastServerCleanupDt = lastServerCleanupDt;
	}

	/**
	 * @return Returns the useDeviceProperties.
	 */
	public Boolean getUseSSHTerminal() {
		return useSSHTerminal;
	}
	
	/**
	 * @param useDeviceProperties The useDeviceProperties to set.
	 */
	public void setUseSSHTerminal(Boolean useSSHTerminalparameter) {
		this.useSSHTerminal = useSSHTerminalparameter;
	}
}