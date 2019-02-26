package com.kuvata.kmf.util;

import java.util.List;

import org.hibernate.Session;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.MirroredDevice;
import com.kuvata.kmf.SchemaDirectory;

public class PopulateMirrorDevices {

	private static void doConversion() throws Exception
	{
		try
		{
			SchemaDirectory.initialize("kuvata", "PopulateMirrorDevices", null, false, true);				
			Session session = HibernateSession.currentSession();
			String hql = "SELECT device "
				+ "FROM Device device "				
				+ "WHERE device.mirrorSource IS NOT NULL";				
			List<Device> devices = session.createQuery( hql ).list();
			for( Device device : devices )
			{
				System.out.println("Creating mirrored device: "+ device.getDeviceName() +" - "+ device.getMirrorSource().getDeviceName());
				MirroredDevice.create( device, device.getMirrorSource() );	
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
			doConversion();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
