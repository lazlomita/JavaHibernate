package com.kuvata.kmf.reports;

import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.struts.util.LabelValueBean;
import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.ContentScheduleEvent;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.PlaybackEvent;
import com.kuvata.kmf.PlaybackEventSummary;
import com.kuvata.kmf.util.Reformat;

public class DevicePlaybackReport extends Report{

	public DevicePlaybackReport(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		super(selections, startDate, endDate, showResultsByDeviceGroup, showDetails, detailsFilter, showZeros, orderBy, reverseOrder);
	}
	
	public List<PlaybackEventInfo> getReportData() throws Exception{
		
		String user = KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		logger.info("Started running report of type " + this.getClass().getName() + " by user " + user);
		
		ReportEntitySelectionType assetSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Asset.class);
		ReportEntitySelectionType deviceSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		
		Device device = Device.getDevice(Long.parseLong(deviceSelection.getSelectedIds()));
		
		// Determine which table to generate the reports off of
		String reportsSourceTable = Constants.REPORTS_SOURCE_TABLE_DEFAULT;
		try{
			reportsSourceTable = KuvataConfig.getPropertyValue( Constants.REPORTS_SOURCE_TABLE );
		}catch(KmfException e){
			logger.info("Could not locate property: "+ Constants.REPORTS_SOURCE_TABLE +". Using default: "+ Constants.REPORTS_SOURCE_TABLE_DEFAULT);
		}
		
		List<PlaybackEventInfo> playbackEventInfos = new LinkedList<PlaybackEventInfo>();
		
		Date startDt = UI_DATE_FORMAT.parse(startDate);
		Date endDt = UI_DATE_FORMAT.parse(endDate);
		
		if(device != null){
			// We will use the detailed playback_event table even if the reportsSourceTable is set to use the summary table.
			if( reportsSourceTable.equalsIgnoreCase( PlaybackEvent.class.getSimpleName() ) || reportsSourceTable.equalsIgnoreCase(PlaybackEventSummary.class.getSimpleName()) )
			{
				List<PlaybackEvent> playbackEvents = PlaybackEvent.getPlaybackEvents( device, assetSelection.getSelectionId(), startDt, endDt );
				for( Iterator<PlaybackEvent> i=playbackEvents.iterator(); i.hasNext(); )
				{
					PlaybackEvent playbackEvent = i.next();
					PlaybackEventInfo info = new PlaybackEventInfo();
					
					String strStartDatetime = playbackEvent.getStartDatetime() != null ? UI_DATE_FORMAT.format( playbackEvent.getStartDatetime() ) : ""; 
					String strEndDatetime = playbackEvent.getEndDatetime() != null ? UI_DATE_FORMAT.format( playbackEvent.getEndDatetime() ) : ""; 
					info.setStartDatetime( strStartDatetime );
					info.setEndDatetime( strEndDatetime );
					info.setAssetName( playbackEvent.getAssetName() );
					info.setDisplayareaName( playbackEvent.getDisplayareaName() );
					info.setLayoutName( playbackEvent.getLayoutName() );
					info.setPlaylistName( playbackEvent.getPlaylistName() );
					info.setSegmentName( playbackEvent.getSegmentName() );
					info.setOrigin( playbackEvent.getOrigin() );
					info.setClickCount( playbackEvent.getClickCount().toString() );
					
					// Subtract the number of exceptions from the number of total displays to get the number of displays it aired on
					int numDisplays = playbackEvent.getDisplaysCount() != null ? playbackEvent.getDisplaysCount().intValue() : 0;
					int numExceptions = playbackEvent.getDisplayExceptionsCount() != null ? playbackEvent.getDisplayExceptionsCount().intValue() : 0;
					info.setNumDisplays( String.valueOf( numDisplays - numExceptions ) );								
					playbackEventInfos.add( info );
				}
			}
			else if( reportsSourceTable.equalsIgnoreCase( ContentScheduleEvent.class.getSimpleName() ) )
			{
				List<ContentScheduleEvent> contentScheduleEvents = ContentScheduleEvent.getContentScheduleEvents( device, assetSelection.getSelectionId(), startDt, endDt );
				
				for( Iterator<ContentScheduleEvent> i=contentScheduleEvents.iterator(); i.hasNext(); )
				{
					ContentScheduleEvent contentScheduleEvent = i.next();
					PlaybackEventInfo info = new PlaybackEventInfo();
					
					String strStartDatetime = contentScheduleEvent.getStartDatetime() != null ? UI_DATE_FORMAT.format( contentScheduleEvent.getStartDatetime() ) : ""; 
					String strEndDatetime = contentScheduleEvent.getEndDatetime() != null ? UI_DATE_FORMAT.format( contentScheduleEvent.getEndDatetime() ) : ""; 
					info.setStartDatetime( strStartDatetime );
					info.setEndDatetime( strEndDatetime );
					info.setAssetName( contentScheduleEvent.getAssetName() );
					info.setDisplayareaName( contentScheduleEvent.getDisplayareaName() );
					info.setLayoutName( contentScheduleEvent.getLayoutName() );
					info.setPlaylistName( contentScheduleEvent.getPlaylistName() );
					info.setSegmentName( contentScheduleEvent.getSegmentName() );
					info.setOrigin( contentScheduleEvent.getOrigin() );
					info.setNumDisplays( contentScheduleEvent.getDisplaysCount() != null ? contentScheduleEvent.getDisplaysCount().toString() : "0" );								
					playbackEventInfos.add( info );
				}
			}
		}
		
