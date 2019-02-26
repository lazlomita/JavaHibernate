package com.kuvata.kmf.reports;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.struts.util.LabelValueBean;
import org.hibernate.Query;
import org.hibernate.Session;

import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.attr.MetadataInfo;
import com.kuvata.kmf.comparator.BeanPropertyComparator;

public class DeviceMetadataReport extends Report {
	ReportEntitySelectionType entitySelection;
	private List<String> selectedMetadataIds;
	
	public DeviceMetadataReport(String[] selectedIds, Class[] classes, String selectedMetadataIds, String orderBy) {
		super(selectedIds, classes, null, null, orderBy);
		
		if (selectedMetadataIds.length() > 0)
			this.selectedMetadataIds = new ArrayList<String>(Arrays.asList(selectedMetadataIds.split(",")));
		
		entitySelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
	}

	public List<MetadataInfo> getReportColumns() throws Exception {
		return AttrDefinition.getAttrDefinitionMetadata(Device.class.getName(), selectedMetadataIds);
	}
	
	public List<DeviceMetadataReportInfo> getReportData() throws Exception {
		String hql = "SELECT device " 
				  + "FROM Device as device "
				  + "WHERE device.deviceId IN (  SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) "
				  + "ORDER BY UPPER(device.deviceName)";	
			
		Session session = HibernateSession.currentSession();
		Query q = session.createQuery( hql );
		List<Device> results = q.setParameter("deviceSelectionId", entitySelection.getSelectionId()).list();
		
		List<DeviceMetadataReportInfo> reportList = new ArrayList<DeviceMetadataReportInfo>();
		for( Iterator<Device> i=results.iterator(); i.hasNext(); )
		{
			Device d = i.next();
			DeviceMetadataReportInfo info = new DeviceMetadataReportInfo();
			info.deviceName = d.getDeviceName();
			info.deviceId = d.getDeviceId().toString();
			info.metadata = d.getCustomMetadata(true, selectedMetadataIds);
			reportList.add(info);
		}
		
		// Finally, sort the collection appropriately
		if(orderBy != null && orderBy.length() > 0){
			if( orderBy.equalsIgnoreCase("deviceName") ){
				BeanPropertyComparator comparator = new BeanPropertyComparator( "deviceName" );
				Collections.sort( reportList, comparator );
			}else if( orderBy.equalsIgnoreCase("deviceName DESC") ){
				BeanPropertyComparator comparator = new BeanPropertyComparator( "deviceName" );
				Collections.sort( reportList, Collections.reverseOrder( comparator ) );
			}
		}

		this.report = reportList;
		return reportList;
	}

	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws Exception {
		String DEVICE_NAME = "Device Name";
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Device Metadata Report");
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
		HSSFRow row0 = sheet.createRow( rowCounter++ );
		row0.setHeight((short)0x249);
		HSSFCell cell0 = row0.createCell( (short)0 );
		cell0.setCellValue("Device Metadata Report");
		cell0.setCellStyle( styleTitle );
		
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
		
		List<Integer> maxLength = new ArrayList<Integer>();
		List<MetadataInfo> metadata = AttrDefinition.getAttrDefinitionMetadata(Device.class.getName(), selectedMetadataIds);
		for (MetadataInfo m : metadata) {
			HSSFCell cell = headerRow.createCell( columnCounter++ );
			cell.setCellValue( m.getName() );
			cell.setCellStyle( styleHeaderAlignLeft );
			maxLength.add(m.getName().length());
		}
		
		Integer[] listMaxLength = new Integer[maxLength.size()];
		listMaxLength = maxLength.toArray(listMaxLength);
		
		/*
		 * Report rows
		 */		
		int rowCount = 0;
		for( Iterator i=report.iterator(); i.hasNext(); )
		{
			DeviceMetadataReportInfo info = (DeviceMetadataReportInfo)i.next();
			HSSFRow row = sheet.createRow( rowCounter++ );
			columnCounter = 0;			
			
			HSSFCell assetNameCell = row.createCell( columnCounter++ );
			assetNameCell.setCellValue( info.getDeviceName() );
			assetNameCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
			int metadataCounter = 0;
			for (LabelValueBean b : info.getMetadata()) {
				HSSFCell cell = row.createCell( columnCounter++ );
				cell.setCellValue( b.getValue() );
				cell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );
				if (listMaxLength[metadataCounter] < b.getValue().length())
					listMaxLength[metadataCounter] = b.getValue().length();
					
				metadataCounter++;
			}
			
			// Update the maxLength values if necessary
			if( info.getDeviceName().length() > deviceNameMaxLength ){
				deviceNameMaxLength = info.getDeviceName().length();
			}
			
			rowCount++;
		}
		
		// Set each column width according to the maxLength of each 
		int columnWidthCounter = 0;
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( deviceNameMaxLength * 260 ));
		
		for (int i=0; i<listMaxLength.length; i++) {
			sheet.setColumnWidth((short)columnWidthCounter++, (short) ( listMaxLength[i] * 260 ));
		}
		
		// Landscape mode
		sheet.getPrintSetup().setLandscape( true );
		sheet.setFitToPage( true );
		
		if(response != null){
			response.setHeader("Content-Disposition","attachment; filename=\"DeviceMetadataReport.xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		
		return wb;
	}
	
	public void doExportToCsv(HttpServletResponse response) throws Exception {
		StringBuffer sb = new StringBuffer();
		String columns = "";
		String row = "";

		// Write out the column headers
		sb.append("Device Name");
		List<MetadataInfo> metadata = AttrDefinition.getAttrDefinitionMetadata(Device.class.getName(), selectedMetadataIds);
		for (MetadataInfo m : metadata) {
			columns += "," + m.getName();
		}
		sb.append(columns + "\n");
		
		for( Object o : report )
		{
			DeviceMetadataReportInfo info = (DeviceMetadataReportInfo)o;
			row = info.getDeviceName();
			for (LabelValueBean b : info.getMetadata()) {
				row += "," + b.getValue();
			}
			sb.append(row + "\n");
			row = "";
		}
		
		response.setHeader("Content-Disposition","attachment; filename=\"DeviceMetadatReport.csv\"");
		response.setContentType(new MimeType("application","octet-stream").toString());
		
		// Write out this workbook to the response OutputStream
		OutputStream out = response.getOutputStream();
		out.write(sb.toString().getBytes());
		out.flush();
		out.close();
	}
	
	public String getSelectedDeviceNames() {
		return entitySelection.getSelectionNames();
	}
	
	public class DeviceMetadataReportInfo {
		private String deviceId;
		private String deviceName;
		private List<LabelValueBean> metadata;
		
		public String getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		public List<LabelValueBean> getMetadata() {
			return metadata;
		}
		public void setMetadata(List<LabelValueBean> metadata) {
			this.metadata = metadata;
		}
	}
}
