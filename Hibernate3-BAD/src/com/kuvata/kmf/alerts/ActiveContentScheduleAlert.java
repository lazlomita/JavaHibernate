package com.kuvata.kmf.alerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.ContentUpdateType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.ContentSchedule;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.util.Reformat;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.ParseException;
import electric.xml.XPath;

public class ActiveContentScheduleAlert extends AlertDefinition {

	public ActiveContentScheduleAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		super( schemaName, alertId, alertType, alertName, args, frequency );
	}
	
	/**
	 * Implements the parent's abstract method.
	 * Schedules this timer task
	 */
	public void schedule()
	{
		// Schedule this method to run every "frequency" number of minutes
		long millisInMinute = 1000 * 60 * this.frequency;
		
		logger.info("Scheduling active content schedule alert: "+ this.frequency);
		timer.scheduleAtFixedRate( this, getNextRunTime(), millisInMinute );
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
				
				/*
				 * Get all content schedules for each device which
				 * were scheduled with content for the current time
				 */
				String hql = "SELECT cs FROM ContentSchedule cs "
					+ "WHERE trunc(cs.serverStartDt) <= :now "
					+ "AND trunc(cs.serverEndDt) >= :now ";
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					hql += "AND (cs.device.deviceId IN "
					+ "(SELECT alertDevice.device.deviceId FROM AlertDevice alertDevice WHERE alertDevice.device IS NOT NULL AND alertDevice.alert.alertId = :alertId) "
					+ "OR cs.device.deviceId IN "
					+ "(SELECT dgm.device.deviceId FROM DeviceGrpMember as dgm "
					+ "WHERE dgm.grp.grpId IN (SELECT alertDevice.deviceGrp.grpId FROM AlertDevice alertDevice WHERE alertDevice.deviceGrp IS NOT NULL AND alertDevice.alert.alertId = :alertId))) ";
				}
				if(alert.getShowHeartbeatingDevices() != null && alert.getShowHeartbeatingDevices()){
					hql += "AND cs.device.deviceId IN (SELECT he.deviceId FROM HeartbeatEvent he WHERE he.isLastHeartbeat = 1 AND he.dt >= :timeoutDate) ";
				}
				hql += "AND cs.device.contentUpdateType = :contentUpdateType "
					+ "AND cs.device.applyAlerts = :applyAlerts "
					+ "ORDER BY cs.device.deviceId, cs.runDt DESC";
						
				Session session = HibernateSession.currentSession();
				Query q = session.createQuery( hql );
				Date now = new Date();
				
				// 'setDate' will truncate the HQL date value passed as parameter and ignore the hours, minutes, seconds
				// q.setParameter("now", now );
				q.setDate("now", now);
				
				if( alert.alertSpecificDevices() ){
					q.setParameter("alertId", alertId );
				}
				
				Date timeoutDate = null;
				if(alert.getShowHeartbeatingDevices() != null && alert.getShowHeartbeatingDevices()){
					String heartbeatTimeout = Constants.DEFAULT_HEARTBEAT_TIMEOUT;		
					try{
						// Attempt to locate the Alert.heartbeatTimeout property
						heartbeatTimeout = KuvataConfig.getPropertyValue( Constants.ALERT_HEARTBEAT_TIMEOUT );
					}catch(KmfException e){
						logger.info("Could not locate property "+ Constants.ALERT_HEARTBEAT_TIMEOUT +". Using default "+ Constants.DEFAULT_HEARTBEAT_TIMEOUT +" minutes.");
					}
					
					int timeoutMillis = Integer.parseInt(heartbeatTimeout) * 60000;
					timeoutDate = new Date(System.currentTimeMillis() - timeoutMillis);
					q.setParameter("timeoutDate", timeoutDate);
				}
				q.setParameter("contentUpdateType", ContentUpdateType.NETWORK.getPersistentValue() );
				q.setParameter("applyAlerts", Boolean.TRUE );
				List<ContentSchedule> queryResult = q.list();
				queryResult = removeOldContentSchedules(queryResult);
				
				// Build a list of devices we are alerting on
				List<Device> alertDevices = alert.alertSpecificDevices() ? alert.getDevices() : Device.getDevices();
				
				// Build a list of slave devices we are alerting on
				ArrayList<Device> slaveDevices = new ArrayList();
				
				// For each device we are alerting on
				for(Device d : alertDevices){
					// If this device is mirroring a source
					if(d != null && d.getMirrorSource() != null && d.getContentUpdateType().equals(ContentUpdateType.NETWORK) && d.getApplyAlerts()){
						// If we are meant to alert on heartbeating devices only
						if(alert.getShowHeartbeatingDevices() != null && alert.getShowHeartbeatingDevices()){
							// Make sure that the last heartbeat from this slave device is after the timeout date
							if(d.getLastHeartbeatEvent() != null && d.getLastHeartbeatEvent().getDt().after(timeoutDate)){
								slaveDevices.add(d);
							}
						}else{
							slaveDevices.add(d);
						}
					}
				}
				
				// If we are alerting on slave devices
				if(slaveDevices.size() > 0){
					// Group the query result by device (master device) for use by it's slave
					HashMap<Long, ArrayList<ContentSchedule>> hm = new HashMap();
					for(ContentSchedule cs : queryResult){
						ArrayList al = hm.get(cs.getDevice().getDeviceId()) != null ? hm.get(cs.getDevice().getDeviceId()) : new ArrayList();
						al.add(cs);
						hm.put(cs.getDevice().getDeviceId(), al);
					}
					
					// Build a list of master devices which are not a part of the devices this alert is running for
					ArrayList<Long> noCsSlavesMasterDeviceIds = new ArrayList();
					for(Iterator<Device> i=slaveDevices.iterator();i.hasNext();){
						Device slave = i.next();
						// If we don't have content schedule info for the master to this slave
						if(hm.keySet().contains(slave.getMirrorSource().getDeviceId()) == false){
							noCsSlavesMasterDeviceIds.add(slave.getMirrorSource().getDeviceId());
						}
						// If we have content schedule info for it's master
						else{
							// Copy it's master CS and add to the query result
							for(ContentSchedule cs : hm.get(slave.getMirrorSource().getDeviceId())){
								ContentSchedule newCs = cs.copyIntoNewObject();
								newCs.setDevice(slave);
								queryResult.add(newCs);
							}
							// Remove from the list of slave devices
							i.remove();
						}
					}
					
					// Get the CS for each slave that still needs content schedule info
					if(noCsSlavesMasterDeviceIds.size() > 0){
						
						hql = "SELECT cs FROM ContentSchedule cs "
							+ "WHERE cs.serverStartDt <= :now "
							+ "AND cs.serverEndDt > :now "
							+ "AND cs.device.deviceId IN (:noCsSlavesMasterDeviceIds) "
							+ "ORDER BY cs.device.deviceId, cs.runDt DESC";
						
						q = session.createQuery( hql );
						q.setParameter("now", now );
						q.setParameterList("noCsSlavesMasterDeviceIds", noCsSlavesMasterDeviceIds);
						List<ContentSchedule> slaveQueryResult = q.list();
						slaveQueryResult = removeOldContentSchedules(slaveQueryResult);
						
						// Group the query result by device for use by the slave
						hm = new HashMap();
						for(ContentSchedule cs : slaveQueryResult){
							ArrayList al = hm.get(cs.getDevice().getDeviceId()) != null ? hm.get(cs.getDevice().getDeviceId()) : new ArrayList();
							al.add(cs);
							hm.put(cs.getDevice().getDeviceId(), al);
						}
						
						// For each slave missing its CS info
						for(Device slave : slaveDevices){
							// Get masters CS and add to query result
							if(hm.keySet().contains(slave.getMirrorSource().getDeviceId())){
								for(ContentSchedule cs : hm.get(slave.getMirrorSource().getDeviceId())){
									ContentSchedule newCs = cs.copyIntoNewObject();
									newCs.setDevice(slave);
									queryResult.add(newCs);
								}
							}
						}
					}
				}
				
				HashMap<Long, ContentSchedule> mostRecentInactiveContentSchedules = new HashMap<Long, ContentSchedule>();
				HashMap<Long, ContentSchedule> activeContentSchedules = new HashMap<Long, ContentSchedule>();
				Date alertBeforeDate = alert.getParameters() != null ? new Date(now.getTime() - (Integer.parseInt(alert.getParameters()) * 60 * 1000)) : now;
				
				// Populate the hashmap with the most recent content schedule using the runDt of the contentSchedule object
				for( ContentSchedule cs : queryResult ){
					// If this content schedule is different than the active CS the device is reporting
					if(cs.getDevicesActiveContentSchedule().length() == 0 || cs.getDevicesActiveContentSchedule().equals(cs.getFileName()) == false){
						// If this content schedule was run before the alertBeforeDate
						if(cs.getRunDt().before(alertBeforeDate)){
							// If this CS is newer than the active CS
							if(activeContentSchedules.get(cs.getDevice().getDeviceId()) == null || activeContentSchedules.get(cs.getDevice().getDeviceId()).getRunDt().before(cs.getRunDt())){
								// Put the latest CS in the map
								ContentSchedule csInMap = mostRecentInactiveContentSchedules.get(cs.getDevice().getDeviceId());
								if(csInMap == null){
									mostRecentInactiveContentSchedules.put(cs.getDevice().getDeviceId(), cs);
								}else if(cs.getRunDt().after(csInMap.getRunDt())){
									mostRecentInactiveContentSchedules.put(cs.getDevice().getDeviceId(), cs);
								}
							}
						}
					}
					else{
						// This is a match with the active content schedule reported by the device
						activeContentSchedules.put(cs.getDevice().getDeviceId(), cs);
					}
				}
				
				// Sort by CS start date
				List<ContentSchedule> inactiveContentSchedules = new LinkedList(mostRecentInactiveContentSchedules.values());
				BeanPropertyComparator comparator = new BeanPropertyComparator( "activeContentScheduleLocalStartTime" );
				Collections.sort( inactiveContentSchedules, comparator );
				
				// Get the list of email address to send for this alert 
				String emailAddresses = null;
				emailAddresses = alert.buildEmailAddresses();
				
				// If we found any email addresses for this alert
				if( emailAddresses != null && emailAddresses.length() > 0 ) {
					this.sendAlert( alert, inactiveContentSchedules, emailAddresses);
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
	private void sendAlert(Alert alert, List<ContentSchedule> inactiveContentSchedules, String emailAddresses) throws java.text.ParseException
	{			
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		ArrayList<String> colHeaders = new ArrayList<String>();
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
		Date nowDate = new Date();
		String now = dateTimeFormat.format(nowDate);
		
		String subject = alert.getAlertName() +" - Active Content Schedule Alert";	
		colHeaders.add( "Date" );
		colHeaders.add( "Device Name (MAC Address)" ); 
		colHeaders.add( "Active CS" );
		colHeaders.add( "Scheduled CS" );
		
		/*
		 * 1. Determine if each entry is valid (based on the onlyAlertOnChange property)
		 * 2. Build the row data and lastValues collections
		 */
		HashMap<Long, Object[]> lastResults = new HashMap<Long, Object[]>();
		LinkedList<ContentSchedule> validContentSchedules = new LinkedList<ContentSchedule>();
		for( ContentSchedule cs : inactiveContentSchedules ) 
		{
			Object[] o = getValueHasChangedAndDateAdded( alert, cs.getDevice().getDeviceId(), cs.getFileName() );
			boolean valueHasChanged = (Boolean)o[0];
			String dateAdded = (String)o[1];
			
			// If this alert is supposed to alert even if the value hasn't changed -- add this entry to the list of valid entries
			if( alert.getOnlyAlertOnChange() == null || alert.getOnlyAlertOnChange().booleanValue() == false ){
				validContentSchedules.add(cs);
			}
			// If this alert is supposed to alert only if the value has changed, AND the value has changed -- add this entry to the list of valid entries
			else if( alert.getOnlyAlertOnChange() != null && alert.getOnlyAlertOnChange().booleanValue() == true && valueHasChanged ){ 
				validContentSchedules.add( cs );
			}
			
			// If the value has changed from the previous alert
			if( valueHasChanged )
			{
				// Add the data for this row to the "new" section
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add( now );
				rowData.add( cs.getDevice().getDeviceName() + " (" + cs.getDevice().getMacAddr() + ")" );
				rowData.add( cs.getDevicesActiveContentSchedule() );
				rowData.add( cs.getFileName() );
				newData.add( rowData );
			}
				
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();
			String date = dateAdded != null && dateAdded.length() > 0 ? dateAdded : now;
			rowData.add( date );
			rowData.add( cs.getDevice().getDeviceName() + " (" + cs.getDevice().getMacAddr() + ")" );
			rowData.add( cs.getDevicesActiveContentSchedule() );
			rowData.add( cs.getFileName() );
			allData.add( rowData );
			lastResults.put( cs.getDevice().getDeviceId(), new Object[]{cs.getFileName(), rowData} );
		}				

		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, lastResults, 3 );
		
		// If we found any valid entries or any entries that were removed
		boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
		if( (onlyAlertOnChange == false && (validContentSchedules.size() > 0 || removedData.size() > 0 )) ||
			(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
		{			
			// Build the contents of the email
			StringBuffer msg = new StringBuffer();
			msg.append("This alert is active for ");
			if(alert.getShowHeartbeatingDevices() != null && alert.getShowHeartbeatingDevices()){
				msg.append("Heartbeating ");
			}
			msg.append("Devices from " + EMAIL_TIME_FORMAT.format(alert.getActiveStartTime()) + " to " + EMAIL_TIME_FORMAT.format(alert.getActiveEndTime()) + " in the Device's timezone.");
			msg.append("\n\nThe following Device(s) are not playing their latest content:\n\n");
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
	
	// Returns an object array with the first element as the boolean result for valueHasChanged
	// and the second element as the dateAdded
	protected Object[] getValueHasChangedAndDateAdded(Alert alert, Long id, String value)
	{
		boolean result = true;
		String dateAdded = null;

		// If there are any previously saved results for this alert
		String lastResultsXml = this.getLastResults(alert);
		if( lastResultsXml != null && lastResultsXml.length() > 0 ){
			try 
			{
				// Attempt to locate an element associated with the given id
				Document doc = new Document( lastResultsXml );
				Element e = doc.getElement( new XPath("//"+ LAST_RESULT +"[@"+ ID_ATTRIBUTE +"=\""+ id +"\"]"));
				if( e != null )
				{
					// If the value in lastResultsXml is NOT different from the given value -- do not add this device to the alert
					String lastValue = Reformat.getUnescapeXml( e.getAttribute( VALUE_ATTRIBUTE ) );
					
					// We need to make sure that the last value was null for
					// the device to be a part of the new section
					if(lastValue != null && lastValue.length() > 0){
						result = false;
						
						String lastRow = Reformat.getUnescapeXml( e.getAttribute( LAST_ROW_ATTRIBUTE ) );
						
						// Parse out the date from the last row
						dateAdded = lastRow.split(Constants.UNIQUE_DELIMITER)[0];
					}
				}
			} catch (ParseException e) {
				logger.error( e );
			}
		}

		return new Object[]{new Boolean(result), dateAdded};
	}
	
	// Since the list of content schedules being passed in is sorted by runDt desc,
	// we need to keep only the first CS for each device.
	private List<ContentSchedule> removeOldContentSchedules(List<ContentSchedule> l){
		ArrayList<Long> uniqueDeviceIds = new ArrayList<Long>();
		for(Iterator<ContentSchedule> i = l.iterator(); i.hasNext();){
			ContentSchedule cs = i.next();
			if(uniqueDeviceIds.contains(cs.getDevice().getDeviceId()) == false){
				uniqueDeviceIds.add(cs.getDevice().getDeviceId());
			}else{
				i.remove();
			}
		}
		return l;
	}
}
