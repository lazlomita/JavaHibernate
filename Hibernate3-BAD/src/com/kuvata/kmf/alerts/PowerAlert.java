package com.kuvata.kmf.alerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.usertype.McmCommandType;
import parkmedia.usertype.PeripheralType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.McmHistoryEntry;
import com.kuvata.kmf.SchemaDirectory;

/**
 * Determines if a display device's power is on or off, given a time of day local to the display
 * 
 * @author jrandesi
 */
public class PowerAlert extends AlertDefinition 
{
	private String desiredPower = "";
		
	public PowerAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		super( schemaName, alertId, alertType, alertName, args, frequency );
		
		// If the correct number of parameters were passed in -- use them
		if( args.length == 1 ) {
			this.desiredPower = args[0];
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
		SchemaDirectory.initialize( this.schemaName, "PowerAlert", "Power Alert", false, false);
		try
		{		
			HibernateSession.clearCache();
			Alert alert = Alert.getAlert( this.alertId );
			if( alert != null )
			{
				// Set the last run date of the alert
				alert.setLastRunDt(new Date());
				alert.update();
				
				/*
				 * Retrieve the most recent mcm history entry
				 * and it meets the criteria based on the desiredPower
				 */
				String hql = "";
				String criteria = "";
				Calendar cMinimumTime = Calendar.getInstance();
				if( alert.getMinimumTime() != null ){
					cMinimumTime.add( Calendar.MINUTE, -alert.getMinimumTime() );
				}
				
				// If the specified power is blank -- search where IS NULL
				if( this.desiredPower == null || this.desiredPower.trim().length() == 0 ){
					criteria = " IS NULL";
				}else{
					criteria = "="+ this.desiredPower;
				}
				hql = "SELECT mcmHistoryEntry FROM McmHistoryEntry mcmHistoryEntry "
					+"WHERE mcmHistoryEntry.power "+ criteria +" "
					+"AND mcmHistoryEntry.isLastMcmHistory = 1 ";					
				if( alert.alertSpecificDevices() )
				{
					// Get all devices that are either in the specified device group or are individual members of this alert
					hql += "AND (mcmHistoryEntry.mcm.device.deviceId IN "
					+ "(SELECT alertDevice.device.deviceId FROM AlertDevice alertDevice WHERE alertDevice.device IS NOT NULL AND alertDevice.alert.alertId = :alertId) "
					+ "OR mcmHistoryEntry.mcm.device.deviceId IN "
					+ "(SELECT dgm.device.deviceId FROM DeviceGrpMember as dgm "
					+ "WHERE dgm.grp.grpId IN (SELECT alertDevice.deviceGrp.grpId FROM AlertDevice alertDevice WHERE alertDevice.deviceGrp IS NOT NULL AND alertDevice.alert.alertId = :alertId))) ";
				}
				if( alert.getAlertIfContentScheduled() != null && alert.getAlertIfContentScheduled() ){
					hql += "AND mcmHistoryEntry.mcm.device.deviceId NOT IN (SELECT DISTINCT cse.deviceId FROM ContentScheduleEvent cse WHERE cse.assetName = '" + Constants.NO_CONTENT_EVENT_ASSET_NAME + "' "
					+ "AND cse.startDatetime < :now AND cse.endDatetime > :now) ";
				}
				hql += "AND mcmHistoryEntry.mcm.device.contentUpdateType = 'Network' "
					+ "AND mcmHistoryEntry.mcm.device.applyAlerts = 1 "
					+ "AND mcmHistoryEntry.mcm.peripheralType IN ('"+ PeripheralType.DISPLAY +"') "
					+ "ORDER BY mcmHistoryEntry.timestamp DESC, UPPER(mcmHistoryEntry.mcm.device.deviceName), UPPER(mcmHistoryEntry.mcm.mcmName)";
						
				Session session = HibernateSession.currentSession();
				Query q = session.createQuery( hql );
				if( alert.alertSpecificDevices() ){
					q.setParameter("alertId", alertId );
				}
				if(alert.getAlertIfContentScheduled() != null && alert.getAlertIfContentScheduled()){
					q.setParameter("now", new Date());
				}
					
				List<McmHistoryEntry> l = q.list();
				HashMap<Long, McmHistoryEntry> mcmHistoryEntries = new HashMap<Long, McmHistoryEntry>();
				for( Iterator<McmHistoryEntry> i=l.iterator(); i.hasNext(); )
				{					
					// Add the device to the collection of devices to alert as long as it meets all criteria
					McmHistoryEntry mcmHistoryEntry = i.next();
					if( this.addDevice( mcmHistoryEntry.getMcm().getDevice(), alert ) )
					{
						// If minimumTime is set for this alert
						boolean isValidDevice = true;
						if( alert.getMinimumTime() != null )
						{
							// Adjust minimum time using devices timezone
							Calendar minTime = (Calendar)cMinimumTime.clone();
							minTime.add(Calendar.SECOND, mcmHistoryEntry.getMcm().getDevice().getTimezoneAdjustment());
							
							// Get the next most recent mcmHistoryEntry where power is not desiredPower
							// If the specified power is blank -- search where IS NOT NULL
							if( this.desiredPower == null || this.desiredPower.trim().length() == 0 ){
								criteria = " IS NOT NULL";
							}else if(this.desiredPower.equals("0")){
								criteria = "IS NOT NULL AND mcmHistoryEntry.power = '1' ";
							}else if(this.desiredPower.equals("1")){
								criteria = "IS NULL OR mcmHistoryEntry.power = '0' ";
							}
							
							// If there is at least one entry in past "minimumTime" with our desiredValue -- do not add the device							
							String hql2 = "SELECT mcmHistoryEntry FROM McmHistoryEntry mcmHistoryEntry "
								+ "WHERE mcmHistoryEntry "+ criteria +" " 
								+ "AND mcmHistoryEntry.timestamp > :param2 "
								+ "AND mcmHistoryEntry.mcm.mcmId = :param3 "
								+ "ORDER BY mcmHistoryEntry.timestamp DESC";
							List<McmHistoryEntry> l2 = session.createQuery( hql2 )
								.setParameter("param2", minTime.getTime())
								.setParameter("param3", mcmHistoryEntry.getMcm().getMcmId())
								.setMaxResults(1).list();	
							
							// If we found at least one row that is different from the desiredPower
							if( l2.size() > 0 ){
								isValidDevice = false;
							}else{
								// Make sure that the previous value was the desired value 
								String hql3 = "SELECT mcmHistoryEntry FROM McmHistoryEntry mcmHistoryEntry "
									+ "WHERE mcmHistoryEntry.timestamp <= :param1 "
									+ "AND mcmHistoryEntry.mcm.mcmId = :param2 "
									+ "ORDER BY mcmHistoryEntry.timestamp DESC";
								List<McmHistoryEntry> l3 = session.createQuery(hql3)
														.setParameter("param1", minTime.getTime())
														.setParameter("param2", mcmHistoryEntry.getMcm().getMcmId())
														.setMaxResults(1).list();
								if(l3.size() > 0){
									McmHistoryEntry lastEntry = l3.get(0);
									if( this.desiredPower == null || this.desiredPower.trim().length() == 0 ){
										isValidDevice = lastEntry == null;
									}else if(this.desiredPower.equals("0")){
										isValidDevice = lastEntry == null || lastEntry.getPower().equals("0");
									}else if(this.desiredPower.equals("1")){
										isValidDevice = lastEntry != null && lastEntry.getPower().equals("1");
									}
								}
							}
						}						
						
						// If this is a valid device and we have not already added this mcm 
						if( isValidDevice && mcmHistoryEntries.get( mcmHistoryEntry.getMcm().getMcmId() ) == null ){
							mcmHistoryEntries.put( mcmHistoryEntry.getMcm().getMcmId(), mcmHistoryEntry );
						}
					}											
				}				
				
				// Get the list of email address to send for this alert 				
				String emailAddresses = alert.buildEmailAddresses();
					
				// If we found any email addresses for this alert
				if( emailAddresses != null && emailAddresses.length() > 0 ) {
					this.sendAlert( alert, mcmHistoryEntries, emailAddresses);
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
	private void sendAlert(Alert alert, HashMap<Long, McmHistoryEntry> mcmHistoryEntries, String emailAddresses) throws InterruptedException
	{
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		ArrayList<String> colHeaders = new ArrayList<String>();
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
		boolean fixAttempted = false;
		
		String subject = alert.getAlertName() +" - Power Alert";		
		colHeaders.add( "Date" ); 
		colHeaders.add( "Device Name (MAC Address - MCM Name)" ); 
		colHeaders.add( "Current Power" );
		
		/*
		 * 1. Determine if each entry is valid (based on the onlyAlertOnChange property)
		 * 2. Build the row data and lastValues collections
		 */
		HashMap<Long, Object[]> lastResults = new HashMap<Long, Object[]>();
		LinkedList<McmHistoryEntry> validMcmHistoryEntries = new LinkedList<McmHistoryEntry>();
		for( Iterator<McmHistoryEntry> i=mcmHistoryEntries.values().iterator(); i.hasNext(); ) 
		{
			McmHistoryEntry mcmHistoryEntry = i.next();			
			Device device = mcmHistoryEntry.getMcm().getDevice();			
			String power = mcmHistoryEntry.getPower() != null ? mcmHistoryEntry.getPower() : "";
			String dt = dateTimeFormat.format( mcmHistoryEntry.getTimestamp() );
			String col2 = device.getDeviceName() +" ("+ device.getMacAddr() +" - "+ mcmHistoryEntry.getMcm().getMcmName() +")";
			boolean valueHasChanged = valueHasChanged( alert, mcmHistoryEntry.getMcm().getMcmId(), power );
			
			// If this alert is supposed to alert even if the value hasn't changed -- add this entry to the list of valid entries
			if( alert.getOnlyAlertOnChange() == null || alert.getOnlyAlertOnChange().booleanValue() == false ){
				validMcmHistoryEntries.add( mcmHistoryEntry );
			}
			// If this alert is supposed to alert only if the value has changed, AND the value has changed -- add this entry to the list of valid entries
			else if( alert.getOnlyAlertOnChange() != null && alert.getOnlyAlertOnChange().booleanValue() == true && valueHasChanged ){ 
				validMcmHistoryEntries.add( mcmHistoryEntry );
			}
			
			// If the value has changed from the previous alert
			if( valueHasChanged )
			{
				// Add the data for this row to the "new" section
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add( dt );
				rowData.add( col2 );
				rowData.add( power );
				newData.add( rowData );
				
				// If we are supposed to make an attempt to fix this mcm
				if( alert.getAttemptToFix() != null && alert.getAttemptToFix().booleanValue() == true )
				{
					// If an alert is being sent because the monitor is powered off
					if( this.desiredPower != null ){
						if( this.desiredPower.equals("0") )
						{
							// Send an mcm command to try to power on the monitor
							mcmHistoryEntry.getMcm().addMcmCommand( McmCommandType.POWER_ON, null, false, false );
							fixAttempted = true;
						}
						// If an alert is being sent because the monitor is powered on
						else if( this.desiredPower.equals("1") )
						{
							// Send an mcm command to try to power off the monitor
							mcmHistoryEntry.getMcm().addMcmCommand( McmCommandType.POWER_OFF, null, false, false );
							fixAttempted = true;
						}	
					}
				}				
			}
			
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();
			rowData.add( dt );
			rowData.add( col2 );
			rowData.add( power );
			allData.add( rowData );		
			lastResults.put( mcmHistoryEntry.getMcm().getMcmId(), new Object[]{power, rowData} );
		}	
		
		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, lastResults, 2 );
		
		// If we found any valid entries or any entries that were removed
		boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
		if( (onlyAlertOnChange == false && (validMcmHistoryEntries.size() > 0 || removedData.size() > 0 )) ||
			(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
		{		
			// Build the contents of the email
			StringBuffer msg = new StringBuffer();
			msg.append("This alert is active for Devices from " + EMAIL_TIME_FORMAT.format(alert.getActiveStartTime()) + " to " + EMAIL_TIME_FORMAT.format(alert.getActiveEndTime()) + " in the Device's timezone.");
			msg.append("\n\nOne or more displays attached to the following device(s) are set to the power status of '"+ this.desiredPower +"'");
			if( alert.getMinimumTime() != null ){
				msg.append(" for at least "+ alert.getMinimumTime() +" minutes");
			}
			if( fixAttempted ){
				msg.append(". Attempting to fix all New Devices");
			}			
			msg.append(".\n\n");			
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
