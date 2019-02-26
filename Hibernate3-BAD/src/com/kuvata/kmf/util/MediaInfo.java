/*
 * Created on Nov 10, 2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;

/**
 * @author anaber
 *
 */
public class MediaInfo implements ControllerListener {	
	
	private static Player player = null;
	private static String mediaFile = null;
	private static double dur = 0;
	
	public MediaInfo(String MediaFile)
	{		
		mediaFile = MediaFile;
	}

	public String getDuration()
	{		
		String duration = "";

		try 
		{
			MediaLocator locator = new MediaLocator("file:" + mediaFile);
			if(locator == null) return duration;
			player = Manager.createPlayer(locator);	
			player.addControllerListener(this);
			player.realize();			
		
			while (dur == 0)
			{
				Thread.sleep(200);
			}

			NumberFormat formatter = new DecimalFormat("0.00");
    		duration = formatter.format(dur);     		
		}
		catch(Exception e)
		{			
			System.err.println(e.toString());	
		}	
		
		return duration;
	}	

	void realizeComplete() 
	{  		
 		player.prefetch();
	}
 
	void prefetchComplete() 
	{		
		Time t = player.getDuration();	
		dur = t.getSeconds();
		player.close();
   		// player.start();
 	}

 	void controllerError() 
	{
  		System.err.println("Controller Error");
		player.close();
		dur = -1;		
  		player = null;
 	}

 	void controllerClosed() 
	{
   		player.realize();
  	}  
	
	public synchronized void controllerUpdate(ControllerEvent e) 
	{		
		// Determine event type
		if(e instanceof RealizeCompleteEvent) realizeComplete();
		else if(e instanceof PrefetchCompleteEvent) prefetchComplete();  
	 	else if(e instanceof ControllerErrorEvent) controllerError();
		else if(e instanceof ControllerClosedEvent) controllerClosed();
	}
	
	public static void main(String[] args) 
	{
		if(args.length != 1)
		{			
			System.out.println("Usage: MediaInfo MediaFile");
		}
		else
		{
			MediaInfo info = new MediaInfo(args[0]);
			String dur = info.getDuration();
			System.out.println("Duration: " + dur);	
		}
	}
}