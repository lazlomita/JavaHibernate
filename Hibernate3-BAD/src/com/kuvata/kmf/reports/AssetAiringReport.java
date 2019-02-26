package com.kuvata.kmf.reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.attr.Attr;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.util.Reformat;

public class AssetAiringReport extends Report{
	
	public AssetAiringReport(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		super(selections, startDate, endDate, showResultsByDeviceGroup, showDetails, detailsFilter, showZeros, orderBy, reverseOrder);
	}
	
	public List<AssetAiringReportInfo> getReportData() throws IOException, ParseException
	{
		String user = KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		logger.info("Started running report of type " + this.getClass().getName() + " by user " + user);
		
		ReportEntitySelectionType assetRest = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Asset.class);
		ReportEntitySelectionType deviceRest = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		
		ArrayList totals = new ArrayList();
		Date startDate = UI_DATE_FORMAT.parse( this.startDate );
		Date endDate = UI_DATE_FORMAT.parse( this.endDate );
		
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

		// Get the totals number of times this asset aired on each device, filtered by device group
		String sql = "SELECT /*+ ORDERED INDEX (pe, pes_combined) */ pe.device_id, pe.device_name, COUNT(*) as numDeviceAirings, SUM(pe.asset_length) as deviceAiringsLength, "
			+ "(SUM(pe.displays_count) - SUM(pe.display_exceptions_count)) as numDisplayAirings, "
			+ "(SUM((pe.displays_count - pe.display_exceptions_count) * pe.asset_length)) as displayAiringsLength "		
			+ "FROM "+ reportsSourceTable +" pe " 
			+ "WHERE pe.asset_id IN ( SELECT entity_id FROM selected_entities WHERE selection_id = :assetSelectionId ) "
			+ "AND pe.start_datetime >= :startDate "
			+ "AND pe.start_datetime < :endDate ";
		
		if( deviceRest.getSelectionId() != null ) {
			sql += "AND pe.device_id IN ( SELECT entity_id FROM selected_entities WHERE selection_id = :deviceSelectionId ) ";
		}		  
		sql += "GROUP BY pe.device_id, pe.device_name, pe.asset_id";
		
		// If we are using the summary table for this report
		if(reportsSourceTable.equalsIgnoreCase("playback_event_summary")){
			sql = sql.replace("COUNT(*) as numDeviceAirings", "SUM(pe.num_airings) as numDeviceAirings");
			sql = sql.replace("SUM(pe.asset_length) as deviceAiringsLength", "SUM(pe.airing_length) as deviceAiringsLength");
			sql = sql.replace("(SUM((pe.displays_count - pe.display_exceptions_count) * pe.asset_length)) as displayAiringsLength", "SUM(pe.display_airing_length) as displayAiringsLength");
		}
		
		Session session = HibernateSession.currentSession();
		Query q = session.createSQLQuery( sql )
				.setParameter( "assetSelectionId", assetRest.getSelectionId() )
				.setParameter( "startDate", startDate )
				.setParameter( "endDate", endDate );
		if( deviceRest.getSelectionId() != null ) {
			q.setParameter("deviceSelectionId", deviceRest.getSelectionId());
		}
		
		List<Object[]> results = q.list();
		
		// Get asset metadata column names
		TreeMap<String, String> assetMetadataMap = new TreeMap<String, String>();
		String hql = "SELECT a.attrDefinitionName FROM AttrDefinition a WHERE a.showInReport = :showInReport AND a.entityClass.className = :entityClass ORDER BY a.attrDefinitionName";
		List<String> attrDefinitionNames = session.createQuery(hql).setParameter("showInReport", Boolean.TRUE).setParameter("entityClass", Asset.class.getName()).list();
		
		hql = "SELECT a.attrDefinition.attrDefinitionName, a FROM Attr a WHERE a.attrDefinition.showInReport = :showInReport AND a.ownerId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :assetSelectionId ) ORDER BY a.attrDefinition.attrDefinitionName";
		List<Object[]> l = session.createQuery(hql).setParameter("showInReport", Boolean.TRUE).setParameter("assetSelectionId", assetRest.getSelectionId()).list();
		for(Object[] o : l){
			String attrDefinitionName = (String)o[0];
			Attr attr = (Attr)o[1];
			assetMetadataMap.put(attrDefinitionName, attr.getFormattedValue());
		}
		
		// Add missing columns
		for(String attrDefinitionName : attrDefinitionNames){
			if(assetMetadataMap.containsKey(attrDefinitionName) == false){
				assetMetadataMap.put(attrDefinitionName, "");
			}
		}
		
		ThreadLocal<TreeMap<String, String>> assetMetadata = new ThreadLocal<TreeMap<String, String>>();
		assetMetadata.set(assetMetadataMap);
		AssetAiringReportInfo.assetMetadata = assetMetadata;
		
