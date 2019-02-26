package com.kuvata.kmf.alerts;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.AssetType;

import com.kuvata.dispatcher.scheduling.ScheduleInfo;
import com.kuvata.dispatcher.scheduling.SegmentBlock;
import com.kuvata.kmf.Alert;
import com.kuvata.kmf.AlertCampaign;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.billing.Campaign;
import com.kuvata.kmf.billing.CampaignAsset;
import com.kuvata.kmf.billing.VenuePartner;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

public class CampaignAlert extends AlertDefinition {
	
	public CampaignAlert(String schemaName, Long alertId, String alertType, String alertName, String[] args, Integer frequency)
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
		// Schedule this method to run every "frequency" number of minutes
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
		
		timer.schedule( this, scheduleTime.getTime() );
		logger.info("Scheduling campaign alert to run at "+ scheduleTime.getTime());
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
				
				// Get yesterday date params
				Calendar yesterdayStart = Calendar.getInstance();
				yesterdayStart.add( Calendar.DATE, -1 );
				yesterdayStart.set( Calendar.HOUR_OF_DAY, 0 );
				yesterdayStart.set( Calendar.MINUTE, 0 );
				yesterdayStart.set( Calendar.SECOND, 0 );
				yesterdayStart.set( Calendar.MILLISECOND, 0 );
				
				Calendar yesterdayEnd = Calendar.getInstance();
				yesterdayEnd.add( Calendar.DATE, -1 );
				yesterdayEnd.set( Calendar.HOUR_OF_DAY, 23 );
				yesterdayEnd.set( Calendar.MINUTE, 59 );
				yesterdayEnd.set( Calendar.SECOND, 59 );
				yesterdayEnd.set( Calendar.MILLISECOND, 999 );
				
				// Determine the campaigns we are running this alert for
				LinkedList<Campaign> campaigns = new LinkedList();
				ArrayList<Long> campaignIds = new ArrayList();
				List<Campaign> alertCampaigns = new LinkedList();
				if(alert.getAlertAllCampaigns() != null && alert.getAlertAllCampaigns()){
					alertCampaigns = Campaign.getCampaigns();
				}else{
					 for(AlertCampaign ac : alert.getAlertCampaigns()){
						 alertCampaigns.add(ac.getCampaign());
					 }
				}
				
				for(Campaign c : alertCampaigns){
					// If this campaign was suppose to be running yesterday according to it's start and end dates
					// and make sure that at least one of the two alert parameters are not null
					if(c.getStartDt() != null && (c.getStartDt().before(yesterdayStart.getTime()) || c.getStartDt().getTime() == yesterdayStart.getTimeInMillis())
							&& c.getEndDt() != null && (c.getEndDt().after(yesterdayEnd.getTime()) || c.getEndDt().getTime() == yesterdayEnd.getTimeInMillis() )
							&& (c.getNumDevices() != null || c.getFrequency() != null) ){
						campaigns.add(c);
						campaignIds.add(c.getCampaignId());
					}
				}
				
