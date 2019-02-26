package com.kuvata.kmf.alerts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.usertype.ContentUpdateType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.util.Reformat;

/**
 * Determines if one or more devices have not heartbeated
 * within a given threshold and sends an alert if necessary.
 * 
 * @author jrandesi
 */
public class DiskUsageAlert extends AlertDefinition 
{		
	public DiskUsageAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		super( schemaName, alertId, alertType, alertName, args, frequency );		
	}
	
	/**
	 * Implements the parent's abstract method.
	 * Schedules this timer task to run every 30 minutes
	 */
	public void schedule()
	{		
		// Schedule this method to run every "frequency" number of minutes
		long millisInMinute = 1000 * 60 * this.frequency;
		timer.scheduleAtFixedRate( this, getNextRunTime(), millisInMinute );			
	}
	
	/**
	 * If a Device has not heartbeated within the given threshold (in minutes),
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
				
				String hql = "SELECT device FROM Device device "
					+ "WHERE device.diskUsage IS NOT NULL "
					+ "AND device.maxFileStorage IS NOT NULL ";
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					hql += "AND (device.deviceId IN "
					+ "(SELECT alertDevice.device.deviceId FROM AlertDevice alertDevice WHERE alertDevice.device IS NOT NULL AND alertDevice.alert.alertId = :alertId) "
					+ "OR device.deviceId IN "
					+ "(SELECT dgm.device.deviceId FROM DeviceGrpMember as dgm "
					+ "WHERE dgm.grp.grpId IN (SELECT alertDevice.deviceGrp.grpId FROM AlertDevice alertDevice WHERE alertDevice.deviceGrp IS NOT NULL AND alertDevice.alert.alertId = :alertId))) ";
				}
				hql += "AND device.contentUpdateType = :contentUpdateType "
					+ "AND device.applyAlerts = :applyAlerts "				
					+ "ORDER BY UPPER(device.deviceName)";										
						
				Session session = HibernateSession.currentSession();
				Query q = session.createQuery( hql );
				if( alert.alertSpecificDevices() ){
					q.setParameter("alertId", alertId );
				}	
				q.setParameter("contentUpdateType", ContentUpdateType.NETWORK.getPersistentValue() );
				q.setParameter("applyAlerts", Boolean.TRUE );				
												
				List l = q.list();
				LinkedList devices = new LinkedList();
				for( Iterator i = l.iterator(); i.hasNext(); )
				{
					Device d = (Device)i.next();
					double lDiskCapacity = Reformat.convertStringToBytes( d.getDiskCapacity() );
					double lDiskUsage = Reformat.convertStringToBytes( d.getDiskUsage() );
					double lMaxFileStorage = d.getMaxFileStorage() * Constants.GIGABYTE;
					if( lDiskUsage >= lMaxFileStorage ){
						// Add the device to the collection of devices to alert as long as it meets all criteria
						if( this.addDevice( d, alert ) ){
							devices.add( d );
						}
					}else if( lMaxFileStorage > lDiskCapacity ){
						// Add the device to the collection of devices to alert as long as it meets all criteria
						if( this.addDevice( d, alert ) ){
							devices.add( d );
						}
					}
				}
				
				// Get the list of email address to send for this alert 				
				String emailAddresses = alert.buildEmailAddresses();
					
				// If we found any email addresses for this alert
				if( emailAddresses != null && emailAddresses.length() > 0 ) {
					this.sendAlert( alert, devices, emailAddresses);
				}									
			}	
		} catch(Exception e) {
			logger.error(e);
		} finally {
			HibernateSession.closeSession();
		}
	}
	
	/**
	 * Builds the message and calls the parent method to send it
	 * 
	 * @param devices
	 * @param emailAddresses
	 */
	private void sendAlert(Alert alert, LinkedList devices, String emailAddresses)
	{		
		ArrayList<String> colHeaders = new ArrayList<String>();
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
		
		String subject = alert.getAlertName() +" - Disk Usage Alert";		
		colHeaders.add( "Device Name" );
		colHeaders.add( "MAC Address" );
		colHeaders.add( "Disk Usage" );
		colHeaders.add( "Max FileStorage" );		
		
		/*
		 * 1. Determine if each entry is valid (based on the onlyAlertOnChange property)
		 * 2. Build the row data and lastValues collections
		 */
		HashMap<Long, Object[]> lastResults = new HashMap<Long, Object[]>();
		LinkedList<Device> validDevices = new LinkedList<Device>();
		for( Iterator<Device> i=devices.iterator(); i.hasNext(); ) 
		{
			Device d = i.next();
			String diskUsage = d.getDiskUsage() != null ? d.getDiskUsage() : "";			
			boolean valueHasChanged = valueHasChanged( alert, d.getDeviceId(), diskUsage );
			
			// If this alert is supposed to alert even if the value hasn't changed -- add this entry to the list of valid entries
			if( alert.getOnlyAlertOnChange() == null || alert.getOnlyAlertOnChange().booleanValue() == false ){
				validDevices.add( d );
			}
			// If this alert is supposed to alert only if the value has changed, AND the value has changed -- add this entry to the list of valid entries
			else if( alert.getOnlyAlertOnChange() != null && alert.getOnlyAlertOnChange().booleanValue() == true && valueHasChanged ){ 
				validDevices.add( d );
			}
						
			// If the value has changed from the previous alert
			if( valueHasChanged )
			{
				// Add the data for this row to the "new" section
				ArrayList<String> rowData = new ArrayList<String>();				
				rowData.add( d.getDeviceName() );
				rowData.add( d.getMacAddr() );
				rowData.add( diskUsage );
				rowData.add( d.getMaxFileStorage() != null ? String.valueOf( d.getMaxFileStorage() ) : "" );
				newData.add( rowData );
			}
			
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();
			rowData.add( d.getDeviceName() );
			rowData.add( d.getMacAddr() );
			rowData.add( diskUsage );
			rowData.add( d.getMaxFileStorage() != null ? String.valueOf( d.getMaxFileStorage() ) : "" );
			allData.add( rowData );
			lastResults.put( d.getDeviceId(), new Object[]{diskUsage, rowData} );
		}	
		
		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, lastResults, 2 );
		
		// If we found any valid entries or any entries that were removed
		boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
		if( (onlyAlertOnChange == false && (validDevices.size() > 0 || removedData.size() > 0 )) ||
			(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
		{			
			// Build the contents of the email
			StringBuffer msg = new StringBuffer();
			msg.append("This alert is active for Devices from " + EMAIL_TIME_FORMAT.format(alert.getActiveStartTime()) + " to " + EMAIL_TIME_FORMAT.format(alert.getActiveEndTime()) + " in the Device's timezone.");
			msg.append("\n\nThe following device(s) have reported a Disk Usage greater than its Max File Storage or a Max File Storage greater than its Disk Capacity:\n\n");
			msg.append("New ("+ newData.size() +"):\n\n");
			msg.append( Emailer.buildEmailSection( colHeaders, newData ) );	
			msg.append("Removed ("+ removedData.size() +"):\n\n");
			msg.append( Emailer.buildEmailSection( colHeaders, removedData ) );			
			msg.append("All ("+ allData.size() +"):\n\n");
			msg.append( Emailer.buildEmailSection( colHeaders, allData ) );					
			
			// Send off the alert
			this.sendAlert( subject, msg.toString(), emailAddresses );				
		}
		
		// Serialize to XML the results of this alert and save them in the database  
		this.serializeLastResults( alert, lastResults );					
	}
}
