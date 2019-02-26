package com.kuvata.kmf;

public class CSFileTransmission extends PersistentEntity{

	private Long csFileTransmissionId;
	private ContentSchedule contentSchedule;
	private FileTransmission fileTransmission;
	
	public static void create(ContentSchedule contentSchedule, FileTransmission fileTransmission){
		CSFileTransmission csft = new CSFileTransmission();
		csft.contentSchedule = contentSchedule;
		csft.fileTransmission = fileTransmission;
		csft.save();
	}
	public Long getCsFileTransmissionId() {
		return csFileTransmissionId;
	}
	public void setCsFileTransmissionId(Long csFileTransmissionId) {
		this.csFileTransmissionId = csFileTransmissionId;
	}
	public ContentSchedule getContentSchedule() {
		return contentSchedule;
	}
	public void setContentSchedule(ContentSchedule contentSchedule) {
		this.contentSchedule = contentSchedule;
	}
	public FileTransmission getFileTransmission() {
		return fileTransmission;
	}
	public void setFileTransmission(FileTransmission fileTransmission) {
		this.fileTransmission = fileTransmission;
	}
}
