package com.kuvata.kmf.util;

import java.lang.reflect.Constructor;
import java.util.List;


import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.presentation.Presentation;

public class RegeneratePresentationFiles {
	
	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", "RegeneratePresentationFiles", null, false, true);
		
		System.out.println("Deleting all Audio presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/audio/*.xml"}).waitFor();
		serializeAssets("Audio");
		
		System.out.println("Deleting all Flash presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/flash/*.xml"}).waitFor();
		serializeAssets("Flash");
		
		System.out.println("Deleting all Image presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/image/*.xml"}).waitFor();
		serializeAssets("Image");
		
		System.out.println("Deleting all Ticker presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/ticker/*.xml"}).waitFor();
		serializeAssets("Ticker");
		
		System.out.println("Deleting all Url presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/url/*.xml"}).waitFor();
		serializeAssets("Url");
		
		System.out.println("Deleting all Video presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/video/*.xml"});
		serializeAssets("Video");
		
		// We will wait till the next run of the CS to generate xml files for the following two asset types since they are device specific
		System.out.println("Deleting all Html presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/html/*.xml"}).waitFor();
		
		System.out.println("Deleting all Webapp presentation xml files");
		Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "rm -f /kuvata/schemas/kuvata/presentations/webapp/*.xml"}).waitFor();
	}
	
	private static void serializeAssets(String type) throws Exception{
		List<Asset> l = HibernateSession.currentSession().createQuery("FROM " + type).list();
		for(Asset asset : l){
			try {
				System.out.println("Generating presentation xml for " + asset.getAssetName() + " of type " + type);
		        Class<?> cls = Class.forName( asset.getPresentationType() );
		        Class<?> partypes[] = new Class[1];
		        partypes[0] = asset.getClass();
		        Constructor<?> ct = cls.getConstructor(partypes);
		        Object arglist[] = new Object[1];
		        arglist[0] = asset;
		        Presentation presentation = (Presentation) ct.newInstance(arglist);
		        presentation.xmlEncode();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
