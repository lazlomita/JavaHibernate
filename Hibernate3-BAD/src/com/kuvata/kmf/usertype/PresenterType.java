/*
 * Created on Nov 21, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;
import com.kuvata.presenters.AuxInputPresenter;
import com.kuvata.presenters.DeviceVolumePresenter;
import com.kuvata.presenters.PowerOffPresenter;
import com.kuvata.presenters.StreamingClientPresenter;
import com.kuvata.presenters.StreamingServerPresenter;

/**
 * @author craigm
 */

public class PresenterType extends PersistentStringEnum {

    public static final PresenterType AUDIO = new PresenterType("Audio", "com.kuvata.presenters.AudioPresenter");
    public static final PresenterType FLASH = new PresenterType("Flash", "com.kuvata.presenters.FlashPresenter");
    public static final PresenterType HTML = new PresenterType("Html", "com.kuvata.presenters.HtmlPresenter");
    public static final PresenterType TICKER = new PresenterType("Ticker", "com.kuvata.presenters.TickerPresenter");
    public static final PresenterType IMAGE = new PresenterType("Image", "com.kuvata.presenters.ImagePresenter");
    public static final PresenterType VIDEO = new PresenterType("Video", "com.kuvata.presenters.VideoPresenter");
    public static final PresenterType WEBAPP = new PresenterType("Webapp", "com.kuvata.presenters.WebappPresenter");//TODO
    public static final PresenterType XML = new PresenterType("XML", "com.kuvata.presenters.XmlPresenter");
    public static final PresenterType URL = new PresenterType("URL", "com.kuvata.presenters.UrlPresenter");
	public static final PresenterType AUX_INPUT = new PresenterType("AuxInput", AuxInputPresenter.class.getName());
	public static final PresenterType POWER_OFF = new PresenterType("PowerOff", PowerOffPresenter.class.getName());
	public static final PresenterType STREAMING_CLIENT = new PresenterType("StreamingClient", StreamingClientPresenter.class.getName());
	public static final PresenterType STREAMING_SERVER = new PresenterType("StreamingServer", StreamingServerPresenter.class.getName());
	public static final PresenterType DEVICE_VOLUME = new PresenterType("DeviceVolume", DeviceVolumePresenter.class.getName());
    
    public static final Map INSTANCES = new HashMap();
    public String name;										// public so it can be serialized
    /**
     * 
     */
	static
	{
		INSTANCES.put(AUDIO.toString(), AUDIO);
		INSTANCES.put(FLASH.toString(), FLASH);
		INSTANCES.put(HTML.toString(), HTML);
		INSTANCES.put(TICKER.toString(), TICKER);
		INSTANCES.put(IMAGE.toString(), IMAGE);
		INSTANCES.put(VIDEO.toString(), VIDEO);
		INSTANCES.put(WEBAPP.toString(), WEBAPP);
		INSTANCES.put(XML.toString(), XML);
		INSTANCES.put(STREAMING_CLIENT.toString(), STREAMING_CLIENT);
		INSTANCES.put(STREAMING_SERVER.toString(), STREAMING_SERVER);
	}
	/**
	 * 
	 *
	 */
	public PresenterType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public PresenterType(String name, String persistentValue) {
		super(name, persistentValue);
		this.name = name;
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
	 * @return
	 */
	public String getPresenterTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static List getPresenterTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext())
		{
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}
	/**
	 * 
	 * @param presenterTypeName
	 * @return
	 */
	public static PresenterType getPresenterType(String presenterTypeName)
	{
		return (PresenterType) INSTANCES.get( presenterTypeName );
	}	
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}

