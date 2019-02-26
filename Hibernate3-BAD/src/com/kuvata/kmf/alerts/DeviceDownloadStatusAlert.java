package com.kuvata.kmf.alerts;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import parkmedia.DispatcherConstants;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.FileTransmissionStatus;

import com.kuvata.kmf.Alert;
import com.kuvata.kmf.AlertDevice;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.ContentSchedule;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.FileTransmission;
import com.kuvata.kmf.GrpMember;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class DeviceDownloadStatusAlert extends AlertDefinition {

	private static final int HEARTBEAT = 1;
	private static final int NO_HEARTBEAT = 2;
	private static final int NEVER_HEARTBEAT = 3;
	
	public DeviceDownloadStatusAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
	{
		// We don't need the frequency since this alert runs once and only once per day
		super( schemaName, alertId, alertType, alertName, args, null );
	}
	
	/**
	 * Implements the parent's abstract method.
	 * Schedules this timer task
	 */
	public void schedule()
	{
		Alert alert = Alert.getAlert( this.alertId );
		
		Calendar now = Calendar.getInstance();
		Calendar scheduleTime = Calendar.getInstance();
		scheduleTime.setTime(alert.getActiveStartTime());
		scheduleTime.set( Calendar.YEAR, now.get(Calendar.YEAR) );
		scheduleTime.set( Calendar.MONTH, now.get(Calendar.MONTH) );
		scheduleTime.set( Calendar.DATE, now.get(Calendar.DATE) );
		
		// Schedule it for the next day if the schedule time is before now
		if(scheduleTime.getTime().before(now.getTime())){
			scheduleTime.add(Calendar.DATE, 1);
		}
		
		timer.schedule(this, scheduleTime.getTime());
		logger.info("Scheduling device download status alert to run at "+ scheduleTime.getTime());
	}
	
	/**
	 * If a Device is not playing the latest content,
	 * email the appropriate users.
	 * 
	 * @param threshold
	 */
	public void runAlert()
	{
		SchemaDirectory.setup(this.schemaName, this.getClass().getName());
		try
		{
			HibernateSession.clearCache();
			Alert alert = Alert.getAlert( this.alertId );
			if( alert != null )
			{
				// Set the last run date of the alert
				alert.setLastRunDt(new Date());
				alert.update();
				
				// Get a list of devices
				List<Device> devices = new LinkedList<Device>();
				if(alert.alertSpecificDevices()){
					for(AlertDevice ad : alert.getAlertDevices()){
						if(ad.getDevice() != null){
							devices.add(ad.getDevice());
						}else if(ad.getDeviceGrp() != null){
							for(GrpMember gm : ad.getDeviceGrp().getGrpMembers()){
								devices.add((Device)gm.getChild());
							}
						}
					}
				}else{
					devices = Device.getDevices();
				}
				
				// Result
				LinkedList<DownloadInfo> downloadInfos = new LinkedList<DownloadInfo>();
				TreeMap<Integer, TreeMap<Boolean, List<Device>>> contentRemainingDevices = new TreeMap<Integer, TreeMap<Boolean, List<Device>>>();
				
				// 15 mins ago
				long timeout = System.currentTimeMillis() - 15*60*1000;
				
				// Get heartbeat status
				for(Device d : devices){
					// Only alert on appropriate devices
					if(d.getApplyAlerts() != null && d.getApplyAlerts()){
						// Determine last CS and heartbeat status
						String status;
						boolean playingCurrentCs = false;
						String currentCs = d.getMirrorSource() != null ? ContentSchedule.getScheduledContentScheduleFileName(d.getMirrorSource()) : ContentSchedule.getScheduledContentScheduleFileName(d);
						
						int heartbeatStatus = 0;
						if(d.getLastHeartbeatEvent() != null){
							if(d.getLastHeartbeatEvent().getDt().getTime() >= timeout){
								status = "Heartbeating";
								heartbeatStatus = HEARTBEAT;
							}else{
								status = "No Heartbeat";
								heartbeatStatus = NO_HEARTBEAT;
							}
							
							if(d.getLastHeartbeatEvent().getLastContentSchedule() != null){
								playingCurrentCs = currentCs != null ? currentCs.equals(d.getLastHeartbeatEvent().getLastContentSchedule()) : false;
							}
						}else{
							status = "Never Heartbeat";
							heartbeatStatus = NEVER_HEARTBEAT;
						}
						
						// Don't include non-heartbeating devices if this alert is suppose to alert only on heartbeating devices
						if(alert.getShowHeartbeatingDevices() != null && alert.getShowHeartbeatingDevices() && heartbeatStatus != HEARTBEAT){
							continue;
						}
						
						// Prepare result
						DownloadInfo di = new DownloadInfo();
						di.setDeviceName(d.getDeviceName());
						di.setStatus(status);
						di.setPlayingLatestContent(playingCurrentCs);
						downloadInfos.add(di);
						
						if(alert.getShowContentRemaining() != null && alert.getShowContentRemaining()){
							TreeMap<Boolean, List<Device>> subMap = contentRemainingDevices.get(heartbeatStatus) != null ? contentRemainingDevices.get(heartbeatStatus) : new TreeMap<Boolean, List<Device>>();
							List<Device> subList = subMap.get(Boolean.valueOf(playingCurrentCs)) != null ? subMap.get(Boolean.valueOf(playingCurrentCs)) : new LinkedList<Device>();
							subList.add(d);
							subMap.put(Boolean.valueOf(playingCurrentCs), subList);
							contentRemainingDevices.put(heartbeatStatus, subMap);
						}
					}
				}
				
				// Get the list of email address to send for this alert 
				String emailAddresses = alert.buildEmailAddresses();
					
				// If we found any email addresses for this alert
				if( emailAddresses != null && emailAddresses.length() > 0 ) {
					
					// Sort all collections
					Collections.sort( downloadInfos, new Comparator<DownloadInfo>(){
						public int compare(DownloadInfo di1, DownloadInfo di2){
							return di1.deviceName.compareToIgnoreCase(di2.deviceName);
						}
					});
					
					for(Entry<Integer, TreeMap<Boolean, List<Device>>> e: contentRemainingDevices.entrySet()){
						TreeMap<Boolean, List<Device>> map = e.getValue();
						for(Entry<Boolean, List<Device>> subE : map.entrySet()){
							List list = subE.getValue();
							Collections.sort( list, new Comparator<Device>(){
								public int compare(Device d1, Device d2){
									return d1.getDeviceName().compareToIgnoreCase(d2.getDeviceName());
								}
							} );
						}
					}
					
					SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
					StringBuilder sb = new StringBuilder();
					sb.append("<html><head><style type=\"text/css\">" +
							".txtTopLabel {font-weight: bold;text-align: left;border-top: 1px solid #000000;border-right: 1px solid #000000;border-bottom: 1px solid #000000;border-left: 1px solid #000000;" +
							"border-left: 1px solid #000000;color: #FFFFFF;background-color: #5A7A36;font-family:\"Verdana\"}" +
							".datagrid{background-color:#FFFFFF;}" +
							".dgHeader {text-align: center;border-top:1px solid #000000;border-left:1px solid #000000;border-right:1px solid #000000;border-bottom:1px solid #000000;" +
							"font-family:\"Verdana\";color: #FFFFFF;background-color: #666666;background-repeat:repeat-x;background-position:bottom;}" +
							".txtHeader {font-weight: bold;font-family:\"Verdana\";}" +
							".dgItem0{font-size:12px;background-color:#F0F0F0;font-family:\"Verdana\";}" +
							".dgItem1{font-size:12px;font-family:\"Verdana\";}" +
							"</style></head>" +
							"<body>" +
								"<table width=\"100%\">" +
									"<tr valign=\"top\"><td height=\"10px\">" +
										"<table border=\"0\" width=\"100%\" cellpadding=\"5\" cellspacing=\"0\">" +
											"<tr><td width=\"100%\" class=\"txtTopLabel\">Device Download Status Report</td></tr>" +
										"</table>" +
									"</td></tr>" +
									"<tr><td class=\"txtHeader\"><br/>" + sdf.format(new Date()) + "</td></tr>" +
									"<tr><td class=\"txtHeader\">Alert : <font style=\"font-style:italic;\">" + alert.getAlertName() + "</font></td></tr>" +
									"<tr><td><br/>" +
										"<table border=\"0\" class=\"datagrid\">" +
											"<tr><td class=\"dgHeader\">Device Name</td><td class=\"dgHeader\">Status</td><td colspan=\"2\" class=\"dgHeader\">Playing latest content?</td></tr>");
					
					int row = 0;
					for(DownloadInfo di : downloadInfos){
						String rowClass = row % 2 == 0 ? "dgItem0" : "dgItem1";
						String contentStatusHtml = di.playingLatestContent ? "<td style=\"text-align:center;\">Yes</td><td></td>" : "<td></td><td style=\"text-align:center;\">No</td>";
						sb.append("<tr class=\"" + rowClass + "\"><td>" + di.deviceName + "</td><td style=\"text-align:center;\">" + di.status + "</td>" + contentStatusHtml + "</tr>");
						row++;
					}
					
					sb.append("</table></td></tr>");
					
					// Add the content remaining section
					if(contentRemainingDevices.size() > 0){
						
						DecimalFormat df = new DecimalFormat("###,###");
						
						sb.append("<tr><td class=\"txtHeader\"><br/>Content Remaining :</td></tr>");
						sb.append("<tr><td><table>");
						
						for(Integer heartbeatStatus : contentRemainingDevices.keySet()){
							String heartbeatLabel = heartbeatStatus == HEARTBEAT ? "Heartbeating" : heartbeatStatus == NO_HEARTBEAT ? "No Heartbeat" : "Never Heartbeat";
							Map<Boolean, List<Device>> subMap = contentRemainingDevices.get(heartbeatStatus);
							for(Boolean playlingCurrentCS : subMap.keySet()){
								List<Device> subList = subMap.get(playlingCurrentCS);
								for(Device d : subList){
									
									List<FileTransmission> fileTransmissions;
									if(playlingCurrentCS == false){
										fileTransmissions = FileTransmission.getFileTransmissionsToDownload(d);
									}else{
										fileTransmissions = FileTransmission.getFutureFileTransmissionsToDownload(d);
									}
									
									long totalBytesForCurrentAssets = 0;
									long totalBytesForFutureAssets = 0;
									boolean addedDeviceHeader = false;
									boolean addedCurrentAssetsRemainingHeader = false;
									boolean addedCurrentAssetsTotal = false;
									if(fileTransmissions.size() > 0){
										if(playlingCurrentCS == false){
											if(addedDeviceHeader == false){
												addedDeviceHeader = true;
												sb.append("<tr class=\"dgItem1\" style=\"font-weight:bold;\"><td colspan=\"4\"><br/>" + d.getDeviceName() + " - " + heartbeatLabel + "</td></tr>");
											}
											
											addedCurrentAssetsRemainingHeader = true;
											sb.append("<tr><td style=\"width:20px;\">&nbsp;</td><td class=\"dgHeader\" nowrap>Current Assets Remaining</td><td class=\"dgHeader\" nowrap>KB Remaining</td><td width=\"100%\">&nbsp;</td></tr>");
											
											row = 0;
											for(FileTransmission ft : fileTransmissions){
												if(ft.getStatus().equals(FileTransmissionStatus.NEEDED_FOR_FUTURE) == false){
													// Get the asset id of all presentation files ignoring xml files
													if(ft.getFilename().startsWith(DispatcherConstants.PRESENTATIONS_DIRECTORY) && ft.getFilename().endsWith(".xml") == false){
														int beginIndex = ft.getFilename().lastIndexOf("/") + 1;
														String assetId = ft.getFilename().substring(beginIndex, ft.getFilename().indexOf("-", beginIndex));
														
														Asset a = Asset.getAsset(Long.parseLong(assetId));
														if(a != null){
															String tdClass = row % 2 == 0 ? "dgItem0" : "dgItem1";
															sb.append("<tr><td style=\"width:20px;\">&nbsp;</td><td class=\"" + tdClass + "\" nowrap>" + a.getAssetName() + "</td><td class=\"" + tdClass + "\" align=\"right\" nowrap>" + df.format(ft.getFilesize() / 1024) + "</td><td width=\"100%\">&nbsp;</td></tr>");
															totalBytesForCurrentAssets += ft.getFilesize();
															row++;
														}
													}
												}
											}
											
											addedCurrentAssetsTotal = true;
											sb.append("<tr class=\"dgItem1\" style=\"font-weight:bold;\"><td style=\"width:20px;\">&nbsp;</td><td>Total</td><td align=\"right\">" + df.format(totalBytesForCurrentAssets / 1024) + "</td><td width=\"100%\">&nbsp;</td></tr>");
										}
										
										if(totalBytesForCurrentAssets == 0 && playlingCurrentCS == false){
											if(addedDeviceHeader == false){
												addedDeviceHeader = true;
												sb.append("<tr class=\"dgItem1\" style=\"font-weight:bold;\"><td colspan=\"4\"><br/>" + d.getDeviceName() + " - " + heartbeatLabel + "</td></tr>");
											}
											if(addedCurrentAssetsRemainingHeader == false){
												addedCurrentAssetsRemainingHeader = true;
												sb.append("<tr><td style=\"width:20px;\">&nbsp;</td><td class=\"dgHeader\" nowrap>Current Assets Remaining</td><td class=\"dgHeader\" nowrap>KB Remaining</td><td width=\"100%\">&nbsp;</td></tr>");
											}
											if(addedCurrentAssetsTotal == false){
												sb.append("<tr class=\"dgItem1\" style=\"font-weight:bold;\"><td style=\"width:20px;\">&nbsp;</td><td>Total</td><td align=\"right\">" + df.format(totalBytesForCurrentAssets / 1024) + "</td><td width=\"100%\">&nbsp;</td></tr>");
											}
											sb.append("<tr class=\"dgItem1\"><td style=\"width:20px;\">&nbsp;</td><td colspan=\"3\">*Note that all Assets have been downloaded but Content Schedule is not updated.</td></tr>");
										}
										
										// Get a list of future file transmissions (if any)
										List<FileTransmission> futureFileTransmissions = new ArrayList<FileTransmission>();
										for(FileTransmission ft : fileTransmissions){
											if(ft.getStatus().equals(FileTransmissionStatus.NEEDED_FOR_FUTURE)){
												futureFileTransmissions.add(ft);
											}
										}
										
										if(alert.getIncludeFutureContent() != null && alert.getIncludeFutureContent() && futureFileTransmissions.size() > 0){
											if(addedDeviceHeader == false){
												addedDeviceHeader = true;
												sb.append("<tr class=\"dgItem1\" style=\"font-weight:bold;\"><td colspan=\"4\"><br/>" + d.getDeviceName() + " - " + heartbeatLabel + "</td></tr>");
											}
											
											sb.append("<tr><td style=\"width:20px;\">&nbsp;</td><td class=\"dgHeader\" nowrap>Future Assets Remaining</td><td class=\"dgHeader\" nowrap>KB Remaining</td><td width=\"100%\">&nbsp;</td></tr>");
											
											row = 0;
											for(FileTransmission ft : futureFileTransmissions){
												if(ft.getStatus().equals(FileTransmissionStatus.NEEDED_FOR_FUTURE)){
													// Get the asset id of all presentation files ignoring xml files
													if(ft.getFilename().startsWith(DispatcherConstants.PRESENTATIONS_DIRECTORY) && ft.getFilename().endsWith(".xml") == false){
														int beginIndex = ft.getFilename().lastIndexOf("/") + 1;
														String assetId = ft.getFilename().substring(beginIndex, ft.getFilename().indexOf("-", beginIndex));
														
														Asset a = Asset.getAsset(Long.parseLong(assetId));
														if(a != null){
															String tdClass = row % 2 == 0 ? "dgItem0" : "dgItem1";
															sb.append("<tr><td style=\"width:20px;\">&nbsp;</td><td class=\"" + tdClass + "\" nowrap>" + a.getAssetName() + "</td><td class=\"" + tdClass + "\" align=\"right\" nowrap>" + df.format(ft.getFilesize() / 1024) + "</td><td width=\"100%\">&nbsp;</td></tr>");
															totalBytesForFutureAssets += ft.getFilesize();
															row++;
														}
													}
												}
											}
											
											sb.append("<tr class=\"dgItem1\" style=\"font-weight:bold;\"><td style=\"width:20px;\">&nbsp;</td><td>Total</td><td align=\"right\">" + df.format(totalBytesForFutureAssets / 1024) + "</td><td width=\"100%\">&nbsp;</td></tr>");
										}
									}
								}
							}
						}
						sb.append("</table></td></tr>");
					}
					
					sb.append("</table></body></html>");
					
					// Send the alert
					try {
						logger.info("Sending alert to: "+ emailAddresses);
						String subject = alert.getAlertName() +" - Device Download Status Alert";
						String fromAddress = KuvataConfig.getPropertyValue("Alert.fromAddress");
						String mailServer = KuvataConfig.getPropertyValue("Alert.mailServer");			
						Emailer.sendMessage(subject, sb.toString(), emailAddresses, fromAddress, mailServer, true);
					} catch (KmfException e) {
						logger.error("Could not locate property. Unable to send email.", e);
					}
				}
			}
	
		} catch(Exception e) {
			logger.error(e);
		} finally {
			HibernateSession.closeSession();
		}
	}
	
	public class DownloadInfo{
		private String deviceName;
		private String status;
		private boolean playingLatestContent;
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public boolean isPlayingLatestContent() {
			return playingLatestContent;
		}
		public void setPlayingLatestContent(boolean playingLatestContent) {
			this.playingLatestContent = playingLatestContent;
		}
	}
}
