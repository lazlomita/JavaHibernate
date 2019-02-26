/*
 * Created on Nov 17, 2008
 *
 * Kuvata, Inc.
 */
package com.kuvata.kmf.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KuvataConfig;

import com.kuvata.dispatcher.services.PlaybackEventInserter;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class ImportPlaybackEvents {

	public static void main(String[] args) {
		/*
		 * Initialize Hibernate
		 * 
		 * Even though JDBC might be used for importing, Hibernate is necessary
		 * to resolve the device name
		 */
		SchemaDirectory.initialize( "kuvata", "Import Playback Events", null, false, true );
		Session session = HibernateSession.currentSession();

		/*
		 * Use /kuvata/tmp/pe unless an argument has been specified...
		 */
		String importDir = "/kuvata/tmp/pe";
		if( args.length>0 )
		{
			importDir = args[0];
		}
		
		try
		{
			/*
			 * Set up the connection to be used for importing...
			 */
			String connectString = KuvataConfig.getPropertyValue("ImportPlaybackEvents.connectString");
			Connection conn = null;
			if( connectString != null && connectString.length()>0 )
			{
				// if a JDBC connect string has been specified as a property, use that for a connection
				DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
		        conn = DriverManager.getConnection(connectString, "kuvata", "kuvata");
			}
			else
			{
				// no JDBC connect string was found, so use the hibernate connection
				SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
				conn = sessionImplementor.getConnectionProvider().getConnection();
			}
	        conn.setAutoCommit( false );

	        /*
	         * Import away...
	         */
			File dir = new File( importDir ); 		
			String[] children = dir.list(); 
			for (int i=0; i<children.length; i++) 
			{ 
				String filename = children[i];
				File f = new File(dir.getAbsolutePath() + "/" + filename);
				if (f.exists()) {
					System.out.println("Handling " + f.getName());
					try
					{
						PlaybackEventInserter.insertPlaybackEventsFromFile(conn, f);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

}
