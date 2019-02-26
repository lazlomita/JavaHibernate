package com.kuvata.kmf.reports;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.hibernate.Session;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.Grp;
import com.kuvata.kmf.GrpGrpMember;
import com.kuvata.kmf.GrpMember;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.PlaybackEventSummary;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.permissions.FilterManager;
import com.kuvata.kmf.permissions.FilterType;
import com.kuvata.kmf.util.Reformat;
import com.kuvata.kmm.KMMServlet;

public class AssetSummaryReport extends Report{
	
	public AssetSummaryReport(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		super(selections, startDate, endDate, showResultsByDeviceGroup, showDetails, detailsFilter, showZeros, orderBy, reverseOrder);
	}
	
	public List<AssetSummaryReportInfo> getReportData() throws SQLException, ParseException
	{
		String user = KmfSession.getKmfSession() != null ? KmfSession.getKmfSession().getAppUsername() : "Auto";
		logger.info("Started running report of type " + this.getClass().getName() + " by user " + user);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
		Date startDate = dateFormat.parse( this.startDate );
		Date endDate = dateFormat.parse( this.endDate );
		boolean showResultsByDeviceGroup = this.showResultsByDeviceGroup != null ? this.showResultsByDeviceGroup.booleanValue() : false;
		
		ReportEntitySelectionType assetSelections = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Asset.class);
		ReportEntitySelectionType deviceSelections = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		
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
		
		// Get the totals number of times the given assets aired on the given devices
		String sql = "";
		if( showResultsByDeviceGroup )
		{	
			sql = "SELECT /*+ ORDERED INDEX (a, pes_combined) */ a.asset_id, a.asset_name, COUNT(*) as numDeviceAirings, "
				+ "SUM(a.asset_length) as deviceAiringsLength, COUNT(DISTINCT(a.device_id)) as deviceId, "
				+ "(SUM(a.displays_count) - SUM(a.display_exceptions_count)) as numDisplayAirings, "
				+ "(SUM((a.displays_count - a.display_exceptions_count) * a.asset_length)) as displayAiringsLength, d.grp_id, d.grp_name "
				+ "FROM "+ reportsSourceTable +" a, device_grp_member b, grp_member c, grp d "
				+ "WHERE a.device_id = b.device_id "
				+ "AND b.device_grp_member_id = c.grp_member_id "
				+ "AND c.grp_id = d.grp_id "
				+ "AND a.start_datetime >= :startDatetime "
				+ "AND a.start_datetime < :endDatetime ";
			if( assetSelections.getSelectionId() != null ) {
				sql += "AND a.asset_id IN ( SELECT entity_id FROM selected_entities WHERE selection_id = :assetSelectionId ) ";			  
			}
			if( deviceSelections.getSelectionId() != null ) {
				sql += "AND a.device_id IN ( SELECT entity_id FROM selected_entities WHERE selection_id = :deviceSelectionId ) ";
			}
			sql += "GROUP BY a.asset_id, a.asset_name, d.grp_id, d.grp_name";			
		}
		else
		{
			sql = "SELECT /*+ ORDERED INDEX (a, pes_combined) */ a.asset_id, a.asset_name, COUNT(*) as numDeviceAirings, "
				+ "SUM(a.asset_length) as deviceAiringsLength, COUNT( DISTINCT(a.device_id)) as deviceId, "
				+ "(SUM(a.displays_count) - SUM(a.display_exceptions_count)) as numDisplayAirings, "
				+ "(SUM((a.displays_count - a.display_exceptions_count) * a.asset_length)) as displayAiringsLength "
				+ "FROM "+ reportsSourceTable +" a "
				+ "WHERE a.start_datetime >= :startDatetime "
				+ "AND a.start_datetime < :endDatetime ";
			if( assetSelections.getSelectionId() != null ) {
				sql += "AND a.asset_id IN (  SELECT entity_id FROM selected_entities WHERE selection_id = :assetSelectionId ) ";
			}
			if( deviceSelections.getSelectionId() != null ) {
				sql += "AND a.device_id IN (  SELECT entity_id FROM selected_entities WHERE selection_id = :deviceSelectionId ) ";
			}	
			sql += "GROUP BY a.asset_id, a.asset_name";				
		}
		
