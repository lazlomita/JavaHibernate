package com.kuvata.kmf.alerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.usertype.ContentUpdateType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HeartbeatEvent;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

/**
 * Determines if one or more devices have not heartbeated
 * within a given threshold and sends an alert if necessary.
 * 
 * @author jrandesi
 */
public class HeartbeatAlert extends AlertDefinition 
{
	public static final String HOUR_FORMAT = "h";
	public static final String MINUTE_FORMAT = "mm";
	public static final String AMPM_FORMAT = "a";	
	private String threshold = "15";	// Default 15 minutes
	

		
	public HeartbeatAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		super( schemaName, alertId, alertType, alertName, args, frequency );
		
		// If the correct number of parameters were passed in -- use it
		if( args.length == 1 ) {
			this.threshold = args[0];
		}
	}
	
	/**
	 * Implements the parent's abstract method.
	 * Schedules this timer task to run every 30 minutes
	 */
	public void schedule()
	{
		// Schedule this method to run every "frequency" number of minutes
		long millisInMinute = 1000 * 60 * this.frequency;
		
		logger.info("Scheduling heartbeat alert: "+ this.frequency);
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
				// Since this alert runs based on server time,
				// make sure we are in the alert time window
				Calendar calAlertStartDateTime = Calendar.getInstance();
				calAlertStartDateTime.setTime( alert.getActiveStartTime() );
				Calendar calAlertEndDateTime = Calendar.getInstance();
				calAlertEndDateTime.setTime( alert.getActiveEndTime() );				
				Calendar calCurrentTime = new GregorianCalendar();				
				int currentHour = calCurrentTime.get( Calendar.HOUR_OF_DAY );
				int currentMinute = calCurrentTime.get( Calendar.MINUTE );
				int alertStartHour = calAlertStartDateTime.get( Calendar.HOUR_OF_DAY );		
				int alertStartMinute = calAlertStartDateTime.get( Calendar.MINUTE );
				int alertEndHour = calAlertEndDateTime.get( Calendar.HOUR_OF_DAY );		
				int alertEndMinute = calAlertEndDateTime.get( Calendar.MINUTE );			
				if( ( (currentHour > alertStartHour || (currentHour == alertStartHour && currentMinute >= alertStartMinute))
						&& (currentHour < alertEndHour || (currentHour == alertEndHour && currentMinute <= alertEndMinute)) ) == false ){
					logger.info("Alert '" + this.alertName + "' can not be executed at this time");
					return;
				}
				
				// Set the last run date of the alert
				alert.setLastRunDt(new Date());
				alert.update();
				
				// Calculate the threshold date to compare the lastHeartbeat dates 
				int threshold = new Integer( this.threshold ).intValue();
				Calendar c = Calendar.getInstance();
				c.add( Calendar.MINUTE, -(threshold) );
				
				/*
				 * Get the last heartbeat event for each device
				 * If the last heartbeat was more that 15 minutes ago
				 */
				String hql = "SELECT device FROM HeartbeatEvent heartbeatEvent, Device device "
					+ "WHERE heartbeatEvent.isLastHeartbeat = 1 "
					+ "AND heartbeatEvent.deviceId = device.deviceId "
					+ "AND heartbeatEvent.dt < :startDatetime ";					
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
					+ "ORDER BY heartbeatEvent.dt desc, UPPER(device.deviceName)";										
						
				Session session = HibernateSession.currentSession();
				Query q = session.createQuery( hql );
				q.setParameter("startDatetime", c.getTime() );
				if( alert.alertSpecificDevices() ){
					q.setParameter("alertId", alertId );
				}	
				q.setParameter("contentUpdateType", ContentUpdateType.NETWORK.getPersistentValue() );
				q.setParameter("applyAlerts", Boolean.TRUE );				
				
				List l = q.list();
				LinkedList<Device> devices = new LinkedList<Device>();
				for( Iterator<Device> i=l.iterator(); i.hasNext(); )
				{					
					// Add the device to the collection of devices to alert as long as it meets all criteria
					Device device = i.next();
					if( this.addDeviceHeartbeat( device, alert ) ){
						devices.add( device );
					}
				}
				
				// Get the list of email address to send for this alert 
				String emailAddresses = null;
				emailAddresses = alert.buildEmailAddresses();
					
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
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
		
		/*
		 * 1. Determine if each entry is valid (based on the onlyAlertOnChange property)
		 * 2. Build the row data and lastValues collections
		 */
		HashMap<Long, Object[]> lastResults = new HashMap<Long, Object[]>();
		LinkedList<Device> validDevices = new LinkedList<Device>();
		for( Iterator<Device> i=devices.iterator(); i.hasNext(); ) 
		{
			Device d = i.next();
			String lastHeartbeat = "";			
			HeartbeatEvent he = d.getLastHeartbeatEvent(true);							
			if( he != null ) 
			{
				// Convert the last heartbeat date to device time
				TimeZone tz = TimeZone.getTimeZone( d.getSafeTimezone() );
				dateTimeFormat.setTimeZone( tz );							
				lastHeartbeat = dateTimeFormat.format( he.getDt() );
			}						
			boolean valueHasChanged = valueHasChanged( alert, d.getDeviceId(), lastHeartbeat );
			
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
				rowData.add( lastHeartbeat );
				newData.add( rowData );
			}
				
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();			
			rowData.add( d.getDeviceName() );
			rowData.add( d.getMacAddr() );
			rowData.add( lastHeartbeat );
			allData.add( rowData );
			lastResults.put( d.getDeviceId(), new Object[]{lastHeartbeat, rowData} );
		}				

		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, lastResults, 2 );
		
		// Alert only if we are over the threshold of devices
		if(alert.getNumDevices() == null || alert.getNumDevices() <= allData.size() || alert.getNumDevices() <= allData.size() + removedData.size() - newData.size()){
			
			// If we found any valid entries or any entries that were removed
			boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
			if( (onlyAlertOnChange == false && (validDevices.size() > 0 || removedData.size() > 0 )) ||
				(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
			{
				ArrayList<String> colHeaders = new ArrayList<String>();
				String subject = alert.getAlertName() +" - Heartbeat Alert";				 
				colHeaders.add( "Device Name" ); 
				colHeaders.add( "MAC Address" );
				colHeaders.add( "Last Heartbeat" );
				
				// Build the contents of the email
				StringBuffer msg = new StringBuffer();
				msg.append("This alert is active for Devices from " + EMAIL_TIME_FORMAT.format(alert.getActiveStartTime()) + " to " + EMAIL_TIME_FORMAT.format(alert.getActiveEndTime()) + " in the Device's timezone.");
				msg.append("\n\nThe following device(s) have not issued a heartbeat to the server in the past "+ this.threshold +" minutes:\n\n");
				msg.append("New ("+ newData.size() +"):\n\n");
				msg.append( Emailer.buildEmailSection( colHeaders, newData ) );	
				msg.append("Removed ("+ removedData.size() +"):\n\n");
				msg.append( Emailer.buildEmailSection( colHeaders, removedData ) );			
				msg.append("All ("+ allData.size() +"):\n\n");
				msg.append( Emailer.buildEmailSection( colHeaders, allData ) );

				// Send off the alert
				this.sendAlert( subject, msg.toString(), emailAddresses );				
				
				// Serialize to XML the results of this alert and save them in the database  
				this.serializeLastResults( alert, lastResults );
			}
		}
		
		// If the custom metadata fields are defined for a device, send out a custom email based on what the values of the metadata fields are
		for (Long deviceId : lastResults.keySet()) {
		    
			try {
				Device d = Device.getDevice(deviceId);
				String alertText = d.getMetadataInfo(Device.class.getName(), Constants.HEARTBEAT_ALERT_TEXT);
				String toAddress = d.getMetadataInfo(Device.class.getName(), Constants.HEARTBEAT_ALERT_TO_EMAIL);
				String fromAddress = d.getMetadataInfo(Device.class.getName(), Constants.HEARTBEAT_ALERT_FROM_EMAIL);
				String fromName = d.getMetadataInfo(Device.class.getName(), Constants.HEARTBEAT_ALERT_FROM_NAME);
								
				if (alertText != null && alertText.length() > 0 &&
					toAddress != null && toAddress.length() > 0 &&
					fromAddress != null && fromAddress.length() > 0 &&
					validDevices.indexOf(d) >= 0) {						
										
					String subject = "Heartbeat Alert for " + d.getDeviceName();
					this.sendCustomAlert(subject, alertText, fromAddress, fromName, toAddress);
				}
			}
			catch(Exception e) {
				logger.error(e);				
			}
		}
	}
}
