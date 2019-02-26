package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import parkmedia.device.configurator.DeviceProperty;
import parkmedia.device.configurator.PlatformProperty;

import com.kuvata.configurator.PlatformPropertyLinux;
import com.kuvata.configurator.PlatformPropertyWindows;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class DeviceConfiguratorActionType 
{	
	public static final DeviceConfiguratorActionType REBOOT = new DeviceConfiguratorActionType( "Reboot", 0 );
	public static final DeviceConfiguratorActionType RESTART = new DeviceConfiguratorActionType( "Restart", 1 );
	public static final DeviceConfiguratorActionType RESTART_NETWORK_INTERFACE = new DeviceConfiguratorActionType( "Restart Network Interface", 2 );
	public static final DeviceConfiguratorActionType RESTART_OPENVPN_CLIENT = new DeviceConfiguratorActionType( "Restart OpenVPN Client", 3 );
	public static final DeviceConfiguratorActionType RESTART_OPENVPN_SERVER = new DeviceConfiguratorActionType( "Restart OpenVPN Server", 3 );
	public static final DeviceConfiguratorActionType STOP_OPENVPN_SERVER = new DeviceConfiguratorActionType( "Stop OpenVPN Server", 3 );
	public static final DeviceConfiguratorActionType RESTART_APACHE = new DeviceConfiguratorActionType( "Restart Apache", 6 );
	public static final DeviceConfiguratorActionType RESTART_X = new DeviceConfiguratorActionType( "Restart X", 4 );
	public static final DeviceConfiguratorActionType RECALCULATE_OUTPUT_MODE = new DeviceConfiguratorActionType( "Re-calculate Output Mode", 5 );
	public static final DeviceConfiguratorActionType RESTART_PRESENTER = new DeviceConfiguratorActionType( "Restart Presenter", 6 );
	public static final DeviceConfiguratorActionType NO_ACTION = new DeviceConfiguratorActionType( "No Action", 7 );
		
	public static final Map INSTANCES = new HashMap();
	private String action;
	private int priority;	

	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(REBOOT.toString(), REBOOT);
		INSTANCES.put(RESTART.toString(), RESTART);
		INSTANCES.put(RESTART_NETWORK_INTERFACE.toString(), RESTART_NETWORK_INTERFACE);
		INSTANCES.put(RESTART_OPENVPN_CLIENT.toString(), RESTART_OPENVPN_CLIENT);
		INSTANCES.put(RESTART_OPENVPN_SERVER.toString(), RESTART_OPENVPN_SERVER);
		INSTANCES.put(STOP_OPENVPN_SERVER.toString(), STOP_OPENVPN_SERVER);
		INSTANCES.put(RESTART_APACHE.toString(), RESTART_APACHE);
		INSTANCES.put(RESTART_X.toString(), RESTART_X);		
		INSTANCES.put(RECALCULATE_OUTPUT_MODE.toString(), RECALCULATE_OUTPUT_MODE);
		INSTANCES.put(RESTART_PRESENTER.toString(), RESTART_PRESENTER);	
		INSTANCES.put(NO_ACTION.toString(), NO_ACTION);		
	}
	
	/**
	 * 
	 * @param name
	 * @param className
	 */
	public DeviceConfiguratorActionType(String action, int priority) {
		this.action = action;
		this.priority = priority;
	}

	public void performAction() {
		// Initialize the platform specific class based on the current platform
		PlatformProperty pp = null;
		if(DeviceProperty.getCurrentPlatform().equals(DeviceProperty.PLATFORM_LINUX))
			pp = new PlatformPropertyLinux();
		else if(DeviceProperty.getCurrentPlatform().equals(DeviceProperty.PLATFORM_WINDOWS))
			pp = new PlatformPropertyWindows();	
		if( pp != null ){
			if( this.getAction().equalsIgnoreCase( RESTART_X.getAction() ) ){
				pp.restartX();
			}else if( this.getAction().equalsIgnoreCase( RESTART.getAction() ) ){
				pp.restart();
			}else if( this.getAction().equalsIgnoreCase( REBOOT.getAction() ) ){
				pp.reboot();
			}else if( this.getAction().equalsIgnoreCase( RESTART_NETWORK_INTERFACE.getAction() ) ){
				pp.restartNetworkInterface();
			}else if( this.getAction().equalsIgnoreCase( RESTART_OPENVPN_CLIENT.getAction() ) ){
				pp.restartOpenVpnClient();
			}else if( this.getAction().equalsIgnoreCase( RESTART_OPENVPN_SERVER.getAction() ) ){
				pp.restartOpenVpnServer();
			}else if( this.getAction().equalsIgnoreCase( STOP_OPENVPN_SERVER.getAction() ) ){
				pp.stopOpenVpnServer();
			}else if( this.getAction().equalsIgnoreCase( RESTART_APACHE.getAction() ) ){
				pp.restartApache();
			}else if( this.getAction().equalsIgnoreCase( RECALCULATE_OUTPUT_MODE.getAction() ) ){
				pp.recalculateOutputMode();
			}else if(this.getAction().equalsIgnoreCase(RESTART_PRESENTER.getAction())){
				PlatformProperty.terminatePresenter();
			}
		}
	}
	
	/**
	 * 
	 */
	public String toString()
	{
		return this.action;
	}
	/**
	 * 
	 * @return
	 */
	public String getAction()
	{
		return this.action;
	}	
	/**
	 * 
	 * @return
	 */
	public int getPriority()
	{
		return this.priority;
	}
}
