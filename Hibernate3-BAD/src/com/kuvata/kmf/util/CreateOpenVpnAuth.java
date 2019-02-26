package com.kuvata.kmf.util;

import java.io.File;

import org.hibernate.HibernateException;

import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;

import com.kuvata.dispatcher.DispatcherClient;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class CreateOpenVpnAuth {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( CreateOpenVpnAuth.class );		
	
	private static void createOpenVpnAuth()
	{
		// Get the mac address based on the networkInterface property
		String macAddr = DispatcherClient.getMacAddressUnencrypted();
		
		if( macAddr != null && macAddr.length() > 0 )
		{
			// Replace all ":" with underscores
			macAddr = macAddr.replaceAll(":", "_");			
			
			// Generate the password 
			int password = CreateOpenVpnAuth.generatePassword( macAddr );

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
			SchemaDirectory.initialize( "kuvata", "CreateOpenVpnAuth", null, false, true );
			CreateOpenVpnAuth.createOpenVpnAuth();								
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
