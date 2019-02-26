/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.PresenterType;

import com.kuvata.kmf.asset.AuxInput;
import com.kuvata.kmf.util.Reformat;
/**
 * 
 * @author anaber
 *
 */
public class AuxInputPresentation extends Presentation {
	
	public AuxInputPresentation()
	{
	    SUBDIRECTORY = "auxinput";	    
	}
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public AuxInputPresentation(AuxInput ai, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(ai);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public AuxInputPresentation(AuxInput ai) throws HibernateException, IOException
	{
	    this();	    
	    this.setPresenterType(PresenterType.AUX_INPUT);
	    this.setAssetId( ai.getAssetId() );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "AuxInputPresentation".hashCode();
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
