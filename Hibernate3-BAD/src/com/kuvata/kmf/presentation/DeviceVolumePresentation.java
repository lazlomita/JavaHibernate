/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.PresenterType;

import com.kuvata.kmf.asset.DeviceVolume;
import com.kuvata.kmf.util.Reformat;

public class DeviceVolumePresentation extends Presentation {
	
	public Integer volume;
	
	public DeviceVolumePresentation(){
		SUBDIRECTORY = "devicevolume";
	}
	
	/**
	 * 
	 * @param u
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public DeviceVolumePresentation(DeviceVolume dv, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(dv);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param u
	 * @throws HibernateException
	 * @throws IOException
	 */
	public DeviceVolumePresentation(DeviceVolume dv) throws HibernateException, IOException
	{
	    this();
	    this.setPresenterType(PresenterType.DEVICE_VOLUME);
	    this.setAssetId( dv.getAssetId() );
	    this.setVolume(dv.getVolume());
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "DeviceVolumePresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getVolume().hashCode(),result,5);
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

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}
}
