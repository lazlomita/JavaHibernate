package com.kuvata.kmf.util;

import java.util.List;

import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.PlaylistContentRotation;
import com.kuvata.kmf.PlaylistDisplayarea;
import com.kuvata.kmf.SchemaDirectory;

public class FixMissingPlaylistDisplayareaPlaylists {

	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", FixMissingPlaylistDisplayareaPlaylists.class.getName(), null, false, true);
		
		// Get all playlists that don't have any rows in the playlist_displayarea table
		String hql = "SELECT p FROM Playlist p WHERE p.playlistId NOT IN (SELECT DISTINCT(pd.playlist.playlistId) FROM PlaylistDisplayarea pd)";
		List<Playlist> playlists = HibernateSession.currentSession().createQuery(hql).list();
		for(Playlist p : playlists){
			// Verify that this has playlist content rotations
			if(p.getPlaylistContentRotations().size() > 0){
				System.out.println("Creatint rows for playlist " + p.getPlaylistName());
				// Create missing playlist_displayarea rows
				for(PlaylistContentRotation pcr : p.getPlaylistContentRotations()){
					if(PlaylistDisplayarea.getPlaylistDisplayarea(p, pcr.getDisplayarea()) == null){
						System.out.println("Creating playlist_displayarea for " + pcr.getDisplayarea().getDisplayareaName());
						PlaylistDisplayarea.create(p, pcr.getDisplayarea(), false);
					}
				}
			}
		}
	}
}
