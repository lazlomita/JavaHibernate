package com.kuvata.kmf.alerts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.EventType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.AlertDevice;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.EventHistory;
import com.kuvata.kmf.GrpMember;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class AntivirusAlert extends AlertDefinition {
	
	public AntivirusAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		// We don't need the frequency since this alert runs once and only once per day
		super( schemaName, alertId, alertType, alertName, args, null );
	}
	
	/**
	 * Implements the parent's abstract method.
	 * Schedules this timer task
	 */
	public void schedule()
	{
		Alert alert = Alert.getAlert( this.alertId );
		
		Calendar now = Calendar.getInstance();
		Calendar scheduleTime = Calendar.getInstance();
		scheduleTime.setTime(alert.getActiveStartTime());
		scheduleTime.set( Calendar.YEAR, now.get(Calendar.YEAR) );
		scheduleTime.set( Calendar.MONTH, now.get(Calendar.MONTH) );
		scheduleTime.set( Calendar.DATE, now.get(Calendar.DATE) );
		
		// Schedule it for the next day if the schedule time is before now
		if(scheduleTime.getTime().before(now.getTime())){
			scheduleTime.add(Calendar.DATE, 1);
		}
		
		timer.schedule(this, scheduleTime.getTime());
		logger.info("Scheduling antivirus alert to run at "+ scheduleTime.getTime());
	}
	
	/**
	 * If a Device is not playing the latest content,
	 * email the appropriate users.
	 * 
	 * @param threshold
	 */
	public void runAlert()
	{
		SchemaDirectory.setup(this.schemaName, this.getClass().getName());
		try
		{
			HibernateSession.clearCache();
			Alert alert = Alert.getAlert( this.alertId );
			if( alert != null )
			{
				// Set the last run date of the alert
				alert.setLastRunDt(new Date());
				alert.update();
				
				// Get a list of devices
				List<Device> devices = new LinkedList<Device>();
				if(alert.alertSpecificDevices()){
					for(AlertDevice ad : alert.getAlertDevices()){
						if(ad.getDevice() != null){
							devices.add(ad.getDevice());
						}else if(ad.getDeviceGrp() != null){
							for(GrpMember gm : ad.getDeviceGrp().getGrpMembers()){
								devices.add((Device)gm.getChild());
							}
						}
					}
				}else{
					devices = Device.getDevices();
				}
				
				HashMap<Long, String> deviceMap = new HashMap<Long, String>();
				for(Device d : devices){
					// Only alert on appropriate devices
					if(d.getApplyAlerts() != null && d.getApplyAlerts()){						
						deviceMap.put(d.getDeviceId(), d.getDeviceName());
					}
				}
				
				// Get the list of email address to send for this alert 
				String emailAddresses = alert.buildEmailAddresses();
				
				HashMap<Long, EventHistory> results = new HashMap<Long, EventHistory>();
				if(deviceMap.size() > 0 && emailAddresses != null && emailAddresses.length() > 0){
					String hql = "SELECT eventHistory FROM EventHistory eventHistory "
						+ "WHERE eventHistory.deviceId = :deviceId "
						+ "AND eventHistory.eventType = '" + EventType.ANTIVIRUS_SCAN.getPersistentValue() + "' "
						+ "ORDER BY eventHistory.eventDt DESC";
					
					// Get the latest CLAM result for each device
					for(Long deviceId : deviceMap.keySet()){
						Session session = HibernateSession.currentSession();
						List<EventHistory> l = session.createQuery(hql).setParameter("deviceId", deviceId).setMaxResults(1).list();
						
						if(l.size() > 0){
							results.put(deviceId, l.get(0));
						}
					}
					
					// Create the message body
					String msgBody = "The latest CLAM Antivirus results from '" + alert.getAlertName() + "' are listed below (format: Device Name / Date / Results). \n\n";
					
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
					// First list the infected devices
					for(Long deviceId : deviceMap.keySet()){
						EventHistory eh = results.get(deviceId);
						
						if(eh != null && eh.getEventDetails().equals("No infections found") == false){
							msgBody += deviceMap.get(deviceId) + "\t\t" + sdf.format(eh.getEventDt()) + "\t\t" + eh.getEventDetails() + "\n";
						}
					}
					
					// Now list the non-infected devices
					for(Long deviceId : deviceMap.keySet()){
						EventHistory eh = results.get(deviceId);
						
						if(eh != null && eh.getEventDetails().equals("No infections found")){
							msgBody += deviceMap.get(deviceId) + "\t\t" + sdf.format(eh.getEventDt()) + "\t\t" + eh.getEventDetails() + "\n";
						}
					}
					
					// Now list the devices that don't have a result at all
					for(Long deviceId : deviceMap.keySet()){
						EventHistory eh = results.get(deviceId);
						
						if(eh == null){
							msgBody += deviceMap.get(deviceId) + "\t\tNo results available\n";
						}
					}
					
					// Send an email with the results
					try {
						System.out.println("Sending CLAM Antivirus Results");
						String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
						String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");
						Emailer.sendMessage("CLAM Antivirus Results", msgBody, emailAddresses, fromAddress, mailServer, false);
					} catch (KmfException e) {
						System.out.println("Could not locate property. Unable to send email.");
					}
				}
			}
	
		} catch(Exception e) {
			logger.error(e);
		} finally {
			HibernateSession.closeSession();
		}
	}
}
