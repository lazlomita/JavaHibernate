package com.kuvata.kmf.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;

import parkmedia.usertype.ContentUpdateType;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.SchemaDirectory;

public class RunContentSchedulerForAllDevices {

	private static void runContentScheduler(String schemaName)
	{
		try {
			SchemaDirectory.initialize( schemaName, "RunContentSchedulerAllDevices", null, false, true );
			// For each master device
			ArrayList devices = new ArrayList();
			List l = Device.getMasterDevices();
			for( Iterator i=l.iterator(); i.hasNext(); )
			{
				Device d = (Device)i.next();
				
				// If the device is not active -- ignore
				if( d.getContentUpdateType().equals(ContentUpdateType.NETWORK.getPersistentValue()) == false )
				{
					System.out.println("Ignoring device because its contentUpdateType is not set to Network: "+ d.getDeviceName());
					continue;
				}								
				devices.add( d.loadContentSchedulerArg( true ) );
			}
			// Run the content scheduler for each device
			Device.launchContentScheduler( devices );
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if( args.length == 1 ){
			runContentScheduler( args[0] );	
		}else{
			System.out.println("Usage: java RunContentSchedulerForAllDevices schema");
		}		
	}

}
