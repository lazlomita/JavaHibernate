package com.kuvata.kmf.usertype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class McmCommandType extends PersistentStringEnum 
{		
	public static final McmCommandType AUTO_ADJUST = new McmCommandType(
			"Auto Adjust", 
			"com.kuvata.mcm.McmCommand.autoAdjust",
			null,
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType CHANGE_INPUT_SIGNAL = new McmCommandType(
			"Change Input Signal", 
			"com.kuvata.mcm.McmCommand.changeInputSignal",
			new String[]{ "Auxiliary Signal", "Device Signal" },
			new PeripheralType[]{PeripheralType.DISPLAY, PeripheralType.VIDEO_SWITCH}
			);	
	public static final McmCommandType POWER_OFF = new McmCommandType(
			"Power Off", 
			"com.kuvata.mcm.McmCommand.powerOff",
			null,
			new PeripheralType[]{PeripheralType.DISPLAY, PeripheralType.POWER_SWITCH, PeripheralType.VIDEO_SWITCH}
			);
	public static final McmCommandType POWER_ON = new McmCommandType(
			"Power On", 
			"com.kuvata.mcm.McmCommand.powerOn",
			null,
			new PeripheralType[]{PeripheralType.DISPLAY, PeripheralType.POWER_SWITCH, PeripheralType.VIDEO_SWITCH}
			);	
	public static final McmCommandType SET_VOLUME = new McmCommandType(
			"Set Volume", 
			"com.kuvata.mcm.McmCommand.setVolume",
			new String[]{ "100", "90", "80", "70", "60", "50", "40", "30", "20", "10", "0" },
			new PeripheralType[]{PeripheralType.DISPLAY}
			);	
	public static final McmCommandType APPLY_SETTINGS = new McmCommandType(
			"Apply Settings", 
			"com.kuvata.mcm.McmCommand.applySettings",
			null,
			new PeripheralType[]{PeripheralType.AUDIO, PeripheralType.AV_SWITCH}
			);
	public static final McmCommandType BUTTON_LOCK_OFF = new McmCommandType(
			"Button Lock Off", 
			"com.kuvata.mcm.McmCommand.buttonLockOff",
			null,
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType BUTTON_LOCK_ON = new McmCommandType(
			"Button Lock On", 
			"com.kuvata.mcm.McmCommand.buttonLockOn",
			null,
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType SET_BRIGHTNESS = new McmCommandType(
			"Set Brightness", 
			"com.kuvata.mcm.McmCommand.setBrightness",
			new String[]{ "100", "90", "80", "70", "60", "50", "40", "30", "20", "10", "0" },
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType SET_CONTRAST = new McmCommandType(
			"Set Contrast", 
			"com.kuvata.mcm.McmCommand.setContrast",
			new String[]{ "100", "90", "80", "70", "60", "50", "40", "30", "20", "10", "0" },
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType MASTER_RESET = new McmCommandType(
			"Master Reset", 
			"com.kuvata.mcm.McmCommand.masterReset",
			null,
			new PeripheralType[]{PeripheralType.AV_SWITCH}
			);
	public static final McmCommandType AUDIO_SOURCE = new McmCommandType(
			"Audio Source", 
			"com.kuvata.mcm.McmCommand.setAudioSource",
			new String[]{ "000", "001", "002", "003"},
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType SPEAKER = new McmCommandType(
			"Speaker", 
			"com.kuvata.mcm.McmCommand.setSpeaker",
			new String[]{ "000", "001", "002"},
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType POWER_SAVE = new McmCommandType(
			"Power Save",
			"com.kuvata.mcm.McmCommand.setPowerSave",
			new String[]{ "000", "001", "002", "003"},
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType REMOTE_CONTROL = new McmCommandType(
			"Remote Control", 
			"com.kuvata.mcm.McmCommand.setRemoteControl",
			new String[]{ "000", "001", "002"},
			new PeripheralType[]{PeripheralType.DISPLAY}
			);
	public static final McmCommandType GET_HISTORY = new McmCommandType(
			"Get History", 
			"com.kuvata.mcm.McmCommand.getHistory",
			null,
			new PeripheralType[]{PeripheralType.DISPLAY, PeripheralType.AUDIO, PeripheralType.AV_SWITCH, PeripheralType.VIDEO_SWITCH}
			);
	
	public static final Map ALL_INSTANCES = new LinkedHashMap();
	private McmCommandInfo mcmCommandInfo;
	private PeripheralType[] peripheralTypes;
	
	/**
	 * 
	 */	    
	static
	{
		// Alphabetical Order
		ALL_INSTANCES.put(APPLY_SETTINGS.toString(), APPLY_SETTINGS);
		ALL_INSTANCES.put(AUDIO_SOURCE.toString(), AUDIO_SOURCE);
		ALL_INSTANCES.put(AUTO_ADJUST.toString(), AUTO_ADJUST);
		ALL_INSTANCES.put(BUTTON_LOCK_OFF.toString(), BUTTON_LOCK_OFF);
		ALL_INSTANCES.put(BUTTON_LOCK_ON.toString(), BUTTON_LOCK_ON);
		ALL_INSTANCES.put(CHANGE_INPUT_SIGNAL.toString(), CHANGE_INPUT_SIGNAL);
		ALL_INSTANCES.put(GET_HISTORY.toString(), GET_HISTORY);
		ALL_INSTANCES.put(MASTER_RESET.toString(), MASTER_RESET);
		ALL_INSTANCES.put(POWER_OFF.toString(), POWER_OFF);
		ALL_INSTANCES.put(POWER_ON.toString(), POWER_ON);
		ALL_INSTANCES.put(POWER_SAVE.toString(), POWER_SAVE);
		ALL_INSTANCES.put(REMOTE_CONTROL.toString(), REMOTE_CONTROL);
		ALL_INSTANCES.put(SET_BRIGHTNESS.toString(), SET_BRIGHTNESS);
		ALL_INSTANCES.put(SET_CONTRAST.toString(), SET_CONTRAST);
		ALL_INSTANCES.put(SET_VOLUME.toString(), SET_VOLUME);
		ALL_INSTANCES.put(SPEAKER.toString(), SPEAKER);
	}
	/**
	 * 
	 *
	 */
	public McmCommandType() {}
	
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public McmCommandType(String name, String methodName, String[] parameterOptions, PeripheralType[] peripheralTypes) {
		super(name, methodName);
				
		// Build the McmCommandInfo object		
		McmCommandInfo mci = new McmCommandInfo();
		mci.setMethodName( methodName );
		mci.setParameterOptions( parameterOptions );		
		this.mcmCommandInfo = mci;
		this.peripheralTypes = peripheralTypes;
	}	
	
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	
	/**
	 * @return Returns the alertDefinitionInfo.
	 */
	public McmCommandInfo getMcmCommandInfo() {
		return mcmCommandInfo;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public static LinkedHashMap getMcmCommands(String peripheralType)
	{
		// Get a list of all peripherals types which have at least one mcm command associated with them
		List peripheralTypes = new ArrayList();
		for(Iterator<McmCommandType> i = ALL_INSTANCES.values().iterator(); i.hasNext();){
			PeripheralType[] pt = i.next().getPeripheralTypes();
			for(int j=0; j<pt.length; j++){
				if(!peripheralTypes.contains(pt[j]))
					peripheralTypes.add(pt[j]);
			}
		}
		
		LinkedHashMap hm = new LinkedHashMap();
		
		// Iterate through the peripheral types
		for(Iterator<PeripheralType> i = peripheralTypes.iterator();i.hasNext();){
			
			List values = new ArrayList();
			PeripheralType key = i.next();
			
			// Iterate through all mcm command types
			for(Iterator<McmCommandType> j = ALL_INSTANCES.values().iterator(); j.hasNext();){
				McmCommandType mct = j.next();
				PeripheralType[] pt = mct.getPeripheralTypes();
				
				// If this mcm command type has the selected peripheral as a peripheral
				for(int k=0; k<pt.length; k++){
					if(pt[k].equals(key)){
						values.add(mct);
						break;
					}
				}
			}
			
			// Add peripheral type as key, mcmCommandTypes as values
			hm.put(key.getName(), values);
		}
		
		if(peripheralType == null || peripheralType.length() == 0 || peripheralType.equals(Constants.NOT_APPLICABLE))
			return hm;
		else{
			LinkedHashMap map = new LinkedHashMap();
			map.put(peripheralType, hm.get(peripheralType));
			return map;
		}
	}	
	/**
	 * 
	 * @param persistentValue
	 * @return
	 */
	public static McmCommandType getMcmCommandByName(String name)
	{		
		for(Iterator i = McmCommandType.ALL_INSTANCES.values().iterator() ; i.hasNext(); )
		{
			McmCommandType ps = (McmCommandType)i.next();
			if( ps.getName().equals( name) )
			{
				return ps;
			}
		}
		return null;
	}
	
	public class McmCommandInfo {
		
		private String methodName;		
		private String[] parameterOptions;		
		
		public McmCommandInfo() {}

		/**
		 * @return Returns the methodName.
		 */
		public String getMethodName() {
			return methodName;
		}
		/**
		 * @param methodName The methodName to set.
		 */
		public void setMethodName(String methodName) {			
			this.methodName = methodName;
		}
		/**
		 * @return Returns the parameterOptions.
		 */
		public String[] getParameterOptions() {
			return parameterOptions;
		}
		/**
		 * @param parameterOptions The parameterOptions to set.
		 */
		public void setParameterOptions(String[] parameterOptions) {
			this.parameterOptions = parameterOptions;
		}
	}

	public PeripheralType[] getPeripheralTypes() {
		return peripheralTypes;
	}

	public void setPeripheralTypes(PeripheralType[] peripheralTypes) {
		this.peripheralTypes = peripheralTypes;
	}
}
