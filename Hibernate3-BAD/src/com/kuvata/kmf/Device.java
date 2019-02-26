package com.kuvata.kmf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.exception.ConstraintViolationException;

import parkmedia.DispatcherConstants;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.device.configurator.DeviceProperty;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.AudioConnectionType;
import com.kuvata.kmf.usertype.BillingStatusType;
import com.kuvata.kmf.usertype.ContentSchedulerStatusType;
import com.kuvata.kmf.usertype.ContentUpdateType;
import com.kuvata.kmf.usertype.DeviceAutoUpdateType;
import com.kuvata.kmf.usertype.DeviceCommandType;
import com.kuvata.kmf.usertype.DevicePropertyType;
import com.kuvata.kmf.usertype.DeviceScalingModeType;
import com.kuvata.kmf.usertype.DeviceSearchType;
import com.kuvata.kmf.usertype.DirtyType;
import com.kuvata.kmf.usertype.DisplayRotationType;
import com.kuvata.kmf.usertype.FileTransmissionStatus;
import com.kuvata.kmf.usertype.FileTransmissionType;
import com.kuvata.kmf.usertype.FramesyncType;
import com.kuvata.kmf.usertype.McmCommandType;
import com.kuvata.kmf.usertype.McmImplementationType;
import com.kuvata.kmf.usertype.OutputModeType;
import com.kuvata.kmf.usertype.PeripheralType;
import com.kuvata.kmf.usertype.SearchInterfaceType;
import com.kuvata.kmf.usertype.SegmentType;
import com.kuvata.kmf.usertype.StatusType;

import com.kuvata.HttpClient;
import com.kuvata.configurator.McmProperty;
import com.kuvata.dispatcher.scheduling.ContentScheduler;
import com.kuvata.dispatcher.scheduling.ContentSchedulerArg;
import com.kuvata.dispatcher.scheduling.ContentSchedulerThread;
import com.kuvata.dispatcher.scheduling.ScheduleInfo;
import com.kuvata.dispatcher.scheduling.SegmentBlock;
import com.kuvata.dispatcher.services.DeviceCommandCreator;
import com.kuvata.kmf.asset.Html;
import com.kuvata.kmf.asset.Webapp;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.billing.VenuePartner;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.logging.HistoryEntry;
import com.kuvata.kmf.permissions.FilterManager;
import com.kuvata.kmf.permissions.FilterType;
import com.kuvata.kmf.presentation.HtmlPresentation;
import com.kuvata.kmf.presentation.Presentation;
import com.kuvata.kmf.presentation.WebappPresentation;
import com.kuvata.kmf.usertype.PersistentStringEnum;
import com.kuvata.kmf.util.Files;
import com.kuvata.kmf.util.Reformat;
import com.kuvata.kmf.util.TripleDES;
import com.kuvata.kmm.DeviceResultsPage;
import com.kuvata.kmm.EntityOverviewPage;
import com.kuvata.kmm.KMMServlet;
import com.kuvata.kmm.Page;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.XPath;
import electric.xml.XPathException;

