package com.kuvata.kmf;

import java.util.Date;

import org.hibernate.HibernateException;

import parkmedia.usertype.ContentRotationImportType;

import com.kuvata.kmf.logging.HistorizableCollectionMember;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class PlaylistImport extends Entity implements HistorizableCollectionMember 
{
	private Long playlistImportId;
	private Playlist playlist;
	private ContentRotation contentRotation;
	private Layout layout;
	private Displayarea displayarea;
	private Date importDate;
	private ContentRotationImportType importType;	
	
	/**
	 * 
	 *
	 */
	public PlaylistImport()
	{		
	}
	
	/**
	 * Returns a PlaylistImport with the given playlistImportId
	 * 
	 * @param playlistId
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistImport getPlaylistImport(Long playlistImportId) throws HibernateException
	{
		return (PlaylistImport)Entity.load(PlaylistImport.class, playlistImportId);		
	}	
	
	/**
	 * Creates a new PlaylistImport object.
	 * 
	 * @param playlist
	 * @param contentRotation
	 * @param layout
	 * @param displayarea
	 * @param importDate
	 * @param importType
	 */
	public static void create(Playlist playlist, ContentRotation contentRotation, Layout layout, Displayarea displayarea, Date importDate, ContentRotationImportType importType)
	{
		// Strange, but true -- we have to put the collection of playlistImports into memory before adding to its collection
		// If we do not do this, the collection will not become and will not historize correctly
		int numPlaylistImports = playlist.getPlaylistImports().size();
		PlaylistImport playlistImport = new PlaylistImport();
		playlistImport.setPlaylist( playlist );
		playlistImport.setContentRotation( contentRotation );
		playlistImport.setLayout( layout );
		playlistImport.setDisplayarea( displayarea );		
		playlistImport.setImportDate( importDate );
		playlistImport.setImportType( importType );
		playlistImport.save();
		playlist.getPlaylistImports().add( playlistImport );
		playlist.update();
	}
	
	
			
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPlaylistImportId();
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
		return this.getContentRotation().getContentRotationName();
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
	 * @return Returns the playlistImportId.
	 */
	public Long getPlaylistImportId() {
		return playlistImportId;
	}
	
	/**
	 * @param playlistImportId The playlistImportId to set.
	 */
	public void setPlaylistImportId(Long playlistImportId) {
		this.playlistImportId = playlistImportId;
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
	 * @return Returns the layout.
	 */
	public Layout getLayout() {
		return layout;
	}
	

	/**
	 * @param layout The layout to set.
	 */
	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	/**
	 * @return Returns the importDate.
	 */
	public Date getImportDate() {
		return importDate;
	}
	

	/**
	 * @param importDate The importDate to set.
	 */
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}

	/**
	 * @return Returns the importType.
	 */
	public ContentRotationImportType getImportType() {
		return importType;
	}
	

	/**
	 * @param importType The importType to set.
	 */
	public void setImportType(ContentRotationImportType importType) {
		this.importType = importType;
	}
	
	
	

}
