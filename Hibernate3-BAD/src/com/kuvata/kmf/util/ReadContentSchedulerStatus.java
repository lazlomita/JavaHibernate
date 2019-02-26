/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.kuvata.kmf.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import parkmedia.KmfException;

import com.kuvata.kmf.ContentSchedulerStatus;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

/**
 * @author anaber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReadContentSchedulerStatus {	
	
	public ReadContentSchedulerStatus(String schemaName)	
	{
		try
		{
			SchemaDirectory.initialize( schemaName, "Read Content Scheduler Status", null, false, true );
		}
		catch(KmfException e)
		{
			e.printStackTrace();
		}		
	}
	
	private void read(String schemaName, String contentSchedulerStatusId)
	{
		try
		{
			SchemaDirectory.setup(schemaName, this.getClass().getName());
			Session session = HibernateSession.currentSession();			
			ContentSchedulerStatus css = ContentSchedulerStatus.getContentSchedulerStatus( new Long(contentSchedulerStatusId) );			
			if( css != null )
			{
				System.out.println("Args: ");
				System.out.println( Reformat.convertClobToString( css.getArgs(), true ) );
				System.out.println("----------------------");
				System.out.println("Status: ");
				System.out.println( Reformat.convertClobToString( css.getStatus(), true ) );
			}
			else
			{
				System.out.println("Could not locate ContentSchedulerStatus object for id: "+ contentSchedulerStatusId);
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
	
	public static void main(String[] args) {
		if(args.length != 2 )
		{
			System.out.println("Usage: java com.kuvata.kmf.util.ReadContentSchedulerStatus schema contentSchedulerStatusId");			
			System.exit(1);
		}

		ReadContentSchedulerStatus as = new ReadContentSchedulerStatus( args[0] );
		as.read(args[0], args[1]);		
		System.out.println("DONE");
	}
}
