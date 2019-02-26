package com.kuvata.kmf;

import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import parkmedia.KuvataConfig;


public class ContentUpdate extends Entity{
	
	private static Logger logger = Logger.getLogger(ContentUpdate.class);

	private Long contentUpdateId;
	private Date createDt;
	private String filename;
	private Long filesize;
	
	public static ContentUpdate create(String filename){
		ContentUpdate contentUpdate = new ContentUpdate();
		contentUpdate.setCreateDt(new Date());
		contentUpdate.setFilename(filename);
		contentUpdate.save();
		return contentUpdate;
	}
	
	public static List<ContentUpdate> getContentUpdates(){
		Session session = HibernateSession.currentSession();
		String hql = "SELECT cu FROM ContentUpdate as cu ORDER BY cu.createDt DESC";
		return session.createQuery( hql ).list();
	}
	
	public static ContentUpdate getContentUpdate(Long contentUpdateId){
		Session session = HibernateSession.currentSession();
		String hql = "SELECT cu FROM ContentUpdate as cu WHERE cu.contentUpdateId = ?";
		return (ContentUpdate)session.createQuery( hql ).setParameter(0, contentUpdateId).uniqueResult();
	}
	
	public void delete(){
		try {
			// Delete the content update directory
			String cmd = "rm -rf " + KuvataConfig.getKuvataHome() + "/" + Constants.CONTENT_UPDATES + "/" + contentUpdateId;
			Runtime.getRuntime().exec(cmd).waitFor();
			
			// Delete the object
			super.delete();
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	public List<ContentSchedulerStatus> getContentSchedulerStatuses(){
		Session session = HibernateSession.currentSession();
		String hql = "SELECT css FROM ContentSchedulerStatus as css WHERE css.contentUpdate.contentUpdateId = :contentUpdateId ORDER BY css.dt";
		return session.createQuery( hql ).setParameter("contentUpdateId", contentUpdateId).list();
	}
	
	public Long getEntityId(){
		return contentUpdateId;
	}
	public Long getContentUpdateId() {
		return contentUpdateId;
	}
	public void setContentUpdateId(Long contentUpdateId) {
		this.contentUpdateId = contentUpdateId;
	}
	public Date getCreateDt() {
		return createDt;
	}
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Long getFilesize() {
		return filesize;
	}
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}
}
