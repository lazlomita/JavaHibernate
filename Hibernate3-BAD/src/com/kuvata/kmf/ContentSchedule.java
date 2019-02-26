package com.kuvata.kmf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import com.kuvata.kmf.usertype.FileTransmissionStatus;


public class ContentSchedule extends PersistentEntity{
	
	private static Logger logger = Logger.getLogger(ContentSchedule.class);

	private Long contentScheduleId;
	private Device device;
	private Date runDt;
	private Date startDt;
	private Date endDt;
	private Date serverStartDt;
	private Date serverEndDt;
	private Integer seqNum;
	
	// Non-persistent variables
	private String activeContentSchedule;
	private Date activeContentScheduleLocalStartTime;
	
	public static final String CS_FILE_DATE_FORMAT = "yyyy-MM-dd-HH.mm.ss";
	
	public String getFileName(){
		SimpleDateFormat dateFormat = new SimpleDateFormat( CS_FILE_DATE_FORMAT );

		String localStartDate = dateFormat.format(this.getStartDt());
		String seqNum = this.getSeqNum().toString().length() > 1 ? this.getSeqNum().toString() : "0" + this.getSeqNum();
		// We need the following for slave devices
		String deviceId = this.getDevice().getMirrorSource() != null ? this.getDevice().getMirrorSource().getDeviceId().toString() : this.getDevice().getDeviceId().toString();
		String csFileName = deviceId + "-" + localStartDate + "-" + seqNum + ".xml";
		return csFileName;
	}
	
	public String getDevicesActiveContentSchedule() throws ParseException{
		if(this.activeContentSchedule == null || this.activeContentSchedule.length() == 0){
			String result = "";
			HeartbeatEvent he = this.getDevice().getLastHeartbeatEvent();
			if(he != null && he.getLastContentSchedule() != null){
				result = he.getLastContentSchedule();
			}
			this.activeContentSchedule = result;
		}
		
		if(this.activeContentScheduleLocalStartTime == null && this.activeContentSchedule != null && this.activeContentSchedule.length() > 0){
			SimpleDateFormat dateFormat = new SimpleDateFormat( CS_FILE_DATE_FORMAT );
			int beginIndex = this.activeContentSchedule.indexOf("-") + 1;
			int endIndex = this.activeContentSchedule.lastIndexOf("-");
			this.activeContentScheduleLocalStartTime = dateFormat.parse(this.activeContentSchedule.substring(beginIndex, endIndex));
		}
		
		return this.activeContentSchedule;
	}
	
	public static String getScheduledContentScheduleFileName(Device device){
		String result = null;
		
		// Use mirror source if this device is actively mirroring
		long deviceId = device.getMirrorSource() != null ? device.getMirrorSource().getDeviceId() : device.getDeviceId();
		
		String hql = "SELECT cs FROM ContentSchedule cs "
			+ "WHERE cs.device.deviceId = :deviceId "
			+ "AND cs.serverStartDt <= :now "
			+ "AND cs.serverEndDt > :now "
			+ "ORDER BY cs.runDt DESC";
		List l = HibernateSession.currentSession().createQuery(hql).setParameter("deviceId",deviceId).setParameter("now", new Date()).setMaxResults(1).list();
		if(l.size() > 0){
			ContentSchedule cs = (ContentSchedule)l.get(0);
			result = cs.getFileName();
		}
		// Since we couldn't locate a CS for this time range, get the latest CS that has a start time before now
		else{
			hql = "SELECT cs FROM ContentSchedule cs "
				+ "WHERE cs.device.deviceId = :deviceId "
				+ "AND cs.serverStartDt <= :now "
				+ "ORDER BY cs.runDt DESC";
			l = HibernateSession.currentSession().createQuery(hql).setParameter("deviceId",deviceId).setParameter("now", new Date()).setMaxResults(1).list();
			
			if(l.size() > 0){
				ContentSchedule cs = (ContentSchedule)l.get(0);
				result = cs.getFileName();
			}
		}
		return result;
	}
	
	public static ContentSchedule getMostRecentCurrentContentSchedule(Long deviceId){
		ContentSchedule result = null;
		String hql = "SELECT cs FROM ContentSchedule cs "
			+ "WHERE cs.device.deviceId = :deviceId "
			+ "AND cs.serverStartDt <= :now AND cs.serverEndDt > :now "
			+ "ORDER BY cs.runDt DESC";
		List l = HibernateSession.currentSession().createQuery(hql).setParameter("deviceId",deviceId).setParameter("now", new Date()).setMaxResults(1).list();
		if(l.size() > 0){
			ContentSchedule cs = (ContentSchedule)l.get(0);
			result = cs;
		}
		return result;
	}
	