		logger.info("Finished running report of type " + this.getClass().getName() + " by user " + user);
		
		this.report = playbackEventInfos;
		return playbackEventInfos;
	}
	
	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws Exception
	{
		ReportEntitySelectionType deviceSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		ReportEntitySelectionType assetSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Asset.class);
		Device device = Device.getDevice(Long.parseLong(deviceSelection.getSelectedIds()));
		
		String TIME = "Time";
		String ASSET = "Asset";
		String DISPLAY_AREA = "Display Area";	
		String LAYOUT = "Layout";
		String PLAYLIST = "Playlist";
		String SEGMENT = "Schedule";
		String ORIGIN = "Origin";
		String DISPLAY_AIRINGS = "Display Airings";
		String CLICK_COUNT = "Click Count";
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Device Playback Report");
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
		cell0.setCellValue("Device Playback Report");
		cell0.setCellStyle( styleTitle );		
		
		// Spacer
		sheet.createRow( rowCounter++ );
		
		HSSFRow row1 = sheet.createRow( rowCounter++ );
		HSSFCell row1Cell0 = row1.createCell( (short)0 );
		row1Cell0.setCellValue( "Device:" );
		row1Cell0.setCellStyle( styleAlignLeft );		
		row1.createCell( (short)1 ).setCellValue( device != null ? device.getDeviceName() : "");
		
		// Metadata rows
		if(device != null){
			for( Iterator i=device.getCustomMetadata(false).iterator(); i.hasNext(); )
			{
				LabelValueBean metadataInfo = (LabelValueBean)i.next();
				HSSFRow row2 = sheet.createRow( rowCounter++ );
				HSSFCell metadataCell = row2.createCell( (short)0 );
				metadataCell.setCellValue( metadataInfo.getLabel() +":" );
				metadataCell.setCellStyle( styleAlignLeft );		
				row2.createCell( (short)1 ).setCellValue( metadataInfo.getValue() );			
			}
		}
		
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Assets:", assetSelection.getSelectionNames(), rowCounter);
		
		HSSFRow row4 = sheet.createRow( rowCounter++ );
		HSSFCell row4Cell0 = row4.createCell( (short)0 );
		row4Cell0.setCellValue( "Date Range:" );
		row4Cell0.setCellStyle( styleAlignLeft );
		row4.createCell( (short)1 ).setCellValue( startDate +" - "+ endDate);
		
		// Spacer
		sheet.createRow( rowCounter++ );			
	
		/*
		 * Report column headers
		 */
		columnCounter = 0;
		HSSFRow headerRow = sheet.createRow( rowCounter++ );
		HSSFCell cell1 = headerRow.createCell( columnCounter++ );
		cell1.setCellValue( TIME );
		cell1.setCellStyle( styleHeaderAlignLeft );
		int timeMaxLength = TIME.length();
		HSSFCell cell2 = headerRow.createCell( columnCounter++ );
		cell2.setCellValue( ASSET );
		cell2.setCellStyle( styleHeaderAlignLeft );
		int assetMaxLength = ASSET.length();
		HSSFCell cell3 = headerRow.createCell( columnCounter++ );
		cell3.setCellValue( DISPLAY_AREA );
		cell3.setCellStyle( styleHeaderAlignLeft );
		int displayareaMaxLength = DISPLAY_AREA.length();
		HSSFCell cell4 = headerRow.createCell( columnCounter++ );
		cell4.setCellValue( LAYOUT );
		cell4.setCellStyle( styleHeaderAlignLeft );
		int layoutMaxLength = LAYOUT.length();
		HSSFCell cell5 = headerRow.createCell( columnCounter++ );
		cell5.setCellValue( PLAYLIST );
		cell5.setCellStyle( styleHeaderAlignLeft );
		int playlistMaxLength = PLAYLIST.length();
		HSSFCell cell6 = headerRow.createCell( columnCounter++ );
		cell6.setCellValue( SEGMENT );
		cell6.setCellStyle( styleHeaderAlignLeft );
		int segmentMaxLength = SEGMENT.length();
		HSSFCell cell7 = headerRow.createCell( columnCounter++ );
		cell7.setCellValue( ORIGIN );
		cell7.setCellStyle( styleHeaderAlignLeft );
		int originMaxLength = ORIGIN.length();
		HSSFCell cell8 = headerRow.createCell( columnCounter++ );
		cell8.setCellValue( DISPLAY_AIRINGS );
		cell8.setCellStyle( styleHeaderAlignLeft );
		int displayAiringsMaxLength = DISPLAY_AIRINGS.length();
		HSSFCell cell9 = headerRow.createCell( columnCounter++ );
		cell9.setCellValue( CLICK_COUNT );
		cell9.setCellStyle( styleHeaderAlignLeft );
		int clickCountMaxLength = CLICK_COUNT.length();		
		
		/*
		 * Report rows
		 */						
		int rowCount = 0;
		Session session = HibernateSession.currentSession();
		for( Iterator<PlaybackEventInfo> i=report.iterator(); i.hasNext(); )
		{
			PlaybackEventInfo info = i.next();
			String startDatetime = info.getStartDatetime();
			String endDatetime = info.getEndDatetime();
			String assetName = info.getAssetName();
			String displayareaName = info.getDisplayareaName();
			String layoutName = info.getLayoutName();
			String playlistName = info.getPlaylistName();
			String segmentName = info.getSegmentName();
			String origin = info.getOrigin();
			String displayAirings = info.getNumDisplays();
			String clickCount = info.getClickCount();
			String time = startDatetime +" - "+ endDatetime;
			
			HSSFRow row = sheet.createRow( rowCounter++ );
			columnCounter = 0;			
	
			HSSFCell reportCell0 = row.createCell( columnCounter++ );
			reportCell0.setCellValue( time );
			reportCell0.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0LeftWrap : styleRow1LeftWrap );
			
			HSSFCell reportCell1 = row.createCell( columnCounter++ );
			reportCell1.setCellValue( assetName );
			reportCell1.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell2 = row.createCell( columnCounter++ );
			reportCell2.setCellValue( displayareaName );
			reportCell2.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell3 = row.createCell( columnCounter++ );
			reportCell3.setCellValue( layoutName );
			reportCell3.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell4 = row.createCell( columnCounter++ );
			reportCell4.setCellValue( playlistName );
			reportCell4.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell5 = row.createCell( columnCounter++ );
			reportCell5.setCellValue( segmentName );
			reportCell5.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell6 = row.createCell( columnCounter++ );
			reportCell6.setCellValue( origin );
			reportCell6.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell7 = row.createCell( columnCounter++ );
			reportCell7.setCellValue( displayAirings );
			reportCell7.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			HSSFCell reportCell8 = row.createCell( columnCounter++ );
			reportCell8.setCellValue( clickCount );
			reportCell8.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
													
			// Update the maxLength values if necessary
			if( time.length() > timeMaxLength ){
				timeMaxLength = time.length();
			}
			if( assetName.length() > assetMaxLength ){
				assetMaxLength = assetName.length();
			}
			if( displayareaName.length() > displayareaMaxLength ){
				displayareaMaxLength = displayareaName.length();
			}
			if( layoutName.length() > layoutMaxLength ){
				layoutMaxLength = layoutName.length();
			}
			if( playlistName != null && playlistName.length() > playlistMaxLength ){
				playlistMaxLength = playlistName.length();
			}
			if( segmentName != null && segmentName.length() > segmentMaxLength ){
				segmentMaxLength = segmentName.length();
			}
			if( origin != null && origin.length() > originMaxLength ){
				originMaxLength = origin.length();
			}
			if( displayAirings != null && displayAirings.length() > displayAiringsMaxLength ){
				displayAiringsMaxLength = displayAirings.length();
			}
			if( clickCount != null && clickCount.length() > clickCountMaxLength ){
				clickCountMaxLength = clickCount.length();
			}
			rowCount++;
		}

		// Set each column width according to the maxLength of each 
		sheet.setColumnWidth((short)0, (short) ( 25 * 256 ));
		sheet.setColumnWidth((short)1, (short) ( assetMaxLength * 256 ));
		sheet.setColumnWidth((short)2, (short) ( displayareaMaxLength * 256 ));
		sheet.setColumnWidth((short)3, (short) ( layoutMaxLength * 256 ));
		sheet.setColumnWidth((short)4, (short) ( playlistMaxLength * 256 ));
		sheet.setColumnWidth((short)5, (short) ( segmentMaxLength * 256 ));		
		sheet.setColumnWidth((short)6, (short) ( originMaxLength * 256 ));		
		sheet.setColumnWidth((short)7, (short) ( displayAiringsMaxLength * 256 ));
		sheet.setColumnWidth((short)8, (short) ( clickCountMaxLength * 256 ));
		
		// Landscape mode
		sheet.getPrintSetup().setLandscape( true );
		sheet.setFitToPage( true );
		
		if(response != null){
			response.setHeader("Content-Disposition","attachment; filename=\"DevicePlaybackReport-"+startDate.replace("/", "-")+"-"+endDate.replace("/", "-")+"-"+Reformat.windowsEscape(device.getDeviceName())+".xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		
		return wb;
	}
	
	public static class PlaybackEventInfo
	{
		private String startDatetime;
		private String endDatetime;
		private String assetName;
		private String displayareaName;
		private String layoutName;
		private String playlistName;
		private String segmentName;
		private String origin;
		private String numDisplays;
		private String clickCount;

		/**
		 * @return the startDatetime
		 */
		public String getStartDatetime() {
			return startDatetime;
		}
		/**
		 * @param startDatetime the startDatetime to set
		 */
		public void setStartDatetime(String startDatetime) {
			this.startDatetime = startDatetime;
		}
		/**
		 * @return the endDatetime
		 */
		public String getEndDatetime() {
			return endDatetime;
		}
		/**
		 * @param endDatetime the endDatetime to set
		 */
		public void setEndDatetime(String endDatetime) {
			this.endDatetime = endDatetime;
		}
		/**
		 * @return the assetName
		 */
		public String getAssetName() {
			return assetName;
		}
		/**
		 * @param assetName the assetName to set
		 */
		public void setAssetName(String assetName) {
			this.assetName = assetName;
		}
		/**
		 * @return the displayareaName
		 */
		public String getDisplayareaName() {
			return displayareaName;
		}
		/**
		 * @param displayareaName the displayareaName to set
		 */
		public void setDisplayareaName(String displayareaName) {
			this.displayareaName = displayareaName;
		}
		/**
		 * @return the layoutName
		 */
		public String getLayoutName() {
			return layoutName;
		}
		/**
		 * @param layoutName the layoutName to set
		 */
		public void setLayoutName(String layoutName) {
			this.layoutName = layoutName;
		}
		/**
		 * @return the playlistName
		 */
		public String getPlaylistName() {
			return playlistName;
		}
		/**
		 * @param playlistName the playlistName to set
		 */
		public void setPlaylistName(String playlistName) {
			this.playlistName = playlistName;
		}
		/**
		 * @return the segmentName
		 */
		public String getSegmentName() {
			return segmentName;
		}
		/**
		 * @param segmentName the segmentName to set
		 */
		public void setSegmentName(String segmentName) {
			this.segmentName = segmentName;
		}
		/**
		 * @return the origin
		 */
		public String getOrigin() {
			return origin;
		}
		/**
		 * @param origin the origin to set
		 */
		public void setOrigin(String origin) {
			this.origin = origin;
		}
		/**
		 * @return the numDisplays
		 */
		public String getNumDisplays() {
			return numDisplays;
		}
		/**
		 * @param numDisplays the numDisplays to set
		 */
		public void setNumDisplays(String numDisplays) {
			this.numDisplays = numDisplays;
		}
		public String getClickCount() {
			return clickCount;
		}
		public void setClickCount(String clickCount) {
			this.clickCount = clickCount;
		}
	}
}
