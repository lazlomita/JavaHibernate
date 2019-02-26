package com.kuvata.kmf.reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
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
import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.billing.Campaign;
import com.kuvata.kmf.util.Reformat;

public class DailyCampaignReport extends Report{

	public DailyCampaignReport(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		super(selections, startDate, endDate, showResultsByDeviceGroup, showDetails, detailsFilter, showZeros, orderBy, reverseOrder);
	}
	
	public LinkedList<CampaignInfo> getReportData() throws Exception{
		
		String user = KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		logger.info("Started running report of type " + this.getClass().getName() + " by user " + user);
		
		boolean isShowDetails = showDetails != null && showDetails ? true : false;
		LinkedList<CampaignInfo> cInfos = getDataFromReportSourceTable(selections, startDate, endDate, isShowDetails);
		LinkedHashMap<String, CampaignInfo> groupedInfos = new LinkedHashMap();
		
		CampaignInfo totalInfo = new CampaignInfo();
		totalInfo.setDate("Total Averages:");
		totalInfo.setNumDevices(0f);
		totalInfo.setAvgDeviceAirings(0f);
		totalInfo.setNumDisplayAirings(0l);
		totalInfo.setNumDisplayExceptions(0l);
		
		// If we need to show details
		if(isShowDetails){
			// Group these by date
			for(CampaignInfo cInfo : cInfos){
				if(groupedInfos.keySet().contains(cInfo.getDate())){
					CampaignInfo ci = groupedInfos.get(cInfo.getDate());
					ci.setNumDevices(ci.getNumDevices() + cInfo.getNumDevices());
					ci.setNumAirings(ci.getNumAirings() + cInfo.getNumAirings());
					ci.setNumDisplayAirings(ci.getNumDisplayAirings() + cInfo.getNumDisplayAirings());
					ci.setNumDisplayExceptions(ci.getNumDisplayExceptions() + cInfo.getNumDisplayExceptions());
					HashMap hm = ci.getDeviceAiringInfos();
					hm.put(cInfo.getDeviceName(), cInfo.getNumAirings());
					ci.setDeviceAiringInfos(hm);
					groupedInfos.put(ci.getDate(), ci);
				}else{
					// Create cInfo elements
					HashMap deviceAiringInfos = new HashMap();
					deviceAiringInfos.put(cInfo.getDeviceName(), cInfo.getNumAirings());
					cInfo.setDeviceAiringInfos(deviceAiringInfos);
					groupedInfos.put(cInfo.getDate(), cInfo);
				}
			}
			
			// For each date, sort the device details by number of airings
			for(CampaignInfo ci : groupedInfos.values()){
				String detailInfo = "Aired on these Devices: ";
				HashMap<String, Object> sortedMap = sortHashMapByValues(ci.getDeviceAiringInfos());
				for(String deviceName : sortedMap.keySet()){
					detailInfo += deviceName + " - " + sortedMap.get(deviceName) + ", ";
				}
				
				// Set UI parameters
				ci.setDetailInfo(detailInfo.substring(0, detailInfo.length() - 2));
				ci.setAvgDeviceAirings((float)ci.getNumAirings()/ci.getNumDevices());
				
				if(ci.getNumDisplayAirings() != 0){
					ci.setAvgDisplayAirings( (((float)ci.getNumDisplayAirings() - (float)ci.getNumDisplayExceptions()) / (float)ci.getNumDisplayAirings()) * ci.getAvgDeviceAirings() );
				}else{
					ci.setAvgDisplayAirings(0f);
				}
				
				totalInfo.setNumDevices(totalInfo.getNumDevices() + ci.getNumDevices());
				totalInfo.setAvgDeviceAirings(totalInfo.getAvgDeviceAirings() + ci.getAvgDeviceAirings());
				totalInfo.setNumDisplayAirings(totalInfo.getNumDisplayAirings() + ci.getNumDisplayAirings());
				totalInfo.setNumDisplayExceptions(totalInfo.getNumDisplayExceptions() + ci.getNumDisplayExceptions());
			}
			
			cInfos = new LinkedList(groupedInfos.values());
		}else{
			for(CampaignInfo cInfo : cInfos){
				cInfo.setAvgDeviceAirings((float)cInfo.getNumAirings()/cInfo.getNumDevices());
				
				if(cInfo.getNumDisplayAirings() != 0){
					cInfo.setAvgDisplayAirings( (((float)cInfo.getNumDisplayAirings() - (float)cInfo.getNumDisplayExceptions()) / (float)cInfo.getNumDisplayAirings()) * (float)cInfo.getAvgDeviceAirings() );
				}else{
					cInfo.setAvgDisplayAirings(0f);
				}
				
				totalInfo.setNumDevices(totalInfo.getNumDevices() + cInfo.getNumDevices());
				totalInfo.setAvgDeviceAirings(totalInfo.getAvgDeviceAirings() + cInfo.getAvgDeviceAirings());
				totalInfo.setNumDisplayAirings(totalInfo.getNumDisplayAirings() + cInfo.getNumDisplayAirings());
				totalInfo.setNumDisplayExceptions(totalInfo.getNumDisplayExceptions() + cInfo.getNumDisplayExceptions());
			}
		}
		
		// Calculate total averages
		if(cInfos.size() > 0){
			totalInfo.setNumDevices(totalInfo.getNumDevices() / (float)cInfos.size());
			totalInfo.setAvgDeviceAirings(totalInfo.getAvgDeviceAirings() / (float)cInfos.size());
			
			if(totalInfo.getNumDisplayAirings() != 0){
				totalInfo.setAvgDisplayAirings( ((float)totalInfo.getNumDisplayAirings() - (float)totalInfo.getNumDisplayExceptions()) / (float)totalInfo.getNumDisplayAirings()  * totalInfo.getAvgDeviceAirings());
			}else{
				totalInfo.setAvgDisplayAirings(0f);
			}
		}
		
		cInfos.add(totalInfo);
		
		logger.info("Finished running report of type " + this.getClass().getName() + " by user " + user);
		
		this.report = cInfos;
		return cInfos;
	}

