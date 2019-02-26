package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import parkmedia.device.configurator.DeviceProperty;
import com.kuvata.configurator.ConnectionInfoProperty;
import com.kuvata.configurator.KuvataProperty;
import com.kuvata.configurator.McmProperty;
import com.kuvata.configurator.OrientationPropertyLinux;
import com.kuvata.configurator.OrientationPropertyWindows;
import com.kuvata.configurator.PlatformPropertyLinux;
import com.kuvata.configurator.PlatformPropertyWindows;
import com.kuvata.configurator.ResolutionPropertyLinux;
import com.kuvata.configurator.ResolutionPropertyWindows;
import com.kuvata.configurator.StatusProperty;
import com.kuvata.configurator.TimezonePropertyLinux;
import com.kuvata.configurator.TimezonePropertyWindows;
import com.kuvata.kmf.Device;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class DevicePropertyType 
{
	private static final String PLATFORM_LINUX = "linux";
	private static final String PLATFORM_WINDOWS = "windows";
	
	// Device properties
	private static final String PROPERTY_RESOLUTION = "resolution";
	private static final String PROPERTY_ROTATION = "rotation";
	private static final String PROPERTY_TIMEZONE = "timezone";
	private static final String PROPERTY_HEARTBEAT_INTERVAL = "Heartbeat.interval";	
	private static final String PROPERTY_DISPLAY = "Configurator.display";
	private static final String PROPERTY_DISK_CAPACITY = "diskCapacity";
	private static final String PROPERTY_DISK_USAGE = "diskUsage";
	private static final String PROPERTY_VERSION = "version";
	private static final String PROPERTY_SCALING_MODE = "Presenter.scalingMode";	
	private static final String PROPERTY_MAX_FILE_STORAGE = "maxFileStorage";
	private static final String PROPERTY_MAC_ADDRESS = "macAddress";
	private static final String PROPERTY_IP_ADDRESS = "ipAddr";
	private static final String PROPERTY_VPN_IP_ADDRESS = "vpnIpAddr";
	private static final String PROPERTY_GATEWAY = "gateway";
	private static final String PROPERTY_NETMASK = "netmask";
	private static final String PROPERTY_DHCP_ENABLED = "dhcpEnabled";
	private static final String PROPERTY_DNS_SERVER = "dnsServer";
	private static final String PROPERTY_SSID = "ssid";
	private static final String PROPERTY_ENCRYPTION_TYPE = "encryptionType";
	private static final String PROPERTY_PASSPHRASE = "passphrase";
	private static final String PROPERTY_NETWORK_INTERFACE = "Dispatcher.networkInterface";
	private static final String PROPERTY_MAC_ADDR_INTERFACE = "Dispatcher.macAddrInterface";
	private static final String PROPERTY_DISPATCHER_SERVERS = "DispatcherServers";
	private static final String PROPERTY_VPN_SERVER = "VpnServer";
	private static final String PROPERTY_EDGE_SERVER = "EdgeServer";
	private static final String PROPERTY_MASTER_DEVICE_ID = "MasterDeviceId";
	private static final String PROPERTY_LCD_PIN = "LcdPin";
	private static final String PROPERTY_LCD_BRANDING = "LcdBranding";
	private static final String PROPERTY_VOLUME = "Volume";
	private static final String PROPERTY_IOWAIT_THRESHOLD = "IowaitThreshold";
	private static final String PROPERTY_CPU_THRESHOLD = "CpuThreshold";
	private static final String PROPERTY_MEMORY_THRESHOLD = "MemoryThreshold";
	private static final String PROPERTY_LOAD_THRESHOLD = "LoadThreshold";
	private static final String PROPERTY_IOWAIT_UTILIZATION = "iowaitUtilization";
	private static final String PROPERTY_CPU_UTILIZATION = "cpuUtilization";
	private static final String PROPERTY_MEMORY_UTILIZATION = "memoryUtilization";
	private static final String PROPERTY_LOAD_UTILIZATION = "loadUtilization";
	private static final String PROPERTY_OUTPUT_MODE = "OutputMode";
	private static final String PROPERTY_SCREENSHOT_UPLOAD_TIME = "ScreenshotUploadTime";
	private static final String PROPERTY_SSHD_SERVER = "SshdServer";
	private static final String PROPERTY_AUTO_UPDATE = "AutoUpdate";
	private static final String PROPERTY_FILESYNC_START_TIME = "FilesyncStartTime";
	private static final String PROPERTY_FILESYNC_END_TIME = "FilesyncEndTime";
	private static final String PROPERTY_OS_VERSION = "OsVersion";
	private static final String PROPERTY_EDGE_SERVER_OPENVPN_HOST_IP = "EdgeServerOpenvpnHostIp";	
	private static final String PROPERTY_REDIRECT_GATEWAY = "RedirectGateway";
	private static final String PROPERTY_X_OFFSET = "xOffset";
	private static final String PROPERTY_Y_OFFSET = "yOffset";
	private static final String PROPERTY_ZOOM = "Zoom";
	private static final String PROPERTY_ANTIVIRUS_SCAN = "AntivirusScan";
	private static final String PROPERTY_AUDIO_NORMALIZATION = "AudioNormalization";
	private static final String PROPERTY_CONNECT_ADDRESS = "ConnectAddress";
	private static final String PROPERTY_CONTENT_UPDATE_TYPE = "ContentUpdateType";
	private static final String PROPERTY_DEVICE_NAME = "deviceName";
	private static final String PROPERTY_MULTICAST_ADDRESS = "MulticastAddress";
	private static final String PROPERTY_MULTICAST_SERVER_PORT = "MulticastServerPort";
	private static final String PROPERTY_MULTICAST_DEVICE_PORT = "MulticastDevicePort";
	private static final String PROPERTY_CONTROL = "control";
	private static final String PROPERTY_ALPHA_COMPOSITING = "AlphaCompositing";
	private static final String PROPERTY_BANDWIDTH_LIMIT = "BandwidthLimit";
	private static final String PROPERTY_AUDIO_CONNECTION = "AudioConnection";
	private static final String PROPERTY_FRAMESYNC = "Framesync";
	private static final String PROPERTY_TYPE2_VIDEO_PLAYBACK = "Presenter.enableType2Video";
	private static final String PROPERTY_USE_CHROME = "Presenter.useChrome";
	private static final String PROPERTY_CHROME_DISABLE_GPU = "Presenter.chromeDisableGpu";
	
	private static final String PROPERTY_RESOLUTION_DISPLAY_NAME = "Resolution";
	private static final String PROPERTY_ROTATION_DISPLAY_NAME = "Display Orientation";
	private static final String PROPERTY_TIMEZONE_DISPLAY_NAME = "Timezone";
	private static final String PROPERTY_HEARTBEAT_INTERVAL_DISPLAY_NAME = "Heartbeat Interval";	
	private static final String PROPERTY_DISPLAY_DISPLAY_NAME = "Display";
	private static final String PROPERTY_DISK_CAPACITY_DISPLAY_NAME = "Disk Capacity";
	private static final String PROPERTY_DISK_USAGE_DISPLAY_NAME = "Disk Usage";
	private static final String PROPERTY_VERSION_DISPLAY_NAME = "Version";
	private static final String PROPERTY_SCALING_MODE_DISPLAY_NAME = "Scaling Mode";	
	private static final String PROPERTY_MAX_FILE_STORAGE_DISPLAY_NAME = "Max File Storage";
	private static final String PROPERTY_MAC_ADDRESS_DISPLAY_NAME = "MAC Address";
	private static final String PROPERTY_IP_ADDRESS_DISPLAY_NAME = "IP Address";
	private static final String PROPERTY_VPN_IP_ADDRESS_DISPLAY_NAME = "VPN IP Address";
	private static final String PROPERTY_GATEWAY_DISPLAY_NAME = "Gateway";
	private static final String PROPERTY_NETMASK_DISPLAY_NAME = "Netmask";
	private static final String PROPERTY_DHCP_ENABLED_DISPLAY_NAME = "DHCP Enabled";
	private static final String PROPERTY_DNS_SERVER_DISPLAY_NAME = "DNS Server";
	private static final String PROPERTY_SSID_DISPLAY_NAME = "SSID";
	private static final String PROPERTY_ENCRYPTION_TYPE_DISPLAY_NAME = "Encryption Type";
	private static final String PROPERTY_PASSPHRASE_DISPLAY_NAME = "Passphrase";
	private static final String PROPERTY_NETWORK_INTERFACE_DISPLAY_NAME = "Network Interface";
	private static final String PROPERTY_MAC_ADDR_INTERFACE_DISPLAY_NAME = "Mac Address Interface";
	private static final String PROPERTY_DISPATCHER_SERVERS_DISPLAY_NAME = "Dispatcher Servers";
	private static final String PROPERTY_VPN_SERVER_DISPLAY_NAME = "VPN Server IP";
	private static final String PROPERTY_EDGE_SERVER_DISPLAY_NAME = "Edge Server";
	private static final String PROPERTY_MASTER_DEVICE_ID_DISPLAY_NAME = "Master Device";
	private static final String PROPERTY_LCD_PIN_DISPLAY_NAME = "LCD PIN";
	private static final String PROPERTY_LCD_BRANDING_DISPLAY_NAME = "LCD Branding";
	private static final String PROPERTY_VOLUME_DISPLAY_NAME = "Volume";
	private static final String PROPERTY_IOWAIT_THRESHOLD_DISPLAY_NAME = "I/O Wait Threshold";
	private static final String PROPERTY_CPU_THRESHOLD_DISPLAY_NAME = "CPU Utilization Threshold";
	private static final String PROPERTY_MEMORY_THRESHOLD_DISPLAY_NAME = "Memory Usage Threshold";
	private static final String PROPERTY_LOAD_THRESHOLD_DISPLAY_NAME = "Load Threshold";
	private static final String PROPERTY_IOWAIT_UTILIZATION_DISPLAY_NAME = "I/O Wait";
	private static final String PROPERTY_CPU_UTILIZATION_DISPLAY_NAME = "CPU Utilization";
	private static final String PROPERTY_MEMORY_UTILIZATION_DISPLAY_NAME = "Memory Utilization";
	private static final String PROPERTY_LOAD_UTILIZATION_DISPLAY_NAME = "Load";
	private static final String PROPERTY_OUTPUT_MODE_DISPLAY_NAME = "Output Mode";
	private static final String PROPERTY_SCREENSHOT_UPLOAD_TIME_DISPLAY_NAME = "Screenshot Upload Time";
	private static final String PROPERTY_SSHD_SERVER_DISPLAY_NAME = "SSH Server";
	private static final String PROPERTY_AUTO_UPDATE_DISPLAY_NAME = "Auto Update";
	private static final String PROPERTY_FILESYNC_START_TIME_DISPLAY_NAME = "File Transfer Window Start Time";
	private static final String PROPERTY_FILESYNC_END_TIME_DISPLAY_NAME = "File Transfer Window End Time";
	private static final String PROPERTY_OS_VERSION_DISPLAY_NAME = "Operating System Version";
	private static final String PROPERTY_EDGE_SERVER_OPENVPN_HOST_IP_DISPLAY_NAME = "Edge Server OpenVPN Host IP";
	private static final String PROPERTY_REDIRECT_GATEWAY_DISPLAY_NAME = "Route internet requests through edge server";
	private static final String PROPERTY_X_OFFSET_DISPLAY_NAME = "Horizontal Shift";
	private static final String PROPERTY_Y_OFFSET_DISPLAY_NAME = "Vertical Shift";
	private static final String PROPERTY_ZOOM_DISPLAY_NAME = "Zoom";
	private static final String PROPERTY_ANTIVIRUS_SCAN_DISPLAY_NAME = "Antivirus Scan";
	private static final String PROPERTY_AUDIO_NORMALIZATION_DISPLAY_NAME = "Audio Normalization";
	private static final String PROPERTY_CONNECT_ADDRESS_DISPLAY_NAME = "Connect Address";
	private static final String PROPERTY_CONTENT_UPDATE_TYPE_DISPLAY_NAME = "Content Update Type";
	private static final String PROPERTY_DEVICE_NAME_DISPLAY_NAME = "Device Name";
	private static final String PROPERTY_MULTICAST_ADDRESS_DISPLAY_NAME = "Multicast Address";
	private static final String PROPERTY_MULTICAST_SERVER_PORT_DISPLAY_NAME = "Multicast Server Port";
	private static final String PROPERTY_MULTICAST_DEVICE_PORT_DISPLAY_NAME = "Multicast Device Port";
	private static final String PROPERTY_CONTROL_DISPLAY_NAME = "Control";
	private static final String PROPERTY_ALPHA_COMPOSITING_DISPLAY_NAME = "Alpha Compositing";
	private static final String PROPERTY_BANDWIDTH_LIMIT_DISPLAY_NAME = "Throttle Downstream Bandwidth";
	private static final String PROPERTY_AUDIO_CONNECTION_DISPLAY_NAME = "Audio Connection";
	private static final String PROPERTY_FRAMESYNC_DISPLAY_NAME = "Framesync";
	private static final String PROPERTY_TYPE2_VIDEO_PLAYBACK_DISPLAY_NAME = "Type2 Video Playback";
	private static final String PROPERTY_USE_CHROME_DISPLAY_NAME = "Use Chrome";
	private static final String PROPERTY_CHROME_DISABLE_GPU_DISPLAY_NAME = "Chrome disable GPU";
	
	// Mcm properties
	private static final String PROPERTY_MCM_HOSTSTRING = "mcmHoststring";
	private static final String PROPERTY_MCM_PERIPHERAL_NAME = "peripheralName";
	private static final String PROPERTY_MCM_PERIPHERAL_TYPE = "peripheralType";	
	private static final String PROPERTY_SERIAL_PORT = "serialPort";
	private static final String PROPERTY_SWITCH_TO_AUX_COMMAND = "switchToAuxCommand";
	private static final String PROPERTY_SWITCH_TO_MEDIACAST_COMMAND = "switchToMediacastCommand";
	private static final String PROPERTY_CURRENT_POWER_COMMAND = "currentPowerCommand";
	private static final String PROPERTY_CURRENT_VOLUME_COMMAND = "currentVolumeCommand";
	private static final String PROPERTY_CURRENT_BRIGHTNESS_COMMAND = "currentBrightnessCommand";
	private static final String PROPERTY_CURRENT_CONTRAST_COMMAND = "currentContrastCommand";
	private static final String PROPERTY_CURRENT_INPUT_COMMAND = "currentInputCommand";
	private static final String PROPERTY_CURRENT_BUTTON_LOCK_COMMAND = "currentButtonLockCommand";
	private static final String PROPERTY_CURRENT_AUDIO_SOURCE_COMMAND = "currentAudioSourceCommand";
	private static final String PROPERTY_CURRENT_SPEAKER_COMMAND = "currentSpeakerCommand";
	private static final String PROPERTY_CURRENT_POWER_SAVE_COMMAND = "currentPowerSaveCommand";
	private static final String PROPERTY_CURRENT_REMOTE_CONTROL_COMMAND = "currentRemoteControlCommand";
	private static final String PROPERTY_CURRENT_TEMPERATURE_COMMAND = "currentTemperatureCommand";
	private static final String PROPERTY_CURRENT_SERIAL_CODE_COMMAND = "currentSerialCodeCommand";
	private static final String PROPERTY_SET_VOLUME_COMMAND = "setVolumeCommand";
	private static final String PROPERTY_AUTO_ADJUST_COMMAND = "autoAdjustCommand";
	private static final String PROPERTY_DIAGNOSTIC_INTERVAL = "diagnosticInterval";
	private static final String PROPERTY_MCM_IMPLEMENTATION_TYPE = "implementationType";
	private static final String PROPERTY_SET_INVERSION_ON_COMMAND = "setInversionOnCommand";
	private static final String PROPERTY_SET_INVERSION_OFF_COMMAND = "setInversionOffCommand";
	private static final String PROPERTY_SET_SIGNAL_PATTERN_ON_COMMAND = "setSignalPatternOnCommand";
	private static final String PROPERTY_SET_SIGNAL_PATTERN_OFF_COMMAND = "setSignalPatternOffCommand";
	private static final String PROPERTY_SET_PIXEL_SHIFT_ON_COMMAND = "setPixelShiftOnCommand";
	private static final String PROPERTY_SET_PIXEL_SHIFT_OFF_COMMAND = "setPixelShiftOffCommand";
	private static final String PROPERTY_VOLUME_OFFSET_COMMAND = "volumeOffsetCommand";
	private static final String PROPERTY_MIN_GAIN_COMMAND = "minGainCommand";
	private static final String PROPERTY_MAX_GAIN_COMMAND = "maxGainCommand";
	private static final String PROPERTY_MUTE_COMMAND = "muteCommand";
	private static final String PROPERTY_WOOFER_OFFSET_COMMAND = "wooferOffsetCommand";
	private static final String PROPERTY_RESPONSE_TIME_COMMAND = "responseTimeCommand";
	private static final String PROPERTY_INPUT_SELECTION_COMMAND = "inputSelectionCommand";
	private static final String PROPERTY_FRONT_PANEL_LOCK_COMMAND = "frontPanelLockCommand";
	private static final String PROPERTY_VIDEO_MUTE_COMMAND = "videoMuteCommand";
	private static final String PROPERTY_AUDIO_MUTE_COMMAND = "audioMuteCommand";
	
	// Mcm commands
	public static final String PROPERTY_CURRENT_AUDIO_INPUT = "audioInput";
	public static final String PROPERTY_CURRENT_AMBIENT_NOISE = "currentAmbientNoise";
	public static final String PROPERTY_APPLY_SETTINGS_COMMAND = "applySettings";
	public static final String PROPERTY_CURRENT_SETTINGS_COMMAND = "currentSettings";
	public static final String PROPERTY_BUTTON_LOCK_ON_COMMAND = "buttonLockOnCommand";
	public static final String PROPERTY_BUTTON_LOCK_OFF_COMMAND = "buttonLockOffCommand";
	public static final String PROPERTY_SET_BRIGHTNESS_SIGNAL_COMMAND = "setBrightnessSignalCommand";
	public static final String PROPERTY_SET_CONTRAST_SIGNAL_COMMAND = "setContrastSignalCommand";
	public static final String PROPERTY_ON_COMMAND = "onCommand";
	public static final String PROPERTY_OFF_COMMAND = "offCommand";
	public static final String PROPERTY_OSD_ON_COMMAND = "osdOnCommand";
	public static final String PROPERTY_OSD_OFF_COMMAND = "osdOffCommand";
	public static final String PROPERTY_OUTPUT_COMMAND = "outputCommand";
	public static final String PROPERTY_MASTER_RESET_COMMAND = "masterReset";
	public static final String PROPERTY_VIDEO_INPUT = "videoInput";
	public static final String PROPERTY_AUDIO_INPUT = "audioInput";
	public static final String PROPERTY_SET_AUDIO_SOURCE_COMMAND = "setAudioSourceCommand";
	public static final String PROPERTY_SET_SPEAKER_COMMAND = "setSpeakerCommand";
	public static final String PROPERTY_SET_POWER_SAVE_COMMAND = "setPowerSaveCommand";
	public static final String PROPERTY_SET_REMOTE_CONTROL_COMMAND = "setRemoteControlCommand";
	public static final String PROPERTY_DNS_SERVER_1 = "DNS1";
	
	private static final String PROPERTY_MCM_HOSTSTRING_DISPLAY_NAME = "MCM Hoststring";
	private static final String PROPERTY_MCM_PERIPHERAL_NAME_DISPLAY_NAME = "Peripheral";
	private static final String PROPERTY_MCM_PERIPHERAL_TYPE_DISPLAY_NAME = "Peripheral Type";
	private static final String PROPERTY_SERIAL_PORT_DISPLAY_NAME = "Serial Port";
	private static final String PROPERTY_SWITCH_TO_AUX_COMMAND_DISPLAY_NAME = "Switch To Auxiliary Input";
	private static final String PROPERTY_SWITCH_TO_MEDIACAST_COMMAND_DISPLAY_NAME = "Switch To Mediacast Command";
	private static final String PROPERTY_ON_COMMAND_DISPLAY_NAME = "On Command";
	private static final String PROPERTY_OFF_COMMAND_DISPLAY_NAME = "Off Command";
	private static final String PROPERTY_CURRENT_POWER_COMMAND_DISPLAY_NAME = "Current Power Command";
	private static final String PROPERTY_CURRENT_VOLUME_COMMAND_DISPLAY_NAME = "Current Volume Command";
	private static final String PROPERTY_CURRENT_BRIGHTNESS_COMMAND_DISPLAY_NAME = "Current Brightness Command";
	private static final String PROPERTY_CURRENT_CONTRAST_COMMAND_DISPLAY_NAME = "Current Contrast Command";
	private static final String PROPERTY_CURRENT_INPUT_COMMAND_DISPLAY_NAME = "Current Input Command";
	private static final String PROPERTY_CURRENT_BUTTON_LOCK_COMMAND_DISPLAY_NAME = "Current Button Lock Command";
	private static final String PROPERTY_SET_VOLUME_COMMAND_DISPLAY_NAME = "Set Volume Command";
	private static final String PROPERTY_OSD_ON_COMMAND_DISPLAY_NAME = "Osd On Command";
	private static final String PROPERTY_OSD_OFF_COMMAND_DISPLAY_NAME = "Osd Off Command";
	private static final String PROPERTY_AUTO_ADJUST_COMMAND_DISPLAY_NAME = "Auto Adjust Command";
	private static final String PROPERTY_DIAGNOSTIC_INTERVAL_DISPLAY_NAME = "Diagnostic Interval";
	private static final String PROPERTY_MCM_IMPLEMENTATION_TYPE_DISPLAY_NAME = "Implementation Type";
	private static final String PROPERTY_SET_INVERSION_ON_COMMAND_DISPLAY_NAME = "Set Inversion On Command";
	private static final String PROPERTY_SET_INVERSION_OFF_COMMAND_DISPLAY_NAME = "Set Inversion Off Command";
	private static final String PROPERTY_SET_SIGNAL_PATTERN_ON_COMMAND_DISPLAY_NAME = "Set Signal Pattern On Command";
	private static final String PROPERTY_SET_SIGNAL_PATTERN_OFF_COMMAND_DISPLAY_NAME = "Set Signal Pattern Off Command";
	private static final String PROPERTY_SET_PIXEL_SHIFT_ON_COMMAND_DISPLAY_NAME = "Set Pixel Shift On Command";
	private static final String PROPERTY_SET_PIXEL_SHIFT_OFF_COMMAND_DISPLAY_NAME = "Set Pixel Shift Off Command";
	private static final String PROPERTY_VOLUME_OFFSET_COMMAND_DISPLAY_NAME = "Volume Offset Command";
	private static final String PROPERTY_MIN_GAIN_COMMAND_DISPLAY_NAME = "Minimum Gain Command";
	private static final String PROPERTY_MAX_GAIN_COMMAND_DISPLAY_NAME = "Maximum Gain Command";
	private static final String PROPERTY_MUTE_COMMAND_DISPLAY_NAME = "Mute Command";
	private static final String PROPERTY_WOOFER_OFFSET_COMMAND_DISPLAY_NAME = "Woofer Offset Command";
	private static final String PROPERTY_RESPONSE_TIME_COMMAND_DISPLAY_NAME = "Response Time Command";
	private static final String PROPERTY_BUTTON_LOCK_ON_COMMAND_DISPLAY_NAME = "Button Lock On Command";
	private static final String PROPERTY_BUTTON_LOCK_OFF_COMMAND_DISPLAY_NAME = "Button Lock Off Command";
	private static final String PROPERTY_SET_BRIGHTNESS_SIGNAL_COMMAND_DISPLAY_NAME = "Button Brightness Signal Command";
	private static final String PROPERTY_SET_CONTRAST_SIGNAL_COMMAND_DISPLAY_NAME = "Button Contrast Signal Command";
	private static final String PROPERTY_INPUT_SELECTION_COMMAND_DISPLAY_NAME = "Input Selection Command";
	private static final String PROPERTY_FRONT_PANEL_LOCK_COMMAND_DISPLAY_NAME = "Front Panel Lock Command";
	private static final String PROPERTY_VIDEO_MUTE_COMMAND_DISPLAY_NAME = "Video Mute Command";
	private static final String PROPERTY_AUDIO_MUTE_COMMAND_DISPLAY_NAME = "Audio Mute Command";
	private static final String PROPERTY_CURRENT_AUDIO_SOURCE_COMMAND_DISPLAY_NAME = "Current Audio Source Command";
	private static final String PROPERTY_CURRENT_SPEAKER_COMMAND_DISPLAY_NAME = "Current Speaker Command";
	private static final String PROPERTY_CURRENT_POWER_SAVE_COMMAND_DISPLAY_NAME = "Current Power Save Command";
	private static final String PROPERTY_CURRENT_REMOTE_CONTROL_COMMAND_DISPLAY_NAME = "Current Remote Control Command";
	private static final String PROPERTY_CURRENT_TEMPERATURE_COMMAND_DISPLAY_NAME = "Current Temperature Command";
	private static final String PROPERTY_CURRENT_SERIAL_CODE_COMMAND_DISPLAY_NAME = "Current Serial Code Command";
	private static final String PROPERTY_SET_AUDIO_SOURCE_COMMAND_DISPLAY_NAME = "Set Audio Source Command";
	private static final String PROPERTY_SET_SPEAKER_COMMAND_DISPLAY_NAME = "Set Speaker Command";
	private static final String PROPERTY_SET_POWER_SAVE_COMMAND_DISPLAY_NAME = "Set Power Save Command";
	private static final String PROPERTY_SET_REMOTE_CONTROL_COMMAND_DISPLAY_NAME = "Set Remote Control Command";
		
	// Default values
	public static final String HEARTBEAT_INTERVAL_DEFAULT = "60";	// seconds
	public static final String RESOLUTION_DEFAULT = "1024x768";
	public static final String MCM_HOSTSTRING_DEFAULT = "localhost";
	public static final String DIAGNOSTIC_INTERVAL_DEFAULT = "900";
	
	public static final DevicePropertyType HEARTBEAT_INTERVAL = new DevicePropertyType( PROPERTY_HEARTBEAT_INTERVAL, PROPERTY_HEARTBEAT_INTERVAL_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0);
	public static final DevicePropertyType MAX_FILE_STORAGE = new DevicePropertyType( PROPERTY_MAX_FILE_STORAGE, PROPERTY_MAX_FILE_STORAGE_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType DISPLAY = new DevicePropertyType( PROPERTY_DISPLAY, PROPERTY_DISPLAY_DISPLAY_NAME, KuvataProperty.class.getName(), false, true, false, true, 0 );
	public static final DevicePropertyType VERSION = new DevicePropertyType( PROPERTY_VERSION, PROPERTY_VERSION_DISPLAY_NAME, KuvataProperty.class.getName(), false, true, false, false, 0 );
	public static final DevicePropertyType SCALING_MODE = new DevicePropertyType( PROPERTY_SCALING_MODE, PROPERTY_SCALING_MODE_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0 );	
	public static final DevicePropertyType NETWORK_INTERFACE = new DevicePropertyType( PROPERTY_NETWORK_INTERFACE, PROPERTY_NETWORK_INTERFACE_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType MAC_ADDR_INTERFACE = new DevicePropertyType( PROPERTY_MAC_ADDR_INTERFACE, PROPERTY_MAC_ADDR_INTERFACE_DISPLAY_NAME, KuvataProperty.class.getName(), true, false, false, true, 0 );
	public static final DevicePropertyType DISPATCHER_SERVERS = new DevicePropertyType( PROPERTY_DISPATCHER_SERVERS, PROPERTY_DISPATCHER_SERVERS_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType VPN_SERVER = new DevicePropertyType( PROPERTY_VPN_SERVER, PROPERTY_VPN_SERVER_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), false, true, false, false, 0 );
	public static final DevicePropertyType EDGE_SERVER = new DevicePropertyType( PROPERTY_EDGE_SERVER, PROPERTY_EDGE_SERVER_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), true, true, true, true, 0 );	
	public static final DevicePropertyType EDGE_SERVER_OPENVPN_HOST_IP = new DevicePropertyType( PROPERTY_EDGE_SERVER_OPENVPN_HOST_IP, PROPERTY_EDGE_SERVER_OPENVPN_HOST_IP_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType REDIRECT_GATEWAY = new DevicePropertyType( PROPERTY_REDIRECT_GATEWAY, PROPERTY_REDIRECT_GATEWAY_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType LCD_PIN = new DevicePropertyType( PROPERTY_LCD_PIN, PROPERTY_LCD_PIN_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType LCD_BRANDING = new DevicePropertyType( PROPERTY_LCD_BRANDING, PROPERTY_LCD_BRANDING_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType VOLUME = new DevicePropertyType( PROPERTY_VOLUME, PROPERTY_VOLUME_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );	
	public static final DevicePropertyType IOWAIT_THRESHOLD = new DevicePropertyType( PROPERTY_IOWAIT_THRESHOLD, PROPERTY_IOWAIT_THRESHOLD_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType CPU_THRESHOLD = new DevicePropertyType( PROPERTY_CPU_THRESHOLD, PROPERTY_CPU_THRESHOLD_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType MEMORY_THRESHOLD = new DevicePropertyType( PROPERTY_MEMORY_THRESHOLD, PROPERTY_MEMORY_THRESHOLD_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType LOAD_THRESHOLD = new DevicePropertyType( PROPERTY_LOAD_THRESHOLD, PROPERTY_LOAD_THRESHOLD_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType CONTENT_UPDATE_TYPE = new DevicePropertyType( PROPERTY_CONTENT_UPDATE_TYPE, PROPERTY_CONTENT_UPDATE_TYPE_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType OUTPUT_MODE = new DevicePropertyType( PROPERTY_OUTPUT_MODE, PROPERTY_OUTPUT_MODE_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType SCREENSHOT_UPLOAD_TIME = new DevicePropertyType( PROPERTY_SCREENSHOT_UPLOAD_TIME, PROPERTY_SCREENSHOT_UPLOAD_TIME_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), false, true, false, true, 0 );
	public static final DevicePropertyType AUTO_UPDATE = new DevicePropertyType( PROPERTY_AUTO_UPDATE, PROPERTY_AUTO_UPDATE_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType FILESYNC_START_TIME = new DevicePropertyType( PROPERTY_FILESYNC_START_TIME, PROPERTY_FILESYNC_START_TIME_DISPLAY_NAME, StatusProperty.class.getName(), true, true, true, true, 0 );
	public static final DevicePropertyType FILESYNC_END_TIME = new DevicePropertyType( PROPERTY_FILESYNC_END_TIME, PROPERTY_FILESYNC_END_TIME_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType X_OFFSET = new DevicePropertyType( PROPERTY_X_OFFSET, PROPERTY_X_OFFSET_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType Y_OFFSET = new DevicePropertyType( PROPERTY_Y_OFFSET, PROPERTY_Y_OFFSET_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType ZOOM = new DevicePropertyType( PROPERTY_ZOOM, PROPERTY_ZOOM_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType MASTER_DEVICE_ID = new DevicePropertyType( PROPERTY_MASTER_DEVICE_ID, PROPERTY_MASTER_DEVICE_ID_DISPLAY_NAME, StatusProperty.class.getName(), false, true, true, true, 0 );
	public static final DevicePropertyType ANTIVIRUS_SCAN = new DevicePropertyType( PROPERTY_ANTIVIRUS_SCAN, PROPERTY_ANTIVIRUS_SCAN_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType AUDIO_NORMALIZATION = new DevicePropertyType( PROPERTY_AUDIO_NORMALIZATION, PROPERTY_AUDIO_NORMALIZATION_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType CONNECT_ADDRESS = new DevicePropertyType( PROPERTY_CONNECT_ADDRESS, PROPERTY_CONNECT_ADDRESS_DISPLAY_NAME, StatusProperty.class.getName(), false, false, true, true, 0 );
	public static final DevicePropertyType DEVICE_NAME = new DevicePropertyType( PROPERTY_DEVICE_NAME, PROPERTY_DEVICE_NAME_DISPLAY_NAME, Device.class.getName(), false, false, false, false, 0 );
	public static final DevicePropertyType MULTICAST_ADDRESS = new DevicePropertyType( PROPERTY_MULTICAST_ADDRESS, PROPERTY_MULTICAST_ADDRESS_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), false, true, true, true, 0 );
	public static final DevicePropertyType MULTICAST_SERVER_PORT = new DevicePropertyType( PROPERTY_MULTICAST_SERVER_PORT, PROPERTY_MULTICAST_SERVER_PORT_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), false, true, true, true, 0 );
	public static final DevicePropertyType MULTICAST_DEVICE_PORT = new DevicePropertyType( PROPERTY_MULTICAST_DEVICE_PORT, PROPERTY_MULTICAST_DEVICE_PORT_DISPLAY_NAME, ConnectionInfoProperty.class.getName(), false, true, true, true, 0 );
	public static final DevicePropertyType CONTROL = new DevicePropertyType( PROPERTY_CONTROL, PROPERTY_CONTROL_DISPLAY_NAME, StatusProperty.class.getName(), false, true, false, false, 0 );
	public static final DevicePropertyType BANDWIDTH_LIMIT = new DevicePropertyType( PROPERTY_BANDWIDTH_LIMIT, PROPERTY_BANDWIDTH_LIMIT_DISPLAY_NAME, StatusProperty.class.getName(), true, true, true, true, 0 );
	public static final DevicePropertyType FRAMESYNC = new DevicePropertyType( PROPERTY_FRAMESYNC, PROPERTY_FRAMESYNC_DISPLAY_NAME, StatusProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType TYPE2_VIDEO_PLAYBACK = new DevicePropertyType( PROPERTY_TYPE2_VIDEO_PLAYBACK, PROPERTY_TYPE2_VIDEO_PLAYBACK_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType USE_CHROME = new DevicePropertyType( PROPERTY_USE_CHROME, PROPERTY_USE_CHROME_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0 );
	public static final DevicePropertyType CHROME_DISABLE_GPU = new DevicePropertyType( PROPERTY_CHROME_DISABLE_GPU, PROPERTY_CHROME_DISABLE_GPU_DISPLAY_NAME, KuvataProperty.class.getName(), true, true, false, true, 0 );
	
	// Mcm Properties
	public static final DevicePropertyType MCM_PERIPHERAL_NAME = new DevicePropertyType( PROPERTY_MCM_PERIPHERAL_NAME, PROPERTY_MCM_PERIPHERAL_NAME_DISPLAY_NAME, McmProperty.class.getName(), true, true, false, false, 0 );
	public static final DevicePropertyType MCM_PERIPHERAL_TYPE = new DevicePropertyType( PROPERTY_MCM_PERIPHERAL_TYPE, PROPERTY_MCM_PERIPHERAL_TYPE_DISPLAY_NAME, McmProperty.class.getName(), false, true, false, false, 0 );
	public static final DevicePropertyType MCM_IMPLEMENTATION_TYPE = new DevicePropertyType( PROPERTY_MCM_IMPLEMENTATION_TYPE, PROPERTY_MCM_IMPLEMENTATION_TYPE_DISPLAY_NAME, McmProperty.class.getName(), true, true, false, false, 0 );
	public static final DevicePropertyType DIAGNOSTIC_INTERVAL = new DevicePropertyType( PROPERTY_DIAGNOSTIC_INTERVAL, PROPERTY_DIAGNOSTIC_INTERVAL_DISPLAY_NAME, McmProperty.class.getName(), true, true, false, false, 0 );
	public static final DevicePropertyType SERIAL_PORT = new DevicePropertyType( PROPERTY_SERIAL_PORT, PROPERTY_SERIAL_PORT_DISPLAY_NAME, McmProperty.class.getName(), true, true, false, false, 0 );
	public static final DevicePropertyType MCM_HOSTSTRING = new DevicePropertyType( PROPERTY_MCM_HOSTSTRING, PROPERTY_MCM_HOSTSTRING_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	
	public static final DevicePropertyType SWITCH_TO_AUX_COMMAND = new DevicePropertyType( PROPERTY_SWITCH_TO_AUX_COMMAND, PROPERTY_SWITCH_TO_AUX_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType SWITCH_TO_MEDIACAST_COMMAND = new DevicePropertyType( PROPERTY_SWITCH_TO_MEDIACAST_COMMAND, PROPERTY_SWITCH_TO_MEDIACAST_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType ON_COMMAND = new DevicePropertyType( PROPERTY_ON_COMMAND, PROPERTY_ON_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType OFF_COMMAND = new DevicePropertyType( PROPERTY_OFF_COMMAND, PROPERTY_OFF_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType CURRENT_POWER_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_POWER_COMMAND, PROPERTY_CURRENT_POWER_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_VOLUME_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_VOLUME_COMMAND, PROPERTY_CURRENT_VOLUME_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_BRIGHTNESS_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_BRIGHTNESS_COMMAND, PROPERTY_CURRENT_BRIGHTNESS_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_CONTRAST_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_CONTRAST_COMMAND, PROPERTY_CURRENT_CONTRAST_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_INPUT_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_INPUT_COMMAND, PROPERTY_CURRENT_INPUT_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_BUTTON_LOCK_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_BUTTON_LOCK_COMMAND, PROPERTY_CURRENT_BUTTON_LOCK_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_AUDIO_SOURCE_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_AUDIO_SOURCE_COMMAND, PROPERTY_CURRENT_AUDIO_SOURCE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_SPEAKER_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_SPEAKER_COMMAND, PROPERTY_CURRENT_SPEAKER_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_POWER_SAVE_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_POWER_SAVE_COMMAND, PROPERTY_CURRENT_POWER_SAVE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_REMOTE_CONTROL_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_REMOTE_CONTROL_COMMAND, PROPERTY_CURRENT_REMOTE_CONTROL_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_TEMPERATURE_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_TEMPERATURE_COMMAND, PROPERTY_CURRENT_TEMPERATURE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType CURRENT_SERIAL_CODE_COMMAND = new DevicePropertyType( PROPERTY_CURRENT_SERIAL_CODE_COMMAND, PROPERTY_CURRENT_SERIAL_CODE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 1 );
	public static final DevicePropertyType SET_AUDIO_SOURCE_COMMAND = new DevicePropertyType( PROPERTY_SET_AUDIO_SOURCE_COMMAND, PROPERTY_SET_AUDIO_SOURCE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType SET_SPEAKER_COMMAND = new DevicePropertyType( PROPERTY_SET_SPEAKER_COMMAND, PROPERTY_SET_SPEAKER_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType SET_POWER_SAVE_COMMAND = new DevicePropertyType( PROPERTY_SET_POWER_SAVE_COMMAND, PROPERTY_SET_POWER_SAVE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType SET_REMOTE_CONTROL_COMMAND = new DevicePropertyType( PROPERTY_SET_REMOTE_CONTROL_COMMAND, PROPERTY_SET_REMOTE_CONTROL_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType SET_VOLUME_COMMAND = new DevicePropertyType( PROPERTY_SET_VOLUME_COMMAND, PROPERTY_SET_VOLUME_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType OSD_ON_COMMAND = new DevicePropertyType( PROPERTY_OSD_ON_COMMAND, PROPERTY_OSD_ON_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType OSD_OFF_COMMAND = new DevicePropertyType( PROPERTY_OSD_OFF_COMMAND, PROPERTY_OSD_OFF_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType AUTO_ADJUST_COMMAND = new DevicePropertyType( PROPERTY_AUTO_ADJUST_COMMAND, PROPERTY_AUTO_ADJUST_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 3 );
	public static final DevicePropertyType BUTTON_LOCK_ON_COMMAND = new DevicePropertyType( PROPERTY_BUTTON_LOCK_ON_COMMAND, PROPERTY_BUTTON_LOCK_ON_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType BUTTON_LOCK_OFF_COMMAND = new DevicePropertyType( PROPERTY_BUTTON_LOCK_OFF_COMMAND, PROPERTY_BUTTON_LOCK_OFF_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType SET_BRIGHTNESS_SIGNAL_COMMAND = new DevicePropertyType(PROPERTY_SET_BRIGHTNESS_SIGNAL_COMMAND, PROPERTY_SET_BRIGHTNESS_SIGNAL_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2);
	public static final DevicePropertyType SET_CONTRAST_SIGNAL_COMMAND = new DevicePropertyType(PROPERTY_SET_CONTRAST_SIGNAL_COMMAND, PROPERTY_SET_CONTRAST_SIGNAL_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2);
	public static final DevicePropertyType SET_INVERSION_ON_COMMAND = new DevicePropertyType( PROPERTY_SET_INVERSION_ON_COMMAND, PROPERTY_SET_INVERSION_ON_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType SET_INVERSION_OFF_COMMAND = new DevicePropertyType( PROPERTY_SET_INVERSION_OFF_COMMAND, PROPERTY_SET_INVERSION_OFF_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType SET_SIGNAL_PATTERN_ON_COMMAND = new DevicePropertyType( PROPERTY_SET_SIGNAL_PATTERN_ON_COMMAND, PROPERTY_SET_SIGNAL_PATTERN_ON_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType SET_SIGNAL_PATTERN_OFF_COMMAND = new DevicePropertyType( PROPERTY_SET_SIGNAL_PATTERN_OFF_COMMAND, PROPERTY_SET_SIGNAL_PATTERN_OFF_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType SET_PIXEL_SHIFT_ON_COMMAND = new DevicePropertyType( PROPERTY_SET_PIXEL_SHIFT_ON_COMMAND, PROPERTY_SET_PIXEL_SHIFT_ON_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType SET_PIXEL_SHIFT_OFF_COMMAND = new DevicePropertyType( PROPERTY_SET_PIXEL_SHIFT_OFF_COMMAND, PROPERTY_SET_PIXEL_SHIFT_OFF_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), true, true, true, false, 2 );
	public static final DevicePropertyType VOLUME_OFFSET_COMMAND = new DevicePropertyType( PROPERTY_VOLUME_OFFSET_COMMAND, PROPERTY_VOLUME_OFFSET_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true,false,  0 );
	public static final DevicePropertyType MIN_GAIN_COMMAND = new DevicePropertyType( PROPERTY_MIN_GAIN_COMMAND, PROPERTY_MIN_GAIN_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType MAX_GAIN_COMMAND = new DevicePropertyType( PROPERTY_MAX_GAIN_COMMAND, PROPERTY_MAX_GAIN_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType MUTE_COMMAND = new DevicePropertyType( PROPERTY_MUTE_COMMAND, PROPERTY_MUTE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType WOOFER_OFFSET_COMMAND = new DevicePropertyType( PROPERTY_WOOFER_OFFSET_COMMAND, PROPERTY_WOOFER_OFFSET_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType RESPONSE_TIME_COMMAND = new DevicePropertyType( PROPERTY_RESPONSE_TIME_COMMAND, PROPERTY_RESPONSE_TIME_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType INPUT_SELECTION_COMMAND = new DevicePropertyType( PROPERTY_INPUT_SELECTION_COMMAND, PROPERTY_INPUT_SELECTION_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType FRONT_PANEL_LOCK_COMMAND = new DevicePropertyType( PROPERTY_FRONT_PANEL_LOCK_COMMAND, PROPERTY_FRONT_PANEL_LOCK_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType VIDEO_MUTE_COMMAND = new DevicePropertyType( PROPERTY_VIDEO_MUTE_COMMAND, PROPERTY_VIDEO_MUTE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	public static final DevicePropertyType AUDIO_MUTE_COMMAND = new DevicePropertyType( PROPERTY_AUDIO_MUTE_COMMAND, PROPERTY_AUDIO_MUTE_COMMAND_DISPLAY_NAME, McmProperty.class.getName(), false, true, true, false, 0 );
	
	// Properties with platform specific implementations
	public static final DevicePropertyType RESOLUTION = new DevicePropertyType( PROPERTY_RESOLUTION, PROPERTY_RESOLUTION_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] resolutionImplementations = new DevicePropertyImplementation[]{ 
				RESOLUTION.new DevicePropertyImplementation( PLATFORM_LINUX, ResolutionPropertyLinux.class.getName() ),
				RESOLUTION.new DevicePropertyImplementation( PLATFORM_WINDOWS, ResolutionPropertyWindows.class.getName() ) };
	public static final DevicePropertyType ROTATION = new DevicePropertyType( PROPERTY_ROTATION, PROPERTY_ROTATION_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] orientationImplementations = new DevicePropertyImplementation[]{ 
				ROTATION.new DevicePropertyImplementation( PLATFORM_LINUX, OrientationPropertyLinux.class.getName() ),
				ROTATION.new DevicePropertyImplementation( PLATFORM_WINDOWS, OrientationPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType TIMEZONE = new DevicePropertyType( PROPERTY_TIMEZONE, PROPERTY_TIMEZONE_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] timezoneImplementations = new DevicePropertyImplementation[]{ 
				TIMEZONE.new DevicePropertyImplementation( PLATFORM_LINUX, TimezonePropertyLinux.class.getName() ),
				TIMEZONE.new DevicePropertyImplementation( PLATFORM_WINDOWS, TimezonePropertyWindows.class.getName() ) };
	public static final DevicePropertyType DISK_CAPACITY = new DevicePropertyType( PROPERTY_DISK_CAPACITY, PROPERTY_DISK_CAPACITY_DISPLAY_NAME, null, false, true, false, false, 0 );
	private static final DevicePropertyImplementation[] diskCapacityImplementations = new DevicePropertyImplementation[]{ 
				DISK_CAPACITY.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				DISK_CAPACITY.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType DISK_USAGE = new DevicePropertyType( PROPERTY_DISK_USAGE, PROPERTY_DISK_USAGE_DISPLAY_NAME, null, false, true, false, false, 0 );
	private static final DevicePropertyImplementation[] diskUsageImplementations = new DevicePropertyImplementation[]{ 
				DISK_USAGE.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				DISK_USAGE.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType MAC_ADDRESS = new DevicePropertyType( PROPERTY_MAC_ADDRESS, PROPERTY_MAC_ADDRESS_DISPLAY_NAME, null, false, true, false, false, 0 );
	private static final DevicePropertyImplementation[] macAddressImplementations = new DevicePropertyImplementation[]{ 
				MAC_ADDRESS.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				MAC_ADDRESS.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType IP_ADDRESS = new DevicePropertyType( PROPERTY_IP_ADDRESS, PROPERTY_IP_ADDRESS_DISPLAY_NAME, null, true, true, false, false, 0 );
	private static final DevicePropertyImplementation[] ipAddressImplementations = new DevicePropertyImplementation[]{ 
				IP_ADDRESS.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				IP_ADDRESS.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType VPN_IP_ADDRESS = new DevicePropertyType( PROPERTY_VPN_IP_ADDRESS, PROPERTY_VPN_IP_ADDRESS_DISPLAY_NAME, null, false, true, false, false, 0 );
	private static final DevicePropertyImplementation[] vpnIpAddressImplementations = new DevicePropertyImplementation[]{ 
				VPN_IP_ADDRESS.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				VPN_IP_ADDRESS.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType GATEWAY = new DevicePropertyType( PROPERTY_GATEWAY, PROPERTY_GATEWAY_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] gatewayImplementations = new DevicePropertyImplementation[]{ 
				GATEWAY.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				GATEWAY.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType NETMASK = new DevicePropertyType( PROPERTY_NETMASK, PROPERTY_NETMASK_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] netmaskImplementations = new DevicePropertyImplementation[]{ 
				NETMASK.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				NETMASK.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType DHCP_ENABLED = new DevicePropertyType( PROPERTY_DHCP_ENABLED, PROPERTY_DHCP_ENABLED_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] dhcpEnabledImplementations = new DevicePropertyImplementation[]{ 
				DHCP_ENABLED.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				DHCP_ENABLED.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType SSID = new DevicePropertyType( PROPERTY_SSID, PROPERTY_SSID_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] ssidImplementations = new DevicePropertyImplementation[]{ 
				SSID.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				SSID.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType ENCRYPTION_TYPE = new DevicePropertyType( PROPERTY_ENCRYPTION_TYPE, PROPERTY_ENCRYPTION_TYPE_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] encryptionTypeImplementations = new DevicePropertyImplementation[]{ 
				ENCRYPTION_TYPE.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				ENCRYPTION_TYPE.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType PASSPHRASE = new DevicePropertyType( PROPERTY_PASSPHRASE, PROPERTY_PASSPHRASE_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] passphraseImplementations = new DevicePropertyImplementation[]{ 
				PASSPHRASE.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				PASSPHRASE.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType DNS_SERVER = new DevicePropertyType( PROPERTY_DNS_SERVER, PROPERTY_DNS_SERVER_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] dnsServerImplementations = new DevicePropertyImplementation[]{ 
				DNS_SERVER.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				DNS_SERVER.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType IOWAIT_UTILIZATION = new DevicePropertyType( PROPERTY_IOWAIT_UTILIZATION, PROPERTY_IOWAIT_UTILIZATION_DISPLAY_NAME, null, false, false, true, false, 0 );
	private static final DevicePropertyImplementation[] iowaitImplementations = new DevicePropertyImplementation[]{ 
				IOWAIT_UTILIZATION.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				IOWAIT_UTILIZATION.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType CPU_UTILIZATION = new DevicePropertyType( PROPERTY_CPU_UTILIZATION, PROPERTY_CPU_UTILIZATION_DISPLAY_NAME, null, false, false, true, false, 0 );
	private static final DevicePropertyImplementation[] cpuImplementations = new DevicePropertyImplementation[]{ 
				CPU_UTILIZATION.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				CPU_UTILIZATION.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType MEMORY_UTILIZATION = new DevicePropertyType( PROPERTY_MEMORY_UTILIZATION, PROPERTY_MEMORY_UTILIZATION_DISPLAY_NAME, null, false, false, true, false, 0 );
	private static final DevicePropertyImplementation[] memoryImplementations = new DevicePropertyImplementation[]{ 
				MEMORY_UTILIZATION.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				MEMORY_UTILIZATION.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType LOAD_UTILIZATION = new DevicePropertyType( PROPERTY_LOAD_UTILIZATION, PROPERTY_LOAD_UTILIZATION_DISPLAY_NAME, null, false, false, true, false, 0 );
	private static final DevicePropertyImplementation[] loadImplementations = new DevicePropertyImplementation[]{ 
				LOAD_UTILIZATION.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				LOAD_UTILIZATION.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };	
	public static final DevicePropertyType SSHD_SERVER = new DevicePropertyType( PROPERTY_SSHD_SERVER, PROPERTY_SSHD_SERVER_DISPLAY_NAME, null, true, true, false, false, 0 );
	private static final DevicePropertyImplementation[] sshdServerImplementations = new DevicePropertyImplementation[]{ 
				SSHD_SERVER.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				SSHD_SERVER.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType OS_VERSION = new DevicePropertyType( PROPERTY_OS_VERSION, PROPERTY_OS_VERSION_DISPLAY_NAME, null, false, true, false, false, 0 );
	private static final DevicePropertyImplementation[] osVersionImplementations = new DevicePropertyImplementation[]{ 
				OS_VERSION.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				OS_VERSION.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyWindows.class.getName() ) };
	public static final DevicePropertyType ALPHA_COMPOSITING = new DevicePropertyType( PROPERTY_ALPHA_COMPOSITING, PROPERTY_ALPHA_COMPOSITING_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] alphaCompositingImplementations = new DevicePropertyImplementation[]{ 
				ALPHA_COMPOSITING.new DevicePropertyImplementation( PLATFORM_LINUX, ResolutionPropertyLinux.class.getName() ),
				ALPHA_COMPOSITING.new DevicePropertyImplementation( PLATFORM_WINDOWS, ResolutionPropertyWindows.class.getName() ) };
	public static final DevicePropertyType AUDIO_CONNECTION = new DevicePropertyType( PROPERTY_AUDIO_CONNECTION, PROPERTY_AUDIO_CONNECTION_DISPLAY_NAME, null, true, true, false, true, 0 );
	private static final DevicePropertyImplementation[] audioConnectionImplementations = new DevicePropertyImplementation[]{ 
				AUDIO_CONNECTION.new DevicePropertyImplementation( PLATFORM_LINUX, PlatformPropertyLinux.class.getName() ),
				AUDIO_CONNECTION.new DevicePropertyImplementation( PLATFORM_WINDOWS, PlatformPropertyLinux.class.getName() ) };	
	
	public static final LinkedHashMap INSTANCES = new LinkedHashMap();
	private String propertyName;
	private String displayName;
	private String className;
	private boolean isModifiable;
	private boolean reportOnHeartbeat;
	private boolean blankable;
	private boolean isAdvancedProperty;
	private int priority;
	private DevicePropertyImplementation[] devicePropertyImplementations;

	/**
	 * 
	 */	    
	static
	{
		// Set the devicePropertyImplementation property of the appropriate objects
		RESOLUTION.setDevicePropertyImplementations( resolutionImplementations );
		ROTATION.setDevicePropertyImplementations( orientationImplementations );
		TIMEZONE.setDevicePropertyImplementations( timezoneImplementations );
		DISK_CAPACITY.setDevicePropertyImplementations( diskCapacityImplementations );
		DISK_USAGE.setDevicePropertyImplementations( diskUsageImplementations );
		MAC_ADDRESS.setDevicePropertyImplementations( macAddressImplementations );
		IP_ADDRESS.setDevicePropertyImplementations( ipAddressImplementations );
		GATEWAY.setDevicePropertyImplementations( gatewayImplementations );
		NETMASK.setDevicePropertyImplementations( netmaskImplementations );
		DHCP_ENABLED.setDevicePropertyImplementations( dhcpEnabledImplementations );
		DNS_SERVER.setDevicePropertyImplementations( dnsServerImplementations );
		IOWAIT_UTILIZATION.setDevicePropertyImplementations( iowaitImplementations );
		CPU_UTILIZATION.setDevicePropertyImplementations( cpuImplementations );
		MEMORY_UTILIZATION.setDevicePropertyImplementations( memoryImplementations );
		LOAD_UTILIZATION.setDevicePropertyImplementations( loadImplementations );
		VPN_IP_ADDRESS.setDevicePropertyImplementations( vpnIpAddressImplementations );
		SSHD_SERVER.setDevicePropertyImplementations( sshdServerImplementations );
		OS_VERSION.setDevicePropertyImplementations( osVersionImplementations );
		SSID.setDevicePropertyImplementations( ssidImplementations );
		ENCRYPTION_TYPE.setDevicePropertyImplementations( encryptionTypeImplementations );
		PASSPHRASE.setDevicePropertyImplementations( passphraseImplementations );
		ALPHA_COMPOSITING.setDevicePropertyImplementations( alphaCompositingImplementations );
		AUDIO_CONNECTION.setDevicePropertyImplementations( audioConnectionImplementations );
		
		// NOTE: The order in which these properties are added to the collection will be the 
		// order in which they appear in the DeviceConfigurator and LCD screen
		INSTANCES.put(DISPATCHER_SERVERS.toString().toLowerCase(), DISPATCHER_SERVERS);
		INSTANCES.put(EDGE_SERVER.toString().toLowerCase(), EDGE_SERVER);
		INSTANCES.put(REDIRECT_GATEWAY.toString().toLowerCase(), REDIRECT_GATEWAY);
		INSTANCES.put(CONTENT_UPDATE_TYPE.toString().toLowerCase(), CONTENT_UPDATE_TYPE);
		INSTANCES.put(OUTPUT_MODE.toString().toLowerCase(), OUTPUT_MODE);
		INSTANCES.put(RESOLUTION.toString().toLowerCase(), RESOLUTION);
		INSTANCES.put(Y_OFFSET.toString().toLowerCase(), Y_OFFSET);
		INSTANCES.put(X_OFFSET.toString().toLowerCase(), X_OFFSET);
		INSTANCES.put(ZOOM.toString().toLowerCase(), ZOOM);
		INSTANCES.put(ROTATION.toString().toLowerCase(), ROTATION);
		INSTANCES.put(SCALING_MODE.toString().toLowerCase(), SCALING_MODE);
		INSTANCES.put(ALPHA_COMPOSITING.toString().toLowerCase(), ALPHA_COMPOSITING);
		INSTANCES.put(TIMEZONE.toString().toLowerCase(), TIMEZONE);
		INSTANCES.put(AUTO_UPDATE.toString().toLowerCase(), AUTO_UPDATE);
		INSTANCES.put(HEARTBEAT_INTERVAL.toString().toLowerCase(), HEARTBEAT_INTERVAL);
		INSTANCES.put(MAX_FILE_STORAGE.toString().toLowerCase(), MAX_FILE_STORAGE);
		INSTANCES.put(MEMORY_THRESHOLD.toString().toLowerCase(), MEMORY_THRESHOLD);
		INSTANCES.put(IOWAIT_THRESHOLD.toString().toLowerCase(), IOWAIT_THRESHOLD);
		INSTANCES.put(CPU_THRESHOLD.toString().toLowerCase(), CPU_THRESHOLD);				
		INSTANCES.put(LOAD_THRESHOLD.toString().toLowerCase(), LOAD_THRESHOLD);
		INSTANCES.put(FILESYNC_START_TIME.toString().toLowerCase(), FILESYNC_START_TIME);
		INSTANCES.put(FILESYNC_END_TIME.toString().toLowerCase(), FILESYNC_END_TIME);
		INSTANCES.put(BANDWIDTH_LIMIT.toString().toLowerCase(), BANDWIDTH_LIMIT);
		INSTANCES.put(ANTIVIRUS_SCAN.toString().toLowerCase(), ANTIVIRUS_SCAN);
		INSTANCES.put(FRAMESYNC.toString().toLowerCase(), FRAMESYNC);
		INSTANCES.put(USE_CHROME.toString().toLowerCase(), USE_CHROME);
		INSTANCES.put(CHROME_DISABLE_GPU.toString().toLowerCase(), CHROME_DISABLE_GPU);
		INSTANCES.put(LCD_PIN.toString().toLowerCase(), LCD_PIN);
		INSTANCES.put(LCD_BRANDING.toString().toLowerCase(), LCD_BRANDING);
		INSTANCES.put(AUDIO_CONNECTION.toString().toLowerCase(), AUDIO_CONNECTION);
		INSTANCES.put(AUDIO_NORMALIZATION.toString().toLowerCase(), AUDIO_NORMALIZATION);
		INSTANCES.put(VOLUME.toString().toLowerCase(), VOLUME);
		INSTANCES.put(DHCP_ENABLED.toString().toLowerCase(), DHCP_ENABLED);
		INSTANCES.put(IP_ADDRESS.toString().toLowerCase(), IP_ADDRESS);
		INSTANCES.put(NETMASK.toString().toLowerCase(), NETMASK);
		INSTANCES.put(GATEWAY.toString().toLowerCase(), GATEWAY);
		INSTANCES.put(DNS_SERVER.toString().toLowerCase(), DNS_SERVER);
		INSTANCES.put(SSID.toString().toLowerCase(), SSID);
		INSTANCES.put(ENCRYPTION_TYPE.toString().toLowerCase(), ENCRYPTION_TYPE);
		INSTANCES.put(PASSPHRASE.toString().toLowerCase(), PASSPHRASE);
		INSTANCES.put(NETWORK_INTERFACE.toString().toLowerCase(), NETWORK_INTERFACE);
		INSTANCES.put(SSHD_SERVER.toString().toLowerCase(), SSHD_SERVER);
		
		// Server properties
		INSTANCES.put(CONNECT_ADDRESS.toString().toLowerCase(), CONNECT_ADDRESS);
		INSTANCES.put(DEVICE_NAME.toString().toLowerCase(), DEVICE_NAME);
		
		// Read-only properties
		INSTANCES.put(VPN_SERVER.toString().toLowerCase(), VPN_SERVER);
		INSTANCES.put(DISK_CAPACITY.toString().toLowerCase(), DISK_CAPACITY);
		INSTANCES.put(DISK_USAGE.toString().toLowerCase(), DISK_USAGE);
		INSTANCES.put(VERSION.toString().toLowerCase(), VERSION);				
		INSTANCES.put(MAC_ADDRESS.toString().toLowerCase(), MAC_ADDRESS);
		INSTANCES.put(DISPLAY.toString().toLowerCase(), DISPLAY);
		INSTANCES.put(IOWAIT_UTILIZATION.toString().toLowerCase(), IOWAIT_UTILIZATION);
		INSTANCES.put(CPU_UTILIZATION.toString().toLowerCase(), CPU_UTILIZATION);
		INSTANCES.put(MEMORY_UTILIZATION.toString().toLowerCase(), MEMORY_UTILIZATION);
		INSTANCES.put(LOAD_UTILIZATION.toString().toLowerCase(), LOAD_UTILIZATION);
		INSTANCES.put(SCREENSHOT_UPLOAD_TIME.toString().toLowerCase(), SCREENSHOT_UPLOAD_TIME);
		INSTANCES.put(VPN_IP_ADDRESS.toString().toLowerCase(), VPN_IP_ADDRESS);
		INSTANCES.put(OS_VERSION.toString().toLowerCase(), OS_VERSION);
		INSTANCES.put(EDGE_SERVER_OPENVPN_HOST_IP.toString().toLowerCase(), EDGE_SERVER_OPENVPN_HOST_IP);
		INSTANCES.put(MASTER_DEVICE_ID.toString().toLowerCase(), MASTER_DEVICE_ID);
		INSTANCES.put(MULTICAST_ADDRESS.toString().toLowerCase(), MULTICAST_ADDRESS);
		INSTANCES.put(MULTICAST_SERVER_PORT.toString().toLowerCase(), MULTICAST_SERVER_PORT);
		INSTANCES.put(MULTICAST_DEVICE_PORT.toString().toLowerCase(), MULTICAST_DEVICE_PORT);
		INSTANCES.put(CONTROL.toString().toLowerCase(), CONTROL);
		
		// MCM Properties
		INSTANCES.put(MCM_PERIPHERAL_NAME.toString().toLowerCase(), MCM_PERIPHERAL_NAME);
		INSTANCES.put(MCM_PERIPHERAL_TYPE.toString().toLowerCase(), MCM_PERIPHERAL_TYPE);
		INSTANCES.put(SERIAL_PORT.toString().toLowerCase(), SERIAL_PORT);
		INSTANCES.put(SWITCH_TO_AUX_COMMAND.toString().toLowerCase(), SWITCH_TO_AUX_COMMAND);		
		INSTANCES.put(SWITCH_TO_MEDIACAST_COMMAND.toString().toLowerCase(), SWITCH_TO_MEDIACAST_COMMAND);
		INSTANCES.put(ON_COMMAND.toString().toLowerCase(), ON_COMMAND);
		INSTANCES.put(OFF_COMMAND.toString().toLowerCase(), OFF_COMMAND);
		INSTANCES.put(CURRENT_POWER_COMMAND.toString().toLowerCase(), CURRENT_POWER_COMMAND);
		INSTANCES.put(CURRENT_VOLUME_COMMAND.toString().toLowerCase(), CURRENT_VOLUME_COMMAND);
		INSTANCES.put(CURRENT_BRIGHTNESS_COMMAND.toString().toLowerCase(), CURRENT_BRIGHTNESS_COMMAND);
		INSTANCES.put(CURRENT_CONTRAST_COMMAND.toString().toLowerCase(), CURRENT_CONTRAST_COMMAND);
		INSTANCES.put(CURRENT_INPUT_COMMAND.toString().toLowerCase(), CURRENT_INPUT_COMMAND);
		INSTANCES.put(CURRENT_BUTTON_LOCK_COMMAND.toString().toLowerCase(), CURRENT_BUTTON_LOCK_COMMAND);
		INSTANCES.put(SET_VOLUME_COMMAND.toString().toLowerCase(), SET_VOLUME_COMMAND);
		INSTANCES.put(OSD_ON_COMMAND.toString().toLowerCase(), OSD_ON_COMMAND);
		INSTANCES.put(OSD_OFF_COMMAND.toString().toLowerCase(), OSD_OFF_COMMAND);
		INSTANCES.put(AUTO_ADJUST_COMMAND.toString().toLowerCase(), AUTO_ADJUST_COMMAND);
		INSTANCES.put(MCM_IMPLEMENTATION_TYPE.toString().toLowerCase(), MCM_IMPLEMENTATION_TYPE);
		INSTANCES.put(SET_INVERSION_ON_COMMAND.toString().toLowerCase(), SET_INVERSION_ON_COMMAND);
		INSTANCES.put(SET_INVERSION_OFF_COMMAND.toString().toLowerCase(), SET_INVERSION_OFF_COMMAND);
		INSTANCES.put(SET_SIGNAL_PATTERN_ON_COMMAND.toString().toLowerCase(), SET_SIGNAL_PATTERN_ON_COMMAND);
		INSTANCES.put(SET_SIGNAL_PATTERN_OFF_COMMAND.toString().toLowerCase(), SET_SIGNAL_PATTERN_OFF_COMMAND);
		INSTANCES.put(SET_PIXEL_SHIFT_ON_COMMAND.toString().toLowerCase(), SET_PIXEL_SHIFT_ON_COMMAND);
		INSTANCES.put(SET_PIXEL_SHIFT_OFF_COMMAND.toString().toLowerCase(), SET_PIXEL_SHIFT_OFF_COMMAND);		
		INSTANCES.put(DIAGNOSTIC_INTERVAL.toString().toLowerCase(), DIAGNOSTIC_INTERVAL);
		INSTANCES.put(MUTE_COMMAND.toString().toLowerCase(), MUTE_COMMAND);
		INSTANCES.put(WOOFER_OFFSET_COMMAND.toString().toLowerCase(), WOOFER_OFFSET_COMMAND);
		INSTANCES.put(RESPONSE_TIME_COMMAND.toString().toLowerCase(), RESPONSE_TIME_COMMAND);
		INSTANCES.put(VOLUME_OFFSET_COMMAND.toString().toLowerCase(), VOLUME_OFFSET_COMMAND);
		INSTANCES.put(MIN_GAIN_COMMAND.toString().toLowerCase(), MIN_GAIN_COMMAND);
		INSTANCES.put(MAX_GAIN_COMMAND.toString().toLowerCase(), MAX_GAIN_COMMAND);
		INSTANCES.put(BUTTON_LOCK_ON_COMMAND.toString().toLowerCase(), BUTTON_LOCK_ON_COMMAND);
		INSTANCES.put(BUTTON_LOCK_OFF_COMMAND.toString().toLowerCase(), BUTTON_LOCK_OFF_COMMAND);
		INSTANCES.put(SET_BRIGHTNESS_SIGNAL_COMMAND.toString().toLowerCase(), SET_BRIGHTNESS_SIGNAL_COMMAND);
		INSTANCES.put(SET_CONTRAST_SIGNAL_COMMAND.toString().toLowerCase(), SET_CONTRAST_SIGNAL_COMMAND);
		INSTANCES.put(INPUT_SELECTION_COMMAND.toString().toLowerCase(), INPUT_SELECTION_COMMAND);
		INSTANCES.put(VIDEO_MUTE_COMMAND.toString().toLowerCase(), VIDEO_MUTE_COMMAND);
		INSTANCES.put(AUDIO_MUTE_COMMAND.toString().toLowerCase(), AUDIO_MUTE_COMMAND);
		INSTANCES.put(FRONT_PANEL_LOCK_COMMAND.toString().toLowerCase(), FRONT_PANEL_LOCK_COMMAND);
		INSTANCES.put(CURRENT_AUDIO_SOURCE_COMMAND.toString().toLowerCase(), CURRENT_AUDIO_SOURCE_COMMAND);
		INSTANCES.put(CURRENT_SPEAKER_COMMAND.toString().toLowerCase(), CURRENT_SPEAKER_COMMAND);
		INSTANCES.put(CURRENT_POWER_SAVE_COMMAND.toString().toLowerCase(), CURRENT_POWER_SAVE_COMMAND);
		INSTANCES.put(CURRENT_REMOTE_CONTROL_COMMAND.toString().toLowerCase(), CURRENT_REMOTE_CONTROL_COMMAND);
		INSTANCES.put(CURRENT_TEMPERATURE_COMMAND.toString().toLowerCase(), CURRENT_TEMPERATURE_COMMAND);
		INSTANCES.put(CURRENT_SERIAL_CODE_COMMAND.toString().toLowerCase(), CURRENT_SERIAL_CODE_COMMAND);
		INSTANCES.put(SET_AUDIO_SOURCE_COMMAND.toString().toLowerCase(), SET_AUDIO_SOURCE_COMMAND);
		INSTANCES.put(SET_SPEAKER_COMMAND.toString().toLowerCase(), SET_SPEAKER_COMMAND);
		INSTANCES.put(SET_POWER_SAVE_COMMAND.toString().toLowerCase(), SET_POWER_SAVE_COMMAND);
		INSTANCES.put(SET_REMOTE_CONTROL_COMMAND.toString().toLowerCase(), SET_REMOTE_CONTROL_COMMAND);
		
		// Read-only mcm property
		INSTANCES.put(MCM_HOSTSTRING.toString().toLowerCase(), MCM_HOSTSTRING);
	}
	
	public DevicePropertyType(String propertyName, String displayName, String className, boolean isModifiable, boolean reportOnHeartbeat, boolean blankable, boolean isAdvancedProperty, int priority) {
		this.propertyName = propertyName;
		this.displayName = displayName;
		this.className = className;
		this.isModifiable = isModifiable;
		this.reportOnHeartbeat = reportOnHeartbeat;
		this.blankable = blankable;
		this.isAdvancedProperty = isAdvancedProperty;
		this.priority = priority;
	}
	
	/**
	 * 
	 * @param propertyName
	 * @return
	 */
	public static DevicePropertyType getDevicePropertyType(String propertyName)
	{
		return (DevicePropertyType) INSTANCES.get( propertyName.toLowerCase() );
	}	
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List<DevicePropertyType> getDevicePropertyTypes(boolean getModifiablePropertyTypesOnly, boolean getPropertiesToBeUploadedOnHeartbeatOnly )
	{		
		List<DevicePropertyType> l = new LinkedList<DevicePropertyType>();		
		for( Iterator<DevicePropertyType> i = DevicePropertyType.INSTANCES.values().iterator(); i.hasNext(); ) {
			DevicePropertyType dpt = (DevicePropertyType)i.next();
			if( (getModifiablePropertyTypesOnly == false || (getModifiablePropertyTypesOnly && dpt.isModifiable()))
					&& (getPropertiesToBeUploadedOnHeartbeatOnly == false || (getPropertiesToBeUploadedOnHeartbeatOnly && dpt.isReportOnHeartbeat())) ){
				l.add( dpt );
			}			
		}		
		return l;
	}
	
	/**
	 * Helper function used to determine if this DevicePropertyType is of type mcm.
	 * @return
	 */
	public boolean isMcmProperty(){
		boolean result = false;
		if( this.getClassName() != null && this.getClassName().equalsIgnoreCase( McmProperty.class.getName() ) ){
			result = true;
		}
		return result;
	}
	
	/**
	 * 
	 */
	public String toString()
	{
		return this.propertyName;
	}
	/**
	 * 
	 * @return
	 */
	public String getPropertyName()
	{
		return this.propertyName;
	}	
	/**
	 * 
	 * @return
	 */
	public String getClassName()
	{
		// If there is not a className associated with this devicePropertyType -- look for a platform specific implementation
		String result = this.className;
		
		if( this.className == null || this.className.length() == 0 )
		{
			// Retrieve the kuvata property to determine which platform we're on
			String platform = DeviceProperty.getCurrentPlatform();
			
			// Otherwise, look for a platform specific implementation
			DevicePropertyImplementation[] devicePropertyImplementations = this.getDevicePropertyImplementations();
			for( int j=0; j<devicePropertyImplementations.length; j++ ){
				DevicePropertyImplementation devicePropertyImplementation = devicePropertyImplementations[j];
				if( devicePropertyImplementation.getPlatform().equalsIgnoreCase( platform ) ){
					className = devicePropertyImplementation.getClassName();
					break;
				}
			} 
		}					
		return result;
	}
	
	/**
	 * @return Returns the isModifiable.
	 */
	public boolean isModifiable() {
		return isModifiable;
	}
	
	/**
	 * @param isModifiable The isModifiable to set.
	 */
	public void setModifiable(boolean isModifiable) {
		this.isModifiable = isModifiable;
	}	
	
	public class DevicePropertyImplementation
	{
		private String platform;
		private String className;
		
		public DevicePropertyImplementation(String platform, String className){
			this.platform = platform;
			this.className = className;
		}
		/**
		 * @return Returns the className.
		 */
		public String getClassName() {
			return className;
		}
		
		/**
		 * @param className The className to set.
		 */
		public void setClassName(String className) {
			this.className = className;
		}
		
		/**
		 * @return Returns the platform.
		 */
		public String getPlatform() {
			return platform;
		}
		
		/**
		 * @param platform The platform to set.
		 */
		public void setPlatform(String platform) {
			this.platform = platform;
		}
		
		
		
	}

	/**
	 * @return Returns the devicePropertyImplementations.
	 */
	public DevicePropertyImplementation[] getDevicePropertyImplementations() {
		return devicePropertyImplementations;
	}
	
	/**
	 * @param devicePropertyImplementations The devicePropertyImplementations to set.
	 */
	public void setDevicePropertyImplementations(
			DevicePropertyImplementation[] devicePropertyImplementations) {
		this.devicePropertyImplementations = devicePropertyImplementations;
	}
	
	/**
	 * @param className The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return Returns the reportOnHeartbeat.
	 */
	public boolean isReportOnHeartbeat() {
		return reportOnHeartbeat;
	}
	
	/**
	 * @param reportOnHeartbeat The reportOnHeartbeat to set.
	 */
	public void setReportOnHeartbeat(boolean reportOnHeartbeat) {
		this.reportOnHeartbeat = reportOnHeartbeat;
	}
	/**
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isBlankable() {
		return blankable;
	}

	public void setBlankable(boolean blankable) {
		this.blankable = blankable;
	}

	public boolean isAdvancedProperty() {
		return isAdvancedProperty;
	}

	public void setAdvancedProperty(boolean isAdvancedProperty) {
		this.isAdvancedProperty = isAdvancedProperty;
	}
}
