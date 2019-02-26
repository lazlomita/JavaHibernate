package com.kuvata.kmf.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import parkmedia.KMFLogger;


/**
 * Converts the ContentRotation data to PlaylistContentRotation data
 * @author jrandesi
 *
 */
public class ConvertToPlaylistContentRotations {

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( ConvertToPlaylistContentRotations.class );
	
	/*
	 * 1. Create playlist_content_rotation table
	 * 2. Execute ConvertToPlaylistContentRotations
	 * 3. Remove playlist_id and displayarea_id from content_rotation table
	 */
	
	public static void doIt(String datasource, String username, String password) throws Exception
	{		
		// Set up connection to the database without using hibernate
		//DriverManager.registerDriver( new oracle.jdbc.OracleDriver() );
		Connection conn = DriverManager.getConnection(datasource, username, password);
		int counter = 0;
		
		// For each record in the content_rotation table		
		String sql = "SELECT content_rotation_id, playlist_id, displayarea_id "		
				+ "FROM content_rotation "
				+ "ORDER BY playlist_id";
		logger.info( sql) ;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Statement stmt2 = conn.createStatement();
		while( rs.next() )
		{
			// Create a playlist_content_rotation record using playlist_id and displayarea_id and content_rotation_id, is_default = 1
			String sql2 = "INSERT INTO playlist_content_rotation "
				+ "VALUES (entity_seq.nextVal, "
				+ "'"+ rs.getString("playlist_id") +"', "
				+ "'"+ rs.getString("content_rotation_id") +"', "
				+ "'"+ rs.getString("displayarea_id") +"', "
				+ "'1')";			
			logger.info( sql2 );
			stmt2.addBatch( sql2 );
			counter++;
			
			// Insert into the db every 100 records
			if( counter == 100 )
			{
				logger.info("Executing batch update");
				stmt2.executeBatch();
				counter = 0;
			}			
		}						
		stmt2.executeBatch();
		rs.close();
		stmt2.close();
		stmt.close();		
		conn.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		if( args.length != 3 ){
			System.out.println("Usage: ConvertToPlaylistContentRotations datasource username password");
		}
		else
		{
			try {
				doIt( args[0], args[1], args[2] );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
