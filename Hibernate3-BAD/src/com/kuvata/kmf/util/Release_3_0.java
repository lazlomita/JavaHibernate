package com.kuvata.kmf.util;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import parkmedia.usertype.DeviceCommandType;
import parkmedia.usertype.DevicePropertyType;

import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class Release_3_0 {

	private static void encryptExistingPasswords() throws Exception
	{
		SchemaDirectory.initialize( Constants.BASE_SCHEMA, "ExcryptExistingPasswords", null, false, true );			
		try
		{
			TripleDES decrypter = new TripleDES();			

			// Encrypt the password of each appuser
			String hql = "SELECT appUser " 
				+ "FROM AppUser as appUser";
			Session session = HibernateSession.currentSession();
			List<AppUser> l = session.createQuery( hql ).list();	
			for( Iterator<AppUser> i=l.iterator(); i.hasNext(); )
			{
				AppUser appuser = i.next();
				
				// Encrypt the password as a string			
				String encryptedPassword = decrypter.encryptToString( appuser.getPassword() );
				appuser.setPassword( encryptedPassword );
				appuser.update();			
			}
			System.out.println("DONE");		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			HibernateSession.closeSession();
		}
	}
	
	private static void addAutoUpdateDeviceCommands(String schema) throws Exception
	{
		SchemaDirectory.initialize( schema, "Release_3_0", null, false, true );			
		try
		{
			// For each device
			List<Device> l = Device.getDevices();	
			for( Iterator<Device> i=l.iterator(); i.hasNext(); )
			{				
				// Send the autoUpdate propertyChange command to the device
				Device device = i.next();
				if( device.getAutoUpdate() != null ){
					device.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.AUTO_UPDATE.getPropertyName() +","+ Reformat.escape(device.getAutoUpdate().getPersistentValue()), true );
				}
			}
			System.out.println("DONE");		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			HibernateSession.closeSession();
		}
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if( args.length != 1 ){
				System.out.println("Usage Release_3_0 [schema|encrypt]");
			}else{
				if( args[0].equalsIgnoreCase("encrypt") ){
					encryptExistingPasswords();
				}else{
					addAutoUpdateDeviceCommands( args[0] );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
