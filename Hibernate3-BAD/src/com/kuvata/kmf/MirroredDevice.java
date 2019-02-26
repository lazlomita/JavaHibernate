package com.kuvata.kmf;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.KMFLogger;
import parkmedia.usertype.DeviceCommandType;
import parkmedia.usertype.DevicePropertyType;
import parkmedia.usertype.FileTransmissionStatus;
import parkmedia.usertype.StatusType;

import com.kuvata.kmf.logging.HistorizableChildEntity;
/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class MirroredDevice extends Entity implements HistorizableChildEntity
{ 
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( MirroredDevice.class );
	private Long mirroredDeviceId;	
	private Device mirrorPlayer;
	private Device mirrorSource;	
	
	/**
	 * 
	 *
	 */
	public MirroredDevice()
	{		
	}
	
	public Long getEntityId() {
		return this.mirroredDeviceId;
	}
	
	public Long getHistoryEntityId() {
		return this.getMirrorPlayer().getDeviceId();
	}
	
	public String getEntityName() {
		return this.getMirrorSource().getDeviceName();
	}
	
	/**
	 * Creates a new MirroredDevice object.
	 * 
	 * @param mirrorPlayer
	 * @param mirrorSource
	 */
	public static void create(Device mirrorPlayer, Device mirrorSource) throws InterruptedException, ParseException
	{		
		MirroredDevice md = MirroredDevice.getMirroredDevice( mirrorPlayer, mirrorSource );
		if( md == null )
		{
			md = new MirroredDevice();
			md.setMirrorPlayer( mirrorPlayer );
			md.setMirrorSource( mirrorSource );
			md.save();	
			
			// Send down the most recent content schedule file of the mirror source device to the mirror player device
			mirrorPlayer.reissueContentScheduleDeviceCommand( mirrorSource, true, false );
		}		
	}

	/**
	 * Returns the MirroredDevice object associated with the given mirrorPlayer and mirrorSource
	 * @param mirrorPlayer
	 * @param mirrorSource
	 * @return
	 */
	public static MirroredDevice getMirroredDevice(Device mirrorPlayer, Device mirrorSource)
	{
		Session session = HibernateSession.currentSession();	
		return (MirroredDevice)session.createCriteria(MirroredDevice.class)
				.createAlias("mirrorPlayer", "mp")
				.createAlias("mirrorSource", "ms")
				.add( Expression.eq("mp.deviceId", mirrorPlayer.getDeviceId()) )
				.add( Expression.eq("ms.deviceId", mirrorSource.getDeviceId()) )
				.uniqueResult();
	}	
	
	public void delete(){
		// If the mirrored device that we're about to delete is currently being used as the active mirror device (master device), 
		// clear out the master device property of the mirror player
		if( this.getMirrorPlayer().getMirrorSource() != null 
				&& this.getMirrorPlayer().getMirrorSource().getDeviceId().longValue() == this.getMirrorSource().getDeviceId().longValue() ){
			this.getMirrorPlayer().setMirrorSource( null );
			this.getMirrorPlayer().update();
			
			// Send down the device command so this change is reflected on the device
			try {
				this.getMirrorPlayer().addDeviceCommand( DeviceCommandType.PROPERTY_CHANGE_COMMAND, DevicePropertyType.MASTER_DEVICE_ID.getPropertyName() +",null", true );
			} catch (Exception e) {			
				logger.error( e );
			}
		}
		
		try {
			// Cancel any pending CS and getCS commands
			List<DeviceCommand> dcs = DeviceCommand.getDeviceCommands(this.getMirrorPlayer(), DeviceCommandType.GET_CONTENT_SCHEDULE, new StatusType[]{StatusType.IN_PROGRESS, StatusType.PENDING, StatusType.QUEUED}, null);
			for(DeviceCommand dc : dcs){
				// Make sure this command is for a CS for the mirror source
				if(dc.getParameters().contains("/" + this.getMirrorSource().getDeviceId() + "-")){
					// Set the status of file transmission to "Cancelled" as well, so it is not reflected in the bandwidth report
					FileTransmission fileTransmission = FileTransmission.getFileTransmission( dc.getDevice(), dc.getParameters() );
					if( fileTransmission != null ){
						fileTransmission.setStatus( FileTransmissionStatus.CANCELLED );
						fileTransmission.update();
					}
					
					ContentSchedule.cancelCS(this.getMirrorPlayer(), dc.getParameters().substring(dc.getParameters().indexOf("/") + 1), false);
					
					dc.setStatus( StatusType.CANCELLED );
					dc.setLastModifiedDt( new Date() );
					dc.update();
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		super.delete();
	}
	
	/**
	 * @return the mirroredDeviceId
	 */
	public Long getMirroredDeviceId() {
		return mirroredDeviceId;
	}

	/**
	 * @param mirroredDeviceId the mirroredDeviceId to set
	 */
	public void setMirroredDeviceId(Long mirroredDeviceId) {
		this.mirroredDeviceId = mirroredDeviceId;
	}

	/**
	 * @return the mirrorPlayer
	 */
	public Device getMirrorPlayer() {
		return mirrorPlayer;
	}

	/**
	 * @param mirrorPlayer the mirrorPlayer to set
	 */
	public void setMirrorPlayer(Device mirrorPlayer) {
		this.mirrorPlayer = mirrorPlayer;
	}

	/**
	 * @return the mirrorSource
	 */
	public Device getMirrorSource() {
		return mirrorSource;
	}

	/**
	 * @param mirrorSource the mirrorSource to set
	 */
	public void setMirrorSource(Device mirrorSource) {
		this.mirrorSource = mirrorSource;
	}
	
}
