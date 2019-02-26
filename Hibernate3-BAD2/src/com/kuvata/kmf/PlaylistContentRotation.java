package com.kuvata.kmf;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class PlaylistContentRotation extends Entity implements HistorizableCollectionMember {

	private Long playlistContentRotationId;
	private Playlist playlist;
	private ContentRotation contentRotation;
	private Displayarea displayarea;
	private Boolean isDefault = false;
	private Set<ContentRotationTarget> contentRotationTargets = new HistorizableSet<ContentRotationTarget>();
	private List<ContentRotationTarget> contentRotationTargetsSorted;
		
	/**
	 * 
	 *
	 */
	public PlaylistContentRotation()
	{		
	}
	
	/**
	 * 
	 * @param playlistContentRotationId
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistContentRotation getPlaylistContentRotation(Long playlistContentRotationId) throws HibernateException
	{
		return (PlaylistContentRotation)Entity.load(PlaylistContentRotation.class, playlistContentRotationId);		
	}
	
	/**
	 * 
	 * @param p
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistContentRotation getPlaylistContentRotation(ContentRotation cr, Playlist p, Displayarea da, boolean isDefault) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		PlaylistContentRotation pcr = (PlaylistContentRotation)session.createCriteria(PlaylistContentRotation.class)
				.add( Expression.eq("contentRotation.contentRotationId", cr.getContentRotationId()) )		
				.add( Expression.eq("playlist.playlistId", p.getPlaylistId()) )
				.add( Expression.eq("displayarea.displayareaId", da.getDisplayareaId()) )
				.add( Expression.eq("isDefault", Boolean.valueOf( isDefault ) ) )
				.uniqueResult();							
		return pcr;		
	}
	
	/**
	 * 
	 * @param p
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistContentRotation getDefaultPlaylistContentRotation(Playlist p, Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		PlaylistContentRotation pcr = (PlaylistContentRotation)session.createCriteria(PlaylistContentRotation.class)					
				.add( Expression.eq("playlist.playlistId", p.getPlaylistId()) )
				.add( Expression.eq("displayarea.displayareaId", da.getDisplayareaId()) )
				.add( Expression.eq("isDefault", true) )
				.uniqueResult();							
		return pcr;		
	}	
		
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the given ContentRotation object
	 * that have at least one ContentRotationTarget object.
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylistContentRotations(ContentRotation cr, boolean requireContentRotationTargets) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT pcr "
			+ "FROM PlaylistContentRotation as pcr "
			+ "WHERE pcr.contentRotation.contentRotationId = "+ cr.getContentRotationId() +" ";
			if( requireContentRotationTargets ){
				hql += "AND SIZE(pcr.contentRotationTargets) > 0 ";
			}
			hql += "ORDER BY UPPER(pcr.playlist.playlistName)";		
		return session.createQuery( hql ).list();		
	}
	
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the given Displayarea object
	 * that are not default content rotations
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylistContentRotations(Displayarea displayarea, boolean isDefault) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List result = session.createCriteria(PlaylistContentRotation.class)								
			.add( Expression.eq("displayarea.displayareaId", displayarea.getDisplayareaId()) )
			.add( Expression.eq("isDefault", isDefault) )
			.list();		
		return result;		
	}		
	
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the given Displayarea object
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylistContentRotations(Displayarea displayarea) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List result = session.createCriteria(PlaylistContentRotation.class)								
			.add( Expression.eq("displayarea.displayareaId", displayarea.getDisplayareaId()) )
			.list();		
		return result;		
	}			
	
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the given ContentRotation and displayarea objects.
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylistContentRotations(ContentRotation cr, Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT pcr "
			+ "FROM PlaylistContentRotation as pcr "
			+ "WHERE pcr.contentRotation.contentRotationId = "+ cr.getContentRotationId() +" "
			+ "AND pcr.displayarea.displayareaId = "+ da.getDisplayareaId() +" "
			+ "ORDER BY UPPER(pcr.playlist.playlistName)";		
		return session.createQuery( hql ).list();		
	}	
	
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the given Playlist and Displayarea objects.
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public static List<PlaylistContentRotation> getPlaylistContentRotations(Playlist p, Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT pcr "
			+ "FROM PlaylistContentRotation as pcr "
			+ "WHERE pcr.playlist.playlistId = "+ p.getPlaylistId() +" "
			+ "AND pcr.displayarea.displayareaId = "+ da.getDisplayareaId() +" "
			+ "AND pcr.isDefault = ? "
			+ "ORDER BY UPPER(pcr.contentRotation.contentRotationName)";
		return session.createQuery( hql ).setParameter(0, false).list();	
	}		
	
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the this playlist and displayarea 
	 * that have at least one ContentRotationTarget object.
	 * @param cr
	 * @return
	 * @throws HibernateException
	 */
	public List getPlaylistContentRotations() throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT pcr "
			+ "FROM PlaylistContentRotation as pcr "
			+ "WHERE pcr.playlist.playlistId = "+ this.getPlaylist().getPlaylistId() +" "
			+ "AND pcr.displayarea.displayareaId = "+ this.getDisplayarea().getDisplayareaId() +" "
			+ "AND pcr.isDefault = ? "
			+ "AND SIZE(pcr.contentRotationTargets) > 0 "
			+ "ORDER BY UPPER(pcr.contentRotation.contentRotationName)";		
		return session.createQuery( hql ).setParameter(0, false).list();		
	}		
	
	/**
	 * Returns a list of PlaylistContentRotation objects associated with the given list of playlistIds
	 * @param playlistIds
	 * @return
	 * @throws HibernateException
	 */
	public static List<PlaylistContentRotation> getPlaylistContentRotations(List<Long> playlistIds) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		String hql = "SELECT pcr "
			+ "FROM PlaylistContentRotation as pcr "
			+ "WHERE pcr.playlist.playlistId IN ( :playlistIds )";		
		return session.createQuery( hql ).setParameterList("playlistIds", playlistIds).list();		
	}			
	
	public static PlaylistContentRotation create(ContentRotation cr, Playlist p, Displayarea da, boolean isDefault)
	{
		// If this is an un-named CR
		// Make sure that the content rotation has all and only the roles of the playlist
		if(cr.getContentRotationName() == null || cr.getContentRotationName().length() == 0){
			cr.copyPermissionEntries(p, true, true);
		}
		
		PlaylistContentRotation pcr = new PlaylistContentRotation();
		pcr.setContentRotation( cr );
		pcr.setPlaylist( p );
		pcr.setDisplayarea( da );
		pcr.setIsDefault( Boolean.valueOf( isDefault ) );
		pcr.save();
		
		p.getPlaylistContentRotations().add( pcr );
		p.update();
		
		return pcr;
	}
	
	public static PlaylistContentRotation createOrUpdate(ContentRotation cr, Playlist p, Displayarea da, boolean isDefault)
	{
		// If we have not yet created a PlaylstContentRotation object for this playlist/displayarea
		PlaylistContentRotation pcr = getDefaultPlaylistContentRotation( p, da );
		if( pcr == null )
		{
			// Create one
			pcr = create( cr, p, da, isDefault );
		}
		else
		{
			// Update the existing one if necessary
			if( cr.getContentRotationId() != pcr.getContentRotation().getContentRotationId() 
					|| p.getPlaylistId() != pcr.getPlaylist().getPlaylistId() 
					|| da.getDisplayareaId() != pcr.getDisplayarea().getDisplayareaId() 
					|| Boolean.valueOf( isDefault ) != pcr.getIsDefault() )
			{
				pcr.setContentRotation( cr );
				pcr.setPlaylist( p );
				pcr.setDisplayarea( da );
				pcr.setIsDefault( Boolean.valueOf( isDefault ) );
				pcr.update();	
			}
			
			// If this is an un-named CR
			// Make sure that the content rotation has all and only the roles of the playlist
			if(cr.getContentRotationName() == null || cr.getContentRotationName().length() == 0){
				cr.copyPermissionEntries(p, true, true);
			}
		}
		
		return pcr;
	}	
	
	public static PlaylistContentRotation copy(PlaylistContentRotation playlistContentRotationToCopy, Playlist playlist, ContentRotation contentRotation) throws Exception
	{
		PlaylistContentRotation newPlaylistContentRotation = new PlaylistContentRotation();
		newPlaylistContentRotation.setPlaylist( playlist );
		newPlaylistContentRotation.setDisplayarea( playlistContentRotationToCopy.getDisplayarea() );
		newPlaylistContentRotation.setIsDefault( playlistContentRotationToCopy.getIsDefault() );
		
		// If we're copying a "named" content rotation
		if( contentRotation.getContentRotationName() != null && contentRotation.getContentRotationName().length() > 0 ){
			newPlaylistContentRotation.setContentRotation( contentRotation );
			
			// Save the content rotation but do not create permission entries since we are going to copy them	
			newPlaylistContentRotation.save( false );
			newPlaylistContentRotation.copyPermissionEntries( playlistContentRotationToCopy );
		}else{
			
			// Create a new "unnamed" content rotatation and copy the content rotation assets
			ContentRotation newContentRotation = ContentRotation.create();
			newPlaylistContentRotation.setContentRotation( newContentRotation );
			
			// Save the content rotation but do not create permission entries since we are going to copy them	
			newPlaylistContentRotation.save( false );
			newPlaylistContentRotation.copyPermissionEntries( playlistContentRotationToCopy );
			
			// Since this is an un-named CR
			newContentRotation.copyPermissionEntries(playlist, true, true);
			newContentRotation.setType(contentRotation.getType());
			
			// Set type and dynamic content if dynamic CR
			if(contentRotation.getType().equals("dynamic")){
				if(contentRotation.getDynamicContentType() != null && contentRotation.getDynamicContentType().equals("metadata")){
					for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(contentRotation)){
						DynamicQueryPart.create(null, newContentRotation, dqp.getAttrDefinition(), dqp.getOperator(), dqp.getValue(), dqp.getSelectedDate(), dqp.getNumDaysAgo(), dqp.getIncludeNull(), dqp.getSeqNum().intValue());
					}
				}else if(contentRotation.getNumAssets() != null){
					newContentRotation.setNumAssets(contentRotation.getNumAssets());
					for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(contentRotation)){
						DynamicContentPart.create(null, newContentRotation, dcp.getContentRotation(), null, null, dcp.getSeqNum());
					}
				}else if(contentRotation.getHql() != null){
					newContentRotation.setHql(contentRotation.getHql());
				}else if(contentRotation.getCustomMethod() != null){
					newContentRotation.setCustomMethod(contentRotation.getCustomMethod());
				}
				
				newContentRotation.setRunFromContentScheduler(contentRotation.getRunFromContentScheduler());
				newContentRotation.setUseRoles(contentRotation.getUseRoles());
				newContentRotation.setMaxResults(contentRotation.getMaxResults());
				newContentRotation.setDynamicContentType(contentRotation.getDynamicContentType());
				
				newContentRotation.update();
				
				newContentRotation.updateContentRotation(playlist, null, false);
			}else{
				newContentRotation.copyContentRotationAssets( contentRotation );
			}
		}
		
		// Copy contenRotationTarget objects
		for( Iterator i=playlistContentRotationToCopy.getContentRotationTargets().iterator(); i.hasNext(); )
		{
			ContentRotationTarget crt = (ContentRotationTarget)i.next();
			ContentRotationTarget.create( contentRotation, playlist, playlistContentRotationToCopy.getDisplayarea(), crt.getDevice() );
		}
		
		newPlaylistContentRotation.update();
		return newPlaylistContentRotation;
	}
	
	/**
	 * Determines if a ContentRotationTarget object exists for the given device
	 * within this PlaylistContentRotations's collection of ContentRotationTargets.
	 * @param device
	 * @return
	 */
	public boolean contentRotationTargetExists(Device device)
	{
		// Determine if there is already a ContentRotationTarget object for this device
		boolean foundContentRotationTargetForDevice = false;		
		for( Iterator j=this.getContentRotationTargets().iterator(); j.hasNext(); )
		{
			ContentRotationTarget crt = (ContentRotationTarget)j.next();
			if( crt.getDevice().getDeviceId() == device.getDeviceId() ) 
			{
				// Set the flag so we don't have to delete objects while iterating through its child collection
				foundContentRotationTargetForDevice = true;
				break;
			}
		}
		return foundContentRotationTargetForDevice;
	}
	
	public void addContentRotationTarget(Device device)
	{	
		ContentRotationTarget crt = new ContentRotationTarget();
		crt.setPlaylistContentRotation( this );
		crt.setDevice( device );
		crt.save();		
		this.contentRotationTargets.add( crt );
	}	
	
	public void removeContentRotationTargets()
	{
		for( Iterator i=this.contentRotationTargets.iterator(); i.hasNext(); )
		{
			ContentRotationTarget crt = (ContentRotationTarget)i.next();
			crt.delete();
		}
	}
	
	/**
	 * Deletes the ContentRotationTarget associated with this PlaylistContentRotation and device.
	 * @param device
	 */
	public void removeContentRotationTarget(Device device)
	{
		for( Iterator i=this.contentRotationTargets.iterator(); i.hasNext(); )
		{
			ContentRotationTarget crt = (ContentRotationTarget)i.next();
			if( crt.getDevice().getDeviceId() == device.getDeviceId() ){
				crt.delete();
				break;
			}			
		}
	}	
	
	public void delete()
	{	
		// Remove all targeting associated with the PlaylistContentRotation object
		this.getContentRotationTargets().clear();
		super.delete();			
	}
	
	/**
	 * 
	 * @return
	 */
	public List getContentRotationTargetsSorted()
	{
		if(this.contentRotationTargets != null)
		{				
			List l = new LinkedList( contentRotationTargets );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator("deviceName");
			BeanPropertyComparator comparator2 = new BeanPropertyComparator("device", comparator1 );
			Collections.sort( l, comparator2 );
			contentRotationTargetsSorted = l ;			
		}
		return contentRotationTargetsSorted;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PlaylistContentRotation) ) result = false;
		
		PlaylistContentRotation c = (PlaylistContentRotation) other;		
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
		result = Reformat.getSafeHash( this.getPlaylistContentRotationId(), result, 29 );
		result = Reformat.getSafeHash( this.getPlaylist().getPlaylistId(), result, 31 );
		result = Reformat.getSafeHash( this.getContentRotation().getContentRotationId(), result, 37 );
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId(), result, 39 );
		return result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPlaylistContentRotationId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getPlaylist().getPlaylistId();
	}	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getDisplayarea().getDisplayareaName();
	}	
	/**
	 * @return Returns the contentRotation.
	 */
	public ContentRotation getContentRotation() {
		return contentRotation;
	}
	

	/**
	 * @param contentRotation The contentRotation to set.
	 */
	public void setContentRotation(ContentRotation contentRotation) {
		this.contentRotation = contentRotation;
	}
	

	/**
	 * @return Returns the displayarea.
	 */
	public Displayarea getDisplayarea() {
		return displayarea;
	}
	

	/**
	 * @param displayarea The displayarea to set.
	 */
	public void setDisplayarea(Displayarea displayarea) {
		this.displayarea = displayarea;
	}
	

	/**
	 * @return Returns the playlist.
	 */
	public Playlist getPlaylist() {
		return playlist;
	}
	

	/**
	 * @param playlist The playlist to set.
	 */
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}
	

	/**
	 * @return Returns the playlistContentRotationId.
	 */
	public Long getPlaylistContentRotationId() {
		return playlistContentRotationId;
	}
	

	/**
	 * @param playlistContentRotationId The playlistContentRotationId to set.
	 */
	public void setPlaylistContentRotationId(Long playlistContentRotationId) {
		this.playlistContentRotationId = playlistContentRotationId;
	}

	/**
	 * @return the contentRotationTargets
	 */
	public Set<ContentRotationTarget> getContentRotationTargets() {
		return contentRotationTargets;
	}

	/**
	 * @param contentRotationTargets the contentRotationTargets to set
	 */
	public void setContentRotationTargets(
			Set<ContentRotationTarget> contentRotationTargets) {
		this.contentRotationTargets = contentRotationTargets;
	}

	/**
	 * @return Returns the isDefault.
	 */
	public Boolean getIsDefault() {
		return isDefault;
	}
	/**
	 * @param isDefault The isDefault to set.
	 */
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
}
