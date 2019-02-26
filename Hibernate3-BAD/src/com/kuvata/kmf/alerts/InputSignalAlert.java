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
public class InputSignalAlert extends AlertDefinition 
{		
	private String desiredInput = "";
	
	public InputSignalAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		super( schemaName, alertId, alertType, alertName, args, frequency );
		
		// If the correct number of parameters were passed in -- use them
		if( args.length == 1 ) {
			this.desiredInput = args[0];
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
	 * If a Device's display is not switched to the appropriate input signal
	 * email the appropriate users.
	 * 
	 * @param threshold
	 */
	public void runAlert()
	{
		SchemaDirectory.initialize( this.schemaName, "InputSignalAlert", "Input Signal Alert", false, false);
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
				 * for any mcm's that do not have it's brightness value set to the specified brightness (as defined by the alert)
				 */
				Calendar cMinimumTime = Calendar.getInstance();
				if( alert.getMinimumTime() != null ){
					cMinimumTime.add( Calendar.MINUTE, -alert.getMinimumTime() );
				}
				String hql = "SELECT mcmHistoryEntry FROM McmHistoryEntry mcmHistoryEntry "
					+"WHERE mcmHistoryEntry.input IS NOT NULL AND mcmHistoryEntry.input != :input "
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
				hql += "AND mcmHistoryEntry.mcm.device.contentUpdateType = 'Network' "
					+ "AND mcmHistoryEntry.mcm.device.applyAlerts = 1 "
					+ "AND mcmHistoryEntry.mcm.peripheralType IN ('"+ PeripheralType.DISPLAY +"') "
					+ "ORDER BY mcmHistoryEntry.timestamp DESC, UPPER(mcmHistoryEntry.mcm.device.deviceName), UPPER(mcmHistoryEntry.mcm.mcmName)";
						
				Session session = HibernateSession.currentSession();
				Query q = session.createQuery( hql )
					.setParameter("input", this.desiredInput );
				if( alert.alertSpecificDevices() ){
					q.setParameter("alertId", alertId );
				}		
					
				List l = q.list();
				HashMap<Long, McmHistoryEntry> mcmHistoryEntries = new HashMap<Long, McmHistoryEntry>();
				for( Iterator<McmHistoryEntry> i=l.iterator(); i.hasNext(); )
				{					
					// Add the device to the collection of devices to alert as long as it meets all criteria
					McmHistoryEntry mcmHistoryEntry  = i.next();
					if( this.addDevice( mcmHistoryEntry.getMcm().getDevice(), alert ) )
					{
						// If minimumTime is set for this alert
						boolean isValidDevice = true;
						if( alert.getMinimumTime() != null )
						{
							// If there is at least one entry in past "minimumTime" with our desiredValue -- do not add the device							
							String hql2 = "SELECT mcmHistoryEntry FROM McmHistoryEntry mcmHistoryEntry "
								+ "WHERE mcmHistoryEntry IS NOT NULL AND mcmHistoryEntry.input = :param1 " 
								+ "AND mcmHistoryEntry.timestamp > :param2 "
								+ "AND mcmHistoryEntry.mcm.mcmId = :param3 "
								+ "ORDER BY mcmHistoryEntry.timestamp DESC";
							List<McmHistoryEntry> l2 = session.createQuery( hql2 )
								.setParameter("param1", this.desiredInput)
								.setParameter("param2", cMinimumTime.getTime())
								.setParameter("param3", mcmHistoryEntry.getMcm().getMcmId())
								.setMaxResults(1).list();	
							
							// If we found at least one row that is different from the desiredPower
							if( l2.size() > 0 ){
								isValidDevice = false;
							}
						}	
						
						// If we have not already added this mcm 
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
		
		String subject = alert.getAlertName() +" - Input Signal Alert";		
		colHeaders.add( "Date" ); 
		colHeaders.add( "Device Name (MAC Address - MCM Name)" ); 
		colHeaders.add( "Current Input" );

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
			String inputSignal = mcmHistoryEntry.getInput() != null ? mcmHistoryEntry.getInput() : "";
			String dt = dateTimeFormat.format( mcmHistoryEntry.getTimestamp() );
			String col2 = device.getDeviceName() +" ("+ device.getMacAddr() +" - "+ mcmHistoryEntry.getMcm().getMcmName() +")";
			boolean valueHasChanged = valueHasChanged( alert, mcmHistoryEntry.getMcm().getMcmId(), inputSignal );
			
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
				rowData.add( inputSignal );
				newData.add( rowData );
				
				// If we are supposed to make an attempt to fix this mcm
				if( alert.getAttemptToFix() != null && alert.getAttemptToFix().booleanValue() == true )
				{
					// Send an mcm command to try to set this property to the desired value
					mcmHistoryEntry.getMcm().addMcmCommand( McmCommandType.CHANGE_INPUT_SIGNAL, this.desiredInput, false, false );
					fixAttempted = true;
				}				
			}
			
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();
			rowData.add( dt );
			rowData.add( col2 );
			rowData.add( inputSignal );
			allData.add( rowData );		
			lastResults.put( mcmHistoryEntry.getMcm().getMcmId(), new Object[]{inputSignal, rowData} );
		}			
		
		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, lastResults, 1 );
		
		// If we found any valid entries or any entries that were removed
		boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
		if( (onlyAlertOnChange == false && (validMcmHistoryEntries.size() > 0 || removedData.size() > 0 )) ||
			(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
		{		
			// Build the contents of the email
			StringBuffer msg = new StringBuffer();
			msg.append("This alert is active for Devices from " + EMAIL_TIME_FORMAT.format(alert.getActiveStartTime()) + " to " + EMAIL_TIME_FORMAT.format(alert.getActiveEndTime()) + " in the Device's timezone.");
			msg.append("\n\nOne or more displays attached to the following device(s) are not set to the correct input signal ("+ this.desiredInput +")");
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
