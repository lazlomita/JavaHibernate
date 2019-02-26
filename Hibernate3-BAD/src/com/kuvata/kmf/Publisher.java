package com.kuvata.kmf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.hibernate.HibernateException;

import parkmedia.KMFLogger;

import com.kuvata.dispatcher.scheduling.ContentSchedulerArg;
import com.kuvata.dispatcher.services.Dispatchable;
import com.kuvata.dispatcher.services.DispatchableServiceLocator;

/**
 * @author jrandesi
 *
 * 
 */
public class Publisher {
		
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Publisher.class );
	
	/**
	 * 
	 * @param updateCurrentContent
	 * @param args
	 * @throws HibernateException
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public static void publishAll(boolean updateCurrentContent, ContentSchedulerArg[] args) 
		throws HibernateException, IOException, ServiceException, RemoteException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException, electric.xml.ParseException, FileNotFoundException
	{		
		// Convert the list of dirty objects to a list of dirtyIds		
		List<Dirty> dirtyEntities = Dirty.getDirtyEntities();
		List<Long> dirtyIds = new ArrayList<Long>();
		for( Iterator<Dirty> i=dirtyEntities.iterator(); i.hasNext(); ){
			dirtyIds.add( i.next().getDirtyId() );
		}
		
		// Publish all the dirtyIds
		Dirty.publish( dirtyIds );		

		// If the updateCurrentContent flag is true
		if( updateCurrentContent == true )
		{			
			// If any ContentSchedulerArgs were passed in
			if( args != null )
			{
	 			// Launch the content scheduler in it's own thread		
				logger.info("About to run content scheduler");
				Dispatchable dispatcher = DispatchableServiceLocator.getJobServerDispatcher();
				dispatcher.runContentScheduler( SchemaDirectory.getSchema().getSchemaName(), null, null, args, true, KmfSession.getKmfSession().getAppUserId() );
				logger.info("Finished running content scheduler");				
			}
			else
			{
				// TODO: Run the Content Scheduler for all devices that are out of date
				// dispatcher.runContentScheduler( SchemaDirectory.getSchema().getSchemaName() );
			}			
		}			
	}
	/**
	 * 
	 * @param p
	 * @throws HibernateException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws SQLException
	 */
	public static void publishPlaylist(Playlist p) 
		throws HibernateException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, SQLException
	{		
		// Update the length of this playlist and clear the dirty status
		p.updateLength();
		Dirty.makeNotDirty( p );	
				
		// Update the length of all segments that contain this playlist		
		for( Iterator i = p.getPlaylistSegmentParts().iterator(); i.hasNext(); )
		{
			PlaylistSegmentPart psp = (PlaylistSegmentPart)i.next();
			Segment s = psp.getSegment();
			s.updateLength();
			
			// Clear the dirty status for the segment
			Dirty.makeNotDirty( s );
		}		
	}	
		
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static boolean contentOutOfDate() throws HibernateException
	{
		boolean result = false;
		
		// Continue until we've found a device that has content out of date
		Iterator dirtySegments = Segment.getDirtySegments().iterator();
		while( (dirtySegments.hasNext()) && (result == false) )
		{
			// Update their length
			Segment s = (Segment)dirtySegments.next();
			s.updateLength();
			
			// For each device containing this segment
			List l = Device.getDevices( s );
			for( Iterator i = l.iterator(); i.hasNext(); )
			{
				// If the content currently on the device is out of date
				Device d = (Device)i.next();
				if(d.contentOutOfDate( s ))
				{
					// Set the flag to exit the loop
					result = true;					
				}				
			}
		}	
		return result;
	}

}
