/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.asset.PowerOff;
import com.kuvata.kmf.util.Reformat;
/**
 * 
 * @author anaber
 *
 */
public class PowerOffPresentation extends Presentation {
	
	public PowerOffPresentation()
	{
	    SUBDIRECTORY = "poweroff";	    
	}
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public PowerOffPresentation(PowerOff po, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(po);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public PowerOffPresentation(PowerOff po) throws HibernateException, IOException
	{
	    this();	    
	    this.setPresenterType(PresenterType.POWER_OFF);
	    this.setAssetId( po.getAssetId() );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PowerOffPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);			
		if( result < 0 )
		{
		    return -result;
		}
		else
		{
		    return result;
		}
	}
	
	/**
	 * Implements parent's abstract method
	 */
	public ReferencedFile getReferencedFile()
	{
		return null;
	}
}
