/*
 * Created on Apr 24, 2007
 *
 * Kuvata, Inc.
 */
package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import parkmedia.KuvataConfig;
import parkmedia.device.configurator.DeviceProperty;
import parkmedia.device.configurator.PlatformProperty;
import parkmedia.device.entities.FileStatus;
import parkmedia.device.entities.Mcm;
import parkmedia.usertype.DevicePropertyType;
import parkmedia.usertype.DownloadPriorityType;
import parkmedia.usertype.FileStatusStatusType;

import com.kuvata.configurator.McmProperty;
import com.kuvata.configurator.PlatformPropertyLinux;
import com.kuvata.kmf.SchemaDirectory;

public class CreateOrUpdateProperty {
	
	public CreateOrUpdateProperty(){}
	
	private void createOrUpdate(String propertyName, String propertyValue, String previousPropertyName, boolean replaceValue) throws IOException {
		KuvataConfig.createOrUpdateProperty(propertyName, propertyValue, previousPropertyName, replaceValue);
	}
	
	private void createOrUpdate(String fileloc, String propertyName, String propertyValue, String previousPropertyName, boolean replaceValue) throws IOException {
		KuvataConfig.createOrUpdateProperty(fileloc, propertyName, propertyValue, previousPropertyName, replaceValue);
	}
	
	private void createFileStatusesForFilesInCache(){
		SchemaDirectory.initialize("kuvata", "createFileStatusesForFilesInCache", null, false, true);
		File presentationsDir = new File("/kuvata/cache/presentations");
		for(File dir : presentationsDir.listFiles()){
			if(dir.isDirectory()){
				System.out.println("Checking " + dir.getAbsolutePath());
				for(File f : dir.listFiles()){
					String relativePath = f.getAbsolutePath();
					relativePath = relativePath.substring(relativePath.indexOf("presentations"));
					List l = FileStatus.getFileStatus(relativePath);
					if(l.size() == 0){
						System.out.println("Couldn't locate file_status row. Creating one for " + relativePath);
						FileStatus.createOrUpdate(relativePath, FileStatusStatusType.IN_PROGRESS, null, null, DownloadPriorityType.NORMAL_PRIORITY_CS, false, false);
					}
				}
			}
		}
	}
	
