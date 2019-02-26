/*
 * Created on Apr 11, 2008
 *
 * Kuvata, Inc.
 */
package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KmfException;

import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class PopulateSegments 
{
	private String schemaName;	
	HashMap<String, String> segmentNameMap = new HashMap<String, String>();	
	HashMap<String, String> deviceNameMap = new HashMap<String, String>();	
	HashMap<String, String> additionalContentMap = new HashMap<String, String>();	
	HashMap<String, String> entityClassMap = new HashMap<String, String>();
	
	private final String appuserId = "670";
	private final String roleId = "58178306";
				
	private static final SimpleDateFormat shortdate = new SimpleDateFormat("MM/dd/yyyy");
	private static final SimpleDateFormat alphadate = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat timeformat = new SimpleDateFormat("h:mm a");
	private static final SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd h:mm a");
	
	public PopulateSegments(String schemaName)	
	{
		try
		{
			this.schemaName = schemaName;
			SchemaDirectory.initialize( schemaName, "Populate Segments", null, false, true );
			
			segmentNameMap.put("TD Bank North Garden", "AMN_TDBankNorthBruins_");
			segmentNameMap.put("TDBankNorth Garden Celtics", "AMN_TDBankNorthCeltics_");
			segmentNameMap.put("Oracle Arena", "AMN_OracleArena_");
			segmentNameMap.put("Verizon Center", "AMN_VerizonCenterCapitals_");
			segmentNameMap.put("Verizon Center Wizards", "AMN_VerizonCenterWizards_");
			segmentNameMap.put("Toyota Center", "AMN_ToyotaCenter_");
			segmentNameMap.put("Target Center", "AMN_TargetCenter_");
			segmentNameMap.put("Conseco Fieldhouse", "AMN_ConsecoFieldhouse_");
			segmentNameMap.put("Time Warner Cable Center (Charlotte Arena)", "AMN_TimeWarnerCenter_");
			segmentNameMap.put("Nassau Coliseum", "AMN_NassauColiseum_");
			
			deviceNameMap.put("TD Bank North Garden", "AM_TDBankNorth_101");
			deviceNameMap.put("TDBankNorth Garden Celtics", "AM_TDBankNorth_101");
			deviceNameMap.put("Oracle Arena", "AM_OracleArena_N-1");
			deviceNameMap.put("Verizon Center", "AM_VerizonCenter_Q-1");
			deviceNameMap.put("Verizon Center Wizards", "AM_VerizonCenter_Q-1");
			deviceNameMap.put("Toyota Center", "AM_ToyotaCenter_101");
			deviceNameMap.put("Target Center", "AM_TargetCenter_201-sp");
			deviceNameMap.put("Conseco Fieldhouse", "AM_ConsecoFieldhouse_I-1");
			deviceNameMap.put("Time Warner Cable Center (Charlotte Arena)", "AM_CharlotteArena_R-1");
			deviceNameMap.put("Nassau Coliseum", "AM_NassauColiseumPlaceHolder");
			
			additionalContentMap.put("Turner Field", "AMN_BravesTeamContent_Playlist");
			additionalContentMap.put("Shea Stadium", "Mets_TeamContent_Playlist");
			additionalContentMap.put("Petco Park", "AMN_Padres_Playerspotlight_Playlist");
			additionalContentMap.put("US Cellular", "AMN_WhiteSox_TeamContent_playlist");
			
			entityClassMap.put("com.kuvata.kmf.Segment", "25");
			entityClassMap.put("com.kuvata.kmf.SegmentPart", "26");
			entityClassMap.put("com.kuvata.kmf.GrpMember", "17");
			entityClassMap.put("com.kuvata.kmf.DeviceSchedule", "13");
			entityClassMap.put("com.kuvata.kmf.PermissionEntry", "56");			
		}
		catch(KmfException e)
		{
			e.printStackTrace();
		}		
	}
	
	private void execute() 
	{
		try
		{
			SchemaDirectory.setup(this.schemaName, this.getClass().getName());
			Session session = HibernateSession.currentSession();
			SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
			Connection conn = sessionImplementor.getConnectionProvider().getConnection();
			
			BufferedWriter out = new BufferedWriter(new FileWriter("E:\\Documents and Settings\\jmattson\\Desktop\\AMN DONE\\out.sql"));
			
			// Read in the file line by line
			
			File directory = new File("E:\\Documents and Settings\\jmattson\\Desktop\\AMN");
			File[] files = directory.listFiles();			
			for (int i=0; i<files.length; i++)
			{			
				File f = files[i];
				String path = f.getAbsolutePath();
				
			    FileInputStream fstream = new FileInputStream(path);
			    DataInputStream in = new DataInputStream(fstream);
			    BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
			    String strLine;
				strLine = br.readLine();
			    while ((strLine = br.readLine()) != null)   {
				  
				  String[] inputArray = strLine.split(",");
				  
				  if (inputArray.length > 0 && inputArray[0].length() != 0) {
				  
					  String startDate = alphadate.format(shortdate.parse(inputArray[1]));
					  
					  if (inputArray[2].contains("(AM)")) {
						  inputArray[2] = inputArray[2].replace("(", "");
						  inputArray[2] = inputArray[2].replace(")", "");
					  }
					  if (!inputArray[2].contains("AM") && !inputArray[2].contains("PM")) {
						  inputArray[2] = inputArray[2] + " PM";
					  }
					  
					  Date startTime = timeformat.parse(inputArray[2].replace("a", " AM").replace("p", " PM"));
					  
					  
					  Calendar startCal = Calendar.getInstance();
					  startCal.setTime(startTime);
					  long startTimeMillis = startCal.getTimeInMillis();
					  
					  String endAfter = "";
					  if (inputArray[3].startsWith("After")) {
						  endAfter = inputArray[3].substring(6);
						  endAfter = endAfter.substring(0,1);
					  }
					  else {
						  Date endTime = timeformat.parse(inputArray[3].replace("a", " AM").replace("p", " PM"));
						  Calendar endCal = Calendar.getInstance();
						  endCal.setTime(endTime);
						  long endTimeMillis = endCal.getTimeInMillis();	
						  if (endTimeMillis < startTimeMillis) endTimeMillis = endTimeMillis + 87600000;
						  endAfter = String.valueOf((endTimeMillis - startTimeMillis) / 3600000);
					  }
					  
					  String segmentId = getEntitySeq(conn);	  
					  if (segmentNameMap.get(inputArray[0]) == null) { throw new Exception ("Could not find Segment Name"); }
					  String segmentName = segmentNameMap.get(inputArray[0]) + startDate;	
					  String startTimeString = startDate + " " + inputArray[2].replace("a", " AM").replace("p", " PM");					  
					  
					  if (additionalContentMap.get(inputArray[0]) == null) {
						  
						  String sql = "insert into segment " +
		  						"(segment_id, segment_name, length, start_datetime, end_type, end_after, end_after_units, " +
		  						"priority, interruption_type, use_server_time, start_before_tolerance, start_before_tolerance_units, " +
		  						"start_after_tolerance, start_after_tolerance_units, end_before_tolerance, end_before_tolerance_units, " +
		  						"end_after_tolerance, end_after_tolerance_units, end_after_num_assets, use_asset_interval_playlist, " +
		  						"asset_interval_frequency, asset_interval_num_assets, asset_interval_playlist_id, asset_interval_units) " +
		  					"values " +
		  						"('" + segmentId + "', '" + segmentName + "', '25200', to_date('" + startTimeString + "', 'YYYY-MM-DD HH:MI AM'), " +
		  						"'End After', '" + endAfter + "', 'hours', 'Normal', 'at point', 'F', '0', 'seconds', '0', 'seconds', '0', 'seconds', '0', 'seconds', " +
		  						"'', '0', '', '', '', '')";
		  
						  System.out.println(sql);
						  out.write(sql + ";\n\n");	
					  }
					  else {
						  
						  String additionalContentPlaylistId = "";
						  String query = "select playlist_id from playlist where playlist_name = '" + additionalContentMap.get(inputArray[0]) + "'";
						  Statement stmt = conn.createStatement();
						  ResultSet rs = stmt.executeQuery(query);
						  if (rs.next()) 
						  {
							  additionalContentPlaylistId = rs.getString(1);
						  }
						  else {
							  throw new Exception("Additional Content Playlist " + additionalContentMap.get(inputArray[0]) + " does not exist");
						  }
						  rs.close();
						  stmt.close();
						  
						  String sql = "insert into segment " +
								"(segment_id, segment_name, length, start_datetime, end_type, end_after, end_after_units, " +
								"priority, interruption_type, use_server_time, start_before_tolerance, start_before_tolerance_units, " +
								"start_after_tolerance, start_after_tolerance_units, end_before_tolerance, end_before_tolerance_units, " +
								"end_after_tolerance, end_after_tolerance_units, end_after_num_assets, use_asset_interval_playlist, " +
								"asset_interval_frequency, asset_interval_num_assets, asset_interval_playlist_id, asset_interval_units) " +
							"values " +
								"('" + segmentId + "', '" + segmentName + "', '25200', to_date('" + startTimeString + "', 'YYYY-MM-DD HH:MI AM'), " +
								"'End After', '" + endAfter + "', 'hours', 'Normal', 'at point', 'F', '0', 'seconds', '0', 'seconds', '0', 'seconds', '0', 'seconds', " +
								"'', '1', '3', '1', '" + additionalContentPlaylistId + "', 'assets')";
	
						  System.out.println(sql);
						  out.write(sql + ";\n\n");						  
					  }
					  
					  // Populate Segment
					  createEntityInstance(segmentId, "com.kuvata.kmf.Segment", out, conn, true);
					  
					  // Populate Segment Part				  
					  String playlistId = "";
					  String query = "select playlist_id from playlist where playlist_name = '" + inputArray[4] + "'";
					  Statement stmt = conn.createStatement();
					  ResultSet rs = stmt.executeQuery(query);
					  if (rs.next()) 
					  {
						  playlistId = rs.getString(1);
					  }
					  else {
						  throw new Exception("Segment Part " + inputArray[4] + " does not exist");
					  }
					  rs.close();
					  stmt.close();
					  
					  String segmentPartId = getEntitySeq(conn);
					  String sql = "insert into segment_part " +
					  			"(segment_part_id, segment_part_type, segment_id, seq_num, playlist_id, asset_id) " +
					  		"values " +
					  			"('" + segmentPartId + "', 'Playlist', '" + segmentId + "', '0', '" + playlistId + "', '')";
					  
					  out.write(sql + ";\n\n");
					  createEntityInstance(segmentPartId, "com.kuvata.kmf.SegmentPart", out, conn, false);
					  
					  // Populate Device Schedule			
					  String deviceName = deviceNameMap.get(inputArray[0]);
					  if (deviceName == null) { throw new Exception("Device Name Not Found"); }
					  String deviceScheduleId = getEntitySeq(conn);
					  sql = "insert into device_schedule " +
					  			"(device_schedule_id, device_id, segment_id) " +
					  		"values " +
					  			"('" + deviceScheduleId + "', (select device_id from device where device_name = '" + deviceName + "'), '" + segmentId + "')";
					  
					  out.write(sql + ";\n\n");
					  createEntityInstance(deviceScheduleId, "com.kuvata.kmf.DeviceSchedule", out, conn, false);		  
				  	}
				}				  
			}
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}				
	}
	
	private String getEntitySeq(Connection conn) throws Exception {
		
		String result = "";
		
		String sql = "select entity_seq.NextVal from dual";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next()) 
		{
			result = rs.getString(1);
		}		
		rs.close();
		stmt.close();
		return result;
	}
	
	private void createEntityInstance(String entityId, String entityClass, BufferedWriter out, Connection conn, boolean createPermissionEntry) throws Exception
	{
		String sql = "insert into entity_instance " +
						"(entity_id, entity_class_id, appuser_id, last_modified) " +
					 "values " +
					 	"('" + entityId + "', '" + entityClassMap.get(entityClass) + "', '" + appuserId + "', sysdate)";
		
		out.write(sql + ";\n\n");
		
		if (createPermissionEntry == true) {
		
			String permissionEntryId = getEntitySeq(conn);
			
			sql = "insert into permission_entry " +
						"(permission_entry_id, permission_entity_id, role_id) " +
				  "values " +
				  		"('" + permissionEntryId + "', '" + entityId + "', '" + roleId + "')";
			
			out.write(sql + ";\n\n");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PopulateSegments ps = new PopulateSegments("kuvata");
		ps.execute();
	}

}
