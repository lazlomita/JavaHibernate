package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;

import com.kuvata.StreamGobbler;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.PlaybackEventSummary;
import com.kuvata.kmf.Schema;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.reports.ReportManager;

public class AggregatePlaybackEvents extends TimerTask{

	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( AggregatePlaybackEvents.class );
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	private static final int AGGREGATION_BEGIN = 0;
	private static final int INSERT_COMPLETE = 1;
	private static final int AGGREGATION_POPULATED = 2;
	
	public static void main(String[] args) throws SQLException{
		
		boolean debug = args.length == 3 && args[2].equals("debug");
		BufferedReader br = null;
		
		if(args.length == 2 || debug){
			
			Connection conn = null;
			Session session = null;
			
			try {
				
				logger.info("Starting Playback Event Aggregation");
				
				SchemaDirectory.initialize(args[0], AggregatePlaybackEvents.class.getName(), null, false, true);
				
				// Generate the start and end dates from the argument
				Date start = DATE_FORMAT.parse(args[1]);
				Calendar c = Calendar.getInstance();
				c.setTime(start);
				c.add(Calendar.DATE, 1);
				Date end = c.getTime();
				
				// Get a Hibernate connection/session
				session = HibernateSession.currentSession();
				
				// Get a JDBC connection
				SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
				conn = sessionImplementor.getConnectionProvider().getConnection();
				
				// Get the schema
				Schema schema = Schema.getSchema(SchemaDirectory.getSchemaName());
				
				// Get the last aggregated date
				Date lastAggregatedDate = schema.getLastAggregatedDate();
				
				if(lastAggregatedDate == null){
					c.setTime(start);
					c.add(Calendar.DATE, -1);
					lastAggregatedDate = c.getTime();
				}

				// Make sure we are running the aggregation for a date after the lastAggregatedDate
				if(start.after(lastAggregatedDate)){
					
					// Failsafe cases
					if(schema.getAggregationProgress() != null){
						
						logger.info("Aggregation progress denotes a failure. Aggregation Progress = " + schema.getAggregationProgress());
						logger.info("Attempting to recover.");
						
						if(schema.getAggregationProgress().equals(AGGREGATION_BEGIN) || schema.getAggregationProgress().equals(INSERT_COMPLETE)){
							// Delete all partially aggregated rows
							session.createQuery("DELETE FROM PlaybackEventSummary WHERE startDatetime > ?").setParameter(0, lastAggregatedDate).executeUpdate();
							session.flush();
							
							if(schema.getAggregationProgress().equals(INSERT_COMPLETE)){
								
								// Clear the aggregation table
								session.createQuery("DELETE FROM PlaybackEventAggregation").executeUpdate();
								session.flush();
							}
							
						}else if(schema.getAggregationProgress().equals(AGGREGATION_POPULATED)){
							
							// Transfer data from the aggregation table
							populateSummaryFromAggregationTable(session, conn);
							
						}
						
						// Update progress status
						HibernateSession.beginTransaction();
						schema.setAggregationProgress(null);
						schema.update();
						HibernateSession.commitTransaction();
						
						logger.info("Recovery complete.");
					}
	
					// Get the maxId from the playbackEvent table
					List l = session.createQuery("SELECT MAX(pe.playbackEventId) FROM PlaybackEvent as pe").list();
					long maxId = l.size() > 0 ? (Long)l.get(0) : 0;
					
					// Update progress status
					HibernateSession.beginTransaction();
					schema.setAggregationProgress(AGGREGATION_BEGIN);
					schema.update();
					HibernateSession.commitTransaction();
					
					logger.info("Step 1: Beginning aggregation - Insert into Summary table.");
					
					if(debug){
						br = new BufferedReader(new InputStreamReader(System.in));
						System.out.println("Step 1: Begin Aggregation. Press Enter to continue.");
						br.readLine();
					}
					
					// Aggregate all rows for all dates that have not yet been aggregated
					c.setTime(lastAggregatedDate);
					c.add(Calendar.DATE, 1);
					Date aggregationBegin = c.getTime();
					
					aggregateRows(aggregationBegin, end, maxId, session, conn);
					
					// Update progress status
					HibernateSession.beginTransaction();
					schema.setAggregationProgress(INSERT_COMPLETE);
					schema.update();
					HibernateSession.commitTransaction();
					
					logger.info("Step 2: Insert complete. Beginning aggregation - Insert into aggregation table");
					
					if(debug){
						System.out.println("Step 2: Insert Complete. Press Enter to continue.");
						br.readLine();
					}
					
					// Aggregate all rows for dates prior to dates we aggregated in the above step
					populateAggregationTable(aggregationBegin, maxId, session, conn);
					
					// Update progress status
					HibernateSession.beginTransaction();
					schema.setLastAggregatedId(maxId);
					schema.setLastAggregatedDate(start);
					schema.setAggregationProgress(AGGREGATION_POPULATED);
					schema.update();
					HibernateSession.commitTransaction();
					
					logger.info("Step 3: Aggregation table populated. Beginning to move data - Insert/Update into Summary table.");
					
					if(debug){
						System.out.println("Step 3: Aggregation Table Populated. Press Enter to continue.");
						br.readLine();
					}
					
					// Populate the summary table from the aggregation table
					populateSummaryFromAggregationTable(session, conn);
					
					// Update progress status
					schema.setAggregationProgress(null);
					schema.update();
					
					logger.info("Aggregation complete.");
				}else{
					logger.error("You are trying to aggregate for a date that has already been aggregated. Process terminated.");
				}
				
			} catch (Exception e) {
				logger.error(e);
			} finally {
				if(conn != null){
					conn.close();
				}
				if(session != null){
					session.flush();
					session.close();
				}
			}
		}else{
			logger.error("Usage: java AggregatePlaybackEvents schemaName date(YYYY/MM/DD)");
			logger.error("Args passed in: "+ args.length);
			for(int i=0; i<args.length; i++) {
				logger.error("arg"+ i +"="+ args[i]);
			}
		}
	}
	
