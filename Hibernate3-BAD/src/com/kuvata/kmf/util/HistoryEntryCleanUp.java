package com.kuvata.kmf.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KMFLogger;

import com.kuvata.kmf.EntityInstance;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class HistoryEntryCleanUp {
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( HistoryEntryCleanUp.class );
	private static final int MAX_DB_RESULTS = 100;
	
	private static void doCleanUp(String schemaName) throws Exception
	{
		SchemaDirectory.initialize( schemaName, "HistoryEntryCleanUp", "Server Cleanup", false, true );
		
		// Delete all rows except for the most recent row for each entity		
		String selectSql = "SELECT history_id FROM  history_entry WHERE history_id NOT IN ("
			+ "  SELECT maxhistory FROM ("
			+ "    SELECT max(history_id) as maxhistory, entity_id FROM history_entry GROUP BY entity_id ) )"
			+ " AND rownum < "+ MAX_DB_RESULTS;
		String deleteSql = "DELETE FROM history_entry WHERE history_id IN";		
		
		// Keep going until our selectHql does not return any records
		Session session = HibernateSession.currentSession();
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();
		
		while( true )
		{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery( selectSql );			
			boolean foundRecord = false;
			ArrayList ids = new ArrayList();
			
			/*
			 * Build the delete sql to be used in the prepared statement
			 */
			String sql = deleteSql +" (";			
			while( rs.next() )
			{				
				// Delete rows using bound parameters
				sql += "?,";	
				ids.add( rs.getLong("history_id") );
				foundRecord = true;
			}
			rs.close();
			stmt.close();
			
			// If our query returned one or more records
			if( foundRecord )
			{
				// Remove the last comma
				if( sql.endsWith(",") ){
					sql = sql.substring( 0, sql.length() - 1 );
				}
				sql += ")";				

				// Build the prepared statement and delete
				logger.info("Cleaning up db: "+ sql );				
				PreparedStatement stmt2 = conn.prepareStatement( sql );
				Object[] deleteIds  = ids.toArray();		
				for( int j=0; j<deleteIds.length; j++ ) {							
					stmt2.setLong( j + 1, ((Long)( deleteIds[ j ] )).longValue());				
				}
				ids = null;
				deleteIds = null;
				stmt2.executeUpdate();
				stmt2.close();
				
				// It is necessary to set this flag in order to avoid the "You cannot commit with autocommit set!" error
				conn.setAutoCommit( false );			
				conn.commit();	
				
			}
			// If our query did not return any results -- break out of the continuous loop
			else {
				break;
			}
			session.flush();
			session.clear();			
		}				
		conn.close();		
	}
	
	/**
	 * Copy the timestamp value in the history_entry table for each entity 
	 * to the last_modified column in the entity_instance table
	 * @param schemaName
	 * @throws Exception
	 */
	private static void updateEntityInstances(String schemaName) throws Exception
	{
		// Get the most recent timestamp for each entity in the history_entry table 
		String sql = "SELECT he1.entity_id, he1.timestamp FROM history_entry he1 "
			+ "GROUP BY he1.entity_id, he1.timestamp "
			+ "HAVING he1.timestamp = ("
			+ "SELECT max(he2.timestamp) FROM history_entry he2 GROUP BY he2.entity_id HAVING he1.entity_id = he2.entity_id)";
		Session session = HibernateSession.currentSession();
		HibernateSession.beginTransaction();
		Connection conn = session.connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery( sql );			
		while( rs.next() )
		{				
			// Retrieve the entity instance object for this entity and update the lastModified column
			EntityInstance ei = EntityInstance.getEntityInstance( rs.getLong("entity_id") );
			if( ei != null ){
				logger.info("setting timestamp for entity: "+ ei.getEntityId() +" - "+ rs.getTimestamp("timestamp"));
				ei.setLastModified( (Date)rs.getTimestamp("timestamp") );
				session.update( ei );
			}
		}
		HibernateSession.commitTransaction();
		rs.close();
		stmt.close();
		conn.close();
		HibernateSession.closeSession();
		conn.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if( args.length != 1 ){
				System.out.println("Usage: HistoryEntryCleanUp schemaName");
			}else{
				doCleanUp( args[0] );	
				updateEntityInstances( args[0] );
				System.out.println("DONE");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HibernateSession.closeSession();
		}
	}

}
