/*
 * Created on Sep 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.kuvata.kmf;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.util.Reformat;

/**
 * @author Jeff Mattson
 *
 */
public class ExcludedPlaylist extends Entity
{	
	private Long excludedPlaylistId;
	private Playlist ownerPlaylist;	
	private Playlist playlist;
	/**
	 * 
	 *
	 */
	public ExcludedPlaylist()
	{
	}
	/**
	 * 
	 * @param ownerPlaylist
	 * @return
	 * @throws HibernateException
	 */
	public static List getExcludedPlaylists(Playlist ownerPlaylist) throws HibernateException
	{		
		Session session = HibernateSession.currentSession();					
		List l = session.createCriteria( ExcludedPlaylist.class )
			.add( Expression.eq("ownerPlaylist.playlistId", ownerPlaylist.getPlaylistId()) )
			.list();
		return l;
	}
	/**
	 * 
	 */		
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof ExcludedPlaylist) ) result = false;
	
		ExcludedPlaylist o = (ExcludedPlaylist) other;		
		if(this.hashCode() == o.hashCode())
			result =  true;
	
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "ExcludedPlaylist".hashCode();
		result = Reformat.getSafeHash( this.getOwnerPlaylist().getPlaylistId(), result, 13 );
		result = Reformat.getSafeHash( this.getExcludedPlaylistId(), result, 13 );		
		return result;
	}
	/**
	 * 
	 */
	public Long getEntityId() 
	{
		return this.getExcludedPlaylistId();
	}
	/**
	 * @return Returns the excludedPlaylistId.
	 */
	public Long getExcludedPlaylistId() {
		return excludedPlaylistId;
	}
	/**
	 * @param excludedPlaylistId The excludedPlaylistId to set.
	 */
	public void setExcludedPlaylistId(Long excludedPlaylistId) {
		this.excludedPlaylistId = excludedPlaylistId;
	}
	/**
	 * @return Returns the ownerPlaylist.
	 */
	public Playlist getOwnerPlaylist() {
		return ownerPlaylist;
	}
	/**
	 * @param ownerPlaylist The ownerPlaylist to set.
	 */
	public void setOwnerPlaylist(Playlist ownerPlaylist) {
		this.ownerPlaylist = ownerPlaylist;
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
}
