package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


public class PairingRuleType extends PersistentStringEnum 
{	
	public static final PairingRuleType TARGETED_ASSET_PAIRING = new PairingRuleType("Use Targeted Asset Pairing", "Targeted Asset Pairing");
	public static final PairingRuleType MEMBER_PAIRING = new PairingRuleType("Use Member Pairing", "Member Pairing");	
	public static final Map INSTANCES = new HashMap();	
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(TARGETED_ASSET_PAIRING.toString(), TARGETED_ASSET_PAIRING);
		INSTANCES.put(MEMBER_PAIRING.toString(), MEMBER_PAIRING);		
	}
	/**
	 * 
	 *
	 */
	public PairingRuleType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected PairingRuleType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}

	/**
	 * 
	 * @param segmentEndTypeName
	 * @return
	 */
	public static PairingRuleType getPairingRuleType(String type)
	{
		return (PairingRuleType) INSTANCES.get( type );
	}	
	
	/**
	 * 
	 * @param persistentValue
	 * @return
	 */
	public static PairingRuleType getPairingRuleTypeByPersistentValue(String persistentValue)
	{				
		for( Iterator<PairingRuleType> i = PairingRuleType.INSTANCES.values().iterator(); i.hasNext(); ){
			PairingRuleType prt = i.next();
			if( prt.getPersistentValue().equals( persistentValue) ){
				return prt;
			}
		}
		return null;
	}	
}