				if(campaigns.size() > 0){
					
					// Determine which table to generate the rows off of
					String reportsSourceTable = Constants.REPORTS_SOURCE_TABLE_DEFAULT;
					try{
						reportsSourceTable = KuvataConfig.getPropertyValue( Constants.REPORTS_SOURCE_TABLE );
					}catch(KmfException e){
						logger.info("Could not locate property: "+ Constants.REPORTS_SOURCE_TABLE +". Using default: "+ Constants.REPORTS_SOURCE_TABLE_DEFAULT);
					}
					
					/*
					 * Get all playback rows per Campaign per Device
					 */
					String hql = "SELECT pe.assetId, pe.deviceId, COUNT(*) as numAirings FROM " + reportsSourceTable + " pe "
						+ "WHERE pe.startDatetime >= :yesterdayStart "
						+ "AND pe.startDatetime < :yesterdayEnd "
						+ "AND pe.assetId IN "
						+ "(SELECT DISTINCT ca.asset.assetId FROM CampaignAsset ca WHERE ca.campaign.campaignId IN (:campaignIds) ) "
						+ "GROUP BY pe.assetId, pe.deviceId";
					
					if(reportsSourceTable.equals(Constants.REPORTS_SOURCE_TABLE_SUMMARY)){
						hql = hql.replace("COUNT(*) as numAirings", "SUM(numAirings) as numAirings");
					}
					
					Session session = HibernateSession.currentSession();
					Query q = session.createQuery( hql );
					q.setParameter("yesterdayStart", yesterdayStart.getTime() );
					q.setParameter("yesterdayEnd", yesterdayEnd.getTime() );
					q.setParameterList("campaignIds", campaignIds);
					List<Object[]> queryResult = q.list();
					
					// Map: <assetId, List<PlaybackInfo>>
					HashMap<Long, List<PlaybackInfo>> playbackInfosByAsset = new HashMap();
					
					// Map: <deviceId, noContentLength>
					HashMap<Long, Long> deviceNoContentMap = new HashMap();
					
					// For each returned row, populate playbackInfosByAsset
					for(Object[] o : queryResult){
						Long assetId = (Long)o[0];
						Long deviceId = (Long)o[1];
						
						// Make sure that we should be alerting on this device
						Device d = Device.getDevice(deviceId);
						if(d != null && d.getApplyAlerts() != null && d.getApplyAlerts()){
							PlaybackInfo pi = new PlaybackInfo();
							pi.setDeviceId(deviceId);
							pi.setNumAirings((Long)o[2]);
							List<PlaybackInfo> playbackInfos =  playbackInfosByAsset.containsKey(assetId) ? playbackInfosByAsset.get(assetId) : new ArrayList();
							playbackInfos.add(pi);
							playbackInfosByAsset.put(assetId, playbackInfos);
							deviceNoContentMap.put(deviceId, 0l);
						}
					}
					
					// Map: <CampaignId, <DeviceId, numAirings>>
					HashMap<Long, HashMap<Long, Long>> campaignMap = new HashMap();
					
					// For each campaign, populate campaignMap
					for(Campaign c : campaigns){
						// For each campaign asset
						for(CampaignAsset ca : c.getCampaignAssets()){
							HashMap<Long, Long> deviceMap = campaignMap.containsKey(c.getCampaignId()) ? campaignMap.get(c.getCampaignId()) : new HashMap();
							// If we have playback info for this asset
							if(playbackInfosByAsset.containsKey(ca.getAsset().getAssetId())){
								// For each playback info for this asset
								for(PlaybackInfo pi : playbackInfosByAsset.get(ca.getAsset().getAssetId())){
									Long numAirings = deviceMap.containsKey(pi.getDeviceId()) ? deviceMap.get(pi.getDeviceId()) : 0;
									numAirings += pi.getNumAirings();
									deviceMap.put(pi.deviceId, numAirings);
								}
							}
							
							// Populate the campaign map
							campaignMap.put(c.getCampaignId(), deviceMap);
						}
					}
					
					/*
					 * Calculate content length for each device based on content scheduled.
					 * This can be done by getting playback length of power off assets and
					 * empty segment blocks denoting no content using the graphing component.
					 */
					// Get the playback of power off assets
					hql = "SELECT pe.deviceId, pe.startDatetime, pe.endDatetime From PlaybackEvent as pe "
						+ "WHERE ((pe.startDatetime >= :yesterdayStart AND pe.endDatetime <= :yesterdayEnd) "
						+ "OR (pe.startDatetime < :yesterdayStart AND pe.endDatetime > :yesterdayStart AND pe.endDatetime <= :yesterdayEnd) "
						+ "OR (pe.startDatetime >= :yesterdayStart AND pe.startDatetime < :yesterdayEnd AND pe.endDatetime > :yesterdayEnd)) "
						+ "AND pe.assetId IN ( SELECT a.assetId from Asset a WHERE a.class = :powerOff )";
					
					String assetTypeClassName = AssetType.POWER_OFF.getPersistentValue().substring( AssetType.POWER_OFF.getPersistentValue().lastIndexOf(".") + 1 );
					List<Object[]> powerOffPlaybacks = session.createQuery(hql).setParameter("yesterdayStart", yesterdayStart.getTime() ).setParameter("yesterdayEnd", yesterdayEnd.getTime() ).setParameter("powerOff", assetTypeClassName).list();
					for(Object[] o: powerOffPlaybacks){
						Long deviceId = (Long)o[0];
						
						// Only for devices we are alerting on
						if(deviceNoContentMap.keySet().contains(deviceId)){
							Date start = (Date)o[1];
							Date end = (Date)o[2];
							long noContentLength = 0;
							
							// If the power off was completely in the time window
							if( (start.after(yesterdayStart.getTime()) || start.getTime() == yesterdayStart.getTimeInMillis()) && (end.before(yesterdayEnd.getTime()) || end.getTime() == yesterdayEnd.getTimeInMillis())){
								noContentLength = end.getTime() - start.getTime();
							}else if(start.before(yesterdayStart.getTime())){
								noContentLength = end.getTime() - yesterdayStart.getTimeInMillis();
							}else if(end.after(yesterdayEnd.getTime())){
								noContentLength = yesterdayEnd.getTimeInMillis() - start.getTime();
							}
							deviceNoContentMap.put(deviceId, deviceNoContentMap.get(deviceId) + noContentLength);
						}
					}
					
					for(Long deviceId : deviceNoContentMap.keySet()){
						Device d = Device.getDevice(deviceId);
						ScheduleInfo si = ScheduleInfo.getScheduleInfo( d, yesterdayStart.getTime(), yesterdayEnd.getTime(), false );
						long lastEnd = yesterdayStart.getTimeInMillis();
						long millisOfNoContent = 0;
						for(SegmentBlock sb : si.getSegmentBlocks()){
							// If this segment block is starting after yesterdayStart
							if(sb.getStartTime().getTime() > lastEnd){
								millisOfNoContent += sb.getStartTime().getTime() - lastEnd;
							}
							lastEnd = sb.getEndTime().getTime();
						}
						
						// Make sure that the last segment block had enough content
						if(lastEnd < yesterdayEnd.getTimeInMillis()){
							millisOfNoContent += yesterdayEnd.getTimeInMillis() - lastEnd;
						}
						
						// Update the map if needed
						if(millisOfNoContent > 0){
							deviceNoContentMap.put(deviceId, deviceNoContentMap.get(deviceId) + millisOfNoContent);
						}
					}
					
					/*
					 * End of calculating no content length
					 */
					
					LinkedList<CampaignInfo> failedCampaigns = new LinkedList();
					
					// For each campaign
					for(Campaign c : campaigns){
						HashMap<Long, Long> deviceMap = campaignMap.get(c.getCampaignId());
						
						float alertParameter = (Float.parseFloat(alert.getParameters()) / 100f);
						
						// Check for num devices criteria
						if(c.getNumDevices() != null){
							// If we didn't meet the device criteria
							if((float)deviceMap.keySet().size() < alertParameter * (float)c.getNumDevices()){
								CampaignInfo ci = new CampaignInfo();
								ci.setCampaign(c);
								ci.setNumDevices(deviceMap.keySet().size());
								failedCampaigns.add(ci);
							}
						}
						
						// Check for frequency criteria
						if(c.getFrequency() != null){
							// If we didn't meet the frequency criteria per device basis
							for(Long deviceId : deviceMap.keySet()){
								Long numAirings = deviceMap.get(deviceId);
								Long noContentMillis = deviceNoContentMap.get(deviceId);
								Float hoursOfContent = ((float)Constants.MILLISECONDS_IN_A_DAY - (float)noContentMillis) / 3600000f;
								Float airingsToMeet = hoursOfContent * (float)c.getFrequency() * alertParameter;
								if(numAirings < airingsToMeet){
									Device d = Device.getDevice(deviceId);
									CampaignInfo ci = new CampaignInfo();
									ci.setCampaign(c);
									ci.setDevice(d);
									ci.setNumAirings(numAirings);
									ci.setHoursOfContent(hoursOfContent);
									failedCampaigns.add(ci);
								}
							}
						}
						
						// Check impressions
						if(c.getIsBillable() != null && c.getIsBillable() && c.getDailyMinImpressions() != null){
							// Calculate total impressions
							float totalImpressions = 0;
							for(Long deviceId : deviceMap.keySet()){
								Long numAirings = deviceMap.get(deviceId);
								
								Device d = Device.getDevice(deviceId);
								VenuePartner vp = d.getVenuePartner();
								if(vp.getOpenHours() != null && vp.getVisitors() != null && vp.getDwellTime() != null){
									
									// Calculate impressions
									float impressions = numAirings * vp.getVisitors() * vp.getDwellTime() / vp.getOpenHours();
									if(vp.getImpressionDiscount() != null){
										impressions = impressions * ((100f - vp.getImpressionDiscount()) / 100f);
									}
									
									totalImpressions += impressions;
								}
							}
							
							if(totalImpressions < c.getDailyMinImpressions()){
								CampaignInfo ci = new CampaignInfo();
								ci.setCampaign(c);
								ci.setImpressions(totalImpressions);
								failedCampaigns.add(ci);
							}
						}
					}
					
					// Get the list of email address to send for this alert
					String emailAddresses = null;
					emailAddresses = alert.buildEmailAddresses();
					
					// If we found any email addresses for this alert
					if( emailAddresses != null && emailAddresses.length() > 0 ) {
						this.sendAlert( alert, failedCampaigns, emailAddresses);
					}					
				}	
			}
	
		} catch(Exception e) {
			logger.error(e);
		} finally {
			HibernateSession.closeSession();
		}
	}
	
	/**
	 * Builds the message and calls the parent method to send it
	 * 
	 * @param devices
	 * @param emailAddresses
	 */
	private void sendAlert(Alert alert, Collection<CampaignInfo> failedCampaigns, String emailAddresses)
	{
		ArrayList<String> colHeaders = new ArrayList<String>();
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
		
		String subject = alert.getAlertName() +" - Campaign Alert";
		
		/*
		 * 1. Determine if each entry is valid (based on the onlyAlertOnChange property)
		 * 2. Build the row data and lastValues collections
		 */
		HashMap<Long, Object[]> currentResults = new HashMap<Long, Object[]>();
		LinkedList<CampaignInfo> validCampaignInfos = new LinkedList<CampaignInfo>();
		for( CampaignInfo ci : failedCampaigns ) 
		{
			Long lastResultId = ci.getNumDevices() != null ? ci.getCampaign().getCampaignId() : ci.getImpressions() != null ? -ci.getCampaign().getCampaignId() : ci.getDevice().getDeviceId();
			boolean valueHasChanged = valueHasChanged( alert, lastResultId, null );
			
			// If this alert is supposed to alert even if the value hasn't changed -- add this entry to the list of valid entries
			if( alert.getOnlyAlertOnChange() == null || alert.getOnlyAlertOnChange().booleanValue() == false ){
				validCampaignInfos.add(ci);
			}
			// If this alert is supposed to alert only if the value has changed, AND the value has changed -- add this entry to the list of valid entries
			else if( alert.getOnlyAlertOnChange() != null && alert.getOnlyAlertOnChange().booleanValue() == true && valueHasChanged ){ 
				validCampaignInfos.add(ci);
			}
			
			DecimalFormat df = new DecimalFormat("###,###.##");
			String row;
			// If we are alerting on num of devices
			if(ci.getNumDevices() != null){
				row = ci.getCampaign().getCampaignName() +  " only aired on " + df.format(ci.getNumDevices()) + " Devices and should have aired on " + df.format(ci.getCampaign().getNumDevices()) + " Devices.";
			}
			// If we are alerting on impressions
			else if(ci.getImpressions() != null){
				row = ci.getCampaign().getCampaignName() +  " only had " + df.format(ci.getImpressions()) + " audience impressions and should have had a minimum of " + df.format(ci.getCampaign().getDailyMinImpressions());
			}
			// If we alerting on a device's frequency
			else{
				row = ci.getCampaign().getCampaignName() +  " only aired " + df.format(ci.getNumAirings()) + " times on " + ci.getDevice().getDeviceName() + " (" + ci.getDevice().getMacAddr() + ") and should have aired " + df.format(ci.getHoursOfContent() * ci.getCampaign().getFrequency()) + " times (" + df.format(ci.getCampaign().getFrequency()) + " per hour at " + df.format(ci.getHoursOfContent()) + " hours).";
			}
			
			// If the value has changed from the previous alert
			if( valueHasChanged )
			{
				// Add the data for this row to the "new" section
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add( row );
				newData.add( rowData );
			}
				
			// Add the data for this row to the "all" section
			ArrayList<String> rowData = new ArrayList<String>();
			rowData.add( row );
			allData.add( rowData );
			currentResults.put( lastResultId, new Object[]{lastResultId.toString(), rowData} );
		}				

		// Get the information of any rows that may have been removed from the last time this alert was run
		ArrayList<ArrayList<String>> removedData = this.getRemovedRows( alert, currentResults);
		
		// If we found any valid entries or any entries that were removed
		boolean onlyAlertOnChange = (alert.getOnlyAlertOnChange() == null) ? false : alert.getOnlyAlertOnChange().booleanValue();
		if( (onlyAlertOnChange == false && (validCampaignInfos.size() > 0 || removedData.size() > 0 )) ||
			(onlyAlertOnChange == true && (newData.size()>0 || removedData.size() > 0)) )
		{			
			Date yesterday = new Date(new Date().getTime() - Constants.MILLISECONDS_IN_A_DAY);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			// Build the contents of the email
			StringBuffer msg = new StringBuffer();
			msg.append("For the period of " + sdf.format(yesterday) + " 12:00 AM to 11:59 PM the following cases did not meet the Campaign requirements at least " + alert.getParameters() + "% of the time:\n\n");
			msg.append("New ("+ newData.size() +"):\n\n");
			msg.append( Emailer.buildEmailSection( colHeaders, newData ) );	
			msg.append("Removed ("+ removedData.size() +"):\n\n");
			msg.append( Emailer.buildEmailSection( colHeaders, removedData ) );			
			msg.append("All ("+ allData.size() +"):\n\n");
			msg.append( Emailer.buildEmailSection( colHeaders, allData ) );					
			
			// Send off the alert
			this.sendAlert( subject, msg.toString(), emailAddresses );				
		}
		
		// Serialize to XML the results of this alert and save them in the database  
		this.serializeLastResults( alert, currentResults );					
	}
	
	protected ArrayList<ArrayList<String>> getRemovedRows(Alert alert, HashMap<Long, Object[]> currentResults)
	{	
		ArrayList<ArrayList<String>> removedData = new ArrayList<ArrayList<String>>();

		// If there are any previously saved results for this alert
		String lastResultsXml = this.getLastResults(alert);
		if( lastResultsXml == null || lastResultsXml.length() == 0 ) return removedData;
			
		try 
		{
			// Attempt to locate an element associated with the given id
			Document doc = new Document( lastResultsXml );
			Elements es = doc.getElements( new XPath("//"+ LAST_RESULT) );
			while( es.hasMoreElements() )
			{
				Element e = es.next();
				Long lastId = Long.valueOf( e.getAttribute( ID_ATTRIBUTE ) );

				// Put the saved data for this id into an ArrayList
				ArrayList<String> rowData = new ArrayList<String>();
				String lastRow = e.getAttribute( LAST_ROW_ATTRIBUTE );
				if( lastRow != null )
				{
					// If this row contained a delimited list of values -- parse and add each one
					if( lastRow.indexOf( Constants.UNIQUE_DELIMITER ) >=0 ){
						String[] lastRowParts = lastRow.split("\\"+ Constants.UNIQUE_DELIMITER );
						for( int i=0; i<lastRowParts.length; i++ ){
							rowData.add( lastRowParts[i] );
						}
					}
					// Otherwise, add the entire row
					else{
						rowData.add( lastRow );
					}
				}							

				// If this id is not in the collection of current results and there is either no associated device
				// OR the device is not in the alert's window, then remove the row
				if( currentResults.containsKey( lastId ) == false )
				{
					Device d = Device.getDevice(lastId);
					if(d == null || (d != null && this.addDevice(d, alert) == true) ){
						removedData.add( rowData );
					}
				}
			}
		} catch (ParseException e) {
			logger.error( e );
		}	
		
		return removedData;
	}	
	
	public class PlaybackInfo{
		private Long deviceId;
		private Long numAirings;
		public Long getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(Long deviceId) {
			this.deviceId = deviceId;
		}
		public Long getNumAirings() {
			return numAirings;
		}
		public void setNumAirings(Long numAirings) {
			this.numAirings = numAirings;
		}
	}
	
	public class CampaignInfo{
		private Campaign campaign;
		private Integer numDevices;
		private Device device;
		private Long numAirings;
		private Float hoursOfContent;
		private Float impressions;

		public Integer getNumDevices() {
			return numDevices;
		}
		public void setNumDevices(Integer numDevices) {
			this.numDevices = numDevices;
		}
		public Long getNumAirings() {
			return numAirings;
		}
		public void setNumAirings(Long numAirings) {
			this.numAirings = numAirings;
		}
		public Campaign getCampaign() {
			return campaign;
		}
		public void setCampaign(Campaign campaign) {
			this.campaign = campaign;
		}
		public Float getHoursOfContent() {
			return hoursOfContent;
		}
		public void setHoursOfContent(Float hoursOfContent) {
			this.hoursOfContent = hoursOfContent;
		}
		public Device getDevice() {
			return device;
		}
		public void setDevice(Device device) {
			this.device = device;
		}
		public Float getImpressions() {
			return impressions;
		}
		public void setImpressions(Float impressions) {
			this.impressions = impressions;
		}
	}
}
