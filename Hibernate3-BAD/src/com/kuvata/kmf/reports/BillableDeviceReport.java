package com.kuvata.kmf.reports;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import org.hibernate.Query;

import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.util.Reformat;

public class BillableDeviceReport extends Report{

	public BillableDeviceReport(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		super(selections, startDate, endDate, showResultsByDeviceGroup, showDetails, detailsFilter, showZeros, orderBy, reverseOrder);
	}
	
	public List getReportData() throws Exception{
		
		String user = KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		logger.info("Started running report of type " + this.getClass().getName() + " by user " + user);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		Date startDate = dateFormat.parse( this.startDate );
		Date endDate = dateFormat.parse( this.endDate );
		
		ReportEntitySelectionType deviceSelections = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		
		List reportRows = new ArrayList();
		
		// Overview view
		if(showDetails == null || showDetails == false){
			String hql = "SELECT COUNT(*) FROM Device WHERE replacedBy.deviceId IS NULL";
			if( deviceSelections.getSelectionId() != null ) {
				hql += " AND deviceId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) ";
			}
			Query q = HibernateSession.currentSession().createQuery(hql);
			if( deviceSelections.getSelectionId() != null){
				q.setParameter("deviceSelectionId", deviceSelections.getSelectionId());
			}
			Long devicesCount = (Long)q.iterate().next();
			
			hql = "SELECT COUNT(*) FROM Device as d WHERE d.replacedBy.deviceId IS NULL AND ( (d.readableBillableStartDt < :startDate AND (d.readableBillableEndDt IS NULL OR d.readableBillableEndDt > :startDate)) OR " +
													"(d.readableBillableStartDt >= :startDate AND d.readableBillableStartDt < :endDate) )";
			if( deviceSelections.getSelectionId() != null ) {
				hql += " AND d.deviceId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) ";
			}
			q = HibernateSession.currentSession().createQuery(hql).setParameter("startDate", startDate).setParameter("endDate", endDate);
			if( deviceSelections.getSelectionId() != null){
				q.setParameter("deviceSelectionId", deviceSelections.getSelectionId());
			}
			Long billableCount = (Long)q.iterate().next();
			
			BillableDeviceInfo bdi = new BillableDeviceInfo();
			bdi.billableDevices = billableCount;
			bdi.nonBillableDevices = devicesCount - billableCount;
			reportRows.add(bdi);
		}else{
			if(detailsFilter.equals("Billable")){
				String hql = "SELECT d.deviceName, d.readableBillingStatus, d.readableBillableStartDt, d.readableBillableEndDt, d.osVersion FROM Device as d WHERE d.replacedBy.deviceId IS NULL " + 
							"AND ( (d.readableBillableStartDt < :startDate AND (d.readableBillableEndDt IS NULL OR d.readableBillableEndDt > :startDate)) OR " +
								"(d.readableBillableStartDt >= :startDate AND d.readableBillableStartDt < :endDate) )";
				if( deviceSelections.getSelectionId() != null ){
					hql += " AND d.deviceId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) ";
				}
				hql += "ORDER BY d.readableBillableStartDt DESC";
				
				Query q = HibernateSession.currentSession().createQuery(hql).setParameter("startDate", startDate).setParameter("endDate", endDate);
				if( deviceSelections.getSelectionId() != null){
					q.setParameter("deviceSelectionId", deviceSelections.getSelectionId());
				}
				
				List<Object[]> l = q.list();
				for(Object[] o : l){
					BillableDeviceInfo bdi = new BillableDeviceInfo();
					bdi.deviceName = (String)o[0];
					bdi.licenseStatus = (String)o[1];
					bdi.billableStartDt = (Date)o[2];
					bdi.billableEndDt = (Date)o[3];
					bdi.type = (String)o[4] != null && ((String)o[4]).startsWith(Constants.ANDROID) ? Constants.ANDROID : "Linux";
					reportRows.add(bdi);
				}
			}else{
				String hql = "SELECT d.deviceName, d.readableBillingStatus, d.readableBillableStartDt, d.readableBillableEndDt, d.osVersion FROM Device as d WHERE d.replacedBy.deviceId IS NULL " + 
				"AND ( d.readableBillableStartDt IS NULL OR d.readableBillableEndDt <= :startDate OR d.readableBillableStartDt >= :endDate )";
				if( deviceSelections.getSelectionId() != null ){
					hql += " AND d.deviceId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) ";
				}
				hql += "ORDER BY d.readableBillableStartDt DESC";
				
				Query q = HibernateSession.currentSession().createQuery(hql).setParameter("startDate", startDate).setParameter("endDate", endDate);
				if( deviceSelections.getSelectionId() != null){
					q.setParameter("deviceSelectionId", deviceSelections.getSelectionId());
				}
				
				List<Object[]> l = q.list();
				for(Object[] o : l){
					BillableDeviceInfo bdi = new BillableDeviceInfo();
					bdi.deviceName = (String)o[0];
					bdi.licenseStatus = (String)o[1];
					bdi.billableStartDt = (Date)o[2];
					bdi.billableEndDt = (Date)o[3];
					bdi.type = (String)o[4] != null && ((String)o[4]).startsWith(Constants.ANDROID) ? Constants.ANDROID : "Linux";
					reportRows.add(bdi);
				}
			}
		}
		
		this.report = reportRows;
		
		logger.info("Finished running report of type " + this.getClass().getName() + " by user " + user);
		
		return reportRows;
	}
	
	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws Exception
	{
		String selectedNames = selections[0].getSelectionNames();
		
		String[] columns;
		
		if(showDetails == null || showDetails == false){
			columns = new String[]{"Billable Devices", "Non-Billable Devices"};
		}else{
			columns = new String[]{"Device Name", "License Status", "Type", "Billable Start Date", "Billable End Date"};
		}
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Billable Device Report");
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
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Devices:", selectedNames, rowCounter);
		
		if(showDetails != null && showDetails){
			HSSFRow row2 = sheet.createRow( rowCounter++ );
			HSSFCell row2Cell0 = row2.createCell( (short)0 );
			row2Cell0.setCellValue( "Filter By:" );
			row2Cell0.setCellStyle( styleAlignLeft );
			row2.createCell( (short)1 ).setCellValue( detailsFilter );
		}
		
		HSSFRow row3 = sheet.createRow( rowCounter++ );
		HSSFCell row3Cell0 = row3.createCell( (short)0 );
		row3Cell0.setCellValue( "Date Range:" );
		row3Cell0.setCellStyle( styleAlignLeft );
		row3.createCell( (short)1 ).setCellValue( startDate +" - "+ endDate );
		
		if(showDetails != null && showDetails){
			HSSFRow row4 = sheet.createRow( rowCounter++ );
			HSSFCell row4Cell0 = row4.createCell( (short)0 );
			row4Cell0.setCellValue( "Total Devices:" );
			row4Cell0.setCellStyle( styleAlignLeft );
			row4.createCell( (short)1 ).setCellValue( report.size() );
		}
				
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
		
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
				
		/*
		 * Report rows
		 */		
		int rowCount = 0;
		for( Iterator<BillableDeviceInfo> i=this.report.iterator(); i.hasNext(); )
		{
			BillableDeviceInfo info = i.next();
			
			// Create row array
			String[] rowData = null;
			if(showDetails == null || showDetails == false){
				rowData = new String[]{info.billableDevices.toString(), info.nonBillableDevices.toString()};
			}else{
				rowData = new String[]{info.deviceName, info.licenseStatus, info.type, info.billableStartDt != null ? sdf.format(info.billableStartDt) : null, info.billableEndDt != null ? sdf.format(info.billableEndDt) : null};
			}
			
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
			response.setHeader("Content-Disposition","attachment; filename=\"BillableDeviceReport-" + Reformat.windowsEscape(selectedNames) + ".xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		return wb;
	}
	
	public class BillableDeviceInfo{
		private Long billableDevices;
		private Long nonBillableDevices;
		private String deviceName;
		private String licenseStatus;
		private Date billableStartDt;
		private Date billableEndDt;
		private String type;
		
		public Long getBillableDevices() {
			return billableDevices;
		}
		public void setBillableDevices(Long billableDevices) {
			this.billableDevices = billableDevices;
		}
		public Long getNonBillableDevices() {
			return nonBillableDevices;
		}
		public void setNonBillableDevices(Long nonBillableDevices) {
			this.nonBillableDevices = nonBillableDevices;
		}
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		public String getLicenseStatus() {
			return licenseStatus;
		}
		public void setLicenseStatus(String licenseStatus) {
			this.licenseStatus = licenseStatus;
		}
		public Date getBillableStartDt() {
			return billableStartDt;
		}
		public void setBillableStartDt(Date billableStartDt) {
			this.billableStartDt = billableStartDt;
		}
		public Date getBillableEndDt() {
			return billableEndDt;
		}
		public void setBillableEndDt(Date billableEndDt) {
			this.billableEndDt = billableEndDt;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
}