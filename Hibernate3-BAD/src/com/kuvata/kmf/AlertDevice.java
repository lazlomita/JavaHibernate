package com.kuvata.kmf;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableCollectionMember;

public class AlertDevice extends PersistentEntity implements HistorizableCollectionMember
{
	private Long alertDeviceId;
	private Alert alert;
	private Device device;	
	private Grp deviceGrp;
	
	/**
	 * 
	 *
	 */
	public AlertDevice()
	{		
	}
	
	/**
	 * Creates a new AlertDevice object for the given alert and device
	 * @param device
	 * @param asset
	 */
	public static AlertDevice create(Alert alert, Device device)
	{
		AlertDevice alertDevice = new AlertDevice();
		alertDevice.setAlert( alert );
		alertDevice.setDevice( device );
		alertDevice.save();
		return alertDevice;
	}
	
	/**
	 * Creates a new AlertDevice object for the given alert and deviceGrp
	 * @param device
	 * @param asset
	 */
	public static AlertDevice create(Alert alert, Grp deviceGrp)
	{
		AlertDevice alertDevice = new AlertDevice();
		alertDevice.setAlert( alert );
		alertDevice.setDeviceGrp( deviceGrp );
		alertDevice.save();
		return alertDevice;
	}
	
	/**
	 * 
	 * @param alert
	 * @param device
	 * @return
	 * @throws HibernateException
	 */
	public static AlertDevice getAlertDevice(Alert alert, Device device) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		return (AlertDevice)session.createCriteria(AlertDevice.class)
				.add( Expression.eq("alert.alertId", alert.getAlertId()) )
				.add( Expression.eq("device.deviceId", device.getDeviceId()) )
				.uniqueResult();						
	}	

	/**
	 * 
	 * @param alert
	 * @param deviceGrp
	 * @return
	 * @throws HibernateException
	 */
	public static AlertDevice getAlertDevice(Alert alert, Grp deviceGrp) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		return (AlertDevice)session.createCriteria(AlertDevice.class)
				.add( Expression.eq("alert.alertId", alert.getAlertId()) )
				.add( Expression.eq("deviceGrp.grpId", deviceGrp.getGrpId()) )
				.uniqueResult();						
	}	
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAlertDeviceId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getEntityId();
	}	
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		String result = "";
		if( this.getDeviceGrp() != null ){
			result = this.getDeviceGrp().getGrpName();
		}else if( this.getDevice() != null ){
			result = this.getDevice().getDeviceName();
		}
		return result;
	}	

	/**
	 * @return Returns the alert.
	 */
	public Alert getAlert() {
		return alert;
	}
	

	/**
	 * @param alert The alert to set.
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
	}
	

	/**
	 * @return Returns the alertDeviceId.
	 */
	public Long getAlertDeviceId() {
		return alertDeviceId;
	}
	

	/**
	 * @param alertDeviceId The alertDeviceId to set.
	 */
	public void setAlertDeviceId(Long alertDeviceId) {
		this.alertDeviceId = alertDeviceId;
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
	 * @return the deviceGrp
	 */
	public Grp getDeviceGrp() {
		return deviceGrp;
	}

	/**
	 * @param deviceGrp the deviceGrp to set
	 */
	public void setDeviceGrp(Grp deviceGrp) {
		this.deviceGrp = deviceGrp;
	}
	
	
	
	
	

}
