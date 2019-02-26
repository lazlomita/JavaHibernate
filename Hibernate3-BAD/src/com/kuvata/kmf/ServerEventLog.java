package com.kuvata.kmf;

import java.util.Date;
import java.util.List;

public class ServerEventLog extends Entity{

	private Long serverEventLogId;
	private String username;
	private String action;
	private Date startDt;
	private Date endDt;
	private String status;
	private String details;
	
	public ServerEventLog(String username, String action, Date startDt, Date endDt, String status, String details){
		this.setUsername(username);
		this.setAction(action);
		this.setStartDt(startDt);
		this.setEndDt(endDt);
		this.setStatus(status);
		this.setDetails(details);
	}
	
	public ServerEventLog(){
		
	}
	
	public static List<ServerEventLog> getServerEventLogs(String type){
		String hql = "SELECT sel FROM ServerEventLog sel WHERE action = :type ORDER BY startDt DESC";
		return HibernateSession.currentSession().createQuery(hql).setParameter("type", type).setMaxResults(10).list();
	}
	
	public Long getEntityId(){
		return serverEventLogId;
	}
	
	public Long getServerEventLogId() {
		return serverEventLogId;
	}
	public void setServerEventLogId(Long serverEventLogId) {
		this.serverEventLogId = serverEventLogId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Date getStartDt() {
		return startDt;
	}
	public void setStartDt(Date startDt) {
		this.startDt = startDt;
	}
	public Date getEndDt() {
		return endDt;
	}
	public void setEndDt(Date endDt) {
		this.endDt = endDt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	
}
