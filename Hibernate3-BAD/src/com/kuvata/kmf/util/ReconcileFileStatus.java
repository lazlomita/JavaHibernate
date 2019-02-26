package com.kuvata.kmf.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import parkmedia.KuvataConfig;
import parkmedia.device.entities.FileStatus;

import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class ReconcileFileStatus {

	private static void doIt() throws Exception
	{
		try
		{
			SchemaDirectory.initialize("kuvata", "ReconcileFileStatus", null, false, true);
			
			// For each row in the file_status table
			Session session = HibernateSession.currentSession();				
			String hql = "SELECT fileStatus FROM FileStatus fileStatus";						
			List<FileStatus> fileStatuses = session.createQuery( hql ).list();
			for( Iterator<FileStatus> i=fileStatuses.iterator(); i.hasNext(); )
			{
				// Try to find the file on disk
				FileStatus fs = i.next();
				String fullPath = KuvataConfig.getKuvataHome() +"/"+ fs.getFilename();
				File f = new File( fullPath );
				if( f.exists() == false )
				{
					System.out.println("File does not exist: "+ f.getAbsolutePath() +". Deleting file_status row.");
					fs.delete();
				}
			}		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			HibernateSession.closeSession();
		}		
	}
	
	private static void reconcileList(List<FileStatus> fileStatuses)
	{

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			doIt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