	private static LinkedList<CampaignInfo> getDataFromReportSourceTable(ReportEntitySelectionType[] selections, String startDate, String endDate, boolean isShowDetails) throws Exception{
		
		ReportEntitySelectionType restDevice = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		ReportEntitySelectionType restCampaign = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Campaign.class);
		
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
		
		/*
		 * Read from the playback_event table using jdbc
		 */
		
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();
		String sql = "";
			sql += "SELECT /*+ NO_MERGE(subQuery) */ dt, COUNT(numDevices) as numDevices, SUM(numAirings) as numAirings, SUM(numDisplayAirings) as numDisplayAirings, SUM(numDisplayExceptions) as numDisplayExceptions";
			sql += isShowDetails ? ", device_name " : " ";
			sql +=	"FROM ( ";
			sql	+=		"SELECT to_char(start_datetime,'YYYY/MM/DD') as dt, device_id, ";
			sql += 		"COUNT(DISTINCT device_name) as numDevices";
			sql +=		", COUNT(*) as numAirings, SUM(displays_count) as numDisplayAirings, SUM(display_exceptions_count) as numDisplayExceptions";
			sql +=		isShowDetails ? ", device_name " : " ";
			sql +=		"FROM (SELECT asset_id FROM campaign_asset WHERE campaign_id IN (SELECT entity_id FROM selected_entities WHERE selection_id = ?) ) assets, " + reportsSourceTable + " pe "
				+ 		"WHERE start_datetime >= ? AND start_datetime < ? AND pe.asset_id = assets.asset_id "
				+		"GROUP BY to_char(start_datetime,'YYYY/MM/DD') ";
			sql += ", device_id, device_name ) subQuery WHERE subQuery.device_id in (SELECT entity_id FROM selected_entities WHERE selection_id = ?) "
				+ "GROUP BY dt";
			sql += isShowDetails ? ", device_name " : " ";
			sql += "ORDER BY dt";
		
		// If we are using the summary table for this report
		if(reportsSourceTable.equalsIgnoreCase("playback_event_summary")){
			sql = sql.replace("COUNT(*) as numAirings", "SUM(num_airings) as numAirings");
		}
		
		PreparedStatement pstmt = conn.prepareStatement( sql );
		pstmt.setLong(1, restCampaign.getSelectionId());
		pstmt.setDate(2, new java.sql.Date(UI_DATE_FORMAT.parse(startDate).getTime()));
		pstmt.setDate(3, new java.sql.Date(UI_DATE_FORMAT.parse(endDate).getTime()));
		pstmt.setLong(4, restDevice.getSelectionId());
		ResultSet rs = pstmt.executeQuery();       
	    
		LinkedList<CampaignInfo> cInfos = new LinkedList();
		
		// Generate cInfo objects for each returned row
	    while(rs.next()){
	    	CampaignInfo cInfo = new CampaignInfo();
	    	cInfo.setDate(rs.getString("dt"));
	    	cInfo.setNumDevices(rs.getFloat("numDevices"));
	    	cInfo.setNumAirings(rs.getLong("numAirings"));
	    	cInfo.setNumDisplayAirings(rs.getLong("numDisplayAirings"));
	    	cInfo.setNumDisplayExceptions(rs.getLong("numDisplayExceptions"));
			if(isShowDetails){
				cInfo.setDeviceName(rs.getString("device_name"));
			}
			
	    	// Add to the list
			cInfos.add(cInfo);
	    }
	    rs.close();
	    pstmt.close();
	    conn.close();
		
