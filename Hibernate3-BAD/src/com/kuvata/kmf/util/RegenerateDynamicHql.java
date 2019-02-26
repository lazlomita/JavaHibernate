package com.kuvata.kmf.util;

import java.util.List;

import org.hibernate.Session;

import com.kuvata.kmf.ContentRotation;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.SchemaDirectory;

public class RegenerateDynamicHql {

	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", RegenerateDynamicHql.class.getName(), null, false, true);
		Session session = HibernateSession.currentSession();
		List<Long> playlists = session.createQuery("SELECT DISTINCT playlist.playlistId FROM DynamicQueryPart").list();
		for(Long id : playlists){
			if(id != null){
				Playlist p = Playlist.getPlaylist(id);
				System.out.println("Updating playlist " + p.getPlaylistName());
				p.generateHql(null);
			}
		}
		List<Long> crs = session.createQuery("SELECT DISTINCT contentRotation.contentRotationId FROM DynamicQueryPart").list();
		for(Long id : crs){
			if(id != null){
				ContentRotation cr = ContentRotation.getContentRotation(id);
				System.out.println("Updating content rotation " + cr.getContentRotationName());
				cr.generateHql(null);
			}
		}
	}
}