/**
 *
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class Device extends Entity implements TreeViewable, Historizable
{
	private static Logger logger = Logger.getLogger(Device.class);
	private static final int DOWNLOAD_PRIORITY_DEFAULT = 0;
	private static final boolean DHCP_ENABLED_DEFAULT = false;
	private static final Float MEMORY_THRESHOLD_DEFAULT = 99F;
	private static final Float CPU_THRESHOLD_DEFAULT = 75F;
	private static final Float IOWAIT_THRESHOLD_DEFAULT = 25F;
	private static final Float LOAD_THRESHOLD_DEFAULT = 3.0F;
	private static final String VOLUME_DEFAULT = "90";
	private static final String LCD_PIN_DEFAULT = "0";
	private static final String LCD_BRANDING_DEFAULT = Constants.SHOW_CURRENT_PLAYING_ASSET;
	private static final String OPENVPN_MANAGEMENT_PORT_DEFAULT = "444";
	
	// Represents an "invalid" timezone -- returns GMT
	private static final TimeZone invalidTimezone = TimeZone.getTimeZone( "xxx" );
	
	// Collection to avoid multiple threads
	private static List edgeServerOpenVpnHostIpThreads = new ArrayList();
	
	// Current dispatcher's IP Address
	private static String dispatcherIpAddr = null;
	
	private Long deviceId;
	private String deviceName;
	private String macAddr;
	private String ipAddr;
	private String publicIpAddr;
	private Date lastScheduledContent;
	private Integer contentSchedulingHorizon;
	private Integer additionalContentAmount;
	private Integer contentOutOfDate;
	private String contentSchedulerStatus;
	private Device edgeServer;
	private Device mirrorSource;
	private String contentSchedulerMessages;
	private String volume;
	private String maxVolume;
	private Boolean applyAlerts;
	private String contentUpdateType;
	private String heartbeatInterval;
	private String diskCapacity;
	private String diskUsage;
	private DisplayRotationType rotation;
	private Boolean streamingServerCapable;
	private Boolean streamingClientCapable;
	private String timezone;
	private String display;
	private String resolution;
	private DeviceAutoUpdateType autoUpdate;
	private DeviceScalingModeType scalingMode;
	private String version;
	private Date lastFileExistsDt;
	private Float maxFileStorage;	
	private String networkInterface;
	private String gateway;
	private String netmask;
	private String dnsServer;
	private Boolean dhcpEnabled;
	private String lcdPin;
	private String lcdBranding;	
	private String screenshot;
	private Float iowaitThreshold;
	private Float cpuThreshold;
	private Float memoryThreshold;
	private Float loadThreshold;
	private OutputModeType outputMode;
	private Date screenshotUploadTime;
	private String vpnIpAddr;	
	private Boolean deleted;
	private Date filesyncStartTime;
	private Date filesyncEndTime;
	private String osVersion;
	private Long bytesToDownload;
	private String dispatcherServers;
	private String edgeServerOpenvpnHostIp;
	private Boolean redirectGateway;
	private Integer xOffset;
	private Integer yOffset;
	private Integer zoom;
	private String connectAddr;
	private String antivirusScan;
	private Integer downloadPriority;
	private String audioNormalization;
	private VenuePartner venuePartner;
	private Date createDate;
	private MulticastNetwork multicastNetwork;
	private String control;
	private String ssid;
	private String encryptionType;
	private String passphrase;
	private String encryptedBillingStatus;
	private String readableBillingStatus;
	private String encryptedBillableStartDt;
	private Date readableBillableStartDt;
	private String encryptedBillableEndDt;
	private Date readableBillableEndDt;
	private String alphaCompositing;
	private Integer bandwidthLimit;
	private AudioConnectionType audioConnection;
	private String activeDispatcher;
	private Boolean deviceSideScheduling;
	private Device replacedBy;
	private FramesyncType framesync;
	private Date lastModified;
	private Integer installReleaseHour;
	private Boolean type2VideoPlayback;
	private Boolean useChrome;
	private Boolean chromeDisableGpu;
	
	private Boolean initPropertiesFromDevice = Boolean.TRUE;
	private Boolean initPropertiesProcessed = Boolean.FALSE;
	private Set<DeviceGrpMember> deviceGrpMembers = new HashSet<DeviceGrpMember>();
	private Set<DeviceSchedule> deviceSchedules = new HashSet<DeviceSchedule>();
	private Set heartbeatEvents = new HashSet();
	private Set<ContentRotationTarget> contentRotationTargets = new HashSet<ContentRotationTarget>();
	private Set<AssetExclusion> assetExclusions = new HistorizableSet<AssetExclusion>();
	private Set<AlertDevice> alertDevices = new HashSet<AlertDevice>();
	private List deviceCommands = new LinkedList();			
	
	/**
	 * 
	 *
	 */
	public Device()
	{		
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getDeviceId();
	}
	
	/**
	 * Helper method used to create a new device. Called from CreateDeviceAction
	 * 
	 * @param deviceName
	 * @param macAddr
	 * @param additionalContentAmount
	 * @param schedulingHorizon
	 * @param isActive
	 * @param applyAlerts
	 * @param createDefaultMcm
	 * @return
	 * @throws InterruptedException
	 */
	public static Device create( String deviceName, String macAddr, Integer additionalContentAmount, Integer schedulingHorizon,
			String contentUpdateType, Boolean applyAlerts, Boolean createDefaultMcm) throws InterruptedException, ClassNotFoundException, CloneNotSupportedException, ParseException
	{
		return Device.createOrUpdate( null, deviceName, macAddr, additionalContentAmount, schedulingHorizon, BillingStatusType.STAGING.getPersistentValue(), contentUpdateType, applyAlerts, 
				null, null, null, null, null, null, null,
				null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, 
				null, null, null, null, null, null, null,
				null, null, null, null, null, null, createDefaultMcm,
				false, false, null, null, null, null, null, null, null, false, false, false );
	}
	
	/**
	 * If the given device is not null, it will update the device with the given properties as long as the 
	 * given property is not null or blank.
	 * Otherwise, it will create a new device with the given properties.
	 * 
	 * @param device
	 * @param deviceName
	 * @param macAddress
	 * @param csLength
	 * @param csHorizon
	 * @param isActive
	 * @param applyAlerts
	 * @param autoUpdate
	 * @param display
	 * @param outputMode
	 * @param resolution
	 * @param vertRefreshMin
	 * @param vertRefreshMax
	 * @param horizRefreshMin
	 * @param horizRefreshMax
	 * @param pixelClock
	 * @param orientation
	 * @param scalingMode
	 * @param timezone
	 * @param edgeServer
	 * @param masterDevice
	 * @param heartbeatInterval
	 * @param maxFileStorage
	 * @param memoryThreshold
	 * @param iowaitThreshold
	 * @param cpuThreshold
	 * @param loadThreshold
	 * @param screenshotUploadTime
	 * @param volume
	 * @param lcdPin
	 * @param lcdBranding
	 * @param dhcpEnabled
	 * @param ipAddress
	 * @param netmask
	 * @param gateway
	 * @param dnsServer
	 * @param networkInterface
	 * @param receiveSiteId
	 * @param multicastEnabled
	 * @param createDefaultMcm
	 * @param type2VideoPlayback
	 * @param useChrome
	 * @param chromeDisableGpu
	 */
	public static Device createOrUpdate(Device device, String deviceName, String macAddress, Integer csLength, Integer csHorizon, String billingStatus, String contentUpdateType, Boolean applyAlerts, 
			DeviceAutoUpdateType autoUpdate, String display, OutputModeType outputMode, String resolution, DisplayRotationType orientation,
			DeviceScalingModeType scalingMode, String timezone, Device edgeServer, Boolean redirectGateway, Device masterDevice, String heartbeatInterval,
			Float maxFileStorage, Float memoryThreshold, Float iowaitThreshold, Float cpuThreshold, Float loadThreshold, Date screenshotUploadTime, 
			String volume, String lcdPin, String lcdBranding, Boolean dhcpEnabled, String ipAddress, String connectAddress, String netmask, String gateway, String dnsServer, 
			String networkInterface, String dispatcherServers, String filesyncStartTime, String filesyncEndTime, Integer xOffset, Integer yOffset, Integer zoom,
			Device templateDevice, Boolean createDefaultMcm, boolean removeEdgeServer, boolean removeMasterDevice, String antivirusScan, Integer downloadPriority,
			String audioNormalization, FramesyncType framesync, String alphaCompositing, Integer bandwidthLimit, String audioConnection, 
			Boolean type2VideoPlayback, Boolean useChrome, Boolean chromeDisableGpu) throws InterruptedException, ClassNotFoundException, CloneNotSupportedException, ParseException
	{
		SimpleDateFormat timeFormat = new SimpleDateFormat( Constants.TIME_FORMAT );
		boolean isUpdate = false;
		boolean createHistoryEntries = false;
		
		// If a device was passed in, we must be performing an update
		if( device != null ){
			isUpdate = true;
		}
		else
		{
			// If a templateDevice was passed in -- copy the template device
			if( templateDevice != null )
			{
				// If a deviceName was not given, use the macAddress
				device = templateDevice.copy( deviceName != null && deviceName.length() > 0 ? deviceName : macAddress, macAddress );
			}
			else
			{
				// Create a new device
				device = new Device();
				device.setCreateDate(new Date());
				createHistoryEntries = true;
			}
		}
		
		if( deviceName != null && deviceName.length() > 0 ){
			device.setDeviceName( deviceName.trim() );	
		}
		// If we are creating a new device and the device name was not passed in,
		// use the mac address for the device name.
		else if( !isUpdate && macAddress != null && macAddress.length() > 0 )
		{
			// If a deviceName was not given, use the macAddress
			device.setDeviceName( macAddress );
		}
		
		if( macAddress != null && macAddress.length() > 0 ){
			// If we are creating a new device
			if(isUpdate == false){
				// Create an entry in the mac_addr_schema if a row with this macAddr doesn't already exist
				MacAddrSchema mas = MacAddrSchema.getMacAddrSchema(macAddress.trim());
				if(mas == null){
					MacAddrSchema.create(macAddress.trim(), SchemaDirectory.getSchemaName());
				}
			}else{
				// Check if the macAddr has changed
				if(Device.propertyHasChanged(device.getMacAddr(), macAddress.trim())){
					// Get the current row associated to this macAddr and update it
					MacAddrSchema mas = MacAddrSchema.getMacAddrSchema(device.getMacAddr());
					if(mas != null){
						mas.setMacAddr(macAddress.trim());
						mas.update();
					}
					// To create rows for all devices that don't have corresponding rows as of 3.4
					else{
						MacAddrSchema.create(macAddress.trim(), SchemaDirectory.getSchemaName());
					}
				}
			}
			
			device.setMacAddr( macAddress.trim() );
			
		}
		
		// If we're updating an existing device
		if( isUpdate )
		{
			String rotation = orientation != null ? orientation.getPersistentValue() : null;
			String strScalingMode = scalingMode != null ? scalingMode.getPersistentValue() : null;
			String strOutputMode = outputMode != null ? outputMode.getPersistentValue() : null;
			SimpleDateFormat scTimeFormat = new SimpleDateFormat( "hh:mm:ss a" );
			String strScreenshotUploadTime = screenshotUploadTime != null ? scTimeFormat.format( screenshotUploadTime ) : null;
			String strEdgeServer = removeEdgeServer ? Constants.NULL : edgeServer != null ? edgeServer.getDeviceId().toString() : null;
			String strIoWaitThreshold = iowaitThreshold != null ? iowaitThreshold.toString() : null;
			String strCpuThreshold = cpuThreshold != null ? cpuThreshold.toString() : null;
			String strMemoryThreshold = memoryThreshold != null ? memoryThreshold.toString() : null;
			String strLoadThreshold = loadThreshold != null ? loadThreshold.toString() : null;
			String strAutoUpdate = autoUpdate != null ? autoUpdate.getPersistentValue() : null;
			String strMasterDevice = masterDevice != null ? masterDevice.getDeviceId().toString() : null;
			String strCsHorizon = csHorizon != null ? csHorizon.toString() : null;
			String strCsLength = csLength != null ? csLength.toString() : null;
			String strMaxFileStorage = maxFileStorage != null ? maxFileStorage.toString() : null;
			String strFramesync = framesync != null ? framesync.toString() : null;
			Boolean multicastEnabled = null;
			String receiveSiteId = null;
			String diskCapacity = null;
			String diskUsage = null;
			String version = null;
			String osVersion = null;
			
			// Handle filesyncStartTime issues. This special case is due to the fact that we null out the database field
			// in case of an 'off'. The default value is 12:00 AM though. Since this field is nullable
			// according to the device property, we need to avoid sending a null unless we are meant to set it to off.
			if(filesyncStartTime == null || filesyncStartTime.length() == 0){
				// Since we are updating the device and the filesyncStartTime doesn't exists, we will use the devices value.
				if(device.filesyncStartTime != null){
					filesyncStartTime = timeFormat.format(device.filesyncStartTime);
				}
			}
			
			// Update each property that has changed
			device.updateProperties( rotation, strScalingMode, strOutputMode,
					strScreenshotUploadTime, filesyncStartTime, filesyncEndTime, timezone,
					resolution, heartbeatInterval, strMaxFileStorage, 
					dhcpEnabled, ipAddress, connectAddress, gateway, netmask, networkInterface, dispatcherServers,
					dnsServer, strEdgeServer, redirectGateway, lcdPin, lcdBranding, volume,
					strIoWaitThreshold, strCpuThreshold, strMemoryThreshold, strLoadThreshold, 
					display, strAutoUpdate, strMasterDevice, strCsHorizon, strCsLength, billingStatus, contentUpdateType, applyAlerts, xOffset, yOffset, zoom, 
					multicastEnabled, receiveSiteId, diskCapacity, diskUsage, version, osVersion, 
					antivirusScan, downloadPriority, audioNormalization, strFramesync, null, null, null, null, alphaCompositing, bandwidthLimit, audioConnection, 
					type2VideoPlayback, useChrome, chromeDisableGpu, null, false, false, false);
			
			if( masterDevice != null ){
				device.setMirrorSource( masterDevice );
			}else if( removeMasterDevice ){
				device.setMirrorSource( null );
			}			
		}
		else
		{
			// Generate a 12:00 AM, 1970 default calendar
			Calendar midnight1970 = Calendar.getInstance();
			midnight1970.set( Calendar.HOUR_OF_DAY, 0 );
			midnight1970.set( Calendar.MINUTE, 0 );
			midnight1970.set( Calendar.SECOND, 0 );
			midnight1970.set( Calendar.MILLISECOND, 0 );
			midnight1970.set( Calendar.YEAR, 1970);
			midnight1970.set( Calendar.MONTH, Calendar.JANUARY);
			midnight1970.set( Calendar.DATE, 1);
			
			// Either set the property to the given value, or use the appropriate defaults for each property
			if( csLength != null ){
				device.setAdditionalContentAmount( csLength );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setAdditionalContentAmount( Integer.valueOf( Constants.ADDITIONAL_CONTENT_AMOUNT_DEFAULT ) );
			}
			if( csHorizon != null ){
				device.setContentSchedulingHorizon( csHorizon );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setContentSchedulingHorizon( Integer.valueOf( Constants.SCHEDULING_HORIZON_DEFAULT ) );			
			}
			if( applyAlerts != null ){
				device.setApplyAlerts( applyAlerts );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setApplyAlerts( Boolean.TRUE );			
			}
			if( contentUpdateType != null ){
				device.setContentUpdateType(contentUpdateType);
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setContentUpdateType(ContentUpdateType.NETWORK.getPersistentValue());
			}
			if( autoUpdate != null ){
				device.setAutoUpdate( autoUpdate );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setAutoUpdate( DeviceAutoUpdateType.ON );			
			}
			if( display != null && display.length() > 0 ){
				device.setDisplay( display );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setDisplay( Constants.CUSTOM_DISPLAY_NAME );			
			}
			if( outputMode != null ){
				device.setOutputMode( outputMode );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setOutputMode( OutputModeType.AUTO );			
			}
			if( resolution != null && resolution.length() > 0 ){
				device.setResolution( resolution );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setResolution( DevicePropertyType.RESOLUTION_DEFAULT );			
			}				
			if( orientation != null ){
				device.setRotation( orientation );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setRotation( DisplayRotationType.HORIZONTAL );			
			}
			if( scalingMode != null ){
				device.setScalingMode( scalingMode );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setScalingMode( DeviceScalingModeType.NONE );			
			}			
			if( timezone != null && timezone.length() > 0 ){
				device.setTimezone( timezone );
			}else if( templateDevice == null ){
				// Default timezone to server's timezone
				Calendar c = Calendar.getInstance();
				TimeZone tz = c.getTimeZone();
				device.setTimezone( tz.getID() );
			}
			if( edgeServer != null ){
				device.setEdgeServer( edgeServer );
			}else if( removeEdgeServer ){
				device.setEdgeServer( null );
			}
			if( redirectGateway != null ){
				device.setRedirectGateway( redirectGateway );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setRedirectGateway( Boolean.FALSE );			
			}	
			if( masterDevice != null ){
				device.setMirrorSource( masterDevice );
			}else if( removeMasterDevice ){
				device.setMirrorSource( null );
			}	
			if( heartbeatInterval != null && heartbeatInterval.length() > 0 ){
				device.setHeartbeatInterval( heartbeatInterval );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setHeartbeatInterval( DevicePropertyType.HEARTBEAT_INTERVAL_DEFAULT );			
			}				
			if( maxFileStorage != null ){
				device.setMaxFileStorage( maxFileStorage );
			}
			if( memoryThreshold != null ){
				device.setMemoryThreshold( memoryThreshold );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setMemoryThreshold( MEMORY_THRESHOLD_DEFAULT );			
			}
			if( iowaitThreshold != null ){
				device.setIowaitThreshold( iowaitThreshold );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setIowaitThreshold( IOWAIT_THRESHOLD_DEFAULT );			
			}
			if( cpuThreshold != null ){
				device.setCpuThreshold( cpuThreshold );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setCpuThreshold( CPU_THRESHOLD_DEFAULT );			
			}
			if( loadThreshold != null ){
				device.setLoadThreshold( loadThreshold );	
			}else if( templateDevice == null ){
				// Use default if template device was not specified
				device.setLoadThreshold( LOAD_THRESHOLD_DEFAULT );			
			}
			if( screenshotUploadTime != null ){
				device.setScreenshotUploadTime( screenshotUploadTime );
			}else if( templateDevice == null ){
				device.setScreenshotUploadTime( midnight1970.getTime() );
			}
			if( volume != null && volume.length() > 0 ){
				device.setVolume( volume );
			}else{
				// Use default
				device.setVolume(VOLUME_DEFAULT);
			}
			if( lcdPin != null && lcdPin.length() > 0 ){
				device.setLcdPin( lcdPin );
			}else{
				// Use default
				device.setLcdPin(LCD_PIN_DEFAULT);
			}
			if( lcdBranding != null && lcdBranding.length() > 0 ){
				device.setLcdBranding( lcdBranding );
			}else{
				// Use default
				device.setLcdBranding(LCD_BRANDING_DEFAULT);
			}
			if( dhcpEnabled != null ){
				device.setDhcpEnabled( dhcpEnabled );
			}else{
				device.setDhcpEnabled(DHCP_ENABLED_DEFAULT);
			}
			if( ipAddress != null && ipAddress.length() > 0 ){
				device.setIpAddr( ipAddress );
			}	
			if( connectAddress != null && connectAddress.length() > 0 ){
				device.setConnectAddr( connectAddress );
			}				
			if( netmask != null && netmask.length() > 0 ){
				device.setNetmask( netmask );
			}	
			if( gateway != null && gateway.length() > 0 ){
				device.setGateway( gateway );
			}			
			if( dnsServer != null && dnsServer.length() > 0 ){
				device.setDnsServer( dnsServer );
			}
			if( networkInterface != null && networkInterface.length() > 0 ){
				device.setNetworkInterface( networkInterface );
			}
			if( filesyncStartTime != null && filesyncStartTime.length() > 0){
				if(filesyncStartTime.equalsIgnoreCase(Constants.OFF)){
					device.setFilesyncStartTime(null);
				}else{
					device.setFilesyncStartTime( timeFormat.parse(filesyncStartTime) );
				}
			}else{
				device.setFilesyncStartTime( midnight1970.getTime() );
			}		
			if( filesyncEndTime != null && filesyncEndTime.length() > 0){
				device.setFilesyncEndTime( timeFormat.parse(filesyncEndTime) );
			}else{
				device.setFilesyncEndTime( midnight1970.getTime() );
			}
			if( dispatcherServers != null ){
				device.setDispatcherServers( dispatcherServers );
			}
			if( xOffset != null ){
				device.setxOffset( xOffset );
			}
			if( yOffset != null ){
				device.setyOffset( yOffset );
			}
			if( zoom != null ){
				device.setZoom( zoom );
			}
			if (antivirusScan != null){
				device.setAntivirusScan(antivirusScan);
			}else{
				// Use default
				device.setAntivirusScan(Constants.OFF);
			}
			if(downloadPriority != null){
				device.setDownloadPriority(downloadPriority);
			}else{
				// Use default
				device.setDownloadPriority(DOWNLOAD_PRIORITY_DEFAULT);
			}
			if(audioNormalization != null){
				device.setAudioNormalization(audioNormalization);
			}else{
				// Use default
				device.setAudioNormalization(Constants.OFF);
			}
			if(alphaCompositing != null){
				device.setAlphaCompositing(alphaCompositing);
			}else{
				// Use default
				device.setAlphaCompositing("Enable");
			}
			if(audioConnection != null){
				device.setAudioConnection(AudioConnectionType.getAudioConnectionType(audioConnection));
			}else{
				// Use default
				device.setAudioConnection(AudioConnectionType.STEREO_JACK);
			}
			if(framesync != null){
				device.setFramesync(framesync);
			}else{
				device.setFramesync(FramesyncType.OFF);
			}
			if( type2VideoPlayback != null ){
				device.setType2VideoPlayback(type2VideoPlayback);
			}else if( type2VideoPlayback == null ){
				device.setType2VideoPlayback( Boolean.FALSE );
			}
			if( useChrome != null ){
				device.setUseChrome(useChrome);
			}else if( useChrome == null ){
				device.setUseChrome( Boolean.FALSE );
			}
			if( chromeDisableGpu != null ){
				device.setChromeDisableGpu(chromeDisableGpu);
			}else if( chromeDisableGpu == null ){
				device.setChromeDisableGpu( Boolean.FALSE );
			}
			device.setDeviceSideScheduling(true);
		}

		// Either save or update the device object
		if( isUpdate ){
			device.update();
		}
		else
		{
			/*
			 * If we're creating a new device, set some default values
			 */
			device.setStreamingClientCapable( Boolean.TRUE );
			device.setStreamingServerCapable( Boolean.FALSE );
			
			// Hardcoding last_scheduled content to 4am of current day
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 4);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);				
			device.setLastScheduledContent( c.getTime() );				
			device.save();
			
			if(createHistoryEntries){
				// Create history entries for name and mac_addr
				String username = KmfSession.getKmfSession() != null && KmfSession.getKmfSession().getAppUsername() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
				HistoryEntry.logEvent("create", device, device.getDeviceName(), null, "deviceName", SchemaDirectory.getProgram(), username);
				HistoryEntry.logEvent("create", device, device.getMacAddr(), null, "macAddr", SchemaDirectory.getProgram(), username);
			}
			
			// We need the device id to be able to encrypt the billing status
			if( billingStatus != null){
				device.setBillingStatus(billingStatus, false);
			}else if(templateDevice == null){
				// Use default if template device was not specified
				device.setBillingStatus(BillingStatusType.STAGING.getPersistentValue(), false);
			}
			
			device.update();
		}
		
		// If the box was checked to create a default MCM
		if( createDefaultMcm != null && createDefaultMcm.equals( Boolean.TRUE ) )
		{
			// And this device does not already have an MCM associated with it
			if( device.getMcms().size() == 0 )
			{
				// Create an MCM for this device
				Mcm.create( "MCM 1", device );
			}
		}	
		return device;
	}
	
	/**
	 * 
	 * @param getAllDevices
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param formatter
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static String treeViewFormat(boolean getAllDevices, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, Method formatter) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException
	{		
		Method allBranchMethod = null;
		StringBuffer result = new StringBuffer();	
		
		if(getAllDevices == true)
		{	
			// Declare the method that will be used to build the "All Devices" branch of the tree
			Class[] methodParamTypes = { boolean.class, boolean.class, boolean.class, boolean.class };			
			allBranchMethod = Class.forName(Device.class.getName()).getDeclaredMethod("getAllDevicesBranch", methodParamTypes);
		}					
		result.append( Grp.getTree(Constants.DEVICE_GROUPS, includeLeaves, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, Constants.TYPE_DEVICE_GROUP, formatter, allBranchMethod) );
		return result.toString();
	}
	/**
	 * 
	 * @param getAllDevices
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param formatter
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static String treeViewFormatSchedule(boolean getAllDevices, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, Method formatter) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException
	{		
		Method allBranchMethod = null;
		StringBuffer result = new StringBuffer();	
		
		if(getAllDevices == true)
		{	
			// Declare the method that will be used to build the "All Devices" branch of the tree
			Class[] methodParamTypes = { boolean.class, boolean.class, boolean.class, boolean.class };
			allBranchMethod = Class.forName(Device.class.getName()).getDeclaredMethod("getAllDevicesBranchSchedule", methodParamTypes);
		}					
		result.append( Grp.getTree(Constants.DEVICE_GROUPS, includeLeaves, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, Constants.TYPE_DEVICE_GROUP, formatter, allBranchMethod) );
		return result.toString();
	}	
	/**
	 * 
	 * @param getAllDevices
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param formatter
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static String treeViewFormatMasterDevicesOnly(boolean getAllDevices, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, Method formatter) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException
	{		
		Method allBranchMethod = null;
		StringBuffer result = new StringBuffer();	
		
		if(getAllDevices == true)
		{	
			// Declare the method that will be used to build the "All Devices" branch of the tree
			Class[] methodParamTypes = { boolean.class, boolean.class, boolean.class, boolean.class };
			allBranchMethod = Class.forName(Device.class.getName()).getDeclaredMethod("getAllDevicesBranchMasterDevicesOnly", methodParamTypes);
		}					
		result.append( Grp.getTree(Constants.DEVICE_GROUPS, includeLeaves, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, Constants.TYPE_DEVICE_GROUP, formatter, allBranchMethod) );
		return result.toString();
	}		
	/**
	 * 
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @return
	 * @throws HibernateException
	 */
	public static String getAllDevicesBranchSchedule(boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, boolean includeAllLeaves) throws HibernateException
	{		
		StringBuffer result = new StringBuffer();
		String href = "\'javascript:allDevicesOnClick()\'";
		result.append("[");
		result.append("{id:-1}, \"<span class=\\\"treeNode\\\">All Devices</span>\", "+ href +", null, type_device_group,");
						
		List l = Device.getMasterDevices();			
		for(int i=0; i<l.size(); i++)
		{
			Device d = (Device)l.get(i);
			result.append( d.treeViewFormatSchedule(1, true, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, null, null) );
			
			// Limit the number of child nodes
			if( i >= Constants.MAX_CHILD_NODES - 1 ){
				
				// Append a "more" child node and break
				result.append("[");					
				result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, type_device,");
				result.append("],\n");
				break;
			}		
		}				
		result.append("],\n");
		return result.toString();
	}	
	/**
	 * 
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @return
	 * @throws HibernateException
	 */
	public static String getAllDevicesBranchMasterDevicesOnly(boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, boolean includeAllLeaves) throws HibernateException
	{		
		StringBuffer result = new StringBuffer();
		String onClick = "";	
		String onDoubleClick = "";
		if( includeHref && doubleClickLeavesOnly == false )
		{
			onClick = "\'javascript:allDevicesOnClick()\'";							
		}		
		if( includeDoubleClick && doubleClickLeavesOnly == false )
		{
			// Pass in the device group suffix so we know how to handle it in javascript
			onDoubleClick = " onDblClick=\\\"grpOnDoubleClick('-1', '"+ Constants.DEVICE_GROUP_SUFFIX +"')\\\"";	
		}			
		result.append("[");
		result.append("{id:-1}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">All Devices</span>\","+ onClick +", null, type_device_group,\n");
				
		// If we are include all leaves
		if( includeAllLeaves )
		{		
			List l = Device.getMasterDevices(true); 		
			for(int i=0; i<l.size(); i++)
			{
				Device d = (Device)l.get(i);
				result.append( d.treeViewFormatMasterDevicesOnly(1, true, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, null, null) );
				
				// Limit the number of child nodes
				if( i >= Constants.MAX_CHILD_NODES - 1 ){
					
					// Append a "more" child node and break
					result.append("[");					
					result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, type_device,");
					result.append("],\n");
					break;
				}					
			}			
		}
		result.append("],\n");
		return result.toString();
	}		
	/**
	 * 
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param treeNodeCssClass
	 * @param allBranchMethod
	 * @return
	 */
	public String treeViewFormatSchedule(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();		
		if(includeLeaves == true)
		{	
			// If this device does not have a master
			if( this.getMirrorSource() == null )
			{				
				// Build the string for each device
				String url = "\'javascript:top.deviceScheduleOnClick("+ this.getDeviceId() +")\'";
				result.append("[");					
				result.append("{id:"+ this.getDeviceId() +"}, \"<span class=\\\"treeNodeSchedule\\\">"+ Reformat.jspEscape(this.getDeviceName()) + "</span>\", "+ url +", null, type_device,");	
				result.append("],\n");
			}
		}
		return result.toString();
	}	
	/**
	 * 
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param treeNodeCssClass
	 * @param allBranchMethod
	 * @return
	 */
	public String treeViewFormatMasterDevicesOnly(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();			
		if(includeLeaves == true)
		{	
			// If this device does not have a master
			if( this.getMirrorSource() == null )
			{				
				// Build the string for each device
				String onClick = "null";	
				String onDoubleClick = "";
				if( includeHref )
				{
					onClick = "\'javascript:top.deviceOnClick("+ this.getDeviceId() +")\'";	
				}		
				if( includeDoubleClick )
				{
					onDoubleClick = " onDblClick=\\\"add('treeNodeDevice')\\\"";
				}
				result.append("[");					
				result.append("{id:"+ this.getDeviceId() +"}, \"<span class=\\\"treeNodeDevice\\\""+ onDoubleClick +">"+ Reformat.jspEscape(this.getDeviceName()) + "</span>\", "+ onClick +", null, type_device,");	
				result.append("],\n");
			}
		}
		return result.toString();
	}		
	/**
	 * 
	 * @param deviceName
	 * @return
	 * @throws HibernateException
	 */
	public static String getAllDevicesBranch(boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, boolean includeAllLeaves) throws HibernateException
	{		
		String onClick = "";
		String onDoubleClick = "";
		if( ( includeHref ) && (doubleClickLeavesOnly == false) )
		{
			onClick = "\'javascript:grpOnClick(-1)\'";
		}	
		if( ( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
		{
			onDoubleClick = " onDblClick=\\\"grpOnDoubleClick(-1, '"+ Constants.DEVICE_GROUP_SUFFIX +"')\\\"";	
		}	
				
		StringBuffer result = new StringBuffer();
		result.append("[");
		result.append("{id:-1}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">All Devices</span>\", "+ onClick +", null, type_device_group,\n");		
		
		// If we're including the leaves of this branch
		if( includeAllLeaves ){
			List l = Device.getDevices();		
			for(int i=0; i<l.size(); i++)
			{
				Device d = (Device)l.get(i);
				result.append( d.treeViewFormat(0, true, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, "type_device_group", null) );
				
				// Limit the number of child nodes
				if( i >= Constants.MAX_CHILD_NODES - 1 ){
					
					// Append a "more" child node and break
					result.append("[");					
					result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, type_device_group,");
					result.append("],\n");
					break;
				}					
			}				
		}			
		result.append("],\n");
		return result.toString();
	}
	/**
	 * 
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();			
		if( includeLeaves == true && (this.getDeleted() == null || this.getDeleted().equals( Boolean.FALSE )) )
		{	
			// Build the string for each device
			String onClick = "null";	
			String onDoubleClick = "";
			if( includeHref )
			{
				onClick = "\'javascript:top.deviceOnClick("+ this.getDeviceId() +")\'";	
			}		
			if( includeDoubleClick )
			{
				onDoubleClick = " onDblClick=\\\"javascript:add('treeNodeDevice')\\\"";
			}
			result.append("[");					
			result.append("{id:"+ this.getDeviceId() +"}, \"<span class=\\\"treeNodeDevice\\\""+ onDoubleClick +">"+ Reformat.jspEscape(this.getDeviceName()) + "</span>\", "+ onClick +", null, type_device,");	
			result.append("],\n");
		}
		return result.toString();
	}
	
	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws HibernateException
	 */
	public static Device getDevice(Long deviceId) throws HibernateException
	{
		return (Device)Entity.load(Device.class, deviceId);		
	}
	
	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws HibernateException
	 */
	public static List<Device> getDevices(List<Long> deviceIds) throws HibernateException
	{
		return Entity.load(Device.class, deviceIds);		
	}
	
	public static List getDeviceNames(List deviceIds) throws HibernateException
	{		
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT d.deviceName "
			+ "FROM Device as d WHERE d.deviceId IN (:deviceIds) "				
			+ "ORDER BY UPPER(d.deviceName)"; 		
		return session.createQuery( hql ).setParameterList("deviceIds", deviceIds).list();	
	}
	
	/**
	 * Returns true if a device with the given macAddr already exists in the database
	 * 
	 * @param deviceName
	 * @return
	 */
	public static Device getDeviceByMacAddr(String macAddr) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		Device d = (Device) session.createCriteria(Device.class)
					.add( Expression.eq("macAddr", macAddr.trim()).ignoreCase() )
					.uniqueResult();
		return d;
	}
	
	public static List getRepurposedDevices(String macAddr) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		return session.createQuery("SELECT d FROM Device as d WHERE d.macAddr LIKE '" + macAddr + "-R%' ORDER BY d.macAddr DESC").list();
	}
	
	/**
	 * Returns true if a device with the given name already exists in the database
	 * 
	 * @param deviceName
	 * @return
	 */
	public static boolean deviceExists(String deviceName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(Device.class)
					.add( Expression.eq("deviceName", deviceName.trim()).ignoreCase() )
					.list();
		return l.size() > 0 ? true : false;
	}
	
	/**
	 * Returns true if a device with the given hoststring already exists in the database
	 * 
	 * @param deviceName
	 * @return
	 */
	public static boolean macAddressExists(String macAddress) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		List l = session.createCriteria(Device.class)
					.add( Expression.eq("macAddr", macAddress.trim()).ignoreCase() )
					.list();		
		return l.size() > 0 ? true : false;
	}	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List getDeviceGroups() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List l = session.createCriteria(DeviceGrpMember.class)
				.add( Expression.eq("device", this))
				.list();
		return l;
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Device> getDevices() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List<Device> l = session.createQuery(				
				"SELECT d "
				+ "FROM Device as d "				
				+ "WHERE (d.deleted is null OR d.deleted = 0) "
				+ "ORDER BY UPPER(d.deviceName)"						
				).list();				
		return l;
	}
	
	/**
	 * Get all devices that have the given asset currently scheduled
	 * in either a playlist or segment
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getDevices(Asset a) throws HibernateException
	{
		List result = new LinkedList();		
		Session session = HibernateSession.currentSession();		
		
		// First get all master devices that have this asset (or this asset is paired to one of the assets) in one of their scheduled playlists
		String hql = "SELECT DISTINCT device "
			+ "FROM PlaylistSegmentPart psp "
			+ "INNER JOIN psp.segment.deviceSchedules as ds "
			+ "INNER JOIN ds.device as device "
			+ "LEFT JOIN psp.playlist.playlistAssets as playlistAsset "
			+ "LEFT JOIN psp.playlist.playlistContentRotations as playlistContentRotation "
			+ "LEFT JOIN playlistContentRotation.contentRotation as contentRotation "
			+ "LEFT JOIN contentRotation.contentRotationAssets as contentRotationAsset "
			+ "LEFT JOIN playlistAsset.assetPresentation.pairedDisplayareas as pairedDisplayarea "
			+ "LEFT JOIN pairedDisplayarea.pairedAssets as pairedAsset "
			+ "WHERE device.mirrorSource IS NULL "
			+ "AND ((playlistAsset.asset.assetId = "+ a.getAssetId().toString() +") "
			+ "OR (pairedAsset.asset.assetId = "+ a.getAssetId().toString() +") "
			+ "OR (contentRotationAsset.asset.assetId = "+ a.getAssetId().toString() +"))";			
		result = session.createQuery( hql ).list();	
						
		// Next, get all master devices that have this asset in one of their scheduled segments
		hql = "SELECT DISTINCT device "
				+ "FROM AssetSegmentPart asp "
				+ "JOIN asp.segment.deviceSchedules as ds "
				+ "JOIN ds.device as device "
				+ "WHERE device.mirrorSource IS NULL "
				+ "AND asp.asset.assetId = "+ a.getAssetId().toString();				
		Iterator i = session.createQuery( hql ).list().iterator();
		
		// Only add devices that we have not already added
		for( Iterator iter=i; i.hasNext(); ) {
			Device d = (Device)i.next();
			if(result.contains( d ) == false) {
				result.add( d );
			}
		}	
		
		// Get all devices that 
		return result;
	}	
	
	/**
	 * 
	 * @return Returns a list of devices matching the provided deviceName
	 * @throws HibernateException
	 */
	public static List<Device> getDevices(String deviceName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		List<Device> l = session.createCriteria(Device.class)
				.add( Expression.eq("deviceName", deviceName).ignoreCase() )
				.list();		
		return l;
	}
	
	public static List<Device> getDevicesForGetPresentation(Asset a, Device edgeServer){
		List<Device> result = new LinkedList<Device>();		
		Session session = HibernateSession.currentSession();
		String hql = "SELECT DISTINCT ft.device FROM FileTransmission ft WHERE ft.device.edgeServer.deviceId = :edgeServerId "
					+ "AND ft.filename LIKE :assetIdClause AND ft.status IN ( '" + FileTransmissionStatus.NEEDED.getPersistentValue() + "', '" + FileTransmissionStatus.NEEDED_FOR_FUTURE.getPersistentValue() + "' )";
		result = session.createQuery(hql).setParameter("edgeServerId", edgeServer.getDeviceId()).setParameter("assetIdClause", "%/" + a.getAssetId() + "-%").list();
		return result;
	}
	
	/**
	 * Get all devices that have the given asset currently scheduled
	 * in either a playlist or segment. 
	 * If an edgeServer is passed in, this method will return any devices 
	 * that are "node" devices under the given edgeServer, and will include "slave" devices.
	 * If an edgeServer is not passed in, this method will return only master devices.  
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Device> getDevices(Asset a, Device edgeServer) throws HibernateException
	{
		List<Device> result = new LinkedList<Device>();		
		Session session = HibernateSession.currentSession();
		
		// First get all devices that have this asset (or this asset is paired to one of the assets) in one of their scheduled playlists
		String hql = "SELECT DISTINCT device "
			+ "FROM PlaylistSegmentPart psp "
			+ "INNER JOIN psp.segment.deviceSchedules as ds "
			+ "INNER JOIN ds.device as device "
			+ "INNER JOIN psp.playlist.playlistAssets as playlistAsset ";
						
			// If we only want devices that are "nodes" to the given edgeServer (include slave devices)
			if( edgeServer != null ){
				hql += "WHERE device.edgeServer.deviceId = "+ edgeServer.getDeviceId().toString() +" ";
			}
			// Make sure we only include master devices
			else{
				hql += "WHERE device.mirrorSource IS NULL ";	
			}
			
			hql += "AND playlistAsset.asset.assetId = "+ a.getAssetId().toString();
			
		result.addAll(session.createQuery( hql ).list());
		
		// First get all devices that have this asset (or this asset is paired to one of the assets) in one of their scheduled playlists
		hql = "SELECT DISTINCT device "
			+ "FROM AssetSegmentPart asp "
			+ "INNER JOIN asp.segment.deviceSchedules as ds "
			+ "INNER JOIN ds.device as device ";
						
			// If we only want devices that are "nodes" to the given edgeServer (include slave devices)
			if( edgeServer != null ){
				hql += "WHERE device.edgeServer.deviceId = "+ edgeServer.getDeviceId().toString() +" ";
			}
			// Make sure we only include master devices
			else{
				hql += "WHERE device.mirrorSource IS NULL ";	
			}
			
			hql += "AND asp.asset.assetId = "+ a.getAssetId().toString();
		
		result.addAll(session.createQuery( hql ).list());
		
		// First get all devices that have this asset (or this asset is paired to one of the assets) in one of their scheduled playlists
		hql = "SELECT DISTINCT device "
			+ "FROM AssetSegmentPart asp "
			+ "INNER JOIN asp.segment.deviceSchedules as ds "
			+ "INNER JOIN ds.device as device "
			+ "INNER JOIN asp.asset.assetPresentation.pairedDisplayareas as pairedDisplayarea "
			+ "INNER JOIN pairedDisplayarea.pairedAssets as pairedAsset ";
						
			// If we only want devices that are "nodes" to the given edgeServer (include slave devices)
			if( edgeServer != null ){
				hql += "WHERE device.edgeServer.deviceId = "+ edgeServer.getDeviceId().toString() +" ";
			}
			// Make sure we only include master devices
			else{
				hql += "WHERE device.mirrorSource IS NULL ";	
			}
			
			hql += "AND pairedAsset.asset.assetId = "+ a.getAssetId().toString();
		
		result.addAll(session.createQuery( hql ).list());
		
		// First get all devices that have this asset (or this asset is paired to one of the assets) in one of their scheduled playlists
		hql = "SELECT DISTINCT device "
			+ "FROM PlaylistSegmentPart psp "
			+ "INNER JOIN psp.segment.deviceSchedules as ds "
			+ "INNER JOIN ds.device as device "
			+ "INNER JOIN psp.playlist.playlistContentRotations as playlistContentRotation "
			+ "INNER JOIN playlistContentRotation.contentRotation as contentRotation "
			+ "INNER JOIN contentRotation.contentRotationAssets as contentRotationAsset ";
						
			// If we only want devices that are "nodes" to the given edgeServer (include slave devices)
			if( edgeServer != null ){
				hql += "WHERE device.edgeServer.deviceId = "+ edgeServer.getDeviceId().toString() +" ";
			}
			// Make sure we only include master devices
			else{
				hql += "WHERE device.mirrorSource IS NULL ";	
			}
			
			hql += "AND contentRotationAsset.asset.assetId = "+ a.getAssetId().toString();
		
		result.addAll(session.createQuery( hql ).list());
		
		// First get all devices that have this asset (or this asset is paired to one of the assets) in one of their scheduled playlists
		hql = "SELECT DISTINCT device "
			+ "FROM PlaylistSegmentPart psp "
			+ "INNER JOIN psp.segment.deviceSchedules as ds "
			+ "INNER JOIN ds.device as device "
			+ "INNER JOIN psp.playlist.playlistAssets as playlistAsset "
			+ "INNER JOIN playlistAsset.assetPresentation.pairedDisplayareas as pairedDisplayarea "
			+ "INNER JOIN pairedDisplayarea.pairedAssets as pairedAsset ";
				
			// If we only want devices that are "nodes" to the given edgeServer (include slave devices)
			if( edgeServer != null ){
				hql += "WHERE device.edgeServer.deviceId = "+ edgeServer.getDeviceId().toString() +" ";
			}
			// Make sure we only include master devices
			else{
				hql += "WHERE device.mirrorSource IS NULL ";	
			}
			
			hql += "AND pairedAsset.asset.assetId = "+ a.getAssetId().toString();
			
		result.addAll(session.createQuery( hql ).list());
		
		// Next, get all master devices that have this asset in one of their scheduled segments
		hql = "SELECT DISTINCT device "
				+ "FROM AssetSegmentPart asp "
				+ "JOIN asp.segment.deviceSchedules as ds "
				+ "JOIN ds.device as device ";				
				
				// If we only want devices that are "nodes" to the given edgeServer (include slave devices)
				if( edgeServer != null ){
					hql += "WHERE device.edgeServer.deviceId = "+ edgeServer.getDeviceId().toString() +" ";
				}
				// Make sure we only include master devices
				else{
					hql += "WHERE device.mirrorSource IS NULL ";	
				}				
				
				hql += "AND asp.asset.assetId = "+ a.getAssetId().toString();				
				
		// Only add devices that we have not already added
		List<Device> l = session.createQuery( hql ).list();
		for( Iterator<Device> i=l.iterator(); i.hasNext(); ) {
			Device d = i.next();
			if(result.contains( d ) == false) {
				result.add( d );
			}
		}	
		
		// If an edgeServer was passed in, we also want to include any slave devices to the edgeServer
		if( edgeServer != null ){
			List<Device> slaveDevices = edgeServer.getMirrorPlayerDevices();
			for( Iterator<Device> i=slaveDevices.iterator(); i.hasNext(); ) {
				Device d = i.next();
				if(result.contains( d ) == false) {
					result.add( d );
				}				
			}
		}
		
		// Get all devices that 
		return result;
	}	
	
	/**
	 * Get all devices currently airing the given segment
	 * 
	 * @param s
	 * @return
	 * @throws HibernateException
	 */
	public static List getDevices(Segment s) throws HibernateException
	{						
		// First get all devices that have this asset in one of their scheduled playlists
		String hql = "SELECT distinct ds.device "
			+ "FROM DeviceSchedule ds "
			+ "WHERE ds.segment.segmentId = "+ s.getSegmentId().toString();			
		Session session = HibernateSession.currentSession();
		List l = session.createQuery( hql ).list();
		return l;
	}
	
	public static List getDeviceIds(Segment s) throws HibernateException
	{						
		// First get all devices that have this asset in one of their scheduled playlists
		String hql = "SELECT distinct ds.device.deviceId "
			+ "FROM DeviceSchedule ds "
			+ "WHERE ds.segment.segmentId = "+ s.getSegmentId().toString();			
		Session session = HibernateSession.currentSession();
		List l = session.createQuery( hql ).list();
		return l;
	}
	
	/**
	 * Get all devices currently airing the given playlist
	 * 
	 * @param s
	 * @return
	 * @throws HibernateException
	 */
	public static List getDeviceIds(Playlist p) throws HibernateException
	{						
		String hql = "SELECT distinct ds.device.deviceId "
			+ "FROM DeviceSchedule ds "
			+ "WHERE ds.segment.segmentId IN "
			+ "(SELECT distinct ps.segment.segmentId "
			+ "FROM PlaylistSegmentPart as  ps "				
			+ "WHERE ps.playlist.playlistId = "+ p.getPlaylistId().toString() + " "
			+ "OR (ps.segment.assetIntervalPlaylist != null "
			+ "AND ps.segment.assetIntervalPlaylist.playlistId = "+ p.getPlaylistId().toString() +"))";					
		Session session = HibernateSession.currentSession();
		List l = session.createQuery( hql ).list();
		return l;
	}		
		
	/**
	 * @return Returns a page of devices
	 */
	public static Page getDevices(String hql, int pageNum, int iSelectedItemsPerPage, Object[] params, String namedParameter, boolean isEntityOverviewPage, boolean returnIdsOnly) throws HibernateException 
	{		
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery( hql );

		// If the params parameter was passed in, use them in the query
		if( params != null )
		{
			// If a namedParameter was passed in -- use it
			// This is required because .setParameterList(int, Object[]) is not supported
			if( namedParameter != null ){
				q.setParameterList( namedParameter, params );
			}
			else{
				for( int i=0; i<params.length; i++ ){
					q.setParameter( i, params[i] );
				}
			}
		}
		
		// If we're meant to populate an EntityOverviewPage
		if( isEntityOverviewPage ){
			return new EntityOverviewPage( q, pageNum, iSelectedItemsPerPage );
		}else{
			return new DeviceResultsPage(q, pageNum, iSelectedItemsPerPage, returnIdsOnly);	
		}		
	}
	
	/**
	 * @return Returns a page of devices
	 */
	public static Page getDevices(String hql, int pageNum, int iSelectedItemsPerPage, Object[] params, boolean isEntityOverviewPage, boolean returnIdsOnly) throws HibernateException 
	{	
		return getDevices( hql, pageNum, iSelectedItemsPerPage, params, null, isEntityOverviewPage, returnIdsOnly );
	}
	
	/**
	 * @return Returns a page of devices
	 */
	public static Page getDevices(String hql, int pageNum, int iSelectedItemsPerPage, boolean isEntityOverviewPage, boolean returnIdsOnly) throws HibernateException 
	{		
		return getDevices( hql, pageNum, iSelectedItemsPerPage, null, null, isEntityOverviewPage, returnIdsOnly );
	}		
	
	/**
	 * Returns all devices that have a dirty status
	 * @return 
	 */
	public static List getDirtyDevices() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT device "
					+ "FROM Device device, Dirty d "
					+ "WHERE device.deviceId = d.dirtyEntityId "
					+ "AND d.dirtyType = '"+ DirtyType.DEVICE.getPersistentValue() + "' "
					+ "ORDER BY UPPER(device.deviceName)";		
		List l = session.createQuery( hql ).list(); 
		return l; 	
	}
	
	/**
	 * Returns a list of devices that are marked for deletion
	 * @return 
	 */
	public static List<Device> getDevicesToDelete() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT device "
					+ "FROM Device device "
					+ "WHERE device.deleted = 1";							
		return session.createQuery( hql ).list();  	
	}	
	
	/**
	 * Returns a list of devices that have multicast enabled
	 * and has a receiveSiteId assigned.
	 * @return 
	 */
	public static List getMulticastDevices() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();			
		List l = session.createCriteria(Device.class)
			.add( Expression.isNotNull("multicastNetwork") )
			.list();		
		return l; 	
	}	
	
	/**
	 * Returns true of false depending whether or not there is a 
	 * dirty object of type Device for this object.
	 * 
	 * @return 
	 */
	public boolean isDirty() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();
		String hql = "SELECT d "
					+ "FROM Dirty d "
					+ "WHERE d.dirtyEntityId = ? "
					+ "AND d.dirtyType = '"+ DirtyType.DEVICE.getPersistentValue() + "'";
		Iterator i = session.createQuery( hql ).setParameter(0, this.getDeviceId()).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}	
	
	/**
	 * Returns true or false depending whether or not there is a 
	 * dirty object of type ContentScheduler for this object
	 * @return 
	 */
	public boolean contentSchedulerRunning() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();
		String hql = "SELECT d "
					+ "FROM Dirty d "
					+ "WHERE d.dirtyEntityId = ? "
					+ "AND d.dirtyType = '"+ DirtyType.CONTENT_SCHEDULER.getPersistentValue() + "'";
		Iterator i = session.createQuery( hql ).setParameter(0, this.getDeviceId()).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}	
		
	/**
	 * Returns a list of all devices that do not have a master device assigned
	 * @return
	 * @throws HibernateException
	 */
	public static List getMasterDevices() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		List l = session.createQuery(
				"SELECT d FROM Device as d "
				+ "WHERE d.mirrorSource IS NULL "
				+ "ORDER BY UPPER(d.deviceName)").list();				
		return l;
	}	
	
	public static List getMasterDevices(boolean masterDevicesOnly) throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT d FROM Device as d ";
		if (masterDevicesOnly) {
			hql += "WHERE d.mirrorSource IS NULL ";
			hql += "AND (d.readableBillingStatus IN ('" + BillingStatusType.PRODUCTION.getName() + "', '" + BillingStatusType.INTERNAL.getName() + "') OR (d.readableBillingStatus = '" + BillingStatusType.STAGING.getName() + "' AND d.createDate >= sysdate - 14) ) ";
		}
		else
			hql += "WHERE (d.readableBillingStatus IN ('" + BillingStatusType.PRODUCTION.getName() + "', '" + BillingStatusType.INTERNAL.getName() + "') OR (d.readableBillingStatus = '" + BillingStatusType.STAGING.getName() + "' AND d.createDate >= sysdate - 14) ) ";
		
		hql += "ORDER BY UPPER(d.deviceName)";
		
		List l = session.createQuery(hql).list();				
		return l;
	}
	
	public static List getMasterDeviceInfos() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List<Object[]> l = session.createQuery(				
				"SELECT d.deviceId, d.deviceName "
				+ "FROM Device as d "	
				+ "WHERE d.mirrorSource IS NULL "
				+ "AND (d.readableBillingStatus IN ('" + BillingStatusType.PRODUCTION.getName() + "', '" + BillingStatusType.INTERNAL.getName() + "') OR (d.readableBillingStatus = '" + BillingStatusType.STAGING.getName() + "' AND d.createDate >= sysdate - 14) ) "
				+ "ORDER BY UPPER(d.deviceName)"
				).list();
		
		LinkedList deviceInfos = new LinkedList();
		
		// Dummy object
		Device d = new Device();
		
		for(Object[] o : l){
			DeviceInfo di = d.new DeviceInfo();
			di.setDeviceId((Long)o[0]);
			di.setDeviceName((String)o[1]);
			deviceInfos.add(di);
		}
		return deviceInfos;
	}
	
	/*
	 * Returns a list of all devices that do not have a master device assigned
	 * and are active.
	 * @return
	 * @throws HibernateException
	 */
	public static List getActiveMasterDevices() throws HibernateException
	{
		String hql = "SELECT d "
			+ "FROM Device as d "	
			+ "WHERE d.mirrorSource IS NULL "
			+ "AND (d.contentUpdateType = ?) "
			+ "ORDER BY UPPER(d.deviceName)";
		Session session = HibernateSession.currentSession();	
		List l = session.createQuery( hql ).setParameter(0, ContentUpdateType.NETWORK.getPersistentValue()).list();				
		return l;
	}		
	
	/**
	 * Returns a list of all devices are candidates to be an edge servers. That is:
	 * 1. not a node device to another edge server
	 * 2. do not have DHCP enabled
	 * 3. and (if applicable) are not the given device.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Device> getPossibleEdgeServers(Device d) throws HibernateException
	{
		String hql = "SELECT d FROM Device as d WHERE ";
		if( d != null ){
			hql += "d.deviceId != :deviceId AND ";
		}
		hql += "(d.dhcpEnabled IS NULL OR d.dhcpEnabled = :dhcpEnabled) AND "
			+ "d.deviceId NOT IN "
			+ "(SELECT d2 FROM Device as d2 WHERE d2.edgeServer IS NOT NULL) "
			+ "AND ( d.osVersion LIKE 'Linux%fc%' OR d.osVersion LIKE 'Linux%ARCH%' )"
			+ "ORDER BY UPPER(d.deviceName)";
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery( hql );
		q.setParameter("dhcpEnabled", Boolean.FALSE);
		if( d != null ){
			q.setParameter("deviceId", d.getDeviceId());
		}
		List<Device> l = q.list();
		return l;
	}	
	
	/**
	 * Returns a list of all devices that are being used as mirror source devices for this device.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Device> getMirrorSourceDevices(Device d) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery(				
				"SELECT ms "
				+ "FROM MirroredDevice as md "
				+ "JOIN md.mirrorSource as ms "
				+ "WHERE md.mirrorPlayer.deviceId = ? "				
				+ "ORDER BY UPPER(ms.deviceName)"						
				);			
		List l = q.setParameter(0, d.getDeviceId()).list();
		return l;
	}		
	
	/**
	 * Returns a list of all MirroredDevice objects that are being used as mirror source devices for this device.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List<MirroredDevice> getMirrorSourceMirroredDevices() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery(				
				"SELECT md "
				+ "FROM MirroredDevice as md "
				+ "JOIN md.mirrorSource as ms "
				+ "WHERE md.mirrorPlayer.deviceId = ? "
				+ "ORDER BY UPPER(ms.deviceName)"
				);			
		List<MirroredDevice> l = q.setParameter(0, this.getDeviceId()).list();
		return l;
	}			

	/**
	 * Returns a list of all devices to which this device is an edge server.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List<Device> getEdgeDevices() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery(				
				"SELECT d "
				+ "FROM Device as d "					
				+ "WHERE d.edgeServer.deviceId = ? "
				+ "AND d.deviceId != ? "
				+ "ORDER BY UPPER(d.deviceName)"						
				);		
		return q.setParameter(0, this.getDeviceId()).setParameter(1, this.getDeviceId()).list();
	}
	
	public List<Device> getOtherEdgeDevices(Device nodeDevice) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery(				
				"SELECT d "
				+ "FROM Device as d "					
				+ "WHERE d.edgeServer.deviceId = :edgeServerId "
				+ "AND d.deviceId NOT IN (:ids) "
				+ "ORDER BY UPPER(d.deviceName)"						
				);
		
		ArrayList ids = new ArrayList();
		ids.add(this.getDeviceId());
		ids.add(nodeDevice.getDeviceId());
		
		return q.setParameter("edgeServerId", this.getDeviceId()).setParameterList("ids", ids).list();
	}
	
	/**
	 * Returns a list of all devices to which this device is an edge server.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List<Long> getEdgeDeviceIds() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery(				
				"SELECT d.deviceId "
				+ "FROM Device as d "					
				+ "WHERE d.edgeServer.deviceId = ? "
				+ "AND d.deviceId != ?"						
				);		
		return q.setParameter(0, this.getDeviceId()).setParameter(1, this.getDeviceId()).list();
	}
	
	/**
	 * Returns a list of all devices to which this device is a mirror source
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List<Device> getMirrorPlayerDevices() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Query q = session.createQuery(				
				"SELECT mp "
				+ "FROM MirroredDevice as md "
				+ "JOIN md.mirrorPlayer as mp "
				+ "WHERE md.mirrorSource.deviceId = ? "
				+ "ORDER BY UPPER(mp.deviceName)"						
				);				
		return q.setParameter(0, this.getDeviceId()).list();
	}
	
	public boolean deviceNeedsContentSchedule() throws HibernateException, NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException, IOException
	{
		boolean result = false;
		
		// If this device's content scheduler status is set to FAILED
		if( this.getContentSchedulerStatus() != null && 
				this.getContentSchedulerStatus().equalsIgnoreCase(ContentSchedulerStatusType.FAILED.toString()) )
		{
			logger.info("The following device has a content scheduler status of \""+ ContentSchedulerStatusType.FAILED.toString() +"\": "+ this.getDeviceName() +". Ignoring device.");
		    return false;
		}			
		
		// If this device has a master device -- ignore
		if( this.getMirrorSource() != null )
		{
			return false;
		}
		
		// If the device is not a network device
		if( this.contentUpdateType.equals(ContentUpdateType.NETWORK.getName()) == false ){
			return false;
		}
		
		// If the billing status of this device is expired
		if( hasValidLicense() == false ){
			return false;
		}
		
		// Another dispatcher might be running a CS for this device
		if(ContentScheduler.isRunning(this)){
			return false;
		}
				
		Calendar now = Calendar.getInstance();
		Calendar dateScheduled = Calendar.getInstance();			
		dateScheduled.setTime( this.getLastScheduledContent() );
		
		// Convert "now" and dateScheduled to device time according to the timezone adjustment
		now.add( Calendar.SECOND, this.getTimezoneAdjustment() );
		//dateScheduled.add( Calendar.SECOND, d.getTimezoneAdjustment() );				
		
		// If the difference between now and lastScheduledContent date is 
		// less than the contentSchedulingHorizon amount
		long diffInSecs = (dateScheduled.getTimeInMillis() - now.getTimeInMillis()) / 1000;		
		long horizonInSecs = this.getContentSchedulingHorizon().longValue();
		if (diffInSecs < horizonInSecs)
		{			
			// Do not run the content scheduler for this device if it is dirty
			if( this.isDirty() )
			{
				// Create a dirty object for this device to notify the interface that a publish is required for this device, if one does not already exist
				List contentSchedulerCouldNotRunList = Dirty.getDirtyEntities( DirtyType.CONTENT_SCHEDULER_COULD_NOT_RUN, this.getDeviceId() );
				if( contentSchedulerCouldNotRunList.size() == 0 )
				{
					Dirty dirty = new Dirty();
					dirty.setDirtyType( DirtyType.CONTENT_SCHEDULER_COULD_NOT_RUN );
					dirty.setDirtyEntityId( this.getDeviceId() );
					dirty.save();
				}						
				logger.info("Unable to run Content Scheduler for Device, \""+ this.getDeviceName() + "\". A Publish is required.");
				return false;
			}						
			
			logger.info("Found device that needs automatic content schedule generation: "+ this.getDeviceName() + " :: now="+ now.getTime() + " :: lastDateScheduled="+this.getLastScheduledContent());
			result = true;				
		}
		return result;
	}
	
	public boolean hasValidLicense(){
		if( this.readableBillingStatus.equals(BillingStatusType.OUT_OF_SERVICE.getName()) ||
				(this.readableBillingStatus.equals(BillingStatusType.STAGING.getName()) && System.currentTimeMillis() - this.createDate.getTime() > 14 * Constants.MILLISECONDS_IN_A_DAY) ){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Determines which devices need content schedules created and creates them
	 *
	 */
	private static Lock generateCSLock = new ReentrantLock();
	public static void generateContentSchedules(Device device) throws HibernateException, NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException, IOException
	{
		boolean launchCS = false;
		if(generateCSLock.tryLock()){
			try {
				// Make this check before determining which devices may need new content
				// If either the content scheduler is running or if a publish is required
				if(ContentScheduler.isCalculatingOrRunningAutomatically() == false){
					// If the current device needs a content scheduler, or if there was no current device passed in
					if( (device != null && device.deviceNeedsContentSchedule()) || device == null ){
						try {
							logger.info("Creating Content Scheduler Calculating row");
							
							// Create a row to denote that the device list is being calculated
							ContentScheduler.makeDirtyForAutoRunCalculating();
							launchCS = true;
						}catch(ConstraintViolationException e){
							// If there is already another auto CS calculation running
							logger.info("Another Content Scheduler is currently calculating. Caught hibernate ConstraintViolationException. Exiting auto-generation of content schedules.");
							launchCS = false;
						}catch(Exception e){
							launchCS = false;
							logger.info(e);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e);
			} finally {
				generateCSLock.unlock();
			}
		}else{
			launchCS = false;
		}
		
		// Return if we can't launch the CS
		if(launchCS == false){
			return;
		}
		
		ArrayList<ContentSchedulerArg> devicesThatNeedContent = new ArrayList<ContentSchedulerArg>();
		try {
			// Check if current device required auto-generation
			if( device != null ){
				devicesThatNeedContent.add( device.loadContentSchedulerArg(true) );
			}
			
			// Check all devices to see if they need a content schedule
			List l = getDevices();
			for( Iterator i = l.iterator(); i.hasNext(); )
			{
				Device d = (Device)i.next();
				if( device == null || d.getDeviceId() != device.getDeviceId() ){
					if( d.deviceNeedsContentSchedule() ){
						devicesThatNeedContent.add( d.loadContentSchedulerArg(true) );
						
						// Only allow 100 devices to run at a time
						if( devicesThatNeedContent.size() >= ContentSchedulerThread.MAX_DEVICES_PER_RUN ){
							break;
						}
					}
				}
			}
			
			// Make rows for each device we are going to run the CS for
			ContentScheduler.makeDirty(devicesThatNeedContent.toArray(new ContentSchedulerArg[0]));
			
			// Set the flag that says the content scheduler is running as a result of automatic content schedule generation
			ContentScheduler.makeDirtyForAutoRunRunning();
		} catch (Exception e) {
			logger.error(e);
			
			// We've encountered an exception, don't launch the CS
			launchCS = false;
			
			// Delete rows for devices
			ContentScheduler.makeNotDirty(devicesThatNeedContent.toArray(new ContentSchedulerArg[0]));
			
			// Delete calculating row
			ContentScheduler.makeNotDirtyForCalculating();
		}
		
		try {
			if(launchCS){
				// Launch the content scheduler from a synchronous method
				launchContentScheduler( devicesThatNeedContent );
			}
		} catch (Exception e) {
			logger.error(e);
			
			// Delete rows for devices
			ContentScheduler.makeNotDirty(devicesThatNeedContent.toArray(new ContentSchedulerArg[0]));
			
			// Delete running row
			ContentScheduler.makeNotDirtyForAutoRun();
		}
	}
	
	/**
	 * Synchronous method used to set the dirty flag and run the content scheduler for the given devices.
	 * 
	 * @param devicesThatNeedContent
	 * @throws IOException
	 */
	public static synchronized void launchContentScheduler(ArrayList<ContentSchedulerArg> devicesThatNeedContent) throws IOException{
		
		if( devicesThatNeedContent.size() > 0 ){
			
			// Convert the array list to an array of ContentSchedulerArgs
			ContentSchedulerArg[] args = new ContentSchedulerArg[ devicesThatNeedContent.size() ];
			for(int i=0; i<args.length; i++){
				args[i] = (ContentSchedulerArg)devicesThatNeedContent.get(i);
			}
			
			ContentScheduler.run( args, null, true, true );
		}
	}
	
	public static void removeAssetAffinity(Asset a) throws HibernateException{
		Session session = HibernateSession.currentSession();
		String hql = "DELETE FROM TargetedAssetMember WHERE asset_id = ?";
		HibernateSession.beginTransaction();
		session.createQuery(hql).setParameter(0, a.getAssetId()).executeUpdate();
		HibernateSession.commitTransaction();
	}
	
	/**
	 * Copies this device and assigns the given new device name and push address.
	 * 
	 * @param newDeviceName
	 * @param newPushAddress
	 * @return
	 */
	public Device copy(String newDeviceName, String newMacAddress) throws HibernateException, CloneNotSupportedException, ClassNotFoundException, InterruptedException, ParseException
	{		
		Session session = HibernateSession.currentSession();
		session.lock( this, LockMode.READ );
		
		// First, create a new device object
		Device newDevice = new Device();
		newDevice.setDeviceName( newDeviceName );
		newDevice.setMacAddr( newMacAddress );		
		newDevice.setContentSchedulingHorizon( this.getContentSchedulingHorizon() );
		newDevice.setAdditionalContentAmount( this.getAdditionalContentAmount() );
		newDevice.setEdgeServer( this.getEdgeServer() );
		newDevice.setMirrorSource( this.getMirrorSource() );
		newDevice.setApplyAlerts( this.getApplyAlerts() );
		newDevice.setContentUpdateType( this.getContentUpdateType() );
		newDevice.setHeartbeatInterval( this.getHeartbeatInterval() );
		newDevice.setDiskCapacity( this.getDiskCapacity() );
		newDevice.setStreamingClientCapable( this.getStreamingClientCapable() );
		newDevice.setStreamingServerCapable( this.getStreamingServerCapable() );
		newDevice.setTimezone( this.getTimezone() );
		newDevice.setDisplay( this.getDisplay() );
		newDevice.setResolution( this.getResolution() );
		newDevice.setRotation( this.getRotation() );
		newDevice.setMulticastNetwork(this.getMulticastNetwork());
		newDevice.setMaxFileStorage( this.getMaxFileStorage() );
		newDevice.setAutoUpdate( this.getAutoUpdate() );
		newDevice.setScalingMode( this.getScalingMode() );
		newDevice.setNetworkInterface( this.getNetworkInterface() );
		newDevice.setDispatcherServers( this.getDispatcherServers() );
		newDevice.setLcdPin( this.getLcdPin() );
		newDevice.setLcdBranding( this.getLcdBranding() );
		newDevice.setIowaitThreshold( this.getIowaitThreshold() );
		newDevice.setCpuThreshold( this.getCpuThreshold() );
		newDevice.setMemoryThreshold( this.getMemoryThreshold() );
		newDevice.setLoadThreshold( this.getLoadThreshold() );
		newDevice.setInitPropertiesFromDevice( this.getInitPropertiesFromDevice() );
		newDevice.setScreenshotUploadTime( this.getScreenshotUploadTime() );
		newDevice.setOutputMode( this.getOutputMode() );
		newDevice.setVolume( this.getVolume() );
		newDevice.setxOffset( this.getxOffset() );
		newDevice.setyOffset( this.getyOffset() );
		newDevice.setZoom( this.getZoom() );
		newDevice.setFilesyncStartTime( this.getFilesyncStartTime() );
		newDevice.setFilesyncEndTime( this.getFilesyncEndTime() );
		newDevice.setAntivirusScan(this.getAntivirusScan());
		newDevice.setDownloadPriority(this.getDownloadPriority());
		newDevice.setAudioNormalization(this.getAudioNormalization());
		newDevice.setVenuePartner(this.getVenuePartner());
		newDevice.setAlphaCompositing(this.getAlphaCompositing());
		newDevice.setBandwidthLimit(this.getBandwidthLimit());
		newDevice.setAudioConnection(this.getAudioConnection());
		newDevice.setDeviceSideScheduling(this.getDeviceSideScheduling());
		newDevice.setFramesync(this.getFramesync());
		newDevice.setCreateDate(new Date());
		newDevice.setType2VideoPlayback(this.getType2VideoPlayback());
		newDevice.setUseChrome(this.getUseChrome());
		newDevice.setChromeDisableGpu(this.getChromeDisableGpu());
		
		// Network Properties
		newDevice.setDhcpEnabled( this.getDhcpEnabled() );
		if(this.getDhcpEnabled() == false){
			newDevice.setGateway( this.getGateway() );
			newDevice.setNetmask( this.getNetmask() );
			newDevice.setDnsServer( this.getDnsServer() );
		}
		
		/*
		 * Set the lastScheduledContent date of the new device to the lastScheduledContent date - cs length
		 */
		Calendar cLastScheduledContent = Calendar.getInstance();
		cLastScheduledContent.setTime( this.getLastScheduledContent() );
		cLastScheduledContent.add( Calendar.SECOND, -(this.getAdditionalContentAmount()) );
		Date lastScheduledContentDate = cLastScheduledContent.getTime();
		Calendar now = Calendar.getInstance();
		
		// Keep going until our lastScheduledContentDate is in the past
		while( lastScheduledContentDate.after( now.getTime() ) )
		{
			// Use lastScheduledContent date - additionalContentAmount
			cLastScheduledContent.add( Calendar.SECOND, -(this.getAdditionalContentAmount()) );
			lastScheduledContentDate = cLastScheduledContent.getTime();
		}
		newDevice.setLastScheduledContent( lastScheduledContentDate );
		
		// Save the device but do not create permission entries since we are going to copy them		
		Long newDeviceId = newDevice.save( false );
		newDevice.copyPermissionEntries( this );
		newDevice.setBillingStatus(BillingStatusType.STAGING.getPersistentValue(), false);
		newDevice.update();
		
		// Create history entries for name and mac_addr
		String username = KmfSession.getKmfSession() != null && KmfSession.getKmfSession().getAppUsername() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		HistoryEntry.logEvent("create", newDevice, newDevice.getDeviceName(), null, "deviceName", SchemaDirectory.getProgram(), username);
		HistoryEntry.logEvent("create", newDevice, newDevice.getMacAddr(), null, "macAddr", SchemaDirectory.getProgram(), username);
		
		// Second, copy all the device group members		
		for( Iterator i = this.getDeviceGrpMembers().iterator(); i.hasNext(); )
		{
			DeviceGrpMember dgm = (DeviceGrpMember)i.next();
			DeviceGrpMember newDeviceGrpMember = new DeviceGrpMember();
			newDeviceGrpMember.setGrp( dgm.getGrp() );
			newDeviceGrpMember.setDevice( newDevice );
			newDeviceGrpMember.save();			
		}
		
		// Third, copy all device schedules associated with this device
		for( Iterator i= this.getDeviceSchedules().iterator(); i.hasNext(); )
		{
			DeviceSchedule ds = (DeviceSchedule)i.next();
			DeviceSchedule newDeviceSchedule = new DeviceSchedule();			
			newDeviceSchedule.setSegment( ds.getSegment() );
			newDeviceSchedule.setDevice( newDevice );
			ds.getSegment().getDeviceSchedules().add(newDeviceSchedule);
			newDeviceSchedule.save();
		}		
		
		// Fourth, copy all Targets associated with this device
		List l = TargetedAssetMember.getStaticTargetedAssetMembers(this);
		for(Iterator<TargetedAssetMember> i=l.iterator();i.hasNext();){
			TargetedAssetMember tam = i.next();
			TargetedAssetMember.create(tam.getTargetedAsset(), tam.getAsset(), newDevice, tam.getSeqNum());
		}
		
		// Fifth, Copy available mirror sources
		List<Device> mirrorSources = getMirrorSourceDevices(this);
		for(Device mirrorSource : mirrorSources){
			MirroredDevice.create(newDevice, mirrorSource);
		}
		
		// Copy any MCM associated with this device
		for( Iterator i=this.getMcms().iterator(); i.hasNext(); )
		{
			// Create a new MCM and associate it with the new device
			Mcm mcm = (Mcm)i.next();
			Mcm newMcm = new Mcm();
			newMcm.setMcmName( mcm.getMcmName() );
			newMcm.setDevice( newDevice );
			newMcm.setMcmHoststring( mcm.getMcmHoststring() );			
			newMcm.setSwitchToAuxCommand( mcm.getSwitchToAuxCommand() );
			newMcm.setSwitchToMediacastCommand( mcm.getSwitchToMediacastCommand() );
			newMcm.setOnCommand( mcm.getOnCommand() );
			newMcm.setOffCommand( mcm.getOffCommand() );
			newMcm.setCurrentPowerCommand( mcm.getCurrentPowerCommand() );
			newMcm.setCurrentVolumeCommand( mcm.getCurrentVolumeCommand() );
			newMcm.setCurrentInputCommand( mcm.getCurrentInputCommand() );
			newMcm.setCurrentBrightnessCommand( mcm.getCurrentBrightnessCommand() );
			newMcm.setCurrentContrastCommand( mcm.getCurrentContrastCommand() );
			newMcm.setSetVolumeCommand( mcm.getSetVolumeCommand() );
			newMcm.setOsdOnCommand( mcm.getOsdOnCommand() );
			newMcm.setOsdOffCommand( mcm.getOsdOffCommand() );
			newMcm.setAutoAdjustCommand( mcm.getAutoAdjustCommand() );
			newMcm.setPeripheralName( mcm.getPeripheralName() );
			newMcm.setSerialPort( mcm.getSerialPort() );
			newMcm.setDiagnosticInterval( mcm.getDiagnosticInterval() );			
			newMcm.setVolumeOffsetCommand( mcm.getVolumeOffsetCommand() );
			newMcm.setMinGainCommand( mcm.getMinGainCommand() );
			newMcm.setMaxGainCommand( mcm.getMaxGainCommand() );
			newMcm.setMuteCommand( mcm.getMuteCommand() );			
			newMcm.setImplementationType( mcm.getImplementationType() );
			newMcm.setSetInversionOnCommand( mcm.getSetInversionOnCommand() );
			newMcm.setSetInversionOffCommand( mcm.getSetInversionOffCommand() );
			newMcm.setSetSignalPatternOnCommand( mcm.getSetSignalPatternOnCommand() );
			newMcm.setSetSignalPatternOffCommand( mcm.getSetSignalPatternOffCommand() );
			newMcm.setSetPixelShiftOnCommand( mcm.getSetPixelShiftOnCommand() );
			newMcm.setSetPixelShiftOffCommand( mcm.getSetPixelShiftOffCommand() );
			newMcm.setPeripheralType( mcm.getPeripheralType() );						
			newMcm.save();		

			// Add a device command for each mcm property that is not blank
			if( newMcm.getMcmHoststring() != null && newMcm.getMcmHoststring().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MCM_HOSTSTRING.getPropertyName() +","+ Reformat.escape(mcm.getMcmHoststring()), true );
			}
			if( newMcm.getSwitchToAuxCommand() != null && newMcm.getSwitchToAuxCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SWITCH_TO_AUX_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSwitchToAuxCommand()), true );
			}
			if( newMcm.getSwitchToMediacastCommand() != null && newMcm.getSwitchToMediacastCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SWITCH_TO_MEDIACAST_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSwitchToMediacastCommand()), true );
			}
			if( newMcm.getOnCommand() != null && newMcm.getOnCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.ON_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getOnCommand()), true );
			}
			if( newMcm.getOffCommand() != null && newMcm.getOffCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.OFF_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getOffCommand()), true );
			}
			if( newMcm.getCurrentPowerCommand() != null && newMcm.getCurrentPowerCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getCurrentPowerCommand()), true );
			}
			if( newMcm.getCurrentVolumeCommand() != null && newMcm.getCurrentVolumeCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.CURRENT_VOLUME_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getCurrentVolumeCommand()), true );
			}
			if( newMcm.getCurrentInputCommand() != null && newMcm.getCurrentInputCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getCurrentInputCommand()), true );
			}
			if( newMcm.getCurrentBrightnessCommand() != null && newMcm.getCurrentBrightnessCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getCurrentBrightnessCommand()), true );
			}
			if( newMcm.getCurrentContrastCommand() != null && newMcm.getCurrentContrastCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.CURRENT_CONTRAST_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getCurrentContrastCommand()), true );
			}
			if( newMcm.getSetVolumeCommand() != null && newMcm.getSetVolumeCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_VOLUME_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetVolumeCommand()), true );
			}
			if( newMcm.getOsdOnCommand() != null && newMcm.getOsdOnCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.OSD_ON_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getOsdOnCommand()), true );
			}
			if( newMcm.getOsdOffCommand() != null && newMcm.getOsdOffCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.OSD_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getOsdOffCommand()), true );
			}
			if( newMcm.getAutoAdjustCommand() != null && newMcm.getAutoAdjustCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.AUTO_ADJUST_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getAutoAdjustCommand()), true );
			}
			if( newMcm.getPeripheralName() != null && newMcm.getPeripheralName().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MCM_PERIPHERAL_NAME.getPropertyName() +","+ Reformat.escape(mcm.getPeripheralName()), true );
			}
			if( newMcm.getSerialPort() != null && newMcm.getSerialPort().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SERIAL_PORT.getPropertyName() +","+ Reformat.escape(mcm.getSerialPort()), true );
			}
			if( newMcm.getDiagnosticInterval() != null && newMcm.getDiagnosticInterval().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.DIAGNOSTIC_INTERVAL.getPropertyName() +","+ Reformat.escape(mcm.getDiagnosticInterval()), true );
			}	
			if( newMcm.getVolumeOffsetCommand() != null && newMcm.getVolumeOffsetCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.VOLUME_OFFSET_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getVolumeOffsetCommand()), true );
			}	
			if( newMcm.getMinGainCommand() != null && newMcm.getMinGainCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MIN_GAIN_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getMinGainCommand()), true );
			}	
			if( newMcm.getMaxGainCommand() != null && newMcm.getMaxGainCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MAX_GAIN_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getMaxGainCommand()), true );
			}	
			if( newMcm.getMuteCommand() != null && newMcm.getMuteCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MUTE_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getMuteCommand()), true );
			}	
			if( newMcm.getImplementationType() != null ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MCM_IMPLEMENTATION_TYPE.getPropertyName() +","+ Reformat.escape(mcm.getImplementationType().getPersistentValue()), true );
			}	
			if( newMcm.getSetInversionOnCommand() != null && newMcm.getSetInversionOnCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_INVERSION_ON_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetInversionOnCommand()), true );
			}	
			if( newMcm.getSetInversionOffCommand() != null && newMcm.getSetInversionOffCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_INVERSION_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetInversionOffCommand()), true );
			}	
			if( newMcm.getSetSignalPatternOnCommand() != null && newMcm.getSetSignalPatternOnCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_SIGNAL_PATTERN_ON_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetSignalPatternOnCommand()), true );
			}	
			if( newMcm.getSetSignalPatternOffCommand() != null && newMcm.getSetSignalPatternOffCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_SIGNAL_PATTERN_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetSignalPatternOffCommand()), true );
			}	
			if( newMcm.getSetPixelShiftOnCommand() != null && newMcm.getSetPixelShiftOnCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_PIXEL_SHIFT_ON_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetPixelShiftOnCommand()), true );
			}	
			if( newMcm.getSetPixelShiftOffCommand() != null && newMcm.getSetPixelShiftOffCommand().length() > 0 ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.SET_PIXEL_SHIFT_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(mcm.getSetPixelShiftOffCommand()), true );
			}	
			if( newMcm.getPeripheralType() != null ){
				newDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, newMcm.getMcmId().toString() +","+ DevicePropertyType.MCM_PERIPHERAL_TYPE.getPropertyName() +","+ Reformat.escape(mcm.getPeripheralType().getPersistentValue()), true );
			}				
		}			
		
		// Copy any device metadata
		this.copyMetadata( newDevice.getDeviceId() );
		
		// Copy any ContentRotationTargets associated with this device
		for( Iterator i=this.getContentRotationTargets().iterator(); i.hasNext(); )
		{
			// Create a new ContentRotationTarget and associate it with the new device
			ContentRotationTarget crt = (ContentRotationTarget)i.next();			
			ContentRotationTarget.create( crt.getPlaylistContentRotation().getContentRotation(), crt.getPlaylistContentRotation().getPlaylist(), crt.getPlaylistContentRotation().getDisplayarea(), newDevice );					
		}	
		
		// Copy any AlertDevices associated with this device		
		for( Iterator i=this.getAlertDevices().iterator(); i.hasNext(); )
		{
			// Create a new AlertDevice and associate it with the new device
			AlertDevice alertDevice = (AlertDevice)i.next();			
			AlertDevice.create( alertDevice.getAlert(), newDevice );					
		}
		
		return newDevice;
	}
	
	public Map<Long, DeviceSchedule> getDeviceSchedules(SegmentType type, Date startDatetime) {
		Map<Long, DeviceSchedule> result = new HashMap<Long, DeviceSchedule>();
		Iterator i = this.getDeviceSchedules().iterator();
		while( i.hasNext() ) {
			DeviceSchedule ds = (DeviceSchedule)i.next();
			if( ds.getSegment().getType().equals(type) && ds.getSegment().isExpired(startDatetime) == false ) {
				result.put(ds.getDeviceScheduleId(), ds);
			}
		}
		return result;
	}
	

	public Device repurpose(String newDeviceName){
		String macAddr = this.macAddr;
		String newMacAddr = macAddr + "-R";
		
		// Get the number of times this device has been repurposed
		List<Device> repurposedDevices = getRepurposedDevices(macAddr);
		if(repurposedDevices.size() == 0){
			newMacAddr += "1";
		}else{
			int count = Integer.parseInt(repurposedDevices.get(0).getMacAddr().split("-R")[1]);
			newMacAddr += ++count;
		}
		
		// Update the current devices mac addr
		this.setMacAddr(newMacAddr);
		this.update();
		
		Device newDevice = null;
		
		try {
			newDevice = this.copy(newDeviceName, macAddr);
			newDevice.setContentUpdateType(ContentUpdateType.NETWORK.getPersistentValue());
			newDevice.setApplyAlerts( Boolean.TRUE );
			newDevice.update();
		} catch (Exception e) {
			logger.error(e);
		}
		
		return newDevice;
	}
	
	public Device replace(String newDeviceName, String newMacAddr){
		Device newDevice = null;
		
		try {
			newDevice = this.copy(newDeviceName, newMacAddr);
			newDevice.setContentUpdateType(ContentUpdateType.NETWORK.getPersistentValue());
			
			this.setReplacedBy(newDevice);
			this.update();
			
			// If this device has a billing start date
			if(this.getReadableBillableStartDt() != null){
				newDevice.setBillingStatus(BillingStatusType.PRODUCTION.getPersistentValue(), false);
				newDevice.setBillableStartDt(this.getReadableBillableStartDt());
				newDevice.update();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		return newDevice;
	}
	
	/**
	 * 
	 * @return
	 */
	public Set getPlaylistsToUpdate(Date startDatetime)
	{
	    Set playlistsToUpdate = new HashSet();
	    Set deviceSchedules = this.getDeviceSchedules();
	    for( Iterator it=deviceSchedules.iterator(); it.hasNext(); )
	    {
	        DeviceSchedule deviceSchedule = (DeviceSchedule) it.next();
	        Segment segment = deviceSchedule.getSegment();
	        
	        if(segment.isExpired(startDatetime) == false){
	        	List segmentParts = segment.getSegmentParts();
		        for( Iterator it2=segmentParts.iterator(); it2.hasNext(); )
		        {
		            SegmentPart segmentPart = (SegmentPart) it2.next();
		            if( segmentPart instanceof PlaylistSegmentPart )
		            {
		                PlaylistSegmentPart playlistSegmentPart = (PlaylistSegmentPart) segmentPart;
		                Playlist playlist = playlistSegmentPart.getPlaylist();
	                    playlistsToUpdate.add( playlist );
		            }
		        }
		        
		        if(segment.getAssetIntervalPlaylist() != null){
		        	playlistsToUpdate.add(segment.getAssetIntervalPlaylist());
		        }
	        }
	    }
	    return playlistsToUpdate;
	}
	
	public Set getPlaylistsToUpdate(Date startDatetime, Date endDatetime) throws HibernateException, Exception
	{
		Set playlistsToUpdate = new HashSet();
		Set<Long> airingSegments = new HashSet<Long>();
		ScheduleInfo si = ScheduleInfo.getScheduleInfo( this, startDatetime, endDatetime, false );
		SegmentBlock[] segmentBlocks =  si.getSegmentBlocks();
		for(SegmentBlock sb : segmentBlocks){
			airingSegments.add(sb.getSegment().getSegmentId());
		}
		
		for(DeviceSchedule ds : this.getDeviceSchedules()){
			if(airingSegments.contains(ds.getSegment().getSegmentId())){
				Segment segment = ds.getSegment();
				if(segment.isExpired(startDatetime) == false){
		        	List segmentParts = segment.getSegmentParts();
			        for( Iterator it2=segmentParts.iterator(); it2.hasNext(); )
			        {
			            SegmentPart segmentPart = (SegmentPart) it2.next();
			            if( segmentPart instanceof PlaylistSegmentPart )
			            {
			                PlaylistSegmentPart playlistSegmentPart = (PlaylistSegmentPart) segmentPart;
			                Playlist playlist = playlistSegmentPart.getPlaylist();
		                    playlistsToUpdate.add( playlist );
			            }
			        }
			        
			        if(segment.getAssetIntervalPlaylist() != null){
			        	playlistsToUpdate.add(segment.getAssetIntervalPlaylist());
			        }
		        }
			}
		}
		return playlistsToUpdate;
	}
	
	public float floatVersion() {
		float result = 0;
		try {
			String version = this.getVersion();
			if( version != null && version.contains(".") ) {
				int dot2 = -1;
				int dot = version.indexOf(".");
				String subVersion = version.substring(dot+1);
				if( subVersion.contains(".") ) {
					dot2 = version.indexOf(".", dot+1);
				}
				if( dot2>0 ) {
					version = version.substring(0, dot2);
				}
			}
			result = Float.parseFloat(version);
		} catch(Exception e) {}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public HashSet<ContentRotation> getContentRotationsToUpdate(HashSet<Playlist> playlists)
	{
		HashSet<ContentRotation> contentRotationsToUpdate = new HashSet<ContentRotation>();
		
		/*
		 * Convert the list of playlists to a list of playlistIds so we can make one single query
		 * to retrieve all playlistContentRotations associated with all playlist that are scheduled to this device
		 */
		ArrayList<Long> playlistIds = new ArrayList<Long>();
		for( Iterator<Playlist> i=playlists.iterator(); i.hasNext(); ){
			playlistIds.add( i.next().getPlaylistId() );
		}
		
		/*
		 * Get a list of all playlistContentRotations associated with each playlist that is scheduled to this device,
		 * and add their content rotations to the list of contentRotationsToUpdate
		 */
		if( playlistIds.size() > 0 ){
			List<PlaylistContentRotation> playlistContentRotations = PlaylistContentRotation.getPlaylistContentRotations( playlistIds );
			for( Iterator<PlaylistContentRotation> i=playlistContentRotations.iterator(); i.hasNext(); )
			{
				contentRotationsToUpdate.add( i.next().getContentRotation() );
			}
		}
				
		// Add each content rotation that has a target to this device to the list of contentRotationsToUpdate
		for( Iterator<ContentRotationTarget> i=this.getContentRotationTargets().iterator(); i.hasNext(); )
		{
			contentRotationsToUpdate.add( i.next().getPlaylistContentRotation().getContentRotation() );							
		}			
	    return contentRotationsToUpdate;
	}	
	
	/**
	 * Determines whether or not the content currently on this device
	 * is out of date by seeing if the given segment is scheduled
	 * onto the device after the current time
	 * 
	 * @return
	 */
	public boolean contentOutOfDate(Segment s) throws HibernateException
	{
		// Query the content_schedule_event table and see if the given
		// segment is scheduled onto this device after the current time
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT cse "
				+ "FROM ContentScheduleEvent as cse "
				+ "WHERE cse.deviceId = ? "
				+ "AND cse.segmentId = ? "
				+ "AND cse.startDatetime > sysdate";
		Iterator i = session.createQuery( hql ).setParameter(0, this.getDeviceId()).setParameter(1, s.getSegmentId()).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}
	
	/**
	 * 
	 *
	 */
	public void makeDirty() throws HibernateException
	{		
		// If there is not a dirty object for this object		
		Dirty d = Dirty.getDirty( this.getEntityId() );		
		if(d == null)
		{		
			// Create a new dirty object
			d = new Dirty();			
			d.setDirtyEntityId( this.getEntityId() );
			d.setDirtyType( DirtyType.DEVICE );
			d.save();
		}
	}
	
	/**
	 * Make dirty any devices that contain the given segment.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void makeDirty(Segment s) throws HibernateException
	{					
		makeDirty(Device.getDeviceIds(s));
	}
	
	/**
	 * Make dirty any devices that contain the given asset.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void makeDirty(Asset a) throws HibernateException
	{				
		List deviceIds = new ArrayList();
		for( Iterator i = Device.getDevices( a, null ).iterator(); i.hasNext(); ){
			Device d = (Device)i.next();
			deviceIds.add(d.getDeviceId());
		}
		makeDirty(deviceIds);
	}
	
	/**
	 * Make dirty any devices that contain the given playlist.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void makeDirty(Playlist p) throws HibernateException
	{		
		makeDirty(Device.getDeviceIds(p));	
	}	
	
	public static void makeDirty(List<Long> deviceIds){
		List<Long> dirtyDeviceIds = Dirty.getDirtyEntityIds(DirtyType.DEVICE);
		
		// For each device that needs to be made dirty
		for(Long deviceId : deviceIds){
			// Make a dirty object if one doesn't already exist
			if(dirtyDeviceIds.contains(deviceId) == false){
				// Create a new dirty object
				Dirty d = new Dirty();			
				d.setDirtyEntityId( deviceId );
				d.setDirtyType( DirtyType.DEVICE );
				d.save();
			}
		}
	}
	
	/**
	 * Returns true if any segments that are scheduled to this device are dirty,
	 * otherwise returns false.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public boolean hasDirtySegments() throws HibernateException
	{						
		// Get all dirty segments that are scheduled to this device
		String hql = "SELECT dirty "
			+ "FROM Dirty dirty "
			+ "WHERE dirty.dirtyEntityId IN "
			+ "(SELECT ds.segment.segmentId FROM DeviceSchedule ds "
			+ "WHERE ds.device.deviceId = ?)";
		Session session = HibernateSession.currentSession();	
		Iterator i = session.createQuery( hql ).setParameter(0,this.getDeviceId()).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}	
	
	/**
	 * Returns true if any playlists that are scheduled to this device are dirty,
	 * otherwise returns false.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public boolean hasDirtyPlaylists() throws HibernateException
	{						
		// Get all dirty playlists that are scheduled to this device
		String hql = "SELECT dirty "
			+ "FROM Dirty dirty "
			+ "WHERE dirty.dirtyEntityId IN "
			+ 	"(SELECT DISTINCT psp.playlist.playlistId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"WHERE psp.segment.segmentId IN "
			+ 		"(SELECT ds.segment.segmentId FROM DeviceSchedule ds "
			+ 		"WHERE ds.device.deviceId = ?))";
		Session session = HibernateSession.currentSession();	
		Iterator i = session.createQuery( hql ).setParameter(0,this.getDeviceId()).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}		
	
	/**
	 * Returns true if any assets are scheduled to this device are dirty,
	 * otherwise returns false.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public boolean hasDirtyAssets() throws HibernateException
	{						
		/*
		 * Get all assets that are scheduled to this device
		 */
		String hql = "SELECT dirty "
			+ "FROM Dirty dirty "
			+ "WHERE dirty.dirtyEntityId IN "		
			+ "(SELECT asset.assetId FROM Asset asset WHERE asset.assetId IN "
				// Get all playlist assets scheduled to this device
			+ 	"(SELECT DISTINCT playlistAsset.asset.assetId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"JOIN psp.segment.deviceSchedules as ds "
			+ 	"JOIN ds.device as device "
			+ 	"JOIN psp.playlist.playlistAssets as playlistAsset "						
			+ 	"WHERE device.deviceId = ?) "			
			+ 	"OR asset.assetId IN "
				// Get all content rotation assets scheduled to this device
			+ 	"(SELECT contentRotationAsset.asset.assetId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"JOIN psp.segment.deviceSchedules as ds "
			+ 	"JOIN ds.device as device "			
			+ 	"JOIN psp.playlist.playlistContentRotations as playlistContentRotation "
			+ 	"JOIN playlistContentRotation.contentRotation as contentRotation "
			+ 	"JOIN contentRotation.contentRotationAssets as contentRotationAsset "			
			+ 	"WHERE device.deviceId = ?) "			
			+ 	"OR asset.assetId IN "
				// Get all paired assets scheduled to this device
			+ 	"(SELECT pairedAsset.asset.assetId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"JOIN psp.segment.deviceSchedules as ds "
			+ 	"JOIN ds.device as device "		
			+	"JOIN psp.playlist.playlistAssets as playlistAsset "
			+ 	"JOIN playlistAsset.assetPresentation.pairedDisplayareas as pairedDisplayarea "
			+ 	"JOIN pairedDisplayarea.pairedAssets as pairedAsset "
			+ 	"WHERE device.deviceId = ?)"	
			+ 	"OR asset.assetId IN "			
			// Get all asset segment parts scheduled to this device
			+ 	"(SELECT asp.asset.assetId "
			+ 	"FROM AssetSegmentPart asp "
			+	"JOIN asp.segment.deviceSchedules as ds "
			+	"JOIN ds.device as device "
			+ 	"WHERE device.deviceId = ?))";	
		Session session = HibernateSession.currentSession();	
		Iterator i = session.createQuery( hql )
			.setParameter(0,this.getDeviceId())
			.setParameter(1,this.getDeviceId())
			.setParameter(2,this.getDeviceId())
			.setParameter(3,this.getDeviceId()).iterate();				
		boolean result = i.hasNext() ? true : false;
		Hibernate.close( i );
		return result;
	}	
	
	public List<Long> getDirtyAssetsIds() throws HibernateException
	{
		String hql = "SELECT dirtyId "
				+ "FROM Dirty dirty "
				+ "WHERE dirty.dirtyEntityId IN "		
				+ "(SELECT asset.assetId FROM Asset asset WHERE asset.assetId IN "
					// Get all playlist assets scheduled to this device
				+ 	"(SELECT DISTINCT playlistAsset.asset.assetId "
				+ 	"FROM PlaylistSegmentPart psp "
				+ 	"JOIN psp.segment.deviceSchedules as ds "
				+ 	"JOIN ds.device as device "
				+ 	"JOIN psp.playlist.playlistAssets as playlistAsset "						
				+ 	"WHERE device.deviceId = ?) "			
				+ 	"OR asset.assetId IN "
					// Get all content rotation assets scheduled to this device
				+ 	"(SELECT contentRotationAsset.asset.assetId "
				+ 	"FROM PlaylistSegmentPart psp "
				+ 	"JOIN psp.segment.deviceSchedules as ds "
				+ 	"JOIN ds.device as device "			
				+ 	"JOIN psp.playlist.playlistContentRotations as playlistContentRotation "
				+ 	"JOIN playlistContentRotation.contentRotation as contentRotation "
				+ 	"JOIN contentRotation.contentRotationAssets as contentRotationAsset "			
				+ 	"WHERE device.deviceId = ?) "			
				+ 	"OR asset.assetId IN "
					// Get all paired assets scheduled to this device
				+ 	"(SELECT pairedAsset.asset.assetId "
				+ 	"FROM PlaylistSegmentPart psp "
				+ 	"JOIN psp.segment.deviceSchedules as ds "
				+ 	"JOIN ds.device as device "		
				+	"JOIN psp.playlist.playlistAssets as playlistAsset "
				+ 	"JOIN playlistAsset.assetPresentation.pairedDisplayareas as pairedDisplayarea "
				+ 	"JOIN pairedDisplayarea.pairedAssets as pairedAsset "
				+ 	"WHERE device.deviceId = ?)"	
				+ 	"OR asset.assetId IN "			
				// Get all asset segment parts scheduled to this device
				+ 	"(SELECT asp.asset.assetId "
				+ 	"FROM AssetSegmentPart asp "
				+	"JOIN asp.segment.deviceSchedules as ds "
				+	"JOIN ds.device as device "
				+ 	"WHERE device.deviceId = ?))";
		
		Session session = HibernateSession.currentSession();	
		List<Long> l = session.createQuery( hql )
			.setParameter(0,this.getDeviceId())
			.setParameter(1,this.getDeviceId())
			.setParameter(2,this.getDeviceId())
			.setParameter(3,this.getDeviceId()).list();
		
		return l;
	}
	
	public void updatePlayLists(Date startDate) throws Exception
	{
		Set<Playlist> playlists = getPlaylistsToUpdate(startDate);
		for( Iterator<Playlist> it=playlists.iterator(); it.hasNext(); )
	    {
			Playlist playlist = it.next();
			playlist.updatePlaylist(null);
			
			Set<PlaylistContentRotation> pcrs = playlist.getPlaylistContentRotations();
			for( Iterator<PlaylistContentRotation> pcrit=pcrs.iterator(); pcrit.hasNext(); ) {
				PlaylistContentRotation pcr = pcrit.next();
				pcr.getContentRotation().updateContentRotation( pcr.getPlaylist(), null, true );
			}
	    }
	}
	
	/**
	 * Returns a list of assets of the given assetTypes that are scheduled to this device.
	 * 
	 * @return
	 * @throws HibernateException
	 */
 	public List getScheduledAssets(List assetTypes) throws HibernateException
	{												
		// Build the string of asset types		
		String strAssetTypes = "";
		for( Iterator i=assetTypes.iterator(); i.hasNext(); )
		{
			AssetType assetType = (AssetType)i.next();
			if( strAssetTypes.length() > 0 ){
				strAssetTypes += ", ";					
			}
			String assetTypeClassName = assetType.getPartialPersistentValue( false ); 
			strAssetTypes += "'"+ assetTypeClassName +"'";
		}	
		
		/*
		 * Get all assets of the given assetTypes that are scheduled to this device
		 */		
		String hql = "SELECT asset FROM Asset asset "
			+ "WHERE asset.class IN ("+ strAssetTypes +") "
			+ "AND asset.assetId IN "
				// Get all playlist assets scheduled to this device
			+ 	"(SELECT DISTINCT playlistAsset.asset.assetId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"JOIN psp.segment.deviceSchedules as ds "
			+ 	"JOIN ds.device as device "
			+ 	"JOIN psp.playlist.playlistAssets as playlistAsset "						
			+ 	"WHERE device.deviceId = ?) "			
			+ 	"OR asset.assetId IN "
				// Get all content rotation assets scheduled to this device
			+ 	"(SELECT contentRotationAsset.asset.assetId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"JOIN psp.segment.deviceSchedules as ds "
			+ 	"JOIN ds.device as device "			
			+ 	"JOIN psp.playlist.playlistContentRotations as playlistContentRotation "
			+ 	"JOIN playlistContentRotation.contentRotation as contentRotation "
			+ 	"JOIN contentRotation.contentRotationAssets as contentRotationAsset "			
			+ 	"WHERE device.deviceId = ?) "			
			+ 	"OR asset.assetId IN "
				// Get all paired assets scheduled to this device
			+ 	"(SELECT pairedAsset.asset.assetId "
			+ 	"FROM PlaylistSegmentPart psp "
			+ 	"JOIN psp.segment.deviceSchedules as ds "
			+ 	"JOIN ds.device as device "		
			+	"JOIN psp.playlist.playlistAssets as playlistAsset "
			+ 	"JOIN playlistAsset.assetPresentation.pairedDisplayareas as pairedDisplayarea "
			+ 	"JOIN pairedDisplayarea.pairedAssets as pairedAsset "
			+ 	"WHERE device.deviceId = ?)"	
			+ 	"OR asset.assetId IN "			
			// Get all asset segment parts scheduled to this device
			+ 	"(SELECT asp.asset.assetId "
			+ 	"FROM AssetSegmentPart asp "
			+	"JOIN asp.segment.deviceSchedules as ds "
			+	"JOIN ds.device as device "
			+ 	"WHERE device.deviceId = ?)";	
		Session session = HibernateSession.currentSession();	
		return session.createQuery( hql ).setParameter(0,this.getDeviceId()).setParameter(1,this.getDeviceId()).setParameter(2,this.getDeviceId()).setParameter(3,this.getDeviceId()).list();
	}	

	/**
	 * Recursive method that returns a set of unique devices based on the edge server and master/slave
	 * characteristics of each device. 
	 * This method is called when determining which DeviceCommands should be added for a particular device
	 * as well as when determining which FileTransmission records should be created given a particular device. 
	 * 
	 * @param cmd
	 * @param originalDevice
	 * @param result
	 * @param ignoreEdgeServer
	 * @return
	 */
	public synchronized HashMap<Long, Device> getDevicesRecursively(DeviceCommandType cmd, Device originalDevice, HashMap<Long, Device> result, boolean ignoreEdgeServer)
	{
		// If we've already added this device to the collection of devices for which to add this device command -- return
		if( result.get(this.getDeviceId()) != null ){
			return result;
		}

		// If this device has an edge server, and we're executing one of the file download device commands
		// and we're not trying to issue the command to the edge device (and therefore want to ignore the edge server property)  
		if( this.getEdgeServer() != null && (cmd == null || cmd.isFileTransferCommand()) && ignoreEdgeServer == false ) 
		{
			// Issue the device command to the edge server only
			result.put( this.getEdgeServer().getDeviceId(), this.getEdgeServer() );
		}
		else
		{
			// Add this device to the collection of devices for which to add this device command
			result.put( this.getDeviceId(), this );
			
			// Also add the device command to any "slave" devices if the original device this function was called for was a Master
			// and if the device command is a "content related" command (getContentSchedule or getPresentation)
			if( this.getDeviceId() == originalDevice.getDeviceId() 
					&& (cmd == null 
							|| cmd.getPersistentValue().equalsIgnoreCase( DeviceCommandType.GET_CONTENT_SCHEDULE.getPersistentValue() )
							|| cmd.getPersistentValue().equalsIgnoreCase( DeviceCommandType.GET_PRESENTATION.getPersistentValue() )) )
			{
				List<Device> slaveDevices = this.getMirrorPlayerDevices();
				for( Iterator<Device> i=slaveDevices.iterator(); i.hasNext(); ) {
					Device slaveDevice = i.next();
					if(slaveDevice.getContentUpdateType().equals(ContentUpdateType.NETWORK.getPersistentValue())){
						slaveDevice.getDevicesRecursively(cmd, originalDevice, result, ignoreEdgeServer);
					}
				}
			}			
		}		
		return result;
	}
	
	/**
	 * Helper method to call addDeviceCommand in it's most generic way
	 * @param cmd
	 * @param params
	 * @param wait
	 * @throws HibernateException
	 * @throws InterruptedException
	 */
	public List<DeviceCommand> addDeviceCommand(DeviceCommandType cmd, String params) throws HibernateException, InterruptedException{
		return addDeviceCommand( cmd, params, true, false, true, null );
	}
	
	public List<DeviceCommand> addDeviceCommand(DeviceCommandType cmd, String params, boolean wait) throws HibernateException, InterruptedException{
		return addDeviceCommand( cmd, params, true, false, true, null );
	}
	
	public List<DeviceCommand> addDeviceCommand(DeviceCommandType cmd, String params, boolean attemptPush, boolean ignoreEdgeServer, boolean updateBytesToDownload, FileTransmissionType fileTransmissionType) throws HibernateException, InterruptedException{
		return addDeviceCommand(cmd, params, attemptPush, ignoreEdgeServer, updateBytesToDownload, fileTransmissionType, false);
	}
	
	/**
	 * First, attempts to push the given DeviceCommand to the device. If it is
	 * not successful in connecting to the device, it creates and saves a 
	 * deviceCommand object for this device.
	 * 
	 * @param cmd The command to add
	 */
	public List<DeviceCommand> addDeviceCommand(DeviceCommandType cmd, String params, boolean attemptPush, boolean ignoreEdgeServer, boolean updateBytesToDownload, FileTransmissionType fileTransmissionType, boolean createNow) throws HibernateException, InterruptedException
	{
		List<DeviceCommand> result = new LinkedList<DeviceCommand>();
		HashMap<Long, Device> devicesForWhichToAddDeviceCommands = getDevicesRecursively( cmd, this, new HashMap<Long, Device>(), ignoreEdgeServer );
		
		for( Iterator<Device> i=devicesForWhichToAddDeviceCommands.values().iterator(); i.hasNext(); )
		{
			Device device = i.next();
			logger.info("Adding device command: "+ device.getDeviceId() +" - "+ cmd.toString());
			
			/*
			 * We do not want to create the file transmission rows within the DeviceCommandThread
			 * because when creating the file transmission rows, bytesToUpdate may need to be updated for this device, 
			 * which if executed within the thread could throw a hibernate exception "Illegal attempt to associate a collection with two open sessions".
			 */
			// If we're executing one of the file download commands
			if( cmd.isFileTransferCommand() )
			{
				// Create a new file_rransmission row for the edge server
				if( fileTransmissionType == null ) {
					if( cmd.getPersistentValue().equalsIgnoreCase( DeviceCommandType.GET_CONTENT_SCHEDULE.getPersistentValue() ) ){
						fileTransmissionType = FileTransmissionType.CONTENT_SCHEDULE;
					}else if(params.startsWith(DispatcherConstants.PRESENTATIONS_DIRECTORY)){
						fileTransmissionType = FileTransmissionType.PRESENTATION; 
					}else if(params.startsWith(DispatcherConstants.DEVICE_SCRIPTS_DIRECTORY)){
						fileTransmissionType = FileTransmissionType.DEVICE_SCRIPT;
					}
				}
				
				// Strip out the priority if any
				String filename = params.contains("&priority") ? params.substring(0, params.indexOf("&priority")) : params;
				
				// Create a file transmission row
				FileTransmission.createOrUpdate( device, filename, FileTransmissionStatus.NEEDED, fileTransmissionType, new Date(), null, updateBytesToDownload );
			}
			
			if(createNow){
				result.add(DeviceCommand.create(device, cmd, params, attemptPush));
			}else{
				// Add this command to the creator queue
				DeviceCommandCreator.addCommandToQueue(device.getDeviceId(), cmd, params, attemptPush);
			}
		}
		
		return result;
	}
	
	/**
	 * Adds a MCM command for each MCM associated with this device
	 *  
	 * @param cmd The command to add
	 */
	public void addMcmCommand(McmCommandType cmd, String parameter, boolean attemptPush, boolean createNow) throws HibernateException, InterruptedException
	{	
		// For each MCM associated with this device
		for( Iterator i = this.getMcms().iterator(); i.hasNext(); )
		{
			Mcm mcm = (Mcm)i.next();
			PeripheralType[] pt = cmd.getPeripheralTypes();
			
			// Add the mcm command only if its valid for the selected mcm.PeripheralType
			for(int j=0; j<pt.length; j++){
				if( mcm.getPeripheralType().equals(pt[j]) ){
					mcm.addMcmCommand( cmd, parameter, attemptPush, createNow );
					break;
				}
			}
		}		
	}	
	
	/**
	 * Rebuilds the presentation file associated with the given asset.
	 * Then issues the getPresentation() device command for the new presentation.
	 * 
	 * @param asset
	 */
	public void addGetPresentationDeviceCommand(Asset asset, boolean ignoreEdgeServer, Integer priority) 
	{	
		try
		{
			/*
			 * We can't add getPresentation commands for targeted assets since there is no way
			 * to determine the underlying asset for this TA. That code resides in the CS and
			 * this issue will be resolved in IT #3162.
			 */
			if(asset.getAssetType().equals(AssetType.TARGETED_ASSET) == false && asset.getAssetType().equals(AssetType.AD_SERVER) == false){
				/*
				 * Rebuild presentation file.
				 * If the presentation is of type WebappPresentation or HtmlPresentation, we need to call the alternative constructor.
				 */
				Presentation presentation = null;
				if( asset.getPresentationType().equals( WebappPresentation.class.getName() ) )
				{
					presentation = new WebappPresentation( (Webapp)asset, this );
					presentation.xmlEncode();			
				}	
				else if( asset.getPresentationType().equals( HtmlPresentation.class.getName() ) )
				{
					presentation = new HtmlPresentation( (Html)asset, this );
					presentation.xmlEncode();			
				}				
				else
				{
			        Class<?> cls = Class.forName( asset.getPresentationType() );
			        Class<?> partypes[] = new Class[1];
			        partypes[0] = asset.getClass();
			        Constructor<?> ct = cls.getConstructor(partypes);
			        Object arglist[] = new Object[1];
			        arglist[0] = asset;
			        presentation = (Presentation) ct.newInstance(arglist);
			        presentation.xmlEncode();			
				}
				
				// Add the getPresentation() device command for this presentation
				String parameters = DeviceCommand.addPriority(this, priority, Constants.PRESENTATIONS +"/"+ presentation.getRelativePathname());
				this.addDeviceCommand( DeviceCommandType.GET_PRESENTATION, parameters, true, ignoreEdgeServer, true, null );
			}
		}
		catch(Exception e){
			logger.error("An unexpected error occured while adding getPresentation device command.", e);
		}
	}	
	
	/**
	 * Scan the schedules directory and add a device command to get
	 * the latest content schedule for this device
	 * Adds a getContentSchedule device command to the given device and contentScheduleFileName
	 */
	public String reissueContentScheduleDeviceCommand(Device device, boolean attemptPush, boolean createNow) throws ParseException, InterruptedException
	{
		String mostRecentContentScheduleFileName = null;
		String result = null;
		
		// If a device was passed in -- find its most recent content schedule		
		if( device != null ){
			mostRecentContentScheduleFileName = device.getLastContentScheduleFileName();
		}
		else
		{
			// If this device has a master, find the master's most recent content schedule
			// Otherwise, find this device's most recent content schedule
			mostRecentContentScheduleFileName = this.getMirrorSource() != null ? this.getMirrorSource().getLastContentScheduleFileName() : this.getLastContentScheduleFileName();
		}

		// If we found a content schedule file
		if( mostRecentContentScheduleFileName != null )
		{
			// Add the getContentSchedule device command
			this.addDeviceCommand(DeviceCommandType.GET_CONTENT_SCHEDULE, Constants.SCHEDULES +"/"+ mostRecentContentScheduleFileName, attemptPush, false, true, null, createNow);
			result = mostRecentContentScheduleFileName;			
		}	
		return result;
	}
	
	/**
	 * Returns the relative path to the most recent content schedule
	 * @return
	 */
	public String getLastContentScheduleFileName() throws HibernateException, ParseException
	{
		Date mostRecentContentSchedule = null;
		String mostRecentFilename = null;			
    	String schedulesDir = KuvataConfig.getKuvataHome() + 
							"/"+ Constants.SCHEMAS +					
							"/"+ SchemaDirectory.getSchema().getSchemaName() +
							"/"+ Constants.SCHEDULES;
    	
    	File f = new File( schedulesDir );       
		File[] files = f.listFiles();
		for(int i=0; i<files.length; i++)
		{
			// If this is a file and not a directory
			if( files[i].isFile() )
			{
				// Make sure this is an .xml file
				if( files[i].getName().endsWith(".xml") )
				{			
					// Parse the deviceId out of the filename
					String deviceId = files[i].getName().substring(0, files[i].getName().indexOf("-"));
					
					// If this is a content schedule for this device
					if( deviceId.equals( this.getDeviceId().toString() ) )
					{
						// Parse the date out of the filename	
			        	Date contentScheduleDate = parseContentScheduleDate( files[i].getName() );
			        	
						// Locate the most recent content schedule
			        	// If we've found a more recent date
						if(( mostRecentContentSchedule == null ) || ( contentScheduleDate.getTime() > mostRecentContentSchedule.getTime() ))
						{
							mostRecentContentSchedule = contentScheduleDate;
							mostRecentFilename = files[i].getName();
						}
						// If we've found two files of the same date -- compare version numbers
						else if(( mostRecentContentSchedule == null ) || ( contentScheduleDate.getTime() == mostRecentContentSchedule.getTime() ))
						{					
							if( mostRecentContentSchedule == null )
							{
								mostRecentContentSchedule = contentScheduleDate;
								mostRecentFilename = files[i].getName();	
							}
							else
							{								
								// Parse the version number out of the most recent filename
								String mostRecentVersionNumber = mostRecentFilename.substring(0, mostRecentFilename.lastIndexOf(".") );
								mostRecentVersionNumber = mostRecentVersionNumber.substring( mostRecentVersionNumber.lastIndexOf("-") + 1 );
								String testVersionNumber = files[i].getName().substring(0, files[i].getName().lastIndexOf(".") );
								testVersionNumber = testVersionNumber.substring( testVersionNumber.lastIndexOf("-") + 1 );
								
								// If we've found a higher version number
								if( Integer.valueOf(testVersionNumber).intValue() > Integer.valueOf(mostRecentVersionNumber).intValue() )
								{
									mostRecentContentSchedule = contentScheduleDate;
									mostRecentFilename = files[i].getName();
								}							
							}
						}
					}	
				}
			}
		} 
		return mostRecentFilename;
	}
	
	/**
	 * Parses the date out of the given content schedule file.
	 * 
	 * @param contentScheduleFilePath
	 * @return
	 * @throws ParseException
	 */
	public static Date parseContentScheduleDate(String contentScheduleFilePath) throws ParseException
	{
		DateFormat dateTimeFormat = new SimpleDateFormat( Constants.OUTPUT_DATE_FORMAT );
		String datePart = contentScheduleFilePath;
		datePart = datePart.substring( datePart.indexOf("-") + 1 );
		datePart = datePart.substring( 0, datePart.lastIndexOf("-") );    				
    	Date contentScheduleDate = dateTimeFormat.parse( datePart );
    	return contentScheduleDate;
	}
	
	/**
	 * Retrieve the first heartbeat event for this device
	 * @return
	 * @throws HibernateException
	 */
	public HeartbeatEvent getFirstHeartbeatEvent() throws HibernateException
	{
		HeartbeatEvent result = null;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT he FROM HeartbeatEvent as he "
			+"WHERE he.deviceId = ? "
			+"ORDER BY he.dt";			
		Query q = session.createQuery( hql ); 
		q.setMaxResults(1);		
		List heartbeatEvents = q.setParameter(0, this.getDeviceId()).list();
		if( heartbeatEvents != null && heartbeatEvents.size() > 0 ) {
			result = (HeartbeatEvent)heartbeatEvents.get(0);			
		}
		
		return result;		
	}
	
	public HeartbeatEvent getLastHeartbeatEvent() throws HibernateException
	{
		return getLastHeartbeatEvent(true);
	}
	
	/**
	 * Retrieve the most recent heartbeat event for this device
	 * @return
	 * @throws HibernateException
	 */
	public HeartbeatEvent getLastHeartbeatEvent(boolean doQuery) throws HibernateException
	{	
		// If this is the first time this device is heartbeating, put the id in the static cache
		if(HeartbeatEvent.heartbeatedDevices.containsKey(this.getDeviceId()) == false){
			HeartbeatEvent.heartbeatedDevices.put(this.getDeviceId(), this.getDeviceId());
		}
		
		// Synchronize this block so that the multiple threads from the same device
		// can't get into this block at the same time
		synchronized(HeartbeatEvent.heartbeatedDevices.get(this.getDeviceId())){
			if( HeartbeatEvent.containedInHeartbeatEventCache( this.getDeviceId() ) && doQuery == false )
			{
				HeartbeatEvent lastHeartbeat = HeartbeatEvent.getFromHeartbeatEventCache( this.getDeviceId() );
				return lastHeartbeat;
			}
			else
			{
				HeartbeatEvent lastHeartbeat = null;
				Session session = HibernateSession.currentSession();
				String hql = "SELECT he FROM HeartbeatEvent as he "
					+"WHERE he.deviceId = ? "
					+"AND he.isLastHeartbeat = 1 "
					+"ORDER BY he.dt DESC";
				Query q = session.createQuery( hql ); 
				List heartbeatEvents = q.setParameter(0, this.getDeviceId()).list();
				
				// If we found at least one HeartbeatEvent where isLastHeartbeat = true for this device
				if( heartbeatEvents != null && heartbeatEvents.size() > 0 )
				{				
					lastHeartbeat = (HeartbeatEvent)heartbeatEvents.get(0);
					
					// If there are more than one row with isLastHeartbeat = true for this device
					if( heartbeatEvents.size() > 1 )
					{
						/*
						 * Update the is_last_heartbeat column for any rows associated with this device
						 * that currently have their is_last_heartbeat set to true,
						 * excluding the most recent heartbeat_event 
						 */					
						hql = "UPDATE HeartbeatEvent "
							+ "SET is_last_heartbeat = 0 "						
							+ "WHERE device_id = :deviceId "
							+ "AND is_last_heartbeat = 1 "
							+ "AND heartbeat_event_id != :lastHeartbeatEventId";
						HibernateSession.beginTransaction();
						session.createQuery( hql )
							.setParameter("deviceId", this.getDeviceId())
							.setParameter("lastHeartbeatEventId", lastHeartbeat.getHeartbeatEventId())
							.executeUpdate();				
						HibernateSession.commitTransaction();
					}						
				}
				if(doQuery == false){
					HeartbeatEvent.setInHeartbeatEventCache(this.getDeviceId(), lastHeartbeat);
				}
				return lastHeartbeat;		
			}
		}
	}
	
	/**
	 * Retrieves the last 25 heartbeat events for this device.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List getMostRecentHeartbeatEvents() throws Exception{
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();
		
		String sql = "SELECT * FROM ( SELECT h.dt, h.downloads_in_progress, " +
				"h.uploads_in_progress, h.content_schedules_in_staging, h.last_content_schedule, h.bytes_to_download, h.is_last_heartbeat, h.bandwidth_utilization, h.last_service_mode FROM heartbeat_event h " +
				"WHERE h.device_id = ? ORDER BY h.dt DESC ) WHERE rownum <= 25";
		
		PreparedStatement stmt = conn.prepareStatement( sql );
		stmt.setLong(1, this.deviceId);			
		ResultSet rs = stmt.executeQuery();
		
		List<HeartbeatEvent> hes = new LinkedList();
		while(rs.next()){
			HeartbeatEvent he = new HeartbeatEvent();
			he.setDeviceId(this.deviceId);
			he.setDt(rs.getTimestamp(1));
			he.setDownloadsInProgress(rs.getString(2));
			he.setUploadsInProgress(rs.getString(3));
			he.setContentSchedulesInStaging(rs.getString(4));
			he.setLastContentSchedule(rs.getString(5));
			he.setBytesToDownload(rs.getBigDecimal(6) != null ? rs.getBigDecimal(6).longValue() : null);
			he.setIsLastHeartbeat(rs.getBigDecimal(7) != null ? rs.getBigDecimal(7).longValue() == 1 : false);
			he.setBandwidthUtilization(rs.getBigDecimal(8) != null ? rs.getBigDecimal(8).longValue() : null);
			he.setLastServiceMode(rs.getTimestamp(9) != null ? rs.getTimestamp(9) : null);
			hes.add(he);
        }
        rs.close();
        stmt.close();
        conn.close();		
		return hes;
	}
	
	/**
	 * Retrieves the last 10 self testsfor this device.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public List getMostRecentSelfTests() throws HibernateException
	{		
		Session session = HibernateSession.currentSession();
		String hql = "SELECT selfTestHistory FROM SelfTestHistory as selfTestHistory "
			+"WHERE selfTestHistory.device.deviceId = ?"
			+" ORDER BY selfTestHistory.dt DESC";
		List result = session.createQuery( hql ).setMaxResults(10).setParameter(0, this.getDeviceId()).list(); 				
		return result;		
	}	
	
	/**
	 * 
	 * @param deviceGrpMember
	 */
	public void addDeviceGrpMember(DeviceGrpMember deviceGrpMember) 
	{
		if (deviceGrpMember == null)
			throw new IllegalArgumentException("Null deviceGrpMember!");
				
		deviceGrpMembers.add(deviceGrpMember);
	}
	/**
	 * 
	 * @param deviceGrpMember
	 */
	public void removeDeviceGrpMember(DeviceGrpMember deviceGrpMember)
	{
		deviceGrpMembers.remove(deviceGrpMember);
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeDeviceGrpMembers() throws HibernateException
	{
		this.deviceGrpMembers.clear();
		this.update();
	}
	
	/**
	 * Removes all MirroredDevice objects associated with this device
	 * @throws HibernateException
	 */
	public void removeMirroredDevices() throws HibernateException
	{
		// Remove all MirroredDevice objects associated with this device
		for( Iterator<MirroredDevice> i = this.getMirrorSourceMirroredDevices().iterator(); i.hasNext(); ){
			i.next().delete();
		}
	}	
	
	/**
	 * Deletes this device and also deletes any slaves that may be associated with this device
	 */
	public void delete() throws HibernateException
	{
		HibernateSession.startBulkmode();
		
		// Clear any dirty flags associated with this device
		Dirty.makeNotDirty( this );
		
		// Delete device hardware info if present
		DeviceHardwareInfo dhi = DeviceHardwareInfo.getDeviceHardwareInfo(this);
		if(dhi != null){
			dhi.delete();
		}
		
		// If this device is a Master -- then remove it as a mirror source from it's slaves
		for( Iterator i = this.getMirrorPlayerDevices().iterator(); i.hasNext(); )
		{
			Device slaveDevice = (Device)i.next();
			Device mirrorSource = slaveDevice.getMirrorSource();
			
			// Clear out the mirror source if this was the active mirror source
			if(mirrorSource != null && mirrorSource.getDeviceId().equals(this.getDeviceId())){
				slaveDevice.setMirrorSource(null);
			}
			
			// Remove this device from the available list of mirror sources for it's mirror players
			MirroredDevice md = MirroredDevice.getMirroredDevice(slaveDevice, this);
			if(md != null){
				md.delete();
			}
		}	
		
		// If this device is an edge server to any devices -- remove those devices edge server
		for( Iterator i = this.getEdgeDevices().iterator(); i.hasNext(); )
		{			
			// Perform all actions required when un-assigning an edge server from a device
			Device edgeDevice = (Device)i.next();
			edgeDevice.unassignEdgeServer( edgeDevice.getEdgeServer() );
			
			try{
				// Add a device command to clear out the edgeServer property for the edge device
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.EDGE_SERVER.getPropertyName() +","+ Reformat.escape(null), true );
			} catch (InterruptedException e) {
				logger.error( e );
			}
		}	
		
		// If this device was using an edge server and this was the last node device for that edge server
		try {			
			if( this.getEdgeServer() != null && this.getEdgeServer().getOtherEdgeDevices(this).size() == 0 )
			{
				// Clear out the edgeServerOpenVpnHostIp on the edge server
				logger.info("Clearing edgeServerOpenVpnHostIp for: "+ this.getEdgeServer().getDeviceId());
				this.getEdgeServer().setEdgeServerOpenvpnHostIp( null );
				this.getEdgeServer().update();
				this.getEdgeServer().addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.EDGE_SERVER_OPENVPN_HOST_IP.getPropertyName() +","+ Reformat.escape(null), true );
			}
		} catch (InterruptedException e1) {
			logger.error( e1 );
		}
		
		// Clean up any mcm's (and their history) associated with this device
		List mcms = this.getMcms();
		for( Iterator i = mcms.iterator(); i.hasNext(); )
		{
			Mcm mcm = (Mcm)i.next();
			mcm.delete();
		}
		
		// Update the device's replaced by field
		HibernateSession.currentSession().createQuery("UPDATE Device d SET d.replacedBy.deviceId = NULL WHERE d.replacedBy.deviceId = :deviceId").setParameter("deviceId", this.getDeviceId()).executeUpdate();
		
		// Remove this device's macAddr from the MacAddrSchema table
		MacAddrSchema.delete( this.getMacAddr(), SchemaDirectory.getSchema().getSchemaName() );
		
		// If this device is the template device for the schema -- remove it
		Schema schema = Schema.getSchema( SchemaDirectory.getSchema().getSchemaName() );
		if( schema != null && schema.getTemplateDeviceId() != null && schema.getTemplateDeviceId() == this.getDeviceId() ){
			schema.setTemplateDeviceId( null );
			schema.update();
		}
		
		try {
			Session session = HibernateSession.currentSession();
			
			// Delete any targeted asset member rows as long as they are not the last row for that targeted asset member
			String hql = "DELETE FROM TargetedAssetMember WHERE device.deviceId = :deviceId and targetedAsset.assetId || '-' || asset.assetId IN " +
						"(SELECT targetedAsset.assetId || '-' || asset.assetId FROM TargetedAssetMember WHERE targetedAsset.assetId || '-' || asset.assetId IN " +
						"(SELECT targetedAsset.assetId || '-' || asset.assetId FROM TargetedAssetMember WHERE device.deviceId = :deviceId) " +
						"GROUP BY targetedAsset.assetId || '-' || asset.assetId HAVING COUNT(*) > 1)";
			session.createQuery(hql).setParameter("deviceId", this.getDeviceId()).executeUpdate();

			// Otherwise set the device for the last targeted asset member row to null
			hql = "UPDATE TargetedAssetMember SET device_id = "+ null + " WHERE device_id = :deviceId";
			session.createQuery( hql ).setParameter("deviceId",this.getDeviceId()).executeUpdate();								
		} 
		catch (Exception e) {
			logger.error( e );		
		}
		HibernateSession.stopBulkmode();

		/*
		 * Use JDBC/SQL to delete rows from the heartbeat_event table and the self_test_history table.
		 * Also delete these entities associated rows in the entity_instance table
		 */		
		try {
			Session session = HibernateSession.currentSession();			
			SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
			Connection conn = sessionImplementor.getConnectionProvider().getConnection();						
			Statement stmt = conn.createStatement();
			String sql = "DELETE FROM heartbeat_event WHERE device_id = "+ this.getDeviceId().toString();
			stmt.executeUpdate( sql );
			stmt.close();
			
			stmt = conn.createStatement();
			stmt.executeUpdate("commit");
			stmt.close();
			
			stmt = conn.createStatement();
			sql = "DELETE FROM self_test_history WHERE device_id = "+ this.getDeviceId().toString();
			stmt.executeUpdate( sql );
			stmt.close();
			
			stmt = conn.createStatement();
			stmt.executeUpdate("commit");
			stmt.close();
			conn.close();
		} catch (Exception e) {		
			logger.error( e );
		}
		
		// Disconnect the openVpn client so that the device re-registers if its heartbeating
		// and the public ip gets populated
		this.disconnectOpenVpnClient();
		
		super.delete();
	}
	
	/**
	 * Populates the properties of a ContentScheduleArg object for this device
	 * @return
	 */
	public ContentSchedulerArg loadContentSchedulerArg(boolean isAuto)
	{
		boolean withinContentSchedulingHorizon = false;
		Date startDatetime = null;
		TimeZone timeZone = TimeZone.getTimeZone( this.getSafeTimezone() );
		Date now = new Date();
		
		/*
		 * Calculate the startDatetime.
		 * Start by setting the startDateTime to the lastScheduledContent for the device.
		 * Since the date/time of the lastScheduledContent is in device time, but the timezone is in server timezone,
		 * we have to "un-adjust" the lastScheduledContent so that both "now" and lastScheduledContent is in server time.
		 */		
		Calendar startDateTimeCal = Calendar.getInstance();
		startDateTimeCal.setTime( this.getLastScheduledContent() );
		startDateTimeCal.add( Calendar.SECOND, -this.getTimezoneAdjustment() );
		
		// If this puts the startDateTime in the future, 
		// keep going back in decrements of additionalContentAmount until the startDateTime is in the past
		while( startDateTimeCal.getTime().after( now ) )
		{
			startDateTimeCal.add( Calendar.SECOND, -this.getAdditionalContentAmount() );
		}
				
		// Continue adding additionalContentAmount to the startDateTime 
		// until the current time is between startDateTime and startDateTime + additionalContentAmount
		Calendar startDatetimePlusAdditionalContentAmount = Calendar.getInstance();
		startDatetimePlusAdditionalContentAmount.setTime( startDateTimeCal.getTime() );
		startDatetimePlusAdditionalContentAmount.add( Calendar.SECOND, this.getAdditionalContentAmount() );
		while( (now.getTime() >= startDateTimeCal.getTimeInMillis() && now.getTime() <= startDatetimePlusAdditionalContentAmount.getTimeInMillis() ) == false )
		{
			startDateTimeCal.add( Calendar.SECOND, this.getAdditionalContentAmount() );
			startDatetimePlusAdditionalContentAmount.add( Calendar.SECOND, this.getAdditionalContentAmount() );
		}

		// This is the "content schedule start time candidate"
		startDatetime = startDateTimeCal.getTime();
		
		// If we're in auto-mode		  
		if( isAuto )
		{
			// And the startDateTime + additionalContentAmount will put us within the content scheduling horizon
			// meaning "content schedule start time candidate" + additional_content_amount - content_scheduling_horizon is less than the current time
			Calendar horizonTest = Calendar.getInstance();
			horizonTest.setTime( startDatetime );
			horizonTest.add( Calendar.SECOND, this.getAdditionalContentAmount() );
			horizonTest.add( Calendar.SECOND, -this.getContentSchedulingHorizon() );
			
			// Then the content schedule start time should be: "content schedule start time candidate" + additional_content_amount
			if( horizonTest.getTimeInMillis() < now.getTime() )
			{				
				startDatetime = startDatetimePlusAdditionalContentAmount.getTime();
			}
		}		
		
		/*
		 * Calculate the endDatetime.
		 * by setting the end date time to start date time plus content schedule length
		 */
		Calendar endDatetime = Calendar.getInstance();
		endDatetime.setTime( startDatetime );
		endDatetime.add( Calendar.SECOND, this.getAdditionalContentAmount().intValue() );		
		
		/*
		 * Account for daylight savings.
		 * NOTE: We are intentionally parsing the start and end dates to exclude timezone information.
		 * However, we needed to add myformat2 and myformat3 to the mix because of a strange DateFormat bug as follows:
		 *   1. Start with a date object:
		 *     Date endDateTime = "11/01/2008 01:00:00 PDT";
		 *   2. Parse (format) the date object to exclude timezone information (using myformat)
		 *     String endStr = "11/01/2008 01:00:00";
		 *   3. Set the timezone on myformat (myformat.setTimeZone()) to the current timezone (i.e. PDT)
		 *   4. Parse the endStr using the myformat DateFormat object
		 *   5. The resulting date object is returned as "11/01/2008 01:00:00 PST"! (should be "11/01/2008 01:00:00 PDT")
		 *   This was causing issues when crossing daylight savings. The fix was made to append the short timezone
		 *   to the endStr and parse the string using a DateFormat object that includes timezone.
		 */		
		DateFormat myformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		DateFormat myformat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
		DateFormat myformat3 = new SimpleDateFormat("Z");
		myformat.setTimeZone( timeZone );
		myformat2.setTimeZone( timeZone );
		myformat3.setTimeZone( timeZone );
		String startStr = myformat.format( startDatetime );
		String endStr = myformat.format( endDatetime.getTime() );
		Date tzStart = null;
		Date tzEnd = null;
		try
		{
			String shortTimezoneStart = myformat3.format( startDatetime );
			String shortTimezoneEnd = myformat3.format( endDatetime.getTime() );
			tzStart = myformat2.parse(startStr +" "+ shortTimezoneStart);
			tzEnd = myformat2.parse(endStr +" "+ shortTimezoneEnd);
		}
		catch(Exception e) { }
		
		// If the start and end dates span the start of daylight savings
		if( timeZone.inDaylightTime( tzStart ) == false && timeZone.inDaylightTime( tzEnd ) == true )
		{		
			// Decrement the length of the content schedule by 1 hour
			tzEnd.setTime( tzEnd.getTime() - 3600*1000 );
		}		
		// If the start and end dates span the end of daylight savings
		else if( timeZone.inDaylightTime( tzStart ) == true && timeZone.inDaylightTime( tzEnd ) == false )
		{
			// Increment the length of the content schedule by 1 hour
			tzEnd.setTime( tzEnd.getTime() + 3600*1000 );
		}
		
		Calendar convertedStartDatetime = Calendar.getInstance();
		Calendar convertedEndDatetime = Calendar.getInstance();
		try
		{			
			// Convert the startDatetime into Calendar object so that
			// XMLEncoder can instantiate the object properly when running content scheduler.
			convertedStartDatetime.setTimeZone( timeZone );
			convertedEndDatetime.setTimeZone( timeZone );
			convertedStartDatetime.setTime( tzStart );
			convertedEndDatetime.setTime( tzEnd );
		}
		catch(Exception e) {
			logger.error( e );
		}		
		
		/*
		 * Load the ContentSchedulerArg
		 */
		ContentSchedulerArg result = new ContentSchedulerArg();
		result.setDeviceId( this.getDeviceId() );
		result.setDeviceName( this.getDeviceName() );
		result.setStartDatetime( convertedStartDatetime.getTime() );
		result.setEndDatetime(  convertedEndDatetime.getTime());
		result.setTimezone( this.getSafeTimezone() );
		result.setUpdatePlaylists( true ); 				// Default
		result.setIssueDeviceCommands( true );			// Default		
		result.setFutureContentMode( false );			// Default
		return result;		
	}
	
	/**
	 * Returns an Mcm if one is associated with this device
	 * @return
	 * @throws HibernateException
	 */
	public List<Mcm> getMcms() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List<Mcm> result = session.createQuery(				
				"SELECT mcm "
				+ "FROM Mcm as mcm "	
				+ "WHERE mcm.device.deviceId = ? "
				+ "ORDER BY UPPER(mcm.mcmName)"						
				).setParameter(0, this.getDeviceId()).list();				
		return result;
	}	
	
	/**
	 * Returns the number of mcms associated with this device 
	 * where type=DISPLAY or type=UNMANAGED_DISPLAY
	 * @return
	 * @throws HibernateException
	 */
	public int getNumDisplayMcms() throws HibernateException
	{
		int result = 0;
		String hql = "SELECT COUNT(mcm) "
			+ "FROM Mcm as mcm "	
			+ "WHERE mcm.device.deviceId = :deviceId "
			+ "AND mcm.peripheralType IN ('"+ PeripheralType.DISPLAY +"', '"+ PeripheralType.NON_MONITORED_DISPLAY +"')";				
		Session session = HibernateSession.currentSession();
		Iterator i = session.createQuery( hql ).setParameter("deviceId", this.getDeviceId()).iterate();
		result = ((Long) i.next()).intValue();
		Hibernate.close( i );
		return result;		
	}	
	
	/**
	 * Returns the number of mcms of the given type
	 * @return
	 */
	public int getNumMcms(PeripheralType peripheralType)
	{
		int result = 0;
		String hql = "SELECT COUNT(mcm) "
			+ "FROM Mcm as mcm "	
			+ "WHERE mcm.device.deviceId = :deviceId "
			+ "AND mcm.peripheralType IN ('"+ peripheralType +"')";				
		Session session = HibernateSession.currentSession();
		Iterator i = session.createQuery( hql ).setParameter("deviceId", this.getDeviceId()).iterate();
		result = ((Long) i.next()).intValue();
		Hibernate.close( i );
		return result;
	}		
	
	/**
	 * @return Returns the number of devices
	 */
	public static int getDevicesCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(d) FROM Device as d where d.deleted is null OR d.deleted = 0";
		Iterator i = session.createQuery( hql ).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}	
	
	/**
	 * Converts "now" to device time according to the timezone adjustment
	 * @return
	 */
	public Date getDeviceTime()
	{
		Calendar now = Calendar.getInstance();		
		now.add( Calendar.SECOND, this.getTimezoneAdjustment() );
		return now.getTime();
	}
	
	/**
	 * Calculates the difference in time between the server timezone
	 * and this device's timezone.
	 * @return
	 */
	public int getTimezoneAdjustment()
	{
		int result = 0;		
		if( this.getTimezone() != null )
		{
			// Create a timezone object to be used for the device
			TimeZone deviceTimeZone = TimeZone.getTimeZone( this.getTimezone() );
			
			// Create a calendar object for the server time
			Calendar serverCal = new GregorianCalendar();								
						
		    // Create a calendar object using the device's timezone
			Calendar deviceCal = new GregorianCalendar( deviceTimeZone );
			
			// Set the device's time properties to the server's time properties, which will be translated using the device's timezone
			deviceCal.set(Calendar.YEAR, serverCal.get( Calendar.YEAR ));
			deviceCal.set(Calendar.DAY_OF_YEAR, serverCal.get( Calendar.DAY_OF_YEAR ));
			deviceCal.set(Calendar.HOUR_OF_DAY, serverCal.get( Calendar.HOUR_OF_DAY ));
			deviceCal.set(Calendar.MINUTE, serverCal.get( Calendar.MINUTE ));
			deviceCal.set(Calendar.SECOND, serverCal.get( Calendar.SECOND ));
			deviceCal.set(Calendar.MILLISECOND, serverCal.get( Calendar.MILLISECOND ));
			
			// Calculate the difference between the two timezones in milliseconds
			long diff = serverCal.getTimeInMillis() - deviceCal.getTimeInMillis();
						
			// Convert the difference into seconds
			double diffInSeconds = diff / 1000;
			
			// Return the result as an integer
			result = (int)Math.round( diffInSeconds );
		}
		return result;
	}
	
	/**
	 * Append the device's timezone to the supplied timestamp 
	 * @param timestamp
	 * @return
	 */
	public String appendDeviceTimezone(Date timestamp, DateFormat dateFormat, boolean useFourDigitTimezone){		
		// Default to server timezone
		TimeZone tz = Calendar.getInstance().getTimeZone();
		if( this.getTimezone() != null ){
			tz = TimeZone.getTimeZone( this.getTimezone() );
		}
		
		String result = dateFormat.format(timestamp) + " ";
		
		if(useFourDigitTimezone){
			SimpleDateFormat sdf = new SimpleDateFormat("Z");
			sdf.setTimeZone(tz);
			result += sdf.format(timestamp);
		}else{
			SimpleDateFormat sdf = new SimpleDateFormat("Z");
			sdf.setTimeZone(tz);
			result += sdf.format(timestamp);
		}
		return result;
	}	
	
	public String appendDeviceTimezone(Date timestamp, DateFormat dateFormat){
		return appendDeviceTimezone(timestamp, dateFormat, false);
	}
	
	public String appendDeviceTimezone(Date timestamp){		
		SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.SCHEDULE_FORMAT );
		TimeZone deviceTimeZone = TimeZone.getTimeZone( this.getSafeTimezone() );
		dateFormat.setTimeZone( deviceTimeZone );
		return appendDeviceTimezone( timestamp,  dateFormat );
	}
	
	/**
	 * Returns a list of GrpGrpMember objects of the "Device Groups" group
	 * @return
	 */
	public static LinkedList<GrpMember> getAllDeviceGroups()
	{
		LinkedList<GrpMember> result = null;
		Grp deviceGroups = Grp.getUniqueGrp( Constants.DEVICE_GROUPS );
		if( deviceGroups != null )
		{			
			result = new LinkedList<GrpMember>( deviceGroups.getGrpMembers() );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator( "grpName" );
			BeanPropertyComparator comparator2 = new BeanPropertyComparator( "childGrp", comparator1 );				
			Collections.sort( result, comparator2 );
			
			// Initialize the child groups within the list
			for( Iterator i=result.iterator(); i.hasNext(); )
			{
				GrpGrpMember ggm = (GrpGrpMember)i.next();
				Hibernate.initialize( ggm.getChildGrp() );
			}
		}
		return result;
	}
	
	public List getUnexecutedDeviceCommandsOfType(DeviceCommandType type){
		// Order by both createDt and deviceCommandId in case two device commands were added at the "exact" same time
		Session session = HibernateSession.currentSession();
		String hql = "SELECT dc "
			+ "FROM DeviceCommand as dc "	
			+ "WHERE dc.device.deviceId = ? "
			+ "AND dc.command='" + type.getPersistentValue() + "' "
			+ "AND (dc.status IS NULL OR dc.status='"+StatusType.QUEUED.toString()+"' OR dc.status='"+StatusType.IN_PROGRESS.toString()+"')";
		
		// For each unexecuted device command
		List<DeviceCommand> unexecutedDeviceCommands = session.createQuery( hql ).setParameter(0,this.getDeviceId()).list();
		return unexecutedDeviceCommands;
	}
	
	/**
	 * Returns a list of device commands that have yet to be executed for this device (deviceCommand.status is null)
	 * @return
	 * @throws HibernateException
	 */
	public List getUnexecutedDeviceCommands() throws HibernateException, ParseException
	{		
		HashMap<DeviceCommandInfo, DeviceCommand> uniqueDeviceCommands = new LinkedHashMap<DeviceCommandInfo, DeviceCommand>();
		List<DeviceCommand> result = new LinkedList<DeviceCommand>();		
				
		// Order by both createDt and deviceCommandId in case two device commands were added at the "exact" same time
		Session session = HibernateSession.currentSession();
		String hql = "SELECT dc "
			+ "FROM DeviceCommand as dc "	
			+ "WHERE dc.device.deviceId = ? "
			+ "AND (dc.status IS NULL OR dc.status='"+StatusType.QUEUED.toString()+"' OR dc.status='"+StatusType.IN_PROGRESS.toString()+"') "
			+ "ORDER BY dc.createDt desc, dc.deviceCommandId desc";
		
		// For each unexecuted device command
		List<DeviceCommand> unexecutedDeviceCommands = session.createQuery( hql ).setParameter(0,this.getDeviceId()).list();				
		for(Iterator<DeviceCommand> i = unexecutedDeviceCommands.iterator(); i.hasNext();)
		{
			DeviceCommand dc = i.next();
			
			// If this device command is currently in a status of "In Progress" -- add it to the list of unexecuted device commands
			if( dc.getStatus() != null && dc.getStatus().toString().equalsIgnoreCase( StatusType.IN_PROGRESS.toString() ) )
			{
				result.add( dc );
			}
			// For all other statuses
			else
			{
				// Create a new DeviceCommandInfo object which will allow us to test for and ensure unique device commands
				DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
				deviceCommandInfo.setCommand( dc.getCommand() );

				// If this is a "propertyChange" device command, make sure we send down the most recent one for each mcm/property type
				if( dc.getCommand().equalsIgnoreCase( DeviceCommandType.PROPERTY_CHANGE_COMMAND.getPersistentValue() ) )
				{
					// Parse the parameters of the device command -- 
					// We can't use split() here in case the third parameter is "". split() will not count it.
					String params = dc.getParameters();
					String param1 = null;
					String param2 = null;
					String param3 = null;
					if( params.indexOf(",") >= 0 ){
						param1 = params.substring( 0, params.indexOf(",") );
						params = params.substring( params.indexOf(",") + 1 );
					}
					if( params.indexOf(",") >= 0 ){
						param2 = params.substring( 0, params.indexOf(",") );
						param3 = params.substring( params.indexOf(",") + 1 );
					} else {
						param2 = params;
					}				
					
					// If there are 3 parameters, it must be for an mcm property, skip the mcmId parameter
					String propertyType = null;			
					if( param3 != null ){				
						propertyType = param2;			
						deviceCommandInfo.setMcmId( param1 );
					}else {
						propertyType = param1;				
					}	
					deviceCommandInfo.setSubCommand( propertyType );								
				}
				// If this is an "mcmCommand" device command
				else if( dc.getCommand().equalsIgnoreCase( DeviceCommandType.MCM_COMMAND.getPersistentValue() ) )
				{		
					// Parse out the mcmCommand from the parameters
					String subCommand = "";
					if( dc.getParameters().indexOf("(") > 0 ){
						subCommand = dc.getParameters().substring( 0, dc.getParameters().indexOf("(") );
					}
					String mcmId = "";
					if( dc.getParameters().indexOf("\"") > 0 ){
						mcmId = dc.getParameters().substring( dc.getParameters().indexOf("\"") + 1 );
						if( mcmId.indexOf("\"") > 0 ){
							mcmId = mcmId.substring( 0, mcmId.indexOf("\"") );						
						}
					}
					deviceCommandInfo.setSubCommand( subCommand );
					deviceCommandInfo.setMcmId( mcmId );
				}
				// If this is a "getContentSchedule" device command, find the one with the most recent content schedule date
				else if( dc.getCommand().equalsIgnoreCase( DeviceCommandType.GET_CONTENT_SCHEDULE.getPersistentValue() ) )
				{								
					// If this content schedule has an end date in the past -- cancel it
					if( contentScheduleIsOld( dc.getParameters() ) )
					{
						// Set the status of this "old" device command to "Cancelled"
						dc.setStatus( StatusType.CANCELLED );
						dc.setLastModifiedDt( new Date() );
						dc.update();
						
						logger.info("Cancelling getCS command since it is old: " + dc.getDevice().getDeviceId() + ":" + dc.getParameters());
						
						// Set the status of file transmission to "Cancelled" as well, so it is not reflected in the bandwidth report
						FileTransmission fileTransmission = FileTransmission.getFileTransmission( this, dc.getParameters() );
						if( fileTransmission != null ){
							fileTransmission.setStatus( FileTransmissionStatus.CANCELLED );
							fileTransmission.update();
						}
						
						// Run the content schedule cancellation logic
						ContentSchedule.cancelCS(this, dc.getParameters().substring(dc.getParameters().lastIndexOf("/") + 1), true);
						
						// Do not add this device command to the collection of unique device commands
						continue;
					}
					// If this content schedule has an end date in the future -- do not cancel it
					else {					
						deviceCommandInfo.setParameters( dc.getParameters() );
					}
				}			
				// If this is any other device command, make sure the parameters are unique
				else
				{
					deviceCommandInfo.setParameters( dc.getParameters() );				
				}
		
				// Since we are ordering by dt desc, we can assume that if the device command is already in the
				// uniqueDeviceCommands collection, then we have already added the most recent device command of that type
				if( uniqueDeviceCommands.get( deviceCommandInfo ) == null ){
					uniqueDeviceCommands.put( deviceCommandInfo, dc );
				}else{
					// Set the status of this "old" device command to "Cancelled"
					dc.setStatus( StatusType.CANCELLED );
					dc.setLastModifiedDt( new Date() );
					dc.update();
				}											
			}			
		}
		
		// Add each unique device command we found to the resulting linked list		
		for( Iterator<DeviceCommand> i=uniqueDeviceCommands.values().iterator(); i.hasNext(); )
		{
			DeviceCommand dc = i.next();
			result.add( dc );
		}
		
		// Reverse the order of the device commands so that they're in ascending order when sent to the device
		Collections.reverse( result );
		return result;
	}
	
	/**
	 * Parse the given content schedule file to determine if the end date is in the past
	 * @param contentScheduleRelativePath
	 * @return
	 */
	private boolean contentScheduleIsOld(String contentScheduleRelativePath)
	{
		SimpleDateFormat CS_FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

		boolean result = false;
		try {
			
			// Parse out the deviceId from the filename. This CS could be for a slave/mirror player.
			Long deviceId = Long.parseLong(contentScheduleRelativePath.substring(contentScheduleRelativePath.lastIndexOf("/") + 1, contentScheduleRelativePath.indexOf("-")));
			ContentSchedule latestCs = ContentSchedule.getMostRecentCurrentContentSchedule(deviceId);
			
			// If the param is not the latest CS
			if(latestCs != null && contentScheduleRelativePath.contains(latestCs.getFileName()) == false){
				String paramFileName = contentScheduleRelativePath.substring(contentScheduleRelativePath.lastIndexOf("/") + 1);
				Date paramStartDate = CS_FILE_DATE_FORMAT.parse(paramFileName.substring(paramFileName.indexOf("-") + 1, paramFileName.lastIndexOf("-")));
				
				// If the param start is before the latest CS end
				if(paramStartDate.before(latestCs.getEndDt())){
					// Content Schedule is old
					result = true;
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}
		
	/**
	 * Returns a list of the most recent property_change device commands for each property type.
	 * 
	 * @param deviceCommandType
	 * @return
	 */
	public List<String> getMostRecentActivePropertyChangeDeviceCommandParameters()
	{
		Session session = HibernateSession.currentSession();	
		List deviceCommands = session.createQuery(				
				"SELECT dc "
				+ "FROM DeviceCommand as dc "	
				+ "WHERE dc.device.deviceId = ? "
				+ "AND dc.command = '"+ DeviceCommandType.PROPERTY_CHANGE_COMMAND +"' "
				+ "AND (dc.status IS NULL "
				+ "OR dc.status = '"+ StatusType.IN_PROGRESS.getStatusTypeName() + "' "
				+ "OR dc.status = '"+ StatusType.QUEUED.getStatusTypeName() + "' "
				+ "OR dc.status = '"+ StatusType.PENDING.getStatusTypeName() + "') "
				+ "ORDER BY dc.createDt DESC"
				).setParameter(0,this.getDeviceId()).list();	
		
		// Make sure we only have the most recent property change command for each property type
		HashMap propertyChangeCommandParameters = new HashMap();
		for( Iterator i=deviceCommands.iterator(); i.hasNext(); )
		{
			DeviceCommand dc = (DeviceCommand)i.next();
			
			// Parse the parameters of the device command -- 
			// We can't use split() here in case the third parameter is "". split() will not count it.
			String params = dc.getParameters();
			String param1 = null;
			String param2 = null;
			String param3 = null;
			if( params.indexOf(",") >= 0 ){
				param1 = params.substring( 0, params.indexOf(",") );
				params = params.substring( params.indexOf(",") + 1 );
			}
			if( params.indexOf(",") >= 0 ){
				param2 = params.substring( 0, params.indexOf(",") );
				param3 = params.substring( params.indexOf(",") + 1 );
			} else {
				param2 = params;
			}				
			
			// If there are 3 parameters, it must be for an mcm property, skip the mcmId parameter
			String propertyType = null;			
			if( param3 != null ){				
				propertyType = param2;				
			}else {
				propertyType = param1;				
			}					

			// Since we are ordering by dt desc, only put the first propertyChange device command for each property type
			if( propertyChangeCommandParameters.get( propertyType ) == null ){
				propertyChangeCommandParameters.put( propertyType, dc.getParameters() );
			}
		}	
		
		return new ArrayList(propertyChangeCommandParameters.values());
	}	
	
	/**
	 * Gets the count of the assets according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @return
	 * @throws ParseException
	 */
	public static int searchDevicesCount(AttrDefinition attrDefinition, DeviceSearchType deviceSearchType, String deviceSearchString, String selectedDeviceGroup, String selectedSearchOption, String searchString, String[] selectedSearchOptions, 
			String minDate, String maxDate, String minNumber, String maxNumber, int startingRecord, boolean onlyShowDevicesWithMcms) throws ParseException
	{
		int result = 0;
		String hql = buildSearchHql( deviceSearchType, deviceSearchString, selectedDeviceGroup, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, null, true, onlyShowDevicesWithMcms, false, true );		
				
		// If an attrDefinition object was not passed in, we must be searching by device group		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}
			// If we are searching based on auto update
			else if( selectedSearchOption.equals(Constants.AUTO_UPDATE) && selectedSearchOptions.length > 0){
				// Use the selectedSearchOptions a named parameter in the query object
				DeviceAutoUpdateType[] autoUpdateValues = new DeviceAutoUpdateType[selectedSearchOptions.length];
				int count = 0;
				for(String s : selectedSearchOptions){
					autoUpdateValues[count++] = DeviceAutoUpdateType.getDeviceAutoUpdateType(s);
				}
				result = KMMServlet.getRecordCount( hql, autoUpdateValues, "autoUpdateValues" );
			}
			// If we are searching based on billing status
			else if( selectedSearchOption.equals(DeviceSearchType.LICENSE_STATUS.getName()) && selectedSearchOptions.length > 0){
				// Use the selectedSearchOptions a named parameter in the query object
				result = KMMServlet.getRecordCount( hql, selectedSearchOptions, "billingStatusValues" );
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = KMMServlet.getRecordCount( hql, selectedSearchOptions, "attrValues" );					
			}else{
				result = KMMServlet.getRecordCount( hql );
			}
		}		
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) };
				result = KMMServlet.getRecordCount( hql, params );
			}		
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}		
		}
		return result;
	}
	
	public static Page searchDevices(AttrDefinition attrDefinition, DeviceSearchType deviceSearchType, String deviceSearchString, String selectedDeviceGroupId, 
			String selectedSearchOption, String searchString, String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, 
			String maxNumber, String orderBy, int startingRecord, int selectedItemsPerPage, boolean isEntityOverviewPage) throws ParseException
	{
			return searchDevices(attrDefinition, deviceSearchType, deviceSearchString, selectedDeviceGroupId, selectedSearchOption, searchString, selectedSearchOptions,
					minDate, maxDate, minNumber, maxNumber, orderBy, startingRecord, selectedItemsPerPage, isEntityOverviewPage, false, false, true);
	}
	
	/**
	 * Returns a page of devices according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @param selectedItemsPerPage
	 * @return
	 * @throws ParseException
	 */
	public static Page searchDevices(AttrDefinition attrDefinition, DeviceSearchType deviceSearchType, String deviceSearchString, String selectedDeviceGroupId, 
			String selectedSearchOption, String searchString, String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, 
			String maxNumber, String orderBy, int startingRecord, int selectedItemsPerPage, boolean isEntityOverviewPage, boolean onlyShowDevicesWithMcms, boolean returnIdsOnly, boolean addWildcards) throws ParseException
	{
		Page result = null;
		int pageNum = startingRecord / selectedItemsPerPage;
		String hql = buildSearchHql( deviceSearchType, deviceSearchString, selectedDeviceGroupId, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, orderBy, false, onlyShowDevicesWithMcms, returnIdsOnly, addWildcards );
		
		// If an attrDefinition object was not passed in, we must be searching by asset type		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, params, isEntityOverviewPage, returnIdsOnly );
			}
			// If we are searching based on auto update
			else if( selectedSearchOption.equals(Constants.AUTO_UPDATE) && selectedSearchOptions.length > 0){
				// Use the selectedSearchOptions a named parameter in the query object 
				DeviceAutoUpdateType[] autoUpdateValues = new DeviceAutoUpdateType[selectedSearchOptions.length];
				int count = 0;
				for(String s : selectedSearchOptions){
					autoUpdateValues[count++] = DeviceAutoUpdateType.getDeviceAutoUpdateType(s);
				}
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, autoUpdateValues, "autoUpdateValues", isEntityOverviewPage, returnIdsOnly );
			}
			// If we are searching based on billing status
			else if( selectedSearchOption.equals(DeviceSearchType.LICENSE_STATUS.getName()) && selectedSearchOptions.length > 0){
				// Use the selectedSearchOptions a named parameter in the query object 
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, selectedSearchOptions, "billingStatusValues", isEntityOverviewPage, returnIdsOnly );
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, selectedSearchOptions, "attrValues", isEntityOverviewPage, returnIdsOnly );
			}else{
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, isEntityOverviewPage, returnIdsOnly );
			}
		}
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, isEntityOverviewPage, returnIdsOnly );	
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, params, isEntityOverviewPage, returnIdsOnly );					
			}			
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, isEntityOverviewPage, returnIdsOnly );	
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = Device.getDevices( hql, pageNum, selectedItemsPerPage, params, isEntityOverviewPage, returnIdsOnly );						
			}			
		}
		return result;
	}	
	
	/**
	 * Builds the hql to retrieve assets according to the given search criteria.
	 *  
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param getCount
	 * @return
	 */
	public static String buildSearchHql(DeviceSearchType deviceSearchType, String deviceSearchString, String selectedDeviceGroupId, String selectedSearchOption, String searchString, 
			String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber, String orderBy, boolean getCount,
			boolean onlyShowDevicesWithMcms, boolean returnIdsOnly, boolean addWildcards)
	{
		String hql = "";
		
		// Trim input boxes
		deviceSearchString = deviceSearchString != null && deviceSearchString.length() > 0 ? deviceSearchString.trim() : deviceSearchString;
		searchString = searchString != null && searchString.length() > 0 ? searchString.trim() : searchString;
		
		// If the deviceName search string was left blank, use wildcard
		if( deviceSearchString == null || deviceSearchString.trim().length() == 0 ){
			deviceSearchString = "%";
		}
		
		// If this is a list of device names
		String deviceNamesList = "";
		String deviceIdsList = deviceSearchString;
		if(deviceSearchString.contains("~")){
			if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_NAME.getPersistentValue() ) ){
				for(String s : deviceSearchString.split("~")){
					deviceNamesList += deviceNamesList.length() > 0 ? ",'" + s.trim() + "'" : "'" + s.trim() + "'";
				}
			}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_ID.getPersistentValue() ) ){
				deviceIdsList = "";
				for(String s : deviceSearchString.split("~")){
					deviceIdsList += deviceIdsList.length() > 0 ? "," + s.trim() : s.trim();
				}
			}
		}
		// Imply *
		else if(addWildcards){
			if(deviceSearchString.startsWith("*") == false){
				deviceSearchString = "*" + deviceSearchString;
			}
			if(deviceSearchString.endsWith("*") == false){
				deviceSearchString = deviceSearchString + "*";
			}
			if(searchString != null && searchString.length() > 0){
				if(searchString.startsWith("*") == false){
					searchString = "*" + searchString;
				}
				if(searchString.endsWith("*") == false){
					searchString = searchString + "*";
				}
			}
		}
		
		// Convert any "*" to "%" for wildcard searches
		deviceSearchString = deviceSearchString.replaceAll("\\*", "\\%");	
		deviceSearchString = Reformat.oraesc(deviceSearchString);	
		
		// Default
		if( orderBy == null || orderBy.length() == 0 ){
			orderBy = "UPPER(device.deviceName)";
		}
				
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(device) ";
		} else {
			if( returnIdsOnly ){
				hql = "SELECT device.deviceId ";
			}else{
				hql = "SELECT device ";	
			}
		}

		/*
		 * If the "No Metadata", or "Date Modified" option was selected, exclude the metadata from the search criteria 
		 */
		boolean excludeMetadataCriteria = false;
		if( selectedSearchOption == null || selectedSearchOption.equalsIgnoreCase( Constants.NO_METADATA ) || selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) ||
				selectedSearchOption.equalsIgnoreCase(Constants.AUTO_UPDATE) || selectedSearchOption.equalsIgnoreCase(DeviceSearchType.LICENSE_STATUS.getName()) || selectedSearchOption.equalsIgnoreCase(DeviceSearchType.VERSION.getName()))
		{
			excludeMetadataCriteria = true;
		}
		else
		{
			/*
			 * Search by this attr definition
			 */
			AttrDefinition ad = AttrDefinition.getAttrDefinition( new Long( selectedSearchOption ) );
			if( ad != null)
			{				
				// If this is a String attr
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If this is a multi-select
					if( ad.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) )
					{
						// If no items were selected
						if( selectedSearchOptions.length == 0 )
						{
							// Exclude the attrDefinition criteria							
							excludeMetadataCriteria = true;
						}
						else
						{						
							// Get all devices that have a StringAttr with the given criteria
							hql += "FROM Device as device WHERE "
								+   "device.deviceId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND attr.value IN (:attrValues) ) ";
														
							// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedDeviceGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND device.deviceId IN "
								+ 	" (SELECT device.deviceId "
								+ 	"	FROM DeviceGrpMember as dgm "
								+ 	"	JOIN dgm.device as device "
								+ 	"	WHERE dgm.grp.grpId = '"+ selectedDeviceGroupId +"') ";																		
							}
							
							// Search according to the specified deviceSearchType
							if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_NAME.getPersistentValue() ) ){
								if(deviceNamesList != null && deviceNamesList.length() > 0){
									hql += "AND device.deviceName IN (" + deviceNamesList + ") ";
								}else{
									hql += "AND UPPER(device.deviceName) LIKE UPPER('"+ deviceSearchString +"') ";
								}
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_ID.getPersistentValue() ) ){
								hql += "AND device.deviceId IN ("+ deviceIdsList +") ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.MAC_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.macAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.IP_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.ipAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VPN_IP_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.vpnIpAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VERSION.getPersistentValue() ) ){
								hql += "AND UPPER(device.version) LIKE UPPER('"+ deviceSearchString +"') ";
							}
							
							// If we are searching only for devices with MCMs
							if(onlyShowDevicesWithMcms){
								hql += "AND device.deviceId IN (SELECT DISTINCT mcm.device.deviceId FROM Mcm as mcm) ";
							}
							
							hql += "ORDER BY "+ orderBy;									
						}																		
					}
					else
					{						
						// If the searchString was left blank 
						if( searchString == null || searchString.trim().length() == 0 )
						{
							// Exclude the attrDefinition criteria					
							excludeMetadataCriteria = true;						
						}
						else
						{
							// Convert any "*" to "%" for wildcard searches		
							searchString = searchString.replaceAll("\\*", "\\%");	
							searchString = Reformat.oraesc(searchString);											
							
							// Get all devices that have a StringAttr with the given criteria
							hql += "FROM Device as device WHERE "
								+   "device.deviceId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"') ) ";
							// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedDeviceGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND device.deviceId IN "
								+ 	" (SELECT device.deviceId "
								+ 	"	FROM DeviceGrpMember as dgm "
								+ 	"	JOIN dgm.device as device "
								+ 	"	WHERE dgm.grp.grpId = '"+ selectedDeviceGroupId +"') ";																		
							}							
							// Search according to the specified deviceSearchType
							if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_NAME.getPersistentValue() ) ){
								if(deviceNamesList != null && deviceNamesList.length() > 0){
									hql += "AND device.deviceName IN (" + deviceNamesList + ") ";
								}else{
									hql += "AND UPPER(device.deviceName) LIKE UPPER('"+ deviceSearchString +"') ";
								}
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_ID.getPersistentValue() ) ){
								hql += "AND device.deviceId IN ("+ deviceIdsList +") ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.MAC_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.macAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.IP_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.ipAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VPN_IP_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.vpnIpAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VERSION.getPersistentValue() ) ){
								hql += "AND UPPER(device.version) LIKE UPPER('"+ deviceSearchString +"') ";
							}
							
							// If we are searching only for devices with MCMs
							if(onlyShowDevicesWithMcms){
								hql += "AND device.deviceId IN (SELECT DISTINCT mcm.device.deviceId FROM Mcm as mcm) ";
							}							
							hql += "ORDER BY "+ orderBy;
						}										
					}
				}
				// If this is a Date attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// If both the min and max dates were left blank
					if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a DateAttr.value between the two dates
						hql += "FROM Device as device WHERE "
							+   "device.deviceId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						
							// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedDeviceGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND device.deviceId IN "
								+ 	" (SELECT device.deviceId "
								+ 	"	FROM DeviceGrpMember as dgm "
								+ 	"	JOIN dgm.device as device "
								+ 	"	WHERE dgm.grp.grpId = '"+ selectedDeviceGroupId +"') ";																		
							}							
							// Search according to the specified deviceSearchType
							if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_NAME.getPersistentValue() ) ){
								if(deviceNamesList != null && deviceNamesList.length() > 0){
									hql += "AND device.deviceName IN (" + deviceNamesList + ") ";
								}else{
									hql += "AND UPPER(device.deviceName) LIKE UPPER('"+ deviceSearchString +"') ";
								}
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_ID.getPersistentValue() ) ){
								hql += "AND device.deviceId IN ("+ deviceIdsList +") ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.MAC_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.macAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.IP_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.ipAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VPN_IP_ADDRESS.getPersistentValue() ) ){
								hql += "AND UPPER(device.vpnIpAddr) LIKE UPPER('"+ deviceSearchString +"') ";
							}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VERSION.getPersistentValue() ) ){
								hql += "AND UPPER(device.version) LIKE UPPER('"+ deviceSearchString +"') ";
							}
							
							// If we are searching only for devices with MCMs
							if(onlyShowDevicesWithMcms){
								hql += "AND device.deviceId IN (SELECT DISTINCT mcm.device.deviceId FROM Mcm as mcm) ";
							}							
							hql += "ORDER BY "+ orderBy;		
					}
				}
				// If this is a Number attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If both the min and max numbers were left blank
					if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a NumberAttr.value between the two dates
						hql += "FROM Device as device WHERE "
							+	"device.deviceId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
						if( selectedDeviceGroupId.equalsIgnoreCase("-1") == false )
						{					
							hql += "AND device.deviceId IN "
							+ 	" (SELECT device.deviceId "
							+ 	"	FROM DeviceGrpMember as dgm "
							+ 	"	JOIN dgm.device as device "
							+ 	"	WHERE dgm.grp.grpId = '"+ selectedDeviceGroupId +"') ";																		
						}						
						// Search according to the specified deviceSearchType
						if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_NAME.getPersistentValue() ) ){
							if(deviceNamesList != null && deviceNamesList.length() > 0){
								hql += "AND device.deviceName IN (" + deviceNamesList + ") ";
							}else{
								hql += "AND UPPER(device.deviceName) LIKE UPPER('"+ deviceSearchString +"') ";
							}
						}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_ID.getPersistentValue() ) ){
							hql += "AND device.deviceId IN ("+ deviceIdsList +") ";
						}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.MAC_ADDRESS.getPersistentValue() ) ){
							hql += "AND UPPER(device.macAddr) LIKE UPPER('"+ deviceSearchString +"') ";
						}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.IP_ADDRESS.getPersistentValue() ) ){
							hql += "AND UPPER(device.ipAddr) LIKE UPPER('"+ deviceSearchString +"') ";
						}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VPN_IP_ADDRESS.getPersistentValue() ) ){
							hql += "AND UPPER(device.vpnIpAddr) LIKE UPPER('"+ deviceSearchString +"') ";
						}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VERSION.getPersistentValue() ) ){
							hql += "AND UPPER(device.version) LIKE UPPER('"+ deviceSearchString +"') ";
						}
						
						// If we are searching only for devices with MCMs
						if(onlyShowDevicesWithMcms){
							hql += "AND device.deviceId IN (SELECT DISTINCT mcm.device.deviceId FROM Mcm as mcm) ";							
						}						
						hql += "ORDER BY "+ orderBy;		
					}
				}				
			}
		}
	
		// If we're not excluding the metadata criteria in the query
		if( excludeMetadataCriteria )
		{
			hql += "FROM Device as device WHERE ";
			
			// Search according to the specified deviceSearchType
			if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_NAME.getPersistentValue() ) ){
				if(deviceNamesList != null && deviceNamesList.length() > 0){
					hql += "device.deviceName IN (" + deviceNamesList + ") ";
				}else{
					hql += "UPPER(device.deviceName) LIKE UPPER('"+ deviceSearchString +"') ";
				}
			}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.DEVICE_ID.getPersistentValue() ) ){
				hql += "device.deviceId IN ("+ deviceIdsList +") ";
			}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.MAC_ADDRESS.getPersistentValue() ) ){
				hql += "UPPER(device.macAddr) LIKE UPPER('"+ deviceSearchString +"') ";
			}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.IP_ADDRESS.getPersistentValue() ) ){
				hql += "UPPER(device.ipAddr) LIKE UPPER('"+ deviceSearchString +"') ";
			}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VPN_IP_ADDRESS.getPersistentValue() ) ){
				hql += "UPPER(device.vpnIpAddr) LIKE UPPER('"+ deviceSearchString +"') ";
			}else if( deviceSearchType.getPersistentValue().equalsIgnoreCase( DeviceSearchType.VERSION.getPersistentValue() ) ){
				hql += "UPPER(device.version) LIKE UPPER('"+ deviceSearchString +"') ";
			}
			
			// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
			if( selectedDeviceGroupId.equalsIgnoreCase("-1") == false )
			{					
				hql += "AND device.deviceId IN "
				+ 	" (SELECT device.deviceId "
				+ 	"	FROM DeviceGrpMember as dgm "
				+ 	"	JOIN dgm.device as device "
				+ 	"	WHERE dgm.grp.grpId = '"+ selectedDeviceGroupId +"') ";																		
			}
			
			// If we're filtering by last modified date
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
			{
				hql +=	" AND device.lastModified >= ? "
					+	" AND device.lastModified <= ? ";				
			}
			// If we're filtering by auto update
			else if( selectedSearchOption.equalsIgnoreCase( Constants.AUTO_UPDATE ) && selectedSearchOptions.length > 0)
			{
				hql +=   " AND device.autoUpdate IN (:autoUpdateValues) ";				
			}
			// If we're filtering by license status
			else if( selectedSearchOption.equalsIgnoreCase( DeviceSearchType.LICENSE_STATUS.getName()) && selectedSearchOptions.length > 0){
				hql +=   " AND device.readableBillingStatus IN (:billingStatusValues) ";
			}
			// If we're filtering by Version
			else if( selectedSearchOption.equalsIgnoreCase( DeviceSearchType.VERSION.getName()) && searchString.length() > 0){
				// Convert any "*" to "%" for wildcard searches		
				searchString = searchString.replaceAll("\\*", "\\%");	
				searchString = Reformat.oraesc(searchString);
				
				hql += " AND UPPER(device.version) LIKE UPPER('"+ searchString +"') ";
			}
			
			
			// If we are searching only for devices with MCMs
			if(onlyShowDevicesWithMcms){
				hql += "AND device.deviceId IN (SELECT DISTINCT mcm.device.deviceId FROM Mcm as mcm) ";
			}			
			hql += "ORDER BY "+ orderBy;				
		}
		
		// Add second order by device name
		hql += ", UPPER(device.deviceName)";
		
		return hql;
	}
	
	/**
	 * Builds the hql to retrieve assets according to the given search criteria.
	 *  
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param getCount
	 * @return
	 */
	public static String buildSearchHql(String selectedSearchOption, String searchString, String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber)
	{
		// Imply *
		if(searchString != null && searchString.length() > 0){
			if(searchString.startsWith("*") == false){
				searchString = "*" + searchString;
			}
			if(searchString.endsWith("*") == false){
				searchString = searchString + "*";
			}
			
			// Convert any "*" to "%" for wildcard searches		
			searchString = searchString.replaceAll("\\*", "\\%");	
			searchString = Reformat.oraesc(searchString);
		}
		
		String hql = "";
		
		// If we are filtering by version
		if(selectedSearchOption != null && selectedSearchOption.equals(DeviceSearchType.VERSION.getName())){
			hql = "SELECT device.deviceId FROM Device device WHERE device.version LIKE ('"+ searchString +"')";
		}
		
		// If we are filtering by billing status
		else if(selectedSearchOption != null && selectedSearchOption.equals(DeviceSearchType.LICENSE_STATUS.getName())){
			hql = "SELECT device.deviceId FROM Device device WHERE device.readableBillingStatus IN (:billingStatuses)";
		}
		
		// If we are filtering by auto update
		else if(selectedSearchOption != null && selectedSearchOption.equals(Constants.AUTO_UPDATE)){
			hql = "SELECT device.deviceId FROM Device device WHERE device.autoUpdate IN (:autoUpdateValues)";
		}
		
		/*
		 * Search by this attr definition
		 */
		else{
			AttrDefinition ad = AttrDefinition.getAttrDefinition( new Long( selectedSearchOption ) );
			if( ad != null)
			{
				// If this is a String attr
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If this is a multi-select
					if( ad.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) )
					{
						// Get all devices that have a StringAttr with the given criteria
						hql = "SELECT attr.ownerId "
							+	" FROM StringAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value IN (:attrValues) ";																		
					}
					else
					{
						hql = "SELECT attr.ownerId "
							+	" FROM StringAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"')";
					}
				}
				// If this is a Date attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// Get all assets that have a DateAttr.value between the two dates
					hql = "SELECT attr.ownerId "
						+	" FROM DateAttr attr "
						+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
						+	" AND attr.value >= :minDate "
						+	" AND attr.value <= :maxDate ";
				}
				// If this is a Number attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// Get all assets that have a NumberAttr.value between the two dates
					hql = "SELECT attr.ownerId "
						+	" FROM NumberAttr attr "
						+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
						+	" AND attr.value >= :minNumber "
						+	" AND attr.value <= :maxNumber ";
				}				
			}
		}
		
		return hql;
	}
	
	/**
	 * Called from DispatcherSoapBindingImpl.getDeviceCommands().
	 * Adds a device command for each device release that this device may need
	 * @return
	 */
	public void addDeviceReleaseDeviceCommands(boolean ignoreEdgeServer) throws InterruptedException{
		// If this device does not have it's auto-update features set to "Off"
		if( this.getAutoUpdate() != null && this.getAutoUpdate().getPersistentValue().equalsIgnoreCase( DeviceAutoUpdateType.OFF.getPersistentValue() ) == false ){
			// Since we sent down device releases based on the OS, make sure we know the OS of this device
			if( this.getOsVersion() != null && osVersion.length() > 0 ){
				// Get a reference to the device_releases folder for this schema
				String deviceReleasesDir = KuvataConfig.getKuvataHome() +"/"+ DispatcherConstants.SCHEMAS_DIRECTORY +"/"
					+ SchemaDirectory.getSchema().getSchemaName() +"/"+ DispatcherConstants.DEVICE_RELEASES_DIRECTORY;
				
				File f = new File( deviceReleasesDir );
				if( f.exists() ){
					// For each zip file in the releases folder	
					File[] files = f.listFiles();
					for(int i=0; i<files.length; i++){
						// If this is a file and not a directory
						if( files[i].isFile() ){
							// Make sure this is a .zip file
							String ext = files[i].getName().substring( files[i].getName().lastIndexOf(".") );
							if( ext.equalsIgnoreCase(".zip") ){
								// If this device has it's auto-update feature set to "On" or "On + Beta"
								if( this.getAutoUpdate().getPersistentValue().equalsIgnoreCase( DeviceAutoUpdateType.ON.getPersistentValue() )
										|| this.getAutoUpdate().getPersistentValue().equalsIgnoreCase( DeviceAutoUpdateType.ON_PLUS_BETA.getPersistentValue() ) ){
									// If the version of the zip file is greater than the current version of the device
									this.addDeviceReleaseDeviceCommand( files[i].getName().toLowerCase(), ignoreEdgeServer );								
								}																			
							}
						}
					}
				}
			}
		}		
	}	
	
	/**
	 * Determines if the given versionFilename has a version greater than that of this device,
	 * and if so, adds a either a getFile or installDeviceRelease device command for the given versionFilename.
	 * @param versionFilename
	 * @return
	 */
	private void addDeviceReleaseDeviceCommand(String versionFilename, boolean ignoreEdgeServer) throws InterruptedException
	{
		String fileVersion = versionFilename;
		String deviceVersion = this.getVersion();
		
		// If this device does not have a version specified -- return
		if( deviceVersion == null || deviceVersion.length() == 0 ){
			return;
		}		
		// If this file is a beta version and this device does not have it's auto-update property set to "On + Beta" -- return 
		else if( versionFilename.indexOf("b") > 0 && this.getAutoUpdate().getPersistentValue().equalsIgnoreCase( DeviceAutoUpdateType.ON_PLUS_BETA.getPersistentValue() ) == false ){
			return;
		}
		
		// Strip out the file extension from the filename
		if( fileVersion.indexOf(".zip") > 0 ){
			fileVersion = fileVersion.substring( 0, fileVersion.lastIndexOf(".zip") );
		}
		
		// If this is an Android/Arch release
		if( fileVersion.startsWith(Constants.ANDROID.toLowerCase()) || fileVersion.startsWith(Constants.ARCH.toLowerCase()) ){
			fileVersion = fileVersion.substring(fileVersion.indexOf("_") + 1);
		}
		
		final String ANDROID_PRE_REQ1 = "5.2.1";
		final String ANDROID_PRE_REQ2 = "5.3.3b2";
		final String ANDROID_PRE_REQ3 = "5.3.9";
		
		// Only go ahead with device release for the correct OS
		if(this.getOsVersion() != null && this.getOsVersion().startsWith(Constants.ANDROID)){
			if(versionFilename.startsWith(Constants.ANDROID.toLowerCase())){
				
				// This is a temporary hack to avoid 5.3 or above going to devices that aren't on 5.2.1 yet
				if(isNewerVersion(ANDROID_PRE_REQ1, deviceVersion)){ // Device is on a version less than 5.2.1
					if(isNewerVersion(fileVersion, ANDROID_PRE_REQ1)){ // If we're trying to send a version higher than 5.2.1
						return; // Don't send it
					}
				}
				// This is a temporary hack to avoid 5.4 or above going to devices that aren't on 5.3.3b2 yet
				else if(isNewerVersion(ANDROID_PRE_REQ2, deviceVersion)){ // Device is on a version less than 5.3.3b2
					if(isNewerVersion(fileVersion, ANDROID_PRE_REQ3)){ // If we're trying to send a version higher than 5.3.9
						return; // Don't sent it
					}
				}
			}else{
				return;
			}
		}else if( this.getOsVersion() != null && this.getOsVersion().contains(Constants.ARCH) ){
			if(versionFilename.startsWith(Constants.ARCH.toLowerCase()) == false){
				if(versionFilename.startsWith(Constants.ANDROID.toLowerCase()) == false){
					// Allow versions prior to 5.4.0 to go down to arch
					if(isNewerVersion(fileVersion, ANDROID_PRE_REQ3)){ // 5.3.9 is a dummy version implying anything less than 5.4.0b1
						return;
					}
				}else{
					return;
				}
			}
			// We're dealing with an arch specific release
			else{
				// Make sure the device is on 5.3.3b5
				if(isNewerVersion("5.3.3b5", this.getVersion())){
					return;
				}
			}
		}else if(versionFilename.startsWith(Constants.ANDROID.toLowerCase()) || versionFilename.startsWith(Constants.ARCH.toLowerCase())){
			return;
		}
		
		// If the version of the given file is newer than the devices' version
		if( isNewerVersion( fileVersion, deviceVersion) )
		{
			// Make sure not to send a 4.x or above release to a 3.x device since such an upgrade is not allowed via a device release
			if(deviceVersion.startsWith("3.") && Integer.parseInt(fileVersion.substring(0, fileVersion.indexOf("."))) >= 4){
				return;
			}
				
			String params = DispatcherConstants.DEVICE_RELEASES_DIRECTORY +"/"+ versionFilename;
			List<DeviceCommand> existingGetFileDeviceCommands = DeviceCommand.getDeviceCommands( this, DeviceCommandType.GET_FILE.getPersistentValue(), params, false );			
			
			// If there is already a getFile device command for this device/device release,			
			if( existingGetFileDeviceCommands.size() > 0 )
			{
				/*
				 * Determine if at least one of those device commands is set to a status of success
				 * (normally there would be only one device command for this device release, but it is possible there would be more than one)
				 */
				boolean foundSuccessfulGetFileDeviceCommand = false;
				for( DeviceCommand existingGetFileDeviceCommand : existingGetFileDeviceCommands ){
					if( existingGetFileDeviceCommand.getStatus() != null 
							&& existingGetFileDeviceCommand.getStatus().toString().equalsIgnoreCase( StatusType.SUCCESS.toString() ) ){
						foundSuccessfulGetFileDeviceCommand = true;
						break;
					}
				}
				
				// If at least one of those device commands is set to a status of SUCCESS
				if( foundSuccessfulGetFileDeviceCommand )
				{
					/*
					 * And there is not yet an installDeviceRelease device command for this device/device release
					 * NOTE: We want to consider FAILED installDeviceRelease device commands in this query -- 
					 * meaning, if this device has already failed to install this device release, we do not want to retry the installation.
					 * Remember -- if a device does not have the required prereqs, it will not report a status of FAILED to the server.
					 * A FAILED installDeviceRelease will be very rare, and we should legitametly not retry. If we did not consider
					 * FAILED device commands in this query, the device would continue to receive a new installDeviceRelease 
					 * device command on every heartbeat (which would, in turn, continue to get set to FAILED).
					 */
					boolean installDeviceCommandExists = DeviceCommand.getDeviceCommands( this, DeviceCommandType.INSTALL_DEVICE_RELEASE.getPersistentValue(), params, true ).size() > 0 ? true : false;
					if( installDeviceCommandExists == false )
					{
						Calendar now = Calendar.getInstance();
						now.add( Calendar.SECOND, this.getTimezoneAdjustment() );
						Integer hour = now.get(Calendar.HOUR_OF_DAY);
						if( this.getInstallReleaseHour() == null || hour.intValue() == this.getInstallReleaseHour() ) {
							if( this.getInstallReleaseHour() == null ) {
								System.out.println("Device " + this.getDeviceName()+ ": No Release hour set");
							} else {
								System.out.println("Device " + this.getDeviceName()+ ": Release hour " + hour.intValue() + " matches installReleaseHour " + this.getInstallReleaseHour().intValue() );
							}
							this.addDeviceCommand( DeviceCommandType.INSTALL_DEVICE_RELEASE, params, false, ignoreEdgeServer, false, null );												
						} else {
							System.out.println("Device " + this.getDeviceName()+ ": Release hour " + hour.intValue() + " does not match installReleaseHour " + this.getInstallReleaseHour().intValue() );
						}
					}	
				}				
			}
			else
			{				
				// If this device has an edge server
				if( this.getEdgeServer() != null )
				{
					// If there is not yet a getFile device command for this edgeServer/device release					
					boolean getFileDeviceCommandExistsForEdgeServer = DeviceCommand.deviceCommandExists( this.getEdgeServer(), DeviceCommandType.GET_FILE.getPersistentValue(), params );
					if( getFileDeviceCommandExistsForEdgeServer == false )
					{
						// Make sure its not in queue
						if(DeviceCommandCreator.doesDeviceCommandExist(this.getEdgeServer().getDeviceId(), DeviceCommandType.GET_FILE.getPersistentValue(), params) == false){
							// Add the getFile device command for this edgeServer/device release
							this.getEdgeServer().addDeviceCommand( DeviceCommandType.GET_FILE, params, false, ignoreEdgeServer, true, FileTransmissionType.DEVICE_RELEASE );
						}
					}
					/*
					 * If there is already a device command for this edgeServer/device release with a status of SUCCESS,
					 * (and the node does not already have a device command for this device release), add the device command for the node.
					 * Generally, the ServerCommandProcessor will take care of this scenario when setting the device command for the
					 * edge server to SUCCESS (it will find all the edge servers' nodes and add device commands at that time).
					 * However, in situations where the Auto-Update property is changed after the edge server reports SUCCESS,
					 * we would need this block of code.
					 */
					else if( DeviceCommand.getDeviceCommands( this.getEdgeServer(), DeviceCommandType.GET_FILE, StatusType.SUCCESS, params ).size() > 0 )
					{
						// Make sure its not in queue
						if(DeviceCommandCreator.doesDeviceCommandExist(this.getDeviceId(), DeviceCommandType.GET_FILE.getPersistentValue(), params) == false){
							this.addDeviceCommand( DeviceCommandType.GET_FILE, params, false, true, true, FileTransmissionType.DEVICE_RELEASE );
						}
					}
				}
				else
				{
					// Make sure its not in queue
					if(DeviceCommandCreator.doesDeviceCommandExist(this.getDeviceId(), DeviceCommandType.GET_FILE.getPersistentValue(), params) == false){
						// Add the getFile device command for this device/device release
						this.addDeviceCommand( DeviceCommandType.GET_FILE, params, false, true, true, FileTransmissionType.DEVICE_RELEASE );
					}
				}
			}			
		}			
	}
	
	/**
	 * Determines if the given newFileName is "newer" than the given oldFileName
	 * 
	 * @param newFileName
	 * @param oldFileName
	 * @return
	 */
	public static boolean isNewerVersion(String newFileName, String oldFileName){
		
		boolean needsVersion = false;
		
		/*
		 * Iterate through the major/minor/patch versions
		 * until we get the the end of both the file and device versions
		 */
		while( newFileName.length() > 0 || oldFileName.length() > 0 )
		{			
			// Get the next secion of the version
			String newFileNamePart = newFileName;
			String oldFileNamePart = oldFileName; 
			if( newFileName.indexOf(".") > 0 ){
				newFileNamePart = newFileName.substring( 0, newFileName.indexOf(".") );
			}
			if( oldFileName.indexOf(".") > 0 ){
				oldFileNamePart = oldFileName.substring( 0, oldFileName.indexOf(".") );
			}
			
			// Compare this section of each the file version and the device version
			int compareResult = Files.compareVersionSection( newFileNamePart, oldFileNamePart );
			
			// If the file version is less than the device version -- the device does not need this file -- stop comparing
			if( compareResult < 0 ){
				break;
			}
			// If the file version is greater than the device version -- the device does need this file -- stop comparing
			else if( compareResult > 0 ){
				needsVersion = true;
				break;
			}
			
			// If we've gotten this far, go to the next section of the version
			if( newFileName.indexOf(".") > 0 ){			
				newFileName = newFileName.substring( newFileName.indexOf(".") + 1 );
			}else{
				newFileName = "";
			}
			if( oldFileName.indexOf(".") > 0 ){			
				oldFileName = oldFileName.substring( oldFileName.indexOf(".") + 1 );
			}else{
				oldFileName = "";
			}
		}
		return needsVersion;
	}
	
	/**
	 * Creates a MacAddrSchema record and creates a device with the given macAddr
	 * @param macAddr
	 */
	public static synchronized Device autoRegister(String macAddr) throws InterruptedException, ClassNotFoundException, CloneNotSupportedException, ParseException
	{
		// Create a new MacAddrSchema object for this macAddr if necessary
		MacAddrSchema macAddrSchema = MacAddrSchema.getMacAddrSchema( macAddr );
		if( macAddrSchema == null ){
			MacAddrSchema.create( macAddr, SchemaDirectory.getSchema().getSchemaName() );
		}
		
		// If there is not already an existing device with the given madAddr
		Device device = Device.getDeviceByMacAddr( macAddr );
		if( device == null )
		{
			// If we're not allowing the device to "win" and there's a template device exists for this schema
			boolean copiedTemplateDevice = false;
			Schema schema = Schema.getSchema( SchemaDirectory.getSchema().getSchemaName() );
			if( schema != null 
				&& ( schema.getUseDeviceProperties() != null && schema.getUseDeviceProperties() == Boolean.FALSE )
				&& ( schema.getTemplateDeviceId() != null ) )
			{
				// Attempt to locate a device with the given templateDeviceId
				Device templateDevice = Device.getDevice( schema.getTemplateDeviceId() );
				if( templateDevice != null )
				{
					// Copy the template device
					logger.info("Copying template device: "+ templateDevice.getDeviceName());
					device = templateDevice.copy( templateDevice.getDeviceName() +" - "+ macAddr, macAddr );
					copiedTemplateDevice = true;
				}
			}
			
			if( copiedTemplateDevice == false )
			{
				// Create device using default values
				device = Device.create( macAddr, macAddr, new Integer(Constants.ADDITIONAL_CONTENT_AMOUNT_DEFAULT), new Integer(Constants.SCHEDULING_HORIZON_DEFAULT), 
						ContentUpdateType.NETWORK.getPersistentValue(), Boolean.TRUE, Boolean.TRUE);
			}
		}else{
			logger.info("Device with given mac address already exists. Cancelling auto-registration.");
		}
		return device;
	}
	
	/**
	 * Called from DispatcherSoalBindingImpl.logFileUploadComplete().
	 * Moves the given file to the screenshots directory
	 * @param advancedPropertiesFile
	 */
	public static void handleScreenshotFileUpload(File screenshotFile)
	{
		try
		{							
			if( screenshotFile.exists() )
			{
				// Parse the deviceId out of the filename
				String deviceId = screenshotFile.getName().substring( screenshotFile.getName().indexOf( "_" ) + 1 );
				deviceId = deviceId.substring( 0, deviceId.indexOf( "_" ) );
				Device device = Device.getDevice( new Long( deviceId ) );
				
				// Make sure that this screenshot is not more than 5 mins into the future
				String screenshotDate = screenshotFile.getName().substring( screenshotFile.getName().lastIndexOf("_") + 1 );
				screenshotDate = screenshotDate.substring( 0, screenshotDate.lastIndexOf(".") );
				SimpleDateFormat dateFormat = new SimpleDateFormat( DispatcherConstants.UPLOAD_FILE_DATE_FORMAT );
				Date dateScreenshot = dateFormat.parse( screenshotDate );
				Date now = new Date();
				
				// If this screenshot is more than 5 mins into the future
				if( ((dateScreenshot.getTime() - now.getTime()) / 1000) - device.getTimezoneAdjustment() > 300 ){
					logger.info("Deleting screenshot " + screenshotFile.getName() + " since it is more than 5 mins into the future.");
					screenshotFile.delete();
					return;
				}
				
				// Move it to the screenshots directory
				String schemaDir = KuvataConfig.getKuvataHome() +"/"+ DispatcherConstants.SCHEMAS_DIRECTORY +"/"+ SchemaDirectory.getSchema().getSchemaName();
				String screenshotsPath = schemaDir +"/"+ Constants.SCREENSHOTS_DIRECTORY +"/"+ screenshotFile.getName();				
				File f = new File( screenshotsPath );
				
				logger.info("Moving screenshot file from "+ screenshotFile.getAbsolutePath() +" to "+ f.getAbsolutePath() );
				Files.renameTo( screenshotFile, f );	
				
				// Create a thumbnail of the screenshot and put it in the /thumbs directory
				String thumbFullpath = f.getAbsolutePath().substring( 0, f.getAbsolutePath().lastIndexOf("/") );
				thumbFullpath += "/"+ Constants.THUMBS_DIR +"/"+ f.getName();
				KMMServlet.createThumbnail( Asset.MAX_DIMENSION, f.getAbsolutePath(), thumbFullpath );					
								
				if( device != null )
				{					
					// Remove the current screenshot file if found, and if it is has a different name than the screenshot we just moved into the folder 
					if( device.getScreenshot() != null && device.getScreenshot().length() > 0 && device.getScreenshot().equalsIgnoreCase( f.getAbsolutePath() ) == false ){
						String currentScreenshotThumbpath = device.getScreenshot().substring( 0, device.getScreenshot().lastIndexOf("/") );
						currentScreenshotThumbpath += "/"+ Constants.THUMBS_DIR +"/"+ device.getScreenshot().substring( device.getScreenshot().lastIndexOf("/") + 1 );
						File currentScreenshot = new File( device.getScreenshot() );
						File currentScreenshotThumb = new File( currentScreenshotThumbpath );
						if( currentScreenshot.exists() ){
							logger.info("Deleting existing screenshot file: "+ currentScreenshot.getAbsolutePath() );
							currentScreenshot.delete();
						}
						if( currentScreenshotThumb.exists() ){
							logger.info("Deleting existing screenshot thumbnail: "+ currentScreenshotThumb.getAbsolutePath() );
							currentScreenshotThumb.delete();
						}						
					}					 
					device.setScreenshot( f.getAbsolutePath() );					
					device.update();
				}else{
					logger.info("Could not locate device: "+ deviceId +". Unable to handle screenshot file upload.");
				}
			} else {
				logger.info("Could not locate specified screenshot file: "+ screenshotFile.getAbsolutePath() +". Unable to continue.");
			}					
		}
		catch(Exception e) {
			logger.error( e );
		} finally {
			// Delete the log file		
			if( screenshotFile != null && screenshotFile.isDirectory() == false ){				
				screenshotFile.delete();
			}
		}		
	}	
	
	/**
	 * Called from heartbeat implementation on first heartbeat of this device.
	 * Deletes any non-mcm propertyChange device commands for this device
	 *
	 */
	public void deleteDeviceCommandsOnFirstHeartbeat()
	{
		String hql = "SELECT dc FROM DeviceCommand as dc "
			+ "WHERE dc.device.deviceId = "+ this.getDeviceId().toString() +" "
			+ "AND dc.command IN ('"+ DeviceCommandType.PROPERTY_CHANGE_COMMAND.getPersistentValue() +"')";
		Session session = HibernateSession.currentSession();
		List<DeviceCommand> l = session.createQuery( hql ).list();
		
		// Only delete non-mcm propertyChange commands
		for( Iterator<DeviceCommand> i = l.iterator(); i.hasNext(); )
		{
			DeviceCommand dc = i.next();
			String params = dc.getParameters();
			
			// Count the number of commas in the parameters field
			// NOTE: We can't use split() here in case the third parameter is "". split() will not count it.
			int numCommas = 0;
			if( params != null && params.length() > 0 )
			{				
				for( int j=0; j<params.length(); j++){
					char c = params.charAt(j);
					if( String.valueOf(c).equalsIgnoreCase(",") ){
						numCommas++;
					}
				}
			}
			
			// If we did not find 2 commas, it must be not be an mcm propertyChange command -- delete the deviceCommand
			if( numCommas != 2 ){
				dc.delete();
			}
		}
	}
	
	/**
	 * This method is called when updating a device object from the interface or from the KMM web services.
	 * (as opposed to DeviceProperty.updateProperties() which is called when updating a device object from the dispatcher)
	 * 
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	public void updateProperties(String selectedRotation, String selectedScalingMode, String selectedOutputMode,
			String screenshotUploadTime, String filesyncStartTime, String filesyncEndTime, String selectedTimezone,
			String selectedResolution, String heartbeatInterval, String maxFileStorage, 
			Boolean dhcpEnabled, String ipAddr, String connectAddr, String gateway, String netmask, String networkInterface, String dispatcherServers,
			String dnsServer, String selectedEdgeServer, Boolean redirectGateway, String lcdPin, String lcdBranding, String volume, String iowaitThreshold, 
			String cpuThreshold, String memoryThreshold, String loadThreshold, String selectedDisplay, String selectedAutoUpdate, String selectedMasterDevice,
			String csHorizon, String csLength, String billingStatus, String contentUpdateType, Boolean applyAlerts, Integer xOffset, Integer yOffset, 
			Integer zoom, Boolean multicastEnabled, String receiveSiteId, String diskCapacity, String diskUsage, String version, String osVersion, 
			String antivirusScan, Integer downloadPriority, String audioNormalization, String framesync, String multicastAddress, Integer multicastServerPort, 
			Integer multicastDevicePort, String control, String alphaCompositing, Integer bandwidthLimit, String audioConnection, Boolean type2VideoPlayback, 
			Boolean useChrome,  Boolean chromeDisableGpu, Boolean deviceSideScheduling, boolean isUpdateFromDevice, boolean isFirstHeartbeatUpload, 
			boolean createDeviceCommandsNow) throws InterruptedException, ParseException
	{
		DisplayRotationType deviceRotation = DisplayRotationType.getDisplayRotationType( selectedRotation );
		DeviceScalingModeType deviceScalingMode = DeviceScalingModeType.getDeviceScalingModeTypeByPersistentValue( selectedScalingMode );
		OutputModeType outputMode = OutputModeType.getOutputModeTypeByPersistentValue( selectedOutputMode );		
		DeviceAutoUpdateType autoUpdate = DeviceAutoUpdateType.getDeviceAutoUpdateTypeByPersistentValue( selectedAutoUpdate );
		AudioConnectionType selectedAudioConnection = AudioConnectionType.getAudioConnectionType(audioConnection);
		FramesyncType framesyncType = FramesyncType.getFramesyncType(framesync);
		SimpleDateFormat timeFormat = new SimpleDateFormat( Constants.TIME_FORMAT );
		SimpleDateFormat extendedTimeFormat = new SimpleDateFormat( Constants.EXTENDED_TIME_FORMAT );
		Date dateScreenshotUploadTime = null;
		if( screenshotUploadTime != null && screenshotUploadTime.length() > 0 ){
			// We're only concerned about the time. We allow screenshot upload time in both formats (including and excluding seconds)
			try {
				dateScreenshotUploadTime = extendedTimeFormat.parse(screenshotUploadTime);
			} catch (ParseException e) {
				dateScreenshotUploadTime = timeFormat.parse(screenshotUploadTime);
			}
		}
		Date dateFilesyncStartTime = null;
		if( filesyncStartTime != null && filesyncStartTime.length() > 0 && filesyncStartTime.equalsIgnoreCase(Constants.OFF) == false){
			// We're only concerned about the time. We allow filesync time in both formats (including and excluding seconds)
			try {
				dateFilesyncStartTime = extendedTimeFormat.parse( filesyncStartTime );
			} catch (Exception e) {
				dateFilesyncStartTime = timeFormat.parse( filesyncStartTime );
			}
		}
		Date dateFilesyncEndTime = null;
		if( filesyncEndTime != null && filesyncEndTime.length() > 0 ){
			// We're only concerned about the time. We allow filesync time in both formats (including and excluding seconds)
			try {
				dateFilesyncEndTime = extendedTimeFormat.parse( filesyncEndTime );
			} catch (Exception e) {
				dateFilesyncEndTime = timeFormat.parse( filesyncEndTime );
			}
		}		
		
		// Null out the connectAddr if an emtpy string was passed in -- this will allow propertyHasChanged to function as expected
		if( connectAddr != null && connectAddr.length() == 0 ){
			connectAddr = null;
		}
		
		// If any of the values have changed, issue propertyChange device command
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getTimezone(), selectedTimezone, DevicePropertyType.TIMEZONE, null, true ) ){
			this.setTimezone( selectedTimezone );
		}					
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getHeartbeatInterval(), heartbeatInterval, DevicePropertyType.HEARTBEAT_INTERVAL, null, true ) ){			
			this.setHeartbeatInterval( heartbeatInterval );
		}		
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getRotation(), deviceRotation, DevicePropertyType.ROTATION, null, true ) ){	
			this.setRotation( deviceRotation );							
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getScalingMode(), deviceScalingMode, DevicePropertyType.SCALING_MODE, null, true ) ){		
			this.setScalingMode( deviceScalingMode );							
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getDhcpEnabled(), dhcpEnabled, DevicePropertyType.DHCP_ENABLED, null, true ) ){
			this.setDhcpEnabled( dhcpEnabled );						
		}				
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getIpAddr(), ipAddr, DevicePropertyType.IP_ADDRESS, null, true ) ){
			this.setIpAddr( ipAddr );
		}
		if( propertyHasChanged( this.getConnectAddr(), connectAddr, false ) )
		{
			this.setConnectAddr( connectAddr );
			
			// Update the edgeServer property on each node device
			for( Iterator<Device> i=this.getEdgeDevices().iterator(); i.hasNext(); )
			{				
				// If the connectAddr was just set to blank
				Device nodeDevice = i.next();
				String edgeServerIp = null;
				if( this.getConnectAddr() == null || this.getConnectAddr().length() == 0 )
				{
					// Send down the ipAddr of the edgeServer to the node
					edgeServerIp = this.getIpAddr();
				}else{
					edgeServerIp = this.getConnectAddr();
				}
				nodeDevice.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.EDGE_SERVER.getPropertyName() +","+ Reformat.escape(edgeServerIp), true );				
			}
		}		
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getGateway(), gateway, DevicePropertyType.GATEWAY, null, true ) ){	
			this.setGateway( gateway );
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getNetmask(), netmask, DevicePropertyType.NETMASK, null, true ) ){		
			this.setNetmask( netmask );
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getNetworkInterface(), networkInterface, DevicePropertyType.NETWORK_INTERFACE, null, true ) ){
			this.setNetworkInterface( networkInterface );
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getDispatcherServers(), dispatcherServers, DevicePropertyType.DISPATCHER_SERVERS, null, true ) ){
			this.setDispatcherServers( dispatcherServers );
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getDnsServer(), dnsServer, DevicePropertyType.DNS_SERVER, null, true ) ){
			this.setDnsServer( dnsServer );
		}								
				
		// If this is an update from the device -- allow the edge server property to be "blanked" out
		Device savedEdgeServer = this.getEdgeServer();
		boolean edgeServerHasChanged = false;
		if( isUpdateFromDevice && this.getEdgeServer() != null && (selectedEdgeServer == null || selectedEdgeServer.length() == 0 || selectedEdgeServer.equalsIgnoreCase( Constants.NULL ))){
			this.setEdgeServer( null );
			edgeServerHasChanged = true;
		}
		// If we are removing the edge server from the ingester
		else if(selectedEdgeServer != null && selectedEdgeServer.equalsIgnoreCase( Constants.NULL ) && propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getEdgeServer(), null, DevicePropertyType.EDGE_SERVER, null, true )){
			this.setEdgeServer( null );
			edgeServerHasChanged = true;
		}
		// See if the property has changed
		else if( selectedEdgeServer != null && !selectedEdgeServer.equalsIgnoreCase( Constants.NULL ) && propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getEdgeServer(), selectedEdgeServer, DevicePropertyType.EDGE_SERVER, null, true ) ){
			this.setEdgeServer( Device.getDevice( Long.valueOf( selectedEdgeServer ) ) );
			edgeServerHasChanged = true;
			
			String edgeServerIp = null;
			if( this.getEdgeServer() != null )
			{
				// If the connectAddr is set for this edge server -- use it
				// Otherwise, use the ipAddr of the edge server
				if( this.getEdgeServer().getConnectAddr() != null && this.getEdgeServer().getConnectAddr().length() > 0 ){
					edgeServerIp = this.getEdgeServer().getConnectAddr();	
				}else{
					edgeServerIp = this.getEdgeServer().getIpAddr();
				}
			}
			this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.EDGE_SERVER.getPropertyName() +","+ Reformat.escape(edgeServerIp), false, false, false, null, true );
		}
		// If the edge server property was just changed for this device
		if( edgeServerHasChanged )
		{
			// If the edge server property was just cleared out for this device
			if( edgeServer == null )
			{
				// Perform all actions required when un-assigning an edge server from a device
				logger.info("Unassigning edge server: "+ savedEdgeServer.getDeviceId());
				this.unassignEdgeServer( savedEdgeServer );
			}
			else
			{
				// If this was the first device added as a node to the selected edge server
				if( edgeServer.getOtherEdgeDevices(this).size() == 0 )
				{
					// Disconnect the edge servers' OpenVpn client connection using OpenVpn's management interface.
					// The edge server will then reconnect and execute OpenVpnConnect which will assign edgeServerOpenvpnHostIp, etc.
					edgeServer.disconnectOpenVpnClient();
				}
			}				
		}			
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getRedirectGateway(), redirectGateway, DevicePropertyType.REDIRECT_GATEWAY, null, true ) ){
			this.setRedirectGateway( redirectGateway );		
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getxOffset(), xOffset, DevicePropertyType.X_OFFSET, null, true ) ){
			this.setxOffset( xOffset );
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getyOffset(), yOffset, DevicePropertyType.Y_OFFSET, null, true ) ){
			this.setyOffset( yOffset );
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getZoom(), zoom, DevicePropertyType.ZOOM, null, true ) ){
			this.setZoom( zoom );
		}						
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getLcdPin(), lcdPin, DevicePropertyType.LCD_PIN, null, true ) ){
			this.setLcdPin( lcdPin );							
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getLcdBranding(), lcdBranding, DevicePropertyType.LCD_BRANDING, null, true ) ){
			this.setLcdBranding( lcdBranding );						
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getVolume(), volume, DevicePropertyType.VOLUME, null, true ) ){
			this.setVolume( volume );						
		}		
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getIowaitThreshold(), iowaitThreshold, DevicePropertyType.IOWAIT_THRESHOLD, null, true ) ){
			this.setIowaitThreshold( iowaitThreshold != null && iowaitThreshold.length() > 0 ? Float.valueOf( iowaitThreshold ) : null );
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getCpuThreshold(), cpuThreshold, DevicePropertyType.CPU_THRESHOLD, null, true ) ){
			this.setCpuThreshold( cpuThreshold != null && cpuThreshold.length() > 0 ? Float.valueOf( cpuThreshold ) : null );
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getMemoryThreshold(), memoryThreshold, DevicePropertyType.MEMORY_THRESHOLD, null, true ) ){
			this.setMemoryThreshold( memoryThreshold != null && memoryThreshold.length() > 0 ? Float.valueOf( memoryThreshold ) : null );
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getLoadThreshold(), loadThreshold, DevicePropertyType.LOAD_THRESHOLD, null, true ) ){
			this.setLoadThreshold( loadThreshold != null && loadThreshold.length() > 0 ? Float.valueOf( loadThreshold ) : null );
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getOutputMode(), outputMode, DevicePropertyType.OUTPUT_MODE, null, true ) ){
			this.setOutputMode( outputMode);						
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getAutoUpdate(), autoUpdate, DevicePropertyType.AUTO_UPDATE, null, true ) ){
			this.setAutoUpdate( autoUpdate);			
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getScreenshotUploadTime(), dateScreenshotUploadTime, DevicePropertyType.SCREENSHOT_UPLOAD_TIME, null, true ) ){	
			this.setScreenshotUploadTime( dateScreenshotUploadTime );
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getFilesyncStartTime(), dateFilesyncStartTime, DevicePropertyType.FILESYNC_START_TIME, null, true ) ){		
			this.setFilesyncStartTime( dateFilesyncStartTime );							
		}			
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getFilesyncEndTime(), dateFilesyncEndTime, DevicePropertyType.FILESYNC_END_TIME, null, true ) ){	
			this.setFilesyncEndTime( dateFilesyncEndTime );	
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getDiskCapacity(), diskCapacity, DevicePropertyType.DISK_CAPACITY, null, true ) ){	
			this.setDiskCapacity( diskCapacity );							
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getDiskUsage(), diskUsage, DevicePropertyType.DISK_USAGE, null, true ) ){	
			this.setDiskUsage( diskUsage);							
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getMaxFileStorage(), maxFileStorage, DevicePropertyType.MAX_FILE_STORAGE, null, true ) ){			
			this.setMaxFileStorage( maxFileStorage != null && maxFileStorage.length() > 0 ? Float.valueOf(maxFileStorage) : null );
		}
		
		// If this is an update from the device,
		// and maxFileStorage is blank on both server and device
		if( isUpdateFromDevice && this.getMaxFileStorage() == null && (maxFileStorage == null || maxFileStorage.length() == 0) )
		{
			// And we have a value for disk capacity
			if( this.getDiskCapacity() != null && this.getDiskCapacity().length() > 0 )
			{
				// Calculate maxFileStorage (in GB) by taking 90% of diskCapacity 				
				Double calculatedMaxFileStorage = Double.valueOf( (Reformat.convertStringToBytes( this.getDiskCapacity() ) * .9) / Constants.GIGABYTE );
				this.setMaxFileStorage( calculatedMaxFileStorage.floatValue() );
				
				// Send down this calculated maxFileStorage to the device 
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.MAX_FILE_STORAGE.getPropertyName() +","+ calculatedMaxFileStorage.toString(), false, false, false, null, true );
			}
		}		
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getAntivirusScan(), antivirusScan, DevicePropertyType.ANTIVIRUS_SCAN, null, true ) ){	
			this.setAntivirusScan( antivirusScan );							
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getAudioNormalization(), audioNormalization, DevicePropertyType.AUDIO_NORMALIZATION, null, true ) ){	
			this.setAudioNormalization(audioNormalization);							
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getFramesync(), framesyncType, DevicePropertyType.FRAMESYNC, null, true ) ){	
			this.setFramesync(framesyncType);							
		}
		// NOTE: The display property must be set BEFORE the resolution property is set.
		// Otherwise, the device could potentially attempt to set its resolution to an "unsupported" resolution (based on the selected display)
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getDisplay(), selectedDisplay, DevicePropertyType.DISPLAY, null, true ) ){
			this.setDisplay( selectedDisplay );																
		}	
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getResolution(), selectedResolution, DevicePropertyType.RESOLUTION, null, true ) ){
			this.setResolution( selectedResolution );
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getAlphaCompositing(), alphaCompositing, DevicePropertyType.ALPHA_COMPOSITING, null, true ) ){
			this.setAlphaCompositing(alphaCompositing);
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getBandwidthLimit(), bandwidthLimit, DevicePropertyType.BANDWIDTH_LIMIT, null, true ) ){
			this.setBandwidthLimit(bandwidthLimit);
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getAudioConnection(), selectedAudioConnection, DevicePropertyType.AUDIO_CONNECTION, null, true ) ){
			this.setAudioConnection(selectedAudioConnection);
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getType2VideoPlayback(), type2VideoPlayback, DevicePropertyType.TYPE2_VIDEO_PLAYBACK, null, true ) ){
			this.setType2VideoPlayback(type2VideoPlayback);
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getUseChrome(), useChrome, DevicePropertyType.USE_CHROME, null, true ) ){
			this.setUseChrome(useChrome);
		}
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getChromeDisableGpu(), chromeDisableGpu, DevicePropertyType.CHROME_DISABLE_GPU, null, true ) ){
			this.setChromeDisableGpu(chromeDisableGpu);
		}
				
		/* 			Multicast Network Properties
		 * We will only accept these properties from the device
		 * if we are able to locate a valid multicast network on
		 * the server with the same properties.
		 */
		String currentMulticastAddress = null;
		Integer currentMulticastServerPort = null;
		Integer currentMulticastDevicePort = null;
		if(this.getMulticastNetwork() != null){
			currentMulticastAddress = this.getMulticastNetwork().getMulticastAddress();
			currentMulticastServerPort = this.getMulticastNetwork().getServerPort();
			currentMulticastDevicePort = this.getMulticastNetwork().getDevicePort();
		}
		
		// Execute propertyHasChanged() for all properties since it takes care of sending device commands if needed
		boolean multicastPropertiesHaveChanged = propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, currentMulticastAddress, multicastAddress, DevicePropertyType.MULTICAST_ADDRESS, null, true );
		multicastPropertiesHaveChanged = propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, currentMulticastServerPort, multicastServerPort, DevicePropertyType.MULTICAST_SERVER_PORT, null, true ) || multicastPropertiesHaveChanged;
		multicastPropertiesHaveChanged = propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, currentMulticastDevicePort, multicastDevicePort, DevicePropertyType.MULTICAST_DEVICE_PORT, null, true ) || multicastPropertiesHaveChanged;
				
		// If this is the first heartbeat and we are to accept properties from device
		if(multicastPropertiesHaveChanged && isFirstHeartbeatUpload && this.getInitPropertiesFromDevice().equals(Boolean.TRUE)){
			// Locate a multicast network for the properties uploaded by the device
			MulticastNetwork mn = MulticastNetwork.getMulticastNetwork(multicastAddress, multicastServerPort, multicastDevicePort);
			
			// If we located a valid multicast network
			if(mn != null){
				this.setMulticastNetwork(mn);
			}
			// Send down device commands to the device 
			else{
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.MULTICAST_ADDRESS.getPropertyName() +","+ currentMulticastAddress, false, false, false, null, true );
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.MULTICAST_SERVER_PORT.getPropertyName() +","+ currentMulticastServerPort.toString(), false, false, false, null, true );
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.MULTICAST_DEVICE_PORT.getPropertyName() +","+ currentMulticastDevicePort.toString(), false, false, false, null, true );
			}
		}
		
		// We need to disable the devices filter as we need to allow users to
		// set their active mirror sources to devices they don't have permissions to.
		// This should be done only for user requests with a valid kmfSession
		KmfSession kmfSession = KmfSession.getKmfSession();
		boolean enableFilter = false;
		if(kmfSession != null && kmfSession.getCurrentAppUser() != null && (kmfSession.getCurrentAppUser().getUniversalDataAccess() == null || kmfSession.getCurrentAppUser().getUniversalDataAccess().booleanValue() == false) ){
			FilterManager.disableFilter(FilterType.DEVICES_FILTER);
			enableFilter = true;
		}
		
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getMirrorSource(), selectedMasterDevice, DevicePropertyType.MASTER_DEVICE_ID, null, true ) ){		
			if( selectedMasterDevice != null && selectedMasterDevice.length() > 0 ){
				Device masterDevice = Device.getDevice( new Long( selectedMasterDevice ) );
				this.setMirrorSource( masterDevice );
		
				String masterDeviceId = null;
				if( this.getMirrorSource() != null ){
					masterDeviceId = this.getMirrorSource().getDeviceId().toString();
				}
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.MASTER_DEVICE_ID.getPropertyName() +","+ masterDeviceId, false, false, false, null, true );
			}
		}
				
		if(enableFilter){
			// Enable the filter now
			FilterManager.enableFilter(FilterType.DEVICES_FILTER);
		}
				
		/*
		 * These property are not propogated to the device
		 */
		if( propertyHasChanged( this.getReadableBillingStatus(), billingStatus ) ){
			this.setBillingStatus(billingStatus, true);
		}
		if( propertyHasChanged( this.getApplyAlerts(), applyAlerts ) ){		
			this.setApplyAlerts( applyAlerts );										
		}
		if( propertyHasChanged( this.getContentSchedulingHorizon(), csHorizon ) ){		
			this.setContentSchedulingHorizon( new Integer( csHorizon ) );									
		}	
		if( propertyHasChanged( this.getAdditionalContentAmount(), csLength ) ){		
			this.setAdditionalContentAmount( new Integer( csLength ) );							
		}			
		if( propertyHasChanged( this.getOsVersion(), osVersion ) ){		
			this.setOsVersion( osVersion );							
		}
		if( propertyHasChanged( this.getDownloadPriority(), downloadPriority ) ){		
			this.setDownloadPriority(downloadPriority);
		}
		if( propertyHasChanged( this.getControl(), control ) ){		
			this.setControl(control);
		}
		if( propertyHasChanged( this.getDeviceSideScheduling(), deviceSideScheduling ) ){		
			this.setDeviceSideScheduling(deviceSideScheduling);
		}
		
		// We need to set apply alerts before we can set contentUpdateType on the device since applyAlerts could change based on the contentUpdateType value
		if( propertyHasChanged( isUpdateFromDevice, isFirstHeartbeatUpload, this.getContentUpdateType(), contentUpdateType, DevicePropertyType.CONTENT_UPDATE_TYPE, null, true ) ){
			this.setContentUpdateType(contentUpdateType);
		}
		
		// Always use the version as reported from the device
		if( isFirstHeartbeatUpload || this.getVersion() == null || propertyHasChanged( this.getVersion(), version ) ){
			this.setVersion( version );
		}
				
		this.update();
	}	
				
	/**
	 * Parses the given DOM and updates each device property on the server, if necessary
	 * 
	 * @param doc
	 * @param isFirstHeartbeatUpload
	 */
	public static void updateProperties(Document doc, boolean isFirstHeartbeatUpload, boolean isApiCall) throws InterruptedException, ParseException
	{
		HibernateSession.startBulkmode();
		SimpleDateFormat timeFormat = new SimpleDateFormat( Constants.TIME_FORMAT );
		SimpleDateFormat extendedTimeFormat = new SimpleDateFormat( Constants.EXTENDED_TIME_FORMAT );
		Element root = doc.getElement( new XPath("//"+ DeviceProperty.PROPERTIES_ROOT_ELEMENT) );
		String deviceId = root.getAttribute( DeviceProperty.DEVICE_ID_ATTRIBUTE );			
		if( deviceId != null && deviceId.length() > 0 )
		{
			Device device = Device.getDevice( new Long( deviceId ) );
			if( device != null ){
				
				// Lock this row at the oracle level to avoid another JVM from modifying this object
				HibernateSession.currentSession().lock(device, LockMode.UPGRADE);
				
				// Do this if it is a first heartbeat upload or we have already processed the first heartbeat upload
				// we don't process property upload files until the first heartbeat has happened unless its an API call
				if( isFirstHeartbeatUpload || device.getInitPropertiesProcessed().equals( Boolean.TRUE ) || isApiCall ){
					/*
					 * First, update all device properties
					 */
					Element e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.ROTATION.getPropertyName() +"\"]" ) );
					String rotation = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getRotation() != null ? device.getRotation().getPersistentValue() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.SCALING_MODE.getPropertyName() +"\"]" ) );
					String scalingMode = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getScalingMode() != null ? device.getScalingMode().getPersistentValue() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.OUTPUT_MODE.getPropertyName() +"\"]" ) );
					String outputMode = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getOutputMode() != null ? device.getOutputMode().getPersistentValue() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.SCREENSHOT_UPLOAD_TIME.getPropertyName() +"\"]" ) );
					String screenshotUploadTime = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getScreenshotUploadTime() != null ? extendedTimeFormat.format(device.getScreenshotUploadTime()) : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.FILESYNC_START_TIME.getPropertyName() +"\"]" ) );
					String filesyncStartTime = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getFilesyncStartTime() != null ? timeFormat.format(device.getFilesyncStartTime()) : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.FILESYNC_END_TIME.getPropertyName() +"\"]" ) );
					String filesyncEndTime = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getFilesyncEndTime() != null ? timeFormat.format(device.getFilesyncEndTime()) : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.TIMEZONE.getPropertyName() +"\"]" ) );
					String timezone = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getTimezone();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.RESOLUTION.getPropertyName() +"\"]" ) );
					String resolution = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getResolution();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.HEARTBEAT_INTERVAL.getPropertyName() +"\"]" ) );
					String heartbeatInterval = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getHeartbeatInterval();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.MAX_FILE_STORAGE.getPropertyName() +"\"]" ) );
					String maxFileStorage = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getMaxFileStorage() != null ? device.getMaxFileStorage().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DHCP_ENABLED.getPropertyName() +"\"]" ) );
					Boolean dhcpEnabled = e != null ? Boolean.valueOf( e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) ): device.getDhcpEnabled();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.NETWORK_INTERFACE.getPropertyName() +"\"]" ) );
					String networkInterface = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getNetworkInterface();					
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DISPATCHER_SERVERS.getPropertyName() +"\"]" ) );
					String dispatcherServers = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getDispatcherServers();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DNS_SERVER.getPropertyName() +"\"]" ) );
					String dnsServer = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getDnsServer();												
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.EDGE_SERVER.getPropertyName() +"\"]" ) );
					String edgeServer = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getEdgeServer() != null ? device.getEdgeServer().getDeviceId().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.REDIRECT_GATEWAY.getPropertyName() +"\"]" ) );
					Boolean redirectGateway = e != null ? Boolean.valueOf( e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) ) : device.getRedirectGateway();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.LCD_PIN.getPropertyName() +"\"]" ) );
					String lcdPin = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getLcdPin();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.LCD_BRANDING.getPropertyName() +"\"]" ) );
					String lcdBranding = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getLcdBranding();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.VOLUME.getPropertyName() +"\"]" ) );
					String volume = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getVolume();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.IOWAIT_THRESHOLD.getPropertyName() +"\"]" ) );
					String iowaitThreshold = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getIowaitThreshold() != null ? device.getIowaitThreshold().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.CPU_THRESHOLD.getPropertyName() +"\"]" ) );
					String cpuThreshold = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getCpuThreshold() != null ? device.getCpuThreshold().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.MEMORY_THRESHOLD.getPropertyName() +"\"]" ) );
					String memoryThreshold = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getMemoryThreshold() != null ? device.getMemoryThreshold().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.LOAD_THRESHOLD.getPropertyName() +"\"]" ) );
					String loadThreshold = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getLoadThreshold() != null ? device.getLoadThreshold().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DISPLAY.getPropertyName() +"\"]" ) );
					String display = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getDisplay();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.AUTO_UPDATE.getPropertyName() +"\"]" ) );
					String autoUpdate = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getAutoUpdate() != null ? device.getAutoUpdate().getPersistentValue() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.X_OFFSET.getPropertyName() +"\"]" ) );
					Integer xOffset = e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0 ? Integer.valueOf( e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) ): device.getxOffset();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.Y_OFFSET.getPropertyName() +"\"]" ) );
					Integer yOffset = e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0 ? Integer.valueOf( e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) ): device.getyOffset();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.ZOOM.getPropertyName() +"\"]" ) );
					Integer zoom = e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0 ? Integer.valueOf( e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) ): device.getZoom();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DISK_CAPACITY.getPropertyName() +"\"]" ) );
					String diskCapacity = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getDiskCapacity();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DISK_USAGE.getPropertyName() +"\"]" ) );
					String diskUsage = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getDiskUsage();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.VERSION.getPropertyName() +"\"]" ) );
					String version = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getVersion();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.OS_VERSION.getPropertyName() +"\"]" ) );
					String osVersion = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getOsVersion();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.ANTIVIRUS_SCAN.getPropertyName() +"\"]" ) );
					String antivirusScan = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getAntivirusScan();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.AUDIO_NORMALIZATION.getPropertyName() +"\"]" ) );
					String audioNormalization = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getAudioNormalization();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.MASTER_DEVICE_ID.getPropertyName() +"\"]" ) );
					String masterDevice = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getMirrorSource() != null ? device.getMirrorSource().getDeviceId().toString() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.CONNECT_ADDRESS.getPropertyName() +"\"]" ) );
					String connectAddress = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getConnectAddr();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.MULTICAST_ADDRESS.getPropertyName() +"\"]" ) );
					String multicastAddress = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0) ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): null;
					if(multicastAddress == null)
							multicastAddress = device.getMulticastNetwork() != null ? device.getMulticastNetwork().getMulticastAddress() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.MULTICAST_SERVER_PORT.getPropertyName() +"\"]" ) );
					Integer multicastServerPort = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0) ? Integer.parseInt(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )): null;
					if(multicastServerPort == null)
						multicastServerPort = device.getMulticastNetwork() != null ? device.getMulticastNetwork().getServerPort() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.MULTICAST_DEVICE_PORT.getPropertyName() +"\"]" ) );
					Integer multicastDevicePort = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0) ? Integer.parseInt(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )): null;
					if(multicastDevicePort == null)
						multicastDevicePort = device.getMulticastNetwork() != null ? device.getMulticastNetwork().getDevicePort() : null;
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.CONTROL.getPropertyName() +"\"]" ) );
					String control = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getControl();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.CONTENT_UPDATE_TYPE.getPropertyName() +"\"]" ) );
					String contentUpdateType = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0) ? ContentUpdateType.getContentUpdateType(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )).getPersistentValue() : device.getContentUpdateType();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.ALPHA_COMPOSITING.getPropertyName() +"\"]" ) );
					String alphaCompositing = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ): device.getAlphaCompositing();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.BANDWIDTH_LIMIT.getPropertyName() +"\"]" ) );
					Integer bandwidthLimit = e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0 ? Integer.valueOf(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )) : device.getBandwidthLimit();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.AUDIO_CONNECTION.getPropertyName() +"\"]" ) );
					String audioConnection = e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0 ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) : device.getAudioConnection().getPersistentValue();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.FRAMESYNC.getPropertyName() +"\"]" ) );
					String framesync = e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ).length() > 0 ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) : device.getFramesync().getPersistentValue();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.TYPE2_VIDEO_PLAYBACK.getPropertyName() +"\"]" ) );
					Boolean type2VideoPlayback = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) != null) ? Boolean.valueOf(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )) : device.getType2VideoPlayback();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.USE_CHROME.getPropertyName() +"\"]" ) );
					Boolean useChrome = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) != null) ? Boolean.valueOf(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )) : device.getUseChrome();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.CHROME_DISABLE_GPU.getPropertyName() +"\"]" ) );
					Boolean chromeDisableGpu = (e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) != null) ? Boolean.valueOf(e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE )) : device.getChromeDisableGpu();
					
					String csHorizon = null;
					String csLength = null;
					Boolean applyAlerts = null;
					Integer downloadPriority = null;
					
					// The following properties are only excepected from an API call
					if(isApiCall){
						// Special case for deviceName since we don't accept this in the updateProperties method
						e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.DEVICE_NAME.getPropertyName() +"\"]" ) );
						if(e != null){
							device.setDeviceName(e.getAttribute(DeviceProperty.VALUE_ATTRIBUTE));
						}
					}
					
					// Read only WiFi properties
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.SSID.getPropertyName() +"\"]" ) );
					if(e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) != null){
						device.setSsid(e.getAttribute(DeviceProperty.VALUE_ATTRIBUTE));
					}
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.ENCRYPTION_TYPE.getPropertyName() +"\"]" ) );
					if(e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) != null){
						device.setEncryptionType(e.getAttribute(DeviceProperty.VALUE_ATTRIBUTE));
					}
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.PASSPHRASE.getPropertyName() +"\"]" ) );
					if(e != null && e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) != null){
						device.setPassphrase(e.getAttribute(DeviceProperty.VALUE_ATTRIBUTE));
					}
					
					// The device can somtimes upload null values for network properties. Never null out the IP, Netmask and gateway
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.IP_ADDRESS.getPropertyName() +"\"]" ) );
					String ipAddress = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) : device.getIpAddr();
					ipAddress = ipAddress != null && ipAddress.length() > 0 ? ipAddress : device.getIpAddr();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.GATEWAY.getPropertyName() +"\"]" ) );
					String gateway = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) : device.getGateway();
					gateway = gateway != null && gateway.length() > 0 ? gateway : device.getGateway();
					e = doc.getElement( new XPath( "//"+ DeviceProperty.PROPERTY_ELEMENT +"[@name=\""+ DevicePropertyType.NETMASK.getPropertyName() +"\"]" ) );
					String netmask = e != null ? e.getAttribute( DeviceProperty.VALUE_ATTRIBUTE ) : device.getNetmask();
					netmask = netmask != null && netmask.length() > 0 ? netmask : device.getNetmask();
					
					// Reverse lookup the edge server
					if(edgeServer != null && edgeServer.length() > 0){
						String hql = "SELECT d FROM Device d WHERE d.ipAddr = :ipAddr";
						List l = HibernateSession.currentSession().createQuery(hql).setParameter("ipAddr", edgeServer).list();
						
						// If we were able to locate exaclty one device with this ip address
						if(l.size() == 1){
							edgeServer = ((Device)l.get(0)).getDeviceId().toString();
						}
					}
					
					/*
					 * Update each property of the device that has changed
					 */
					device.updateProperties( rotation, scalingMode, outputMode, 
							screenshotUploadTime, filesyncStartTime, filesyncEndTime, timezone, 
							resolution, heartbeatInterval, maxFileStorage, 
							dhcpEnabled, ipAddress, connectAddress, gateway, netmask, networkInterface, dispatcherServers, 
							dnsServer, edgeServer, redirectGateway, lcdPin, lcdBranding, volume, 
							iowaitThreshold, cpuThreshold, memoryThreshold, loadThreshold, 
							display, autoUpdate, masterDevice, csHorizon, csLength, null, contentUpdateType, applyAlerts, xOffset, yOffset, zoom,
							null, null, diskCapacity, diskUsage, version, osVersion, antivirusScan,
							downloadPriority, audioNormalization, framesync, multicastAddress, multicastServerPort, multicastDevicePort, control, alphaCompositing, 
							bandwidthLimit, audioConnection,  type2VideoPlayback, useChrome, chromeDisableGpu, 
							null, !isApiCall, isFirstHeartbeatUpload, false);
							
					/*
					 * Next, update any mcm properties that have changed
					 */
					Elements propertyElements = doc.getElements( new XPath("//"+ DeviceProperty.PROPERTIES_ROOT_ELEMENT +"/"+ DeviceProperty.PROPERTY_ELEMENT) );
					while( propertyElements.hasMoreElements() )
					{
						try
						{
							Element propertyElement = (Element)propertyElements.next();
							String propertyName = propertyElement.getAttribute( DeviceProperty.NAME_ATTRIBUTE );
							String propertyValue = propertyElement.getAttribute( DeviceProperty.VALUE_ATTRIBUTE );
							String mcmId = propertyElement.getAttribute( DeviceProperty.MCM_ID_ATTRIBUTE );
							Mcm mcm = null;
							if( mcmId != null && mcmId.length() > 0 && mcmId.equalsIgnoreCase("null") == false ) {
								mcm = Mcm.getMcm( new Long( mcmId ) );
							}

							
							/*
							 * MCM Properties
							 */
							if( mcm != null )
							{
								if( propertyName.equalsIgnoreCase( DevicePropertyType.MCM_HOSTSTRING.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getMcmHoststring(), propertyValue, DevicePropertyType.MCM_HOSTSTRING, mcmId ) ){
									mcm.setMcmHoststring( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SERIAL_PORT.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSerialPort(), propertyValue, DevicePropertyType.SERIAL_PORT, mcmId ) ){
									mcm.setSerialPort( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SWITCH_TO_AUX_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSwitchToAuxCommand(), propertyValue, DevicePropertyType.SWITCH_TO_AUX_COMMAND, mcmId ) ){
									mcm.setSwitchToAuxCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SWITCH_TO_MEDIACAST_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSwitchToMediacastCommand(), propertyValue, DevicePropertyType.SWITCH_TO_MEDIACAST_COMMAND, mcmId ) ){
									mcm.setSwitchToMediacastCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getCurrentInputCommand(), propertyValue, DevicePropertyType.CURRENT_INPUT_COMMAND, mcmId ) ){
									mcm.setCurrentInputCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.ON_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getOnCommand(), propertyValue, DevicePropertyType.ON_COMMAND, mcmId ) ){
									mcm.setOnCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.OFF_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getOffCommand(), propertyValue, DevicePropertyType.OFF_COMMAND, mcmId ) ){
									mcm.setOffCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getCurrentPowerCommand(), propertyValue, DevicePropertyType.CURRENT_POWER_COMMAND, mcmId ) ){
									mcm.setCurrentPowerCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.CURRENT_VOLUME_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getCurrentVolumeCommand(), propertyValue, DevicePropertyType.CURRENT_VOLUME_COMMAND, mcmId ) ){
									mcm.setCurrentVolumeCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_VOLUME_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetVolumeCommand(), propertyValue, DevicePropertyType.SET_VOLUME_COMMAND, mcmId ) ){
									mcm.setSetVolumeCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getCurrentBrightnessCommand(), propertyValue, DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND, mcmId ) ){
									mcm.setCurrentBrightnessCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.CURRENT_CONTRAST_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getCurrentContrastCommand(), propertyValue, DevicePropertyType.CURRENT_CONTRAST_COMMAND, mcmId ) ){
									mcm.setCurrentContrastCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.OSD_ON_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getOsdOnCommand(), propertyValue, DevicePropertyType.OSD_ON_COMMAND, mcmId ) ){
									mcm.setOsdOnCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.OSD_OFF_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getOsdOffCommand(), propertyValue, DevicePropertyType.OSD_OFF_COMMAND, mcmId ) ){
									mcm.setOsdOffCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.AUTO_ADJUST_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getAutoAdjustCommand(), propertyValue, DevicePropertyType.AUTO_ADJUST_COMMAND, mcmId ) ){
									mcm.setAutoAdjustCommand( propertyValue );
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.MCM_IMPLEMENTATION_TYPE.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getImplementationType(), McmImplementationType.getMcmImplementationType( propertyValue ), DevicePropertyType.MCM_IMPLEMENTATION_TYPE, mcmId ) ){
									McmImplementationType implementationType = McmImplementationType.getMcmImplementationType( propertyValue );
									if( implementationType != null ){
										mcm.setImplementationType( implementationType );
									}	
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_INVERSION_ON_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetInversionOnCommand(), propertyValue, DevicePropertyType.SET_INVERSION_ON_COMMAND, mcmId ) ){
									mcm.setSetInversionOnCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_INVERSION_OFF_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetInversionOffCommand(), propertyValue, DevicePropertyType.SET_INVERSION_OFF_COMMAND, mcmId ) ){
									mcm.setSetInversionOffCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_SIGNAL_PATTERN_ON_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetSignalPatternOnCommand(), propertyValue, DevicePropertyType.SET_SIGNAL_PATTERN_ON_COMMAND, mcmId ) ){
									mcm.setSetSignalPatternOnCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_SIGNAL_PATTERN_OFF_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetSignalPatternOffCommand(), propertyValue, DevicePropertyType.SET_SIGNAL_PATTERN_OFF_COMMAND, mcmId ) ){
									mcm.setSetSignalPatternOffCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_PIXEL_SHIFT_ON_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetPixelShiftOnCommand(), propertyValue, DevicePropertyType.SET_PIXEL_SHIFT_ON_COMMAND, mcmId ) ){
									mcm.setSetPixelShiftOnCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.SET_PIXEL_SHIFT_OFF_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getSetPixelShiftOffCommand(), propertyValue, DevicePropertyType.SET_PIXEL_SHIFT_OFF_COMMAND, mcmId ) ){
									mcm.setSetPixelShiftOffCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.MUTE_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getMuteCommand(), propertyValue, DevicePropertyType.MUTE_COMMAND, mcmId ) ){
									mcm.setMuteCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.VOLUME_OFFSET_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getVolumeOffsetCommand(), propertyValue, DevicePropertyType.VOLUME_OFFSET_COMMAND, mcmId ) ){
									mcm.setVolumeOffsetCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.MIN_GAIN_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getMinGainCommand(), propertyValue, DevicePropertyType.MIN_GAIN_COMMAND, mcmId ) ){
									mcm.setMinGainCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.MAX_GAIN_COMMAND.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getMaxGainCommand(), propertyValue, DevicePropertyType.MAX_GAIN_COMMAND, mcmId ) ){
									mcm.setMaxGainCommand( propertyValue );									
								}else if( propertyName.equalsIgnoreCase( DevicePropertyType.DIAGNOSTIC_INTERVAL.getPropertyName() ) 
										&& device.propertyHasChanged( true, isFirstHeartbeatUpload, mcm.getDiagnosticInterval(), propertyValue, DevicePropertyType.DIAGNOSTIC_INTERVAL, mcmId ) ){
									mcm.setDiagnosticInterval( propertyValue );									
								}
							
								// We must update the mcm object within the loop because not all properties are necessarily associated with the same mcm.								
								mcm.update();					
							}				
						}
						catch(Exception ex) {
							logger.error( ex );
						}	
					}				
				}else{
					logger.info("First heartbeat properties have not yet been processed! Properties will not be processed for device_id = " + deviceId);
				}
								
				// Update the device if necessary				
				if( device != null ){
					
					// If we've just processed the first heartbeat upload -- set the processed flag
					if( isFirstHeartbeatUpload ){
						device.setInitPropertiesProcessed( Boolean.TRUE );
					}
					device.update();
				}				
			}								
		}
		else {
			logger.info("Invalid deviceId found in updateProperties(): "+ deviceId +". Unable to update properties.");
		}	
		HibernateSession.stopBulkmode();
	}	
	
	public static boolean propertyHasChanged(Object prop1, Object prop2) throws ParseException
	{
		return propertyHasChanged( prop1, prop2, true );
	}
	
	public static boolean propertyHasChanged(Object currentValue, Object newValue, boolean ignoreNullNewValue) throws ParseException
	{
		boolean result = false;
		
		// If the current value is null, but the new value is not 
		if( currentValue == null && newValue != null )
		{
			if( newValue instanceof String ){
				if(((String)newValue).length() > 0 ){
					result = true;
				}
			}else{
				result = true;
			}
		}
		// If the new value is null, but the current value is not 
		if( (newValue == null || String.valueOf(newValue).length() == 0) && currentValue != null )
		{
			/*
			 * Flag denoting whether or not to ignore null new values.
			 * This is required because the majority of the time that we call 
			 * this method, if the newValue is null -- we don't want to treat it as though
			 * the property has changed. There are cases, however, where we want to
			 * know if the property was changed from "something" to "nothing"
			 */
			if( ignoreNullNewValue == false ){
				if( currentValue instanceof String ){
					if(((String)currentValue).length() > 0 ){
						result = true;
					}
				}else{
					result = true;
				}
			}
		}		
		// Strings
		else if( currentValue instanceof String ){
			if( newValue != null && ((String)newValue).length() > 0 && ((String)currentValue).equalsIgnoreCase( (String)newValue ) == false ){
				result = true;									
			}
		}
		// Boolean
		else if( currentValue instanceof Boolean ){
			newValue = newValue != null && newValue instanceof String ? Boolean.valueOf( (String)newValue ) : newValue;
			if( newValue != null && ((Boolean)currentValue) != (Boolean)newValue ){
				result = true;								
			}
		}
		// Date
		else if( currentValue instanceof Date ){
			Date d = null;
			if( newValue != null && newValue instanceof String && ((String)newValue).length() > 0 ){
				// We're only concerned about the time
				SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.TIME_FORMAT );
				d = dateTimeFormat.parse( (String)newValue );
			}else if( newValue != null ){
				d = (Date)newValue;
			}
			if( newValue != null && ((Date)currentValue).getTime() != ((Date)d).getTime() ){
				result = true;									
			}
		}	
		// Integer
		else if( currentValue instanceof Integer ){
			if (newValue instanceof Integer){
				if( newValue != null && currentValue.equals( newValue ) == false ){
					result = true;
				}
			}
			else if( newValue != null && ((String)newValue).length() > 0 && ((Integer)currentValue) != new Integer( (String)newValue ) ){
				result = true;									
			}
		}	
		// Long
		else if( currentValue instanceof Long ){
			if( newValue != null && ((String)newValue).length() > 0 && ((Long)currentValue).longValue() != new Long( (String)newValue ).longValue() ){
				result = true;									
			}
		}			
		// Float
		else if( currentValue instanceof Float ){
			if( newValue != null && ((String)newValue).length() > 0 && ((Float)currentValue).floatValue() != new Float( (String)newValue ).floatValue() ){
				result = true;									
			}
		}					
		// PersistentStringEnum
		else if( currentValue instanceof PersistentStringEnum ){
			if( newValue != null && ((PersistentStringEnum)currentValue) != (PersistentStringEnum)newValue ){
				result = true;									
			}
		}	
		// Device
		else if( currentValue instanceof Device ){			
			if( newValue != null && ((String)newValue).length() > 0 )
			{
				// If "none" was selected -- set the property to null
				if( ((String)newValue).equalsIgnoreCase("0") ){
					result = true;
				}else{
					try {
						Device device = Device.getDevice( new Long( (String)newValue ) );
						if( device != null && device.getDeviceId() != ((Device)currentValue).getDeviceId() ){
							result = true;
						}
					} catch(Exception e) {
						result = false;
					}
				}
			}			
		}
		return result;		
	}
	
	private boolean propertyHasChanged(boolean isUpdateFromDevice, boolean isFirstHeartbeatUpload, Object currentValue, Object newValue, DevicePropertyType deviceProperty, String mcmId)  throws InterruptedException, java.text.ParseException {
		return propertyHasChanged(isUpdateFromDevice, isFirstHeartbeatUpload, currentValue, newValue, deviceProperty, mcmId, false);
	}
	
	private boolean propertyHasChanged(boolean isUpdateFromDevice, boolean isFirstHeartbeatUpload, Object currentValue, Object newValue, DevicePropertyType deviceProperty, String mcmId, boolean createDeviceCommandNow) throws InterruptedException, java.text.ParseException
	{
		boolean result = false;
		boolean sendDeviceCommand = false;
				
		// If this is the first heartbeat
		if( isFirstHeartbeatUpload ) 
		{					
			// If the flag to initPropertiesFromDevice is set to true
			if( this.getInitPropertiesFromDevice().equals( Boolean.TRUE ) )
			{				
				// If the value from the device is not null				
				if( newValue != null && String.valueOf(newValue).length() > 0 )
				{
					// The device always wins
					result = true;
				}
				// If the value from the device is null
				else
				{
					// And the server property is null
					if( currentValue == null || (currentValue instanceof String && ((String)currentValue).length() == 0 ) )
					{
						// Do not update the property
						result = false;												
					}
					// If the property on the server is not null
					else if( deviceProperty.isBlankable() == false )
					{
						// Set the flag to send a device command to this device with the server's value
						newValue = currentValue;
						sendDeviceCommand = true;				
					}
					else
					{
						// this is a blankable property with a null coming up from the device
						result = true;
					}
				}
			}
			else // init properties from server
			{
				// If the server property is null
				if( currentValue == null || (currentValue instanceof String && ((String)currentValue).length() == 0 ) )
				{
					if( newValue != null && (newValue instanceof String ? ((String)newValue).length() > 0 : true))
					{
						if(deviceProperty.isBlankable()){
							newValue = currentValue;
							sendDeviceCommand = true;
						}else{
							// Use the value from the device
							result = true;
						}
					}
				}
				// If the property on the server is different than the one reported from the device
				else if( Device.propertyHasChanged( currentValue, newValue, false ) ){
					newValue = currentValue;
					sendDeviceCommand = true;
				}						
			}			
		}
		// If this is not the first heartbeat and the property from the device is different than is on the server
		else if( isUpdateFromDevice && Device.propertyHasChanged(currentValue, newValue, false) )
		{
			if( newValue == null && deviceProperty.isBlankable() == false )
			{
				// the value coming from the device is somehow null, but it's not a blankable property
				// set it back to the value that's on the server
				newValue = currentValue;
				sendDeviceCommand = true;
			}
			else
			{
				// Update the value on the server
				result = true;
			}
		}
			
		// If the newvalue is coming from the form,
		// Accept null values only if this property is blankable
		else if( Device.propertyHasChanged( currentValue, newValue, !deviceProperty.isBlankable() ) )
		{
			sendDeviceCommand = true;
			result = true;																
		}
		
		/*
		 * Special handling on a per property basis before allowing the property to change
		 */
		if( result == true )
		{
			// Make sure the new value is a valid timezone
			if( deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.TIMEZONE.getPropertyName() ) )
			{
				// Attempt to locate this timezone in the collection of available timezones
				TimeZone timezone = TimeZone.getTimeZone( String.valueOf( newValue ) );
				boolean isValidTimezone = false;
				String timezones[] = TimeZone.getAvailableIDs();
				for( int i=0; i<timezones.length; i++ ){
					TimeZone tz = TimeZone.getTimeZone( timezones[i] );
					// A random string returns a GMT timezone. Ignore such strings but allow GMT as a timezone.
					if( timezone.getID().equalsIgnoreCase( invalidTimezone.getID() ) == false || newValue.equals("GMT") ){ 
						if( tz.getID().equalsIgnoreCase( timezone.getID() ) ){
							isValidTimezone = true;
						}
					}		
				}
				
				// If we've determined this is not a valid timezone -- do not set the property to the new value
				if( isValidTimezone == false ){
					result = false;
					sendDeviceCommand = false;
				}	
			}
			// If the resolution property has changed
			else if( deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.RESOLUTION.getPropertyName() ) )
			{
				// If this resolution is not one of the supported resolutions for this display
				if( this.getDisplay() != null && isSupportedResolution( this.getDisplay(), String.valueOf( newValue ) ) == false )
				{
					// Change the display to custom 
					this.setDisplay( null );
				}
			}
			// If the edge server property has changed
			else if( deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.EDGE_SERVER.getPropertyName() ) )
			{
				// If this device does not currently have an edge server, and a "0" (none) was passed in -- ignore the property
				if( this.getEdgeServer() == null && newValue != null && String.valueOf( newValue ).equalsIgnoreCase("0") ) {
					result = false;					
				}
				
				// Do not send a device command in this method -- we will want to send the ipAddr of the edge server (handled outside this method)
				sendDeviceCommand = false;
			}
			// If the edge server property has changed
			else if( deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.MASTER_DEVICE_ID.getPropertyName() ) )
			{
				// If this device does not currently have a master device, and a "0" (none) was passed in -- ignore the property
				if( this.getMirrorSource() == null && newValue != null && String.valueOf( newValue ).equalsIgnoreCase("0") ) {
					result = false;					
				}
				
				// Do not send a device command in this method -- we will want to send the deviceId of the master device (handled outside this method)
				sendDeviceCommand = false;
			}
			// If this is a multicast property
			else if( deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.MULTICAST_ADDRESS.getPropertyName() ) ||
					deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.MULTICAST_SERVER_PORT.getPropertyName()  ) ||
					deviceProperty.getPropertyName().equalsIgnoreCase( DevicePropertyType.MULTICAST_DEVICE_PORT.getPropertyName() ) ){
				// If this change is coming from the device but it's not the first heartbeat
				if(isFirstHeartbeatUpload == false && isUpdateFromDevice){
					// Send device command using the server value
					newValue = currentValue;
					sendDeviceCommand = true;
				}
			}
			
			// Set the init flag based on what property is being changed
			if( isUpdateFromDevice == false && deviceProperty.isAdvancedProperty()){
				this.setInitPropertiesFromDevice( Boolean.FALSE );
			}
		}

		// If the flag was set to send a device command
		if( sendDeviceCommand )
		{
			// Build the parameters string for the propertyChange device command
			String params = deviceProperty.getPropertyName() + ",";
			
			// If this is a PersistentStringEnum property -- get the persistentValue of that object
			if(newValue instanceof PersistentStringEnum){
				params += Reformat.escape( ((PersistentStringEnum)newValue).getPersistentValue() );
			}
			// If this is a date property -- send down only the time portion of the date
			else if( newValue instanceof Date ){
				// Use appropriate format based on device version
				if(this.getVersion() == null || this.isNewerVersion(this.getVersion(), "3.6b1")){
					SimpleDateFormat extendedTimeFormat = new SimpleDateFormat( Constants.EXTENDED_TIME_FORMAT );
					params += Reformat.escape( extendedTimeFormat.format( newValue ) );
				}else{
					SimpleDateFormat timeFormat = new SimpleDateFormat( Constants.TIME_FORMAT );
					params += Reformat.escape( timeFormat.format( newValue ) );
				}
			}else{
				params += Reformat.escape( String.valueOf(newValue) );
			}
			
			// If this is an mcm device property -- add the mcmId to the properties
			if( deviceProperty.getClassName() != null && deviceProperty.getClassName().equalsIgnoreCase( McmProperty.class.getName() ) ){
				params = mcmId +","+ params;
			}
			
			// Send a device command to this device
			if(createDeviceCommandNow){
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, params, false, false, false, null, true );
			}else{
				this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, params, true );
			}
		}
		return result;
	}	
	
	/**
	 * NOTE: This method is required for backward compatibility. 
	 * Once all devices are on 3.2, this method can be removed.
	 * 
	 * Called from DispatcherSoalBindingImpl.logFileUploadComplete().
	 * Updates the database with property values found in the given advanced_properties.xml file
	 * @param advancedPropertiesFile
	 */
	public static synchronized void handleAdvancedPropertiesFileUpload(File zipFile)
	{					
		File advancedPropertiesFile = null;
		try
		{				
			if( zipFile.exists() )
			{
				// First, unzip the zip file (ignoring directory structure) into the logs directory			
				String unzipPath = zipFile.getAbsolutePath().substring( 0, zipFile.getAbsolutePath().lastIndexOf("/") );
				String extractedFileName = Files.unzip( zipFile.getAbsolutePath(), unzipPath, false );
				if( extractedFileName != null && extractedFileName.length() > 0 ){
					String advancedPropertiesPath = unzipPath +"/"+ extractedFileName;
					advancedPropertiesFile = new File( advancedPropertiesPath );
					if( advancedPropertiesFile.exists() && advancedPropertiesFile.isDirectory() == false )
					{			
						Document doc = new Document( advancedPropertiesFile );
						updateProperties( doc, false, false );
					} else {
						logger.info("Could not locate specified file: "+ advancedPropertiesPath +". Unable to continue.");
					}	
				}
			}else {
				logger.info("Could not locate zip file: "+ zipFile.getAbsolutePath() +". Unable to continue.");
			}
		}
		catch(Exception e)
		{
			logger.error( e );
		}	
		finally
		{
			// Delete the zip file		
			if( zipFile != null && zipFile.isDirectory() == false ){				
				zipFile.delete();
			}
			// Delete the advanced_properties.xml file			
			if( advancedPropertiesFile != null && advancedPropertiesFile.isDirectory() == false ){
				advancedPropertiesFile.delete();
			}
		}		
	}
	
	/**
	 * Determines if the given resolution is one of the supported resolutions
	 * for the given display according to the peripherals.xml file.
	 * @param display
	 * @param resolution
	 * @return
	 */
	public static boolean isSupportedResolution(String displayName, String resolutionName)
	{
		boolean result = false;		
		try {
			// Attempt to locate a resolution element with the given name for the given display
			Document doc = KMMServlet.getPeripheralsXml();
			String xpath = "//"+ DeviceProperty.PERIPHERALS_ROOT_ELEMENT +"/"+ DeviceProperty.PERIPHRAL_ELEMENT
				+ "[@type=\"" + DeviceProperty.TYPE_DISPLAY + "\"]/"+ DeviceProperty.NAME_ELEMENT+"[@"+DeviceProperty.VALUE_ATTRIBUTE
				+ "=\""+ displayName +"\"]";
			Element e = doc.getElement( new XPath(xpath ));
			if( e != null ){
				Element eParent = e.getParentElement();
				if( eParent != null ){
					Element resolutionElement = eParent.getElement(new XPath(DeviceProperty.RESOLUTION_ELEMENT +"[@name=\""+ resolutionName +"\"]"));
					if( resolutionElement != null ){
						result = true;
					}
				}
			}			
		} catch (XPathException e) {
			logger.error( e );
		} catch (electric.xml.ParseException e) {
			logger.error( e );
		}			
		return result;
	}	
	
	/**
	 * Clears out the vpnIpAddr property for all devices with the given vpnIpAddr.
	 * This is a necessary step prior to assigning a vpnIpAddr so as to avoid vpnIpAddr conflicts.
	 * @param vpnIpAddr
	 */
	public void clearVpnIpAddresses(String vpnIpAddr)
	{
		Session session = HibernateSession.currentSession();					
		String hql = "UPDATE Device "
			+ "SET vpn_ip_addr = null "
			+ "WHERE vpn_ip_addr = ? "
			+ "AND device_id != ?";
		HibernateSession.beginTransaction();
			session.createQuery( hql )
			.setParameter(0, vpnIpAddr)
			.setParameter(1, this.getDeviceId())
			.executeUpdate();
		HibernateSession.commitTransaction();
	}
	
	/**
	 * @return Returns the number of device command with a status of QUEUED or IN_PROGRESS for this device
	 * excluding the given deviceCommandId
	 */
	public int getNumUnexecutedDeviceCommands(Long deviceCommandId) throws HibernateException 
	{
		int result = 0;
		String hql = "SELECT COUNT(dc) "
			+ "FROM DeviceCommand as dc "
			+ "WHERE dc.device.deviceId = :deviceId "
			+ "AND dc.deviceCommandId != :deviceCommandId "
			+ "AND dc.status IN ('"+ StatusType.QUEUED +"', '"+ StatusType.IN_PROGRESS +"')";		
		Session session = HibernateSession.currentSession();	
		Iterator i = session.createQuery( hql )
			.setParameter("deviceId", this.getDeviceId())
			.setParameter("deviceCommandId", deviceCommandId)
			.iterate();
		result = ((Long) i.next()).intValue();
		Hibernate.close( i );
		return result;
	}		
	
	/**
	 * Updates the bytesToDownload property of this device
	 * by summing the number of outstanding bytes_to_download
	 * according to the file_transmission table.
	 */
	public void updateBytesToDownload()
	{
		Long bytesToDownload = FileTransmission.getBytesToDownload( this );
		this.setBytesToDownload( bytesToDownload );
		this.update();
	}
	
	/**
	 * Calculates the edgeServerOpenvpnHostIp for this device based
	 * on the other edgeServers that have the same firstTwoOctets of 
	 * this devices' vpnIpAddr.
	 * 
	 * @param vpnIpAddr
	 * @return
	 */
	public String buildEdgeServerOpenvpnHostIp(String vpnIpAddr)
	{
		String result = null;
		
		// Locate all devices that have a edgeServerOpenvpnHostIp matching the first two octets of this vpnIpAddr
		String firstTwoOctets = vpnIpAddr.substring( 0, vpnIpAddr.indexOf(".", vpnIpAddr.indexOf(".") + 1) + 1 );
		
		// Generate only one edgeServerOpenvpnHostIp per JVM
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT device.edgeServerOpenvpnHostIp "
			+ "FROM Device device WHERE device.edgeServerOpenvpnHostIp LIKE (:firstTwoOctets) "
			+ "AND device.edgeServerOpenvpnHostIp IS NOT NULL";
		List consumedEgdeServerHostIps = session.createQuery( hql ).setParameter("firstTwoOctets", firstTwoOctets +"%").list();
		
		// Find the lowest available number >= 16 and <= 252 and evenly divisible by 4 in the third octet
		ArrayList<Integer> thirdOctets = new ArrayList<Integer>();
		for( Iterator<String> i=consumedEgdeServerHostIps.iterator(); i.hasNext(); ){
			String[] ipParts = i.next().split("\\.");
			thirdOctets.add( new Integer( ipParts[2] ) );
		}
		
		String validThirdOctet = null;
		if(thirdOctets.size() > 0){
			for( Integer i=16; i<=252; i+=4 ){
				if( thirdOctets.contains(i) == false ){
					validThirdOctet = String.valueOf(i);
					break;
				}
			}
		}else{
			// Default to 16
			validThirdOctet = "16";
		}
		
		// If we found a valid third octet to use
		if( validThirdOctet != null ){
			String[] vpnIpParts = vpnIpAddr.split("\\.");
			result = vpnIpParts[0] +"."+ vpnIpParts[1] +"."+ validThirdOctet +".0";
		}
		
		return result;
	}
	
	/**
	 * Sends a kill command for the given commonName 
	 * via the OpenVPN management interface.
	 * @param username
	 */
	public void disconnectOpenVpnClient()
	{
        Socket echoSocket = null;
        PrintWriter out = null;
        try 
        {        	
        	// Use the vpnIpAddr of this device to build the ip addr to appropriate dispatcher server vpnIpAddr
        	if( this.getVpnIpAddr() != null && this.getVpnIpAddr().indexOf(".") > 0 )
        	{
        		String[] ipParts = this.getVpnIpAddr().split("\\.");
        		String dispatcherVpnIpAddr = ipParts[0] +"."+ ipParts[1] +".0.1";
        		
        		// Get the port that the OpenVpn manangement interface is running on
            	String openVpnManagementPort = OPENVPN_MANAGEMENT_PORT_DEFAULT;
            	try{
            		openVpnManagementPort = KuvataConfig.getPropertyValue("Application.openVPNManagementPort");
            	}catch(KmfException e){
            		logger.info("Could not locate property \"Application.openVPNManagementPort\". Using default: "+ OPENVPN_MANAGEMENT_PORT_DEFAULT);
            	}            	
            	
    			// Replace all ":" with "_" to get the common name as registered by OpenVpn
    			String commonName = this.getMacAddr().replaceAll(":", "_").trim();
    			logger.info("Disconnecting OpenVPN client: "+ commonName +" - at "+ dispatcherVpnIpAddr +":"+ openVpnManagementPort);
    			
                echoSocket = new Socket( dispatcherVpnIpAddr, Integer.valueOf( openVpnManagementPort ).intValue() );
                out = new PrintWriter(echoSocket.getOutputStream(), true);
                out.println("kill "+ commonName);	
        	}
        } 
        catch (Exception e) {
            logger.error( e );
        } 
        finally
        {
        	try {
        		if( out != null ){
        			out.close();
        		}
        		if( echoSocket != null ){
        			echoSocket.close();
        		}
			} catch (IOException e) {
				logger.error( e );
			}	
        }	
	}
	
	/**
	 * Returns true or false depending upon whether or not the given
	 * deviceReleaseFile meets the criteria based on the auto update
	 * property of this device.
	 * NOTE: Currently used by both server and device.
	 * 
	 * @param deviceReleaseFilename
	 * @return
	 */
	public static boolean shouldInstallDeviceRelease(String deviceReleaseFilename, String autoUpdate)
	{
		boolean result = false;		
		if( autoUpdate != null )
		{
			
			// If this device has it's auto-update property set to "On Plus Beta" -- it should install the device release
			if( autoUpdate.equalsIgnoreCase( DeviceAutoUpdateType.ON_PLUS_BETA.getPersistentValue() ) ){
				result = true;
			}
			// If this device has it's auto-update property set to "On"
			else if( autoUpdate.equalsIgnoreCase( DeviceAutoUpdateType.ON.getPersistentValue() ) )
			{
				// And there is not a "b" in the filename -- the device should install this device release
				if( deviceReleaseFilename.indexOf("b") < 0 ){
					result = true;
				}
			}
		}		
		return result;
	}
	
	/**
	 * If there are any "higher" (meaning higher version) installDeviceRelease device commands with a status of FAILED,
	 * reset their status to QUEUED so the device command will be retried on next heartbeat.
	 */
	public void retryInstallDeviceReleaseDeviceCommands(String currentVersion)
	{
		// Parse out the version number of the currentVersion
		if( currentVersion.indexOf("/") > 0 ){
			currentVersion = currentVersion.substring( currentVersion.indexOf("/") + 1 );
		}
		if( currentVersion.indexOf(".") > 0 ){
			currentVersion = currentVersion.substring( 0, currentVersion.lastIndexOf(".") );
		}	
		
		// Get all installDeviceRelease device commands for this device that have a status of FAILED
		List<DeviceCommand> failedCommands = DeviceCommand.getDeviceCommands( this, DeviceCommandType.INSTALL_DEVICE_RELEASE, StatusType.FAILED, null );
		for( DeviceCommand failedCommand : failedCommands )
		{
			// Parse out the version number of this installDeviceRelease device command
			String failedVersion = failedCommand.getParameters();
			if( failedVersion.indexOf("/") > 0 ){
				failedVersion = failedVersion.substring( failedVersion.indexOf("/") + 1 );
			}
			if( failedVersion.indexOf(".") > 0 ){
				failedVersion = failedVersion.substring( 0, failedVersion.lastIndexOf(".") );
			}
			
			// Android releases
			if( failedVersion.indexOf("_") > 0 ){
				failedVersion = failedVersion.substring( failedVersion.indexOf("_") + 1 );
			}
			if( currentVersion.indexOf("_") > 0 ){
				currentVersion = currentVersion.substring( currentVersion.indexOf("_") + 1 );
			}
			
			// If the failedVersion is "newer" than the currentVersion
			if( isNewerVersion( failedVersion, currentVersion) )
			{
				// Reset the status of the failed device command so it will be retried on next heartbeat
				logger.info("Resetting failed installDeviceRelease device command: "+ failedCommand.getParameters() +" for device: "+ this.getDeviceId());
				failedCommand.setSubStatus( null );
				failedCommand.setStatus( StatusType.QUEUED );
				failedCommand.update();
			}		
		}
	}
	
	/**
	 * Perform all actions required when un-assigning an edge server from a device
	 * @param savedEdgeServer
	 */
	public void unassignEdgeServer(Device savedEdgeServer)
	{
		// Reset redirectGateway to false
		this.setRedirectGateway( Boolean.FALSE );
		try {
			this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.REDIRECT_GATEWAY.getPropertyName() +","+ Reformat.escape(Boolean.FALSE.toString()), true );
		} catch (Exception e) {
			logger.error( e );
		}
		
		// And this was the last node device for that edge server
		if( savedEdgeServer != null && savedEdgeServer.getOtherEdgeDevices(this).size() == 0 ){
			/*
			 * Before clearing out the edgeServerOpenVpnHostIp for the edge server,
			 * we want to give the node device an opporunity to heartbeat and receive its
			 * edgeServer=null device command (which was added above). We will try to accomplish 
			 * this by spawning a new thread here that will wait for the length of the heartbeat
			 * interval for this device, and THEN send down the device command to the edge server
			 * to clear out its edgeServerOpenVpnHostIp. This will allow the OpenVPN server tunnel
			 * to remain open on the edge server for at least one more heartbeat of the node device.
			 * 
			 * If the node device does not heartbeat before the edge server
			 * receives its edgeServerOpenVpnHostIp=null device command 
			 * (and subsequently shuts down its openvpn server), the node
			 * will have to wait until self tests run, at which time it should
			 * receive its new connection information and realize that
			 * its edge server has been unassigned.
			 * 
			 * NOTE: If at some future point we can reliably push device commands (including propertyChange device commands),
			 * we could eliminate this thread/logic.
			 */
			if(edgeServerOpenVpnHostIpThreads.contains(savedEdgeServer.getDeviceId()) == false){
				EdgeServerOpenVpnHostIpThread t = new EdgeServerOpenVpnHostIpThread(){
					public void run(){
						try {
							SchemaDirectory.initialize( this.getSchemaName(), "DeviceCommandThread", this.getAppUserId(), false, false );
							long sleepTime = 0;
							if( this.getMaxHeartbeatInterval() != null && this.getMaxHeartbeatInterval().length() > 0 ){
								sleepTime = Long.valueOf( this.getMaxHeartbeatInterval() ).longValue() * 1000;
							}
							
							logger.info("About to sleep in EdgeServerOpenVpnHostIpThread for "+ sleepTime);
							Thread.sleep( sleepTime );
							
							// Clear out the edgeServerOpenVpnHostIp on the edge server
							logger.info("Clearing edgeServerOpenVpnHostIp for: "+ this.getEdgeServer().getDeviceId());
							this.getEdgeServer().setEdgeServerOpenvpnHostIp( null );
							this.getEdgeServer().update();
							this.getEdgeServer().addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.EDGE_SERVER_OPENVPN_HOST_IP.getPropertyName() +","+ Reformat.escape(null), true );
						} catch (Exception e) {
							logger.error( e );
						} finally {
							edgeServerOpenVpnHostIpThreads.remove(this.getEdgeServer().getDeviceId());
						}
					}
				};
				
				edgeServerOpenVpnHostIpThreads.add(savedEdgeServer.getDeviceId());
				
				t.setEdgeServer( savedEdgeServer );
				t.setMaxHeartbeatInterval( this.getHeartbeatInterval() );
				t.setSchemaName( SchemaDirectory.getSchema().getSchemaName() );
				t.setAppUserId( KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUserId() : null );
				t.start();
			}
		}
		this.setEdgeServer( null );
		this.update();
	}
	
	public String getLastModifiedFormatted() {
		String result = "";
		if( this.getLastModified() != null ){
			result = EntityInstance.dateFormat.format( this.getLastModified() );
		}
		return result;
	}
	
	public static void updateActiveDispatcher(Device device){
		if(dispatcherIpAddr == null){
			synchronized(Device.class){
				if(dispatcherIpAddr == null){
					// Update the active dispatcher for this device
					try {
						String dispatcherNetworkInterface = KuvataConfig.getPropertyValue("Dispatcher.networkInterface." + ContentScheduler.getDispatcherVpnIpAddress());
						
						String dispatcherIpAddr = "";
						Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ifconfig " + dispatcherNetworkInterface + " | grep 'inet addr:' | cut -d: -f2 | awk '{print $1}'"});
						p.waitFor();
						
						int c;
						while( (c = p.getInputStream().read()) != -1 ){
							dispatcherIpAddr += (char)c;
						} 
						
						Device.dispatcherIpAddr = dispatcherIpAddr.trim();
						
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
		}
		
		device.setActiveDispatcher(dispatcherIpAddr);
		device.update();
	}
	
	public void heartbeatNow(){
		try {
			// Make the device heartbeat now
			HttpClient client = new HttpClient();
			String activeDispatcher = this.getActiveDispatcher() != null && this.getActiveDispatcher().length() > 0 ? this.getActiveDispatcher() : "localhost";
			client.get("http://" + activeDispatcher + ":8124/dc?id=" + this.getDeviceId() + "&cmd=heartbeatNow", null);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof Device) ) result = false;
		
		Device c = (Device) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "Device".hashCode();
		result = Reformat.getSafeHash( this.getDeviceId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getDeviceName(), result, 11 );		
		return result < 0 ? -result : result;
	}
	
	public int eTag(){
		int result = "Device".hashCode();
		result = Reformat.getSafeHash( this.getDeviceId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getDeviceName(), result, 5 );
		result = Reformat.getSafeHash( this.getTimezone(), result, 7 );
		result = Reformat.getSafeHash( this.getContentUpdateType(), result, 11 );
		result = Reformat.getSafeHash( this.getVolume(), result, 13 );
		if(this.getMirrorSource() != null){
			result = Reformat.getSafeHash( this.getMirrorSource().getDeviceId(), result, 17 );
		}
		return result < 0 ? -result : result;
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
	 * @return Returns the deviceName.
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName The deviceName to set.
	 */
	public void setDeviceName(String deviceName)
	{
		this.deviceName = deviceName;
	}
	/**
	 * @return Returns the deviceGroupMembers.
	 */
	public Set<DeviceGrpMember> getDeviceGrpMembers() {
		return deviceGrpMembers;
	}

	/**
	 * @param deviceGroupMembers The deviceGroupMembers to set.
	 */
	public void setDeviceGrpMembers(Set<DeviceGrpMember> deviceGrpMembers) {
		this.deviceGrpMembers = deviceGrpMembers;
	}



	/**
	 * @return Returns the heartbeatEvents.
	 */
	public Set getHeartbeatEvents() {
		return heartbeatEvents;
	}
	
	/**
	 * @param heartbeatEvents The heartbeatEvents to set.
	 */
	public void setHeartbeatEvents(Set heartbeatEvents) {
		this.heartbeatEvents = heartbeatEvents;
	}
	
	/**
	 * Returns the set of device schedules sorted by segment name
	 * @return
	 */
	public List getDeviceSchedulesSorted()
	{
		ArrayList result = new ArrayList( this.getDeviceSchedules() );
		if( this.getDeviceSchedules() != null ){			
			BeanComparator comparator1 = new BeanComparator("segmentName");
			BeanComparator comparator2 = new BeanComparator("segment", comparator1);
			Collections.sort( result, comparator2 );
		}
		return result;
	}
	
	/**
	 * @return Returns the deviceSchedules.
	 */
	public Set<DeviceSchedule> getDeviceSchedules() {
		return deviceSchedules;
	}

	/**
	 * @param deviceSchedules The deviceSchedules to set.
	 */
	public void setDeviceSchedules(Set deviceSchedules) {
		this.deviceSchedules = deviceSchedules;
	}

	/**
	 * @return Returns the contentSchedulingHorizon.
	 */
	public Integer getContentSchedulingHorizon() {
		return contentSchedulingHorizon;
	}

	/**
	 * @param contentSchedulingHorizon The contentSchedulingHorizon to set.
	 */
	public void setContentSchedulingHorizon(Integer contentSchedulingHorizon) {
		this.contentSchedulingHorizon = contentSchedulingHorizon;
	}

	/**
	 * @return Returns the lastScheduledContent.
	 */
	public Date getLastScheduledContent() {
		return lastScheduledContent;
	}

	/**
	 * @param lastScheduledContent The lastScheduledContent to set.
	 */
	public void setLastScheduledContent(Date lastScheduledContent) {
		this.lastScheduledContent = lastScheduledContent;
	}

	/**
	 * @return Returns the additionalContentAmount.
	 */
	public Integer getAdditionalContentAmount() {
		return additionalContentAmount;
	}

	/**
	 * @param additionalContentAmount The additionalContentAmount to set.
	 */
	public void setAdditionalContentAmount(Integer additionalContentAmount) {
		this.additionalContentAmount = additionalContentAmount;
	}
	/**
	 * @return Returns the contentOutOfDate.
	 */
	public Integer getContentOutOfDate() {
		return contentOutOfDate;
	}
	/**
	 * @param contentOutOfDate The contentOutOfDate to set.
	 */
	public void setContentOutOfDate(Integer contentOutOfDate) {
		this.contentOutOfDate = contentOutOfDate;
	}

	/**
	 * @return Returns the deviceCommands.
	 */
	public List getDeviceCommands() {
		return deviceCommands;
	}
	/**
	 * @param deviceCommands The deviceCommands to set.
	 */
	public void setDeviceCommands(List deviceCommands) {
		this.deviceCommands = deviceCommands;
	}
	/**
	 * @return Returns the contentSchedulerStatus.
	 */
	public String getContentSchedulerStatus() {
		return contentSchedulerStatus;
	}
	/**
	 * @param contentSchedulerStatus The contentSchedulerStatus to set.
	 */
	public void setContentSchedulerStatus(String contentSchedulerStatus) {
		this.contentSchedulerStatus = contentSchedulerStatus;
	}
	/**
	 * @return Returns the edgeServer.
	 */
	public Device getEdgeServer() {
		return edgeServer;
	}
	/**
	 * @param edgeServer The edgeServer to set.
	 */
	public void setEdgeServer(Device edgeServer) {
		this.edgeServer = edgeServer;
	}
	
	/**
	 * @return Returns the contentSchedulerAlertMessages.
	 */
	public String getContentSchedulerMessages() {
		return contentSchedulerMessages;
	}
	/**
	 * @param contentSchedulerAlertMessages The contentSchedulerAlertMessages to set.
	 */
	public void setContentSchedulerMessages(
			String contentSchedulerMessages) {
		this.contentSchedulerMessages = contentSchedulerMessages;
	}
	/**
	 * @return Returns the maxVolume.
	 */
	public String getMaxVolume() {
		return maxVolume;
	}
	
	/**
	 * @param maxVolume The maxVolume to set.
	 */
	public void setMaxVolume(String maxVolume) {
		this.maxVolume = maxVolume;
	}
	
	/**
	 * @return Returns the volume.
	 */
	public String getVolume() {
		return volume;
	}
	
	/**
	 * @param volume The volume to set.
	 */
	public void setVolume(String volume) {
		this.volume = volume;
	}
	/**
	 * @return Returns the applyAlerts.
	 */
	public Boolean getApplyAlerts() {
		if( applyAlerts == null ) applyAlerts = Boolean.FALSE;		
		return applyAlerts;
	}
	
	/**
	 * @param applyAlerts The applyAlerts to set.
	 */
	public void setApplyAlerts(Boolean applyAlerts) {
		this.applyAlerts = applyAlerts;
	}
	/**
	 * @return Returns the heartbeatInterval.
	 */
	public String getHeartbeatInterval() {
		return heartbeatInterval;
	}
	
	/**
	 * @param heartbeatInterval The heartbeatInterval to set.
	 */
	public void setHeartbeatInterval(String heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}
	
	/**
	 * @return Returns the diskCapacity.
	 */
	public String getDiskCapacity() {
		return diskCapacity;
	}
	
	/**
	 * @param diskCapacity The diskCapacity to set.
	 */
	public void setDiskCapacity(String diskCapacity) {
		this.diskCapacity = diskCapacity;
	}
	
	/**
	 * @return Returns the diskUsage.
	 */
	public String getDiskUsage() {
		return diskUsage;
	}
	
	/**
	 * @param diskUsage The diskUsage to set.
	 */
	public void setDiskUsage(String diskUsage) {
		this.diskUsage = diskUsage;
	}
	
	/**
	 * @return Returns the display.
	 */
	public String getDisplay() {
		return display;
	}
	
	/**
	 * @param display The display to set.
	 */
	public void setDisplay(String display) {
		this.display = display;
	}

	
	/**
	 * @return Returns the rotation.
	 */
	public DisplayRotationType getRotation() {
		return rotation;
	}
	
	/**
	 * @param orientation The rotation to set.
	 */
	public void setRotation(DisplayRotationType rotation) {
		this.rotation = rotation;
	}
	
	/**
	 * @return Returns the resolution.
	 */
	public String getResolution() {
		return resolution;
	}
	
	/**
	 * @param resolution The resolution to set.
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	
	/**
	 * @return Returns the streamingClientCapable.
	 */
	public Boolean getStreamingClientCapable() {
		return streamingClientCapable;
	}
	
	/**
	 * @param streamingClientCapable The streamingClientCapable to set.
	 */
	public void setStreamingClientCapable(Boolean streamingClientCapable) {
		this.streamingClientCapable = streamingClientCapable;
	}
	
	/**
	 * @return Returns the streamingServerCapable.
	 */
	public Boolean getStreamingServerCapable() {
		return streamingServerCapable;
	}
	
	/**
	 * @param streamingServerCapable The streamingServerCapable to set.
	 */
	public void setStreamingServerCapable(Boolean streamingServerCapable) {
		this.streamingServerCapable = streamingServerCapable;
	}
	
	/**
	 * @return Returns the timezone.
	 */
	public String getTimezone() {
		return timezone;
	}
	
	/**
	 * @param timezone The timezone to set.
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	/**
	 * @return the assetExclusions
	 */
	public Set<AssetExclusion> getAssetExclusions() {
		return assetExclusions;
	}
	/**
	 * @param assetExclusions the assetExclusions to set
	 */
	public void setAssetExclusions(Set<AssetExclusion> assetExclusions) {
		this.assetExclusions = assetExclusions;
	}
	/**
	 * Returns the set of device schedules sorted by segment name
	 * @return
	 */
	public List getAssetExclusionsSorted()
	{
		ArrayList result = new ArrayList( this.getAssetExclusions() );
		if( this.getAssetExclusions() != null ){			
			BeanComparator comparator1 = new BeanComparator("assetName");
			BeanComparator comparator2 = new BeanComparator("asset", comparator1);
			Collections.sort( result, comparator2 );
		}
		return result;
	}	
	
	/**
	 *  @return Returns the timezone, or the server's timezone if there is no timezone
	 */
	public String getSafeTimezone() {
		if( getTimezone() != null ) {
			return getTimezone();
		}
		else
		{
			// Default to server timezone
			TimeZone tz = Calendar.getInstance().getTimeZone();
			return tz.getID();			
		}
	}
	
	/**
	 * Returns the "short" version of the device's timezone
	 * (e.g. PST, EST, PDT)
	 * @return
	 * @throws Exception
	 */
	public String getTimezoneShort() throws Exception
	{
		TimeZone timeZone = TimeZone.getTimeZone( this.getSafeTimezone() );
		DateFormat df = new SimpleDateFormat("z");
		df.setTimeZone( timeZone );
		return df.format( new Date() );
	}
	
	public String getTimezoneNumeric() throws Exception
	{
		TimeZone timeZone = TimeZone.getTimeZone( this.getSafeTimezone() );
		DateFormat df = new SimpleDateFormat("Z");
		df.setTimeZone( timeZone );
		return df.format( new Date() );		
	}
	
	public byte[] getBillingStatusKey(){
		// Prepare a key with 24 bytes
		String key = this.deviceId.toString();
		while(key.length() < 24){
			key += key;
		}
		return key.substring(0, 24).getBytes();
	}
	
	public void setBillingStatus(String billingStatus, boolean doVerify){
			if(doVerify == false || verifyBillingStatus()){
				try {
					TripleDES encrypter = new TripleDES(getBillingStatusKey());
					setEncryptedBillingStatus(encrypter.encryptToString(billingStatus));
					setReadableBillingStatus(billingStatus);
					
					if(billingStatus.equals(BillingStatusType.PRODUCTION.getPersistentValue())){
						this.setBillableStartDt(new Date());
					}else if(billingStatus.equals(BillingStatusType.OUT_OF_SERVICE.getPersistentValue())){
						this.setBillableEndDt(new Date());
						this.setContentUpdateType(ContentUpdateType.NO_UPDATES.getPersistentValue());
					}
					
					// If an edge server is going out of service
					if(billingStatus.equals(BillingStatusType.OUT_OF_SERVICE.getName())){
						for( Iterator i = this.getEdgeDevices().iterator(); i.hasNext(); )
						{			
							// Perform all actions required when un-assigning an edge server from a device
							Device edgeDevice = (Device)i.next();
							edgeDevice.unassignEdgeServer( edgeDevice.getEdgeServer() );
							
							try{
								// Add a device command to clear out the edgeServer property for the edge device
								this.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.EDGE_SERVER.getPropertyName() +","+ Reformat.escape(null), true );
							} catch (InterruptedException e) {
								logger.error( e );
							}
						}
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}else{
				throw new KmfException("Could not verify billing status for device " + this.getDeviceName()  + "(" + this.getDeviceId() + ")");
			}
	}
	
	public void setBillableStartDt(Date date){
		if(verifyBillableStartDt()){
			if(date != null){
				try {
					TripleDES encrypter = new TripleDES(getBillingStatusKey());
					setEncryptedBillableStartDt(encrypter.encryptToString(String.valueOf(date.getTime())));
					setReadableBillableStartDt(date);
				}catch(Exception e){
					logger.error(e);
				}
			}else{
				setEncryptedBillableStartDt(null);
				setReadableBillableStartDt(null);
			}
		}else{
			throw new KmfException("Could not verify billable start date for device " + this.getDeviceName()  + "(" + this.getDeviceId() + ")");
		}
	}
	
	public void setBillableEndDt(Date date){
		if(verifyBillableEndDt()){
			if(date != null){
				try {
					TripleDES encrypter = new TripleDES(getBillingStatusKey());
					setEncryptedBillableEndDt(encrypter.encryptToString(String.valueOf(date.getTime())));
					setReadableBillableEndDt(date);
				}catch(Exception e){
					logger.error(e);
				}
			}else{
				setEncryptedBillableEndDt(null);
				setReadableBillableEndDt(null);
			}
		}else{
			throw new KmfException("Could not verify billable end date for device " + this.getDeviceName()  + "(" + this.getDeviceId() + ")");
		}
	}
	
	public boolean verifyBillingStatus(){
		try {
			if(getEncryptedBillingStatus() != null && getReadableBillingStatus() != null){
				TripleDES decrypter = new TripleDES(getBillingStatusKey());
				String decryptedBillingStatus = decrypter.decryptFromString(getEncryptedBillingStatus());
				if(decryptedBillingStatus.equals(getReadableBillingStatus())){
					return true;
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}
	
	// Unlike the billing status which can't ever be null,
	// a null value for both dates is acceptable 
	public boolean verifyBillableStartDt(){
		try {
			if(getEncryptedBillableStartDt() != null && getReadableBillableStartDt() != null){
				TripleDES decrypter = new TripleDES(getBillingStatusKey());
				String decryptedBillableStartDt = decrypter.decryptFromString(getEncryptedBillableStartDt());
				
				// Since oracle doesn't store millis, ignore millis
				Long decryptedBillableStartMillis = Long.parseLong(decryptedBillableStartDt);
				
				// If the date is stored in memory (not fetched from oracle)
				if(decryptedBillableStartMillis.longValue() == getReadableBillableStartDt().getTime()){
					return true;
				}else{
					// Since oracle doesn't store millis, ignore millis
					decryptedBillableStartMillis -= decryptedBillableStartMillis % 1000;
					return decryptedBillableStartMillis.longValue() == getReadableBillableStartDt().getTime();
				}
			}else if(getEncryptedBillableStartDt() == null && getReadableBillableStartDt() == null){
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}
	
	// Unlike the billing status which can't ever be null,
	// a null value for both dates is acceptable
	public boolean verifyBillableEndDt(){
		try {
			if(getEncryptedBillableEndDt() != null && getReadableBillableEndDt() != null){
				TripleDES decrypter = new TripleDES(getBillingStatusKey());
				String decryptedBillableEndDt = decrypter.decryptFromString(getEncryptedBillableEndDt());
				
				Long decryptedBillableEndMillis = Long.parseLong(decryptedBillableEndDt);
				
				// If the date is stored in memory (not fetched from oracle)
				if(decryptedBillableEndMillis.longValue() == getReadableBillableEndDt().getTime()){
					return true;
				}else{
					// Since oracle doesn't store millis, ignore millis
					decryptedBillableEndMillis -= decryptedBillableEndMillis % 1000;
					return decryptedBillableEndMillis.longValue() == getReadableBillableEndDt().getTime();
				}
			}else if(getEncryptedBillableEndDt() == null && getReadableBillableEndDt() == null){
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}
	
	/**
	 * Thread used to sleep for the given maxHeartbeatInterval
	 * before sending a device command to clear out the 
	 * edgeServerOpenVpnHostIp for the given edge server.
	 * This will give the node device an opportunity to get its
	 * edgeServer=null device command before the edge server's
	 * openvpn server is shutdown (which would then cause the node
	 * device to not be able to receive its device command).
	 * If the node device does not heartbeat before the edge server
	 * receives its edgeServerOpenVpnHostIp=null device command 
	 * (and subsequently shuts down its openvpn server), the node
	 * will have to wait until self tests run, at which time it should
	 * receive its new connection information and realize that
	 * its edge server has been unassigned.
	 * 
	 * @author jrandesi
	 */
	private class EdgeServerOpenVpnHostIpThread extends Thread
	{
		private Device edgeServer;
		private String maxHeartbeatInterval;
		private String schemaName;
		private String appUserId;
		
		public EdgeServerOpenVpnHostIpThread()
		{
			// By explicitly setting this thread as a non-daemon thread, the JVM will not die
			// even if this theads parent thread finishes before this thread finishes 
			setDaemon( false );
		}

		/**
		 * @return the edgeServer
		 */
		public Device getEdgeServer() {
			return edgeServer;
		}

		/**
		 * @param edgeServer the edgeServer to set
		 */
		public void setEdgeServer(Device edgeServer) {
			this.edgeServer = edgeServer;
		}

		/**
		 * @return the maxHeartbeatInterval
		 */
		public String getMaxHeartbeatInterval() {
			return maxHeartbeatInterval;
		}

		/**
		 * @param maxHeartbeatInterval the maxHeartbeatInterval to set
		 */
		public void setMaxHeartbeatInterval(String maxHeartbeatInterval) {
			this.maxHeartbeatInterval = maxHeartbeatInterval;
		}

		/**
		 * @return the schemaName
		 */
		public String getSchemaName() {
			return schemaName;
		}

		/**
		 * @param schemaName the schemaName to set
		 */
		public void setSchemaName(String schemaName) {
			this.schemaName = schemaName;
		}

		/**
		 * @return the appUserId
		 */
		public String getAppUserId() {
			return appUserId;
		}

		/**
		 * @param appUserId the appUserId to set
		 */
		public void setAppUserId(String appUserId) {
			this.appUserId = appUserId;
		}
		
	}
	
	/**
	 * @return Returns the contentRotationTargets.
	 */
	public Set<ContentRotationTarget> getContentRotationTargets() {
		return contentRotationTargets;
	}
	
	/**
	 * @param contentRotationTargets The contentRotationTargets to set.
	 */
	public void setContentRotationTargets(
			Set<ContentRotationTarget> contentRotationTargets) {
		this.contentRotationTargets = contentRotationTargets;
	}
	/**
	 * @return Returns the autoUpdate.
	 */
	public DeviceAutoUpdateType getAutoUpdate() {
		return autoUpdate;
	}
	
	/**
	 * @param autoUpdate The autoUpdate to set.
	 */
	public void setAutoUpdate(DeviceAutoUpdateType autoUpdate) {
		this.autoUpdate = autoUpdate;
	}
	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return Returns the scalingMode.
	 */
	public DeviceScalingModeType getScalingMode() {
		return scalingMode;
	}
	
	/**
	 * @param scalingMode The scalingMode to set.
	 */
	public void setScalingMode(DeviceScalingModeType scalingMode) {
		this.scalingMode = scalingMode;
	}
	/**
	 * @return Returns the lastFileExistsDt.
	 */
	public Date getLastFileExistsDt() {
		return lastFileExistsDt;
	}
	
	/**
	 * @param lastFileExistsDt The lastFileExistsDt to set.
	 */
	public void setLastFileExistsDt(Date lastFileExistsDt) {
		this.lastFileExistsDt = lastFileExistsDt;
	}
	/**
	 * @return Returns the maxFileStorage.
	 */
	public Float getMaxFileStorage() {
		return maxFileStorage;
	}
	
	/**
	 * @param maxFileStorage The maxFileStorage to set.
	 */
	public void setMaxFileStorage(Float maxFileStorage) {
		this.maxFileStorage = maxFileStorage;
	}
	/**
	 * @return Returns the ipAddr.
	 */
	public String getIpAddr() {
		return ipAddr;
	}
	
	/**
	 * @param ipAddr The ipAddr to set.
	 */
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	/**
	 * @return Returns the macAddr.
	 */
	public String getMacAddr() {
		return macAddr;
	}
	
	/**
	 * @param macAddr The macAddr to set.
	 */
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	
	/**
	 * @return Returns the publicIpAddr.
	 */
	public String getPublicIpAddr() {
		return publicIpAddr;
	}
	
	/**
	 * @param publicIpAddr The publicIpAddr to set.
	 */
	public void setPublicIpAddr(String publicIpAddr) {
		this.publicIpAddr = publicIpAddr;
	}
	/**
	 * @return Returns the networkInterface.
	 */
	public String getNetworkInterface() {
		return networkInterface;
	}
	
	/**
	 * @param networkInterface The networkInterface to set.
	 */
	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}
	/**
	 * @return Returns the dhcpEnabled.
	 */
	public Boolean getDhcpEnabled() {
		return dhcpEnabled;
	}
	
	/**
	 * @param dhcpEnabled The dhcpEnabled to set.
	 */
	public void setDhcpEnabled(Boolean dhcpEnabled) {
		this.dhcpEnabled = dhcpEnabled;
	}
	
	/**
	 * @return Returns the gateway.
	 */
	public String getGateway() {
		return gateway;
	}
	
	/**
	 * @param gateway The gateway to set.
	 */
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	
	/**
	 * @return Returns the netmask.
	 */
	public String getNetmask() {
		return netmask;
	}
	
	/**
	 * @param netmask The netmask to set.
	 */
	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}
	/**
	 * @return Returns the dnsServer.
	 */
	public String getDnsServer() {
		return dnsServer;
	}
	
	/**
	 * @param dnsServer The dnsServer to set.
	 */
	public void setDnsServer(String dnsServer) {
		this.dnsServer = dnsServer;
	}
	/**
	 * @return Returns the lcdPin.
	 */
	public String getLcdPin() {
		return lcdPin;
	}
	
	/**
	 * @param lcdPin The lcdPin to set.
	 */
	public void setLcdPin(String lcdPin) {
		this.lcdPin = lcdPin;
	}
	/**
	 * @return Returns the screenshot.
	 */
	public String getScreenshot() {
		return screenshot;
	}
	
	/**
	 * @param screenshot The screenshot to set.
	 */
	public void setScreenshot(String screenshot) {
		this.screenshot = screenshot;
	}
	/**
	 * @return Returns the lcdBranding.
	 */
	public String getLcdBranding() {
		return lcdBranding;
	}
	
	/**
	 * @param lcdBranding The lcdBranding to set.
	 */
	public void setLcdBranding(String lcdBranding) {
		this.lcdBranding = lcdBranding;
	}
	/**
	 * @return Returns the initPropertiesFromDevice.
	 */
	public Boolean getInitPropertiesFromDevice() {
		return initPropertiesFromDevice;
	}
	
	/**
	 * @param initPropertiesFromDevice The initPropertiesFromDevice to set.
	 */
	public void setInitPropertiesFromDevice(Boolean initPropertiesFromDevice) {
		this.initPropertiesFromDevice = initPropertiesFromDevice;
	}
	/**
	 * @return Returns the initPropertiesProcessed.
	 */
	public Boolean getInitPropertiesProcessed() {
		return initPropertiesProcessed;
	}
	
	/**
	 * @param initPropertiesProcessed The initPropertiesProcessed to set.
	 */
	public void setInitPropertiesProcessed(Boolean initPropertiesProcessed) {
		this.initPropertiesProcessed = initPropertiesProcessed;
	}
	/**
	 * @return Returns the cpuThreshold.
	 */
	public Float getCpuThreshold() {
		return cpuThreshold;
	}
	
	/**
	 * @param cpuThreshold The cpuThreshold to set.
	 */
	public void setCpuThreshold(Float cpuThreshold) {
		this.cpuThreshold = cpuThreshold;
	}
	
	/**
	 * @return Returns the iowaitThreshold.
	 */
	public Float getIowaitThreshold() {
		return iowaitThreshold;
	}
	
	/**
	 * @param iowaitThreshold The iowaitThreshold to set.
	 */
	public void setIowaitThreshold(Float iowaitThreshold) {
		this.iowaitThreshold = iowaitThreshold;
	}
	
	/**
	 * @return Returns the loadThreshold.
	 */
	public Float getLoadThreshold() {
		return loadThreshold;
	}
	
	/**
	 * @param loadThreshold The loadThreshold to set.
	 */
	public void setLoadThreshold(Float loadThreshold) {
		this.loadThreshold = loadThreshold;
	}
	
	/**
	 * @return Returns the memoryThreshold.
	 */
	public Float getMemoryThreshold() {
		return memoryThreshold;
	}
	
	/**
	 * @param memoryThreshold The memoryThreshold to set.
	 */
	public void setMemoryThreshold(Float memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}
	
	/**
	 * @return Returns the outputMode.
	 */
	public OutputModeType getOutputMode() {
		return outputMode;
	}
	
	/**
	 * @param outputMode The outputMode to set.
	 */
	public void setOutputMode(OutputModeType outputMode) {
		this.outputMode = outputMode;
	}
	
	/**
	 * @return Returns the screenshotUploadTime.
	 */
	public Date getScreenshotUploadTime() {
		return screenshotUploadTime;
	}
	
	/**
	 * @param screenshotUploadTime The screenshotUploadTime to set.
	 */
	public void setScreenshotUploadTime(Date screenshotUploadTime) {
		this.screenshotUploadTime = screenshotUploadTime;
	}
	/**
	 * @return Returns the alertDevices.
	 */
	public Set getAlertDevices() {
		return alertDevices;
	}
	
	/**
	 * @param alertDevices The alertDevices to set.
	 */
	public void setAlertDevices(Set alertDevices) {
		this.alertDevices = alertDevices;
	}
	/**
	 * @return Returns the vpnIpAddr.
	 */
	public String getVpnIpAddr() {
		return vpnIpAddr;
	}
	
	/**
	 * @param vpnIpAddr The vpnIpAddr to set.
	 */
	public void setVpnIpAddr(String vpnIpAddr) {
		this.vpnIpAddr = vpnIpAddr;
	}
	
	
	public class DeviceCommandInfo
	{
		private String command;
		private String subCommand;
		private String parameters;		
		private String mcmId;
		
		public boolean equals(Object other)
		{	
			boolean result = false;		
			if(this == other) result = true;
			if( !(other instanceof DeviceCommandInfo) ) result = false;			
			DeviceCommandInfo c = (DeviceCommandInfo) other;		
			if(this.hashCode() == c.hashCode())
				result =  true;
			
			return result;					
		}
		/**
		 * 
		 */
		public int hashCode()
		{
			int result = "DeviceCommandInfo".hashCode();
			result = Reformat.getSafeHash( this.getCommand(), result, 3 );
			result = Reformat.getSafeHash( this.getSubCommand(), result, 7 );
			result = Reformat.getSafeHash( this.getParameters(), result, 11 );
			result = Reformat.getSafeHash( this.getMcmId(), result, 13 );
			return result < 0 ? -result : result;
		}		
		
		/**
		 * @return the command
		 */
		public String getCommand() {
			return command;
		}
		/**
		 * @param command the command to set
		 */
		public void setCommand(String command) {
			this.command = command;
		}
		/**
		 * @return the subCommand
		 */
		public String getSubCommand() {
			return subCommand;
		}
		/**
		 * @param subCommand the subCommand to set
		 */
		public void setSubCommand(String subCommand) {
			this.subCommand = subCommand;
		}
		/**
		 * @return the parameters
		 */
		public String getParameters() {
			return parameters;
		}
		/**
		 * @param parameters the parameters to set
		 */
		public void setParameters(String parameters) {
			this.parameters = parameters;
		}
		/**
		 * @return the mcmId
		 */
		public String getMcmId() {
			return mcmId;
		}
		/**
		 * @param mcmId the mcmId to set
		 */
		public void setMcmId(String mcmId) {
			this.mcmId = mcmId;
		}		
	}


	/**
	 * @return the deleted
	 */
	public Boolean getDeleted() {
		return deleted;
	}
	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	/**
	 * @return the filesyncStartTime
	 */
	public Date getFilesyncStartTime() {
		return filesyncStartTime;
	}
	/**
	 * @param filesyncStartTime the filesyncStartTime to set
	 */
	public void setFilesyncStartTime(Date filesyncStartTime) {
		this.filesyncStartTime = filesyncStartTime;
	}
	/**
	 * @return the filesyncEndTime
	 */
	public Date getFilesyncEndTime() {
		return filesyncEndTime;
	}
	/**
	 * @param filesyncEndTime the filesyncEndTime to set
	 */
	public void setFilesyncEndTime(Date filesyncEndTime) {
		this.filesyncEndTime = filesyncEndTime;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
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
	/**
	 * @return the dispatcherServers
	 */
	public String getDispatcherServers() {
		return dispatcherServers;
	}
	/**
	 * @param dispatcherServers the dispatcherServers to set
	 */
	public void setDispatcherServers(String dispatcherServers) {
		this.dispatcherServers = dispatcherServers;
	}
	/**
	 * @return the edgeServerOpenvpnHostIp
	 */
	public String getEdgeServerOpenvpnHostIp() {
		return edgeServerOpenvpnHostIp;
	}
	/**
	 * @param edgeServerOpenvpnHostIp the edgeServerOpenvpnHostIp to set
	 */
	public void setEdgeServerOpenvpnHostIp(String edgeServerOpenvpnHostIp) {
		this.edgeServerOpenvpnHostIp = edgeServerOpenvpnHostIp;
	}
	/**
	 * @return the redirectGateway
	 */
	public Boolean getRedirectGateway() {
		return redirectGateway;
	}
	/**
	 * @param redirectGateway the redirectGateway to set
	 */
	public void setRedirectGateway(Boolean redirectGateway) {
		this.redirectGateway = redirectGateway;
	}
	public Integer getxOffset() {
		return xOffset;
	}
	public void setxOffset(Integer offset) {
		xOffset = offset;
	}
	public Integer getyOffset() {
		return yOffset;
	}
	public void setyOffset(Integer offset) {
		yOffset = offset;
	}
	public Integer getZoom() {
		return zoom;
	}
	public void setZoom(Integer zoom) {
		this.zoom = zoom;
	}
	/**
	 * @return the connectAddress
	 */
	public String getConnectAddr() {
		return connectAddr;
	}
	/**
	 * @param connectAddress the connectAddress to set
	 */
	public void setConnectAddr(String connectAddr) {
		this.connectAddr = connectAddr;
	}
	public String getAntivirusScan() {
		return antivirusScan;
	}
	public void setAntivirusScan(String antivirusScan) {
		this.antivirusScan = antivirusScan;
	}
	public Integer getDownloadPriority() {
		return downloadPriority;
	}
	public void setDownloadPriority(Integer downloadPriority) {
		this.downloadPriority = downloadPriority;
	}
	public String getAudioNormalization() {
		return audioNormalization;
	}
	public void setAudioNormalization(String audioNormalization) {
		this.audioNormalization = audioNormalization;
	}
	public VenuePartner getVenuePartner() {
		return venuePartner;
	}
	public void setVenuePartner(VenuePartner venuePartner) {
		this.venuePartner = venuePartner;
	}
	public Device getMirrorSource() {
		return mirrorSource;
	}
	public void setMirrorSource(Device mirrorSource) {
		this.mirrorSource = mirrorSource;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public MulticastNetwork getMulticastNetwork() {
		return multicastNetwork;
	}
	public void setMulticastNetwork(MulticastNetwork multicastNetwork) {
		this.multicastNetwork = multicastNetwork;
	}
	
	public class DeviceInfo{
		private Long deviceId;
		private String deviceName;
		public Long getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(Long deviceId) {
			this.deviceId = deviceId;
		}
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
	}

	public String getControl() {
		return control;
	}
	public void setControl(String control) {
		this.control = control;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getEncryptionType() {
		return encryptionType;
	}
	public void setEncryptionType(String encryptionType) {
		this.encryptionType = encryptionType;
	}
	public String getPassphrase() {
		return passphrase;
	}
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
	public String getContentUpdateType() {
		return contentUpdateType;
	}
	public void setContentUpdateType(String contentUpdateType) {
		this.contentUpdateType = contentUpdateType;
		
		// Make sure that all methods call setContentUpdateType after calling setApplyAlerts
		if(contentUpdateType.equals(ContentUpdateType.NETWORK.getPersistentValue()) == false){
			this.setApplyAlerts( Boolean.FALSE );
		}
	}
	public String getEncryptedBillingStatus() {
		return encryptedBillingStatus;
	}
	public void setEncryptedBillingStatus(String encryptedBillingStatus) {
		this.encryptedBillingStatus = encryptedBillingStatus;
	}
	public String getReadableBillingStatus() {
		return readableBillingStatus;
	}
	public void setReadableBillingStatus(String readableBillingStatus) {
		this.readableBillingStatus = readableBillingStatus;
	}
	public String getEncryptedBillableStartDt() {
		return encryptedBillableStartDt;
	}
	public void setEncryptedBillableStartDt(String encryptedBillableStartDt) {
		this.encryptedBillableStartDt = encryptedBillableStartDt;
	}
	public Date getReadableBillableStartDt() {
		return readableBillableStartDt;
	}
	public void setReadableBillableStartDt(Date readableBillableStartDt) {
		this.readableBillableStartDt = readableBillableStartDt;
	}
	public String getEncryptedBillableEndDt() {
		return encryptedBillableEndDt;
	}
	public void setEncryptedBillableEndDt(String encryptedBillableEndDt) {
		this.encryptedBillableEndDt = encryptedBillableEndDt;
	}
	public Date getReadableBillableEndDt() {
		return readableBillableEndDt;
	}
	public void setReadableBillableEndDt(Date readableBillableEndDt) {
		this.readableBillableEndDt = readableBillableEndDt;
	}
	public String getAlphaCompositing() {
		return alphaCompositing;
	}
	public void setAlphaCompositing(String alphaCompositing) {
		this.alphaCompositing = alphaCompositing;
	}
	public Integer getBandwidthLimit() {
		return bandwidthLimit;
	}
	public void setBandwidthLimit(Integer bandwidthLimit) {
		this.bandwidthLimit = bandwidthLimit;
	}
	public AudioConnectionType getAudioConnection() {
		return audioConnection;
	}
	public void setAudioConnection(AudioConnectionType audioConnection) {
		this.audioConnection = audioConnection;
	}
	public String getActiveDispatcher() {
		return activeDispatcher;
	}
	public void setActiveDispatcher(String activeDispatcher) {
		this.activeDispatcher = activeDispatcher;
	}
	public Boolean getDeviceSideScheduling() {
		return deviceSideScheduling;
	}
	public void setDeviceSideScheduling(Boolean deviceSideScheduling) {
		this.deviceSideScheduling = deviceSideScheduling;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public Device getReplacedBy() {
		return replacedBy;
	}
	public void setReplacedBy(Device replacedBy) {
		this.replacedBy = replacedBy;
	}
	public FramesyncType getFramesync() {
		return framesync;
	}
	public void setFramesync(FramesyncType framesync) {
		this.framesync = framesync;
	}
	public Integer getInstallReleaseHour() {
		return installReleaseHour;
	}
	public void setInstallReleaseHour(Integer installReleaseHour) {
		this.installReleaseHour = installReleaseHour;
	}
	public Boolean getType2VideoPlayback() {
		return type2VideoPlayback;
	}
	public void setType2VideoPlayback(Boolean type2VideoPlayback) {
		this.type2VideoPlayback = type2VideoPlayback;
	}
	public Boolean getUseChrome() {
		return useChrome;
	}
	public void setUseChrome(Boolean useChrome) {
		this.useChrome = useChrome;
	}
	public Boolean getChromeDisableGpu() {
		return chromeDisableGpu;
	}
	public void setChromeDisableGpu(Boolean chromeDisableGpu) {
		this.chromeDisableGpu = chromeDisableGpu;
	}
}
