package com.kuvata.kmf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import parkmedia.DispatcherConstants;
import parkmedia.KMFLogger;
import parkmedia.usertype.StatusType;

import com.kuvata.kmf.multicast.MulticastTest;
import com.kuvata.kmf.util.Reformat;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class HeartbeatEvent extends DeviceEvent {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( HeartbeatEvent.class );
	private Long heartbeatEventId;
	private Date dt;	
	private Long deviceId;
	private String downloadsInProgress;
	private String uploadsInProgress;
	private String contentSchedulesInStaging;
	private String lastContentSchedule;
	private Boolean isLastHeartbeat;
	private String deviceResourceUtilization;
	private String multicastTest;
	private Long bytesToDownload;
	private Date lastOpenVpnFailureDt;
	private Date lastServiceMode;
	private Long bandwidthUtilization;

	private static HashMap<Long, HeartbeatEvent> heartbeatEventCache = new HashMap();
	public static HashMap<Long, Long> heartbeatedDevices = new HashMap();
	
	/**
	 * 
	 * @throws Exception
	 */
	public HeartbeatEvent() throws Exception
	{		
	}
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getHeartbeatEventId();
	}
	
	/**
	 * 
	 */
	public static void logEvent(Long deviceId, Date dt, String status) throws Exception
	{
		Session session = HibernateSession.currentSession();
		
		// Create a new HeartbeatEvent object
		HeartbeatEvent he = new HeartbeatEvent();
		he.setDeviceId( deviceId );
		he.setDt( dt );
		
		// Get the device object
		Device d = Device.getDevice( he.getDeviceId() );
		
		// If any status was passed in
		if( status != null )
		{
			try
			{
				Document doc = new Document( status );
				
				// Set the last activated content schedule, if any
				Element eLastContentSchedule = doc.getElement(new XPath("//"+ DispatcherConstants.LAST_CONTENT_SCHEDULE ));
				if( eLastContentSchedule != null && eLastContentSchedule.getTextString() != null && eLastContentSchedule.getTextString().length() > 0 ) {
					he.setLastContentSchedule( eLastContentSchedule.getTextString() );
				}
				
				// Build the string of downloads currently in progress
				Elements eCurrentDownloads = doc.getElements(new XPath("//"+ DispatcherConstants.CURRENT_DOWNLOAD ));
				if( eCurrentDownloads != null )
				{
					String downloadsInProgress = "";
					while( eCurrentDownloads.hasMoreElements() )
					{
						if( downloadsInProgress.length() > 0 )
						{
							downloadsInProgress += ", ";
						}
						downloadsInProgress += eCurrentDownloads.next().getTextString();
					}
					if( downloadsInProgress.length() > 4000 )
					{
						downloadsInProgress = downloadsInProgress.substring(0,4000);
					}
					he.setDownloadsInProgress( downloadsInProgress );
				}
				
				// Build the string of uploads currently in progress
				Elements eCurrentUploads = doc.getElements(new XPath("//"+ DispatcherConstants.CURRENT_UPLOAD ));
				if( eCurrentUploads != null )
				{
					String uploadsInProgress = "";
					while( eCurrentUploads.hasMoreElements() )
					{
						if( uploadsInProgress.length() > 0 )
						{
							uploadsInProgress += ", ";
						}
						uploadsInProgress += eCurrentUploads.next().getTextString();
						if( uploadsInProgress.length() > 4000 )
						{
							uploadsInProgress = uploadsInProgress.substring(0,4000);
						}
					}			
					he.setUploadsInProgress( uploadsInProgress );
				}		
				
				// Build the string of schedules in the staging directory
				Elements eStagedSchedules = doc.getElements(new XPath("//"+ DispatcherConstants.CONTENT_SCHEDULE_IN_STAGING ));
				if( eStagedSchedules != null )
				{
					String stagedSchedules = "";
					while( eStagedSchedules.hasMoreElements() )
					{
						if( stagedSchedules.length() > 0 )
						{
							stagedSchedules += ", ";
						}
						stagedSchedules += eStagedSchedules.next().getTextString();
						if( stagedSchedules.length() > 4000 )
						{
							stagedSchedules = stagedSchedules.substring(0,4000);
						}
					}			
					he.setContentSchedulesInStaging( stagedSchedules );
				}		
				
				// Get multicast test results
				Element eMulticastTest = doc.getElement(new XPath("//"+ DispatcherConstants.MULTICAST_TEST ));
				if( eMulticastTest != null && d.getMulticastNetwork() != null )
				{
					if( eMulticastTest != null && eMulticastTest.getTextString() != null && eMulticastTest.getTextString().length() > 0 )
					{
						try{
							String serverMulticastTime = MulticastTest.getLastMulticastTestTime();
							if(serverMulticastTime.length() > 0){
								long deviceTime = Constants.MULTICAST_DATE_TIME_FORMAT.parse(eMulticastTest.getTextString()).getTime();
								long serverTime = Constants.MULTICAST_DATE_TIME_FORMAT.parse(serverMulticastTime).getTime();
								
								/*
								 * Make sure that the device has missed at least two multicast tests before we report a failure
								 */ 
								if((serverTime - deviceTime) < 4 * MulticastTest.MULTICAST_FREQUENCY + MulticastTest.MULTICAST_TOLERANCE){
									he.setMulticastTest("Pass");
								}else{
									SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
									he.setMulticastTest("Failed. Timestamp on Server: " + df.format(new Date(serverTime)) + ", Device: " + df.format(new Date(deviceTime)));
								}
							}else{
								he.setMulticastTest("Enabled");
							}
						}catch(Exception e){
							logger.error(e);
						}
					}
				}
				
				// Update the vpn_ip_addr property of the device object
				Element eVpnIpAddr = doc.getElement(new XPath("//"+ DispatcherConstants.VPN_IP_ADDR_ELEMENT ));
				String vpnIpAddr = eVpnIpAddr != null && eVpnIpAddr.getTextString() != null ? eVpnIpAddr.getTextString() : null;
				if( (vpnIpAddr == null && d.getVpnIpAddr() == null) == false ){
					if( (vpnIpAddr == null && d.getVpnIpAddr() != null) || (vpnIpAddr != null && d.getVpnIpAddr() == null) || vpnIpAddr.equals( d.getVpnIpAddr() ) == false )
					{
						// First, clear the vpnIpAddr property for any other devices that have this same vpnIpAddr
						d.clearVpnIpAddresses( vpnIpAddr );
						d.setVpnIpAddr( vpnIpAddr );
						HibernateSession.beginTransaction();
						d.update();
						HibernateSession.commitTransaction();
					}
				}
				
				// Update the volume property of the device object
				Element eDeviceResourceUtilization = doc.getElement(new XPath("//"+ DispatcherConstants.DEVICE_RESOURCE_UTILIZATION_ELEMENT ));
				if( eDeviceResourceUtilization != null )
				{
					String s = eDeviceResourceUtilization.getTextString();
					if( s == null ) s = "";
					if( s != null && s.length() > 1000 )
					{
						s = s.substring(0,1000);
					}
					he.setDeviceResourceUtilization( s );
				}				
				
				// Retrieve any deviceCommands and update their completed flag
				Elements eDeviceCommands = doc.getElements(new XPath("//"+ DispatcherConstants.DEVICE_COMMANDS_ELEMENT +"/"+ DispatcherConstants.DEVICE_COMMAND_ELEMENT ));
				if( eDeviceCommands != null )
				{					
					while( eDeviceCommands.hasMoreElements() )
					{				
						Element eDeviceCommand = eDeviceCommands.next();						
						String deviceCommandId = eDeviceCommand.getAttribute( DispatcherConstants.DEVICE_COMMAND_ID_ATTRIBUTE );
						String deviceCommandStatus = eDeviceCommand.getAttribute( DispatcherConstants.DEVICE_COMMAND_STATUS_ATTRIBUTE );						
						if( deviceCommandId != null && deviceCommandId.length() > 0 )
						{							
							// Update the completed field for this device command
							DeviceCommand dc = DeviceCommand.getDeviceCommand( new Long( deviceCommandId ) );
							if( dc != null )
							{
								StatusType dcStatus = StatusType.getStatusType( deviceCommandStatus );
								dc.setStatus( dcStatus );
								logger.info("Setting device command status for: "+ dc.getDeviceCommandId() +" to: "+ dcStatus.getStatusTypeName() );
								dc.setLastModifiedDt( new Date() );
								HibernateSession.beginTransaction();
								dc.update();
								HibernateSession.commitTransaction();
							}
						}						
					}			
				}
				
				// Retrieve open-vpn server status
				Element eOpenVpnServerStatus = doc.getElement(new XPath("//"+ DispatcherConstants.OPENVPN_SERVER_STATUS ));
				if( eOpenVpnServerStatus != null ){
					// If the open vpn server tunnel is down and this is an edge server
					if(eOpenVpnServerStatus.getTextString().equals(Constants.DOWN) && d.getEdgeDeviceIds().size() > 0){
						// We will disconnect the open vpn client which will in turn force the device to re-connect.
						// Upon re-connect, via openvpn-auth a new device command will be sent down to the device
						// forcing it to start its server tunnel. We don't want to disconnect the client in two
						// consecutive heartbeats to allow device command communication. We will also disconnect
						// only if we have received constant failures for over 5 mins.
						HeartbeatEvent lastHeartbeat = getFromHeartbeatEventCache( he.getDeviceId() );
						
						// First failure
						if(lastHeartbeat == null || lastHeartbeat.lastOpenVpnFailureDt == null){
							he.lastOpenVpnFailureDt = new Date();
						}
						// Been over 5 mins and still failing
						else if(System.currentTimeMillis() - lastHeartbeat.lastOpenVpnFailureDt.getTime() > 300000){
							logger.info("Disconnecting openvpn client since the openvpn server seems to be down on this edge server: " + d.getDeviceName());
							d.disconnectOpenVpnClient();
						}
						// Still failing but has not been 5 mins yet
						else{
							he.lastOpenVpnFailureDt = lastHeartbeat.lastOpenVpnFailureDt;
						}
					}
				}
				
				// Retrieve device time
				Element eDeviceTime = doc.getElement(new XPath("//"+ DispatcherConstants.DEVICE_TIME ));
				if( eDeviceTime != null && eDeviceTime.getTextString().length() > 0)
				{
					SimpleDateFormat sdf = new SimpleDateFormat(Constants.HEARTBEAT_FORMAT);
					Date date = sdf.parse(eDeviceTime.getTextString());
					Date now = new Date();
					
					// If the device is off by more than a minute
					long diff = (Math.max(now.getTime(), date.getTime()) - Math.min(now.getTime(), date.getTime())) / 1000;
					if(diff > 60){
						logger.info("Device " + d.getDeviceName() + " is off by " + diff +  " seconds.");
					}
				}
				
				// Retrieve service mode
				Element eServiceMode = doc.getElement(new XPath("//"+ DispatcherConstants.LAST_SERVICE_MODE_ELEMENT ));
				if( eServiceMode != null && eServiceMode.getTextString() != null && eServiceMode.getTextString().length() > 0)
				{
					SimpleDateFormat sdf = new SimpleDateFormat(Constants.HEARTBEAT_FORMAT);
					Date date = sdf.parse(eServiceMode.getTextString());
					he.setLastServiceMode(date);
				}
				
				// Retrieve bandwidth utilization
				Element eBandwidthUtilization = doc.getElement(new XPath("//"+ DispatcherConstants.BANDWIDTH_UTILIZATION_ELEMENT ));
				if( eBandwidthUtilization != null && eBandwidthUtilization.getTextString() != null && eBandwidthUtilization.getTextString().length() > 0)
				{
					he.setBandwidthUtilization(Long.parseLong(eBandwidthUtilization.getTextString()));
				}
				
				// If we found a device with given deviceId -- set bytesToDownload
				if( d != null ){
					he.setBytesToDownload( d.getBytesToDownload() );
				}
			}catch(ParseException e){
				logger.error(e);
			}
		}
		
		HeartbeatEvent lastHeartbeat = getFromHeartbeatEventCache( he.getDeviceId() );
		Long deviceIdInCache = lastHeartbeat != null ? lastHeartbeat.getDeviceId() : deviceId;

		// This block is synchronized on the device id to prevent two threads
		// from the same device to get into it at the same time
		synchronized(deviceIdInCache){
			
			// Re-get due to possible synchronization wait time
			lastHeartbeat = getFromHeartbeatEventCache( he.getDeviceId() );
			
			// Clear the isLastHeartbeat flag for any other heartbeat events that have isLastHeartbeat set to true for this device
			if( lastHeartbeat != null )
			{
				// Make sure that we are inserting a newer heartbeat event
				if( lastHeartbeat != null && he.getDt().after(lastHeartbeat.getDt())){
					HibernateSession.beginTransaction();
					session.lock(lastHeartbeat, LockMode.NONE);
					lastHeartbeat.setIsLastHeartbeat( false );
					session.update( lastHeartbeat );
					HibernateSession.commitTransaction();
				}
			}
			
			try {
				he.setIsLastHeartbeat( lastHeartbeat == null || he.getDt().after(lastHeartbeat.getDt()) );
				HeartbeatEvent.setInHeartbeatEventCache(deviceId, he);
				HibernateSession.beginTransaction();
				session.save( he );
				HibernateSession.commitTransaction(true);
			} catch (ConstraintViolationException e) {
				logger.error("Caught a ConstraintViolationException in logHeartbeatEvent");
				
				/*
				 * Update the is_last_heartbeat column for any rows associated with this device
				 * that currently have their is_last_heartbeat set to true 
				 */				
				String hql = "UPDATE HeartbeatEvent "
					+ "SET is_last_heartbeat = 0 "						
					+ "WHERE device_id = :deviceId "
					+ "AND is_last_heartbeat = 1";
				HibernateSession.beginTransaction();
				session.createQuery( hql )
					.setParameter("deviceId", he.getDeviceId())
					.executeUpdate();	
				HibernateSession.commitTransaction();
				
				// Try saving it now
				HibernateSession.beginTransaction();
				session.save( he );
				HibernateSession.commitTransaction();
			}
		}
	}
	
	/**
	 * 
	 */
	public static HeartbeatEvent getHeartbeatEvent(Long heartbeatEventId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT he "
			+ "FROM HeartbeatEvent as he "
			+ "WHERE he.heartbeatEventId = :heartbeatEventId";
		Query q = session.createQuery( hql ).setParameter("heartbeatEventId", heartbeatEventId );
		return (HeartbeatEvent)q.uniqueResult();
	}	
	
	/**
	 * 
	 */
	public static List getHeartbeatEvents(Device device, Date startDatetime, Date endDatetime) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT he "
			+ "FROM HeartbeatEvent as he "
			+ "WHERE he.deviceId = :deviceId "
			+ "AND he.dt BETWEEN :startDatetime AND :endDatetime "
			+ "ORDER BY he.dt";		
		Query q = session.createQuery( hql )
			.setParameter("deviceId", device.getDeviceId() )
			.setParameter("startDatetime", startDatetime )
			.setParameter("endDatetime", endDatetime ); 		
		return q.list();
	}	
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof HeartbeatEvent) ) result = false;
		
		HeartbeatEvent de = (HeartbeatEvent) other;		
		if(this.hashCode() == de.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "HeartbeatEvent".hashCode();
		result = Reformat.getSafeHash( this.getDt(), result, 13 );
		result = Reformat.getSafeHash( this.getDeviceId(), result, 17 );		
		return result;
	}		
	
	/**
	 * @return Returns the heartbeatEventId.
	 */
	public Long getHeartbeatEventId() {
		return heartbeatEventId;
	}

	/**
	 * @param heartbeatEventId The heartbeatEventId to set.
	 */
	public void setHeartbeatEventId(Long heartbeatEventId) {
		this.heartbeatEventId = heartbeatEventId;
	}
	
	/**
	 * @return Returns the contentSchedulesInStaging.
	 */
	public String getContentSchedulesInStaging() {
		return contentSchedulesInStaging;
	}
	/**
	 * @param contentSchedulesInStaging The contentSchedulesInStaging to set.
	 */
	public void setContentSchedulesInStaging(String contentSchedulesInStaging) {
		this.contentSchedulesInStaging = contentSchedulesInStaging;
	}
	/**
	 * @return Returns the downloadsInProgress.
	 */
	public String getDownloadsInProgress() {
		return downloadsInProgress;
	}
	/**
	 * @param downloadsInProgress The downloadsInProgress to set.
	 */
	public void setDownloadsInProgress(String downloadsInProgress) {
		this.downloadsInProgress = downloadsInProgress;
	}
	/**
	 * @return Returns the lastContentSchedule.
	 */
	public String getLastContentSchedule() {
		return lastContentSchedule;
	}
	/**
	 * @param lastContentSchedule The lastContentSchedule to set.
	 */
	public void setLastContentSchedule(String lastContentSchedule) {
		this.lastContentSchedule = lastContentSchedule;
	}
	/**
	 * @return Returns the uploadsInProgress.
	 */
	public String getUploadsInProgress() {
		return uploadsInProgress;
	}
	/**
	 * @param uploadsInProgress The uploadsInProgress to set.
	 */
	public void setUploadsInProgress(String uploadsInProgress) {
		this.uploadsInProgress = uploadsInProgress;
	}
	/**
	 * @return Returns the isLastHeartbeat.
	 */
	public Boolean getIsLastHeartbeat() {
		return isLastHeartbeat;
	}
	
	/**
	 * @param isLastHeartbeat The isLastHeartbeat to set.
	 */
	public void setIsLastHeartbeat(Boolean isLastHeartbeat) {
		this.isLastHeartbeat = isLastHeartbeat;
	}
	
	/**
	 * @return Returns the dt.
	 */
	public Date getDt() {
		return dt;
	}
	
	/**
	 * @param dt The dt to set.
	 */
	public void setDt(Date dt) {
		this.dt = dt;
	}
	/**
	 * @return Returns the deviceId.
	 */
	public Long getDeviceId() {
		return deviceId;
	}
	
	/**
	 * @param deviceId The deviceId to set.
	 */
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	/**
	 * @return Returns the deviceResourceUtilization.
	 */
	public String getDeviceResourceUtilization() {
		return deviceResourceUtilization;
	}
	
	/**
	 * @param deviceResourceUtilization The deviceResourceUtilization to set.
	 */
	public void setDeviceResourceUtilization(String deviceResourceUtilization) {
		this.deviceResourceUtilization = deviceResourceUtilization;
	}
	
	public static boolean containedInHeartbeatEventCache(Long deviceId)
	{
		boolean result;
		synchronized(heartbeatEventCache){
			result = heartbeatEventCache.containsKey(deviceId);
		}
		return result;
		
	}
	
	public static HeartbeatEvent getFromHeartbeatEventCache(Long deviceId)
	{
		HeartbeatEvent result;
		synchronized(heartbeatEventCache){
			result = (HeartbeatEvent) heartbeatEventCache.get( deviceId );
		}
		return result;
	}

	public static void setInHeartbeatEventCache(Long deviceId, HeartbeatEvent he)
	{
		synchronized(heartbeatEventCache){
			heartbeatEventCache.put( deviceId, he );
		}
	}
	
	public String getMulticastTest() {
		return multicastTest;
	}
	
	public void setMulticastTest(String multicastTest) {
		this.multicastTest = multicastTest;
	}
	/**
	 * @return the bytesToDownload
	 */
	public Long getBytesToDownload() {
		return bytesToDownload;
	}
	/**
	 * @param bytesToDownload the bytesToDownload to set
	 */
	public void setBytesToDownload(Long bytesToDownload) {
		this.bytesToDownload = bytesToDownload;
	}
	public Date getLastServiceMode() {
		return lastServiceMode;
	}
	public void setLastServiceMode(Date lastServiceMode) {
		this.lastServiceMode = lastServiceMode;
	}
	public Long getBandwidthUtilization() {
		return bandwidthUtilization;
	}
	public void setBandwidthUtilization(Long bandwidthUtilization) {
		this.bandwidthUtilization = bandwidthUtilization;
	}
}
