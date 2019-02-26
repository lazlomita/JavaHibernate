package com.kuvata.kmf;

import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class PlaylistGrpMember extends GrpMember
{
	private Long playlistGrpMemberId;
	private Playlist playlist;	
		
	public PlaylistGrpMember()
	{		
	}
	
	/**
	 * Implements the abstract method GrpMember.getName()
	 */	
	public String getName()
	{
		return this.getPlaylist().getPlaylistName();
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PlaylistGrpMember) ) result = false;
		
		PlaylistGrpMember pgm = (PlaylistGrpMember) other;		
		if(this.hashCode() == pgm.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PlaylistGrpMember".hashCode();
		result = Reformat.getSafeHash( this.getGrp().getGrpId(), result, 13 );
		result = Reformat.getSafeHash( this.getPlaylist().getPlaylistId(), result, 13 );
		return result;
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return super.getEntityId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getGrp().getGrpId();
	}		
	/**
	 * 
	 */
	public Entity getChild()
	{
		return this.getPlaylist();
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
	 * @return Returns the playlistGrpMemberId.
	 */
	public Long getPlaylistGrpMemberId() {
		return playlistGrpMemberId;
	}

	/**
	 * @param playlistGrpMemberId The playlistGrpMemberId to set.
	 */
	public void setPlaylistGrpMemberId(Long playlistGrpMemberId) {
		this.playlistGrpMemberId = playlistGrpMemberId;
	}

}
