package com.kuvata.kmf;

import org.hibernate.HibernateException;

import com.kuvata.kmf.util.Reformat;


/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class DeviceGrpMember extends GrpMember
{
	private Long deviceGrpMemberId;
	private Device device;	
		
	public DeviceGrpMember()
	{		
	}
	
	/**
	 * Implements the abstract method GrpMember.getName()
	 */	
	public String getName()
	{
		return this.getDevice().getDeviceName();
	}
	/**
	 * 
	 */
	public void delete() throws HibernateException
	{
		super.delete();		
	}
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return super.getEntityId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getGrp().getGrpId();
	}		
	/**
	 * 
	 */
	public Entity getChild()
	{
		return this.getDevice();
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof DeviceGrpMember) ) result = false;
		
		DeviceGrpMember dgm = (DeviceGrpMember) other;		
		if(this.hashCode() == dgm.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "DeviceGrpMember".hashCode();
		result = Reformat.getSafeHash( this.getGrp().getGrpId(), result, 13 );
		result = Reformat.getSafeHash( this.getDevice().getDeviceId(), result, 29 );		
		return result;
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
	 * @return Returns the deviceGrpMemberId.
	 */
	public Long getDeviceGrpMemberId() {
		return deviceGrpMemberId;
	}

	/**
	 * @param deviceGrpMemberId The deviceGrpMemberId to set.
	 */
	public void setDeviceGrpMemberId(Long deviceGrpMemberId) {
		this.deviceGrpMemberId = deviceGrpMemberId;
	}

}
