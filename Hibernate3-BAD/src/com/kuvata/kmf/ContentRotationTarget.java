package com.kuvata.kmf;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class ContentRotationTarget extends Entity implements HistorizableCollectionMember {

	private Long contentRotationTargetId;
	private PlaylistContentRotation playlistContentRotation;
	private Device device;
		
	/**
	 * 
	 *
	 */
	public ContentRotationTarget()
	{		
	}
	
	/**
	 * 
	 * @param contentRotationId
	 * @return
	 * @throws HibernateException
	 */
	public static ContentRotationTarget getContentRotationTarget(Long contentRotationId) throws HibernateException
	{
		return (ContentRotationTarget)Entity.load(ContentRotationTarget.class, contentRotationId);		
	}
	
	/**
	 * Either updates or creates a new ContentRotationTarget object
	 * @param device
	 * @param asset
	 */
	public static void create(ContentRotation contentRotation, Playlist playlist, Displayarea displayarea, Device device)
	{
		// First, for data integrity, remove any content rotation target objects associated with this device combo
		List l = ContentRotationTarget.getOtherContentRotationTargets( contentRotation, playlist, displayarea, device );
		for( Iterator i=l.iterator(); i.hasNext(); )
		{
			ContentRotationTarget crt = (ContentRotationTarget)i.next();
			crt.delete();
		}
				
		// If a PlaylistContentRotation object does not already exist for this combination
		PlaylistContentRotation pcr = PlaylistContentRotation.getPlaylistContentRotation( contentRotation, playlist, displayarea, false );
		if( pcr == null )
		{
			// Create a new non-default PlaylistContentRotation object
			pcr = PlaylistContentRotation.create( contentRotation, playlist, displayarea, false );
		}
		
		// If a ContentRotationTarget object does not already exist for this combination
		ContentRotationTarget crt = ContentRotationTarget.getContentRotationTarget( pcr, device );
		if( crt == null )
		{
			// Create a new one
			crt = new ContentRotationTarget();
			crt.setPlaylistContentRotation( pcr );
			crt.setDevice( device );
			crt.save();
			pcr.getContentRotationTargets().add( crt );
			
			// Make dirty this device
			device.makeDirty();
		}
		
		// Make the playlist associated with this PlaylistContentRotation dirty
		pcr.getPlaylist().makeDirty( false );
	}
		
	/**
	 * 
	 * @param p
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static ContentRotationTarget getContentRotationTarget(PlaylistContentRotation pcr, Device device) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		ContentRotationTarget crt = (ContentRotationTarget)session.createCriteria(ContentRotationTarget.class)
				.add( Expression.eq("playlistContentRotation.playlistContentRotationId", pcr.getPlaylistContentRotationId()) )		
				.add( Expression.eq("device.deviceId", device.getDeviceId()) )				
				.uniqueResult();							
		return crt;		
	}	
	
	/**
	 * Returns a list of ContentRotationTarget objects that are not associated
	 * with the given contentRotation.
	 *  
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List getOtherContentRotationTargets(ContentRotation contentRotation, Playlist playlist, Displayarea displayarea, Device device) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT crt "
			+ "FROM ContentRotationTarget as crt "
			+ "JOIN crt.playlistContentRotation as pcr "
			+ "WHERE pcr.playlist.playlistId = "+ playlist.getPlaylistId() +" "
			+ "AND pcr.displayarea.displayareaId = "+ displayarea.getDisplayareaId() +" "
			+ "AND pcr.contentRotation.contentRotationId != "+ contentRotation.getContentRotationId() +" "			
			+ "AND crt.device.deviceId = "+ device.getDeviceId();					
		return session.createQuery( hql ).list();		
	}	
	
	/**
	 * Returns a list of ContentRotationTarget objects that are not associated
	 * with the given contentRotation.
	 *  
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List getContentRotationTargets(ContentRotation contentRotation, Playlist playlist, Displayarea displayarea) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT crt "
			+ "FROM ContentRotationTarget as crt "
			+ "JOIN crt.playlistContentRotation as pcr "
			+ "WHERE pcr.playlist.playlistId = "+ playlist.getPlaylistId() +" "
			+ "AND pcr.displayarea.displayareaId = "+ displayarea.getDisplayareaId() +" "
			+ "AND pcr.contentRotation.contentRotationId = "+ contentRotation.getContentRotationId();					
		return session.createQuery( hql ).list();		
	}		
	
	public void delete() throws HibernateException
	{
		// Remove this object from the parent collection
		this.playlistContentRotation.getContentRotationTargets().remove( this );				
		super.delete();
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof ContentRotationTarget) ) result = false;
		
		ContentRotationTarget c = (ContentRotationTarget) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PlaylistContentRotation".hashCode();
		result = Reformat.getSafeHash( this.getContentRotationTargetId(), result, 29 );
		result = Reformat.getSafeHash( this.getPlaylistContentRotation().getPlaylistContentRotationId(), result, 31 );
		result = Reformat.getSafeHash( this.getDevice().getDeviceId(), result, 37 );		
		return result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getContentRotationTargetId();
	}
	
	/**
	 * Returns either the playlistId or the contentRotationId associated with this ContentRotationTarget
	 */
	public Long getHistoryEntityId()
	{
		if( this.getPlaylistContentRotation().getPlaylist() != null ){
			return this.getPlaylistContentRotation().getPlaylist().getPlaylistId();	
		}else{
			return this.getPlaylistContentRotation().getContentRotation().getContentRotationId();	
		}		
	}	
	
	/**
	 * If there is a playlist associated with this content rotation target,
	 * return PlaylistName: DeviceName.
	 * Otherwise, simply return DeviceName (because this information will show up on the entity history
	 * page for the content rotation, so it is unnecessary to preface the DeviceName with the content rotation name) 
	 */
	public String getEntityName()
	{
		if( this.getPlaylistContentRotation().getPlaylist() != null ){
			return this.getPlaylistContentRotation().getPlaylist().getPlaylistName() +": "+ this.getDevice().getDeviceName();	
		}else{
			return this.getDevice().getDeviceName();
		}		
	}		

	/**
	 * @return Returns the contentRotationTargetId.
	 */
	public Long getContentRotationTargetId() {
		return contentRotationTargetId;
	}
	

	/**
	 * @param contentRotationTargetId The contentRotationTargetId to set.
	 */
	public void setContentRotationTargetId(Long contentRotationTargetId) {
		this.contentRotationTargetId = contentRotationTargetId;
	}
	

	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}
	

	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}
	

	/**
	 * @return Returns the playlistContentRotation.
	 */
	public PlaylistContentRotation getPlaylistContentRotation() {
		return playlistContentRotation;
	}
	

	/**
	 * @param playlistContentRotation The playlistContentRotation to set.
	 */
	public void setPlaylistContentRotation(
			PlaylistContentRotation playlistContentRotation) {
		this.playlistContentRotation = playlistContentRotation;
	}
	


}
