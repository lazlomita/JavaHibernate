package com.kuvata.kmf.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.ContentUpdateType;
import parkmedia.usertype.EventType;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.DeviceGrpMember;
import com.kuvata.kmf.EventHistory;
import com.kuvata.kmf.Grp;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.alerts.Emailer;

public class EmailClamResults {

	public static void main(String[] args){
		SchemaDirectory.initialize("kuvata", "EmailClamResults", null, false, true);
		
		// Get the device group
		Grp deviceGroup = Grp.getGrp( new Long(args[0]) );
		
		// For each device in this device group
		HashMap<Long, String> devices = new HashMap<Long, String>();
		for( Iterator i=deviceGroup.getGrpMembers().iterator(); i.hasNext(); )
		{
			DeviceGrpMember dgm = (DeviceGrpMember)i.next();
			Device d = dgm.getDevice();
			if(d.getContentUpdateType().equals(ContentUpdateType.NETWORK.getPersistentValue())){
				devices.put(d.getDeviceId(), d.getDeviceName());
			}
		}
		
		HashMap<Long, EventHistory> results = new HashMap<Long, EventHistory>();
		if(devices.size() > 0){
			String hql = "SELECT eventHistory FROM EventHistory eventHistory "
				+ "WHERE eventHistory.deviceId = :deviceId "
				+ "AND eventHistory.eventType = '" + EventType.ANTIVIRUS_SCAN.getPersistentValue() + "' "
				+ "ORDER BY eventHistory.eventDt DESC";
			
			// Get the latest CLAM result for each device
			for(Long deviceId : devices.keySet()){
				Session session = HibernateSession.currentSession();
				List<EventHistory> l = session.createQuery(hql).setParameter("deviceId", deviceId).setMaxResults(1).list();
				
				if(l.size() > 0){
					results.put(deviceId, l.get(0));
				}
			}
			
			// Create the message body
			String msgBody = "The latest CLAM Antivirus results for the devices in the device group '" + deviceGroup.getGrpName() + "' are listed below (format: Device Name / Date / Results). \n\n";
			
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
			// First list the infected devices
			for(Long deviceId : devices.keySet()){
				EventHistory eh = results.get(deviceId);
				
				if(eh != null && eh.getEventDetails().equals("No infections found") == false){
					msgBody += devices.get(deviceId) + "\t\t" + sdf.format(eh.getEventDt()) + "\t\t" + eh.getEventDetails() + "\n";
				}
			}
			
			// Now list the non-infected devices
			for(Long deviceId : devices.keySet()){
				EventHistory eh = results.get(deviceId);
				
				if(eh != null && eh.getEventDetails().equals("No infections found")){
					msgBody += devices.get(deviceId) + "\t\t" + sdf.format(eh.getEventDt()) + "\t\t" + eh.getEventDetails() + "\n";
				}
			}
			
			// Now list the devices that don't have a result at all
			for(Long deviceId : devices.keySet()){
				EventHistory eh = results.get(deviceId);
				
				if(eh == null){
					msgBody += devices.get(deviceId) + "\t\tNo results available\n";
				}
			}
			
			// Send an email with the results
			try {
				System.out.println("Sending CLAM Antivirus Results");
				String to = "storesupport@stevemadden.com, ITAlerts@stevemadden.com, HebelMorales@stevemadden.com, Dlholman@playnetwork.com, cmalerts@parkmedia.tv";
				String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
				String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");
				Emailer.sendMessage("CLAM Antivirus Results", msgBody, to, fromAddress, mailServer, false);
			} catch (KmfException e) {
				System.out.println("Could not locate property. Unable to send email.");
			}
		}
	}
}
