/*
 * Created on Sep 27, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Session;

import parkmedia.KMFLogger;
import parkmedia.usertype.DevicePropertyType;

import com.kuvata.kmf.util.Files;

import electric.xml.Document;
import electric.xml.ParseException;
import electric.xml.XPath;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public class McmHistoryEntry extends PersistentEntity
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( McmHistoryEntry.class );
	private Long mcmHistoryEntryId;
	private Mcm mcm;
	private Date timestamp;	
	private String screenshotFileloc;
	private String brightness;
	private String contrast;	
	private String volume;
	private String power;
	private String input;
	private String buttonLock;
	private String formattedTimestamp;
	private String mute;
	private String minGain;	
	private String maxGain;
	private String ambientNoise;
	private String volumeOffset;
	private String output;
	private String wooferOffset;
	private String responseTime;
	private String videoInput;
	private String audioInput;
	private String videoMute;
	private String audioMute;
	private String frontPanelLock;
	private String audioSource;
	private String speaker;
	private String powerSave;
	private String remoteControl;
	private String temperature;
	private String serialCode;
	
	private Boolean isLastMcmHistory;

	/**
	 * 
	 *
	 */
	public McmHistoryEntry() 
	{
	}
	
	/**
	 * NOTE: This method is required for backward compatibility.
	 * Once all devices are on 3.2, this method can be removed.
	 * 
	 * Parses the given mcm file and creates a McmHistoryEntry
	 * record according to the information in the given file.
	 * 
	 * @param mcmFilepath
	 */
	public static void createMcmHistoryEntry(File zipFile) throws ParseException
	{		
		File mcmFile = null;
		try
		{
			// First, unzip the zip file (ignoring directory structure) into the logs directory
			String unzipPath = zipFile.getAbsolutePath().substring( 0, zipFile.getAbsolutePath().lastIndexOf("/") );
			String extractedFileName = Files.unzip( zipFile.getAbsolutePath(), unzipPath, false );
			if( extractedFileName != null && extractedFileName.length() > 0 ){
				String mcmPath = unzipPath +"/"+ extractedFileName;
				mcmFile = new File( mcmPath );
				if( mcmFile.exists() )
				{
					Document doc = new Document( mcmFile );
					createMcmHistoryEntry( doc.toString() );
				}else{
					logger.info("Could not locate specified file: "+ mcmPath +". Unable to continue.");
				}
			}
		}catch(Exception e){
			logger.error( e );
		}finally{
			// Delete the zip file		
			if( zipFile != null && zipFile.isDirectory() == false ){				
				zipFile.delete();
			}
			// Delete the mcm.xml file			
			if( mcmFile != null && mcmFile.isDirectory() == false ){
				mcmFile.delete();
			}
		}
	}		
	
	/**
	 * Parses the given mcm history xml structure and creates a McmHistoryEntry
	 * record according to the information in the given string.
	 * 
	 * @param mcmFilepath
	 */
	public static void createMcmHistoryEntry(String mcmHistory) throws ParseException
	{		
		String brightness = null;
		String contrast = null;				
		String volume = null;
		String input = null;				
		String power = null;
		String buttonLockStatus = null;
		String mute = null;	
		String volumeOffset = null;
		String output = null;
		String wooferOffset = null;
		String responseTime = null;
		String ambientNoise = null;	
		String minGain = null;
		String maxGain = null;
		String videoInput = null;
		String audioInput = null;
		String videoMute = null;
		String audioMute = null;
		String frontPanelLock = null;
		String deviceTimestamp = null;
		String audioSource = null;
		String speaker = null;
		String powerSave = null;
		String remoteControl = null;
		String temperature = null;
		String serialCode = null;
		
		try
		{				
			Document doc = new Document( mcmHistory );
			String mcmId = doc.getElement( new XPath("//"+ Mcm.ROOT_ELEMENT) ).getAttribute( Mcm.MCM_ID_ATTRIBUTE );
			deviceTimestamp = doc.getElement( new XPath("//" + Mcm.ROOT_ELEMENT) ).getAttribute( Mcm.TIMESTAMP_ATTRIBUTE );

			// If the mcm is of type Display
			if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName()) ) != null){
				brightness = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_BRIGHTNESS_COMMAND.getPropertyName()) ).getAttribute( "value" );
				contrast = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_CONTRAST_COMMAND.getPropertyName()) ).getAttribute( "value" );				
				volume = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_VOLUME_COMMAND.getPropertyName()) ).getAttribute( "value" );
				input = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName()) ).getAttribute( "value" );				
				power = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName()) ).getAttribute( "value" );
				
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_AUDIO_SOURCE_COMMAND.getPropertyName()) ) != null){
					audioSource = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_AUDIO_SOURCE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_SPEAKER_COMMAND.getPropertyName()) ) != null){
					speaker = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_SPEAKER_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_POWER_SAVE_COMMAND.getPropertyName()) ) != null){
					powerSave = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_POWER_SAVE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_REMOTE_CONTROL_COMMAND.getPropertyName()) ) != null){
					remoteControl = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_REMOTE_CONTROL_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_TEMPERATURE_COMMAND.getPropertyName()) ) != null){
					temperature = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_TEMPERATURE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_SERIAL_CODE_COMMAND.getPropertyName()) ) != null){
					serialCode = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_SERIAL_CODE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_BUTTON_LOCK_COMMAND.getPropertyName()) ) != null){
					buttonLockStatus = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_BUTTON_LOCK_COMMAND.getPropertyName()) ).getAttribute( "value" );
				}
			}
			// If the mcm is of type Audio
			else if(doc.getElement( new XPath("//"+ DevicePropertyType.MUTE_COMMAND.getPropertyName()) ) != null){
				mute = doc.getElement( new XPath("//"+ DevicePropertyType.MUTE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				volumeOffset = doc.getElement( new XPath("//"+ DevicePropertyType.VOLUME_OFFSET_COMMAND.getPropertyName()) ).getAttribute( "value" );
				output = doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_OUTPUT_COMMAND) ).getAttribute( "value" );
				wooferOffset = doc.getElement( new XPath("//"+ DevicePropertyType.WOOFER_OFFSET_COMMAND.getPropertyName()) ).getAttribute( "value" );
				responseTime = doc.getElement( new XPath("//"+ DevicePropertyType.RESPONSE_TIME_COMMAND.getPropertyName()) ).getAttribute( "value" );
				ambientNoise = doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_CURRENT_AMBIENT_NOISE) ).getAttribute( "value" );
				minGain = doc.getElement( new XPath("//"+ DevicePropertyType.MIN_GAIN_COMMAND.getPropertyName()) ).getAttribute( "value" );
				maxGain = doc.getElement( new XPath("//"+ DevicePropertyType.MAX_GAIN_COMMAND.getPropertyName()) ).getAttribute( "value" );
				
				// Backward compatibility for devices on version prior to 3.6b11
				if(doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_CURRENT_AUDIO_INPUT) ) != null){
					input = doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_CURRENT_AUDIO_INPUT) ).getAttribute( "value" );
				}
			}
			// If the mcm is of type Switch
			else if(doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_VIDEO_INPUT) ) != null){
				videoInput = doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_VIDEO_INPUT) ).getAttribute( "value" );
				audioInput = doc.getElement( new XPath("//"+ DevicePropertyType.PROPERTY_AUDIO_INPUT) ).getAttribute( "value" );
				videoMute = doc.getElement( new XPath("//"+ DevicePropertyType.VIDEO_MUTE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				audioMute = doc.getElement( new XPath("//"+ DevicePropertyType.AUDIO_MUTE_COMMAND.getPropertyName()) ).getAttribute( "value" );
				frontPanelLock = doc.getElement( new XPath("//"+ DevicePropertyType.FRONT_PANEL_LOCK_COMMAND.getPropertyName()) ).getAttribute( "value" );
			}
			// If the mcm is of type Power Switch
			else if (doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_POWER_COMMAND) ) != null) {
				power = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_POWER_COMMAND.getPropertyName()) ).getAttribute( "value" );
				
				if(doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName()) ) != null)
					input = doc.getElement( new XPath("//"+ DevicePropertyType.CURRENT_INPUT_COMMAND.getPropertyName()) ).getAttribute( "value" );
			}
			
			// Create the new history entry
			Mcm mcm = Mcm.getMcm( new Long(mcmId) );
			if( mcm != null )
			{	
				DateFormat outputFormat = new SimpleDateFormat( Constants.OUTPUT_DATE_FORMAT );
				Session session = HibernateSession.currentSession();

				String hql = "UPDATE McmHistoryEntry "
					+ "SET is_last_mcm_history = 0 "
					+ "WHERE mcm_id = :mcmId "
					+ "AND is_last_mcm_history = 1";
				HibernateSession.beginTransaction();
				session.createQuery( hql )
					.setParameter("mcmId", mcm.getMcmId())
					.executeUpdate();
				HibernateSession.commitTransaction();
				
				McmHistoryEntry he = new McmHistoryEntry();
				try {
					Timestamp ts = new Timestamp(outputFormat.parse(deviceTimestamp).getTime());
					he.setTimestamp( ts );
				}
				catch(java.text.ParseException e) {
					Timestamp ts = new Timestamp( new Date().getTime() );
					he.setTimestamp( ts );
				}
				he.setMcm( mcm );
				he.setBrightness( brightness );
				he.setContrast( contrast );
				he.setVolume( volume );
				he.setInput( input );
				he.setPower( power );
				he.setButtonLock(buttonLockStatus);
				he.setMute(mute);
				he.setVolumeOffset(volumeOffset);
				he.setOutput(output);
				he.setWooferOffset(wooferOffset);
				he.setResponseTime(responseTime);
				he.setAmbientNoise(ambientNoise);
				he.setMinGain(minGain);
				he.setMaxGain(maxGain);
				he.setVideoInput(videoInput);
				he.setAudioInput(audioInput);
				he.setVideoMute(videoMute);
				he.setAudioMute(audioMute);
				he.setFrontPanelLock(frontPanelLock);
				he.setAudioSource(audioSource);
				he.setSpeaker(speaker);
				he.setPowerSave(powerSave);
				he.setRemoteControl(remoteControl);
				he.setTemperature(temperature);
				he.setSerialCode(serialCode);
				he.setIsLastMcmHistory( true );
				he.save();
			} else {
				logger.info("Could not locate mcm with given id: "+ mcmId);
			}
		}catch(Exception e){
			logger.error( e );
		}
	}	
			
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getMcmHistoryEntryId();
	}	
	/**
	 * @return Returns the brightness.
	 */
	public String getBrightness() {
		return brightness;
	}
	/**
	 * @param brightness The brightness to set.
	 */
	public void setBrightness(String brightness) {
		this.brightness = brightness;
	}
	/**
	 * @return Returns the contrast.
	 */
	public String getContrast() {
		return contrast;
	}
	/**
	 * @param contrast The contrast to set.
	 */
	public void setContrast(String contrast) {
		this.contrast = contrast;
	}
	/**
	 * @return Returns the mcm.
	 */
	public Mcm getMcm() {
		return mcm;
	}
	/**
	 * @param mcm The mcm to set.
	 */
	public void setMcm(Mcm mcm) {
		this.mcm = mcm;
	}
	/**
	 * @return Returns the mcmHistoryEntryId.
	 */
	public Long getMcmHistoryEntryId() {
		return mcmHistoryEntryId;
	}
	/**
	 * @param mcmHistoryEntryId The mcmHistoryEntryId to set.
	 */
	public void setMcmHistoryEntryId(Long mcmHistoryEntryId) {
		this.mcmHistoryEntryId = mcmHistoryEntryId;
	}
	/**
	 * @return Returns the screenshotFileloc.
	 */
	public String getScreenshotFileloc() {
		return screenshotFileloc;
	}
	/**
	 * @param screenshotFileloc The screenshotFileloc to set.
	 */
	public void setScreenshotFileloc(String screenshotFileloc) {
		this.screenshotFileloc = screenshotFileloc;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the formattedTimestamp.
	 */
	public String getFormattedTimestamp() {
		SimpleDateFormat df = new SimpleDateFormat( Constants.OUTPUT_DATE_FORMAT );			
		formattedTimestamp = df.format(this.getTimestamp());
		return formattedTimestamp;
	}
	/**
	 * @param formattedTimestamp The formattedTimestamp to set.
	 */
	public void setFormattedTimestamp(String formattedTimestamp) {
		this.formattedTimestamp = formattedTimestamp;
	}

	public String getInput() {
		return input;
	}
	

	public void setInput(String input) {
		this.input = input;
	}
	

	public String getPower() {
		return power;
	}
	

	public void setPower(String power) {
		this.power = power;
	}
	

	public String getVolume() {
		return volume;
	}
	

	public void setVolume(String volume) {
		this.volume = volume;
	}

	/**
	 * @return Returns the isLastMcmHistory.
	 */
	public Boolean getIsLastMcmHistory() {
		return isLastMcmHistory;
	}
	

	/**
	 * @param isLastMcmHistory The isLastMcmHistory to set.
	 */
	public void setIsLastMcmHistory(Boolean isLastMcmHistory) {
		this.isLastMcmHistory = isLastMcmHistory;
	}

	public String getMute() {
		return mute;
	}

	public void setMute(String mute) {
		this.mute = mute;
	}

	public String getMinGain() {
		return minGain;
	}

	public void setMinGain(String minGain) {
		this.minGain = minGain;
	}

	public String getMaxGain() {
		return maxGain;
	}

	public void setMaxGain(String maxGain) {
		this.maxGain = maxGain;
	}

	public String getAmbientNoise() {
		return ambientNoise;
	}

	public void setAmbientNoise(String ambientNoise) {
		this.ambientNoise = ambientNoise;
	}

	public String getVolumeOffset() {
		return volumeOffset;
	}

	public void setVolumeOffset(String volumeOffset) {
		this.volumeOffset = volumeOffset;
	}

	public String getButtonLock() {
		return buttonLock;
	}

	public void setButtonLock(String buttonLock) {
		this.buttonLock = buttonLock;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getWooferOffset() {
		return wooferOffset;
	}

	public void setWooferOffset(String wooferOffset) {
		this.wooferOffset = wooferOffset;
	}

	public String getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(String responseTime) {
		this.responseTime = responseTime;
	}

	public String getVideoInput() {
		return videoInput;
	}

	public void setVideoInput(String videoInput) {
		this.videoInput = videoInput;
	}

	public String getAudioInput() {
		return audioInput;
	}

	public void setAudioInput(String audioInput) {
		this.audioInput = audioInput;
	}

	public String getVideoMute() {
		return videoMute;
	}

	public void setVideoMute(String videoMute) {
		this.videoMute = videoMute;
	}

	public String getAudioMute() {
		return audioMute;
	}

	public void setAudioMute(String audioMute) {
		this.audioMute = audioMute;
	}

	public String getFrontPanelLock() {
		return frontPanelLock;
	}

	public void setFrontPanelLock(String frontPanelLock) {
		this.frontPanelLock = frontPanelLock;
	}

	public String getAudioSource() {
		return audioSource;
	}

	public void setAudioSource(String audioSource) {
		this.audioSource = audioSource;
	}

	public String getSpeaker() {
		return speaker;
	}

	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}

	public String getPowerSave() {
		return powerSave;
	}

	public void setPowerSave(String powerSave) {
		this.powerSave = powerSave;
	}

	public String getRemoteControl() {
		return remoteControl;
	}

	public void setRemoteControl(String remoteControl) {
		this.remoteControl = remoteControl;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getSerialCode() {
		return serialCode;
	}

	public void setSerialCode(String serialCode) {
		this.serialCode = serialCode;
	}
	
}