		// Get device metadata column names
		hql = "SELECT a.attrDefinitionName FROM AttrDefinition a WHERE a.showInReport = :showInReport AND a.entityClass.className = :entityClass ORDER BY a.attrDefinitionName";
		attrDefinitionNames = session.createQuery(hql).setParameter("showInReport", Boolean.TRUE).setParameter("entityClass", Device.class.getName()).list();
		ThreadLocal<List<String>> deviceMetadataColumns = new ThreadLocal<List<String>>();
		deviceMetadataColumns.set(attrDefinitionNames);
		AssetAiringReportInfo.deviceMetadataColumns = deviceMetadataColumns;
		
		HashMap<Long, LinkedHashMap<String, String>> deviceMetadata = new HashMap<Long, LinkedHashMap<String, String>>();
		if(results.size() > 0){
			hql = "SELECT a.attrDefinition.attrDefinitionName, a FROM Attr a WHERE a.attrDefinition.showInReport = :showInReport ";
			
			if( deviceRest.getSelectionId() != null ) {
				hql += "AND a.ownerId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) ";
			}else{
				hql += "AND a.ownerId IN ( SELECT deviceId FROM Device ) ";
			}
			
			hql += "ORDER BY a.attrDefinition.attrDefinitionName";
			
			q = session.createQuery(hql).setParameter("showInReport", Boolean.TRUE);
			if( deviceRest.getSelectionId() != null ) {
				q.setParameter("deviceSelectionId", deviceRest.getSelectionId());
			}
			
			l = q.list();
			for(Object[] o : l){
				String attrDefinitionName = (String)o[0];
				Attr attr = (Attr)o[1];
				
				LinkedHashMap<String, String> deviceMetadataValues = deviceMetadata.containsKey(attr.getOwnerId()) ? deviceMetadata.get(attr.getOwnerId()) : new LinkedHashMap<String, String>();
				deviceMetadataValues.put(attrDefinitionName, attr.getFormattedValue());
				deviceMetadata.put(attr.getOwnerId(), deviceMetadataValues);
			}
		}
		
		for( Iterator<Object[]> i=results.iterator(); i.hasNext(); ){
			Object[] row = (Object[])i.next();
			AssetAiringReportInfo info = new AssetAiringReportInfo();
			Long deviceId = ((BigDecimal)row[0]).longValue();
			info.setDeviceId( String.valueOf(deviceId) );
			info.setDeviceName( (String)row[1] );
			info.setNumDeviceAirings( ((BigDecimal)row[2]).longValue() );
			info.setDeviceMetadata(deviceMetadata.get(deviceId));
			
			// Convert the total length from seconds to HH:mm:ss			
			info.setDeviceAiringsLength( Reformat.formatTime(  ((BigDecimal)row[3]).floatValue()  ) );
			info.setNumDisplayAirings( row[4] != null ? ((BigDecimal)row[4]).longValue() : Long.valueOf(0) );
			info.setDisplayAiringsLength( row[5] != null ? Reformat.formatTime( ((BigDecimal)row[5] ).floatValue()) : Reformat.formatTime(0) );		
			totals.add( info );
		}
			
		// Order the collection
		if(orderBy != null){
			BeanPropertyComparator comparator = new BeanPropertyComparator( orderBy );
			Collections.sort( totals, comparator );
			if( reverseOrder != null && reverseOrder ){
				Collections.reverse( totals );	
			}
		}
		
		logger.info("Finished running report of type " + this.getClass().getName() + " by user " + user);
		
