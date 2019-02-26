package com.kuvata.kmf.util;

import java.io.File;

import org.hibernate.HibernateException;

import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;
import parkmedia.device.configurator.DeviceProperty;
import parkmedia.device.configurator.PlatformProperty;
import parkmedia.device.entities.ConnectionInfo;
import parkmedia.usertype.DevicePropertyType;

import com.kuvata.dispatcher.DispatcherClient;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class OpenVpnInit {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( OpenVpnInit.class );		

	private static void init() throws Exception
	{
		// Create the openvpn_auth file
		createOpenVpnAuth();
		
		// Update the openvpn client.conf file based on the values in the database
		updateOpenVpnClient();
	}
	
	private static void createOpenVpnAuth()
	{
		// Get the mac address based on the networkInterface property
		String macAddr = DispatcherClient.getMacAddressUnencrypted();
		
		if( macAddr != null && macAddr.length() > 0 )
		{
			// Replace all ":" with underscores
			macAddr = macAddr.replaceAll(":", "_");			
			
			// Generate the password 
			int password = OpenVpnInit.generatePassword( macAddr );

			// Write the username and password to /kuvata/openvpn_auth
			String fileContents = macAddr +"\n"+ password;
			File f = new File( KuvataConfig.getKuvataHome() +"/openvpn_auth");
			Files.write( f, fileContents );
		}
		else{
			logger.error("Could not create openvpn_auth file because a valid MAC Address was not found.");
		}
	}
	
	/**
	 * If an edge_server is configured for this device, update the openvpn configuration file
	 * with the edge_server property. Otherwise, update the openvpn configuration file with 
	 * the dispatcher_servers values.
	 * @throws Exception
	 */
	private static void updateOpenVpnClient() throws Exception
	{
		// If an edge server is defined
		String edgeServer = DeviceProperty.getPropertyValue( DevicePropertyType.EDGE_SERVER );
		if( edgeServer != null && edgeServer.length() > 0 )
		{
			// Update the "remote" property of the openvpn configuration file with the edge server information
			PlatformProperty.updateOpenVpnClient( edgeServer, false );
		}
		else
		{
			// Update the "remote" property of the openvpn configuration file with the dispatcherServers information
			String dispatcherServers = DeviceProperty.getPropertyValue( DevicePropertyType.DISPATCHER_SERVERS );
			if( dispatcherServers != null && dispatcherServers.length() > 0 ){
				PlatformProperty.updateOpenVpnClient( dispatcherServers, false );
			}
		}
	}
	
	/**
	 * Sets the vpn_server property based on the given client vpn_ip_addr
	 * @throws Exception
	 */
	private static void updateVpnServer(String vpnClientIpAddr, String currentDispatcherServer) throws Exception
	{
		ConnectionInfo connectionInfo = ConnectionInfo.getConnectionInfo();
		
		String vpnServer = "";
		if( vpnClientIpAddr != null && vpnClientIpAddr.indexOf(".") > 0 ){
			String[] ipParts = vpnClientIpAddr.split("\\.");
			vpnServer = ipParts[0] +"."+ ipParts[1] +".0.1";
			
			// If the current vpnServer is different than our generated vpnServer -- update it
			if( connectionInfo.getVpnServer() == null || connectionInfo.getVpnServer().equalsIgnoreCase( vpnServer ) == false ){
				connectionInfo.setVpnServer( vpnServer );
				connectionInfo.update();
				
				// Update the vpn server in ntp.conf
				PlatformProperty.getPlatformProperty().updateNtpServers();
			}
		}
		
		// Set the current dispatcher server
		if(connectionInfo.getEdgeServer() != null && connectionInfo.getEdgeServer().length() > 0){
			connectionInfo.setCurrentDispatcherServer(vpnServer);
			connectionInfo.update();
		}else{
			connectionInfo.setCurrentDispatcherServer(currentDispatcherServer);
			connectionInfo.update();
		}
	}
	
	/**
	 * Generates a password based on the given username. This method will be used
	 * by OpenVpnInit and OpenVpnAuth.
	 * 
	 * @param username
	 * @return
	 */
	public static int generatePassword(String username)
	{
		// Generate the password 
		int password = 0;
		for( int i=0; i<username.length(); i++ ){
			password += i * Integer.valueOf( username.charAt(i) ).intValue();
		}
		return password;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{				
		try 
		{
			KMFLogger.isCommandLine = true;
			SchemaDirectory.initialize( "kuvata", "OpenVpnInit", null, false, true );
			
			if( args.length == 2 ){
				OpenVpnInit.updateVpnServer( args[0], args[1] );	
			}
			OpenVpnInit.init();
		} catch (Exception e) {
			logger.error( e );
		}
		finally
		{								
			try {						
				HibernateSession.closeSession();
			} catch(HibernateException he) {			
				he.printStackTrace();
			}
		}	
	}

}
