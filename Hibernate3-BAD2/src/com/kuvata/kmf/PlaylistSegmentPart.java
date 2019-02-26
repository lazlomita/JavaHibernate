package com.kuvata.kmf;

import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class PlaylistSegmentPart extends SegmentPart 
{
	private Long playlistSegmentPartId;
	private Playlist playlist;
	
	// Device side scheduling variable
	private Float length;
	/**
	 * 
	 *
	 */
	public PlaylistSegmentPart()
	{		
	}
	/**
	 * 
	 */
	public float getLength(){
		if(length != null){
			// This is always set on the device side
			return length;
		}else{
			return playlist.getLength().floatValue();
		}
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PlaylistSegmentPart) ) result = false;
		else{
			PlaylistSegmentPart p = (PlaylistSegmentPart) other;		
			if(this.hashCode() == p.hashCode())
				result =  true;
		}
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PlaylistSegmentPart".hashCode();
		result = Reformat.getSafeHash( this.getPlaylistSegmentPartId(), result, 3 );
		result = Reformat.getSafeHash( this.getPlaylist().getPlaylistId(), result, 7 );
		result = Reformat.getSafeHash( this.getSegment().getSegmentId(), result, 11);
		result = Reformat.getSafeHash( this.getSeqNum(), result, 13 );
		return result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPlaylistSegmentPartId();
	}
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getPlaylist().getPlaylistName();
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
	 * @return Returns the playlistSegmentPartId.
	 */
	public Long getPlaylistSegmentPartId() {
		return playlistSegmentPartId;
	}

	/**
	 * @param playlistSegmentPartId The playlistSegmentPartId to set.
	 */
	public void setPlaylistSegmentPartId(Long playlistSegmentPartId) {
		this.playlistSegmentPartId = playlistSegmentPartId;
	}
	public void setLength(Float length) {
		this.length = length;
	}

}
