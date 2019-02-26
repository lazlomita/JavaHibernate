package com.kuvata.kmf;

import java.sql.Clob;
import java.util.Date;

import org.hibernate.Hibernate;

import parkmedia.usertype.StatusType;


public class MulticastNetworkFiles extends PersistentEntity{

	private Long multicastNetworkFilesId;
	private MulticastNetwork multicastNetwork;
	private String fileloc;
	private Long filesize;
	private Long bytesSent;
	private String status;
	private Date createDt;
	private Clob receiveSiteIds;
	
	public static void create(MulticastNetwork multicastNetwork, String fileloc, Long filesize, String receiveSiteIds){
		MulticastNetworkFiles mnf = new MulticastNetworkFiles();
		mnf.multicastNetwork = multicastNetwork;
		mnf.fileloc = fileloc;
		mnf.filesize = filesize;
		mnf.bytesSent = 0l;
		mnf.status = StatusType.QUEUED.getPersistentValue();
		mnf.createDt = new Date();
		mnf.receiveSiteIds = Hibernate.createClob(receiveSiteIds);
		mnf.save();
	}
	
	public Long getMulticastNetworkFilesId() {
		return multicastNetworkFilesId;
	}
	public void setMulticastNetworkFilesId(Long multicastNetworkFilesId) {
		this.multicastNetworkFilesId = multicastNetworkFilesId;
	}
	public String getFileloc() {
		return fileloc;
	}
	public void setFileloc(String fileloc) {
		this.fileloc = fileloc;
	}
	public Long getFilesize() {
		return filesize;
	}
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}
	public Long getBytesSent() {
		return bytesSent;
	}
	public void setBytesSent(Long bytesSent) {
		this.bytesSent = bytesSent;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreateDt() {
		return createDt;
	}
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}
	public Clob getReceiveSiteIds() {
		return receiveSiteIds;
	}
	public void setReceiveSiteIds(Clob receiveSiteIds) {
		this.receiveSiteIds = receiveSiteIds;
	}
	public MulticastNetwork getMulticastNetwork() {
		return multicastNetwork;
	}
	public void setMulticastNetwork(MulticastNetwork multicastNetwork) {
		this.multicastNetwork = multicastNetwork;
	}
}
