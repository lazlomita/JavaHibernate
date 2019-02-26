package com.kuvata.kmf.reports;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.Constants;

public abstract class Report {

	public static final SimpleDateFormat UI_DATE_FORMAT = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
	public static final SimpleDateFormat SAVE_REPORT_NAME_FORMAT = new SimpleDateFormat("yyyyMMdd");
	
	protected ReportEntitySelectionType[] selections;
	protected String startDate;
	protected String endDate;
	protected Boolean showResultsByDeviceGroup;
	protected Boolean showDetails;
	protected String detailsFilter;
	protected Boolean showZeros;
	protected String orderBy;
	protected Boolean reverseOrder;
	protected List report;
	
	public Report(String[] selectedIds, Class[] classes, String startDate, String endDate, String orderBy){
		this.startDate = startDate;
		this.endDate = endDate;
		this.orderBy = orderBy;
		
		if (selectedIds.length != classes.length)
			return;
		
		selections = new ReportEntitySelectionType[selectedIds.length];
		for(int index = 0; index < selectedIds.length; index++) {
			ReportEntitySelectionType entity = new ReportEntitySelectionType(selectedIds[index], classes[index], true, true);
			selections[index] = entity;
		}
	}
	
	public Report(ReportEntitySelectionType[] selections, String startDate, String endDate, Boolean showResultsByDeviceGroup, Boolean showDetails, String detailsFilter, Boolean showZeros, String orderBy, Boolean reverseOrder){
		this.selections = selections;
		this.startDate = startDate;
		this.endDate = endDate;
		this.showResultsByDeviceGroup = showResultsByDeviceGroup;
		this.showDetails = showDetails;
		this.detailsFilter = detailsFilter;
		this.showZeros = showZeros;
		this.orderBy = orderBy;
		this.reverseOrder = reverseOrder;
	}
	
	public void delete() {
		for(ReportEntitySelectionType entity : selections) {
			entity.delete();
		}
	}
	public abstract List getReportData() throws Exception;
	public abstract HSSFWorkbook doExportToExcel(HttpServletResponse response) throws Exception;
}
