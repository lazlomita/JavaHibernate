/*
 * Created on Dec 1, 2004
 *
 * Kuvata, Inc.
 */
package com.kuvata.kmf.alerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.hibernate.Hibernate;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.AlertDefinitionType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HeartbeatEvent;
import com.kuvata.kmf.Mcm;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.util.Reformat;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

/**
 * @author Jeff Randesi
 *
 */
public abstract class AlertDefinition extends TimerTask
{			
	protected static final KMFLogger logger = (KMFLogger)KMFLogger.getInstance( AlertDefinition.class );
	protected static final String LAST_RESULT = "LastResult";
	protected static final String LAST_RESULTS = "LastResults";
	protected static final String ID_ATTRIBUTE = "id";
	protected static final String VALUE_ATTRIBUTE = "value";
	protected static final String LAST_ROW_ATTRIBUTE = "last_row";
	protected static final SimpleDateFormat EMAIL_TIME_FORMAT = new SimpleDateFormat("hh:mm a");
	protected String schemaName;
	protected String[] args = null;
	protected Integer frequency = null;
	protected Long alertId = null;
	protected String alertType = null;
	protected String alertName = null;
	public abstract void schedule();
	public abstract void runAlert();
	public static Integer alertLock = new Integer( 0 );
	
	// Common timer used to run all alerts
	protected static Timer timer = new Timer();
			
