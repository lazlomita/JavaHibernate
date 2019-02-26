package com.kuvata.kmf;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

public class DeviceSchedule extends Entity implements HistorizableCollectionMember {

	private Long deviceScheduleId;
	private Device device;
	private Segment segment;
	/**
	 * 
	 *
	 */
	public DeviceSchedule()
	{		
	}
	/**
	 * Returns a DeviceSchedule object corresponding to the given Device and Segment.
	 * 
	 * @param d
	 * @param s
	 * @return
	 * @throws HibernateException
	 */
	public static DeviceSchedule getDeviceSchedule(Device d, Segment s) throws HibernateException
	{					
		return getDeviceSchedule(d.getDeviceId(), s);		
	}
	
	/**
	 * Returns a DeviceSchedule object corresponding to the given Device and Segment.
	 * 
	 * @param d
	 * @param s
	 * @return
	 * @throws HibernateException
	 */
	public static DeviceSchedule getDeviceSchedule(Long deviceId, Segment s) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		DeviceSchedule ds = (DeviceSchedule) session.createCriteria(DeviceSchedule.class)
				.add( Expression.eq("device.deviceId", deviceId) )
				.add( Expression.eq("segment.segmentId", s.getSegmentId()) )				
				.uniqueResult();					
		return ds;		
	}
	
	/**
	 * 
	 */
	public void delete(boolean deleteSegmentEntry, boolean deleteDeviceEntry) throws HibernateException
	{
		// Remove this object from the parent collection
		if( deleteSegmentEntry ) {
			this.segment.getDeviceSchedules().remove( this );
		}
		if( deleteDeviceEntry ) {
			this.device.getDeviceSchedules().remove( this );				
		}
		super.delete();
		
		// Make both the segment and device associated with this DeviceSchedule dirty
		this.segment.makeDirty( false );
		this.device.makeDirty();		
	}	
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getDeviceScheduleId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getSegment().getSegmentId();
	}		
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getDevice().getDeviceName();
	}	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof DeviceSchedule) ) result = false;
		
		DeviceSchedule ds = (DeviceSchedule) other;		
		if(this.hashCode() == ds.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "DeviceSchedule".hashCode();
		result = Reformat.getSafeHash( this.getDeviceScheduleId(), result, 3 );
		result = Reformat.getSafeHash( this.getSegment().getSegmentId(), result, 13 );
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
	 * @return Returns the deviceScheduleId.
	 */
	public Long getDeviceScheduleId() {
		return deviceScheduleId;
	}

	/**
	 * @param deviceScheduleId The deviceScheduleId to set.
	 */
	public void setDeviceScheduleId(Long deviceScheduleId) {
		this.deviceScheduleId = deviceScheduleId;
	}

	/**
	 * @return Returns the segment.
	 */
	public Segment getSegment() {
		return segment;
	}

	/**
	 * @param segment The segment to set.
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

}
