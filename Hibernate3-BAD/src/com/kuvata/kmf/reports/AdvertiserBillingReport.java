package com.kuvata.kmf.reports;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import parkmedia.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.billing.Campaign;
import com.kuvata.kmf.billing.VenuePartner;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.util.Reformat;

public class AdvertiserBillingReport extends Report{
	
	private static final String ALL_ADVERTISERS = "All Advertisers";
	private Set<Long> uniqueCampaignIds = new HashSet<Long>();
	private Set<Long> uniqueVenueIds = new HashSet<Long>();
	private HashMap<Long, Campaign> uniqueCampaigns = new HashMap<Long, Campaign>();
	private HashMap<Long, VenuePartner> uniqueVenues = new HashMap<Long, VenuePartner>();

	public AdvertiserBillingReport(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		super(selections, startDate, endDate, showResultsByDeviceGroup, showDetails, detailsFilter, showZeros, orderBy, reverseOrder);
	}
	
	public List<AdInfo> getReportData() throws Exception{
		
		String user = KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		logger.info("Started running report of type " + this.getClass().getName() + " by user " + user);
		
		// Get list of playback rows for the selected advertisers
		List<AdInfo> playbackRows = selections[0].getSelectionId() != null ? getDataFromReportSourceTable(startDate, endDate, selections[0].getSelectionId()) : new ArrayList();
		
		// Populate campaigns
		if(uniqueCampaignIds.size() > 0){
			for(Campaign c : Campaign.getCampaigns(uniqueCampaignIds)){
				uniqueCampaigns.put(c.getCampaignId(), c);
			}
		}
		
		// Populate venues
		if(uniqueVenueIds.size() > 0){
			for(VenuePartner vp : VenuePartner.getVenuePartners(uniqueVenueIds)){
				uniqueVenues.put(vp.getVenuePartnerId(), vp);
			}
		}
		
		// Key: campaignId, Key: venueId, Value: Num Airings, Days In Period
		HashMap<Long, HashMap<Long, Long[]>> groupedPlaybackRows = new HashMap<Long, HashMap<Long, Long[]>>();
		
		// Group the playback rows by unique venue campaign combinations
		for(AdInfo ai : playbackRows){
			// Get the campaign associated to this row
			Campaign c = uniqueCampaigns.get(ai.campaignId);
			
			// If this campaign was billable on this date
			if(c != null && c.getIsBillable() != null && c.getIsBillable() && 
				(c.getStartDt() != null && (ai.date.equals(c.getStartDt()) || ai.date.after(c.getStartDt()))) && (c.getEndDt() != null && ai.date.before(c.getEndDt())) ){
				
				// Add these airings to this row line
				HashMap<Long, Long[]> subMap = groupedPlaybackRows.containsKey(ai.campaignId) ? groupedPlaybackRows.get(ai.campaignId) : new HashMap<Long, Long[]>();
				
				Long numAirings = 0l;
				Long daysInPeriod = null;
				if(subMap.containsKey(ai.venueId)){
					numAirings = subMap.get(ai.venueId)[0];
					daysInPeriod = subMap.get(ai.venueId)[1];
				}
				
				// Calculate daysInPeriod
				if(daysInPeriod == null){
					daysInPeriod = 0l;
					
					Date start = UI_DATE_FORMAT.parse(startDate);
					Date end = UI_DATE_FORMAT.parse(endDate);
					
					Calendar pointer = Calendar.getInstance();
					pointer.setTime(start);
					
					while(pointer.getTime().before(end)){
						if( (c.getStartDt() == null || pointer.getTime().equals(c.getStartDt()) || pointer.getTime().after(c.getStartDt())) &&
								(c.getEndDt() == null || pointer.getTime().before(c.getEndDt())) ){
							daysInPeriod++;
						}
						
						pointer.add(Calendar.DATE, 1);
					}
				}
				
				numAirings += ai.numAirings;
				subMap.put(ai.venueId, new Long[]{numAirings, daysInPeriod});
				groupedPlaybackRows.put(ai.campaignId, subMap);
			}
		}
		
		// The rows that will be displayed on the page
		List<AdInfo> reportRows = new ArrayList();
		
		// For each campaign
		for(Long campaignId : groupedPlaybackRows.keySet()){
			
			Campaign c = uniqueCampaigns.get(campaignId);
			
			// For each underlying venue
			HashMap<Long, Long[]> subMap = groupedPlaybackRows.get(campaignId);
			for(Long venueId : subMap.keySet()){
				
				VenuePartner vp = uniqueVenues.get(venueId);
				Long[] values = subMap.get(venueId);
				
				// Create the report row
				AdInfo ai = new AdInfo();
				ai.advertiserName = c.getAdvertiser().getAdvertiserName();
				ai.campaignId = c.getCampaignId();
				ai.campaignName = c.getCampaignName();
				ai.venueId = vp.getVenuePartnerId();
				ai.venueName = vp.getVenuePartnerName();
				ai.campaignStartDate = c.getStartDt();
				ai.campaignEndDate = c.getEndDt();
				ai.daysInPeriod = values[1].intValue();
				ai.airingsPerDay = values[0].floatValue() / (float)ai.daysInPeriod;
				ai.openHoursPerDay = vp.getOpenHours();
				ai.visitorsPerDay = vp.getVisitors();
				ai.dwellHours = vp.getDwellTime();
				ai.discount = vp.getImpressionDiscount();
				ai.cpm = c.getCpm();
				
				// If we have all the information for calculations
				if(ai.openHoursPerDay != null && ai.visitorsPerDay != null && ai.dwellHours != null){

					ai.impressions = ai.daysInPeriod * (ai.airingsPerDay / ai.openHoursPerDay) * ai.visitorsPerDay * ai.dwellHours;
					
					if(ai.discount != null){
						ai.impressions = ai.impressions * ((100f - ai.discount) / 100f);
					}
					
					if(c.getMaxChargedImpressions() != null){
						long campaignPeriod = (c.getEndDt().getTime() - c.getStartDt().getTime()) / Constants.MILLISECONDS_IN_A_DAY;
						float maxChargedImpressions = (float)(c.getMaxChargedImpressions() * ai.daysInPeriod) / campaignPeriod;
						ai.chargedImpressions = ai.impressions > maxChargedImpressions ? maxChargedImpressions : ai.impressions;
					}else{
						ai.chargedImpressions = ai.impressions;
					}
					
					if(ai.cpm != null){
						ai.totalCost = ai.chargedImpressions * ai.cpm / 1000;
					}
				}
				
				// Add to the report
				reportRows.add(ai);
			}
		}
		
		// Sort by advertiser name
		BeanPropertyComparator comparator = new BeanPropertyComparator( "advertiserName" );		
		Collections.sort( reportRows, comparator );
		this.report = reportRows;
		
		logger.info("Finished running report of type " + this.getClass().getName() + " by user " + user);
		
		return reportRows;
	}
	