	public static void cancelCS(Device device, String filename, boolean clearCSFileTransmission) throws ParseException{
		
		logger.info("Canceling content schedule: " + filename + " for device: " + device.getDeviceId());
		
		// Get the contentSchedule object for this device and this filename
		Long csDeviceId = Long.parseLong(filename.substring(0, filename.indexOf("-")));
		SimpleDateFormat dateFormat = new SimpleDateFormat( CS_FILE_DATE_FORMAT );
		Date startDt = dateFormat.parse(filename.substring(filename.indexOf("-") + 1, filename.lastIndexOf("-")));
		Integer seqNum = Integer.parseInt(filename.substring(filename.lastIndexOf("-") + 1, filename.indexOf(".xml")));
		
		String hql = "SELECT cs FROM ContentSchedule cs WHERE cs.device.deviceId = :deviceId AND cs.startDt = :startDt AND cs.seqNum = :seqNum";
		ContentSchedule cs = (ContentSchedule)HibernateSession.currentSession().createQuery(hql).setParameter("deviceId", csDeviceId).setParameter("startDt", startDt).setParameter("seqNum", seqNum).uniqueResult();

		if( cs != null )
		{
			hql = "SELECT ft.fileTransmissionId FROM FileTransmission ft WHERE ft.device.deviceId = :deviceId and ft.status IN ('" + FileTransmissionStatus.NEEDED.getPersistentValue() + "', '" + FileTransmissionStatus.NEEDED_FOR_FUTURE.getPersistentValue() + "') " +
				"AND ft.fileTransmissionId IN (SELECT csft.fileTransmission.fileTransmissionId FROM CSFileTransmission csft WHERE csft.contentSchedule.contentScheduleId = :csId)";
		
			hql = "SELECT fileTransmission.fileTransmissionId, COUNT(*) FROM CSFileTransmission WHERE fileTransmission.fileTransmissionId IN (" + hql + ") GROUP BY fileTransmission.fileTransmissionId";
			List<Object[]> fts = HibernateSession.currentSession().createQuery(hql).setParameter("deviceId", device.getDeviceId()).setParameter("csId", cs.getContentScheduleId()).list();
			List<Long> ftsToCancel = new ArrayList<Long>();
			for(Object[] o : fts){
				Long fileTransmissionId = (Long)o[0];
				Long count = (Long)o[1];
			
				// If there is only one reference to this file transmission, it can be cancelled
				if(count == 1){
					ftsToCancel.add(fileTransmissionId);
				}
			}
			
			if(ftsToCancel.size() > 0){
				int toIndex = 0;
				do{
					toIndex += 1000;
					List<Long> idsForQuery = ftsToCancel.size() > toIndex ? ftsToCancel.subList(toIndex - 1000, toIndex) : ftsToCancel.subList(toIndex - 1000, ftsToCancel.size());
					hql = "DELETE FROM FileTransmission WHERE fileTransmissionId IN (:ftsToCancel)";
					HibernateSession.currentSession().createQuery(hql).setParameterList("ftsToCancel", idsForQuery).executeUpdate();
				}while(ftsToCancel.size() > toIndex);
			}else{
				logger.info("No file_transmission rows to delete for " + filename);
			}
		
			// If this is an edge server canceling a CS that is meant for a node
			if(csDeviceId.equals(device.getDeviceId()) == false){
				for(Long nodeDeviceId : device.getEdgeDeviceIds()){
					// Also cancel this CS for the underlying node device
					if(nodeDeviceId.equals(csDeviceId)){
						ContentSchedule.cancelCS(Device.getDevice(nodeDeviceId), filename, false);
						break;
					}
				}
			}
		
			if(clearCSFileTransmission){
				// Now we should delete all rows from the cs_file_transmission table since all references to these rows have been canceled
				hql = "DELETE FROM CSFileTransmission WHERE contentSchedule.contentScheduleId = :csId";
				HibernateSession.currentSession().createQuery(hql).setParameter("csId", cs.getContentScheduleId()).executeUpdate();
			}
		}
	}
	
	public ContentSchedule copyIntoNewObject(){
		ContentSchedule cs = new ContentSchedule();
		cs.setDevice(device);
		cs.setRunDt(runDt);
		cs.setStartDt(startDt);
		cs.setEndDt(endDt);
		cs.setServerStartDt(serverStartDt);
		cs.setServerEndDt(serverEndDt);
		cs.setSeqNum(seqNum);
		return cs;
	}
	
	public Long getContentScheduleId() {
		return contentScheduleId;
	}
	public void setContentScheduleId(Long contentScheduleId) {
		this.contentScheduleId = contentScheduleId;
	}
	public Date getRunDt() {
		return runDt;
	}
	public void setRunDt(Date runDt) {
		this.runDt = runDt;
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
	public Date getServerStartDt() {
		return serverStartDt;
	}
	public void setServerStartDt(Date serverStartDt) {
		this.serverStartDt = serverStartDt;
	}
	public Date getServerEndDt() {
		return serverEndDt;
	}
	public void setServerEndDt(Date serverEndDt) {
		this.serverEndDt = serverEndDt;
	}
	public Integer getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getActiveContentSchedule() {
		return activeContentSchedule;
	}

	public void setActiveContentSchedule(String activeContentSchedule) {
		this.activeContentSchedule = activeContentSchedule;
	}

	public Date getActiveContentScheduleLocalStartTime() {
		return activeContentScheduleLocalStartTime;
	}

	public void setActiveContentScheduleLocalStartTime(
			Date activeContentScheduleLocalStartTime) {
		this.activeContentScheduleLocalStartTime = activeContentScheduleLocalStartTime;
	}
	
}
