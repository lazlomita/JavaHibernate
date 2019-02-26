package com.kuvata.kmf.reports;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

import com.kuvata.kmf.usertype.DateRangeType;
import com.kuvata.kmf.usertype.ReportType;
import com.kuvata.kmf.Entity;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Recurrence;

public class SavedReport extends Entity{

	private Long savedReportId;
	private String name;
	private ReportType reportType;
	private DateRangeType dateRangeType;
	private Integer startDay;
	private Integer endDay;
	private Boolean showDetails;
	private String detailsFilter;
	private Boolean showByDeviceGroup;
	private Boolean showZeros;
	private Boolean useRoles;
	private Boolean autoRun;
	private String ownerName;
	private Date lastAutoRun;
	private Set<SavedReportFiles> savedReportFiles;
	
	public static List<SavedReport> getSavedReports(boolean populateOwner, String orderBy, boolean reverseOrder){
		Criteria c = HibernateSession.currentSession().createCriteria(SavedReport.class);
		
		if(reverseOrder){
			c.addOrder(Order.desc(orderBy).ignoreCase());
		}else{
			c.addOrder(Order.asc(orderBy).ignoreCase());
		}
		
		List<SavedReport> l = c.list();
		if(populateOwner){
			for(SavedReport sr : l){
				sr.ownerName = sr.getOwner();
			}
		}
		return l;
	}
	
	public static SavedReport getSavedReport(Long savedReportId){
		return (SavedReport)Entity.load(SavedReport.class, savedReportId);
	}
	
	public static List<SavedReport> getRecurringSavedReports(){
		// Get the start time for today
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		
		String hql = "SELECT sr from SavedReport as sr WHERE sr.autoRun = :autoRun AND sr.lastAutoRun IS NULL OR sr.lastAutoRun < :todayStart";
		return HibernateSession.currentSession().createQuery(hql).setParameter("autoRun", Boolean.TRUE).setParameter("todayStart", todayStart.getTime()).list();
		
	}
	
	public HashMap<String, List<Long>> getSelectionIds(){
		String hql = "SELECT srs.selectedEntityClass.className, srs.selectedEntityId from SavedReportSelections as srs WHERE srs.savedReport.savedReportId = :savedReportId " +
				"ORDER BY srs.selectedEntityClass.className, srs.savedReportSelectionsId";
		List<Object[]> l = HibernateSession.currentSession().createQuery(hql).setParameter("savedReportId", savedReportId).list();
		
		HashMap<String, List<Long>> hm = new HashMap<String, List<Long>>();
		for(Object[] o : l){
			String key = (String)o[0];
			Long value = (Long)o[1];
			List ids = hm.get(key) != null ? hm.get(key) : new LinkedList();
			ids.add(value);
			hm.put(key, ids);
		}
		return hm;
	}
	
	public List<Long> getSelectionIds(Class classObject){
		String hql = "SELECT srs.selectedEntityId from SavedReportSelections as srs WHERE srs.savedReport.savedReportId = :savedReportId and srs.selectedEntityClass.className = :className";
		return HibernateSession.currentSession().createQuery(hql).setParameter("savedReportId", savedReportId).setParameter("className", classObject.getName()).list();
	}
	
	public void delete(){
		
		// Delete saved report files
		String hql = "DELETE FROM SavedReportFiles as srf WHERE srf.savedReport.savedReportId = :id";
		HibernateSession.currentSession().createQuery(hql).setParameter("id", this.savedReportId).executeUpdate();
		
		// Delete report selections
		deleteSelections();
		
		// Delete recurrence
		Recurrence r = Recurrence.getSavedReportRecurrence(this.getSavedReportId());
		if(r != null){
			r.delete();
		}
		
		super.delete();
	}
	
	public void deleteSelections(){
		String hql = "DELETE FROM SavedReportSelections as srs WHERE srs.savedReport.savedReportId = :id";
		HibernateSession.currentSession().createQuery(hql).setParameter("id", this.savedReportId).executeUpdate();
	}
	
	public Long getEntityId(){
		return savedReportId;
	}
	public Long getSavedReportId() {
		return savedReportId;
	}
	public void setSavedReportId(Long savedReportId) {
		this.savedReportId = savedReportId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ReportType getReportType() {
		return reportType;
	}
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}
	public DateRangeType getDateRangeType() {
		return dateRangeType;
	}
	public void setDateRangeType(DateRangeType dateRangeType) {
		this.dateRangeType = dateRangeType;
	}
	public Boolean getShowDetails() {
		return showDetails;
	}
	public void setShowDetails(Boolean showDetails) {
		this.showDetails = showDetails;
	}
	public Boolean getShowByDeviceGroup() {
		return showByDeviceGroup;
	}
	public void setShowByDeviceGroup(Boolean showByDeviceGroup) {
		this.showByDeviceGroup = showByDeviceGroup;
	}
	public Boolean getUseRoles() {
		return useRoles;
	}
	public void setUseRoles(Boolean useRoles) {
		this.useRoles = useRoles;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public Boolean getAutoRun() {
		return autoRun;
	}

	public void setAutoRun(Boolean autoRun) {
		this.autoRun = autoRun;
	}

	public Set<SavedReportFiles> getSavedReportFiles() {
		return savedReportFiles;
	}

	public void setSavedReportFiles(Set<SavedReportFiles> savedReportFiles) {
		this.savedReportFiles = savedReportFiles;
	}

	public Date getLastAutoRun() {
		return lastAutoRun;
	}

	public void setLastAutoRun(Date lastAutoRun) {
		this.lastAutoRun = lastAutoRun;
	}

	public Integer getStartDay() {
		return startDay;
	}

	public void setStartDay(Integer startDay) {
		this.startDay = startDay;
	}

	public Integer getEndDay() {
		return endDay;
	}

	public void setEndDay(Integer endDay) {
		this.endDay = endDay;
	}

	public Boolean getShowZeros() {
		return showZeros;
	}

	public void setShowZeros(Boolean showZeros) {
		this.showZeros = showZeros;
	}

	public String getDetailsFilter() {
		return detailsFilter;
	}

	public void setDetailsFilter(String detailsFilter) {
		this.detailsFilter = detailsFilter;
	}
}
