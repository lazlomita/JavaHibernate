package com.kuvata.kmf;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class AssetExclusion extends Entity implements HistorizableCollectionMember {

	private Long assetExclusionId;
	private Device device;
	private IAsset asset;
	private Date excludedDate;

	/**
	 * 
	 * @throws HibernateException
	 */
	public AssetExclusion() throws HibernateException
	{	
	}

	/**
	 * 
	 * @param assetExclusionId
	 * @return
	 * @throws HibernateException
	 */
	public static AssetExclusion getAssetExclusion(Long assetExclusionId) throws HibernateException
	{
		return (AssetExclusion)Entity.load(AssetExclusion.class, assetExclusionId);		
	}
	
	/**
	 * Attempts to locate an AssetExclusion object with the given device and asset.
	 * @param device
	 * @param asset
	 * @return
	 */
	public static AssetExclusion getAssetExclusion(Device device, IAsset asset)
	{
		Session session = HibernateSession.currentSession();			
		AssetExclusion ae = (AssetExclusion)session.createCriteria(AssetExclusion.class)
				.add( Expression.eq("device.deviceId", device.getDeviceId()) )
				.add( Expression.eq("asset.assetId", asset.getAssetId()) )
				.uniqueResult();
		return ae;
	}
	
	public static List<AssetExclusion> getAssetExclusions(Long assetId)
	{
		Session session = HibernateSession.currentSession();
		return session.createQuery("SELECT ae FROM AssetExclusion ae WHERE ae.asset.assetId = :assetId ORDER BY UPPER(ae.device.deviceName)").setParameter("assetId", assetId).list();
	}
	
	/**
	 * Either updates or creates a new AssetExclusion object with the current date/time.
	 * @param device
	 * @param asset
	 */
	public static void createOrUpdate(Device device, IAsset asset)
	{
		// If an AssetExclusion object does not already exist for this combination
		AssetExclusion ae = AssetExclusion.getAssetExclusion( device, asset );
		if( ae == null ){
			// Create a new one
			ae = new AssetExclusion();
			ae.setDevice( device );
			ae.setAsset( asset );
			ae.setExcludedDate( new Date() );
			ae.save();			
			device.getAssetExclusions().add( ae );
		}else{
			ae.setExcludedDate( new Date() );
			ae.update();
		}
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof AssetExclusion) ) result = false;
		
		AssetExclusion ae = (AssetExclusion) other;		
		if(this.hashCode() == ae.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "AssetExclusion".hashCode();
		result = Reformat.getSafeHash( this.getAssetExclusionId(), result, 29 );
		result = Reformat.getSafeHash( this.getAsset().getAssetId(), result, 31 );
		result = Reformat.getSafeHash( this.getDevice().getDeviceId(), result, 39 );
		result = Reformat.getSafeHash( this.getExcludedDate(), result, 41 );		
		return result;
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAssetExclusionId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getDevice().getDeviceId();
	}		

	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getAsset().getAssetName();
	}
	
	/**
	 * @return Returns the asset.
	 */
	public IAsset getAsset() {
		return asset;
	}

	/**
	 * @param asset The asset to set.
	 */
	public void setAsset(IAsset asset) {
		this.asset = asset;
	}

	/**
	 * @return Returns the assetExclusionId.
	 */
	public Long getAssetExclusionId() {
		return assetExclusionId;
	}
	

	/**
	 * @param assetExclusionId The assetExclusionId to set.
	 */
	public void setAssetExclusionId(Long assetExclusionId) {
		this.assetExclusionId = assetExclusionId;
	}
	

	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}
	

	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}
	

	/**
	 * @return Returns the excludedDate.
	 */
	public Date getExcludedDate() {
		return excludedDate;
	}
	

	/**
	 * @param excludedDate The excludedDate to set.
	 */
	public void setExcludedDate(Date excludedDate) {
		this.excludedDate = excludedDate;
	}
	
}
