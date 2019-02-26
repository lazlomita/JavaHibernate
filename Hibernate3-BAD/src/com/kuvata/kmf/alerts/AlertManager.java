package com.kuvata.kmf.alerts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.ContentUpdateType;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Schema;
import com.kuvata.kmf.SchemaDirectory;

public class AlertManager {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( AlertManager.class );
	private static HashMap runningAlerts = new HashMap();
	
	/**
	 * Determines which alerts need to be started and stopped.
	 *
	 */
	public static void manageAlerts()
	{
		try
		{
			// If the schemas have not already been loaded			
			if( SchemaDirectory.schemas == null )
			{
				// Load them
				logger.info("Loading schemas");
				SchemaDirectory.setup(Constants.BASE_SCHEMA, "AlertManager");
				Session session = HibernateSession.currentSession();	
				HibernateSession.closeSession();
			}
	
			// For each schema that has been loaded into the schemas hashmap			
			for (Iterator i = SchemaDirectory.schemas.keySet().iterator(); i.hasNext(); )
			{
				// Skip the BASE_SCHEMA
				String schemaName = String.valueOf( i.next() );	
				if( schemaName.equals( Constants.BASE_SCHEMA ) == false )
				{
					HibernateSession.closeSession();
					logger.info("Managing alerts for "+ schemaName +" schema.");
					SchemaDirectory.setup(schemaName, "AlertManager");
					doManageAlerts();
					HibernateSession.closeSession();
					logger.info("Finished managing alerts for "+ schemaName +".");
				}			
			}					
		}
		catch(Exception e)
		{
			logger.error("Unexpected error occurred in manageAlerts.", e);
		}		
		finally
		{
			try{
				HibernateSession.closeSession();
			}catch( HibernateException e)
			{ e.printStackTrace(); }
		}		
	}
	
