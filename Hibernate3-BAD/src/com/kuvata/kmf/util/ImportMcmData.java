/*
 * Created on Nov 17, 2008
 *
 * Kuvata, Inc.
 */
package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Mcm;
import com.kuvata.kmf.SchemaDirectory;

public class ImportMcmData {

	private static String fileloc = "E:\\Documents and Settings\\jmattson\\Desktop\\Import Devices\\cri_server_mcms.csv";
	
	public ImportMcmData()	
	{
		try
		{
			SchemaDirectory.initialize( "kuvata", "Import MCM Data", null, false, true );
			Session session = HibernateSession.currentSession();
			SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
					
		    FileInputStream fstream = new FileInputStream(fileloc);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			Device d = null;
			String previousDeviceName = "";
		    String line = br.readLine();			
		    while ((line = br.readLine()) != null) {
   				
				String[] values = line.split("\",\"");
				values[0] = values[0].substring(1);
				values[values.length - 1] = values[values.length - 1].substring(0, values[values.length - 1].length() - 1);
				
				if (previousDeviceName.equals("") || !values[0].equals(previousDeviceName)) {
					List<Device> devices = Device.getDevices(values[0]);
					if (devices.size() > 0) {
						d = devices.get(0);
						previousDeviceName = values[0];
					}
					else {
						d = null;
						System.out.println("Device " + values[0] + " does not exist");
					}
				}
				
				if (d != null ) {
					List<Mcm> deviceMcms = d.getMcms();
					boolean alreadyExists = false;
					for (Mcm mcm : deviceMcms) {
						if (mcm.getMcmName().equals(values[1])) {
							System.out.println("MCM " + values[1] + " already exists for device " + d.getDeviceName());
							alreadyExists = true;
						}
					}
					if (alreadyExists == false) {
						Mcm newMcm = Mcm.create(values[1], d);
						newMcm.updateProperties(values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], 
								values[10], values[11], values[12], values[13], values[14], values[15], values[16], values[17], values[18], values[19], 
								values[20], values[21], values[22], values[23], values[24], values[25], values[26], values[27], values[28], values[29], 
								values[30], values[31], values[32], values[33], values[34], values[35], values[36], values[37], values[38], values[40],
								values[40], values[41], values[42], values[43], values[44], values[45], values[46], values[47], values[48], values[50], false);
						System.out.println("MCM " + values[1] + " has been created for device " + d.getDeviceName());
					}
				}
		    }		    
		    in.close();				  
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length > 0 && args[0] != null) {
			fileloc = args[0];
		}
		
		ImportMcmData imd = new ImportMcmData();
	}

}