	public AlertDefinition(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{		
		this.schemaName = schemaName;
		this.alertId = alertId;
		this.alertType = alertType;
		this.alertName = alertName;
		this.args = args;
		this.frequency = frequency;
	}
	
	public void run()
	{
		SchemaDirectory.setup("kuvata", "AlertDefinition");
		
		synchronized( alertLock ){
			try {
				// Run the alert
				logger.info("About to run '" + this.alertType + "' named '" + this.alertName + "' with id=" + this.alertId);
				runAlert();
				logger.info("Finished running '" + this.alertType + "' named '" + this.alertName + "' with id=" + this.alertId);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}	
	
	/**
	 * Cancels this timer task
	 */
	public void stop()
	{		
		this.cancel();	
	}
	
	/**
	 * Determines if the given device should be added to the list of devices to alert for the given alert
	 * @param device
	 * @param alert
	 * @return
	 */
	protected boolean addDevice(Device device, Alert alert) 
	{
		// Make sure that we are meant to alert on this device and we're within the active start and end times for this alert/device
		boolean addDevice = false;
		if( device.getApplyAlerts() != null && device.getApplyAlerts() && alert.getActiveStartTime() != null && alert.getActiveEndTime() != null )
		{				
			Calendar calAlertStartDateTime = Calendar.getInstance();
			calAlertStartDateTime.setTime( alert.getActiveStartTime() );
			Calendar calAlertEndDateTime = Calendar.getInstance();
			calAlertEndDateTime.setTime( alert.getActiveEndTime() );				
			Calendar calDeviceTime = new GregorianCalendar( TimeZone.getTimeZone( device.getSafeTimezone() ));				
			int deviceHour = calDeviceTime.get( Calendar.HOUR_OF_DAY );
			int deviceMinute = calDeviceTime.get( Calendar.MINUTE );
			int alertStartHour = calAlertStartDateTime.get( Calendar.HOUR_OF_DAY );		
			int alertStartMinute = calAlertStartDateTime.get( Calendar.MINUTE );
			int alertEndHour = calAlertEndDateTime.get( Calendar.HOUR_OF_DAY );		
			int alertEndMinute = calAlertEndDateTime.get( Calendar.MINUTE );
			
			if( (deviceHour > alertStartHour || (deviceHour == alertStartHour && deviceMinute >= alertStartMinute))
					&& (deviceHour < alertEndHour || (deviceHour == alertEndHour && deviceMinute <= alertEndMinute)) ){
				addDevice = true;
			}else{
				addDevice = false;
			}
		}
		return addDevice;
	}	
	
	protected boolean addDeviceHeartbeat(Device device, Alert alert) 
	{
		// Make sure that we are meant to alert on this device and we're within the active start and end times for this alert/device
		boolean addDevice = false;
		if( device.getApplyAlerts() != null && device.getApplyAlerts() && alert.getActiveStartTime() != null && alert.getActiveEndTime() != null )
		{				
			Calendar calAlertStartDateTime = Calendar.getInstance();
			calAlertStartDateTime.setTime( alert.getActiveStartTime() );
			Calendar calAlertEndDateTime = Calendar.getInstance();
			calAlertEndDateTime.setTime( alert.getActiveEndTime() );				
			Calendar calDeviceTime = new GregorianCalendar( TimeZone.getTimeZone( device.getSafeTimezone() ));				
			int deviceHour = calDeviceTime.get( Calendar.HOUR_OF_DAY );
			int deviceMinute = calDeviceTime.get( Calendar.MINUTE );
			int alertStartHour = calAlertStartDateTime.get( Calendar.HOUR_OF_DAY );		
			int alertStartMinute = calAlertStartDateTime.get( Calendar.MINUTE );
			int alertEndHour = calAlertEndDateTime.get( Calendar.HOUR_OF_DAY );		
			int alertEndMinute = calAlertEndDateTime.get( Calendar.MINUTE );

			HeartbeatEvent he = device.getLastHeartbeatEvent(true);
			Calendar deviceTime = (Calendar)calDeviceTime.clone();
			deviceTime.add(Calendar.HOUR_OF_DAY, -24);
			if (he.getDt().before(deviceTime.getTime())) {
				addDevice = true;
			}
			else {
				if( (deviceHour > alertStartHour || (deviceHour == alertStartHour && deviceMinute >= alertStartMinute))
						&& (deviceHour < alertEndHour || (deviceHour == alertEndHour && deviceMinute <= alertEndMinute)) ){
					addDevice = true;
				}else{
					addDevice = false;
				}
			}
		}
		return addDevice;
	}
	
	/**
	 * Creates a block of XML containing the values of the "last results"
	 * which will be stored in the alert object.  
	 * @param alert
	 * @param lastValues
	 */
	protected void serializeLastResults(Alert alert, HashMap<Long, Object[]> lastValues)
	{
		StringBuffer result = new StringBuffer();
		result.append("<"+ LAST_RESULTS +">");
		for( Iterator<Entry<Long, Object[]>> i=lastValues.entrySet().iterator(); i.hasNext(); )
		{
			Entry<Long, Object[]> entry = i.next();
			String lastValue = (String)entry.getValue()[0];
			ArrayList<String> rowData = (ArrayList<String>)entry.getValue()[1];
			String lastRow = "";
			for( int j=0; j<rowData.size(); j++ ){
				lastRow += rowData.get( j );
				if( j !=rowData.size() - 1 ){
					lastRow += Constants.UNIQUE_DELIMITER;
				}				
			}
			result.append("<"+ LAST_RESULT +" ");
			result.append( ID_ATTRIBUTE +"=\""+ Reformat.getEscapeXml( entry.getKey().toString() ) +"\" " );
			result.append( VALUE_ATTRIBUTE +"=\""+ Reformat.getEscapeXml( lastValue ) +"\" " );
			result.append( LAST_ROW_ATTRIBUTE +"=\""+ Reformat.getEscapeXml( lastRow ) +"\" " );
			result.append("/>");			
		}
		result.append("</"+ LAST_RESULTS +">");
		
		// Update the lastValues property of this alert object
		alert.setLastResults( Hibernate.createClob( result.toString() ) );
		alert.update();
		cachedLastResults = null;
	}
	
	protected boolean valueHasChanged(Alert alert, Long id, String value)
	{
		boolean result = true;

		// If there are any previously saved results for this alert
		String lastResultsXml = this.getLastResults(alert);
		if( lastResultsXml == null || lastResultsXml.length() == 0 ) return true;
			
		try 
		{
			// Attempt to locate an element associated with the given id
			Document doc = new Document( lastResultsXml );
			Element e = doc.getElement( new XPath("//"+ LAST_RESULT +"[@"+ ID_ATTRIBUTE +"=\""+ id +"\"]"));
			if( e != null )
			{
				// If the value in lastResultsXml is NOT different from the given value -- do not add this device to the alert
				String lastValue = Reformat.getUnescapeXml( e.getAttribute( VALUE_ATTRIBUTE ) );
				
				// If this is a disk usage alert, we don't need to compare values
				// instead, we need to make sure that the last value was null for
				// the device to be a part of the new section
				if(alert.getClassName().equals(AlertDefinitionType.DISK_USAGE_ALERT) || alert.getClassName().equals(AlertDefinitionType.CAMPAIGN_ALERT)){
					if(lastValue != null && lastValue.length() > 0){
						result = false;
					}
				}
				else if( Device.propertyHasChanged( lastValue, value, false ) == false ){
					result = false;
				}
			}
		} catch (ParseException e) {
			logger.error( e );
		}catch (java.text.ParseException e) {
			logger.error( e );
		}	

		return result;
	}
	
	/**
	 * Caches the lastResults clob for the alert into a string so it can be re-used
	 * 
	 * @param alert
	 * @return
	 */
	private String cachedLastResults = null;
	protected String getLastResults(Alert alert)
	{
		String result = null;

		Object syncObj = alert.getLastResults() == null ? new Integer(0) : alert.getLastResults();
		synchronized( syncObj )
		{
			if( cachedLastResults == null )
			{
				cachedLastResults = Reformat.convertClobToString( alert.getLastResults(), true );			
			}
		}
		
		return cachedLastResults;
	}
	
	/**
	 * Determines if any rows have been removed from the last time this alert was run,
	 * and returns the data of the removed rows in an ArrayList.
	 * @param alert
	 * @param currentResults
	 * @param keyIndex 
	 * @return
	 */
	protected ArrayList<ArrayList<String>> getRemovedRows(Alert alert, HashMap<Long, Object[]> currentResults, int keyIndex)
	{	
		ArrayList<ArrayList<String>> removedData = new ArrayList<ArrayList<String>>();

		// If there are any previously saved results for this alert
		String lastResultsXml = this.getLastResults(alert);
		if( lastResultsXml == null || lastResultsXml.length() == 0 ) return removedData;
			
		try 
		{
			// Attempt to locate an element associated with the given id
			Document doc = new Document( lastResultsXml );
			Elements es = doc.getElements( new XPath("//"+ LAST_RESULT) );
			while( es.hasMoreElements() )
			{
				Element e = es.next();
				Long lastId = Long.valueOf( e.getAttribute( ID_ATTRIBUTE ) );
				Device d = null;
				if( keyIndex >=0 )
				{
					// If this is an mcm alert, the lastId is an mcm id and not a device id
					if(alert.getClassName().getAlertDefinitionInfo().getIsMcmAlert()){
						Mcm mcm = Mcm.getMcm(lastId);
						if(mcm != null){
							d = mcm.getDevice();
						}
					}else{
						d = Device.getDevice( lastId );
					}
				}

				// Put the saved data for this id into an ArrayList
				ArrayList<String> rowData = new ArrayList<String>();
				String lastRow = e.getAttribute( LAST_ROW_ATTRIBUTE );
				if( lastRow != null )
				{
					// If this row contained a delimited list of values -- parse and add each one
					if( lastRow.indexOf( Constants.UNIQUE_DELIMITER ) >=0 ){
						String[] lastRowParts = lastRow.split("\\"+ Constants.UNIQUE_DELIMITER );
						for( int i=0; i<lastRowParts.length; i++ ){
							rowData.add( lastRowParts[i] );
						}
					}
					// Otherwise, add the entire row
					else{
						rowData.add( lastRow );
					}
				}							

				if (alert.getClassName().equals(AlertDefinitionType.HEARTBEAT_ALERT)) {
					if( currentResults.containsKey( lastId ) == false && (d == null || (d != null && this.addDeviceHeartbeat(d, alert) == true) ) )
					{
						// If this id is not in the collection of current results and there is either no associated device
						// OR the device is not in the alert's window, then remove the row
						removedData.add( rowData );
					}
					else if( d != null && this.addDeviceHeartbeat(d,alert) == false )
					{
						// if the id is associated with a device, but the device is outside its window
						currentResults.put( lastId, new Object[]{(String)rowData.get(keyIndex), rowData} );  // this is only called for HeartbeatAlert right now, where rowData[2] is the last heartbeat
					}
				}
				else {
					if( currentResults.containsKey( lastId ) == false)
					{
						// If this id is not in the collection of current results and there is either no associated device
						// OR the device is not in the alert's window, then remove the row
						removedData.add( rowData );
					}
				}
			}
		} catch (ParseException e) {
			logger.error( e );
		}	
		
		return removedData;
	}	
	
	/**
	 * Sends an email to each email address in the comma-delimited string
	 * @param emailAddress
	 */
	public void sendAlert(String subject, String msgBody, String recipients)
	{
		try {
			logger.info("Sending alert to: "+ recipients);
			String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
			String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");			
			Emailer.sendMessage(subject, msgBody, recipients, fromAddress, mailServer, false);
		} catch (KmfException e) {
			logger.error("Could not locate property. Unable to send email.", e);
		}
	}
	
	public void sendCustomAlert(String subject, String msgBody, String fromAddress, String fromName, String recipients)
	{
		try {
			logger.info("Sending alert to: "+ recipients);
			String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");			
			Emailer.sendMessage(subject, msgBody, recipients, fromAddress, fromName, mailServer, false);
		} catch (KmfException e) {
			logger.error("Could not locate property. Unable to send email.", e);
		}
	}
	
	public Date getNextRunTime(){
		Alert alert = Alert.getAlert( this.alertId );
		if(alert.getLastRunDt() == null){
			return new Date();
		}else{
			Calendar c = Calendar.getInstance();
			c.setTime(alert.getLastRunDt());
			
			int millis = 1000 * 60 * this.frequency;
			
			c.add(Calendar.MILLISECOND, millis);
			while(c.getTimeInMillis() < System.currentTimeMillis()){
				c.add(Calendar.MILLISECOND, millis);
			}
			
			return c.getTime();
		}
	}
}