	private static void doManageAlerts()
	{
		TimerTask systemAlerts = new TimerTask(){
			public void run(){
				
				SchemaDirectory.initialize("kuvata", AlertManager.class.getName(), null, false, false);
				
				// Check playback event aggregation status and alert if required
				Schema schema = Schema.getSchema(SchemaDirectory.getSchemaName());
				if(schema.getLastAggregatedDate() != null){
					
					// Check the last aggregation date
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DAY_OF_YEAR, -2);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					
					if(schema.getLastAggregatedDate().before(c.getTime())){
						
						boolean sendAlert = true;
						
						// Check to see if the process is still running
						try {
							String[] cmd = {"/bin/sh", "-c", "ps -ef | grep java | grep AggregatePlaybackEvents | grep -v grep | wc -l"};
							Process proc = Runtime.getRuntime().exec(cmd);
							BufferedReader se = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
							String buf;
							
							while( (buf = se.readLine()) != null){
								if(Integer.parseInt(buf) > 0){
									
									// Only alert if the aggregation has failed on a previous day
									c = Calendar.getInstance();
									c.add(Calendar.DAY_OF_YEAR, -3);
									c.set(Calendar.HOUR_OF_DAY, 0);
									c.set(Calendar.MINUTE, 0);
									c.set(Calendar.SECOND, 0);
									c.set(Calendar.MILLISECOND, 0);
									
									if(schema.getLastAggregatedDate().after(c.getTime()) == false){ // Implies before or equal
										sendAlert = true;
									}else{
										sendAlert = false;
									}
									
									break;
								}
							}
							
							se.close();
							proc.waitFor();
						} catch (Exception e) {
							logger.error(e);
						}
						
						if(sendAlert){
							// Send an alert
							String msgBody = "Playback Event Aggregation failure on ";
							try {
						        InetAddress addr = InetAddress.getLocalHost();
						        msgBody += addr.getHostName() + " (" + addr.getHostAddress() + ")";
						    } catch (UnknownHostException e) {
						    	logger.error(e);
						    }

							try {
								logger.info("Sending Playback Event Aggregation Alert");
								String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
								String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");
								Emailer.sendMessage("Playback Event Aggregation Failure", msgBody, "root@localhost", fromAddress, mailServer, false);
							} catch (KmfException e) {
								logger.error("Could not locate property. Unable to send email.", e);
							}	
						}
					}
				}
				
				// Check last server cleanup date and alert if needed
				if(schema.getLastServerCleanupDt() != null){
					
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DAY_OF_YEAR, -1);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					
					if(schema.getLastServerCleanupDt().before(c.getTime())){
						
						boolean sendAlert = true;
						
						// Check to see if the process is still running
						try {
							String[] cmd = {"/bin/sh", "-c", "ps -ef | grep java | grep ServerCleanup | grep -v grep | wc -l"};
							Process proc = Runtime.getRuntime().exec(cmd);
							BufferedReader se = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
							String buf;
							
							while( (buf = se.readLine()) != null){
								if(Integer.parseInt(buf) > 0){
									
									// Only alert if the server cleanup has failed on a previous day
									c = Calendar.getInstance();
									c.add(Calendar.DAY_OF_YEAR, -2);
									c.set(Calendar.HOUR_OF_DAY, 0);
									c.set(Calendar.MINUTE, 0);
									c.set(Calendar.SECOND, 0);
									c.set(Calendar.MILLISECOND, 0);
									
									if(schema.getLastServerCleanupDt().after(c.getTime()) == false){ // Implies before or equal
										sendAlert = true;
									}else{
										sendAlert = false;
									}
									
									break;
								}
							}
							
							se.close();
							proc.waitFor();
						} catch (Exception e) {
							logger.error(e);
						}
						
						if(sendAlert){
							// Send an alert
							String msgBody = "Server cleanup failure detected on ";
							try {
						        InetAddress addr = InetAddress.getLocalHost();
						        msgBody += addr.getHostName() + " (" + addr.getHostAddress() + ")";
						    } catch (UnknownHostException e) {
						    	logger.error(e);
						    }

							try {
								logger.info("Sending Server Cleanup Alert");
								String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
								String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");
								Emailer.sendMessage("Server Cleanup Failure", msgBody, "root@localhost", fromAddress, mailServer, false);
							} catch (KmfException e) {
								logger.error("Could not locate property. Unable to send email.", e);
							}
						}
					}
				}
				
				// Close the session so that the next TimerTask running via this Timer has a fresh session
				HibernateSession.closeSession();
				
			}
		};
		
		TimerTask csAlert = new TimerTask(){
			public void run(){
				
				SchemaDirectory.initialize("kuvata", AlertManager.class.getName(), null, false, false);
				
				// Calculate the threshold date to compare the lastHeartbeat dates 
				int threshold = 15;
				Calendar c = Calendar.getInstance();
				c.add( Calendar.MINUTE, -(threshold) );
				
				String hql = "SELECT device FROM HeartbeatEvent heartbeatEvent, Device device "
								+ "WHERE heartbeatEvent.isLastHeartbeat = 1 AND heartbeatEvent.deviceId = device.deviceId AND heartbeatEvent.dt >= :startDatetime AND device.mirrorSource IS NULL "
								+ "AND device.contentUpdateType = :contentUpdateType ORDER BY UPPER(device.deviceName)";
				
				Query q = HibernateSession.currentSession().createQuery(hql);
				q.setParameter("startDatetime", c.getTime() );
				q.setParameter("contentUpdateType", ContentUpdateType.NETWORK.getPersistentValue() );
				
				// Get actively heartbeating devices with a network delivery mode
				List<Device> devices = q.list();
				
				List<Device> problemDevices = new LinkedList<Device>();
				for(Device d : devices){
					// Make sure the device has a valid license status
					if(d.hasValidLicense()){
						// If this device doesn't have any new content schedule for the current time
						if(System.currentTimeMillis() >= d.getLastScheduledContent().getTime() - (d.getTimezoneAdjustment() * 1000l) ){
							// Make sure this device isn't dirty
							if(d.isDirty() == false){
								problemDevices.add(d);
							}
						}
					}
				}
				
				if(problemDevices.size() > 0){
					SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
					StringBuilder sb = new StringBuilder();
					sb.append("<html><head><style type=\"text/css\">" +
							".txtTopLabel {font-weight: bold;text-align: left;border-top: 1px solid #000000;border-right: 1px solid #000000;border-bottom: 1px solid #000000;border-left: 1px solid #000000;" +
							"border-left: 1px solid #000000;color: #FFFFFF;background-color: #5A7A36;font-family:\"Verdana\"}" +
							".datagrid{background-color:#FFFFFF;}" +
							".dgHeader {text-align: center;border-top:1px solid #000000;border-left:1px solid #000000;border-right:1px solid #000000;border-bottom:1px solid #000000;" +
							"font-family:\"Verdana\";color: #FFFFFF;background-color: #666666;background-repeat:repeat-x;background-position:bottom;}" +
							".txtHeader {font-weight: bold;font-family:\"Verdana\";}" +
							".dgItem0{font-size:12px;background-color:#F0F0F0;font-family:\"Verdana\";}" +
							".dgItem1{font-size:12px;font-family:\"Verdana\";}" +
							"</style></head>" +
							"<body>" +
								"<table width=\"100%\">" +
									"<tr valign=\"top\"><td height=\"10px\">" +
										"<table border=\"0\" width=\"100%\" cellpadding=\"5\" cellspacing=\"0\">" +
											"<tr><td width=\"100%\" class=\"txtTopLabel\">Content Scheduler Issue on ");
					
					try {
				        InetAddress addr = InetAddress.getLocalHost();
				        sb.append(addr.getHostName() + " (" + addr.getHostAddress() + ")");
				    } catch (UnknownHostException e) {
				    	logger.error(e);
				    }
											
								sb.append("</td></tr>" +
										"</table>" +
									"</td></tr>" +
									"<tr><td class=\"txtHeader\"><br/>" + sdf.format(new Date()) + "</td></tr>" +
									"<tr><td class=\"txtHeader\"><font style=\"font-style:italic;\">Content Scheduler Didn't Run Report</font></td></tr>" +
									"<tr><td><br/>" +
										"<table border=\"0\" class=\"datagrid\">" +
											"<tr><td class=\"dgHeader\">Device Name</td><td class=\"dgHeader\">Last Scheduled Content Date</td></tr>");
					
					int row = 0;
					for(Device d : problemDevices){
						String rowClass = row % 2 == 0 ? "dgItem0" : "dgItem1";
						sb.append("<tr class=\"" + rowClass + "\"><td>" + d.getDeviceName() + "</td><td style=\"text-align:center;\">" + sdf.format(d.getLastScheduledContent()) + "</td></tr>");
						row++;
					}
					
							sb.append("</table></td></tr>");
					sb.append("</table></body></html>");
					
					// Send the alert
					try {
						
						// Default to /root/.forward
						String emailAddresses = "root";
						try {
							emailAddresses = KuvataConfig.getPropertyValue("Application.backendAlertEmails");
						} catch (Exception e) {
							// Ignore
						}
						
						logger.info("Sending alert to: "+ emailAddresses);
						String subject = "Content Scheduler Didn't Run Report";
						String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
						String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");			
						Emailer.sendMessage(subject, sb.toString(), emailAddresses, fromAddress, mailServer, true);
					} catch (KmfException e) {
						logger.error("Could not locate property. Unable to send email.", e);
					}
				}
				
				// Close the session so that the next TimerTask running via this Timer has a fresh session
				HibernateSession.closeSession();
			}
		};
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(systemAlerts, 0, Constants.MILLISECONDS_IN_A_DAY);
		t.scheduleAtFixedRate(csAlert, 0, 3600000);
		
		// Get all alerts from the database
		List alerts = Alert.getActiveAlerts();	
		logger.info("Found "+ alerts.size() +" alerts to manage.");
		for( Iterator i = alerts.iterator(); i.hasNext(); )
		{
			Alert alert = (Alert)i.next();
						
			// If this alert is not already running
			String methodToRun = alert.getClassName().getPersistentValue();
			AlertDefinition runningAlert = (AlertDefinition)runningAlerts.get( alert.getAlertId() );
			if( runningAlert == null ) 
			{
				// Start it by running the .schedule method of the given class
				Object alertObj = startAlert( alert );
				
				// Add it to the hashmap
				runningAlerts.put( alert.getAlertId(), alertObj );
			}
		}
			
		// For each alert that is currently running
		for( Iterator i = runningAlerts.entrySet().iterator(); i.hasNext(); )
		{
			// Attempt to locate this alert in our list of alerts from the database
			// to make sure it wasn't removed from the database
			Entry entry = (Entry)i.next();
			Long activeAlert = (Long)entry.getKey();
			boolean alertExists = false;
			for( Iterator j = alerts.iterator(); j.hasNext(); )
			{
				Alert alert = (Alert)j.next();
				if( alert.getAlertId().equals( activeAlert ) ) {
					alertExists = true;
					break;
				}
			}
			
			// If this alert was removed from the database, stop it
			if( ! alertExists )  {
				stopAlert( (AlertDefinition)entry.getValue() );
				i.remove();
			}
		}		
	}
	
	/**
	 * Calls the .schedule method of the class associated with the given alert
	 * @param alert
	 */
	private static Object startAlert(Alert alert)
	{		
		String className = alert.getClassName().getPersistentValue();
		String methodName = "schedule";	
		Object alertObj = null;
		try{
			// Convert the string of parameters into a String array
			String[] args = new String[]{ alert.getParameters() };
			if( alert.getParameters() != null && alert.getParameters().indexOf(",") > 0 ) {
				args = alert.getParameters().split("\\,");
			}			
			
			// Instantiate the given class
	        Class clsAlert = Class.forName( className );
	        Class partypes[] = new Class[6];
			partypes[0] = String.class;
			partypes[1] = Long.class;
			partypes[2] = String.class;
			partypes[3] = String.class;
			partypes[4] = String[].class;
			partypes[5] = Integer.class;
			Constructor ct = clsAlert.getConstructor( partypes );
			Object arglist[] = new Object[6];
	        arglist[0] = SchemaDirectory.getSchema().getSchemaName();
			arglist[1] = alert.getAlertId();
			arglist[2] = alert.getClassName().getName();
			arglist[3] = alert.getAlertName();
			arglist[4] = args;	        
			arglist[5] = alert.getFrequency() != null ? new Integer( alert.getFrequency() ) : 30;
	        alertObj = ct.newInstance( arglist );			
							
			// Invoke the "schedule" method on the given class
			Class c = Class.forName( className );				
			Method m = c.getDeclaredMethod( methodName );						
			m.invoke( alertObj );
		}
		catch(Exception e)
		{
			logger.error("Unexpected error occurred while starting alert: "+ className, e);
		}
		return alertObj;
	}
	
	/**
	 * Stops and starts the alert associated with the given alertId.
	 * Called from Alert.update when changes to an alerts parameters are made.
	 * @param schemaName
	 * @param alertId
	 */
	public static void resetAlert(String schemaName, Long alertId)
	{
		// If the schemas have not already been loaded			
		if( SchemaDirectory.schemas == null )
		{
			// Load them
			SchemaDirectory.setup(Constants.BASE_SCHEMA, "AlertManager");
			Session session = HibernateSession.currentSession();	
			HibernateSession.closeSession();
		}

		// Evict the alert object from the session so the values will be re-read each time through
		Alert alert = Alert.getAlert( alertId );
		if( alert != null ){					
			Session session = HibernateSession.currentSession();
			
			// Re-load the object from the db
			session.refresh( alert );
		}				
		if( alert != null )
		{
			// Locate this alertId in our list of runningAlerts and stop it
			for( Iterator i = runningAlerts.entrySet().iterator(); i.hasNext(); )
			{
				Entry entry = (Entry)i.next();
				Long activeAlertId = (Long)entry.getKey();
				if( activeAlertId.equals( alertId ) )
				{					
					stopAlert( (AlertDefinition)entry.getValue() );
					i.remove();
				}				
			}	
			
			// Make the call to doManageAlerts in order to restart this alert
			doManageAlerts();
		}
		else{
			logger.info("Could not locate an alert with the given alertId: "+ alertId +". Unable to reset alert.");
		}			
	}
	
	/**
	 * Make the call to the given alert definition in order to stop the 
	 * alert timer tasks.
	 * 
	 * @param alert
	 */
	private static void stopAlert(AlertDefinition alert)
	{
		// Stop the alert
		logger.info("Found an alert to stop: "+ alert.getClass().getName());
		alert.stop();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AlertManager();
	}

}
