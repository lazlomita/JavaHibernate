/*
 * Created on Mar 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;

import parkmedia.KMFLogger;
import parkmedia.usertype.ContentRotationImportType;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.ContentRotation;
import com.kuvata.kmf.ContentRotationImport;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.PlaylistImport;
import com.kuvata.kmf.SchemaDirectory;

/**
 * Determines if there are any playlists that need content rotations to be imported
 * and performs the import.
 * 
 * @author jrandesi
 */
public class ImportContentRotationRunner extends TimerTask
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( ImportContentRotationRunner.class );
	private String schema;
	private String importId;
	private static HashMap scheduledContentRotationImports = new HashMap();
	
	public ImportContentRotationRunner(String schema, String importId)
	{		
		this.schema = schema;
		this.importId = importId;
	}
	
	public static void manageContentRotationImports()
	{
		try
		{
			// If the schemas have not already been loaded			
			if( SchemaDirectory.schemas == null )
			{
				// Load them
				logger.info("Loading schemas");
				SchemaDirectory.loadSchemas();
			}

			// For each schema that has been loaded into the schemas hashmap			
			for (Iterator i = SchemaDirectory.schemas.keySet().iterator(); i.hasNext(); )
			{
				String schemaName = String.valueOf( i.next() );

				// Skip the BASE_SCHEMA
				if( schemaName.equals( Constants.BASE_SCHEMA ) == false )
				{
					logger.info("Executing ContentRotationImportRunner for "+ schemaName +" schema.");
					SchemaDirectory.setup( schemaName, "ImportContentRotationIntoPlaylistRunner" );

					// For each playlist
					List l = Playlist.getPlaylists();
					for( Iterator j=l.iterator(); j.hasNext(); )
					{
						// For each content rotation to import
						Playlist p = (Playlist)j.next();
						for( Iterator k=p.getPlaylistImports().iterator(); k.hasNext(); )
						{
							// If the import date is in the future
							PlaylistImport playlistImport = (PlaylistImport)k.next();
							if( playlistImport.getImportDate().after( Calendar.getInstance().getTime() ) )
							{
								// Attempt to retrieve this import in our collection of scheduled imports
								ImportTimer t = (ImportTimer)scheduledContentRotationImports.get( playlistImport.getPlaylistImportId() );
								
								// If this import has already been scheduled, but the date/time of the import has changed
								if( t != null && t.getImportDate().getTime() != playlistImport.getImportDate().getTime() )
								{
									// Cancel the timer so we can re-schedule it with the appropriate time
									logger.info("The content rotation import date has changed for playlist: "+ p.getPlaylistId() +". Rescheduling import.");
									t.cancel();
									scheduledContentRotationImports.remove( playlistImport.getPlaylistImportId() );
									t = null;
								}
								
								// If we have not yet scheduled this import
								if( t == null )
								{
									// Create a timer task for this import
									logger.info("Scheduling import of content rotation: "+ playlistImport.getContentRotation().getContentRotationName() +" into playlist: "+ p.getPlaylistName() +" scheduled for "+ playlistImport.getImportDate());
									t = new ImportTimer();
									t.setImportDate( playlistImport.getImportDate() );
									t.schedule( new ImportContentRotationRunner( schemaName, playlistImport.getPlaylistImportId().toString()), playlistImport.getImportDate() );
									scheduledContentRotationImports.put( playlistImport.getPlaylistImportId(), t );									
								}
							}	
						}		
					}
					
					// For each content rotation
					l = ContentRotation.getNamedContentRotations();
					for( Iterator j=l.iterator(); j.hasNext(); )
					{
						// For each content rotation to import
						ContentRotation cr = (ContentRotation)j.next();
						for( Iterator k=cr.getContentRotationImports().iterator(); k.hasNext(); )
						{
							// If the import date is in the future
							ContentRotationImport crImport = (ContentRotationImport)k.next();
							if( crImport.getImportDate().after( Calendar.getInstance().getTime() ) )
							{
								// Attempt to retrieve this import in our collection of scheduled imports
								ImportTimer t = (ImportTimer)scheduledContentRotationImports.get( crImport.getContentRotationImportId() );
								
								// If this import has already been scheduled, but the date/time of the import has changed
								if( t != null && t.getImportDate().getTime() != crImport.getImportDate().getTime() )
								{
									// Cancel the timer so we can re-schedule it with the appropriate time
									logger.info("The content rotation import date has changed for content rotation: "+ cr.getContentRotationId() +". Rescheduling import.");
									t.cancel();
									scheduledContentRotationImports.remove( crImport.getContentRotationImportId() );
									t = null;
								}
								
								// If we have not yet scheduled this import
								if( t == null )
								{
									// Create a timer task for this import
									logger.info("Scheduling import of content rotation: "+ crImport.getContentRotationToImport().getContentRotationName() +" into content rotation: "+ crImport.getContentRotation().getContentRotationName() +" scheduled for "+ crImport.getImportDate());
									t = new ImportTimer();
									t.setImportDate( crImport.getImportDate() );
									t.schedule( new ImportContentRotationRunner( schemaName, crImport.getContentRotationImportId().toString()), crImport.getImportDate() );
									scheduledContentRotationImports.put( crImport.getContentRotationImportId(), t );									
								}
							}	
						}		
					}
					
					HibernateSession.closeSession();
					logger.info("Finished executing ContentRotationImportRunner for "+ schemaName +".");
				}			
			}		
		}
		catch(Exception e)
		{
			logger.error("Unexpected error occurred in ContentRotationImportRunner.", e);
		}		
		finally
		{
			try{
				HibernateSession.closeSession();
			}catch( HibernateException e)
			{ e.printStackTrace(); }
		}			
	}	
	
	public void run()
	{
		try
		{
			// If the schemas have not already been loaded			
			if( SchemaDirectory.schemas == null )
			{
				// Load them
				logger.info("Loading schemas");
				SchemaDirectory.loadSchemas();
			}
			
			SchemaDirectory.setup( this.schema, "ImportContentRotationIntoPlaylistRunner" );
			PlaylistImport playlistImport = PlaylistImport.getPlaylistImport( Long.valueOf( this.importId ) );
			if( playlistImport != null )
			{
				// Import the content rotation into the playlist			
				boolean appendAssetsToPlaylist = true;
				if( playlistImport.getImportType().toString().equalsIgnoreCase( ContentRotationImportType.REPLACE.toString() ) ){
					appendAssetsToPlaylist = false;
				}
				logger.info("Importing content rotation: "+ playlistImport.getContentRotation().getContentRotationName() +" into playlist: "+ playlistImport.getPlaylist().getPlaylistName());
				playlistImport.getPlaylist().importContentRotation( playlistImport.getContentRotation(), playlistImport.getLayout(), playlistImport.getDisplayarea(), appendAssetsToPlaylist, false );
				
				// Remove the playlist import object
				HibernateSession.currentSession().lock(playlistImport, LockMode.NONE);
				playlistImport.delete();						
			}else{
				ContentRotationImport crImport = ContentRotationImport.getContentRotationImport( Long.valueOf( this.importId ) );
				if( crImport != null )
				{
					// Import the content rotation into the playlist			
					boolean appendAssets = true;
					if( crImport.getImportType().toString().equalsIgnoreCase( ContentRotationImportType.REPLACE.toString() ) ){
						appendAssets = false;
					}
					logger.info("Importing content rotation: "+ crImport.getContentRotationToImport().getContentRotationName() +" into content rotation: "+ crImport.getContentRotation().getContentRotationName());
					crImport.getContentRotation().importContentRotation(crImport.getContentRotationToImport(), appendAssets, false);
					
					// Remove the content rotation import object
					HibernateSession.currentSession().lock(crImport, LockMode.NONE);
					crImport.delete();
				}
			}
			HibernateSession.closeSession();					
		}
		catch(Exception e)
		{
			logger.error("Unexpected error occurred in ContentRotationImportRunner.", e);
		}		
		finally
		{
			try{
				HibernateSession.closeSession();
			}catch( HibernateException e)
			{ e.printStackTrace(); }
		}		
	}
}
