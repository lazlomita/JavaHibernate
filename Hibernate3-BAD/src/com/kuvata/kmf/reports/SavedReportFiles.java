package com.kuvata.kmf.reports;

import java.util.Date;

import com.kuvata.kmf.Entity;

public class SavedReportFiles extends Entity{

	private Long savedReportFilesId;
	private SavedReport savedReport;
	private String fileloc;
	private Date startDate;
	private Date endDate;
	private Date createDt;
	
	
	public static SavedReportFiles getSavedReportFiles(Long savedReportFilesId){
		return (SavedReportFiles)Entity.load(SavedReportFiles.class, savedReportFilesId);
	}
	
	public Long getEntityId(){
		return savedReportFilesId;
	}

	public Long getSavedReportFilesId() {
		return savedReportFilesId;
	}

	public void setSavedReportFilesId(Long savedReportFilesId) {
		this.savedReportFilesId = savedReportFilesId;
	}

	public String getFileloc() {
		return fileloc;
	}

	public void setFileloc(String fileloc) {
		this.fileloc = fileloc;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public SavedReport getSavedReport() {
		return savedReport;
	}

	public void setSavedReport(SavedReport savedReport) {
		this.savedReport = savedReport;
	}

	public Date getCreateDt() {
		return createDt;
	}

	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}
}