	// Aggregate rows for recent day(s) - generally yesterday, Insert only
	private static void aggregateRows(Date start, Date end, long maxId, Session session, Connection conn ) throws Exception{
		ScrollableResults results = null;
		
		String hql = "SELECT pe.deviceId, pe.deviceName, pe.assetId, pe.assetName, SUM(pe.assetLength), SUM(pe.assetLength * (pe.displaysCount - pe.displayExceptionsCount)), " +
		"SUM(pe.displaysCount), SUM(pe.displayExceptionsCount), SUM(pe.clickCount), COUNT(*), to_char(pe.startDatetime,'YYYY/MM/DD') FROM PlaybackEvent as pe " +
		"WHERE pe.startDatetime >= :todayStart AND pe.startDatetime < :todayEnd AND pe.playbackEventId <= :maxId " +
		"GROUP BY pe.deviceId, pe.deviceName, pe.assetId, pe.assetName, to_char(pe.startDatetime,'YYYY/MM/DD')";
		results = session.createQuery(hql).setParameter("todayStart", start).setParameter("todayEnd", end).setParameter("maxId", maxId).scroll();
		
		// Prepare the insert statement
		StringBuffer result = new StringBuffer();
		result.append("INSERT INTO playback_event_summary ");
		result.append("(playback_event_summary_id, device_id, device_name, asset_id, asset_name, airing_length, display_airing_length, start_datetime, ");
		result.append("displays_count, display_exceptions_count, click_count, num_airings) ");				
		result.append("VALUES (playback_event_seq.nextVal, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		PreparedStatement pstmt = conn.prepareStatement( result.toString() );
		
		int batchCount = 0;
        int batchSize = 100;
        
        // Add to the playbackEventSummary table in batches of 100
		while(results.next()){
			Object[] o = results.get();
			Long deviceId = (Long)o[0];
			String deviceName = (String)o[1];
			Long assetId = (Long)o[2];
			String assetName = (String)o[3];
			Double airingLength = (Double)o[4];
			Double displayAiringLength = (Double)o[5];
			Long displaysCount = (Long)o[6];
			Long displayExceptionsCount = (Long)o[7];
			Long clickCount = (Long)o[8];
			Long numAirings = (Long)o[9];
			String startDate = (String)o[10];
			Date playbackDate = DATE_FORMAT.parse(startDate);
			
			pstmt.setLong(1, deviceId);
			pstmt.setString(2, deviceName);
			pstmt.setLong(3, assetId);
			pstmt.setString(4, assetName);
			pstmt.setDouble(5, airingLength);
			pstmt.setDouble(6, displayAiringLength);
			pstmt.setDate(7, new java.sql.Date(playbackDate.getTime()));
			pstmt.setLong(8, displaysCount);
			pstmt.setLong(9, displayExceptionsCount);
			pstmt.setLong(10, clickCount);
			pstmt.setLong(11, numAirings);
			
			pstmt.addBatch();
			batchCount++;
			if( batchCount >= batchSize ) {
	        	pstmt.executeBatch();
				batchCount = 0;
	        }
		}
		if( batchCount > 0 ) {
	    	pstmt.executeBatch();
	    }
		pstmt.close();
		results.close();
		conn.commit();
	}
	
	private static void populateAggregationTable(Date end, long maxId, Session session, Connection conn) throws Exception{
		Long lastAggregatedId = Schema.getSchema(SchemaDirectory.getSchemaName()).getLastAggregatedId();
		lastAggregatedId = lastAggregatedId != null ? lastAggregatedId : 0;
		
		String hql = "SELECT pe.deviceId, pe.deviceName, pe.assetId, pe.assetName, SUM(pe.assetLength), SUM(pe.assetLength * (pe.displaysCount - pe.displayExceptionsCount)), " +
		"SUM(pe.displaysCount), SUM(pe.displayExceptionsCount), SUM(pe.clickCount), COUNT(*), to_char(pe.startDatetime,'YYYY/MM/DD') " +
		"FROM PlaybackEvent as pe WHERE pe.startDatetime < :end AND pe.playbackEventId > :lastAggregatedId AND pe.playbackEventId <= :maxId " +
		"GROUP BY pe.deviceId, pe.deviceName, pe.assetId, pe.assetName, to_char(pe.startDatetime,'YYYY/MM/DD')";
		ScrollableResults results = session.createQuery(hql).setParameter("end", end).setParameter("lastAggregatedId", lastAggregatedId).setParameter("maxId", maxId).scroll();
		
		// Prepare the insert statement
		StringBuffer result = new StringBuffer();
		result.append("INSERT INTO playback_event_aggregation ");
		result.append("(playback_event_aggregation_id, device_id, device_name, asset_id, asset_name, airing_length, display_airing_length, start_datetime, ");
		result.append("displays_count, display_exceptions_count, click_count, num_airings) ");				
		result.append("VALUES (playback_event_seq.nextVal, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		PreparedStatement pstmt = conn.prepareStatement( result.toString() );
		
		int batchCount = 0;
        int batchSize = 100;
		
		// For all returned rows
		while(results.next()){
			Object[] o = results.get();
			Long deviceId = (Long)o[0];
			String deviceName = (String)o[1];
			Long assetId = (Long)o[2];
			String assetName = (String)o[3];
			Double airingLength = (Double)o[4];
			Double displayAiringLength = (Double)o[5];
			Long displaysCount = (Long)o[6];
			Long displayExceptionsCount = (Long)o[7];
			Long clickCount = (Long)o[8];
			Long numAirings = (Long)o[9];
			String startDate = (String)o[10];
			Date playbackDate = DATE_FORMAT.parse(startDate);
			
			pstmt.setLong(1, deviceId);
			pstmt.setString(2, deviceName);
			pstmt.setLong(3, assetId);
			pstmt.setString(4, assetName);
			pstmt.setDouble(5, airingLength);
			pstmt.setDouble(6, displayAiringLength);
			pstmt.setDate(7, new java.sql.Date(playbackDate.getTime()));
			pstmt.setLong(8, displaysCount);
			pstmt.setLong(9, displayExceptionsCount);
			pstmt.setLong(10, clickCount);
			pstmt.setLong(11, numAirings);
			
			pstmt.addBatch();
			batchCount++;
			if( batchCount >= batchSize ) {
	        	pstmt.executeBatch();
				batchCount = 0;
	        }
		}
		
		if( batchCount > 0 ) {
	    	pstmt.executeBatch();
	    }
		pstmt.close();
		results.close();
		conn.commit();
	}
	
	private static void populateSummaryFromAggregationTable(Session session, Connection conn) throws Exception{
		
		String hql = "SELECT p.playbackEventAggregationId, p.deviceId, p.deviceName, p.assetId, p.assetName, p.airingLength, p.displayAiringLength, " +
					"p.startDatetime, p.displaysCount, p.displayExceptionsCount, p.clickCount, p.numAirings FROM PlaybackEventAggregation as p";
		
		// Clear the session and get the aggregated rows
		ScrollableResults results = session.createQuery(hql).scroll();
		
		// Prepare the insert statement
		StringBuffer insert = new StringBuffer();
		insert.append("INSERT INTO playback_event_summary ");
		insert.append("(playback_event_summary_id, device_id, device_name, asset_id, asset_name, airing_length, display_airing_length, start_datetime, ");
		insert.append("displays_count, display_exceptions_count, click_count, num_airings) ");				
		insert.append("VALUES (playback_event_seq.nextVal, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		// Prepare the update statement
		StringBuffer update = new StringBuffer();
		update.append("UPDATE playback_event_summary SET airing_length = ?, display_airing_length = ?, displays_count = ?, display_exceptions_count = ?, click_count = ?, num_airings = ? ");
		update.append("WHERE playback_event_summary_id = ?");
		
		// Prepare the delete statement
		String delete = "DELETE FROM playback_event_aggregation WHERE playback_event_aggregation_id IN (?)";
		
		PreparedStatement pstmtInsert = conn.prepareStatement( insert.toString() );
		PreparedStatement pstmtUpdate = conn.prepareStatement( update.toString() );
		
		int insertBatchCount = 0;
		int updateBatchCount = 0;
        int batchSize = 100;
		ArrayList idsProcessed = new ArrayList();
		
		while(results.next()){
			Object[] o = results.get();
			Long id = (Long)o[0];
			Long deviceId = (Long)o[1];
			String deviceName = (String)o[2];
			Long assetId = (Long)o[3];
			String assetName = (String)o[4];
			Double airingLength = (Double)o[5];
			Double displayAiringLength = (Double)o[6];
			Date startDate = (Date)o[7];
			Integer displaysCount = (Integer)o[8];
			Integer displayExceptionsCount = (Integer)o[9];
			Integer clickCount = (Integer)o[10];
			Integer numAirings = (Integer)o[11];
			idsProcessed.add(id);
			
			// Update the row if it already exists
			PlaybackEventSummary pes = PlaybackEventSummary.getPlaybackEventSummary(deviceId, deviceName, assetId, assetName, startDate);
			
			// Update
			if(pes != null){
				airingLength += pes.getAiringLength();
				displayAiringLength += pes.getDisplayAiringLength();
				displaysCount += pes.getDisplaysCount();
				displayExceptionsCount += pes.getDisplayExceptionsCount();
				clickCount += pes.getClickCount();
				numAirings += pes.getNumAirings();
				
				pstmtUpdate.setDouble(1, airingLength);
				pstmtUpdate.setDouble(2, displayAiringLength);
				pstmtUpdate.setLong(3, displaysCount);
				pstmtUpdate.setLong(4, displayExceptionsCount);
				pstmtUpdate.setLong(5, clickCount);
				pstmtUpdate.setLong(6, numAirings);
				pstmtUpdate.setLong(7, pes.getPlaybackEventSummaryId());
				
				pstmtUpdate.addBatch();
				updateBatchCount++;
			}
			// Insert
			else{
				pstmtInsert.setLong(1, deviceId);
				pstmtInsert.setString(2, deviceName);
				pstmtInsert.setLong(3, assetId);
				pstmtInsert.setString(4, assetName);
				pstmtInsert.setDouble(5, airingLength);
				pstmtInsert.setDouble(6, displayAiringLength);
				pstmtInsert.setDate(7, new java.sql.Date(startDate.getTime()));
				pstmtInsert.setLong(8, displaysCount);
				pstmtInsert.setLong(9, displayExceptionsCount);
				pstmtInsert.setLong(10, clickCount);
				pstmtInsert.setLong(11, numAirings);
				
				pstmtInsert.addBatch();
				insertBatchCount++;
			}
			
			if(updateBatchCount + insertBatchCount >= batchSize){
				if( updateBatchCount > 0 ) pstmtUpdate.executeBatch();
				if( insertBatchCount > 0 ) pstmtInsert.executeBatch();
				
				updateBatchCount = insertBatchCount = 0;
				
				// Convert the list to a comma-delimited string
				String idsToDelete = idsProcessed.toString();
				idsToDelete = idsToDelete.substring(1, idsToDelete.length() - 1);
				
				// Delete from the aggregation table
				// We need to create a new statement each time since we can't bind a list in sql
				String deleteSql = delete.replace("?", idsToDelete);
				PreparedStatement pstmtDelete = conn.prepareStatement( deleteSql );
				pstmtDelete.execute();
				conn.commit();
				pstmtDelete.close();
				idsProcessed.clear();
			}
		}

		if(updateBatchCount + insertBatchCount > 0){
			if( updateBatchCount > 0 ) pstmtUpdate.executeBatch();
			if( insertBatchCount > 0 ) pstmtInsert.executeBatch();
			
			// Convert the list to a comma-delimited string
			String idsToDelete = idsProcessed.toString();
			idsToDelete = idsToDelete.substring(1, idsToDelete.length() - 1);
			
			// Delete from the aggregation table
			// We need to create a new statement each time since we can't bind a list in sql
			String deleteSql = delete.replace("?", idsToDelete);
			PreparedStatement pstmtDelete = conn.prepareStatement( deleteSql );
			pstmtDelete.execute();
			conn.commit();
			pstmtDelete.close();
		}

		pstmtUpdate.close();
		pstmtInsert.close();
		results.close();
		conn.commit();
	}
	
	private static String memoryAllocationMin = "64m";	// Default initial memory allocation 
	private static String memoryAllocationMax = "256m";	// Default maximum memory allocation
	
	public void run()
	{
		try
		{
			// Define the memory allocation range for the java process
			try {
				memoryAllocationMin = KuvataConfig.getPropertyValue("ContentScheduler.memoryMin");
			} catch(KmfException e) {
				logger.info("Could not location property ContentScheduler.memoryMin. Using default: "+ memoryAllocationMin);
			}
			try {
				memoryAllocationMax = KuvataConfig.getPropertyValue("ContentScheduler.memoryMax");
			} catch(KmfException e) {
				logger.info("Could not location property ContentScheduler.memoryMax. Using default: "+ memoryAllocationMax);
			}				
			
			/*
			 * Build and execute the java command to execute the playback_event aggregation
			 */
			Date yesterday = new Date(System.currentTimeMillis() - Constants.MILLISECONDS_IN_A_DAY);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			String cmd = "java -cp "+ KuvataConfig.getPropertyValue("classpath") +" -Xms"+ memoryAllocationMin +" -Xmx"+ memoryAllocationMax +" "+ AggregatePlaybackEvents.class.getName() + " " + SchemaDirectory.KUVATA_SCHEMA + " " + sdf.format(yesterday);
			logger.info("Running playback_event aggregation: "+ cmd);
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec( cmd );
			
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERR");            			
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUT");		
			errorGobbler.start();
			outputGobbler.start();
			
			// Wait for aggregation to finish
			p.waitFor();
			p = null;
			rt = null;
			
			// Start the report manager since we have updated reporting data
			logger.info("Starting Report Manager");
			new ReportManager().start();
			
		}catch(Exception e){
			logger.error("Unexpected error occurred in AggregatePlaybackEvents.", e);
		}
	}
}