	private void createOrUpdateNoProp(String nopropfileloc, String lineText, String action, String previousLineText) throws IOException {
		
		if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("delete") && !action.equalsIgnoreCase("modify") ) {
			throw new IOException("parameter three when editing non-property files must be either add, delete, or modify");
		}		
		KuvataConfig.updateNonPropertyFile(nopropfileloc, lineText, action, previousLineText);
	}
	
	private void insertAutoAdjustMcmProperty() throws IOException
	{
		// Locate each mcm currently on the device
		List<Mcm> mcms = Mcm.getManagedMcms();				
		for( Iterator<Mcm> i = mcms.iterator(); i.hasNext(); )
		{
			Mcm mcm = i.next();						
			McmProperty mcmProp = new McmProperty( mcm.getMcmId().toString() );
			mcmProp.update( DevicePropertyType.AUTO_ADJUST_COMMAND.getPropertyName(), "ju 0 1" );
		}		
	}
	
	private void insertMcmProperties_2_12() throws IOException
	{
		// Locate each mcm currently on the device
		List<Mcm> mcms = Mcm.getManagedMcms();				
		for( Iterator<Mcm> i = mcms.iterator(); i.hasNext(); )
		{
			Mcm mcm = i.next();						
			mcm.setSwitchToAuxCommand("");
			mcm.setSetInversionOnCommand("");
			mcm.setSetInversionOffCommand("");
			mcm.setSetSignalPatternOnCommand("");
			mcm.setSetSignalPatternOffCommand("");
			mcm.setSetPixelShiftOnCommand("");
			mcm.setSetPixelShiftOffCommand("");
			mcm.setImplementationType("");			
		}		
	}
	
	private void updateXorgDepthTo24() throws IOException
	{
		String fileContents = "";
		String line = "";
		
		PlatformPropertyLinux.backupXorg();
		
		BufferedReader in = new BufferedReader(new FileReader( "/etc/X11/xorg.conf" ));
		while((line = in.readLine()) != null)
		{
			if (line.contains("Depth")) {
				String part1 = line.substring(0, line.indexOf("Depth") + 5);
				fileContents += part1 + "	24" + "\n";
			}
			else {
				fileContents += line + "\n";
			}			
		}
		in.close();
		in = null;
		
		/*
		 * Rewrite the properties file
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter( "/etc/X11/xorg.conf" ));	
		out.write( fileContents );
		out.close();
		out = null;	
	}
	
	private void updateComposite() throws IOException
	{
		String filename = "/etc/X11/xorg.conf";
		String fileContents = "";
		String line = "";
		
		PlatformPropertyLinux.backupXorg();
		
		boolean hasCompositeSection = false;
		
		BufferedReader in = new BufferedReader(new FileReader( filename ));
		while((line = in.readLine()) != null)
		{
			// Ignore lines that begin with "#"
			if(!(line.indexOf("#") >= 0))
			{
				if (line.contains("Composite")) {
					hasCompositeSection = true;
				}
			}			
		}
		in.close();
		in = null;
		
		if (hasCompositeSection == true) 
		{
			createOrUpdateNoProp(filename, "Composite", "modify", "	Option \"Composite\" \"Enable\"");
		}
		else 
		{
			in = new BufferedReader(new FileReader( filename ));
			while((line = in.readLine()) != null)
			{
				// Write out the original line
				fileContents += line + "\n";
			}
			fileContents += "\n";
			fileContents += "Section \"Extensions\"\n";
			fileContents += "	Option \"Composite\" \"Enable\"\n";
			fileContents += "EndSection";
			in.close();
			in = null;
			
			/*
			 * Rewrite the properties file
			 */
			BufferedWriter out = new BufferedWriter(new FileWriter( filename ));			
			out.write( fileContents );
			out.close();
			out = null;		
		}		
	}
	
	private void updateGrubWithVesa() throws IOException
	{
		String filename = "/boot/grub/grub.conf";
		String fileContents = "";
		String line = "";
		String newLine = "";
				
		BufferedReader in = new BufferedReader(new FileReader( filename ));
		while((line = in.readLine()) != null)
		{
			if (!line.startsWith("#") && line.contains("kernel") && !line.contains("video=vesafb")) {
				
				if (line.contains("vga=")) {					
					String part1 = line.substring(0, line.indexOf("vga=") - 1);
					newLine = part1 + " video=vesafb vga=771";
				}
				else {
					newLine = line + " video=vesafb";
				}
				fileContents += newLine + "\n";
			}
			
			// If we did not set the newLine
			if(newLine.length() == 0)
			{
				// Write out the original line
				fileContents += line + "\n";
			}
			newLine = "";
		}
		in.close();
		in = null;
					
		/*
		 * Rewrite the properties file
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter( filename ));			
		out.write( fileContents );
		out.close();
		out = null;				
	}
	
	private void updateGrubWithVGA() throws IOException
	{
		String filename = "/boot/grub/grub.conf";
		String fileContents = "";
		String line = "";
		String newLine = "";
				
		BufferedReader in = new BufferedReader(new FileReader( filename ));
		while((line = in.readLine()) != null)
		{
			if (!line.startsWith("#") && line.contains("kernel")) {
				
				if (line.contains("vga=")) {					
					String part1 = line.substring(0, line.indexOf("vga=") - 1);
					newLine = part1 + " vga=786";
				}
				else {
					newLine = line + " vga=786";
				}
				fileContents += newLine + "\n";
			}
			
			// If we did not set the newLine
			if(newLine.length() == 0)
			{
				// Write out the original line
				fileContents += line + "\n";
			}
			newLine = "";
		}
		in.close();
		in = null;
					
		/*
		 * Rewrite the properties file
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter( filename ));			
		out.write( fileContents );
		out.close();
		out = null;				
	}
	
	private void updateOpenVPNServer() throws IOException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		   String currentDispatcherServer = DeviceProperty.getPropertyValue( DevicePropertyType.DISPATCHER_SERVERS );
		   DeviceProperty.setPropertyValue( DevicePropertyType.DISPATCHER_SERVERS, currentDispatcherServer );
		   DeviceProperty.setPropertyValue( DevicePropertyType.VPN_SERVER, "10.251.0.1" );
	}
	
	private void getServerIP() throws IOException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		   String currentDispatcherServer = DeviceProperty.getPropertyValue( DevicePropertyType.DISPATCHER_SERVERS );
		   System.out.println(currentDispatcherServer);
	}
	
	private void removeExtraRotationLine() throws IOException
	{
		String filename = "/etc/X11/xorg.conf";
		String fileContents = "";
		String line = "";
		
		PlatformPropertyLinux.backupXorg();
		
		int numRotationLines = 0;
		
		BufferedReader in = new BufferedReader(new FileReader( filename ));
		while((line = in.readLine()) != null)
		{
			if (line.contains("Rotate")) {
				numRotationLines++;
			}			
		}
		in.close();
		in = null;
		
		if (numRotationLines >= 2) {
			
			boolean foundLine = false;
			in = new BufferedReader(new FileReader( filename ));
			while((line = in.readLine()) != null)
			{
				// Write out the original line
				if (!line.contains("Rotate") || foundLine == true) {
					fileContents += line + "\n";
				}
				else {
					foundLine = true;
				}
			}
			in.close();
			in = null;
			
			/*
			 * Rewrite the properties file
			 */
			BufferedWriter out = new BufferedWriter(new FileWriter( filename ));			
			out.write( fileContents );
			out.close();
			out = null;		
		}				
	}
	
	private void addExecToProperty(String propertyName) throws IOException
	{
		String fileloc = KuvataConfig.getPropertiesFilePath();
		String fileContents = "";
		String line = "";
		String newLine = "";

		
		// Read in the contents of the properties file
		// First pass see if there is a property to be updated
		BufferedReader in = new BufferedReader(new FileReader( fileloc ));
		while((line = in.readLine()) != null)
		{
			// If this is a property line
			if(line.indexOf("=") > 0)
			{
				// Parse out the name and value
				String name = line.substring(0, line.indexOf("=")).trim();															
				if(name.equals(propertyName)){
					
					//	Add exec to beginning if it is not already there
					String propertyValue = KuvataConfig.getPropertyValue(name);
					if (!propertyValue.startsWith("exec")) {
					            propertyValue = "exec " + propertyValue;
					}
					
					newLine = name +"="+ propertyValue;
					if (propertyValue != null) {
						fileContents += newLine + "\n";
					}
				}
			}

			// If we did not set the newLine
			if(newLine.length() == 0)
			{
				// Write out the original line
				fileContents += line + "\n";
			}
			newLine = "";
		}
		in.close();
		in = null;

		/*
		 * Rewrite the properties file
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter( fileloc ));
		out.write( fileContents );
		out.close();
		out = null;
	}
	
	private void updateNtpConf()
	{		
		SchemaDirectory.initialize( "kuvata", "DeviceReleaseUpdateNtpConf", null, false, true );
		PlatformProperty.getPlatformProperty().updateNtpServers();
	}	 
	
	private void updateMplayerCommand() throws IOException
	{
		String fileloc = KuvataConfig.getPropertiesFilePath();
		String fileContents = "";
		String line = "";
		String newLine = "";

		
		// Read in the contents of the properties file
		// First pass see if there is a property to be updated
		BufferedReader in = new BufferedReader(new FileReader( fileloc ));
		while((line = in.readLine()) != null)
		{
			// If this is a property line
			if(line.indexOf("=") > 0)
			{
				// Parse out the name and value
				String name = line.substring(0, line.indexOf("=")).trim();															
				if(name.equals("Presenter.videoCommand")){
					
					//	Add exec to beginning if it is not already there
					String propertyValue = KuvataConfig.getPropertyValue(name);
					
					if (!propertyValue.contains("framedrop"))
					{
						String part1 = propertyValue.substring(0, propertyValue.indexOf("pp=0x20000") + 10);
						String part2 = propertyValue.substring(propertyValue.indexOf("pp=0x20000") + 10);
						propertyValue = part1 + " -noslices -framedrop" + part2;
						
						newLine = name +"="+ propertyValue;
						fileContents += newLine + "\n";
					}
				}
			}

			// If we did not set the newLine
			if(newLine.length() == 0)
			{
				// Write out the original line
				fileContents += line + "\n";
			}
			newLine = "";
		}
		in.close();
		in = null;

		/*
		 * Rewrite the properties file
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter( fileloc ));
		out.write( fileContents );
		out.close();
		out = null;		
	}
	
	private void updateMplayerCommandForGeForce4() throws IOException
	{
		String fileloc = KuvataConfig.getPropertiesFilePath();
		String fileContents = "";
		String line = "";
		String newLine = "";

		
		// Read in the contents of the properties file
		// First pass see if there is a property to be updated
		BufferedReader in = new BufferedReader(new FileReader( fileloc ));
		while((line = in.readLine()) != null)
		{
			// If this is a property line
			if(line.indexOf("=") > 0)
			{
				// Parse out the name and value
				String name = line.substring(0, line.indexOf("=")).trim();															
				if(name.equals("Presenter.videoCommand") || name.equals("Presenter.wmvCommand") || name.equals("Presenter.audioCommand")){
					
					String propertyValue = KuvataConfig.getPropertyValue(name);
					
					if (!propertyValue.contains(":port=128"))
					{
						String part1 = propertyValue.substring(0, propertyValue.indexOf("-vo xv") + 6);
						String part2 = propertyValue.substring(propertyValue.indexOf("-vo xv") + 6);
						propertyValue = part1 + ":port=128" + part2;
						
						newLine = name +"="+ propertyValue;
						fileContents += newLine + "\n";
					}
				}
			}

			// If we did not set the newLine
			if(newLine.length() == 0)
			{
				// Write out the original line
				fileContents += line + "\n";
			}
			newLine = "";
		}
		in.close();
		in = null;

		/*
		 * Rewrite the properties file
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter( fileloc ));
		out.write( fileContents );
		out.close();
		out = null;		
	}
	
	private void updateMplayerCommandForSlaveMode() throws IOException
	{
		String videoCommand = KuvataConfig.getPropertyValue("Presenter.videoCommand");
		String port = "";
		if (videoCommand.contains("port=")) {
			port = videoCommand.substring(videoCommand.indexOf("port=") + 5, videoCommand.indexOf("port=") + 8);			
		}
		else {
			try {
				port = KuvataConfig.getPropertyValue("Presenter.xvPort");
			} catch (Exception e) {
				// Ignore
			}
		}
		String videoOutput = "";
		if (videoCommand.contains(" -vo")) {
			String temp = videoCommand.substring(videoCommand.indexOf(" -vo") + 5);
			int index = temp.indexOf(" ");
			if (temp.indexOf(":") >= 0 && temp.indexOf(":") < index) {
				index = temp.indexOf(":");
			}
			videoOutput = temp.substring(0, index);			
		}
		else
		{
			try {
				videoOutput = KuvataConfig.getPropertyValue("Presenter.videoOutput");
			} catch (Exception e) {
				// Ignore
			}			
		}
		
		String videoCommandOld = null;
		try {
			videoCommandOld = KuvataConfig.getPropertyValue("Presenter.videoCommand.old");
		} catch (Exception e) {
			// Ignore
		}
		
		// Get the current OS version
		String osVersion = PlatformProperty.getOsVersion("uname -s -r");
		
		CreateOrUpdateProperty up = new CreateOrUpdateProperty();
		up.createOrUpdate("Presenter.xvPort", port, "Presenter.videoCommand", true);
		up.createOrUpdate("Presenter.videoOutput", videoOutput, "Presenter.videoCommand", true);
		
		// If this is a fedora core device
		if(osVersion != null && osVersion.contains(".fc")){
			up.createOrUpdate("Presenter.videoCommand", "nice -n -10 /usr/local/bin/mplayer -ao alsa " +
					"-noslices -framedrop -quiet -osdlevel 0 -nolirc -nojoystick -nomouseinput -slave -idle", null, true);
		}
		// If this is not a fedora (debian) device
		else
		{
			up.createOrUpdate("Presenter.videoCommand", "nice -n -10 /usr/local/mplayer-vdpau/bin/mplayer -vc ffh264vdpau -ao alsa " +
					"-autosync 1 -noslices -framedrop -quiet -nolirc -nojoystick -nomouseinput -slave -idle", null, true);
		}
		up.createOrUpdate("Presenter.wmvCommand", null, null, true);
		
		if(videoCommandOld == null) {
			up.createOrUpdate("Presenter.videoCommand.old", videoCommand, "Presenter.xvPort", true);
		}
	}
	
	private void addDeviceCommunicationServerLogger() throws IOException
	{
		String fileloc = KuvataConfig.getPropertiesFilePath();
		String fileContents = "";
		String line = "";
		
		boolean alreadyInserted = false;
		
		BufferedReader in = new BufferedReader(new FileReader( fileloc ));
		while((line = in.readLine()) != null)
		{
			if (line.contains("Separate log for the device communication server")) {
				alreadyInserted = true;
			}			
		}
		in.close();
		in = null;
		
		if (alreadyInserted == false) {			
			
			in = new BufferedReader(new FileReader( fileloc ));
			while((line = in.readLine()) != null)
			{
				// Write out the original line
				if (!line.contains("log4j.appender.P.MaxBackupIndex")) {
					fileContents += line + "\n";
				}
				else {
					fileContents += line + "\n\n" +
					"##### Separate log for the device communication server\n" +
					"log4j.category.com.kuvata.mcm=debug, D\n" +
					"log4j.appender.D=org.apache.log4j.RollingFileAppender\n" +
					"log4j.appender.D.File=/kuvata/logs/deviceCommunicationServer.log\n" +
					"log4j.appender.D.layout=org.apache.log4j.PatternLayout\n" +
					"log4j.appender.D.layout.ConversionPattern=%d %-5p [%C{1}:%L] %m%n\n" +
					"log4j.appender.D.MaxFileSize=100MB\n" +
					"log4j.appender.D.MaxBackupIndex=1\n\n";
				}

			}
			in.close();
			in = null;
			
			/*
			 * Rewrite the properties file
			 */
			BufferedWriter out = new BufferedWriter(new FileWriter( fileloc ));			
			out.write( fileContents );
			out.close();
			out = null;		
		}
	}
	
	private void updateRecordOff() throws IOException
	{
		String filename = "/etc/X11/xorg.conf";
		String fileContents = "";
		String line = "";
		
		PlatformPropertyLinux.backupXorg();
		
		boolean hasModuleSection = false;
		
		BufferedReader in = new BufferedReader(new FileReader( filename ));
		while((line = in.readLine()) != null)
		{
			// Ignore lines that begin with "#"
			if(!(line.indexOf("#") >= 0))
			{				
				if (line.contains("Section \"Module\"")) {
					hasModuleSection = true;
				}
			}			
		}
		in.close();
		in = null;
		
		if (hasModuleSection == true) 
		{
			createOrUpdateNoProp(filename, "    Disable \"record\"", "add", "Section \"Module\"");
		}
		else 
		{
			boolean foundLine = false;
			in = new BufferedReader(new FileReader( filename ));
			while((line = in.readLine()) != null)
			{
				// Write out the new section
				if (line.contains("EndSection") && foundLine == false) {
					fileContents += line + "\n\n";
					fileContents += "Section \"Module\"\n";
					fileContents += "    Disable \"record\"\n";
					fileContents += "EndSection\n";
					foundLine = true;
				}
				else {								
					// Write out the original line
					fileContents += line + "\n";
				}
			}
			in.close();
			in = null;
			
			/*
			 * Rewrite the properties file
			 */
			BufferedWriter out = new BufferedWriter(new FileWriter( filename ));			
			out.write( fileContents );
			out.close();
			out = null;		
		}		
	}

		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			if (args.length != 1 && args.length != 2 && args.length != 3 && args.length != 4) {
				System.out.println("usage: java com.kuvata.kmf.util.CreateOrUpdateProperty fileloc={path}(optional) propertyName properyValue previousPropertyName(optional for create)");
				System.out.println(" 	   if propertyName is found, previousPropertyName will be ignored");
				System.out.println("  	   to delete property, omit propertyValue and previousPropertyName");
				System.out.println("usage: java com.kuvata.kmf.util.CreateOrUpdateProperty nopropfileloc={path} lineText [add|delete] previousLineText(optional for create)");
				System.out.println(" 	   if the first parameter starts with fileloc= that file location will be the file updated");
				System.out.println(" 	   if the first parameter starts with nopropfileloc= that file location will be the file updated.");
				System.out.println("			if add is specified, lineText will be placed at the line after previousLineText, or at end of file");
				System.out.println("			if delete is specified, the line that contains lineText will be deleted from the file");
				System.out.println("			if modify is specified, the line that contains lineText will be modified to be what the value of previousLineText is");
			}
			else {
				
				boolean useFileloc = false;
				String fileloc = "";
				if (args[0].startsWith("fileloc=")) {
					fileloc = args[0].substring(8);
					File f = new File(fileloc);
					if (!f.exists()) {
						throw new IOException("File location at " + fileloc + " does not exist");
					}
					useFileloc = true;
				}
				
				boolean useNoPropFileloc = false;
				String nopropfileloc = "";
				if (args[0].startsWith("nopropfileloc=")) {
					nopropfileloc = args[0].substring(14);
					File f = new File(nopropfileloc);
					if (!f.exists()) {
						throw new IOException("File location at " + nopropfileloc + " does not exist");
					}
					useNoPropFileloc = true;
				}
				
				boolean otherUpdate = false;
				boolean updateMcm = false;
				if (args[0].equals("updateMcm")) {					
					updateMcm = true;
					otherUpdate = true;
				}
				
				boolean updateDepth = false;
				if (args[0].equals("updateDepth")) {
					updateDepth = true;
					otherUpdate = true;
				}
				
				boolean updateComposite = false;
				if (args[0].equals("updateComposite")) {
					updateComposite = true;
					otherUpdate = true;
				}
				
				boolean updateMcm212 = false;
				if (args[0].equals("updateMcm_2_12")) {
					updateMcm212 = true;
					otherUpdate = true;
				}
				
				boolean updateOpenVPNServer = false;
				if (args[0].equals("updateOpenVPNServer")) {
					updateOpenVPNServer = true;
					otherUpdate = true;
				}
				
				boolean getServerIP = false;
				if (args[0].equals("getServerIP")) {
					getServerIP = true;
					otherUpdate = true;
				}
				
				boolean removeExtraRotationLine = false;
				if (args[0].equals("removeExtraRotationLine")) {
					removeExtraRotationLine = true;
					otherUpdate = true;
				}
				
				boolean addExecToProperty = false;
				if (args[0].equals("addExecToProperty") && args[1] != null) {
					addExecToProperty = true;
					otherUpdate = true;
				}

				boolean updateNtpConf = false;
				if (args[0].equals("updateNtpConf")) {
					updateNtpConf = true;
					otherUpdate = true;
				}

				boolean updateGrubWithVesa = false;
				if (args[0].equals("updateGrubWithVesa")) {
					updateGrubWithVesa = true;
					otherUpdate = true;
				}
				
				boolean updateGrubWithVGA = false;
				if (args[0].equals("updateGrubWithVGA")) {
					updateGrubWithVGA = true;
					otherUpdate = true;
				}
				
				boolean updateMplayerCommand = false;
				if (args[0].equals("updateMplayerCommand")) {
					updateMplayerCommand = true;
					otherUpdate = true;
				}
				
				boolean updateMplayerCommandForGeForce4 = false;
				if (args[0].equals("updateMplayerCommandForGeForce4")) {
					updateMplayerCommand = true;
					otherUpdate = true;
				}
				
				boolean addDeviceCommunicationServerLogger = false;
				if (args[0].equals("addDeviceCommunicationServerLogger")) {
					addDeviceCommunicationServerLogger = true;
					otherUpdate = true;
				}
				
				boolean createFileStatusesForFilesInCache = false;
				if (args[0].equals("createFileStatusesForFilesInCache")){
					createFileStatusesForFilesInCache = true;
					otherUpdate = true;
				}
				
				boolean updateMplayerCommandForSlaveMode = false;
				if (args[0].equals("updateMplayerCommandForSlaveMode")){
					updateMplayerCommandForSlaveMode = true;
					otherUpdate = true;
				}
				
				boolean updateRecordOff = false;
				if (args[0].equals("updateRecordOff")){
					updateRecordOff = true;
					otherUpdate = true;
				}

				if ((useFileloc == false) && (useNoPropFileloc == false) && (otherUpdate == false)) {
				
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					if (args.length == 1) {
						up.createOrUpdate(args[0], null, null, true);
					}
					else if (args.length == 2) {				
						up.createOrUpdate(args[0], args[1], null, true);
					}
					else if (args.length == 3) {
						up.createOrUpdate(args[0], args[1], args[2], true);
					}
					else if (args.length == 4) {
						up.createOrUpdate(args[0], args[1], args[2], !args[3].equals("dontUpdate"));
					}
				}
				else if (useFileloc == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					if (args.length == 2) {
						up.createOrUpdate(fileloc, args[1], null, null, true);
					}
					else if (args.length == 3) {				
						up.createOrUpdate(fileloc, args[1], args[2], null, true);
					}
					else if (args.length == 4) {				
						up.createOrUpdate(fileloc, args[1], args[2], args[3], true);
					}
					else if (args.length == 5) {				
						up.createOrUpdate(fileloc, args[1], args[2], args[3], !args[4].equals("dontUpdate"));
					}
				}
				else if (useNoPropFileloc == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					if (args.length == 3) {				
						up.createOrUpdateNoProp(nopropfileloc, args[1], args[2], null);
					}
					else if (args.length == 4) {				
						up.createOrUpdateNoProp(nopropfileloc, args[1], args[2], args[3]);
					}
				}
				else if (updateMcm == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.insertAutoAdjustMcmProperty();
				}			
				else if (updateDepth == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateXorgDepthTo24();
				}
				else if (updateComposite == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateComposite();
				}
				else if (updateMcm212 == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.insertMcmProperties_2_12();
				}
				else if (updateOpenVPNServer == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateOpenVPNServer();
				}
				else if (getServerIP == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.getServerIP();
				}
				else if (removeExtraRotationLine == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.removeExtraRotationLine();
				}
				else if (addExecToProperty == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.addExecToProperty(args[1]);
				}
				else if (updateNtpConf == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateNtpConf();
				}
				else if (updateGrubWithVesa == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateGrubWithVesa();
				}
				else if (updateGrubWithVGA == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateGrubWithVGA();
				}
				else if (updateMplayerCommand == true) {
					
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateMplayerCommand();
				}
				else if (updateMplayerCommandForGeForce4 == true) {
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateMplayerCommandForGeForce4();
				}
				else if (addDeviceCommunicationServerLogger == true) {
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.addDeviceCommunicationServerLogger();
				}
				else if (createFileStatusesForFilesInCache == true){
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.createFileStatusesForFilesInCache();
				}
				else if (updateMplayerCommandForSlaveMode == true){
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateMplayerCommandForSlaveMode();
				}
				else if (updateRecordOff == true){
					CreateOrUpdateProperty up = new CreateOrUpdateProperty();
					up.updateRecordOff();
				}
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
