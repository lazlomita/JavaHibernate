package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class BillingStatusType extends PersistentStringEnum 
{
	public static final BillingStatusType STAGING = new BillingStatusType("Staging", "Staging");
	public static final BillingStatusType INTERNAL = new BillingStatusType("Internal", "Internal");
	public static final BillingStatusType PRODUCTION = new BillingStatusType("Production", "Production");
	public static final BillingStatusType OUT_OF_SERVICE = new BillingStatusType("Out of Service", "Out of Service");
	
	public static final Map INSTANCES = new LinkedHashMap();
			    
	static
	{
		INSTANCES.put(STAGING.toString(), STAGING);
		INSTANCES.put(INTERNAL.toString(), INTERNAL);
		INSTANCES.put(PRODUCTION.toString(), PRODUCTION);
		INSTANCES.put(OUT_OF_SERVICE.toString(), OUT_OF_SERVICE);
	}

	public BillingStatusType() {}
	
	public BillingStatusType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}

	public static BillingStatusType getBillingStatusTypeByPersistentValue(String billingStatusTypePersistentValue){
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			BillingStatusType billingStatusType = (BillingStatusType)i.next();
			if( billingStatusType.getPersistentValue().equalsIgnoreCase( billingStatusTypePersistentValue ) ){
				return billingStatusType;
			}
		}
		return null;
	}
	
	public static BillingStatusType getBillingStatusType(String name)
	{
		return getBillingStatusTypeByPersistentValue(name);
	}		
	
	public static List<BillingStatusType> getBillingStatusTypes()
	{
		List l = new LinkedList();
		Iterator i = BillingStatusType.INSTANCES.values().iterator();
		while(i.hasNext()) {
			l.add(i.next());
		}
		return l;
	}
}
