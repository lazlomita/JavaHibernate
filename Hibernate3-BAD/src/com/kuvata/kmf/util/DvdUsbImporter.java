package com.kuvata.kmf.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import parkmedia.DispatcherConstants;
import parkmedia.KuvataConfig;
import parkmedia.device.entities.FileStatus;
import parkmedia.device.entities.Status;
import parkmedia.device.presenter.ContentScheduleActivator;
import parkmedia.device.presenter.ContentScheduleFileInfo;
import parkmedia.device.presenter.ContentScheduleSelectionHandler;
import parkmedia.device.presenter.PresenterControllerQt;
import parkmedia.usertype.FileStatusStatusType;

import com.kuvata.dispatcher.PresentationsExistContentScheduleHandler;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.presenters.LayoutTransitionPresenter;

public class DvdUsbImporter {
	
	private static SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();
	private static Set<String> presentations = new HashSet<String>();
	private static File currentDir = null;
	private static File schedulesDir = null;
	private static File presentationsDir = null;

	public static void main(String[] args) {
		if(args.length != 1){
			System.out.println("Usage: DvdUsbImported dirPath");
		}else{
			try {
				currentDir = new File(args[0]);
				if(currentDir.isDirectory()){
					schedulesDir = new File(currentDir.getAbsolutePath() + "/" + Constants.SCHEDULES);
					presentationsDir = new File(currentDir.getAbsolutePath() + "/" + Constants.PRESENTATIONS);
					if(schedulesDir.isDirectory() && presentationsDir.isDirectory()){
						
						SchemaDirectory.initialize("kuvata", "DvdUsbImporter", null, false, true);
						
						// Connect to Qt
						PresenterControllerQt qt = new PresenterControllerQt();
						qt.performAction("DvdUsbUpdate", "id-1", "start", null, null, "0", "0", LayoutTransitionPresenter.resolutionWidth, LayoutTransitionPresenter.resolutionHeight, null, null, null, null, null, null, null);
						
						Status status = Status.getStatus();
						File[] schedules = schedulesDir.listFiles();
						Map<File, ContentScheduleFileInfo> validSchedules = new HashMap<File, ContentScheduleFileInfo>();
						for(int i=0; i<schedules.length; i++){
							// Ignore hidden files
							if(schedules[i].isFile() && schedules[i].getName().startsWith(".") == false){
								
								ContentScheduleFileInfo csFileInfo = new ContentScheduleFileInfo();
								try {
									DefaultHandler handler = new ContentScheduleSelectionHandler(csFileInfo);
									SAXParser saxParser = SAX_FACTORY.newSAXParser();
									saxParser.parse(schedules[i], handler);
								} catch (ParserConfigurationException e) {
									e.printStackTrace();
								} catch (SAXException e) {
									// Do not print trace since this is the only way to exit the SAX parser
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								String csName = schedules[i].getName();
								Long csDeviceId = Long.parseLong(csName.substring(0, csName.indexOf("-")));
								boolean isValidCs = csDeviceId.equals(status.getDeviceId());
								
								// If this CS belongs to a mirror source, verify the CS header
								if(isValidCs == false && csDeviceId.equals(status.getMasterDeviceId()) && csFileInfo.getMirrorPlayers() != null){
									List<String> mirrorPlayers = Arrays.asList(csFileInfo.getMirrorPlayers().split(","));
									if(mirrorPlayers.contains(status.getDeviceId().toString())){
										isValidCs = true;
									}
								}
								
								// If this CS is meant for this device
								if(isValidCs){
									validSchedules.put(schedules[i], csFileInfo);
								}
							}
						}
						
						int currentStep = 1;
						int totalSteps = (validSchedules.size() * 2) + 1;
						for(Entry<File, ContentScheduleFileInfo> entry : validSchedules.entrySet()){
							File schedule = entry.getKey();
							ContentScheduleFileInfo csFileInfo = entry.getValue();
							
							// Cancel CS files that can be canceled due to this CS
							String message = "Step " + currentStep++ + " of " + totalSteps + "\n";
							qt.performAction("DvdUsbUpdate", "id-1", message, null, null, "0", "0", LayoutTransitionPresenter.resolutionWidth, LayoutTransitionPresenter.resolutionHeight, null, null, null, null, null, null, null);
							ContentScheduleActivator.scanAndCancelStagedSchedules(schedule.getAbsolutePath(), true);
							
							message = "Step " + currentStep++ + " of " + totalSteps + "\n";
							qt.performAction("DvdUsbUpdate", "id-1", message, null, null, "0", "0", LayoutTransitionPresenter.resolutionWidth, LayoutTransitionPresenter.resolutionHeight, null, null, null, null, null, null, null);
							
							try {
								System.out.println("About to parse schedule file: " + schedule.getName());
								
								DefaultHandler handler;
								if(csFileInfo.containsOption("deviceSideScheduling")){
									handler = new parkmedia.device.scheduler.PresentationsExistContentScheduleHandler(null, null, true, csFileInfo.getEndDatetime());
									SAXParser saxParser = SAX_FACTORY.newSAXParser();
									saxParser.parse(schedule, handler);
								}else{
									handler = new PresentationsExistContentScheduleHandler(null, null, true);
									SAXParser saxParser = SAX_FACTORY.newSAXParser();
									saxParser.parse(schedule, handler);
								}
								
								// If we made it this far, the content schedule must be ready
								if (handler != null) {
									System.out.println("Activating the CS: " + schedule.getName());
									ContentScheduleActivator.moveAndActivate(schedule, csFileInfo, true);
								}
							} catch (Exception e){
								e.printStackTrace();
							} catch (Error e){
								e.printStackTrace();
							}
						}
						
						// Execute onPresentationDownloadComplete for all copied presentations
						String message = "Step " + currentStep++ + " of " + totalSteps + "\n";
						qt.performAction("DvdUsbUpdate", "id-1", message, null, null, "0", "0", LayoutTransitionPresenter.resolutionWidth, LayoutTransitionPresenter.resolutionHeight, null, null, null, null, null, null, null);
						
						for(String relativePath : presentations){
							System.out.println("Executing onPresentationDownloadComplete for " + relativePath);
							ContentScheduleActivator.onPresentationDownloadComplete(relativePath);
						}
						
						qt.performAction("DvdUsbUpdate", "id-1", "end", null, null, "0", "0", LayoutTransitionPresenter.resolutionWidth, LayoutTransitionPresenter.resolutionHeight, null, null, null, null, null, null, null);
					}
				}else{
					System.out.println("Could not locate valid directories under " + args[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void handlePresentation(String relativePath) throws Exception{
		// Parse out the assetId
		int beginIndex = relativePath.lastIndexOf("/") + 1;
		String assetId = relativePath.substring(beginIndex, relativePath.indexOf("-", beginIndex));
		
		System.out.println("Handling presentation: " + relativePath);
		
		// Copy all presentation files
		for(File dir : presentationsDir.listFiles()){
			// Locate appropriate presentations directory
			if(dir.isDirectory() && relativePath.startsWith(dir.getName() + "/")){
				// Locate appropriate presentation files
				for(File f : dir.listFiles()){
					if(f.isFile() && f.getName().startsWith(assetId + "-")){
						relativePath = f.getAbsolutePath().substring(f.getAbsolutePath().indexOf(DispatcherConstants.PRESENTATIONS_DIRECTORY));
						File pf = new File(KuvataConfig.getKuvataHome() + "/" + relativePath);
						if(pf.exists() == false){
							System.out.println("Copying presentation: " + relativePath);
							Files.copyFile(currentDir.getAbsolutePath() + "/" + relativePath, KuvataConfig.getKuvataHome() + "/" + relativePath);
							FileStatus.createOrUpdate( relativePath, FileStatusStatusType.EXISTS, new Date(), null, null, false, false );
							presentations.add(relativePath);
						}else{
							System.out.println("Presentation already exists: " + relativePath);
						}
					}
				}
			}
		}
	}
}
