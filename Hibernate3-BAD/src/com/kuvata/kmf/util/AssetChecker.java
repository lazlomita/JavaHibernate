/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import parkmedia.KmfException;
import parkmedia.usertype.AssetType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.asset.Audio;
import com.kuvata.kmf.asset.Flash;
import com.kuvata.kmf.asset.Html;
import com.kuvata.kmf.asset.Image;
import com.kuvata.kmf.asset.Video;
import com.kuvata.kmf.asset.Webapp;
import com.kuvata.kmf.asset.Xml;

/**
 * @author anaber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AssetChecker {

	private String schemaName;
	private boolean updateAdler32Values;
	private boolean allAssets;
	private boolean createAssetFiles;
	List updateStatements;
	
	public AssetChecker(String schemaName, boolean updateAdler32Values, boolean allAssets, boolean createAssetFiles)	
	{
		try
		{
			this.schemaName = schemaName;
			this.updateAdler32Values = updateAdler32Values;
			this.allAssets = allAssets;
			this.createAssetFiles = createAssetFiles;
			updateStatements = new LinkedList();			
			SchemaDirectory.initialize( schemaName, "Asset Checker", null, false, true );
		}
		catch(KmfException e)
		{
			e.printStackTrace();
		}		
	}
	
	private void checkAssets()
	{
		try
		{
			System.out.println("SCHEMA: " +schemaName);
			SchemaDirectory.setup(this.schemaName, this.getClass().getName());
			Session session = HibernateSession.currentSession();
			
			List l = AssetType.getAssetTypes(false, true);
			Iterator i = l.iterator();
			while(i.hasNext())
			{				
				AssetType at = (AssetType)i.next();
				System.out.println("found asset type "+ at.getPersistentValue());
				if(at.equals(AssetType.AUDIO))
				{
					String hql = "SELECT a FROM Audio a WHERE a.fileloc is not null";
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Audio audio = (Audio)i2.next();
						checkfile(audio.getAssetId().intValue(), audio.getAssetName(), audio.getAssetType().toString(), audio.getFileloc());
					}
				}
				else if(at.equals(AssetType.FLASH))
				{
					String hql = "SELECT a FROM Flash a WHERE a.fileloc is not null";
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Flash flash = (Flash)i2.next();
						checkfile(flash.getAssetId().intValue(), flash.getAssetName(), flash.getAssetType().toString(), flash.getFileloc());
					}
					
				}
				else if(at.equals(AssetType.HTML))
				{
					String hql = "SELECT a FROM Html a WHERE a.fileloc is not null";	
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Html html = (Html)i2.next();
						checkfile(html.getAssetId().intValue(), html.getAssetName(), html.getAssetType().toString(), html.getFileloc());
					}
				}
				else if(at.equals(AssetType.IMAGE))
				{					
					String hql = "SELECT a FROM Image a WHERE a.fileloc is not null";
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Image image = (Image)i2.next();
						checkfile(image.getAssetId().intValue(), image.getAssetName(), image.getAssetType().toString(), image.getFileloc());
					}
				}
				else if(at.equals(AssetType.VIDEO))
				{
					String hql = "SELECT a FROM Video a WHERE a.fileloc is not null";
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Video video = (Video)i2.next();
						checkfile(video.getAssetId().intValue(), video.getAssetName(), video.getAssetType().toString(), video.getFileloc());
					}
				}
				else if(at.equals(AssetType.WEBAPP))
				{
					String hql = "SELECT a FROM Webapp a WHERE a.fileloc is not null";
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Webapp webapp = (Webapp)i2.next();
						checkfile(webapp.getAssetId().intValue(), webapp.getAssetName(), webapp.getAssetType().toString(), webapp.getFileloc());
					}
				}
				else if(at.equals(AssetType.XML))
				{
					String hql = "SELECT a FROM Xml a WHERE a.fileloc is not null";
					List l2 = session.createQuery( hql ).list();
					Iterator i2 = l2.iterator();
					while (i2.hasNext())
					{
						Xml xml = (Xml)i2.next();
						checkfile(xml.getAssetId().intValue(), xml.getAssetName(), xml.getAssetType().toString(), xml.getFileloc());
					}
				}
			}
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
	
	private void checkfile(int assetId, String assetName, String assetType, String fileloc) throws IOException
	{		
		File f = new File(fileloc);
		if (f.exists() == false)
		{
			System.out.println(assetType + " - ID:  " + assetId + ", Name: " + assetName);
			
			// If the flag was set to create the asset files, create a zero-length file
			if( createAssetFiles )
			{
				System.out.println ("Creating file: "+ fileloc);
				f.createNewFile();
			}		
		}
		
		if( f.exists() && updateAdler32Values )
		{
			Long adler32 = Asset.calculateAdler32(fileloc);
			try
			{
				Session session = HibernateSession.currentSession();
				String sql = "update asset set adler32='"+adler32+"' where asset_id="+assetId;
				updateStatements.add( sql );
				System.out.println(sql);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	private void checkAssets2()
	{
		try
		{
			SchemaDirectory.setup(this.schemaName, this.getClass().getName());
			Session session = HibernateSession.currentSession();
			SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
			Connection conn = sessionImplementor.getConnectionProvider().getConnection();

			int count = 0;
			String sql = "SELECT asset_id, asset_name, asset_type, fileloc from asset where fileloc is not null";
			if( allAssets == false )
			{
				sql += " and adler32 is null";
			}
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);	
			System.out.println("The following assets are missing their referenced files:");
			while (rs.next()) 
			{
				int asset_id = rs.getInt("asset_id");
				String asset_name = rs.getString("asset_name");
				String asset_type = rs.getString("asset_type");
				String fileloc = rs.getString("fileloc");

				if( ++count % 1000 == 0 || allAssets == false )
				{
					// System.out.print(count);
					if( allAssets == false )
					{
						System.out.print(": "+asset_id+" "+fileloc);
					}
					// System.out.println("");
				}
				checkfile( asset_id, asset_name, asset_type, fileloc );
			}
			rs.close();
			stmt.close();

			System.out.println("");
			System.out.println("Finished checking " + count + " assets");

			for( Iterator it=updateStatements.iterator(); it.hasNext(); )
			{
				sql = (String) it.next();
				stmt = conn.createStatement();
				stmt.execute(sql);
				System.out.println("executing: " + sql);
				stmt.close();
			}
			
			conn.commit();
			
			conn.close();
			HibernateSession.closeSession();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length < 1 || args.length > 3 || ( args.length == 2 && args[0].toLowerCase().equals("-u") == false ) )
		{
			System.out.println("Invalid arguments passed to Asset Checker");
			System.out.println("ex: java com.kuvata.kmf.util.AssetChecker [-u|-U] schema createAssetFile(true|false)");
			System.out.println("  -u flag causes adler32 values to be updated for assets without an adler32 value");
			System.out.println("  -U flag causes adler32 values to be updated for all assets");
			System.out.println("  createAssetFile flag specifies whether or not to create the asset file if it does not exist");
			System.exit(1);
		}

		String schema = args[0];
		boolean updateAdler32Values = false;
		boolean allAssets = true;
		boolean createAssetFiles = false;
		if( args.length > 1 )
		{
			updateAdler32Values = true;
			schema = args[1];
			if( args[0].equals("-u") )
			{
				allAssets = false;
			}			
		}
		if( args.length == 3 )
		{
			if( args[2].equalsIgnoreCase("true") )
				createAssetFiles = true;
		}
		
		AssetChecker as = new AssetChecker(schema, updateAdler32Values, allAssets, createAssetFiles);
		as.checkAssets2();		
		System.out.println("DONE");
	}
}
