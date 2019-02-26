package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class AlertDefinitionType extends PersistentStringEnum 
{		
	public static final AlertDefinitionType HEARTBEAT_ALERT = new AlertDefinitionType(
			"Heartbeat Alert", 
			"com.kuvata.kmf.alerts.HeartbeatAlert",
			"Alert if at least",
			false);
	public static final AlertDefinitionType CONTENT_SCHEDULER_ALERT = new AlertDefinitionType(
			"Content Scheduler Status Alert", 
			"com.kuvata.kmf.alerts.ContentSchedulerAlert",
			"",
			false);
	public static final AlertDefinitionType POWER_ALERT = new AlertDefinitionType(
			"Power Status Alert", 
			"com.kuvata.kmf.alerts.PowerAlert",
			"Alert if Status is (Off=0, On=1, leave blank for null)",
			true);
	public static final AlertDefinitionType VOLUME_ALERT = new AlertDefinitionType(
			"Volume Alert", 
			"com.kuvata.kmf.alerts.VolumeAlert",
			"Alert if Volume is not set to (0-100)",
			true);
	public static final AlertDefinitionType DISK_USAGE_ALERT = new AlertDefinitionType(
			"Disk Usage Alert", 
			"com.kuvata.kmf.alerts.DiskUsageAlert",
			"",
			false);
	public static final AlertDefinitionType DEVICE_RESOURCE_UTILIZATION_ALERT = new AlertDefinitionType(
			"Device Resource Utilization Alert", 
			"com.kuvata.kmf.alerts.DeviceResourceUtilizationAlert",
			"Alert if any threshold is exceeded",
			false);
	public static final AlertDefinitionType INPUT_SIGNAL_ALERT = new AlertDefinitionType(
			"Input Signal Alert", 
			"com.kuvata.kmf.alerts.InputSignalAlert",
			"Alert if the current input is not",
			true);	
	public static final AlertDefinitionType BRIGHTNESS_ALERT = new AlertDefinitionType(
			"Brightness Alert", 
			"com.kuvata.kmf.alerts.BrightnessAlert",
			"Alert if the brightness is not",
			true);	
	public static final AlertDefinitionType CONTRAST_ALERT = new AlertDefinitionType(
			"Contrast Alert", 
			"com.kuvata.kmf.alerts.ContrastAlert",
			"Alert if the contrast is not",
			true);
	public static final AlertDefinitionType MULTICAST_ALERT = new AlertDefinitionType(
			"Multicast Alert", 
			"com.kuvata.kmf.alerts.MulticastAlert",
			"",
			false);
	public static final AlertDefinitionType ACTIVE_CONTENT_SCHEDULE_ALERT = new AlertDefinitionType(
			"Active Content Schedule Alert", 
			"com.kuvata.kmf.alerts.ActiveContentScheduleAlert",
			"Alert if not playing the most recent Content Schedule after",
			false);
	public static final AlertDefinitionType CAMPAIGN_ALERT = new AlertDefinitionType(
			"Campaign Alert", 
			"com.kuvata.kmf.alerts.CampaignAlert",
			"Alert if properties have not been reached",
			false);
	public static final AlertDefinitionType DEVICE_DOWNLOAD_STATUS_ALERT = new AlertDefinitionType(
			"Device Download Status Alert", 
			"com.kuvata.kmf.alerts.DeviceDownloadStatusAlert",
			"",
			false);
	public static final AlertDefinitionType BUTTON_LOCK_ALERT = new AlertDefinitionType(
			"Button Lock Status Alert", 
			"com.kuvata.kmf.alerts.ButtonLockAlert",
			"Alert if Status is (Off=0, On=1)",
			true);
	public static final AlertDefinitionType ANTIVIRUS_ALERT = new AlertDefinitionType(
			"Antivirus Alert", 
			"com.kuvata.kmf.alerts.AntivirusAlert",
			"",
			false);

	public static final Map INSTANCES = new HashMap();
	private AlertDefinitionInfo alertDefinitionInfo;
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(HEARTBEAT_ALERT.toString(), HEARTBEAT_ALERT);
		INSTANCES.put(CONTENT_SCHEDULER_ALERT.toString(), CONTENT_SCHEDULER_ALERT);		
		INSTANCES.put(POWER_ALERT.toString(), POWER_ALERT);			
		INSTANCES.put(VOLUME_ALERT.toString(), VOLUME_ALERT);
		INSTANCES.put(DISK_USAGE_ALERT.toString(), DISK_USAGE_ALERT);		
		INSTANCES.put(DEVICE_RESOURCE_UTILIZATION_ALERT.toString(), DEVICE_RESOURCE_UTILIZATION_ALERT);
		INSTANCES.put(INPUT_SIGNAL_ALERT.toString(), INPUT_SIGNAL_ALERT);
		INSTANCES.put(BRIGHTNESS_ALERT.toString(), BRIGHTNESS_ALERT);
		INSTANCES.put(CONTRAST_ALERT.toString(), CONTRAST_ALERT);
		INSTANCES.put(MULTICAST_ALERT.toString(), MULTICAST_ALERT);
		INSTANCES.put(ACTIVE_CONTENT_SCHEDULE_ALERT.toString(), ACTIVE_CONTENT_SCHEDULE_ALERT);
		INSTANCES.put(CAMPAIGN_ALERT.toString(), CAMPAIGN_ALERT);
		INSTANCES.put(DEVICE_DOWNLOAD_STATUS_ALERT.toString(), DEVICE_DOWNLOAD_STATUS_ALERT);
		INSTANCES.put(BUTTON_LOCK_ALERT.toString(), BUTTON_LOCK_ALERT);
		INSTANCES.put(ANTIVIRUS_ALERT.toString(), ANTIVIRUS_ALERT);
	}
	/**
	 * 
	 *
	 */
	public AlertDefinitionType() {}
	
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public AlertDefinitionType(String name, String methodName, String usage, boolean isMcmAlert) {
		super(name, methodName);
				
		// Build the AlertDefinitionInfo object		
		AlertDefinitionInfo adi = new AlertDefinitionInfo();
		adi.setMethodName( methodName );
		adi.setUsage( usage );
		adi.setIsMcmAlert( isMcmAlert );
		this.alertDefinitionInfo = adi;
	}	
	
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	
	/**
	 * @return Returns the alertDefinitionInfo.
	 */
	public AlertDefinitionInfo getAlertDefinitionInfo() {
		return alertDefinitionInfo;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getAlertDefinitions()
	{
		List l = new LinkedList();
		Iterator i = AlertDefinitionType.INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param presentationStyleName
	 * @return
	 */
	public static AlertDefinitionType getPresentationStyle(String presentationStyleName)
	{
		return (AlertDefinitionType) INSTANCES.get( presentationStyleName );
	}	
	/**
	 * 
	 * @param persistentValue
	 * @return
	 */
	public static AlertDefinitionType getAlertDefinitionByPersistentValue(String persistentValue)
	{				
		for( Iterator i = AlertDefinitionType.INSTANCES.values().iterator(); i.hasNext(); )
		{
			AlertDefinitionType ps = (AlertDefinitionType)i.next();
			if( ps.getPersistentValue().equals( persistentValue) )
			{
				return ps;
			}
		}
		return null;
	}
	
	public class AlertDefinitionInfo {
		
		private String methodName;		
		private String usage;
		private Boolean isMcmAlert;
		
		public AlertDefinitionInfo() {}

		/**
		 * @return Returns the methodName.
		 */
		public String getMethodName() {
			return methodName;
		}
		/**
		 * @param methodName The methodName to set.
		 */
		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}
		/**
		 * @return Returns the usage.
		 */
		public String getUsage() {
			return usage;
		}
		/**
		 * @param usage The usage to set.
		 */
		public void setUsage(String usage) {
			this.usage = usage;
		}

		public Boolean getIsMcmAlert() {
			return isMcmAlert;
		}

		public void setIsMcmAlert(Boolean isMcmAlert) {
			this.isMcmAlert = isMcmAlert;
		}
	}
}
