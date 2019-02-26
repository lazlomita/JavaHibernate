package com.kuvata.kmf.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import parkmedia.KMFLogger;
import parkmedia.usertype.AssetType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.asset.Audio;
import com.kuvata.kmf.asset.MediaInfo;
import com.kuvata.kmf.asset.Video;

public class DetectMediaLength {
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( DetectMediaLength.class );
	
	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", "DetectMediaLength", null, false, true);
		
		List<Class> entityClasses = new ArrayList<Class>();
		entityClasses.add(Video.class);
		entityClasses.add(Audio.class);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		String hql = "SELECT he.entityId FROM HistoryEntry he WHERE he.property = :property AND he.action = :action AND he.entityClass IN (:entityClasses) and he.timestamp > :timestamp";
		
		List<Long> l = HibernateSession.currentSession().createQuery(hql).setParameter("timestamp", sdf.parse("2012/09/20"))
		.setParameter("property", "length").setParameter("action", "update").setParameterList("entityClasses", entityClasses).list();
		
		for(Long assetId : l){
			Asset a = Asset.getAsset(assetId);
			if(a != null && (a.getAssetType().equals(AssetType.VIDEO) || a.getAssetType().equals(AssetType.AUDIO))){
				
				logger.info("Calculating length for " + a.getAssetName());
				MediaInfo mi = MediaInfo.getMediaInfo(a);

				if(a.getAssetType().equals(AssetType.VIDEO)){
					Video v = (Video)a;
					v.setLength(mi.getLength());
					v.update();
				}else if(a.getAssetType().equals(AssetType.AUDIO)){
					Audio audio = (Audio)a;
					audio.setLength(mi.getLength());
					audio.update();
				}
			}
		}
	}
}
