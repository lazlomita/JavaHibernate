package com.kuvata.kmf.util;

import java.text.DecimalFormat;

import parkmedia.KMFLogger;


public class JvmMemoryLogger extends Thread{
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( JvmMemoryLogger.class );
	
	public void run(){
		while(true){
			
			Runtime r = Runtime.getRuntime();
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			
			try {
				long bytesUsed = r.totalMemory() - r.freeMemory();
				float megsUsed = bytesUsed / (1024 * 1024);
				logger.info("Memory usage: " + df.format(megsUsed) + " MB");
				
				// Sleep for 1 minute
				Thread.sleep(60000);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}
