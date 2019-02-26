package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.asset.Audio;
import com.kuvata.kmf.asset.Video;

public class CalculateNormalizationBeginAvg {
	
	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", "CalculateNormalizationBeginAvg", null, false, true);
		
		String filePath = "/kuvata/tmp/beginAvg";
		File f = new File(filePath);
		
		List<Asset> l = HibernateSession.currentSession().createQuery("FROM Audio WHERE normalizationBeginAvg IS NULL").list();
		for(Asset a : l){
			try {
				
				// Delete old file
				f.delete();
				
				// Get the audio normalization begin average
				String[] cmd = {"/bin/sh", "-c", "mplayer -quiet -ao null -vo null -af volnorm=2:0.25:-1 " + a.getFileloc() + " >" + filePath + " 2>/dev/null"};
				Process p = Runtime.getRuntime().exec(cmd);
				
				// Give it 5 seconds to execute
				Thread.sleep(5000);
				
				// Kill the process if its still running
				try {
					p.exitValue();
				} catch (Exception e) {
					p.destroy();
					p.waitFor();
				}
				
				p.getErrorStream().close();
				p.getOutputStream().close();
				p.getInputStream().close();
				p = null;
				
				Integer normalizationBeginAvg = null;
				String buffer;
				
				if(f.exists()){
					FileReader fr = new FileReader(f);
					BufferedReader in = new BufferedReader(fr);
					while((buffer = in.readLine()) != null){
						if(buffer.contains("beginAvg")){
							normalizationBeginAvg = (int)Float.parseFloat(buffer.split("=")[1].trim());
						}
					
					}
					in.close();
					fr.close();
				}
				
				if(normalizationBeginAvg != null){
					System.out.println("Setting normalizationBeginAvg for assetId: " + a.getAssetId() + " at " + normalizationBeginAvg);
					((Audio)a).setNormalizationBeginAvg(normalizationBeginAvg);
					a.update();
				}else{
					System.out.println("Setting normalizationBeginAvg for assetId: " + a.getAssetId() + " at null");
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		l = HibernateSession.currentSession().createQuery("FROM Video WHERE normalizationBeginAvg IS NULL").list();
		for(Asset a : l){
			try {
				
				// Delete old file
				f.delete();
				
				// Get the audio normalization begin average
				String[] cmd = {"/bin/sh", "-c", "mplayer -quiet -ao null -vo null -af volnorm=2:0.25:-1 " + a.getFileloc() + " >" + filePath + " 2>/dev/null"};
				Process p = Runtime.getRuntime().exec(cmd);
				
				// Give it 5 seconds to execute
				Thread.sleep(5000);
				
				// Kill the process if its still running
				try {
					p.exitValue();
				} catch (Exception e) {
					p.destroy();
					p.waitFor();
				}
				
				p.getErrorStream().close();
				p.getOutputStream().close();
				p.getInputStream().close();
				p = null;
				
				Integer normalizationBeginAvg = null;
				String buffer;
				
				if(f.exists()){
					FileReader fr = new FileReader(f);
					BufferedReader in = new BufferedReader(fr);
					while((buffer = in.readLine()) != null){
						if(buffer.contains("beginAvg")){
							normalizationBeginAvg = (int)Float.parseFloat(buffer.split("=")[1].trim());
						}
					}
					in.close();
					fr.close();
				}
				
				if(normalizationBeginAvg != null){
					System.out.println("Setting normalizationBeginAvg for assetId: " + a.getAssetId() + " at " + normalizationBeginAvg);
					((Video)a).setNormalizationBeginAvg(normalizationBeginAvg);
					a.update();
				}else{
					System.out.println("Setting normalizationBeginAvg for assetId: " + a.getAssetId() + " at null");
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
