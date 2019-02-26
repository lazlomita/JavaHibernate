package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class DeviceIngesterColumnType extends PersistentStringEnum 
{	
	public static final DeviceIngesterColumnType NAME = new DeviceIngesterColumnType("name", "name");
	public static final DeviceIngesterColumnType MAC_ADDRESS = new DeviceIngesterColumnType("mac_address", "mac_address");
	public static final DeviceIngesterColumnType CS_HORIZON = new DeviceIngesterColumnType("cs_horizon", "cs_horizon");
	public static final DeviceIngesterColumnType CS_LENGTH = new DeviceIngesterColumnType("cs_length", "cs_length");
	public static final DeviceIngesterColumnType DELIVERY_MODE = new DeviceIngesterColumnType("delivery_mode", "delivery_mode");
	public static final DeviceIngesterColumnType LICENSE_STATUS = new DeviceIngesterColumnType("license_status", "license_status");
	public static final DeviceIngesterColumnType APPLY_ALERTS = new DeviceIngesterColumnType("apply_alerts", "apply_alerts");
	public static final DeviceIngesterColumnType AUTO_UPDATE = new DeviceIngesterColumnType("auto_update", "auto_update");
	public static final DeviceIngesterColumnType DISPLAY = new DeviceIngesterColumnType("display", "display");
	public static final DeviceIngesterColumnType OUTPUT_MODE = new DeviceIngesterColumnType("output_mode", "output_mode");
	public static final DeviceIngesterColumnType RESOLUTION = new DeviceIngesterColumnType("resolution", "resolution");
	public static final DeviceIngesterColumnType ORIENTATION = new DeviceIngesterColumnType("orientation", "orientation");
	public static final DeviceIngesterColumnType SCALING_MODE = new DeviceIngesterColumnType("scaling_mode", "scaling_mode");
	public static final DeviceIngesterColumnType TIMEZONE = new DeviceIngesterColumnType("timezone", "timezone");	
	public static final DeviceIngesterColumnType EDGE_SERVER = new DeviceIngesterColumnType("edge_server", "edge_server");
	public static final DeviceIngesterColumnType ACTIVE_MIRROR_SOURCE = new DeviceIngesterColumnType("active_mirror_source", "active_mirror_source");
	public static final DeviceIngesterColumnType MIRROR_SOURCE_DEVICES = new DeviceIngesterColumnType("mirror_source_devices", "mirror_source_devices");
	public static final DeviceIngesterColumnType HEARTBEAT_INTERVAL = new DeviceIngesterColumnType("heartbeat_interval", "heartbeat_interval");
	public static final DeviceIngesterColumnType MAX_FILE_STORAGE = new DeviceIngesterColumnType("max_file_storage", "max_file_storage");
	public static final DeviceIngesterColumnType MEMORY_THRESHOLD = new DeviceIngesterColumnType("memory_threshold", "memory_threshold");
	public static final DeviceIngesterColumnType IOWAIT_THRESHOLD = new DeviceIngesterColumnType("iowait_threshold", "iowait_threshold");
	public static final DeviceIngesterColumnType CPU_THRESHOLD = new DeviceIngesterColumnType("cpu_threshold", "cpu_threshold");	
	public static final DeviceIngesterColumnType LOAD_THRESHOLD = new DeviceIngesterColumnType("load_threshold", "load_threshold");
	public static final DeviceIngesterColumnType SCREENSHOT_UPLOAD_TIME = new DeviceIngesterColumnType("screenshot_upload_time", "screenshot_upload_time");
	public static final DeviceIngesterColumnType VOLUME = new DeviceIngesterColumnType("volume", "volume");			
	public static final DeviceIngesterColumnType LCD_PIN = new DeviceIngesterColumnType("lcd_pin", "lcd_pin");
	public static final DeviceIngesterColumnType LCD_BRANDING = new DeviceIngesterColumnType("lcd_branding", "lcd_branding");
	public static final DeviceIngesterColumnType DHCP_ENABLED = new DeviceIngesterColumnType("dhcp_enabled", "dhcp_enabled");
	public static final DeviceIngesterColumnType IP_ADDRESS = new DeviceIngesterColumnType("ip_address", "ip_address");
	public static final DeviceIngesterColumnType CONNECT_ADDRESS = new DeviceIngesterColumnType("connect_address", "connect_address");
	public static final DeviceIngesterColumnType NETMASK = new DeviceIngesterColumnType("netmask", "netmask");
	public static final DeviceIngesterColumnType GATEWAY = new DeviceIngesterColumnType("gateway", "gateway");
	public static final DeviceIngesterColumnType DNS_SERVER = new DeviceIngesterColumnType("dns_server", "dns_server");
	public static final DeviceIngesterColumnType NETWORK_INTERFACE = new DeviceIngesterColumnType("network_interface", "network_interface");		
	public static final DeviceIngesterColumnType FILESYNC_START_TIME = new DeviceIngesterColumnType("file_transfer_window_start_time", "file_transfer_window_start_time");
	public static final DeviceIngesterColumnType FILESYNC_END_TIME = new DeviceIngesterColumnType("file_transfer_window_end_time", "file_transfer_window_end_time");
	public static final DeviceIngesterColumnType CREATE_DEFAULT_MCM = new DeviceIngesterColumnType("create_default_mcm", "create_default_mcm");
	public static final DeviceIngesterColumnType TEMPLATE_DEVICE = new DeviceIngesterColumnType("template_device", "template_device");
	public static final DeviceIngesterColumnType ROLES = new DeviceIngesterColumnType("roles", "roles");
	public static final DeviceIngesterColumnType DEVICE_GROUPS = new DeviceIngesterColumnType("device_groups", "device_groups");
	public static final DeviceIngesterColumnType DISPATCHER_SERVERS = new DeviceIngesterColumnType("dispatcher_servers", "dispatcher_servers");
	public static final DeviceIngesterColumnType REDIRECT_GATEWAY = new DeviceIngesterColumnType("redirect_gateway", "redirect_gateway");
	public static final DeviceIngesterColumnType X_OFFSET = new DeviceIngesterColumnType("horz_shift", "horz_shift");
	public static final DeviceIngesterColumnType Y_OFFSET = new DeviceIngesterColumnType("vert_shift", "vert_shift");
	public static final DeviceIngesterColumnType ZOOM = new DeviceIngesterColumnType("zoom", "zoom");
	public static final DeviceIngesterColumnType ANTIVIRUS_SCAN_TIME = new DeviceIngesterColumnType("antivirus_scan_time", "antivirus_scan_time");
	public static final DeviceIngesterColumnType DOWNLOAD_PRIORITY = new DeviceIngesterColumnType("download_priority", "download_priority");
	public static final DeviceIngesterColumnType AUDIO_NORMALIZATION = new DeviceIngesterColumnType("audio_normalization", "audio_normalization");
	public static final DeviceIngesterColumnType SEGMENTS = new DeviceIngesterColumnType("schedules", "schedules");
	public static final DeviceIngesterColumnType VENUE_NAME = new DeviceIngesterColumnType("venue_name", "venue_name");
	public static final DeviceIngesterColumnType ALPHA_COMPOSITING = new DeviceIngesterColumnType("alpha_compositing", "alpha_compositing");
	public static final DeviceIngesterColumnType BANDWIDTH_LIMIT = new DeviceIngesterColumnType("bandwidth_limit", "bandwidth_limit");
	public static final DeviceIngesterColumnType AUDIO_CONNECTION = new DeviceIngesterColumnType("audio_connection", "audio_connection");
	public static final DeviceIngesterColumnType FRAMESYNC = new DeviceIngesterColumnType("framesync", "framesync");
	public static final DeviceIngesterColumnType REMOVE_DEVICE = new DeviceIngesterColumnType("remove_device", "remove_device");
	public static final DeviceIngesterColumnType TYPE2_VIDEO_PLAYER = new DeviceIngesterColumnType("type2_video_playback", "type2_video_playback");
	public static final DeviceIngesterColumnType USE_CHROME = new DeviceIngesterColumnType("use_chrome", "use_chrome");
	public static final DeviceIngesterColumnType CHROME_DISABLE_GPU = new DeviceIngesterColumnType("chrome_disable_gpu", "chrome_disable_gpu");
	public static final Map<String, DeviceIngesterColumnType> INSTANCES = new HashMap<String, DeviceIngesterColumnType>();
	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(NAME.toString(), NAME);
		INSTANCES.put(MAC_ADDRESS.toString(), MAC_ADDRESS);
		INSTANCES.put(CS_HORIZON.toString(), CS_HORIZON);
		INSTANCES.put(CS_LENGTH.toString(), CS_LENGTH);
		INSTANCES.put(DELIVERY_MODE.toString(), DELIVERY_MODE);
		INSTANCES.put(LICENSE_STATUS.toString(), LICENSE_STATUS);
		INSTANCES.put(APPLY_ALERTS.toString(), APPLY_ALERTS);
		INSTANCES.put(AUTO_UPDATE.toString(), AUTO_UPDATE);
		INSTANCES.put(DISPLAY.toString(), DISPLAY);
		INSTANCES.put(OUTPUT_MODE.toString(), OUTPUT_MODE);
		INSTANCES.put(RESOLUTION.toString(), RESOLUTION);
		INSTANCES.put(ORIENTATION.toString(), ORIENTATION);
		INSTANCES.put(SCALING_MODE.toString(), SCALING_MODE);
		INSTANCES.put(TIMEZONE.toString(), TIMEZONE);
		INSTANCES.put(EDGE_SERVER.toString(), EDGE_SERVER);
		INSTANCES.put(ACTIVE_MIRROR_SOURCE.toString(), ACTIVE_MIRROR_SOURCE);
		INSTANCES.put(MIRROR_SOURCE_DEVICES.toString(), MIRROR_SOURCE_DEVICES);
		INSTANCES.put(HEARTBEAT_INTERVAL.toString(), HEARTBEAT_INTERVAL);
		INSTANCES.put(MAX_FILE_STORAGE.toString(), MAX_FILE_STORAGE);
		INSTANCES.put(MEMORY_THRESHOLD.toString(), MEMORY_THRESHOLD);
		INSTANCES.put(IOWAIT_THRESHOLD.toString(), IOWAIT_THRESHOLD);
		INSTANCES.put(CPU_THRESHOLD.toString(), CPU_THRESHOLD);
		INSTANCES.put(LOAD_THRESHOLD.toString(), LOAD_THRESHOLD);
		INSTANCES.put(SCREENSHOT_UPLOAD_TIME.toString(), SCREENSHOT_UPLOAD_TIME);
		INSTANCES.put(VOLUME.toString(), VOLUME);
		INSTANCES.put(LCD_PIN.toString(), LCD_PIN);
		INSTANCES.put(LCD_BRANDING.toString(), LCD_BRANDING);
		INSTANCES.put(DHCP_ENABLED.toString(), DHCP_ENABLED);		
		INSTANCES.put(IP_ADDRESS.toString(), IP_ADDRESS);		
		INSTANCES.put(NETMASK.toString(), NETMASK);
		INSTANCES.put(GATEWAY.toString(), GATEWAY);
		INSTANCES.put(DNS_SERVER.toString(), DNS_SERVER);
		INSTANCES.put(NETWORK_INTERFACE.toString(), NETWORK_INTERFACE);		
		INSTANCES.put(FILESYNC_START_TIME.toString(), FILESYNC_START_TIME);
		INSTANCES.put(FILESYNC_END_TIME.toString(), FILESYNC_END_TIME);
		INSTANCES.put(CREATE_DEFAULT_MCM.toString(), CREATE_DEFAULT_MCM);
		INSTANCES.put(TEMPLATE_DEVICE.toString(), TEMPLATE_DEVICE);
		INSTANCES.put(ROLES.toString(), ROLES);
		INSTANCES.put(DEVICE_GROUPS.toString(), DEVICE_GROUPS);		
		INSTANCES.put(DISPATCHER_SERVERS.toString(), DISPATCHER_SERVERS);		
		INSTANCES.put(REDIRECT_GATEWAY.toString(), REDIRECT_GATEWAY);
		INSTANCES.put(X_OFFSET.toString(), X_OFFSET);
		INSTANCES.put(Y_OFFSET.toString(), Y_OFFSET);
		INSTANCES.put(ZOOM.toString(), ZOOM);
		INSTANCES.put(CONNECT_ADDRESS.toString(), CONNECT_ADDRESS);
		INSTANCES.put(ANTIVIRUS_SCAN_TIME.toString(), ANTIVIRUS_SCAN_TIME);
		INSTANCES.put(DOWNLOAD_PRIORITY.toString(), DOWNLOAD_PRIORITY);
		INSTANCES.put(AUDIO_NORMALIZATION.toString(), AUDIO_NORMALIZATION);
		INSTANCES.put(SEGMENTS.toString(), SEGMENTS);
		INSTANCES.put(VENUE_NAME.toString(), VENUE_NAME);
		INSTANCES.put(ALPHA_COMPOSITING.toString(), ALPHA_COMPOSITING);
		INSTANCES.put(BANDWIDTH_LIMIT.toString(), BANDWIDTH_LIMIT);
		INSTANCES.put(AUDIO_CONNECTION.toString(), AUDIO_CONNECTION);
		INSTANCES.put(FRAMESYNC.toString(), FRAMESYNC);
		INSTANCES.put(REMOVE_DEVICE.toString(), REMOVE_DEVICE);
		INSTANCES.put(TYPE2_VIDEO_PLAYER.toString(), TYPE2_VIDEO_PLAYER);
		INSTANCES.put(USE_CHROME.toString(), USE_CHROME);
		INSTANCES.put(CHROME_DISABLE_GPU.toString(), CHROME_DISABLE_GPU);
	}
	/**
	 * 
	 *
	 */
	public DeviceIngesterColumnType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected DeviceIngesterColumnType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public String getDeviceIngesterColumnTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @param deviceIngesterColumnName
	 * @return
	 */
	public static DeviceIngesterColumnType getDeviceIngesterColumnType(String deviceIngesterColumnName)
	{
		return (DeviceIngesterColumnType) INSTANCES.get( deviceIngesterColumnName );
	}	
}
