package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.asset.Video;

public class DetectVideoCodecs {
	
	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", "DetectVideoCodecs", null, false, true);
		
		List<Asset> l = HibernateSession.currentSession().createQuery("FROM Video WHERE videoCodec IS NULL").list();
		for(Asset a : l){
			try {
				// Get codec info from ffmpeg
				Process p = Runtime.getRuntime().exec("/usr/bin/ffmpeg -i " + a.getFileloc());
				String videoCodec = null;
				String buffer;
				InputStreamReader isr = new InputStreamReader( p.getErrorStream() );
				BufferedReader se = new BufferedReader( isr );
				while((buffer = se.readLine()) != null){
					if(buffer.matches(".*Stream.*Video:.*")){
						videoCodec = buffer.substring(buffer.indexOf("Video:") + 7);
						videoCodec = videoCodec.substring(0, videoCodec.indexOf(" "));
						videoCodec = videoCodec.replaceAll(",", "");
					}
				
				}
				se.close();
				isr.close();
				p.getOutputStream().close();
				p.getErrorStream().close();
				p.getInputStream().close();
				p = null;
				
				if(videoCodec != null){
					System.out.println("Setting videoCodec for assetId: " + a.getAssetId() + " to " + videoCodec);
					((Video)a).setVideoCodec(videoCodec);
					a.update();
				}else{
					System.out.println("Setting videoCodec for assetId: " + a.getAssetId() + " to null");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