		// If we are using the summary table for this report
		if(reportsSourceTable.equalsIgnoreCase("playback_event_summary")){
			sql = sql.replace("COUNT(*) as numDeviceAirings", "SUM(a.num_airings) as numDeviceAirings");
			sql = sql.replace("SUM(a.asset_length) as deviceAiringsLength", "SUM(a.airing_length) as deviceAiringsLength");
			sql = sql.replace("(SUM((a.displays_count - a.display_exceptions_count) * a.asset_length)) as displayAiringsLength", "SUM(a.display_airing_length) as displayAiringsLength");
		}
		
		List<Long> assetIds = KMMServlet.convertCommaDelimitedStringToList( assetSelections.getSelectedIds() );
		Session session = HibernateSession.currentSession();
		Query q = session.createSQLQuery( sql )
			.setParameter("startDatetime", startDate)
			.setParameter("endDatetime", endDate);
		if( assetSelections.getSelectionId() != null ) {
			q.setParameter("assetSelectionId", assetSelections.getSelectionId());
		}
		if( deviceSelections.getSelectionId() != null ) {
			q.setParameter("deviceSelectionId", deviceSelections.getSelectionId());
		}
		List<Object[]> results = q.list();
		
		// Prepare a filtered list of device groups
		ArrayList<String> permissibleGroups = new ArrayList<String>();
		if(showResultsByDeviceGroup){
			FilterManager.enableFilter(FilterType.DEVICE_GRP_MEMBER_FILTER);
			Grp deviceGroups = Grp.getUniqueGrp(com.kuvata.kmf.Constants.DEVICE_GROUPS);
			for(GrpMember gm : deviceGroups.getGrpMembers()){
				GrpGrpMember ggm = (GrpGrpMember)gm;
				permissibleGroups.add(ggm.getChildGrp().getGrpId().toString());
			}
		}
		
		ArrayList<AssetSummaryReportInfo> totals = new ArrayList();			
		for( Iterator<Object[]> i=results.iterator(); i.hasNext(); )
		{
			Object[] row = i.next();
			AssetSummaryReportInfo info = new AssetSummaryReportInfo();
			BigDecimal assetId = (BigDecimal)row[0];
			BigDecimal numDeviceAirings = (BigDecimal)row[2];
			BigDecimal deviceAiringsLength = (BigDecimal)row[3];			
			info.setAssetId( assetId.toString() );
			info.setAssetName( (String)row[1] );			
			info.setNumDeviceAirings( numDeviceAirings.longValue() );
			
			// Convert the total length from seconds to HH:mm:ss			
			info.setDeviceAiringsLength( Reformat.formatTime( deviceAiringsLength.floatValue() ) );
			info.setNumDevices( ((BigDecimal)row[4]).longValue() );
			info.setNumDisplayAirings( row[5] != null ? ((BigDecimal)row[5]).longValue() : Long.valueOf(0) );
			info.setDisplayAiringsLength( row[6] != null ? Reformat.formatTime( ( ((BigDecimal)row[6]) ).floatValue()) : Reformat.formatTime(0) );
			
			if( showResultsByDeviceGroup ){
				String grpId = String.valueOf( row[7] );
				
				// Add this group only if the user has access to it
				if(permissibleGroups.contains(grpId)){
					info.setDeviceGroupId( grpId );
					info.setDeviceGroupName( String.valueOf( row[8] ) );
				}
			}
			
			// If the checkbox to "show details" was checked
			if( showDetails != null && showDetails.equals( Boolean.TRUE ) ){
				getDeviceDetail( info, deviceSelections.getSelectionId(), startDate, endDate );
			}			
			totals.add( info );
			
			// Remove this assetId from the hashset
			assetIds.remove( Long.valueOf(info.getAssetId()) );	
		}
		
