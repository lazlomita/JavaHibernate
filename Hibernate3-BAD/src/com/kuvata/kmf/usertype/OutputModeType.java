package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class OutputModeType extends PersistentStringEnum 
{	
	public static final OutputModeType AUTO = new OutputModeType("ComputerMode-Auto", "auto", false, 0, 0);
	public static final OutputModeType MODELINE = new OutputModeType("ComputerMode-Modeline", "modeline", false, 0, 0);
	public static final OutputModeType AUTO_EMT_ON = new OutputModeType("ComputerMode-Auto (EMT True)", "auto_emt_on", false, 0, 0);
	public static final OutputModeType MODELINE_EMT_OFF = new OutputModeType("ComputerMode-Modeline (EMT False)", "modeline_emt_off", false, 0, 0);
	public static final OutputModeType NVIDIA_NTSCM_SVIDEO = new OutputModeType("nVidia-480i-SVideo (NTSC)", "NTSC-M 640x480 SVIDEO", true, 64, 48);
	public static final OutputModeType NVIDIA_NTSCM_COMPOSITE = new OutputModeType("nVidia-480i-Composite (NTSC)", "NTSC-M 640x480 COMPOSITE", true, 64, 48);
	public static final OutputModeType NVIDIA_PAL_SVIDEO = new OutputModeType("nVidia-576i-SVideo (PAL)", "PAL-B 768x576 SVIDEO", true, 76, 58);
	public static final OutputModeType NVIDIA_PAL_COMPOSITE = new OutputModeType("nVidia-576i-Composite (PAL)", "PAL-B 768x576 COMPOSITE", true, 76, 58);
	public static final OutputModeType NVIDIA_HD480P = new OutputModeType("nVidia-480p-Component (EDTV)", "HD480p 720x480 COMPONENT", true, 66, 44);
	public static final OutputModeType NVIDIA_HD720p = new OutputModeType("nVidia-720p-Component (HDTV)", "HD720p 1280x720 COMPONENT", true, 128, 72);
	
	public static final Map INSTANCES = new LinkedHashMap();
	private boolean isTVmode;
	private double xOverscan;
	private double yOverscan;
	
	static
	{
		// Insert in the order it needs to be shown on the interface
		INSTANCES.put(AUTO.toString(), AUTO);
		INSTANCES.put(MODELINE.toString(), MODELINE);
		INSTANCES.put(AUTO_EMT_ON.toString(), AUTO_EMT_ON);
		INSTANCES.put(MODELINE_EMT_OFF.toString(), MODELINE_EMT_OFF);
		INSTANCES.put(NVIDIA_NTSCM_SVIDEO.toString(), NVIDIA_NTSCM_SVIDEO);
		INSTANCES.put(NVIDIA_NTSCM_COMPOSITE.toString(), NVIDIA_NTSCM_COMPOSITE);
		INSTANCES.put(NVIDIA_PAL_SVIDEO.toString(), NVIDIA_PAL_SVIDEO);
		INSTANCES.put(NVIDIA_PAL_COMPOSITE.toString(), NVIDIA_PAL_COMPOSITE);
		INSTANCES.put(NVIDIA_HD480P.toString(), NVIDIA_HD480P);
		INSTANCES.put(NVIDIA_HD720p.toString(), NVIDIA_HD720p);
	}
	/**
	 * 
	 *
	 */
	public OutputModeType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	
	protected OutputModeType(String name, String persistentValue, boolean isTVmode, int xOverscan, int yOverscan) {
		super(name, persistentValue);
		this.isTVmode = isTVmode;
		this.xOverscan = xOverscan;
		this.yOverscan = yOverscan;
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns a list of all asset types
	 * @return
	 */
	public static List getOutputModeTypes()
	{
		List l = new LinkedList();
		for( Iterator i=OutputModeType.INSTANCES.values().iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		return l;
	}	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static OutputModeType getOutputModeTypeByName(String name)
	{
		for( Iterator i = OutputModeType.INSTANCES.values().iterator(); i.hasNext(); )
		{
			OutputModeType type = (OutputModeType)i.next();
			if( type.getName().equalsIgnoreCase( name ) ) {
				return type;
			}
		}
		return null;
	}	
	
	public static OutputModeType getOutputModeType(String name){
		return (OutputModeType) INSTANCES.get( name );
	}	
	
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static OutputModeType getOutputModeTypeByPersistentValue(String persistentValue) {
		for( Iterator i = OutputModeType.INSTANCES.values().iterator(); i.hasNext(); ){
			OutputModeType type = (OutputModeType)i.next();
			if( type.getPersistentValue().equalsIgnoreCase( persistentValue ) ) {
				return type;
			}
		}
		return null;
	}
	public boolean isTVmode() {
		return isTVmode;
	}
	public double getXOverscan() {
		return xOverscan;
	}
	public double getYOverscan() {
		return yOverscan;
	}
}
