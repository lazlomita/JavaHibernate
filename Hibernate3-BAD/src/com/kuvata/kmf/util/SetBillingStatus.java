package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import parkmedia.usertype.BillingStatusType;

import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.SchemaDirectory;

public class SetBillingStatus {
	
	private static final String USER = "parkmedia";

	public static void main(String[] args) throws Exception{
		if(args.length < 2){
			System.out.println("Usage: SetBillingStatus device_id billing_status");
		}else{
			SchemaDirectory.initialize("kuvata", SetBillingStatus.class.getName(), USER, true, true);
			
			System.out.print("Enter login credentials:\nUsername: " + USER + "\nPassword: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String password = br.readLine();
			
			if(AppUser.getAppUser(USER, password) != null){
				Device d = Device.getDevice(Long.parseLong(args[0]));
				if(d != null){
					BillingStatusType bst = BillingStatusType.getBillingStatusType(args[1]);
					if(bst != null){
						// If we are moving to production
						// keep the billing start date but null out the billing end date
						if(bst.equals(BillingStatusType.PRODUCTION)){
							Date billableStartDt = d.getReadableBillableStartDt() != null ? d.getReadableBillableStartDt() : new Date();
							d.setBillingStatus(bst.getName(), true);
							d.setBillableStartDt(billableStartDt);
							d.setBillableEndDt(null);
							d.update();
						}
						// Null out both billing dates
						else if(bst.equals(BillingStatusType.STAGING) || bst.equals(BillingStatusType.INTERNAL)){
							d.setBillingStatus(bst.getName(), true);
							d.setBillableStartDt(null);
							d.setBillableEndDt(null);
							d.update();
						}
						
						System.out.println("Updated billing status for device " + d.getDeviceName() + " to " + bst.getName());
					}else{
						System.out.println("Could not locate a valid Billing Status: " + args[1]);
					}
				}else{
					System.out.println("Could not find a device with deviceId: " + args[0]);
				}
			}else{
				System.out.println("Invalid password");
			}
		}
	}

}
