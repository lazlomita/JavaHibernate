package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.hibernate.Session;

import parkmedia.usertype.BillingStatusType;

import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class PopulateBillingStatus {
	
	private static final String USER = "parkmedia";

	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", PopulateBillingStatus.class.getName(), USER, true, true);
		
		System.out.print("Enter login credentials:\nUsername: " + USER + "\nPassword: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String password = br.readLine();
		
		if(AppUser.getAppUser(USER, password) != null){
			Session session = HibernateSession.currentSession();
			List<Device> devices = session.createQuery("FROM Device WHERE readableBillingStatus IS NULL AND encryptedBillingStatus IS NULL").list();
			for(Device d : devices){
				try {
					System.out.println("Updating " + d.getDeviceName());
					d.setBillingStatus(BillingStatusType.INTERNAL.getPersistentValue(), false);
					d.update();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			System.out.println("Invalid password");
		}
	}
}
