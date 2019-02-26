package com.kuvata.kmf;

import java.util.Date;
import java.util.List;

import parkmedia.usertype.JobType;
import parkmedia.usertype.StatusType;


public class Job extends Entity{
	
	private Long jobId;
	private JobType jobType;
	private StatusType status;
	private String parameters;
	private Date createDt;
	private Date lastModifiedDt;
	private AppUser appUser;

	public static Job createJob(JobType jobType, String parameters, AppUser appUser){
		Job job = new Job();
		job.jobType = jobType;
		job.status = StatusType.QUEUED;
		job.parameters = parameters;
		job.createDt = new Date();
		job.setAppUser(appUser);
		job.save();
		
		return job;
	}
	
	public static List<Job> getUnprocessedJobs(){
		String statuses = "'" + StatusType.QUEUED.getPersistentValue() + "', '" + StatusType.IN_PROGRESS.getPersistentValue() + "'";
		return HibernateSession.currentSession().createQuery("SELECT job FROM Job job WHERE job.status IN (" + statuses + ")").list();
	}
	
	public void update(){
		this.setLastModifiedDt(new Date());
		super.update();
	}
	
	public Long getEntityId(){
		return jobId;
	}
	public Long getJobId() {
		return jobId;
	}
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
	public JobType getJobType() {
		return jobType;
	}
	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}
	public StatusType getStatus() {
		return status;
	}
	public void setStatus(StatusType status) {
		this.status = status;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	public Date getCreateDt() {
		return createDt;
	}
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}
	public Date getLastModifiedDt() {
		return lastModifiedDt;
	}
	public void setLastModifiedDt(Date lastModifiedDt) {
		this.lastModifiedDt = lastModifiedDt;
	}
	public AppUser getAppUser() {
		return appUser;
	}
	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}
}