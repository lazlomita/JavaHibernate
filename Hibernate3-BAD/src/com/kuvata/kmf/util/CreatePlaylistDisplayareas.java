package com.kuvata.kmf.util;

import java.util.Iterator;

import parkmedia.KMFLogger;

import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.PlaylistAsset;
import com.kuvata.kmf.SchemaDirectory;

/**
 * Creates PlaylistDisplayarea objects for all existing playlists
 * @author jrandesi
 *
 */
public class CreatePlaylistDisplayareas {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( CreatePlaylistDisplayareas.class );
	
	/*
	 * Creates rows in the playlist_displayarea for each existing playlist
	 */	
	public static void doIt(String schema) throws Exception
	{		
		SchemaDirectory.initialize( schema, "CreatePlaylistDisplayareas", null, false, true );
		
		// For each playlist
		for( Iterator i=Playlist.getPlaylists().iterator(); i.hasNext(); )
		{
			// For each asset in this playlist
			Playlist playlist = (Playlist)i.next();
			System.out.println("Creating rows row playlist: "+ playlist.getPlaylistName());
			for( Iterator j=playlist.getPlaylistAssets().iterator(); j.hasNext(); )
			{
				// Create PlaylistDisplayarea objects for secondary displayareas if necessary
				PlaylistAsset pa = (PlaylistAsset)j.next();
				playlist.createPlaylistDisplayareas( pa );				
			}
		}
		System.out.println("Done");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		if( args.length != 1 ){
			System.out.println("Usage: CreatePlaylistDisplayareas schema");
		}
		else
		{
			try {
				doIt( args[0] );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				HibernateSession.closeSession();
			}
		}
	}

}
