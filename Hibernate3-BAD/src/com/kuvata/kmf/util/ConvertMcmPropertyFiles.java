package com.kuvata.kmf.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import parkmedia.KuvataConfig;
import parkmedia.device.entities.Mcm;
import parkmedia.usertype.DevicePropertyType;
import parkmedia.usertype.McmImplementationType;
import parkmedia.usertype.PeripheralType;

import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class ConvertMcmPropertyFiles {

	private static void doConversion() throws Exception
	{
		try
		{
			SchemaDirectory.initialize("kuvata", "ConvertMcmPropertyFiles", null, false, true);	
			String mcmDirectoryPath = KuvataConfig.getKuvataHome() +"/mcms";
			File file = new File( mcmDirectoryPath );
			if( file.isDirectory() ) 
			{
				// For each mcm property file
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++)
				{
					Properties properties = new Properties();
					properties.load( new FileInputStream( files[i].getAbsolutePath() ) );
				
					// Parse the mcm_id out of the filename 
					String mcmId = files[i].getName();				
					mcmId = mcmId.substring( 0, mcmId.indexOf(".") );
					
					// Create a new row in the mcm table
					Mcm mcm = Mcm.create( Long.valueOf( mcmId ) );
					mcm.setMcmHoststring( properties.getProperty( DevicePropertyType.MCM_HOSTSTRING.getPropertyName() ) );			
					
					PeripheralType peripheralType = PeripheralType.DISPLAY;
					String display = properties.getProperty( "display" );
					if( display.equalsIgnoreCase( McmImplementationType.BROWN_INNOVATIONS.getName() ) ){
						peripheralType = PeripheralType.AUDIO;
					}
					mcm.setPeripheralType( peripheralType );
					mcm.setSerialPort( properties.getProperty( DevicePropertyType.SERIAL_PORT.getPropertyName() ) );
					mcm.setSwitchToAuxCommand( properties.getProperty( DevicePropertyType.SWITCH_TO_AUX_COMMAND.getPropertyName() ) );
					mcm.setSwitchToMediacastCommand( properties.getProperty( DevicePropertyType.SWITCH_TO_MEDIACAST_COMMAND.getPropertyName() ) );
					mcm.setOnCommand( properties.getProperty( DevicePropertyType.ON_COMMAND.getPropertyName() ) );
					mcm.setOffCommand( properties.getProperty( DevicePropertyType.OFF_COMMAND.getPropertyName() ) );
					mcm.setCurrentPowerCommand( properties.getProperty( DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName() ) );
					mcm.setCurrentVolumeCommand( properties.getProperty( DevicePropertyType.CURRENT_VOLUME_COMMAND.getPropertyName() ) );
					mcm.setCurrentBrightnessCommand( properties.getProperty( DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName() ) );
					mcm.setCurrentContrastCommand( properties.getProperty( DevicePropertyType.CURRENT_CONTRAST_COMMAND.getPropertyName() ) );
					mcm.setCurrentInputCommand( properties.getProperty( DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName() ) );
					mcm.setSetVolumeCommand( properties.getProperty( DevicePropertyType.SET_VOLUME_COMMAND.getPropertyName() ) );
					mcm.setOsdOnCommand( properties.getProperty( DevicePropertyType.OSD_ON_COMMAND.getPropertyName() ) );				
					mcm.setOsdOffCommand( properties.getProperty( DevicePropertyType.OSD_OFF_COMMAND.getPropertyName() ) );
					mcm.setAutoAdjustCommand( properties.getProperty( DevicePropertyType.AUTO_ADJUST_COMMAND.getPropertyName() ) );
					mcm.setDiagnosticInterval( properties.getProperty( DevicePropertyType.DIAGNOSTIC_INTERVAL.getPropertyName() ) );
					mcm.setImplementationType( properties.getProperty( "mcmImplementationType" ) );
					mcm.setSetInversionOnCommand( properties.getProperty( DevicePropertyType.SET_INVERSION_ON_COMMAND.getPropertyName() ) );
					mcm.setSetInversionOffCommand( properties.getProperty( DevicePropertyType.SET_INVERSION_OFF_COMMAND.getPropertyName() ) );
					mcm.setSetSignalPatternOnCommand( properties.getProperty( DevicePropertyType.SET_SIGNAL_PATTERN_ON_COMMAND.getPropertyName() ) );
					mcm.setSetSignalPatternOffCommand( properties.getProperty( DevicePropertyType.SET_SIGNAL_PATTERN_OFF_COMMAND.getPropertyName() ) );
					mcm.setSetPixelShiftOnCommand( properties.getProperty( DevicePropertyType.SET_PIXEL_SHIFT_ON_COMMAND.getPropertyName() ) );
					mcm.setSetPixelShiftOffCommand( properties.getProperty( DevicePropertyType.SET_PIXEL_SHIFT_OFF_COMMAND.getPropertyName() ) );
					mcm.update();			
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			HibernateSession.closeSession();
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			doConversion();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
