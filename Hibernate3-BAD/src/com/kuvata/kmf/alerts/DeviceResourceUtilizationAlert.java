package com.kuvata.kmf.alerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.hibernate.Hibernate;
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
public class DeviceResourceUtilizationAlert extends AlertDefinition 
{	
	private float percentThreshold;	
	
	public DeviceResourceUtilizationAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		super( schemaName, alertId, alertType, alertName, args, frequency );		
		
		// If the correct number of parameters were passed in -- use them
		if( args.length == 1 ) {
			this.percentThreshold = new Float( args[0].trim() ).floatValue();
			this.percentThreshold = this.percentThreshold / 100;
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
				
				Calendar startDatetime = Calendar.getInstance();
				startDatetime.add( Calendar.MINUTE, -240 ); // 4 hours
				
				/*
				 * NOTE: Hibernate does not support subqueries in the FROM clause -- using createSQLQuery
				 * Get all heartbeat events between (now - the specified interval) and now	
				 * Count the number of heartbeat events within this period of time that have a deviceResourceUtilizationMessage
				 * If we found more heartbeatEvents that have a deviceResourceUtilizationMessage than our percentage threshold
				 */				
				String sql = "SELECT a.device_id, totalHeartbeats, heartbeatsWithMessages, maxHeartbeatEvent FROM " 
					+ "(SELECT device_id, COUNT(*) totalHeartbeats FROM heartbeat_event "
					+ "WHERE dt > :startDatetime "; 
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					sql += "AND (device_id IN (SELECT device_id FROM alert_device WHERE device_id IS NOT NULL AND alert_id=:alertId) "
						+ "OR device_id IN ("						
						+ "SELECT device_id FROM device_grp_member "
						+ "INNER JOIN grp_member ON device_grp_member_id = grp_member_id "
						+ "WHERE grp_id IN (SELECT device_grp_id FROM alert_device WHERE device_grp_id IS NOT NULL AND alert_id=:alertId))) ";
				}
				sql += "GROUP BY device_id) a, " 					
					+ "("
					+ "SELECT device_id, 0 as heartbeatsWithMessages FROM heartbeat_event "
					+ "WHERE dt > :startDatetime ";
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					sql += "AND (device_id IN (SELECT device_id FROM alert_device WHERE device_id IS NOT NULL AND alert_id=:alertId) "
						+ "OR device_id IN ("						
						+ "SELECT device_id FROM device_grp_member "
						+ "INNER JOIN grp_member ON device_grp_member_id = grp_member_id "
						+ "WHERE grp_id IN (SELECT device_grp_id FROM alert_device WHERE device_grp_id IS NOT NULL AND alert_id=:alertId))) ";
				}				
				sql += "GROUP BY device_id HAVING device_id NOT IN "
					+ "  (SELECT device_id FROM heartbeat_event WHERE dt > :startDatetime AND device_resource_utilization IS NOT NULL ";
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					sql += "AND (device_id IN (SELECT device_id FROM alert_device WHERE device_id IS NOT NULL AND alert_id=:alertId) "
						+ "OR device_id IN ("						
						+ "SELECT device_id FROM device_grp_member "
						+ "INNER JOIN grp_member ON device_grp_member_id = grp_member_id "
						+ "WHERE grp_id IN (SELECT device_grp_id FROM alert_device WHERE device_grp_id IS NOT NULL AND alert_id=:alertId))) ";
				}	
				sql += "GROUP BY device_id) "
					+ "UNION SELECT device_id, COUNT(*) FROM heartbeat_event WHERE dt > :startDatetime AND device_resource_utilization IS NOT NULL ";
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					sql += "AND (device_id IN (SELECT device_id FROM alert_device WHERE device_id IS NOT NULL AND alert_id=:alertId) "
						+ "OR device_id IN ("						
						+ "SELECT device_id FROM device_grp_member "
						+ "INNER JOIN grp_member ON device_grp_member_id = grp_member_id "
						+ "WHERE grp_id IN (SELECT device_grp_id FROM alert_device WHERE device_grp_id IS NOT NULL AND alert_id=:alertId))) ";
				}	
				sql += "GROUP BY device_id "
					+ ") b, "				
				+ "device c, "
				+ "(SELECT MAX(heartbeat_event_id) maxHeartbeatEvent, device_id FROM heartbeat_event "
				+ "WHERE device_resource_utilization IS NOT NULL GROUP BY device_id) d "
				+ "WHERE heartbeatsWithMessages/totalHeartbeats > :percentThreshold "
				+ "AND a.device_id=b.device_id "
				+ "AND a.device_id=c.device_id "
				+ "AND a.device_id=d.device_id "
				+ "AND c.content_update_type=:contentUpdateType " 
				+ "AND c.apply_alerts=:applyAlerts";								
				
				Session session = HibernateSession.currentSession();
				Query q = session.createSQLQuery( sql )
					.addScalar("device_id", Hibernate.LONG)
					.addScalar("totalHeartbeats", Hibernate.LONG)
					.addScalar("heartbeatsWithMessages", Hibernate.LONG)
					.addScalar("maxHeartbeatEvent", Hibernate.LONG);
				q.setParameter("percentThreshold", this.percentThreshold );
				q.setParameter("startDatetime", startDatetime.getTime() );		
				if( alert.alertSpecificDevices() ){
					q.setParameter("alertId", alertId );
				}
				q.setParameter("contentUpdateType", ContentUpdateType.NETWORK.getPersistentValue() );
				q.setParameter("applyAlerts", Boolean.TRUE );	

				List l = q.list();
				HashMap devices = new HashMap();
				for( Iterator i = l.iterator(); i.hasNext(); )
				{
					Object[] o = (Object[])i.next();
					Device d = Device.getDevice( (Long)o[0] );

					Long[] heartbeatInfo = new Long[3];
					heartbeatInfo[0] = (Long)o[1];
					heartbeatInfo[1] = (Long)o[2];
					heartbeatInfo[2] = (Long)o[3];
					
					// Add the device to the collection of devices to alert as long as it meets all criteria
					if( this.addDevice( d, alert ) ){
						devices.put( d, heartbeatInfo );
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
	private void sendAlert(Alert alert, HashMap devices, String emailAddresses)
	{		
		ArrayList<String> colHeaders = new ArrayList<String>();
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();		
		ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();	
		String subject = alert.getAlertName() +" - Device Resource Utilization Alert";
		
		/*
		 * 1. Determine if each entry is valid (based on the onlyAlertOnChange property)
		 * 2. Build the row data and lastValues collections
		 */
		HashMap<Long, Object[]> lastResults = new HashMap<Long, Object[]>();
		LinkedList<Device> validDevices = new LinkedList<Device>();
		for( Iterator i=devices.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry)i.next();
			Device d = (Device)entry.getKey();
			Long[] heartbeatInfo = (Long[])entry.getValue();
			Long numHeartbeatEvents = heartbeatInfo[0];
			Long heartbeatEventsWithMessage = heartbeatInfo[1];
			Long maxHeartbeatEventId = heartbeatInfo[2];
			String lastHeartbeatValues = "";
			
			// Convert the last heartbeat date to device time
			TimeZone tz = TimeZone.getTimeZone( d.getSafeTimezone() );
			SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
			dateFormat.setTimeZone( tz );	
			
			// Retrieve the heartbeat event associated with this heartbeatEventId
			HeartbeatEvent he = HeartbeatEvent.getHeartbeatEvent( maxHeartbeatEventId );
			if( he != null ){
				lastHeartbeatValues = " Last Heartbeat Values:"+ dateFormat.format( he.getDt() ) +"  "+ he.getDeviceResourceUtilization() +"\n";				
			}
			boolean valueHasChanged = valueHasChanged( alert, d.getDeviceId(), lastHeartbeatValues );
			
			// If this alert is supposed to alert even if the value hasn't changed -- add this entry to the list of valid entries
			if( alert.getOnlyAlertOnChange() == null || alert.getOnlyAlertOnChange().booleanValue() == false ){
				validDevices.add( d );
			}
			// If this alert is supposed to alert only if the value has changed, AND the value has changed -- add this entry to the list of valid entries
			else if( alert.getOnlyAlertOnChange() != null && alert.getOnlyAlertOnChange().booleanValue() == true && valueHasChanged ){ 
				validDevices.add( d );
			}

			String col1Data = d.getDeviceName() +" ("+ d.getMacAddr() +") reported resource utilization values greater than their designated thresholds on "+ heartbeatEventsWithMessage +" out of "+ numHeartbeatEvents +" heartbeats.\n";		
			col1Data += lastHeartbeatValues;
		
			// If the value has changed from the previous alert
			if( valueHasChanged )
			{
				// Add the data for this row to the "new" section
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add( col1Data );
				newData.add( rowData );
			}
			
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();
			rowData.add( col1Data );
			allData.add( rowData );
			lastResults.put( d.getDeviceId(), new Object[]{lastHeartbeatValues, rowData} );						
		}			
						
		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, lastResults, 0 );
		
		// If we found any valid entries or any entries that were removed
		boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
		if( (onlyAlertOnChange == false && (validDevices.size() > 0 || removedData.size() > 0 )) ||
			(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
		{			
			// Build the contents of the email
			StringBuffer msg = new StringBuffer();	
			msg.append("This alert is active for Devices from " + EMAIL_TIME_FORMAT.format(alert.getActiveStartTime()) + " to " + EMAIL_TIME_FORMAT.format(alert.getActiveEndTime()) + " in the Device's timezone.");
			msg.append("\nNew ("+ newData.size() +"):\n\n");
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
