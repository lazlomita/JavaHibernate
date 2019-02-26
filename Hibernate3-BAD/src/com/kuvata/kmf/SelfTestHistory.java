package com.kuvata.kmf;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import parkmedia.DispatcherConstants;
import parkmedia.KMFLogger;

import com.kuvata.kmf.util.Files;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.XPath;


/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class SelfTestHistory extends PersistentEntity 
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( SelfTestHistory.class );
	private Long selfTestHistoryId;
	private Device device;
	private Date dt;
	private Boolean initialConnectionTest;
	private Boolean dispatcherServerTest;
	private Boolean contentServerTest;
	private Boolean dispatcherServerRsyncTest;
	private Boolean contentServerRsyncTest;
	private Boolean dispatcherPingTest;
	private Boolean vpnPingTest;


	/**
	 * 
	 *
	 */
	public SelfTestHistory()
	{		
	}
	
	/**
	 * Called from DispatcherSoalBindingImpl.logFileUploadComplete().
	 * Updates the database with device presentations found in the given file_exists.xml file
	 * @param advancedPropertiesFile
	 */
	public static void handleSelfTestsFileUpload(File zipFile)
	{
		File selfTestsFile = null;
		try
		{							
			// First, unzip the zip file (ignoring directory structure) into the logs directory
			logger.info("About to unzip: "+ zipFile.getAbsolutePath());
			if( zipFile.exists() )
			{
				String unzipPath = zipFile.getAbsolutePath().substring( 0, zipFile.getAbsolutePath().lastIndexOf("/") );
				String extractedFileName = Files.unzip( zipFile.getAbsolutePath(), unzipPath, false );
				if( extractedFileName != null && extractedFileName.length() > 0 ){
					String selfTestsPath = unzipPath +"/"+ extractedFileName;
					selfTestsFile = new File( selfTestsPath );
					if( selfTestsFile.exists() )
					{	
						Document doc = new Document( selfTestsFile );
						Element root = doc.getElement( new XPath("//"+ DispatcherConstants.SELF_TESTS_ROOT_ELEMENT ) );
						
						// Get the mac address out of the root element
						String macAddr = root.getAttribute( DispatcherConstants.MAC_ADDRESS_ATTRIBUTE );
						if( macAddr != null && macAddr.length() > 0 )
						{
							Device device = Device.getDeviceByMacAddr( macAddr );
							if( device != null )
							{														
								// Parse the date out of the file name
								String strSelfTestDate = zipFile.getAbsolutePath().substring( zipFile.getAbsolutePath().lastIndexOf("/"), zipFile.getAbsolutePath().lastIndexOf(".") );
								strSelfTestDate = strSelfTestDate.substring( strSelfTestDate.lastIndexOf("_") + 1 );
								SimpleDateFormat dateFormat = new SimpleDateFormat( DispatcherConstants.UPLOAD_FILE_DATE_FORMAT );
								Date selfTestDate = dateFormat.parse( strSelfTestDate );
								Date now = new Date();
								
								// If this self test history is less than 5 mins into the future
								if( ( (selfTestDate.getTime() - now.getTime()) / 1000) - device.getTimezoneAdjustment() <= 300 ){
									
									// Create a new SelfTestHistory object
									SelfTestHistory selfTestHistory = new SelfTestHistory();
									selfTestHistory.setDevice( device );
									selfTestHistory.setDt( selfTestDate );
									
									// Get the result of each self test
									Elements es = root.getElements();						
									while( es.hasMoreElements() )
									{				
										Element e = (Element)es.next();						
										String name = e.getAttribute( DispatcherConstants.NAME_ATTRIBUTE );
										String value = e.getAttribute( DispatcherConstants.VALUE_ATTRIBUTE );
										
										if( name.equalsIgnoreCase( DispatcherConstants.INITIAL_CONNECTION_TEST ) ){
											selfTestHistory.setInitialConnectionTest( Boolean.valueOf( value ) );
										}else if( name.equalsIgnoreCase( DispatcherConstants.DISPATCHER_SERVER_TEST ) ){
											selfTestHistory.setDispatcherServerTest( Boolean.valueOf( value ) );
										}else if( name.equalsIgnoreCase( DispatcherConstants.CONTENT_SERVER_TEST ) ){
											selfTestHistory.setContentServerTest( Boolean.valueOf( value ) );
										}else if( name.equalsIgnoreCase( DispatcherConstants.DISPATCHER_SERVER_RSYNC_TEST ) ){
											selfTestHistory.setDispatcherServerRsyncTest( Boolean.valueOf( value ) );
										}else if( name.equalsIgnoreCase( DispatcherConstants.CONTENT_SERVER_RSYNC_TEST ) ){
											selfTestHistory.setContentServerRsyncTest( Boolean.valueOf( value ) );
										}else if( name.equalsIgnoreCase( DispatcherConstants.DISPATCHER_PING_TEST ) ){
											selfTestHistory.setDispatcherPingTest( Boolean.valueOf( value ) );
										}else if( name.equalsIgnoreCase( DispatcherConstants.VPN_PING_TEST ) ){
											selfTestHistory.setVpnPingTest( Boolean.valueOf( value ) );
										}
									}
									selfTestHistory.save();
								}else{
									logger.info("Ignoring self test history " + zipFile.getName() + " since it is more than 5 mins into the future.");
								}
							}
							else {
								logger.info("Could not locate a device with the given mac address: "+ macAddr +". Unable to continue.");
							}
						} else {
							logger.info("Could not locate macAddr attribute in "+ selfTestsFile.getAbsolutePath() +". Unable to continue.");
						}					
					} else {
						logger.info("Could not locate specified file: "+ selfTestsFile.getAbsolutePath() +". Unable to continue.");
					}
				}else{
					logger.info( "Could not extract self tests file: " + zipFile.getAbsolutePath() );
					return;
				}
			}else{
				logger.info("Could not locate zip file: "+ zipFile.getAbsolutePath() +". Unable to continue.");
				return;
			}
		}
		catch(Exception e)
		{
			logger.error( e );
		}
		finally
		{
			// Delete the zip file		
			if( zipFile != null && zipFile.isDirectory() == false ){				
				zipFile.delete();
			}
			
			// Delete the log file		
			if( selfTestsFile != null && selfTestsFile.isDirectory() == false ){				
				selfTestsFile.delete();
			}
		}		
	}

	public Long getEntityId(){
		return this.selfTestHistoryId;
	}

	/**
	 * @return Returns the contentServerRsyncTest.
	 */
	public Boolean getContentServerRsyncTest() {
		return contentServerRsyncTest;
	}
	


	/**
	 * @param contentServerRsyncTest The contentServerRsyncTest to set.
	 */
	public void setContentServerRsyncTest(Boolean contentServerRsyncTest) {
		this.contentServerRsyncTest = contentServerRsyncTest;
	}
	


	/**
	 * @return Returns the contentServerTest.
	 */
	public Boolean getContentServerTest() {
		return contentServerTest;
	}
	


	/**
	 * @param contentServerTest The contentServerTest to set.
	 */
	public void setContentServerTest(Boolean contentServerTest) {
		this.contentServerTest = contentServerTest;
	}
	


	/**
	 * @return Returns the dispatcherServerRsyncTest.
	 */
	public Boolean getDispatcherServerRsyncTest() {
		return dispatcherServerRsyncTest;
	}
	


	/**
	 * @param dispatcherServerRsyncTest The dispatcherServerRsyncTest to set.
	 */
	public void setDispatcherServerRsyncTest(Boolean dispatcherServerRsyncTest) {
		this.dispatcherServerRsyncTest = dispatcherServerRsyncTest;
	}
	


	/**
	 * @return Returns the dispatcherServerTest.
	 */
	public Boolean getDispatcherServerTest() {
		return dispatcherServerTest;
	}
	


	/**
	 * @param dispatcherServerTest The dispatcherServerTest to set.
	 */
	public void setDispatcherServerTest(Boolean dispatcherServerTest) {
		this.dispatcherServerTest = dispatcherServerTest;
	}
	


	/**
	 * @return Returns the dt.
	 */
	public Date getDt() {
		return dt;
	}
	


	/**
	 * @param dt The dt to set.
	 */
	public void setDt(Date dt) {
		this.dt = dt;
	}
	


	/**
	 * @return Returns the initialConnectionTest.
	 */
	public Boolean getInitialConnectionTest() {
		return initialConnectionTest;
	}
	


	/**
	 * @param initialConnectionTest The initialConnectionTest to set.
	 */
	public void setInitialConnectionTest(Boolean initialConnectionTest) {
		this.initialConnectionTest = initialConnectionTest;
	}



	/**
	 * @return Returns the dispatcherPingTest.
	 */
	public Boolean getDispatcherPingTest() {
		return dispatcherPingTest;
	}
	

	/**
	 * @param dispatcherPingTest The dispatcherPingTest to set.
	 */
	public void setDispatcherPingTest(Boolean dispatcherPingTest) {
		this.dispatcherPingTest = dispatcherPingTest;
	}
	

	/**
	 * @return Returns the vpnPingTest.
	 */
	public Boolean getVpnPingTest() {
		return vpnPingTest;
	}
	

	/**
	 * @param vpnPingTest The vpnPingTest to set.
	 */
	public void setVpnPingTest(Boolean vpnPingTest) {
		this.vpnPingTest = vpnPingTest;
	}
	

	/**
	 * @return Returns the selfTestHistoryId.
	 */
	public Long getSelfTestHistoryId() {
		return selfTestHistoryId;
	}
	


	/**
	 * @param selfTestHistoryId The selfTestHistoryId to set.
	 */
	public void setSelfTestHistoryId(Long selfTestHistoryId) {
		this.selfTestHistoryId = selfTestHistoryId;
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
	
	
	


}
