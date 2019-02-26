/*
 * Created on Oct 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KmfException;
import parkmedia.usertype.AspectRatioType;
import parkmedia.usertype.AudioChannelType;
import parkmedia.usertype.ContentUpdateType;

import com.kuvata.kmf.Device;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.EntityClass;
import com.kuvata.kmf.Grp;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.LayoutDisplayarea;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.SchemaDirectory;

public class DataSeeder {

	private static String FULL_SCREEN = "Full Screen";
	private static String schemaName = "";
	
	public DataSeeder(){
		try{
			SchemaDirectory.initialize( DataSeeder.schemaName, "Data Seeder", "1", false, true );
			System.out.println("SCHEMA: " + DataSeeder.schemaName);
		}catch(KmfException e){
			e.printStackTrace();
		}		
	}
	
	protected void seedData()
	{
		try
		{
			HibernateSession.startBulkmode();			
			createEntityClasses();
			HibernateSession.stopBulkmode();
			
			HibernateSession.clearCache();
			EntityClass.reloadEntityClasses();
			
			HibernateSession.startBulkmode();
			createLayouts();		
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createDeviceGroups();
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createDevices();
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createSegmentGroups();
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createContentRotationGroups();
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createPlaylistGroups();
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createPlaylists();
			HibernateSession.stopBulkmode();
			
			HibernateSession.startBulkmode();
			createDummySelectedEntities();
			HibernateSession.stopBulkmode();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
		finally
		{
			try
			{
				HibernateSession.closeSession();
			}
			catch(HibernateException he)
			{
				he.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void createDeviceGroups() throws HibernateException
	{
		HibernateSession.beginTransaction();
					
		Grp g = new Grp();
		g.setGrpName("Device Groups");		
		g.save();
		
		HibernateSession.commitTransaction();
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	private static void createDevices() throws Exception 
	{
		Device.create("Device One", "Device One", Integer.valueOf(86400), Integer.valueOf(3600), ContentUpdateType.NO_UPDATES.getPersistentValue(), Boolean.TRUE, Boolean.FALSE);	
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void createSegmentGroups() throws HibernateException
	{
		Grp g = new Grp();
		g.setGrpName("Schedule Groups");		
		g.save();		
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void createContentRotationGroups() throws HibernateException
	{
		HibernateSession.beginTransaction();
					
		Grp g = new Grp();
		g.setGrpName("Content Rotation Groups");		
		g.save();
		
		HibernateSession.commitTransaction();
	}	
	/**
	 * 
	 * @throws HibernateException
	 * @throws Exception
	 */
	private static void createLayouts() throws HibernateException, Exception
	{
		HibernateSession.beginTransaction();	
				
		Layout l = new Layout();
		l.setLayoutName( FULL_SCREEN );
		l.setWidth( new Integer(1024) );
		l.setHeight( new Integer(768) );
		l.save();
		
		Displayarea daMain = Displayarea.createOrUpdate(l, null, FULL_SCREEN, new Integer(1024), new Integer(768), AspectRatioType.ASPECT_RATIO_4x3, AudioChannelType.ALL, true, false);

		LayoutDisplayarea lda = new LayoutDisplayarea();
		lda.setLayout( l );
		lda.setDisplayarea( daMain );
		lda.setXpos( new Integer(0) );
		lda.setYpos( new Integer(0) );
		lda.setSeqNum( new Integer(0) );
		lda.save();	
		
		HibernateSession.commitTransaction();
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void createPlaylistGroups() throws HibernateException
	{	
		Grp g = new Grp();
		g.setGrpName("Playlist Groups");
		g.save();
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	private static void createPlaylists() throws HibernateException
	{
		Playlist p = new Playlist();
		p.setPlaylistName("Playlist One");
		p.setLength( new Float(0) );
		p.save();		
	}	
	
	/**
	 * 
	 * @param args
	 */	
	public static void main(String[] args) {
		if(args.length < 1){
			System.out.println("Invalid arguments passed to DataSeeder");
			System.out.println("ex: java DataSeeder schema");
		}
		else{			
			DataSeeder.schemaName = args[0];
			DataSeeder ds = new DataSeeder();
			ds.seedData();		
			System.out.println("DONE");
		}			
	}
	
	/*
	 * We will create 1000 dummy rows in the selected_entities table.
	 * This allows oracle to analyze the date with some data so that when joins happen,
	 * the correct algorithms are used.
	 */
	public static void createDummySelectedEntities() throws Exception{
		HibernateSession.beginTransaction();
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();		
		Statement stmt = conn.createStatement();
		for(int i=0; i<1000; i++){
			String sql = "insert into selected_entities values (0, null, 0)";
			stmt.addBatch( sql );
		}
		stmt.executeBatch();
		stmt.close();			
		HibernateSession.commitTransaction();
		conn.close();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 * @throws SQLException
	 */
	public static void createEntityClasses() throws HibernateException, SQLException
	{
		Vector v = new Vector();
		v.add("com.kuvata.kmf.Asset");
		v.add("com.kuvata.kmf.AssetSegmentPart");
		v.add("com.kuvata.kmf.attr.AttrDefinition");		
		v.add("com.kuvata.kmf.ContentRotation");
		v.add("com.kuvata.kmf.Device");
		v.add("com.kuvata.kmf.HeartbeatEvent");
		v.add("com.kuvata.kmf.DeviceSchedule");
		v.add("com.kuvata.kmf.Displayarea");
		v.add("com.kuvata.kmf.EntityClass");
		v.add("com.kuvata.kmf.Grp");
		v.add("com.kuvata.kmf.GrpMember");
		v.add("com.kuvata.kmf.Layout");
		v.add("com.kuvata.kmf.LayoutDisplayarea");
		v.add("com.kuvata.kmf.PairedAsset");
		v.add("com.kuvata.kmf.Playlist");
		v.add("com.kuvata.kmf.PlaylistSegmentPart");
		v.add("com.kuvata.kmf.Relationship");
		v.add("com.kuvata.kmf.Segment");
		v.add("com.kuvata.kmf.SegmentPart");
		v.add("com.kuvata.kmf.Recurrence");
		v.add("com.kuvata.kmf.WeeklyRecurrence");
		v.add("com.kuvata.kmf.Entity");
		v.add("com.kuvata.kmf.PairedDisplayarea");	
		v.add("com.kuvata.kmf.Contact");
		v.add("com.kuvata.kmf.asset.Image");
		v.add("com.kuvata.kmf.asset.Audio");
		v.add("com.kuvata.kmf.asset.Flash");
		v.add("com.kuvata.kmf.asset.Html");
		v.add("com.kuvata.kmf.asset.Video");
		v.add("com.kuvata.kmf.asset.Webapp");
		v.add("com.kuvata.kmf.Dirty");
		v.add("com.kuvata.kmf.ExcludedPlaylist");
		v.add("com.kuvata.kmf.ContentSchedulerStatus");
		v.add("com.kuvata.kmf.Mcm");
		v.add("com.kuvata.kmf.McmHistoryEntry");
		v.add("com.kuvata.kmf.Alert");
		v.add("com.kuvata.kmf.AlertUser");
		v.add("com.kuvata.kmf.AssetIngesterStatus");
		v.add("com.kuvata.kmf.StringAttr");
		v.add("com.kuvata.kmf.DateAttr");
		v.add("com.kuvata.kmf.PlaybackEvent");
		v.add("com.kuvata.kmf.DisplayareaAssetType");
		v.add("com.kuvata.kmf.AssetExclusion");
		v.add("com.kuvata.kmf.PlaylistContentRotation");
		v.add("com.kuvata.kmf.ContentRotationTarget");
		v.add("com.kuvata.kmf.PermissionEntry");
		v.add("com.kuvata.kmf.Role");
		v.add("com.kuvata.kmf.AppUserRole");
		v.add("com.kuvata.kmf.PlaylistDisplayarea");
		v.add("com.kuvata.kmf.FileTransmission");
		v.add("com.kuvata.kmf.SelfTestHistory");
		v.add("com.kuvata.kmf.PlaylistImport");
		v.add("com.kuvata.kmf.ServerCommand");		
		v.add("com.kuvata.kmf.asset.AdServer");
		v.add("com.kuvata.kmf.DeviceIngesterStatus");
		v.add("com.kuvata.kmf.EventHistory");
		v.add("com.kuvata.kmf.MirroredDevice");
		v.add("com.kuvata.kmf.DeviceScript");
		v.add("com.kuvata.kmf.billing.Advertiser");
		v.add("com.kuvata.kmf.billing.Ad");
		v.add("com.kuvata.kmf.billing.VenuePartner");
		v.add("com.kuvata.kmf.billing.Campaign");
		v.add("com.kuvata.kmf.billing.CampaignAsset");
		v.add("com.kuvata.kmf.ServerEventLog");
		v.add("com.kuvata.kmf.MulticastNetwork");
		v.add("com.kuvata.kmf.reports.SavedReport");
		v.add("com.kuvata.kmf.reports.SavedReportFiles");
		v.add("com.kuvata.kmf.MonthlyRecurrence");
		v.add("com.kuvata.kmf.DailyRecurrence");
		v.add("com.kuvata.kmf.AlertCampaign");
		v.add("com.kuvata.kmf.Job");
		v.add("com.kuvata.kmf.DynamicQueryPart");
		v.add("com.kuvata.kmf.ContentUpdate");
		
		HibernateSession.beginTransaction();
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();		
		Statement stmt = conn.createStatement();
		for(int i=0; i<v.size(); i++)
		{
			String sql = "INSERT INTO entity_class "
						+ "VALUES (entity_seq.nextval, '"+ v.get(i).toString() + "')";
			System.out.println(sql);
			stmt.addBatch( sql );
		}
		stmt.executeBatch();
		stmt.close();			
		HibernateSession.commitTransaction();
		conn.close();
	}
}
