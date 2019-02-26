package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class PlaylistDisplayarea extends Entity {

	private Long playlistDisplayareaId;
	private Playlist playlist;
	private Displayarea displayarea;
	private Boolean isPrimary;
	
	/**
	 * 
	 *
	 */
	public PlaylistDisplayarea()
	{		
	}
	
	/**
	 * 
	 * @param p
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistDisplayarea getPlaylistDisplayarea(Playlist p, Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		PlaylistDisplayarea pd = (PlaylistDisplayarea)session.createCriteria(PlaylistDisplayarea.class)					
				.add( Expression.eq("playlist.playlistId", p.getPlaylistId()) )
				.add( Expression.eq("displayarea.displayareaId", da.getDisplayareaId()) )
				.uniqueResult();							
		return pd;		
	}	
	
	/**
	 * 
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylistDisplayareas(Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(PlaylistDisplayarea.class)									
				.add( Expression.eq("displayarea.displayareaId", da.getDisplayareaId()) )
				.add( Expression.eq("isPrimary", Boolean.FALSE ) )
				.list();									
	}		
	
	public static PlaylistDisplayarea create(Playlist p, Displayarea da, boolean isPrimary)
	{
		PlaylistDisplayarea pd = new PlaylistDisplayarea();		
		pd.setPlaylist( p );
		pd.setDisplayarea( da );		
		pd.setIsPrimary( isPrimary );
		pd.save();
		return pd;
	}	
	
	/**
	 * Converts the list of non-primary PlaylistDisplayareas associated
	 * with the given playlist into Displayarea objects.
	 * @return
	 */
	public static List getSecondaryPlaylistDisplayareas(Playlist p)
	{
		Session session = HibernateSession.currentSession();		
		String hql = "SELECT pd "
			+ "FROM PlaylistDisplayarea as pd "
			+ "WHERE pd.playlist.playlistId = "+ p.getPlaylistId() +" "
			+ "AND pd.isPrimary = ? "
			+ "ORDER BY UPPER(pd.displayarea.displayareaName)";			
		List playlistDisplayareas = session.createQuery( hql ).setParameter(0, false).list();			

		// Convert to displayareas
		ArrayList result = new ArrayList();		
		for( Iterator i=playlistDisplayareas.iterator(); i.hasNext(); )
		{
			PlaylistDisplayarea pda = (PlaylistDisplayarea)i.next();
			Hibernate.initialize( pda.getDisplayarea() );
			result.add( pda.getDisplayarea() );
		}
		return result;	
	}	
	
	public static PlaylistDisplayarea copy(PlaylistDisplayarea playlistDisplayareaToCopy, Playlist playlistToCopy)
	{
		PlaylistDisplayarea newPlaylistDisplayarea = new PlaylistDisplayarea();
		newPlaylistDisplayarea.setPlaylist( playlistToCopy );
		newPlaylistDisplayarea.setDisplayarea( playlistDisplayareaToCopy.getDisplayarea() );
		newPlaylistDisplayarea.setIsPrimary( playlistDisplayareaToCopy.getIsPrimary() );
		
		// Save the playlist displayarea but do not create permission entries since we are going to copy them	
		newPlaylistDisplayarea.save( false );
		
		// Copy the permissions of the playlist displayarea we're copying from
		newPlaylistDisplayarea.copyPermissionEntries( playlistDisplayareaToCopy );						
		return newPlaylistDisplayarea;
	}
	
	/**
	 * 
	 * @param PlaylistDisplayareaId
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistDisplayarea getPlaylistDisplayarea(Long playlistDisplayareaId) throws HibernateException
	{
		return (PlaylistDisplayarea)Entity.load(PlaylistDisplayarea.class, playlistDisplayareaId);		
	}	
	
	/**
	 * Calls playlist.resetPlaylistDisplayareas() for each playlist containing the given layout.
	 * @param layout
	 */
	public static void resetPlaylistDisplayareas(Layout layout)
	{
		// Get all playlists that contain this layout
		// NOTE: We're assuming each playlist_asset has its own asset_presentation
		// NOTE: Cannot select DISTINCT from playlist because the playlist object contains a property of type CLOB 
		Session session = HibernateSession.currentSession();
		String hql = "SELECT p "
				+ "FROM Playlist as p "
				+ "WHERE p.playlistId IN "
				+ "(SELECT DISTINCT playlist.playlistId FROM Playlist playlist "
				+ "JOIN playlist.playlistAssets as pa "
				+ "JOIN pa.assetPresentation.layout.layoutDisplayareas as lda "	
				+ "JOIN lda.layout as layout "
				+ "WHERE layout.layoutId= "+ layout.getLayoutId().toString() +" "
				+ "AND pa.assetPresentation IS NOT NULL)";	
		List l = session.createQuery( hql ).list();	
		
		// For each playlist containing this layout
		for( Iterator i=l.iterator(); i.hasNext(); )
		{
			// Reset the playlist displayareas for this playlist
			Playlist p = (Playlist)i.next();
			p.resetPlaylistDisplayareas();
		}
	}
		
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PlaylistDisplayarea) ) result = false;
		
		PlaylistDisplayarea pda = (PlaylistDisplayarea) other;		
		if(this.hashCode() == pda.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PlaylistDisplayarea".hashCode();
		result = Reformat.getSafeHash( this.getPlaylistDisplayareaId(), result, 13 );
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId(), result, 29 );
		result = Reformat.getSafeHash( this.getPlaylist().getPlaylistId(), result, 31 );			
		return result;
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPlaylistDisplayareaId();
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
	 * @return Returns the playlistDisplayareaId.
	 */
	public Long getPlaylistDisplayareaId() {
		return playlistDisplayareaId;
	}
	
	/**
	 * @param playlistDisplayareaId The playlistDisplayareaId to set.
	 */
	public void setPlaylistDisplayareaId(Long playlistDisplayareaId) {
		this.playlistDisplayareaId = playlistDisplayareaId;
	}

	/**
	 * @return Returns the isPrimary.
	 */
	public Boolean getIsPrimary() {
		return isPrimary;
	}
	

	/**
	 * @param isPrimary The isPrimary to set.
	 */
	public void setIsPrimary(Boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	


}