	private LinkedList<AdInfo> getDataFromReportSourceTable(String startDate, String endDate, Long selectionId) throws Exception{
		
		// Determine which table to generate the reports off of
		String reportsSourceTable = Constants.REPORTS_SOURCE_TABLE_DEFAULT;
		try{
			reportsSourceTable = KuvataConfig.getPropertyValue( Constants.REPORTS_SOURCE_TABLE );
		}catch(KmfException e){
			logger.info("Could not locate property: "+ Constants.REPORTS_SOURCE_TABLE +". Using default: "+ Constants.REPORTS_SOURCE_TABLE_DEFAULT);
		}

		// Convert the Hibernate class name to db table name
		if( reportsSourceTable.equalsIgnoreCase("PlaybackEvent") ){
			reportsSourceTable = "playback_event";				
		}else if( reportsSourceTable.equalsIgnoreCase("ContentScheduleEvent") ){
			reportsSourceTable = "content_schedule_event";				
		}else if( reportsSourceTable.equalsIgnoreCase("PlaybackEventSummary") ){
			reportsSourceTable = "playback_event_summary";
		}
		
		
		// Determine assets sub query
		String assetsSubQuery = "SELECT campaign_id FROM campaign WHERE is_billable = 1 AND advertiser_id IN (SELECT entity_id FROM selected_entities WHERE selection_id = ?)";
		assetsSubQuery = "SELECT asset_id FROM campaign_asset WHERE campaign_id IN (" + assetsSubQuery + ")";
		
		// Determine sub query to run on the reporting table
		String subQuery;
		if(reportsSourceTable.equals("playback_event_summary")){
			subQuery = "SELECT asset_id, device_id, to_char(start_datetime,'YYYY/MM/DD') dt, SUM(num_airings) num_airings FROM " + reportsSourceTable + " " + 
			 			"WHERE asset_id IN ( " + assetsSubQuery + " ) AND start_datetime >= ? AND start_datetime < ? GROUP BY asset_id, device_id, to_char(start_datetime,'YYYY/MM/DD')";
		}else{
			subQuery = "SELECT asset_id, device_id, dt, SUM(num_airings) num_airings FROM " +
			 "(SELECT asset_id, device_id, to_char(start_datetime,'YYYY/MM/DD') dt, COUNT(*) num_airings from " + reportsSourceTable + " " + 
			 		"WHERE asset_id IN ( " + assetsSubQuery + " ) AND start_datetime >= ? AND start_datetime < ? GROUP BY asset_id, device_id, to_char(start_datetime,'YYYY/MM/DD')) " +
			 "GROUP BY asset_id, device_id, dt";
		}
		
		String sql = "SELECT campaign_id, venue_partner_id, dt, SUM(num_airings) num_airings FROM campaign_asset, device, (" + subQuery + ") subQuery " +
					"WHERE campaign_asset.asset_id = subQuery.asset_id AND device.device_id = subQuery.device_id AND venue_partner_id IS NOT NULL " +
					"GROUP BY campaign_id, venue_partner_id, dt ORDER BY campaign_id";
		
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection(); 
		PreparedStatement pstmt = conn.prepareStatement( sql );
		pstmt.setLong(1, selectionId);
		pstmt.setDate(2, new java.sql.Date(UI_DATE_FORMAT.parse(startDate).getTime()));
		pstmt.setDate(3, new java.sql.Date(UI_DATE_FORMAT.parse(endDate).getTime()));
		ResultSet rs = pstmt.executeQuery();
        
		LinkedList<AdInfo> adInfos = new LinkedList();
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
		
		// Generate adInfo objects for each returned row
        while(rs.next()){
        	AdInfo adInfo = new AdInfo();
        	adInfo.campaignId = rs.getLong("campaign_id");
        	adInfo.venueId = rs.getLong("venue_partner_id");
        	adInfo.date = DATE_FORMAT.parse(rs.getString("dt"));
        	adInfo.numAirings = rs.getLong("num_airings");
        	
        	// Add to the list
			adInfos.add(adInfo);
			
			// Add to unique ids
			uniqueCampaignIds.add(adInfo.campaignId);
			uniqueVenueIds.add(adInfo.venueId);
        }
        rs.close();
        pstmt.close();
        conn.close();
		
		return adInfos;
	}
	
	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws Exception
	{
		String selectedAdvertisersNames;
		if(selections[0].getSelectionNames() != null){
			selectedAdvertisersNames = selections[0].getSelectionNames();
		}else{
			selectedAdvertisersNames = Reformat.parseSelectedValue(selections[0].getSelectedIds(), ALL_ADVERTISERS, true);
		}
		
		String[] columns = new String[]{"Advertiser", "Campaign", "Venue", "Campaign Start Date", "Campaign End Date", "Days in Period", "Airings", "Open Hours",
				"Visitors", "Dwell", "Discount", "Audience Impressions", "Charged Impressions", "CPM", "Cost"};
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Advertiser Billing Report");
		int rowCounter = 0;
		short columnCounter = 0;
		
		/*
		 * Fonts/Styles
		 */
		HSSFFont boldFont = wb.createFont();
		boldFont.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );
		
