package com.kuvata.kmf;

import java.util.List;
import java.util.Random;

import org.hibernate.Session;


public class SelectedEntities{

	private Long selectedEntitiesId;
	private Long selectionId;
	private Long entityId;
	
	private static List<Long> usedSelectionIds;
	private static Random r = new Random();
	
	public Long getSelectionId() {
		return selectionId;
	}
	public void setSelectionId(Long selectionId) {
		this.selectionId = selectionId;
	}
	public Long getEntityId() {
		return entityId;
	}
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}
	public Long getSelectedEntitiesId() {
		return selectedEntitiesId;
	}
	public void setSelectedEntitiesId(Long selectedEntitiesId) {
		this.selectedEntitiesId = selectedEntitiesId;
	}
	
	public static Long createSelectedEntities(List<Long> ids, List<Long> allowedIds){
		
		// If this is the first time we are creating selected entities
		if(usedSelectionIds == null){
			String hql = "SELECT DISTINCT selectionId FROM SelectedEntities";
			usedSelectionIds = HibernateSession.currentSession().createQuery(hql).list();
		}
		
		// Get unique selection id
		Long thisSelectionId;
		do{
			thisSelectionId = r.nextLong();
		}while(usedSelectionIds.contains(thisSelectionId));
		
		usedSelectionIds.add(thisSelectionId);
		
		HibernateSession.startBulkmode();
		Session session = HibernateSession.currentSession();
		for(Long id : ids){
			if(allowedIds == null || allowedIds.contains(id)){
				SelectedEntities se = new SelectedEntities();
				se.setSelectionId(thisSelectionId);
				se.setEntityId(id);
				session.save(se);
			}
		}
		HibernateSession.stopBulkmode();
		
		return thisSelectionId;
	}
	
	public static void deleteSelectedEntities(Long selectionId){
		Session session = HibernateSession.currentSession();
		String hql = "DELETE FROM SelectedEntities WHERE selectionId = :id";
		session.createQuery(hql).setParameter("id", selectionId).executeUpdate();
		
		if(usedSelectionIds != null){
			usedSelectionIds.remove(selectionId);
		}
	}
	
	public static List<Long> getSelectedIds(Long selectionId){
		Session session = HibernateSession.currentSession();
		String hql = "SELECT entityId FROM SelectedEntities WHERE selectionId = :id";
		return session.createQuery(hql).setParameter("id", selectionId).list();
	}
}
