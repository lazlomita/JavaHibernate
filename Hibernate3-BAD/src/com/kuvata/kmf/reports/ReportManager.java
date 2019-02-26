package com.kuvata.kmf.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.ReportEntitySelectionType;

import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.Recurrence;
import com.kuvata.kmf.Role;
import com.kuvata.kmf.Schema;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.SelectedEntities;
import com.kuvata.kmf.permissions.FilterManager;
import com.kuvata.kmf.permissions.FilterType;
import com.kuvata.kmf.util.Reformat;

public class ReportManager extends Thread{
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Report.class );
	
	public void run(){
		
		// Setup
		SchemaDirectory.setup("kuvata", "ReportManager");
		KmfSession kmfSession = KmfSession.create("ReportManager");
		kmfSession.setAdmin(true);
		KmfSession.setKmfSession(kmfSession);
		
		// Get the aggregation status
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -2);
		Schema schema = Schema.getSchema("kuvata");
		boolean aggregationSuccessful = schema.getAggregationProgress() == null && schema.getLastAggregatedDate().after(c.getTime());
		
		// For each recurring saved report
		for(SavedReport sr : SavedReport.getRecurringSavedReports()){
			
			try {
				// Make sure that we are meant to recur today
				Recurrence r = Recurrence.getSavedReportRecurrence(sr.getSavedReportId());
				if(r.recurToday()){
					
					logger.info("Auto Run Report: " + sr.getName() + " -- " + sr.getSavedReportId());
					
					String[] dateRange = sr.getDateRangeType().getDateRange(sr.getStartDay(), sr.getEndDay());
					
					if(aggregationSuccessful){
						
						// Setup roles if needed
						if(sr.getUseRoles()){
							List<Role> roles = sr.getRoles(true);
							for( Role role : roles ){
								kmfSession.getAppUserViewableRoleIds().add( role.getRoleId() );
							}
							
							// Enable all filters
							FilterManager.enableFilter(FilterType.ASSETS_FILTER_ADMIN);
							FilterManager.enableFilter(FilterType.DEVICES_FILTER_ADMIN);
							FilterManager.enableFilter(FilterType.ADVERTISERS_FILTER_ADMIN);
							FilterManager.enableFilter(FilterType.VENUE_PARTNERS_FILTER_ADMIN);
							FilterManager.enableFilter(FilterType.CAMPAIGNS_FILTER_ADMIN);
						}else{
							// Enable view to entities with no roles
							kmfSession.setViewDataWithNoRoles(true);
							
							// Enable all roles as viewable
							for( Role role : Role.getAllRoles() ){
								kmfSession.getAppUserViewableRoleIds().add( role.getRoleId() );
							}
						}
						
						// Prepare the generic report selections
						List<ReportEntitySelectionType> restSelections = new ArrayList<ReportEntitySelectionType>();
						HashMap<String, List<Long>> selections = sr.getSelectionIds();
						for(String key : selections.keySet()){
							ReportEntitySelectionType rest = new ReportEntitySelectionType(Reformat.convertListToCommaDelimitedString(selections.get(key)), Class.forName(key), true, true);
							restSelections.add(rest);
						}
						
						Class[] parameterTypes = new Class[]{ReportEntitySelectionType[].class, String.class, String.class, Boolean.class, Boolean.class, String.class, Boolean.class, String.class, Boolean.class};
						Report report = (Report)sr.getReportType().getReportClass().getConstructor(parameterTypes).newInstance(restSelections.toArray(new ReportEntitySelectionType[0]), dateRange[0], dateRange[1], sr.getShowByDeviceGroup(), sr.getShowDetails(), sr.getDetailsFilter(), sr.getShowZeros(), null, null);
						report.getReportData();
						HSSFWorkbook wb = report.doExportToExcel(null);
						report.delete();
						int count = 0;
						String filename;
						File file;
						do{
							count++;
							filename = Report.SAVE_REPORT_NAME_FORMAT.format(new Date()) + "_" + sr.getName() + "-" + count + ".xls";
							file = new File(KuvataConfig.getKuvataHome() + "/reports/" + filename);
						}
						while(file.exists());
						
						FileOutputStream fos = new FileOutputStream(file);
						wb.write(fos);
						fos.flush();
						fos.close();
						
						SavedReportFiles srf = new SavedReportFiles();
						srf.setSavedReport(sr);
						srf.setFileloc(filename);
						srf.setStartDate(Report.UI_DATE_FORMAT.parse(dateRange[0]));
						srf.setEndDate(Report.UI_DATE_FORMAT.parse(dateRange[1]));
						srf.setCreateDt(new Date());
						srf.save();
						
						sr.setLastAutoRun(new Date());
						sr.update();
						
						// Delete selected entities
						for(ReportEntitySelectionType rest : restSelections){
							SelectedEntities.deleteSelectedEntities(rest.getSelectionId());
						}
						
						// Disable filters
						if(sr.getUseRoles()){
							FilterManager.disableFilter(FilterType.ASSETS_FILTER_ADMIN);
							FilterManager.disableFilter(FilterType.DEVICES_FILTER_ADMIN);
							FilterManager.disableFilter(FilterType.ADVERTISERS_FILTER_ADMIN);
							FilterManager.disableFilter(FilterType.VENUE_PARTNERS_FILTER_ADMIN);
							FilterManager.disableFilter(FilterType.CAMPAIGNS_FILTER_ADMIN);
						}else{
							kmfSession.setViewDataWithNoRoles(false);
						}
						
						// Clear all viewable roles
						kmfSession.getAppUserViewableRoleIds().clear();
						
					}else{
						SavedReportFiles srf = new SavedReportFiles();
						srf.setSavedReport(sr);
						srf.setFileloc(Report.SAVE_REPORT_NAME_FORMAT.format(new Date()) + " Auto run report did not save. Data not up to date.");
						srf.setStartDate(Report.UI_DATE_FORMAT.parse(dateRange[0]));
						srf.setEndDate(Report.UI_DATE_FORMAT.parse(dateRange[1]));
						srf.setCreateDt(new Date());
						srf.save();
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}