		HSSFFont fontHeaderBold = wb.createFont();
		fontHeaderBold.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );
		fontHeaderBold.setColor( HSSFColor.WHITE.index );
		
		HSSFFont bigBoldFont = wb.createFont();
		bigBoldFont.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );
		bigBoldFont.setFontHeightInPoints( (short)14 );		
				
		HSSFCellStyle styleHeaderAlignRight = wb.createCellStyle();
		styleHeaderAlignRight.setAlignment( HSSFCellStyle.ALIGN_RIGHT );
		styleHeaderAlignRight.setFont( fontHeaderBold );
		styleHeaderAlignRight.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
		styleHeaderAlignRight.setFillBackgroundColor( HSSFColor.BLACK.index );			

		HSSFCellStyle styleHeaderAlignLeft = wb.createCellStyle();		
		styleHeaderAlignLeft.setFont( fontHeaderBold );
		styleHeaderAlignLeft.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
		styleHeaderAlignLeft.setFillBackgroundColor( HSSFColor.BLACK.index );		
		
		HSSFCellStyle styleAlignLeft = wb.createCellStyle();		
		styleAlignLeft.setFont( boldFont );	
		
		HSSFCellStyle styleTitle = wb.createCellStyle();
		styleTitle.setAlignment( HSSFCellStyle.ALIGN_LEFT );
		styleTitle.setFont( bigBoldFont );		
		
		HSSFCellStyle styleRow0Left = wb.createCellStyle();
		
		HSSFCellStyle styleRow1Left = wb.createCellStyle();
		styleRow1Left.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
		styleRow1Left.setFillForegroundColor( HSSFColor.GREY_25_PERCENT.index );
		
		HSSFCellStyle styleRow0LeftWrap = wb.createCellStyle();
		styleRow0LeftWrap.setWrapText( true );
		
		HSSFCellStyle styleRow1LeftWrap = wb.createCellStyle();
		styleRow1LeftWrap.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
		styleRow1LeftWrap.setFillForegroundColor( HSSFColor.GREY_25_PERCENT.index );
		styleRow1LeftWrap.setWrapText( true );			
		
		HSSFCellStyle styleRow0Right = wb.createCellStyle();
		styleRow0Right.setAlignment( HSSFCellStyle.ALIGN_RIGHT );
		
		HSSFCellStyle styleRow1Right = wb.createCellStyle();			
		styleRow1Right.setAlignment( HSSFCellStyle.ALIGN_RIGHT );		
		styleRow1Right.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );		
		styleRow1Right.setFillForegroundColor( HSSFColor.GREY_25_PERCENT.index );			
		
		/*
		 * Header section
		 */
		HSSFCell cell0 = sheet.createRow( rowCounter++ ).createCell( (short)0 );
		cell0.setCellValue("Advertiser Billing Report");
		cell0.setCellStyle( styleTitle );		
		
		// Spacer
		sheet.createRow( rowCounter++ );
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Advertisers:", selectedAdvertisersNames, rowCounter);
				
		HSSFRow row2 = sheet.createRow( rowCounter++ );
		HSSFCell row2Cell0 = row2.createCell( (short)0 );
		row2Cell0.setCellValue( "Date Range:" );
		row2Cell0.setCellStyle( styleAlignLeft );
		row2.createCell( (short)1 ).setCellValue( startDate +" - "+ endDate );							
				
		// Spacer
		sheet.createRow( rowCounter++ );			
	
		/*
		 * Report column headers
		 */
		columnCounter = 0;
		Integer[] columnMaxLength = new Integer[columns.length];
		HSSFRow headerRow = sheet.createRow( rowCounter++ );
		
		for(String columnName : columns){
			HSSFCell cell = headerRow.createCell( columnCounter++ );
			cell.setCellValue(columnName);
			cell.setCellStyle(columnCounter == 1 ? styleHeaderAlignLeft : styleHeaderAlignRight);
			columnMaxLength[columnCounter - 1] = columnName.length();
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				
		/*
		 * Report rows
		 */		
		int rowCount = 0;
		for( Iterator<AdInfo> i=this.report.iterator(); i.hasNext(); )
		{
			AdInfo info = i.next();
			
			// Create row array
			String[] rowData = new String[]{info.advertiserName, info.campaignName, info.venueName, info.campaignStartDate != null ? sdf.format(info.campaignStartDate) : null,
					info.campaignEndDate != null ? sdf.format(info.campaignEndDate) : null, info.daysInPeriod.toString(), df.format(info.airingsPerDay),
					info.openHoursPerDay != null ? df.format(info.openHoursPerDay) : null, info.visitorsPerDay != null ? df.format(info.visitorsPerDay) : null,
					info.dwellHours != null ? df.format(info.dwellHours) : null, info.discount != null ? df.format(info.discount) + "%" : null,
					info.impressions != null ? df.format(info.impressions) : null, info.chargedImpressions != null ? df.format(info.chargedImpressions) : null,
					info.cpm != null ? "$" + df.format(info.cpm) : null, info.totalCost != null ? "$" + df.format(info.totalCost) : null};
			HSSFRow row = sheet.createRow( rowCounter++ );
			columnCounter = 0;			
			
			for(int j=0;j<rowData.length;j++){
				HSSFCell cell = row.createCell( columnCounter++ );
				try {
					Double d = Double.valueOf(rowData[j]);
					cell.setCellValue(d);
				} catch (Exception e) {
					cell.setCellValue( rowData[j] );
				}
				
				HSSFCellStyle style;
				if(columnCounter == 1){
					style = ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left;
				}else{
					style = ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right;
				}
				
				cell.setCellStyle( style );
				
				// Set column max length
				if(rowData[j] != null && rowData[j].length() > columnMaxLength[j]){
					columnMaxLength[j] = rowData[j].length();
				}
			}		
			rowCount++;
		}
		
		// Set each column width according to the maxLength of each
		for(int j=0;j<columnMaxLength.length;j++){
			sheet.setColumnWidth((short)j, (short)( (columnMaxLength[j] * 256) + 512 ));
		}		
		
		// Landscape mode
		sheet.getPrintSetup().setLandscape( true );
		sheet.setFitToPage( true );
		
		// If we are sending the sheet back to the UI
		if(response != null){
			response.setHeader("Content-Disposition","attachment; filename=\"AdvertiserBillingReport-" + Reformat.windowsEscape(selectedAdvertisersNames) + ".xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		return wb;
	}
	
	public static class AdInfo{
		Long campaignId;
		Long venueId;
		Date date;
		Long numAirings;
		
		String advertiserName;
		String campaignName;
		String venueName;
		Date campaignStartDate;
		Date campaignEndDate;
		Integer daysInPeriod;
		Float airingsPerDay;
		Float openHoursPerDay;
		Float visitorsPerDay;
		Float dwellHours;
		Float discount;
		Float impressions;
		Float chargedImpressions;
		Float cpm;
		Float totalCost;
		
		public String getAdvertiserName() {
			return advertiserName;
		}
		public String getCampaignName() {
			return campaignName;
		}
		public String getVenueName() {
			return venueName;
		}
		public Date getCampaignStartDate() {
			return campaignStartDate;
		}
		public Date getCampaignEndDate() {
			return campaignEndDate;
		}
		public Integer getDaysInPeriod() {
			return daysInPeriod;
		}
		public Float getAiringsPerDay() {
			return airingsPerDay;
		}
		public Float getOpenHoursPerDay() {
			return openHoursPerDay;
		}
		public Float getVisitorsPerDay() {
			return visitorsPerDay;
		}
		public Float getDwellHours() {
			return dwellHours;
		}
		public Float getDiscount() {
			return discount;
		}
		public Float getImpressions() {
			return impressions;
		}
		public Float getCpm() {
			return cpm;
		}
		public Float getTotalCost() {
			return totalCost;
		}
		public Float getChargedImpressions() {
			return chargedImpressions;
		}
		public Long getCampaignId() {
			return campaignId;
		}
		public Long getVenueId() {
			return venueId;
		}
		public void setAdvertiserName(String advertiserName) {
			this.advertiserName = advertiserName;
		}
		public void setCampaignName(String campaignName) {
			this.campaignName = campaignName;
		}
		public void setCampaignStartDate(Date campaignStartDate) {
			this.campaignStartDate = campaignStartDate;
		}
		public void setCampaignEndDate(Date campaignEndDate) {
			this.campaignEndDate = campaignEndDate;
		}
		public void setDaysInPeriod(Integer daysInPeriod) {
			this.daysInPeriod = daysInPeriod;
		}
		public void setImpressions(Float impressions) {
			this.impressions = impressions;
		}
		public void setChargedImpressions(Float chargedImpressions) {
			this.chargedImpressions = chargedImpressions;
		}
		public void setCpm(Float cpm) {
			this.cpm = cpm;
		}
	}
}