		return cInfos;
	}
	
	private static LinkedHashMap sortHashMapByValues(HashMap passedMap) {
	    List mapKeys = new ArrayList(passedMap.keySet());
	    List mapValues = new ArrayList(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap sortedMap = new LinkedHashMap();
	    
	    Iterator valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, val);
	                break;
	            }
	        }
	    }
	    return sortedMap;
	}
	
	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws FileNotFoundException, IOException, MimeTypeParseException
	{
		ReportEntitySelectionType campaignSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Campaign.class);
		ReportEntitySelectionType deviceSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		String selectedCampaignName = Campaign.getCampaign(Long.parseLong(campaignSelection.getSelectedIds())).getCampaignName();
		String selectedDeviceNames = deviceSelection.getSelectionNames() != null ? deviceSelection.getSelectionNames() : "All Devices";
		
		String[] columns = showDetails != null && showDetails ?
				new String[]{"Date", "Devices Aired On", "Average Device Airings", "Average Display Airings", " ", "Aired on these Devices"} :
				new String[]{"Date", "Devices Aired On", "Average Device Airings", "Average Display Airings"};
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Daily Campaign Report");
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
		cell0.setCellValue("Daily Campaign Report");
		cell0.setCellStyle( styleTitle );		
		
		// Spacer
		sheet.createRow( rowCounter++ );	
		
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Campaign:", selectedCampaignName, rowCounter);
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Devices:", selectedDeviceNames, rowCounter);
		
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
		
		DecimalFormat df = new DecimalFormat("0.##");
				
		/*
		 * Report rows
		 */		
		int rowCount = 0;
		for( Iterator<CampaignInfo> i=report.iterator(); i.hasNext(); )
		{
			CampaignInfo info = i.next();
			
			// Create row array
			String airedOnValue = info.getDetailInfo() != null && info.getDetailInfo().indexOf(":") > 0 ? info.getDetailInfo().split(":")[1].trim() : info.getDetailInfo();
			String[] rowData = showDetails != null && showDetails ?
					new String[]{info.getDate(), info.getNumDevices() != null ? df.format(info.getNumDevices()) : null, info.getAvgDeviceAirings() != null ? df.format(info.getAvgDeviceAirings()) : null, info.getAvgDisplayAirings() != null ? df.format(info.getAvgDisplayAirings()) : null, null, airedOnValue} : 
					new String[]{info.getDate(), info.getNumDevices() != null ? df.format(info.getNumDevices()) : null, info.getAvgDeviceAirings() != null ? df.format(info.getAvgDeviceAirings()) : null, info.getAvgDisplayAirings() != null ? df.format(info.getAvgDisplayAirings()) : null}; 
			
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
			sheet.setColumnWidth((short)j, (short) ( columnMaxLength[j] * 256 ));
		}		
		
		// Landscape mode
		sheet.getPrintSetup().setLandscape( true );
		sheet.setFitToPage( true );
		
		if(response != null){
			response.setHeader("Content-Disposition","attachment; filename=\"DailyCampaignReport-" + Reformat.windowsEscape(selectedCampaignName) + ".xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		
		return wb;
	}
	
	public static class CampaignInfo{
		private String date;
		private String deviceName;
		private Float numDevices;
		private Long numAirings;
		private Float avgDeviceAirings;
		private Float avgDisplayAirings;
		private Long numDisplayAirings;
		private Long numDisplayExceptions;
		private String detailInfo;
		private HashMap deviceAiringInfos;
		
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public Float getNumDevices() {
			return numDevices;
		}
		public void setNumDevices(Float numDevices) {
			this.numDevices = numDevices;
		}
		public Float getAvgDeviceAirings() {
			return avgDeviceAirings;
		}
		public void setAvgDeviceAirings(Float avgDeviceAirings) {
			this.avgDeviceAirings = avgDeviceAirings;
		}
		public Float getAvgDisplayAirings() {
			return avgDisplayAirings;
		}
		public void setAvgDisplayAirings(Float avgDisplayAirings) {
			this.avgDisplayAirings = avgDisplayAirings;
		}
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		public Long getNumAirings() {
			return numAirings;
		}
		public void setNumAirings(Long numAirings) {
			this.numAirings = numAirings;
		}
		public String getDetailInfo() {
			return detailInfo;
		}
		public void setDetailInfo(String detailInfo) {
			this.detailInfo = detailInfo;
		}
		public HashMap getDeviceAiringInfos() {
			return deviceAiringInfos;
		}
		public void setDeviceAiringInfos(HashMap deviceAiringInfos) {
			this.deviceAiringInfos = deviceAiringInfos;
		}
		public Long getNumDisplayAirings() {
			return numDisplayAirings;
		}
		public void setNumDisplayAirings(Long numDisplayAirings) {
			this.numDisplayAirings = numDisplayAirings;
		}
		public Long getNumDisplayExceptions() {
			return numDisplayExceptions;
		}
		public void setNumDisplayExceptions(Long numDisplayExceptions) {
			this.numDisplayExceptions = numDisplayExceptions;
		}
	}
}
