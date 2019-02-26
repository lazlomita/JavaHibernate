package com.kuvata.kmf.util;

import java.util.Iterator;

import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.PlaylistContentRotation;
import com.kuvata.kmf.SchemaDirectory;

public class FixPlaylistPermissions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SchemaDirectory.initialize("kuvata", "FixPlaylistPermissions", null, false, true);
		
		// For each playlist
		for(Iterator<Playlist> i=Playlist.getPlaylists().iterator();i.hasNext();){
			Playlist p = i.next();
			
			// For each playlist content rotation
			for(Iterator<PlaylistContentRotation> j=p.getPlaylistContentRotations().iterator();j.hasNext();){
				PlaylistContentRotation pcr = j.next();
				
				// If this is an un-named content rotation
				if(pcr.getContentRotation().getContentRotationName() == null || pcr.getContentRotation().getContentRotationName().length() == 0){
					System.out.println("Fixing permissions for un-named content rotation in " + p.getPlaylistName());
					pcr.getContentRotation().copyPermissionEntries(p, true, true);
				}
			}
		}
	}

}
