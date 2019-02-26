package com.kuvata.kmf.reports;

import com.kuvata.kmf.EntityClass;
import com.kuvata.kmf.PersistentEntity;

public class SavedReportSelections extends PersistentEntity{

	private Long savedReportSelectionsId;
	private SavedReport savedReport;
	private Long selectedEntityId;
	private EntityClass selectedEntityClass;
	
	public static void create(SavedReport savedReport, Long entityId, EntityClass entityClass){
		SavedReportSelections srs = new SavedReportSelections();
		srs.savedReport = savedReport;
		srs.selectedEntityId = entityId;
		srs.selectedEntityClass = entityClass;
		srs.save();
	}
	
	public Long getSavedReportSelectionsId() {
		return savedReportSelectionsId;
	}
	public void setSavedReportSelectionsId(Long savedReportSelectionsId) {
		this.savedReportSelectionsId = savedReportSelectionsId;
	}
	public SavedReport getSavedReport() {
		return savedReport;
	}
	public void setSavedReport(SavedReport savedReport) {
		this.savedReport = savedReport;
	}
	public Long getSelectedEntityId() {
		return selectedEntityId;
	}
	public void setSelectedEntityId(Long selectedEntityId) {
		this.selectedEntityId = selectedEntityId;
	}
	public EntityClass getSelectedEntityClass() {
		return selectedEntityClass;
	}
	public void setSelectedEntityClass(EntityClass selectedEntityClass) {
		this.selectedEntityClass = selectedEntityClass;
	}
}
