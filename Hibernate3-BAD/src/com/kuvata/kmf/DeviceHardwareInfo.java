package com.kuvata.kmf;

import org.hibernate.Session;
import org.hibernate.criterion.Expression;

public class DeviceHardwareInfo extends PersistentEntity{

	private Long deviceHardwareInfoId;
	private Device device;
	private String sysmake;
	private String sysmodel;
	private String boardmfg;
	private String boardmodel;
	private String encfs;
	private String drivesize;
	private String bootloc;
	private String bootsize;
	private String rootloc;
	private String rootsize;
	private String swaploc;
	private String swapsize;
	private String contentloc;
	private String contentsize;
	private String videocard;
	private String audiochipset;
	private String sysram;
	private String procinfo;
	private String corecount;
	private String ethernetchipset;
	private String usbtype;
	private String ttys0;
	private String ttys1;
	private String ttys2;
	private String ttys3;
	
	public static DeviceHardwareInfo create(String[] values, Device device){
		DeviceHardwareInfo dhi = new DeviceHardwareInfo();
		dhi.device = device;
		dhi.sysmake = values[0];
		dhi.sysmodel = values[1];
		dhi.boardmfg = values[2];
		dhi.boardmodel = values[3];
		dhi.encfs = values[4];
		dhi.drivesize = values[5];
		dhi.bootloc = values[6];
		dhi.bootsize = values[7];
		dhi.rootloc = values[8];
		dhi.rootsize = values[9];
		dhi.swaploc = values[10];
		dhi.swapsize = values[11];
		dhi.contentloc = values[12];
		dhi.contentsize = values[13];
		dhi.videocard = values[14];
		dhi.audiochipset = values[15];
		dhi.sysram = values[16];
		dhi.procinfo = values[17];
		dhi.corecount = values[18];
		dhi.ethernetchipset = values[19];
		dhi.usbtype = values[20];
		dhi.ttys0 = values[21];
		dhi.ttys1 = values[22];
		dhi.ttys2 = values[23];
		dhi.ttys3 = values[24];
		dhi.save();
		
		return dhi;
	}
	
	public static DeviceHardwareInfo getDeviceHardwareInfo(Device d){
		Session session = HibernateSession.currentSession();				
		DeviceHardwareInfo dhi = (DeviceHardwareInfo)session.createCriteria(DeviceHardwareInfo.class)
				.add( Expression.eq("device.deviceId", d.getDeviceId()) )				
				.uniqueResult();							
		return dhi;	
	}
	
	public Long getDeviceHardwareInfoId() {
		return deviceHardwareInfoId;
	}
	public void setDeviceHardwareInfoId(Long deviceHardwareInfoId) {
		this.deviceHardwareInfoId = deviceHardwareInfoId;
	}
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public String getSysmake() {
		return sysmake;
	}
	public void setSysmake(String sysmake) {
		this.sysmake = sysmake;
	}
	public String getSysmodel() {
		return sysmodel;
	}
	public void setSysmodel(String sysmodel) {
		this.sysmodel = sysmodel;
	}
	public String getBoardmfg() {
		return boardmfg;
	}
	public void setBoardmfg(String boardmfg) {
		this.boardmfg = boardmfg;
	}
	public String getBoardmodel() {
		return boardmodel;
	}
	public void setBoardmodel(String boardmodel) {
		this.boardmodel = boardmodel;
	}
	public String getEncfs() {
		return encfs;
	}
	public void setEncfs(String encfs) {
		this.encfs = encfs;
	}
	public String getDrivesize() {
		return drivesize;
	}
	public void setDrivesize(String drivesize) {
		this.drivesize = drivesize;
	}
	public String getBootloc() {
		return bootloc;
	}
	public void setBootloc(String bootloc) {
		this.bootloc = bootloc;
	}
	public String getBootsize() {
		return bootsize;
	}
	public void setBootsize(String bootsize) {
		this.bootsize = bootsize;
	}
	public String getRootloc() {
		return rootloc;
	}
	public void setRootloc(String rootloc) {
		this.rootloc = rootloc;
	}
	public String getRootsize() {
		return rootsize;
	}
	public void setRootsize(String rootsize) {
		this.rootsize = rootsize;
	}
	public String getSwaploc() {
		return swaploc;
	}
	public void setSwaploc(String swaploc) {
		this.swaploc = swaploc;
	}
	public String getSwapsize() {
		return swapsize;
	}
	public void setSwapsize(String swapsize) {
		this.swapsize = swapsize;
	}
	public String getContentloc() {
		return contentloc;
	}
	public void setContentloc(String contentloc) {
		this.contentloc = contentloc;
	}
	public String getContentsize() {
		return contentsize;
	}
	public void setContentsize(String contentsize) {
		this.contentsize = contentsize;
	}
	public String getVideocard() {
		return videocard;
	}
	public void setVideocard(String videocard) {
		this.videocard = videocard;
	}
	public String getAudiochipset() {
		return audiochipset;
	}
	public void setAudiochipset(String audiochipset) {
		this.audiochipset = audiochipset;
	}
	public String getSysram() {
		return sysram;
	}
	public void setSysram(String sysram) {
		this.sysram = sysram;
	}
	public String getProcinfo() {
		return procinfo;
	}
	public void setProcinfo(String procinfo) {
		this.procinfo = procinfo;
	}
	public String getCorecount() {
		return corecount;
	}
	public void setCorecount(String corecount) {
		this.corecount = corecount;
	}
	public String getEthernetchipset() {
		return ethernetchipset;
	}
	public void setEthernetchipset(String ethernetchipset) {
		this.ethernetchipset = ethernetchipset;
	}
	public String getUsbtype() {
		return usbtype;
	}
	public void setUsbtype(String usbtype) {
		this.usbtype = usbtype;
	}
	public String getTtys0() {
		return ttys0;
	}
	public void setTtys0(String ttys0) {
		this.ttys0 = ttys0;
	}
	public String getTtys1() {
		return ttys1;
	}
	public void setTtys1(String ttys1) {
		this.ttys1 = ttys1;
	}
	public String getTtys2() {
		return ttys2;
	}
	public void setTtys2(String ttys2) {
		this.ttys2 = ttys2;
	}
	public String getTtys3() {
		return ttys3;
	}
	public void setTtys3(String ttys3) {
		this.ttys3 = ttys3;
	}
}
