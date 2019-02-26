package com.kuvata.kmf;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KMFLogger;
import parkmedia.device.configurator.DeviceProperty;
import parkmedia.usertype.DeviceCommandType;
import parkmedia.usertype.DevicePropertyType;
import parkmedia.usertype.McmCommandType;
import parkmedia.usertype.McmImplementationType;
import parkmedia.usertype.PeripheralType;

import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.usertype.PersistentStringEnum;
import com.kuvata.kmf.util.Reformat;
import com.kuvata.kmm.KMMServlet;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.ParseException;
import electric.xml.XPath;

/**
 *
 * 
 * @author Jeff Randesi
 * Created on Sep. 15, 2005
 * Copyright 2005, Kuvata, Inc.
 */
public class Mcm extends Entity implements Historizable
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Mcm.class );
	private Long mcmId;
	private String mcmName;	
	private Device device;	
	private Set historyEntries = new HashSet();
	private String mcmHoststring;
	private String switchToAuxCommand;	
	private String switchToMediacastCommand;
	private String onCommand;
	private String offCommand;
	private String currentPowerCommand;
	private String currentVolumeCommand;
	private String currentInputCommand;
	private String currentBrightnessCommand;
	private String currentContrastCommand;
	private String currentButtonLockCommand;
	private String setVolumeCommand;
	private String osdOnCommand;
	private String osdOffCommand;
	private String autoAdjustCommand;
	private String peripheralName;
	private String serialPort;
	private String diagnosticInterval;
	private String volumeOffsetCommand;
	private String wooferOffsetCommand;
	private String responseTimeCommand;
	private String minGainCommand;
	private String maxGainCommand;
	private String muteCommand;
	private McmImplementationType implementationType;
	private String setInversionOnCommand;
	private String setInversionOffCommand;
	private String setSignalPatternOnCommand;
	private String setSignalPatternOffCommand;
	private String setPixelShiftOnCommand;
	private String setPixelShiftOffCommand;
	private PeripheralType peripheralType;
	private String buttonLockOnCommand;
	private String buttonLockOffCommand;
	private String setBrightnessSignalCommand;
	private String setContrastSignalCommand;
	private String inputSelectionCommand;
	private String frontPanelLockCommand;
	private String videoMuteCommand;
	private String audioMuteCommand;
	private String currentAudioSourceCommand;
	private String currentSpeakerCommand;
	private String currentPowerSaveCommand;
	private String currentRemoteControlCommand;
	private String currentTemperatureCommand;
	private String currentSerialCodeCommand;
	private String setAudioSourceCommand;
	private String setSpeakerCommand;
	private String setPowerSaveCommand;
	private String setRemoteControlCommand;
	
	// Constants used for to build xml structure of diagnostic information
	public static final String ROOT_ELEMENT = "diagnostics";
	public static final String MCM_ID_ATTRIBUTE = "mcm_id";
	public static final String TIMESTAMP_ATTRIBUTE = "timestamp";
	public static final String SCREENSHOT_ELEMENT = "screenshot";

	public static final String MCM_COMMANDS_ELEMENT = "McmCommands";
	public static final String MCM_COMMAND_ELEMENT = "McmCommand";
	
	
	/**
	 * 
	 *
	 */
	public Mcm()
	{		
	}	
	
	/**
	 * Creates a new mcm object for the given device.
	 * @param mcmName
	 * @param d
	 * @return
	 */
	public static Mcm create(String mcmName, Device d) throws InterruptedException
	{
		// Create a new MCM
		Mcm mcm = new Mcm();
		mcm.setMcmName( mcmName );
		mcm.setDevice( d );
		mcm.setPeripheralType( PeripheralType.NON_MONITORED_DISPLAY );
		mcm.setPeripheralName(PeripheralType.NON_MONITORED_DISPLAY.getName());
		mcm.save();	
		
		// Add device commands for the two mcm properties that were set
		d.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcm.getMcmId().toString() +","+ DevicePropertyType.MCM_PERIPHERAL_NAME.getPropertyName() +","+ Reformat.escape(mcm.getPeripheralName()), false );
		d.addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcm.getMcmId().toString() +","+ DevicePropertyType.MCM_PERIPHERAL_TYPE.getPropertyName() +","+ Reformat.escape(mcm.getPeripheralType().getPersistentValue()), false );				
		return mcm;
	}
	
	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws HibernateException
	 */
	public static Mcm getMcm(Long mcmId) throws HibernateException
	{
		return (Mcm)Entity.load(Mcm.class, mcmId);		
	}	
	
	/**
	 * Builds the MCM command and adds it as a device command to the device
	 * associated with this MCM. 
	 * 
	 * NOTE: Every mcm command should take as it's first parameter, the mcm_id
	 * 
	 * @param cmd The command to add
	 */
	public void addMcmCommand(McmCommandType cmd, String params, boolean attemptPush, boolean createNow) throws HibernateException, InterruptedException
	{
		// Build the mcm command
		StringBuilder mcmCommand = new StringBuilder();
		mcmCommand.append( cmd.getPersistentValue() );
		mcmCommand.append("(");
		mcmCommand.append("\""+ this.getMcmId().toString() +"\"");
		if( params != null )
		{
			mcmCommand.append(",");
			mcmCommand.append("\""+ params +"\"");
		}
		mcmCommand.append(")");
		
		// Add the device command with the mcm command as the parameter
		this.getDevice().addDeviceCommand( DeviceCommandType.MCM_COMMAND, mcmCommand.toString(), attemptPush, false, false, null, createNow );
	}		
	
	
	public List getHistoryEntries(int numRecords)
	{
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery(				
				"SELECT he "
				+ "FROM McmHistoryEntry as he "				
				+ "WHERE he.mcm.mcmId = ? "
				+ "ORDER BY he.timestamp desc"
				);
		
		List l = q.setParameter(0, this.getMcmId()).setMaxResults( numRecords ).list();
		return l;	
	}
	
	public McmHistoryEntry getLastHistoryEntry()
	{
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery(				
				"SELECT he "
				+ "FROM McmHistoryEntry as he "				
				+ "WHERE he.mcm.mcmId = ? "
				+ "AND he.isLastMcmHistory = ?"
				);
		
		List l = q.setParameter(0, this.getMcmId()).setParameter(1, Boolean.TRUE).list();
		return l.size() == 1 ? (McmHistoryEntry)l.get(0) : null;	
	}
	
	/**
	 * Returns true if an mcm with the given name already exists for the given device
	 * 
	 * @param deviceName
	 * @return
	 */
	public static boolean mcmExists(Device device, String mcmName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = (List) session.createCriteria(Mcm.class)
					.add( Expression.eq("mcmName", mcmName).ignoreCase() )
					.add( Expression.eq("device.deviceId", device.getDeviceId()) )
					.list();
				
		// If an mcm with the given name already exists in the database
		if(l.size() > 0 )
			return true;
		else
			return false;
	}	
	
	/**
	 * Locates the default mcm property value for the given propertyName
	 * for the given display name according to the information found in the peripherals.xml file.
	 * 
	 * @param propertyName
	 * @return
	 */
	public static String getPropertyDefault(String peripheralName, String propertyName) throws ParseException
	{
		// Attempt to locate the peripherals.xml file
		String result = "";
		if( peripheralName != null && peripheralName.length() > 0 )
		{
			// Look for the display element for the display associated with this mcm.
			result = Constants.NOT_APPLICABLE;
			Document doc = KMMServlet.getPeripheralsXml();
			Element displayElement = doc.getElement( new XPath("//"+ DeviceProperty.PERIPHERALS_ROOT_ELEMENT +"/"+
					DeviceProperty.PERIPHRAL_ELEMENT + "/" + DeviceProperty.NAME_ELEMENT +
					"[@"+DeviceProperty.VALUE_ATTRIBUTE+"=\""+peripheralName+"\"]")).getParentElement();			
			if( displayElement != null )
			{
				// Look for the mcm command element for the given property
				Element mcmCommandElement = displayElement.getElement( new XPath( MCM_COMMANDS_ELEMENT +"/"+ MCM_COMMAND_ELEMENT +"[@name=\""+ propertyName +"\"]"));
				if( mcmCommandElement != null ) {
					result = mcmCommandElement.getAttribute( DeviceProperty.VALUE_ATTRIBUTE );
				} else {
					logger.info("Could not locate a mcm command element for this property and display: "+ propertyName +", "+ peripheralName);
				}
			} else {
				logger.info("Could not locate a corresponding element in peripherals.xml for the display: "+ peripheralName);
			}				
		}else{
			logger.info("Could not retrieve property default -- display name not specified.");
		}	
		return result;
	}
	
	/**
	 * Locates the type of peripheral the selected peripheral/device is,
	 * according to the information found in the peripherals.xml file.
	 *
	 * @param propertyName
	 * @return
	 */
	public static PeripheralType getTypeForSelectedPeripheral(String peripheralName) throws ParseException
	{
		PeripheralType result = null;
		if( peripheralName != null && peripheralName.length() > 0 )
		{
			// Look for the display element for the display associated with this mcm.
			Document doc = KMMServlet.getPeripheralsXml();
			String peripheralType = doc.getElement( new XPath("//"+ DeviceProperty.PERIPHERALS_ROOT_ELEMENT +"/"+
					DeviceProperty.PERIPHRAL_ELEMENT + "/" + DeviceProperty.NAME_ELEMENT +
					"[@"+DeviceProperty.VALUE_ATTRIBUTE+"=\""+peripheralName+"\"]")).getParentElement().getAttribute("type");
			if( peripheralType != null && peripheralType.length() > 0 ){
				result = PeripheralType.getPeripheralType( peripheralType );
			}
		}else{
			logger.info("Could not retrieve peripheral type -- peripheral name not specified.");
		}
		return result;
	}
	
	public void updateProperties(String diagnosticInterval, String selectedPeripheral, String serialPort, 
			String switchToAuxCommand, String switchToMediacastCommand, String onCommand, String offCommand, 
			String currentPowerCommand, String currentVolumeCommand, String currentBrightnessCommand, 
			String currentContrastCommand, String currentInputCommand, String currentButtonLockCommand, String setVolumeCommand, String osdOnCommand, 
			String osdOffCommand, String autoAdjustCommand, String setInversionOnCommand, String setInversionOffCommand,
			String setSignalPatternOnCommand, String setSignalPatternOffCommand, String setPixelShiftOnCommand,
			String setPixelShiftOffCommand, String selectedMcmImplementationType, String volumeOffsetCommand, String wooferOffsetCommand, String responseTimeCommand,
			String minGainCommand,String maxGainCommand,String muteCommand, String buttonLockOnCommand, String buttonLockOffCommand,
			String setBrightnessSignalCommand, String setContrastSignalCommand, String inputSelectionCommand, String frontPanelLockCommand,
			String videoMuteCommand, String audioMuteCommand, String currentAudioSourceCommand, String currentSpeakerCommand, String currentPowerSaveCommand, String currentRemoteControlCommand,
			String currentTemperatureCommand, String currentSerialCodeCommand, String setAudioSourceCommand, String setSpeakerCommand, String setPowerSaveCommand, String setRemoteControlCommand,
			boolean createDeviceCommandsNow) throws InterruptedException, ParseException
	{
		McmImplementationType implementationType = McmImplementationType.getMcmImplementationType( selectedMcmImplementationType );
		
		// If the property belongs to the selected type and the values have changed, issue propertyChange device command
		Device d = this.getDevice();		
		if( propertyHasChanged( this.getSerialPort(), serialPort ) ){
			this.setSerialPort( serialPort );
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SERIAL_PORT.getPropertyName() +","+ Reformat.escape(serialPort), false, false, false, null, true);
		}
		if( propertyHasChanged( this.getPeripheralName(), selectedPeripheral ) ){
			this.setPeripheralName( selectedPeripheral );
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.MCM_PERIPHERAL_NAME.getPropertyName() +","+ Reformat.escape(selectedPeripheral), false, false, false, null, true);
			
			// Look up the peripheral type associated with this peripheral (as defined in peripherals.xml)
			PeripheralType peripheralType = Mcm.getTypeForSelectedPeripheral( selectedPeripheral );
						
			// If the peripheral type has changed -- send device command
			if( propertyHasChanged( this.getPeripheralType(), peripheralType ) ){
				this.setPeripheralType( peripheralType );
				d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.MCM_PERIPHERAL_TYPE.getPropertyName() +","+ Reformat.escape(peripheralType.getPersistentValue()), false, false, false, null, true);				
			}										
		}			
		if( propertyHasChanged( this.getImplementationType(), implementationType ) ){			
			this.setImplementationType( implementationType );
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.MCM_IMPLEMENTATION_TYPE.getPropertyName() +","+ Reformat.escape(selectedMcmImplementationType), false, false, false, null, true);							
		}			
		if( propertyHasChanged( this.getDiagnosticInterval(), diagnosticInterval ) ){
			this.setDiagnosticInterval( diagnosticInterval );
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.DIAGNOSTIC_INTERVAL.getPropertyName() +","+ Reformat.escape(diagnosticInterval), false, false, false, null, true);							
		}
		if(propertyHasChanged( this.getSwitchToAuxCommand(), switchToAuxCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SWITCH_TO_AUX_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSwitchToAuxCommand( switchToAuxCommand );													
			}else{
				this.setSwitchToAuxCommand(null);			
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SWITCH_TO_AUX_COMMAND.getPropertyName() +","+ Reformat.escape( this.getSwitchToAuxCommand() ), false, false, false, null, true);
		}	
		if(propertyHasChanged( this.getSwitchToMediacastCommand(), switchToMediacastCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SWITCH_TO_MEDIACAST_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setSwitchToMediacastCommand( switchToMediacastCommand );										
			}else{
				this.setSwitchToMediacastCommand(null);				
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SWITCH_TO_MEDIACAST_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSwitchToMediacastCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getOnCommand(), onCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.ON_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){
				this.setOnCommand( onCommand );											
			}else{
				this.setOnCommand( null );										
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.ON_COMMAND.getPropertyName() +","+ Reformat.escape(this.getOnCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getOffCommand(), offCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.OFF_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){		
				this.setOffCommand( offCommand );							
			}else{
				this.setOffCommand( null );											
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.OFF_COMMAND.getPropertyName() +","+ Reformat.escape(this.getOffCommand()), false, false, false, null, true);
		}
		if(propertyHasChanged( this.getCurrentPowerCommand(), currentPowerCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setCurrentPowerCommand( currentPowerCommand );							
			}else{
				this.setCurrentPowerCommand( null );				
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentPowerCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentVolumeCommand(), currentVolumeCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_VOLUME_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentVolumeCommand( currentVolumeCommand );			
			}else{
				this.setCurrentVolumeCommand( null );
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_VOLUME_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentVolumeCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentBrightnessCommand(), currentBrightnessCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){	
				this.setCurrentBrightnessCommand( currentBrightnessCommand );													
			}else{
				this.setCurrentBrightnessCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentBrightnessCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentContrastCommand(), currentContrastCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_CONTRAST_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setCurrentContrastCommand( currentContrastCommand );											
			}else{
				this.setCurrentContrastCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_CONTRAST_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentContrastCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentInputCommand(), currentInputCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setCurrentInputCommand( currentInputCommand );														
			}else{
				this.setCurrentInputCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentInputCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentButtonLockCommand(), currentButtonLockCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_BUTTON_LOCK_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setCurrentButtonLockCommand(currentButtonLockCommand);														
			}else{
				this.setCurrentButtonLockCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_BUTTON_LOCK_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentButtonLockCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetVolumeCommand(), setVolumeCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_VOLUME_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){
					this.setSetVolumeCommand( setVolumeCommand );				
			}else{
				this.setSetVolumeCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_VOLUME_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetVolumeCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getOsdOnCommand(), osdOnCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.OSD_ON_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setOsdOnCommand( osdOnCommand );														
			}else{
				this.setOsdOnCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.OSD_ON_COMMAND.getPropertyName() +","+ Reformat.escape(this.getOsdOnCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getOsdOffCommand(), osdOffCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.OSD_OFF_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setOsdOffCommand( osdOffCommand );				
			}else{
				this.setOsdOffCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.OSD_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(this.getOsdOffCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getAutoAdjustCommand(), autoAdjustCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.AUTO_ADJUST_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setAutoAdjustCommand( autoAdjustCommand );															
			}else{
				this.setAutoAdjustCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.AUTO_ADJUST_COMMAND.getPropertyName() +","+ Reformat.escape(this.getAutoAdjustCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetInversionOnCommand(), setInversionOnCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_INVERSION_ON_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetInversionOnCommand( setInversionOnCommand );														
			}else{
				this.setSetInversionOnCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_INVERSION_ON_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetInversionOnCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetInversionOffCommand(), setInversionOffCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_INVERSION_OFF_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetInversionOffCommand( setInversionOffCommand );				
			}else{
				this.setSetInversionOffCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_INVERSION_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetInversionOffCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetSignalPatternOnCommand(), setSignalPatternOnCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_SIGNAL_PATTERN_ON_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetSignalPatternOnCommand( setSignalPatternOnCommand );				
			}else{
				this.setSetSignalPatternOnCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_SIGNAL_PATTERN_ON_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetSignalPatternOnCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetSignalPatternOffCommand(), setSignalPatternOffCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_SIGNAL_PATTERN_OFF_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetSignalPatternOffCommand( setSignalPatternOffCommand );											
			}else{
				this.setSetSignalPatternOffCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_SIGNAL_PATTERN_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetSignalPatternOffCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetPixelShiftOnCommand(), setPixelShiftOnCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_PIXEL_SHIFT_ON_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){		
				this.setSetPixelShiftOnCommand( setPixelShiftOnCommand );				
			}else{
				this.setSetPixelShiftOnCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_PIXEL_SHIFT_ON_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetPixelShiftOnCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetPixelShiftOffCommand(), setPixelShiftOffCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_PIXEL_SHIFT_OFF_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetPixelShiftOffCommand( setPixelShiftOffCommand );											
			}else{
				this.setSetPixelShiftOffCommand( null );							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_PIXEL_SHIFT_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetPixelShiftOffCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getVolumeOffsetCommand(), volumeOffsetCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.VOLUME_OFFSET_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setVolumeOffsetCommand(volumeOffsetCommand);														
			}else{
				this.setVolumeOffsetCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.VOLUME_OFFSET_COMMAND.getPropertyName() +","+ Reformat.escape(this.getVolumeOffsetCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getWooferOffsetCommand(), wooferOffsetCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.WOOFER_OFFSET_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setWooferOffsetCommand(wooferOffsetCommand);														
			}else{
				this.setWooferOffsetCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.WOOFER_OFFSET_COMMAND.getPropertyName() +","+ Reformat.escape(this.getWooferOffsetCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getResponseTimeCommand(), responseTimeCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.RESPONSE_TIME_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setResponseTimeCommand(responseTimeCommand);														
			}else{
				this.setResponseTimeCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.RESPONSE_TIME_COMMAND.getPropertyName() +","+ Reformat.escape(this.getResponseTimeCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getMinGainCommand(), minGainCommand) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.MIN_GAIN_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){			
				this.setMinGainCommand(minGainCommand);				
			}else{
				this.setMinGainCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.MIN_GAIN_COMMAND.getPropertyName() +","+ Reformat.escape(this.getMinGainCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getMaxGainCommand(), maxGainCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.MAX_GAIN_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setMaxGainCommand(maxGainCommand);				
			}else{
				this.setMaxGainCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.MAX_GAIN_COMMAND.getPropertyName() +","+ Reformat.escape(this.getMaxGainCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getMuteCommand(), muteCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.MUTE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setMuteCommand(muteCommand);				
			}else{
				this.setMuteCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.MUTE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getMuteCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getButtonLockOnCommand(), buttonLockOnCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.BUTTON_LOCK_ON_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setButtonLockOnCommand(buttonLockOnCommand);
			}else{
				this.setButtonLockOnCommand(null);
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.BUTTON_LOCK_ON_COMMAND.getPropertyName() +","+ Reformat.escape(this.getButtonLockOnCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getButtonLockOffCommand(), buttonLockOffCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.BUTTON_LOCK_OFF_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setButtonLockOffCommand(buttonLockOffCommand);
			}else{
				this.setButtonLockOffCommand(null);
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.BUTTON_LOCK_OFF_COMMAND.getPropertyName() +","+ Reformat.escape(this.getButtonLockOffCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetBrightnessSignalCommand(), setBrightnessSignalCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_BRIGHTNESS_SIGNAL_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetBrightnessSignalCommand(setBrightnessSignalCommand);
			}else{
				this.setSetBrightnessSignalCommand(null);
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_BRIGHTNESS_SIGNAL_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetBrightnessSignalCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetContrastSignalCommand(), setContrastSignalCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_CONTRAST_SIGNAL_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetContrastSignalCommand(setContrastSignalCommand);
			}else{
				this.setSetContrastSignalCommand(null);
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_CONTRAST_SIGNAL_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetContrastSignalCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getInputSelectionCommand(), inputSelectionCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.INPUT_SELECTION_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setInputSelectionCommand(inputSelectionCommand);				
			}else{
				this.setInputSelectionCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.INPUT_SELECTION_COMMAND.getPropertyName() +","+ Reformat.escape(this.getInputSelectionCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getFrontPanelLockCommand(), frontPanelLockCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.FRONT_PANEL_LOCK_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setFrontPanelLockCommand(frontPanelLockCommand);				
			}else{
				this.setFrontPanelLockCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.FRONT_PANEL_LOCK_COMMAND.getPropertyName() +","+ Reformat.escape(this.getFrontPanelLockCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getVideoMuteCommand(), videoMuteCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.VIDEO_MUTE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setVideoMuteCommand(videoMuteCommand);				
			}else{
				this.setVideoMuteCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.VIDEO_MUTE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getVideoMuteCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getAudioMuteCommand(), audioMuteCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.AUDIO_MUTE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setAudioMuteCommand(audioMuteCommand);				
			}else{
				this.setAudioMuteCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.AUDIO_MUTE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getAudioMuteCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentAudioSourceCommand(), currentAudioSourceCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_AUDIO_SOURCE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentAudioSourceCommand(currentAudioSourceCommand);				
			}else{
				this.setCurrentAudioSourceCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_AUDIO_SOURCE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentAudioSourceCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentSpeakerCommand(), currentSpeakerCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_SPEAKER_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentSpeakerCommand(currentSpeakerCommand);				
			}else{
				this.setCurrentSpeakerCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_SPEAKER_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentSpeakerCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentPowerSaveCommand(), currentPowerSaveCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_POWER_SAVE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentPowerSaveCommand(currentPowerSaveCommand);				
			}else{
				this.setCurrentPowerSaveCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_POWER_SAVE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentPowerSaveCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentRemoteControlCommand(), currentRemoteControlCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_REMOTE_CONTROL_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentRemoteControlCommand(currentRemoteControlCommand);				
			}else{
				this.setCurrentRemoteControlCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_REMOTE_CONTROL_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentRemoteControlCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentTemperatureCommand(), currentTemperatureCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_TEMPERATURE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentTemperatureCommand(currentTemperatureCommand);				
			}else{
				this.setCurrentTemperatureCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_TEMPERATURE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentTemperatureCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getCurrentSerialCodeCommand(), currentSerialCodeCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.CURRENT_SERIAL_CODE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setCurrentSerialCodeCommand(currentSerialCodeCommand);				
			}else{
				this.setCurrentSerialCodeCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.CURRENT_SERIAL_CODE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getCurrentSerialCodeCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetAudioSourceCommand(), setAudioSourceCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_AUDIO_SOURCE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetAudioSourceCommand(setAudioSourceCommand);				
			}else{
				this.setSetAudioSourceCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_AUDIO_SOURCE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetAudioSourceCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetSpeakerCommand(), setSpeakerCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_SPEAKER_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetSpeakerCommand(setSpeakerCommand);				
			}else{
				this.setSetSpeakerCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_SPEAKER_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetSpeakerCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetPowerSaveCommand(), setPowerSaveCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_POWER_SAVE_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetPowerSaveCommand(setPowerSaveCommand);				
			}else{
				this.setSetPowerSaveCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_POWER_SAVE_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetPowerSaveCommand()), false, false, false, null, true);			
		}
		if(propertyHasChanged( this.getSetRemoteControlCommand(), setRemoteControlCommand ) ){
			if( !getPropertyDefault(selectedPeripheral, DevicePropertyType.SET_REMOTE_CONTROL_COMMAND.getPropertyName()).equals(Constants.NOT_APPLICABLE)){				
				this.setSetRemoteControlCommand(setRemoteControlCommand);				
			}else{
				this.setSetRemoteControlCommand(null);							
			}
			d.addDeviceCommand(DeviceCommandType.PROPERTY_CHANGE_COMMAND, mcmId +","+ DevicePropertyType.SET_REMOTE_CONTROL_COMMAND.getPropertyName() +","+ Reformat.escape(this.getSetRemoteControlCommand()), false, false, false, null, true);			
		}
		
		// Always use "localhost" for mcm hoststring
		this.setMcmHoststring( DevicePropertyType.MCM_HOSTSTRING_DEFAULT );
		this.update();
	}
	
	/**
	 * Determines if the given values are different from each other.
	 * @param savedValue
	 * @param submittedValue
	 * @return
	 */
	public boolean propertyHasChanged(Object mcmValue, Object propertyValue)
	{
		boolean result = false;
		
		// If the device's value is null, but the property value is not 
		if( mcmValue == null && propertyValue != null )
		{
			if( propertyValue instanceof String ){
				if( ((String)propertyValue).length() > 0 && !((String)propertyValue).equals(Constants.NOT_APPLICABLE)){
					result = true;
				}
			}else{
				result = true;
			}
		}
		// Strings
		else if( mcmValue instanceof String ){
			if( propertyValue != null && ((String)propertyValue).length() > 0 && ((String)mcmValue).equalsIgnoreCase( (String)propertyValue ) == false ){
				result = true;									
			}
		}
		// PersistentStringEnum
		else if( mcmValue instanceof PersistentStringEnum ){			
			if( propertyValue != null && ((PersistentStringEnum)mcmValue) != (PersistentStringEnum)propertyValue ){
				result = true;									
			}
		}			
		return result;
	}	
	
	/**
	 * Deletes this mcm and also uses JDBC to delete rows from the mcm_history and entity_instance tables
	 */
	public void delete() throws HibernateException
	{
		/*
		 * Use JDBC/SQL to delete rows from the mcm_history table and the entity_instance table.
		 */		
		try {
			Session session = HibernateSession.currentSession();			
			SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
			Connection conn = sessionImplementor.getConnectionProvider().getConnection();			
			Statement stmt = conn.createStatement();
			String sql = "DELETE FROM mcm_history WHERE mcm_id = "+ this.getMcmId().toString();
			stmt.executeUpdate( sql );
			stmt.close();
			
			stmt = conn.createStatement();
			stmt.executeUpdate("commit");
			stmt.close();	
			conn.close();
		} catch (Exception e) {		
			logger.error( e );
		}				
		super.delete();
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getMcmId();
	}
	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}
	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}
	/**
	 * @return Returns the historyEntries.
	 */
	public Set getHistoryEntries() {
		return historyEntries;
	}
	/**
	 * @param historyEntries The historyEntries to set.
	 */
	public void setHistoryEntries(Set historyEntries) {
		this.historyEntries = historyEntries;
	}
	/**
	 * @return Returns the mcmId.
	 */
	public Long getMcmId() {
		return mcmId;
	}
	/**
	 * @param mcmId The mcmId to set.
	 */
	public void setMcmId(Long mcmId) {
		this.mcmId = mcmId;
	}
	/**
	 * @return Returns the mcmName.
	 */
	public String getMcmName() {
		return mcmName;
	}
	/**
	 * @param mcmName The mcmName to set.
	 */
	public void setMcmName(String mcmName) {
		this.mcmName = mcmName;
	}

	/**
	 * @return Returns the currentBrightnessCommand.
	 */
	public String getCurrentBrightnessCommand() {
		return currentBrightnessCommand;
	}
	

	/**
	 * @param currentBrightnessCommand The currentBrightnessCommand to set.
	 */
	public void setCurrentBrightnessCommand(String currentBrightnessCommand) {
		this.currentBrightnessCommand = currentBrightnessCommand;
	}
	

	/**
	 * @return Returns the currentContrastCommand.
	 */
	public String getCurrentContrastCommand() {
		return currentContrastCommand;
	}
	

	/**
	 * @param currentContrastCommand The currentContrastCommand to set.
	 */
	public void setCurrentContrastCommand(String currentContrastCommand) {
		this.currentContrastCommand = currentContrastCommand;
	}
	

	/**
	 * @return Returns the currentInputCommand.
	 */
	public String getCurrentInputCommand() {
		return currentInputCommand;
	}
	

	/**
	 * @param currentInputCommand The currentInputCommand to set.
	 */
	public void setCurrentInputCommand(String currentInputCommand) {
		this.currentInputCommand = currentInputCommand;
	}
	

	/**
	 * @return Returns the currentPowerCommand.
	 */
	public String getCurrentPowerCommand() {
		return currentPowerCommand;
	}
	

	/**
	 * @param currentPowerCommand The currentPowerCommand to set.
	 */
	public void setCurrentPowerCommand(String currentPowerCommand) {
		this.currentPowerCommand = currentPowerCommand;
	}
	

	/**
	 * @return Returns the currentVolumeCommand.
	 */
	public String getCurrentVolumeCommand() {
		return currentVolumeCommand;
	}
	

	/**
	 * @param currentVolumeCommand The currentVolumeCommand to set.
	 */
	public void setCurrentVolumeCommand(String currentVolumeCommand) {
		this.currentVolumeCommand = currentVolumeCommand;
	}


	/**
	 * @return the peripheralName
	 */
	public String getPeripheralName() {
		return peripheralName;
	}

	/**
	 * @param peripheralName the peripheralName to set
	 */
	public void setPeripheralName(String peripheralName) {
		this.peripheralName = peripheralName;
	}

	/**
	 * @return Returns the mcmHoststring.
	 */
	public String getMcmHoststring() {
		return mcmHoststring;
	}
	

	/**
	 * @param mcmHoststring The mcmHoststring to set.
	 */
	public void setMcmHoststring(String mcmHoststring) {
		this.mcmHoststring = mcmHoststring;
	}
	/**
	 * @return Returns the offCommand.
	 */
	public String getOffCommand() {
		return offCommand;
	}
	

	/**
	 * @param offCommand The offCommand to set.
	 */
	public void setOffCommand(String offCommand) {
		this.offCommand = offCommand;
	}
	

	/**
	 * @return Returns the onCommand.
	 */
	public String getOnCommand() {
		return onCommand;
	}
	

	/**
	 * @param onCommand The onCommand to set.
	 */
	public void setOnCommand(String onCommand) {
		this.onCommand = onCommand;
	}
	

	/**
	 * @return Returns the osdOffCommand.
	 */
	public String getOsdOffCommand() {
		return osdOffCommand;
	}
	

	/**
	 * @param osdOffCommand The osdOffCommand to set.
	 */
	public void setOsdOffCommand(String osdOffCommand) {
		this.osdOffCommand = osdOffCommand;
	}
	

	/**
	 * @return Returns the osdOnCommand.
	 */
	public String getOsdOnCommand() {
		return osdOnCommand;
	}
	

	/**
	 * @param osdOnCommand The osdOnCommand to set.
	 */
	public void setOsdOnCommand(String osdOnCommand) {
		this.osdOnCommand = osdOnCommand;
	}
	

	/**
	 * @return Returns the setVolumeCommand.
	 */
	public String getSetVolumeCommand() {
		return setVolumeCommand;
	}
	

	/**
	 * @param setVolumeCommand The setVolumeCommand to set.
	 */
	public void setSetVolumeCommand(String setVolumeCommand) {
		this.setVolumeCommand = setVolumeCommand;
	}
	

	/**
	 * @return Returns the switchToMediacastCommand.
	 */
	public String getSwitchToMediacastCommand() {
		return switchToMediacastCommand;
	}
	

	/**
	 * @param switchToMediacastCommand The switchToMediacastCommand to set.
	 */
	public void setSwitchToMediacastCommand(String switchToMediacastCommand) {
		this.switchToMediacastCommand = switchToMediacastCommand;
	}

	/**
	 * @return Returns the serialPort.
	 */
	public String getSerialPort() {
		return serialPort;
	}
	

	/**
	 * @param serialPort The serialPort to set.
	 */
	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

	/**
	 * @return Returns the diagnosticInterval.
	 */
	public String getDiagnosticInterval() {
		return diagnosticInterval;
	}
	

	/**
	 * @param diagnosticInterval The diagnosticInterval to set.
	 */
	public void setDiagnosticInterval(String diagnosticInterval) {
		this.diagnosticInterval = diagnosticInterval;
	}

	/**
	 * @return Returns the autoAdjustCommand.
	 */
	public synchronized String getAutoAdjustCommand() {
		return autoAdjustCommand;
	}
	

	/**
	 * @param autoAdjustCommand The autoAdjustCommand to set.
	 */
	public synchronized void setAutoAdjustCommand(String autoAdjustCommand) {
		this.autoAdjustCommand = autoAdjustCommand;
	}

	/**
	 * @return Returns the implementationType.
	 */
	public McmImplementationType getImplementationType() {
		return implementationType;
	}
	

	/**
	 * @param implementationType The implementationType to set.
	 */
	public void setImplementationType(McmImplementationType implementationType) {
		this.implementationType = implementationType;
	}
	

	/**
	 * @return Returns the setInversionOffCommand.
	 */
	public String getSetInversionOffCommand() {
		return setInversionOffCommand;
	}
	

	/**
	 * @param setInversionOffCommand The setInversionOffCommand to set.
	 */
	public void setSetInversionOffCommand(String setInversionOffCommand) {
		this.setInversionOffCommand = setInversionOffCommand;
	}
	

	/**
	 * @return Returns the setInversionOnCommand.
	 */
	public String getSetInversionOnCommand() {
		return setInversionOnCommand;
	}
	

	/**
	 * @param setInversionOnCommand The setInversionOnCommand to set.
	 */
	public void setSetInversionOnCommand(String setInversionOnCommand) {
		this.setInversionOnCommand = setInversionOnCommand;
	}
	

	/**
	 * @return Returns the setPixelShiftOffCommand.
	 */
	public String getSetPixelShiftOffCommand() {
		return setPixelShiftOffCommand;
	}
	

	/**
	 * @param setPixelShiftOffCommand The setPixelShiftOffCommand to set.
	 */
	public void setSetPixelShiftOffCommand(String setPixelShiftOffCommand) {
		this.setPixelShiftOffCommand = setPixelShiftOffCommand;
	}
	

	/**
	 * @return Returns the setPixelShiftOnCommand.
	 */
	public String getSetPixelShiftOnCommand() {
		return setPixelShiftOnCommand;
	}
	

	/**
	 * @param setPixelShiftOnCommand The setPixelShiftOnCommand to set.
	 */
	public void setSetPixelShiftOnCommand(String setPixelShiftOnCommand) {
		this.setPixelShiftOnCommand = setPixelShiftOnCommand;
	}
	

	/**
	 * @return Returns the setSignalPatternOffCommand.
	 */
	public String getSetSignalPatternOffCommand() {
		return setSignalPatternOffCommand;
	}
	

	/**
	 * @param setSignalPatternOffCommand The setSignalPatternOffCommand to set.
	 */
	public void setSetSignalPatternOffCommand(String setSignalPatternOffCommand) {
		this.setSignalPatternOffCommand = setSignalPatternOffCommand;
	}
	

	/**
	 * @return Returns the setSignalPatternOnCommand.
	 */
	public String getSetSignalPatternOnCommand() {
		return setSignalPatternOnCommand;
	}
	

	/**
	 * @param setSignalPatternOnCommand The setSignalPatternOnCommand to set.
	 */
	public void setSetSignalPatternOnCommand(String setSignalPatternOnCommand) {
		this.setSignalPatternOnCommand = setSignalPatternOnCommand;
	}

	/**
	 * @return Returns the switchToAuxCommand.
	 */
	public String getSwitchToAuxCommand() {
		return switchToAuxCommand;
	}
	

	/**
	 * @param switchToAuxCommand The switchToAuxCommand to set.
	 */
	public void setSwitchToAuxCommand(String switchToAuxCommand) {
		this.switchToAuxCommand = switchToAuxCommand;
	}


	/**
	 * @return the volumeOffsetCommand
	 */
	public String getVolumeOffsetCommand() {
		return volumeOffsetCommand;
	}

	/**
	 * @param volumeOffsetCommand the volumeOffsetCommand to set
	 */
	public void setVolumeOffsetCommand(String volumeOffsetCommand) {
		this.volumeOffsetCommand = volumeOffsetCommand;
	}

	/**
	 * @return the minGainCommand
	 */
	public String getMinGainCommand() {
		return minGainCommand;
	}

	/**
	 * @param minGainCommand the minGainCommand to set
	 */
	public void setMinGainCommand(String minGainCommand) {
		this.minGainCommand = minGainCommand;
	}

	/**
	 * @return the maxGainCommand
	 */
	public String getMaxGainCommand() {
		return maxGainCommand;
	}

	/**
	 * @param maxGainCommand the maxGainCommand to set
	 */
	public void setMaxGainCommand(String maxGainCommand) {
		this.maxGainCommand = maxGainCommand;
	}

	/**
	 * @return the muteCommand
	 */
	public String getMuteCommand() {
		return muteCommand;
	}

	/**
	 * @param muteCommand the muteCommand to set
	 */
	public void setMuteCommand(String muteCommand) {
		this.muteCommand = muteCommand;
	}

	/**
	 * @return the peripheralType
	 */
	public PeripheralType getPeripheralType() {
		return peripheralType;
	}

	/**
	 * @param peripheralType the peripheralType to set
	 */
	public void setPeripheralType(PeripheralType peripheralType) {
		this.peripheralType = peripheralType;
	}

	public String getButtonLockOnCommand() {
		return buttonLockOnCommand;
	}

	public void setButtonLockOnCommand(String buttonLockOnCommand) {
		this.buttonLockOnCommand = buttonLockOnCommand;
	}

	public String getButtonLockOffCommand() {
		return buttonLockOffCommand;
	}

	public void setButtonLockOffCommand(String buttonLockOffCommand) {
		this.buttonLockOffCommand = buttonLockOffCommand;
	}

	public String getSetBrightnessSignalCommand() {
		return setBrightnessSignalCommand;
	}

	public void setSetBrightnessSignalCommand(String setBrightnessSignalCommand) {
		this.setBrightnessSignalCommand = setBrightnessSignalCommand;
	}

	public String getSetContrastSignalCommand() {
		return setContrastSignalCommand;
	}

	public void setSetContrastSignalCommand(String setContrastSignalCommand) {
		this.setContrastSignalCommand = setContrastSignalCommand;
	}

	public String getCurrentButtonLockCommand() {
		return currentButtonLockCommand;
	}

	public void setCurrentButtonLockCommand(String currentButtonLockCommand) {
		this.currentButtonLockCommand = currentButtonLockCommand;
	}

	public String getWooferOffsetCommand() {
		return wooferOffsetCommand;
	}

	public void setWooferOffsetCommand(String wooferOffsetCommand) {
		this.wooferOffsetCommand = wooferOffsetCommand;
	}

	public String getResponseTimeCommand() {
		return responseTimeCommand;
	}

	public void setResponseTimeCommand(String responseTimeCommand) {
		this.responseTimeCommand = responseTimeCommand;
	}

	public String getInputSelectionCommand() {
		return inputSelectionCommand;
	}

	public void setInputSelectionCommand(String inputSelectionCommand) {
		this.inputSelectionCommand = inputSelectionCommand;
	}

	public String getFrontPanelLockCommand() {
		return frontPanelLockCommand;
	}

	public void setFrontPanelLockCommand(String frontPanelLockCommand) {
		this.frontPanelLockCommand = frontPanelLockCommand;
	}

	public String getVideoMuteCommand() {
		return videoMuteCommand;
	}

	public void setVideoMuteCommand(String videoMuteCommand) {
		this.videoMuteCommand = videoMuteCommand;
	}

	public String getAudioMuteCommand() {
		return audioMuteCommand;
	}

	public void setAudioMuteCommand(String audioMuteCommand) {
		this.audioMuteCommand = audioMuteCommand;
	}

	public String getCurrentAudioSourceCommand() {
		return currentAudioSourceCommand;
	}

	public void setCurrentAudioSourceCommand(String currentAudioSourceCommand) {
		this.currentAudioSourceCommand = currentAudioSourceCommand;
	}

	public String getCurrentSpeakerCommand() {
		return currentSpeakerCommand;
	}

	public void setCurrentSpeakerCommand(String currentSpeakerCommand) {
		this.currentSpeakerCommand = currentSpeakerCommand;
	}

	public String getCurrentPowerSaveCommand() {
		return currentPowerSaveCommand;
	}

	public void setCurrentPowerSaveCommand(String currentPowerSaveCommand) {
		this.currentPowerSaveCommand = currentPowerSaveCommand;
	}

	public String getCurrentRemoteControlCommand() {
		return currentRemoteControlCommand;
	}

	public void setCurrentRemoteControlCommand(String currentRemoteControlCommand) {
		this.currentRemoteControlCommand = currentRemoteControlCommand;
	}

	public String getCurrentTemperatureCommand() {
		return currentTemperatureCommand;
	}

	public void setCurrentTemperatureCommand(String currentTemperatureCommand) {
		this.currentTemperatureCommand = currentTemperatureCommand;
	}

	public String getCurrentSerialCodeCommand() {
		return currentSerialCodeCommand;
	}

	public void setCurrentSerialCodeCommand(String currentSerialCodeCommand) {
		this.currentSerialCodeCommand = currentSerialCodeCommand;
	}

	public String getSetAudioSourceCommand() {
		return setAudioSourceCommand;
	}

	public void setSetAudioSourceCommand(String setAudioSourceCommand) {
		this.setAudioSourceCommand = setAudioSourceCommand;
	}

	public String getSetSpeakerCommand() {
		return setSpeakerCommand;
	}

	public void setSetSpeakerCommand(String setSpeakerCommand) {
		this.setSpeakerCommand = setSpeakerCommand;
	}

	public String getSetPowerSaveCommand() {
		return setPowerSaveCommand;
	}

	public void setSetPowerSaveCommand(String setPowerSaveCommand) {
		this.setPowerSaveCommand = setPowerSaveCommand;
	}

	public String getSetRemoteControlCommand() {
		return setRemoteControlCommand;
	}

	public void setSetRemoteControlCommand(String setRemoteControlCommand) {
		this.setRemoteControlCommand = setRemoteControlCommand;
	}		
}