		// If we are meant to show rows with zero playback
		if(showZeros != null && showZeros){
			// Now that we've iterated through our db resultset,
			// determine if we missed any of our original asset ids by looking as what remains in our assetIds hashset
			// and create additional assetSummaryReportInfo objects for each
			for( Iterator<Long> i=assetIds.iterator(); i.hasNext(); )
			{
				Long assetId = (Long)i.next();
				Asset a = Asset.getAsset( assetId );
				if(a != null){
					AssetSummaryReportInfo info = new AssetSummaryReportInfo();
					info.setAssetId( String.valueOf(assetId) );
					info.setAssetName( a.getAssetName() );
					info.setNumDeviceAirings( Long.valueOf(0) );
					info.setNumDisplayAirings( Long.valueOf(0) );
					info.setDeviceAiringsLength( Reformat.formatTime(0) );
					info.setDisplayAiringsLength( Reformat.formatTime(0) );
					info.setNumDevices( Long.valueOf(0) );
					info.setDeviceDetail("");
					totals.add( info );
				}
			}
		}
		
		// Finally, sort the collection appropriately
		if(orderBy != null && orderBy.length() > 0){
			if( orderBy.equalsIgnoreCase("assetName") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "assetName" );						
				Collections.sort( totals, comparator1 );		
			}else if( orderBy.equalsIgnoreCase("assetName DESC") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "assetName" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );		
			}else if( orderBy.equalsIgnoreCase("numDeviceAirings") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "numDeviceAirings" );						
				Collections.sort( totals, comparator1 );		
			}else if( orderBy.equalsIgnoreCase("numDeviceAirings DESC") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "numDeviceAirings" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );	
			}else if( orderBy.equalsIgnoreCase("numDisplayAirings") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "numDisplayAirings" );						
				Collections.sort( totals, comparator1 );		
			}else if( orderBy.equalsIgnoreCase("numDisplayAirings DESC") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "numDisplayAirings" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );	
			}else if( orderBy.equalsIgnoreCase("deviceAiringsLength") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "deviceAiringsLength" );						
				Collections.sort( totals, comparator1 );		
			}else if( orderBy.equalsIgnoreCase("deviceAiringsLength DESC") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "deviceAiringsLength" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );		
			}else if( orderBy.equalsIgnoreCase("displayAiringsLength") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "displayAiringsLength" );						
				Collections.sort( totals, comparator1 );		
			}else if( orderBy.equalsIgnoreCase("displayAiringsLength DESC") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "displayAiringsLength" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );		
			}else if( orderBy.equalsIgnoreCase("deviceId") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "numDevices" );						
				Collections.sort( totals, comparator1 );		
			}else if( orderBy.equalsIgnoreCase("deviceId DESC") ){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "numDevices" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );		
			}else if(orderBy.equalsIgnoreCase("deviceGroupName")){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "deviceGroupName" );						
				Collections.sort( totals, comparator1 );
			}else if(orderBy.equalsIgnoreCase("deviceGroupName DESC")){
				BeanPropertyComparator comparator1 = new BeanPropertyComparator( "deviceGroupName" );						
				Collections.sort( totals, Collections.reverseOrder( comparator1 ) );
			}
		}
		
		logger.info("Finished running report of type " + this.getClass().getName() + " by user " + user);
		
		this.report = totals;
		return totals;
	}
	
	private static void getDeviceDetail(AssetSummaryReportInfo info, Long deviceSelectionId, Date startDate, Date endDate)
	{
		// Determine which table to generate the reports off of
		String reportsSourceTable = Constants.REPORTS_SOURCE_TABLE_DEFAULT;
		try{
			reportsSourceTable = KuvataConfig.getPropertyValue( Constants.REPORTS_SOURCE_TABLE );
		}catch(KmfException e){
			logger.info("Could not locate property: "+ Constants.REPORTS_SOURCE_TABLE +". Using default: "+ Constants.REPORTS_SOURCE_TABLE_DEFAULT);
		}	
		
		// Get the totals number of times the given assets aired on the given devices				
		String hql = "SELECT deviceId, deviceName, COUNT(*) as numDeviceAirings, "
			  +	"SUM(assetLength) as deviceAiringsLength, (SUM(displaysCount) - SUM(displayExceptionsCount)) as numDisplayAirings, "
			  + "(SUM((displaysCount - displayExceptionsCount) * assetLength)) as displayAiringsLength "
			  + "FROM "+ reportsSourceTable +" " 
			  + "WHERE startDatetime >= :startDate "
			  + "AND startDatetime < :endDate "
			  + "AND assetId = :assetId ";
		  
		  if( deviceSelectionId != null ) {
			  hql += "AND deviceId IN (  SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) ";
		  }		  
		  if( info.deviceGroupId != null && info.deviceGroupId.length() > 0 ){
			  hql += "AND deviceId IN (SELECT device.deviceId "
					+ 	"	FROM DeviceGrpMember as dgm "
					+ 	"	JOIN dgm.device as device "
					+ 	"	WHERE dgm.grp.grpId = :deviceGroupId) ";			  
		  }
		  hql += "GROUP BY deviceId, deviceName "
			  + "ORDER BY UPPER(deviceName)";
		
		// If we are using the summary table for this report
		if(reportsSourceTable.equalsIgnoreCase(PlaybackEventSummary.class.getSimpleName())){
			hql = hql.replace("COUNT(*) as numDeviceAirings", "SUM(numAirings) as numDeviceAirings");
			hql = hql.replace("SUM(assetLength) as deviceAiringsLength", "SUM(airingLength) as deviceAiringsLength");
			hql = hql.replace("(SUM((displaysCount - displayExceptionsCount) * assetLength)) as displayAiringsLength", "SUM(displayAiringLength) as displayAiringsLength");
		}
		
		Session session = HibernateSession.currentSession();
		Query q = session.createQuery( hql )
				.setParameter( "startDate", startDate )
				.setParameter( "endDate", endDate )
				.setParameter( "assetId", Long.valueOf( info.assetId ) );
		if( info.deviceGroupId != null && info.deviceGroupId.length() > 0 ){
			q.setParameter("deviceGroupId", Long.valueOf( info.deviceGroupId ) );
		}
		if( deviceSelectionId != null )
		{
			q.setParameter("deviceSelectionId", deviceSelectionId );
		}
		// If we're generating a report for a set of devices
		String detailString = "";
		List<Object[]> results = q.list();
		List<DeviceDetailInfo> ddis = new ArrayList();
		for( Iterator<Object[]> i=results.iterator(); i.hasNext(); )
		{
			Object[] row = (Object[])i.next();
			
			DeviceDetailInfo ddi = new DeviceDetailInfo();
			ddi.deviceId = (Long)row[0];
			ddi.deviceName = (String)row[1];
			ddi.numDeviceAirings = (Long)row[2];
			ddi.deviceAiringsLength = (Double)row[3];
			ddi.numDisplayAirings = (Long)row[4];
			ddi.displayAiringsLength = (Double)row[5];
			ddis.add(ddi);
			
			if( detailString.length() > 0 ){
				detailString += ", ";
			}
			
			detailString += ddi.deviceName +" - "+ ddi.numDeviceAirings;				
		}
		
		info.setDeviceDetail(detailString);
		info.setDeviceDetailInfos(ddis);
	}
	
	public HSSFWorkbook doExportToExcel(HttpServletResponse response) throws Exception
	{
		ReportEntitySelectionType assetSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Asset.class);
		ReportEntitySelectionType deviceSelection = ReportEntitySelectionType.getReportEntitySelectionTypeByClass(selections, Device.class);
		
		String DEVICE_GROUP = "Device Group";
		String ASSET_NAME = "Asset Name";
		String DEVICE_AIRINGS = "Device Airings";
		String DEVICE_AIRINGS_LENGTH = "Device Airings Length (hh:mm:ss)";
		String DISPLAY_AIRINGS = "Display Airings";
		String DISPLAY_AIRINGS_LENGTH = "Display Airings Length (hh:mm:ss)";		
		String TOTAL_DEVICES = "Total Devices";
		String DEVICE_AIRING_DETAIL = "Aired on these Devices";		
		boolean showResultsByDeviceGroup = this.showResultsByDeviceGroup != null ? this.showResultsByDeviceGroup.booleanValue() : false;
		
	    HSSFWorkbook wb = new HSSFWorkbook();
	    HSSFSheet sheet = wb.createSheet("Asset Summary Report");
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
		cell0.setCellValue("Asset Summary Report");
		cell0.setCellStyle( styleTitle );		
		
		// Spacer
		sheet.createRow( rowCounter++ );	
		
		
		rowCounter = Reformat.splitExcelCells(sheet, styleAlignLeft, "Selected Assets:", assetSelection.getSelectionNames(), rowCounter);
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
		
		int deviceGroupMaxLength = 0;
		if( showResultsByDeviceGroup ){
			HSSFCell cellDeviceGroup = headerRow.createCell( columnCounter++ );
			cellDeviceGroup.setCellValue( DEVICE_GROUP );
			cellDeviceGroup.setCellStyle( styleHeaderAlignLeft );
			deviceGroupMaxLength = DEVICE_GROUP.length();
		}		
		HSSFCell cell1 = headerRow.createCell( columnCounter++ );
		cell1.setCellValue( ASSET_NAME );
		cell1.setCellStyle( styleHeaderAlignLeft );
		int assetNameMaxLength = ASSET_NAME.length();
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
		HSSFCell cell6 = headerRow.createCell( columnCounter++ );
		cell6.setCellValue( TOTAL_DEVICES );
		cell6.setCellStyle( styleHeaderAlignRight );
		int totalDevicesMaxLength = TOTAL_DEVICES.length();
		
		// If we're meant to show details, create the extra row
		if( showDetails != null && showDetails ) {
			
			HSSFCell cellSpacer = headerRow.createCell( columnCounter++ );
			cellSpacer.setCellValue("");
			
			HSSFCell cell7 = headerRow.createCell( columnCounter++ );
			cell7.setCellValue( DEVICE_AIRING_DETAIL );
			cell7.setCellStyle( styleHeaderAlignLeft );
		}		
		
		/*
		 * Report rows
		 */		
		int rowCount = 0;
		for( Iterator i=report.iterator(); i.hasNext(); )
		{
			AssetSummaryReportInfo info = (AssetSummaryReportInfo)i.next();
			HSSFRow row = sheet.createRow( rowCounter++ );
			columnCounter = 0;			
			
			if( showResultsByDeviceGroup ){
				HSSFCell deviceGroupCell = row.createCell( columnCounter++ );
				deviceGroupCell.setCellValue( info.getDeviceGroupName() );
				deviceGroupCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			}
			
			HSSFCell assetNameCell = row.createCell( columnCounter++ );
			assetNameCell.setCellValue( info.getAssetName() );
			assetNameCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Left : styleRow1Left );
			
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
			
			HSSFCell numDevicesCell = row.createCell( columnCounter++ );
			numDevicesCell.setCellValue( info.getNumDevices() );
			numDevicesCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0Right : styleRow1Right );			
			
			// If we're meant to show details, create the extra cell
			if( showDetails != null && showDetails ) {		

				// Spacer column
				columnCounter++;
				
				HSSFCell detailsCell = row.createCell( columnCounter++ );
				detailsCell.setCellValue( info.getDeviceDetail() );
				detailsCell.setCellStyle( ( rowCount % 2 == 0 ) ? styleRow0LeftWrap : styleRow1LeftWrap );			
			}
			
			// Update the maxLength values if necessary
			if( showResultsByDeviceGroup && info.getDeviceGroupName() != null && info.getDeviceGroupName().length() > deviceGroupMaxLength ){
				deviceGroupMaxLength = info.getDeviceGroupName().length();
			}
			if( info.getAssetName().length() > assetNameMaxLength ){
				assetNameMaxLength = info.getAssetName().length();
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
			if( info.getNumDevices().toString().length() > totalDevicesMaxLength ){
				totalDevicesMaxLength = info.getNumDevices().toString().length();				
			}	
			rowCount++;
		}
		
		// Set each column width according to the maxLength of each 
		int columnWidthCounter = 0;
		if( showResultsByDeviceGroup ){
			sheet.setColumnWidth((short)columnWidthCounter++, (short) ( deviceGroupMaxLength * 256 ));
		}
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( assetNameMaxLength * 256 ));
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( deviceAiringsMaxLength * 256 ));
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( deviceAiringsLengthMaxLength * 256 ));
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( displayAiringsMaxLength * 256 ));
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( displayAiringsLengthMaxLength * 256 ));		
		sheet.setColumnWidth((short)columnWidthCounter++, (short) ( totalDevicesMaxLength * 256 ));		
		
		// If we're showing device details
		if( showDetails != null && showDetails ) {
			
			// Spacer column
			sheet.setColumnWidth((short)columnWidthCounter++, (short) ( ( 1 * 8 ) / ( (double) 1 / 20 ) ));
			
			// Hardcoded column width for device details
			sheet.setColumnWidth((short)columnWidthCounter++, (short) ( ( 75 * 8 ) / ( (double) 1 / 20 ) ));			
		}

		// Landscape mode
		sheet.getPrintSetup().setLandscape( true );
		sheet.setFitToPage( true );
		
		if(response != null){
			response.setHeader("Content-Disposition","attachment; filename=\"AssetSummaryReport.xls\"");
			response.setContentType(new MimeType("application","excel").toString());
			
			// Write out this workbook to the response OutputStream
			OutputStream out = response.getOutputStream();
			wb.write( out );
			out.flush();
			out.close();
		}
		
		return wb;
	}
	
	public static class AssetSummaryReportInfo
	{
		private String assetId;
		private String assetName;		
		private Long numDeviceAirings;
		private Long numDisplayAirings;
		private String deviceAiringsLength;
		private String displayAiringsLength;
		private Long numDevices;
		private String deviceDetail;
		private String deviceGroupId;
		private String deviceGroupName;
		private List<DeviceDetailInfo> deviceDetailInfos;
		
		public List<DeviceDetailInfo> getDeviceDetailInfos() {
			return deviceDetailInfos;
		}

		public void setDeviceDetailInfos(List<DeviceDetailInfo> deviceDetailInfos) {
			this.deviceDetailInfos = deviceDetailInfos;
		}

		/**
		 * @return Returns the assetId.
		 */
		public String getAssetId() {
			return assetId;
		}
		
		/**
		 * @param assetId The assetId to set.
		 */
		public void setAssetId(String assetId) {
			this.assetId = assetId;
		}
		
		/**
		 * @return Returns the assetName.
		 */
		public String getAssetName() {
			return assetName;
		}
		
		/**
		 * @param assetName The assetName to set.
		 */
		public void setAssetName(String assetName) {
			this.assetName = assetName;
		}

		/**
		 * @return the numDevices
		 */
		public Long getNumDevices() {
			return numDevices;
		}

		/**
		 * @param numDevices the numDevices to set
		 */
		public void setNumDevices(Long numDevices) {
			this.numDevices = numDevices;
		}


		/**
		 * @return Returns the deviceDetail.
		 */
		public String getDeviceDetail() {
			return deviceDetail;
		}
		

		/**
		 * @param deviceDetail The deviceDetail to set.
		 */
		public void setDeviceDetail(String deviceDetail) {
			this.deviceDetail = deviceDetail;
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

		/**
		 * @return the deviceGroupId
		 */
		public String getDeviceGroupId() {
			return deviceGroupId;
		}

		/**
		 * @param deviceGroupId the deviceGroupId to set
		 */
		public void setDeviceGroupId(String deviceGroupId) {
			this.deviceGroupId = deviceGroupId;
		}

		/**
		 * @return the deviceGroupName
		 */
		public String getDeviceGroupName() {
			return deviceGroupName;
		}

		/**
		 * @param deviceGroupName the deviceGroupName to set
		 */
		public void setDeviceGroupName(String deviceGroupName) {
			this.deviceGroupName = deviceGroupName;
		}
	}
	
	public static class DeviceDetailInfo{
		private Long deviceId;
		private String deviceName;
		private Long numDeviceAirings;
		private Double deviceAiringsLength;
		private Long numDisplayAirings;
		private Double displayAiringsLength;
		
		public Long getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(Long deviceId) {
			this.deviceId = deviceId;
		}
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		public Long getNumDeviceAirings() {
			return numDeviceAirings;
		}
		public void setNumDeviceAirings(Long numDeviceAirings) {
			this.numDeviceAirings = numDeviceAirings;
		}
		public Double getDeviceAiringsLength() {
			return deviceAiringsLength;
		}
		public void setDeviceAiringsLength(Double deviceAiringsLength) {
			this.deviceAiringsLength = deviceAiringsLength;
		}
		public Long getNumDisplayAirings() {
			return numDisplayAirings;
		}
		public void setNumDisplayAirings(Long numDisplayAirings) {
			this.numDisplayAirings = numDisplayAirings;
		}
		public Double getDisplayAiringsLength() {
			return displayAiringsLength;
		}
		public void setDisplayAiringsLength(Double displayAiringsLength) {
			this.displayAiringsLength = displayAiringsLength;
		}
	}
}
