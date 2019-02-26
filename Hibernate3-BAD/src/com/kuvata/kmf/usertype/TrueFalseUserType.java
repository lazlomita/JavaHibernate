/*
 * Created on Sep 20, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public final class TrueFalseUserType extends PersistentStringEnum 
{
    public static final TrueFalseUserType TRUE = new TrueFalseUserType("true", "T");
    public static final TrueFalseUserType FALSE = new TrueFalseUserType("false", "F");
   
    public static final Map INSTANCES = new HashMap();
    
    static{
    	INSTANCES.put(TRUE.getPersistentValue(), TRUE);
    	INSTANCES.put(FALSE.getPersistentValue(), FALSE);
    }
    
    public TrueFalseUserType() {}
    /**
     * 
     * @param name
     * @param persistentValue
     */
    private TrueFalseUserType(String name, String persistentValue) {
        super(name, persistentValue);
    }
    
    public String getTrueFalseUserTypeName()
	{
		return this.name;
	}
    
    public static TrueFalseUserType getTrueFalseUserTypeByPersistentValue(String persistentValue){
		for( Iterator i = TrueFalseUserType.INSTANCES.values().iterator(); i.hasNext(); ){
			TrueFalseUserType ps = (TrueFalseUserType)i.next();
			if( ps.getPersistentValue().equals( persistentValue) ){
				return ps;
			}
		}
		return null;
	}
}

