/*
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.sql.Clob;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * 
 * @author Jeff Randesi
 */
public class AssetIngesterStatus extends Entity {

	private Long assetIngesterStatusId;
	private Date dt;
	private Clob status;
	private static final int MAX_RECENT_ASSET_INGESTERS = 10;

	public AssetIngesterStatus() 
	{		
	}
	
	public Long getEntityId()
	{
		return this.getAssetIngesterStatusId();
	}
	
	/**
	 * Returns a AssetIngesterStatus object with the given id
	 * @param assetIngesterStatusId
	 * @return
	 * @throws HibernateException
	 */
	public static AssetIngesterStatus getAssetIngesterStatus(Long assetIngesterStatusId) throws HibernateException
	{
		return (AssetIngesterStatus)Entity.load(AssetIngesterStatus.class, assetIngesterStatusId);		
	}
	
	/**
	 * Retrieve the most recent assetIngesterStatus object
	 * @return
	 * @throws HibernateException
	 */
	public static AssetIngesterStatus getLastAssetIngesterStatus() throws HibernateException
	{
		AssetIngesterStatus result = null;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT ais FROM AssetIngesterStatus as ais "
			+"WHERE ais.dt = "
			+"(SELECT MAX(ais2.dt) FROM AssetIngesterStatus as ais2)";			
		Query q = session.createQuery( hql ); 
		q.setMaxResults(1);		
		List l = q.list();
		if( l != null && l.size() > 0 ) {
			result = (AssetIngesterStatus)l.get(0);			
		}
		return result;		
	}
	
	/**
	 * Retrieve the 10 most recent asset ingester status objects
	 * @return
	 * @throws HibernateException
	 */
	public static List getMostRecentAssetIngesterStatuses() throws HibernateException
	{
		AssetIngesterStatus result = null;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT ais FROM AssetIngesterStatus as ais "
			+"ORDER BY ais.dt DESC";
		Query q = session.createQuery( hql ).setMaxResults( MAX_RECENT_ASSET_INGESTERS ); 				
		return q.list();		
	}

	/**
	 * @return Returns the assetIngesterStatusId.
	 */
	public Long getAssetIngesterStatusId() {
		return assetIngesterStatusId;
	}
	

	/**
	 * @param assetIngesterStatusId The assetIngesterStatusId to set.
	 */
	public void setAssetIngesterStatusId(Long assetIngesterStatusId) {
		this.assetIngesterStatusId = assetIngesterStatusId;
	}
	

	/**
	 * @return Returns the dt.
	 */
	public Date getDt() {
		return dt;
	}
	

	/**
	 * @param dt The dt to set.
	 */
	public void setDt(Date dt) {
		this.dt = dt;
	}

	/**
	 * @return the status
	 */
	public Clob getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Clob status) {
		this.status = status;
	}
	

	
	

}