		this.report = totals;
		return totals;
	}
	
	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws FileNotFoundException, IOException, MimeTypeParseException
	{
		ReportEntitySelectionType assetSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Asset.class);
		ReportEntitySelectionType deviceSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		Asset asset = Asset.getAsset(Long.parseLong(assetSelection.getSelectedIds()));
		
		String DEVICE_NAME = "Device Name";
		String DEVICE_AIRINGS = "Device Airings";
		String DEVICE_AIRINGS_LENGTH = "Device Airings Length (hh:mm:ss)";
		String DISPLAY_AIRINGS = "Display Airings";
		String DISPLAY_AIRINGS_LENGTH = "Display Airings Length (hh:mm:ss)";		
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Asset Airing Report");
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
		cell0.setCellValue("Asset Airing Report - Totals By Device");
		cell0.setCellStyle( styleTitle );		
		
		// Spacer
		sheet.createRow( rowCounter++ );	
		
		HSSFRow row1 = sheet.createRow( rowCounter++ );
		HSSFCell row1Cell0 = row1.createCell( (short)0 );
		row1Cell0.setCellValue( "Asset Name:" );
		row1Cell0.setCellStyle( styleAlignLeft );		
		row1.createCell( (short)1 ).setCellValue( asset.getAssetName() );
		
		// Dynamic metadata rows
		for(Entry<String, String> e : AssetAiringReportInfo.assetMetadata.get().entrySet()){
			HSSFRow row = sheet.createRow( rowCounter++ );
			HSSFCell rowCell0 = row.createCell( (short)0 );
			rowCell0.setCellValue( e.getKey() + ":" );
			rowCell0.setCellStyle( styleAlignLeft );		
			row.createCell( (short)1 ).setCellValue( e.getValue() );
		}
		
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Devices:", deviceSelection.getSelectionNames(), rowCounter);
			
		HSSFRow row3 = sheet.createRow( rowCounter++ );
		HSSFCell row3Cell0 = row3.createCell( (short)0 );
		row3Cell0.setCellValue( "Date Range:" );
		row3Cell0.setCellStyle( styleAlignLeft );
		row3.createCell( (short)1 ).setCellValue( startDate +" - "+ endDate );
		
		// Spacer
		sheet.createRow( rowCounter++ );			
	
		/*
		 * Report column headers
		 */
		columnCounter = 0;
		HSSFRow headerRow = sheet.createRow( rowCounter++ );
		HSSFCell cell1 = headerRow.createCell( columnCounter++ );
		cell1.setCellValue( DEVICE_NAME );
		cell1.setCellStyle( styleHeaderAlignLeft );
		int deviceNameMaxLength = DEVICE_NAME.length();
		HSSFCell cell2 = headerRow.createCell( columnCounter++ );
		cell2.setCellValue( DEVICE_AIRINGS );
		cell2.setCellStyle( styleHeaderAlignRight );
		int deviceAiringsMaxLength = DEVICE_AIRINGS.length();
		HSSFCell cell3 = headerRow.createCell( columnCounter++ );
		cell3.setCellValue( DEVICE_AIRINGS_LENGTH );
		cell3.setCellStyle( styleHeaderAlignRight );
		int deviceAiringsLengthMaxLength = DEVICE_AIRINGS_LENGTH.length();
		HSSFCell cell4 = headerRow.createCell( columnCounter++ );
		cell4.setCellValue( DISPLAY_AIRINGS );
		cell4.setCellStyle( styleHeaderAlignRight );
		int displayAiringsMaxLength = DISPLAY_AIRINGS.length();
		HSSFCell cell5 = headerRow.createCell( columnCounter++ );
		cell5.setCellValue( DISPLAY_AIRINGS_LENGTH );
		cell5.setCellStyle( styleHeaderAlignRight );
		int displayAiringsLengthMaxLength = DISPLAY_AIRINGS_LENGTH.length();
		
		// Dynamic metadata column headers
		int index = 0;
		Integer[] metadataColumnMaxLength = new Integer[AssetAiringReportInfo.deviceMetadataColumns.get().size()];
		for(String columnHeader : AssetAiringReportInfo.deviceMetadataColumns.get()){
			HSSFCell cell = headerRow.createCell( columnCounter++ );
			cell.setCellValue( columnHeader );
			cell.setCellStyle( styleHeaderAlignRight );
			metadataColumnMaxLength[index++] = columnHeader.length();
		}
		
		/*
		 * Report rows
		 */		
		int rowCount = 0;
		for( Iterator<AssetAiringReportInfo> i=report.iterator(); i.hasNext(); )
		{
			AssetAiringReportInfo info = i.next();
			HSSFRow row = sheet.createRow( rowCounter++ );
			columnCounter = 0;			
			
			HSSFCell deviceNameCell = row.createCell( columnCounter++ );
			deviceNameCell.setCellValue( info.getDeviceName() );
			deviceNameCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell deviceAiringsCell = row.createCell( columnCounter++ );
			deviceAiringsCell.setCellValue( info.getNumDeviceAirings() );
			deviceAiringsCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );
			
			HSSFCell deviceAiringsLengthCell = row.createCell( columnCounter++ );
			deviceAiringsLengthCell.setCellValue( info.getDeviceAiringsLength() );
			deviceAiringsLengthCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );
			
			HSSFCell displayAiringsCell = row.createCell( columnCounter++ );
			displayAiringsCell.setCellValue( info.getNumDisplayAirings() );
			displayAiringsCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );
			
			HSSFCell displayAiringsLengthCell = row.createCell( columnCounter++ );
			displayAiringsLengthCell.setCellValue( info.getDisplayAiringsLength() );
			displayAiringsLengthCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );
			
			// Dynamic metadata columns
			index = 0;
			for(String columnHeader : AssetAiringReportInfo.deviceMetadataColumns.get()){
				String cellValue = info.getDeviceMetadata() != null && info.getDeviceMetadata().get(columnHeader) != null ? info.getDeviceMetadata().get(columnHeader) : "";
				HSSFCell cell = row.createCell( columnCounter++ );
				cell.setCellValue( cellValue );
				cell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );
				
				if( cellValue.length() > metadataColumnMaxLength[index] ){
					metadataColumnMaxLength[index] = cellValue.length();
				}
				
				// Next column
				index++;
			}
								
			// Update the maxLength values if necessary
			if( info.getDeviceName().length() > deviceNameMaxLength ){
				deviceNameMaxLength = info.getDeviceName().length();
			}
			if( info.getNumDeviceAirings().toString().length() > deviceAiringsMaxLength ){
				deviceAiringsMaxLength = info.getNumDeviceAirings().toString().length();
			}
			if( info.getDeviceAiringsLength().length() > deviceAiringsLengthMaxLength ){
				deviceAiringsLengthMaxLength = info.getDeviceAiringsLength().length();
			}
			if( info.getNumDisplayAirings().toString().length() > displayAiringsMaxLength ){
				displayAiringsMaxLength = info.getNumDisplayAirings().toString().length();
			}
			if( info.getDisplayAiringsLength().length() > displayAiringsLengthMaxLength ){
				displayAiringsLengthMaxLength = info.getDisplayAiringsLength().length();
			}			
			rowCount++;
		}
		
		// Set each column width according to the maxLength of each 
		sheet.setColumnWidth((short)0, (short) ( deviceNameMaxLength * 256 ));
		sheet.setColumnWidth((short)1, (short) ( deviceAiringsMaxLength * 256 ));
		sheet.setColumnWidth((short)2, (short) ( deviceAiringsLengthMaxLength * 256 ));
		sheet.setColumnWidth((short)3, (short) ( displayAiringsMaxLength * 256 ));
		sheet.setColumnWidth((short)4, (short) ( displayAiringsLengthMaxLength * 256 ));
		
		// Set dynamic metadata column lengths
		index = 5;
		for(int columnMaxLength : metadataColumnMaxLength ){
			sheet.setColumnWidth((short)index++, (short) ( columnMaxLength * 256 ));
		}
		
		// Landscape mode
		sheet.getPrintSetup().setLandscape( true );
		sheet.setFitToPage( true );
		
		if(response != null){
			response.setHeader("Content-Disposition","attachment; filename=\"AssetAiringReport-TotalsByDevice-" + Reformat.windowsEscape(asset.getAssetName()) + ".xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		return wb;
	}
	
	public static class AssetAiringReportInfo
	{
		private String deviceId;
		private String deviceName;		
		private Long numDeviceAirings;
		private String deviceAiringsLength;
		private Long numDisplayAirings;
		private String displayAiringsLength;
		private HashMap<String, String> deviceMetadata;
		public static ThreadLocal<TreeMap<String, String>> assetMetadata;
		public static ThreadLocal<List<String>> deviceMetadataColumns;
		
		/**
		 * @return Returns the deviceId.
		 */
		public String getDeviceId() {
			return deviceId;
		}
		

		/**
		 * @param deviceId The deviceId to set.
		 */
		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
		

		/**
		 * @return Returns the deviceName.
		 */
		public String getDeviceName() {
			return deviceName;
		}
		

		/**
		 * @param deviceName The deviceName to set.
		 */
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}


		/**
		 * @return the numDeviceAirings
		 */
		public Long getNumDeviceAirings() {
			return numDeviceAirings;
		}


		/**
		 * @param numDeviceAirings the numDeviceAirings to set
		 */
		public void setNumDeviceAirings(Long numDeviceAirings) {
			this.numDeviceAirings = numDeviceAirings;
		}


		/**
		 * @return the deviceAiringsLength
		 */
		public String getDeviceAiringsLength() {
			return deviceAiringsLength;
		}


		/**
		 * @param deviceAiringsLength the deviceAiringsLength to set
		 */
		public void setDeviceAiringsLength(String deviceAiringsLength) {
			this.deviceAiringsLength = deviceAiringsLength;
		}


		/**
		 * @return the numDisplayAirings
		 */
		public Long getNumDisplayAirings() {
			return numDisplayAirings;
		}


		/**
		 * @param numDisplayAirings the numDisplayAirings to set
		 */
		public void setNumDisplayAirings(Long numDisplayAirings) {
			this.numDisplayAirings = numDisplayAirings;
		}


		/**
		 * @return the displayAiringsLength
		 */
		public String getDisplayAiringsLength() {
			return displayAiringsLength;
		}


		/**
		 * @param displayAiringsLength the displayAiringsLength to set
		 */
		public void setDisplayAiringsLength(String displayAiringsLength) {
			this.displayAiringsLength = displayAiringsLength;
		}


		public HashMap<String, String> getDeviceMetadata() {
			return deviceMetadata;
		}


		public void setDeviceMetadata(HashMap<String, String> deviceMetadata) {
			this.deviceMetadata = deviceMetadata;
		}
	}
}
