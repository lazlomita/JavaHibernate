package com.kuvata.kmf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import parkmedia.DispatcherConstants;


public class Alive {
	
	private static Logger logger = Logger.getLogger(Alive.class);
	
	private long aliveId;
	private String msg;
		
	public long getAliveId() {
		return aliveId;
	}
	public void setAliveId(long aliveId) {
		this.aliveId = aliveId;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public static String alive(){
		SchemaDirectory.initialize("kmf", "Alive", null, false, false);
		
		Long aliveId = null;
		Date date = new Date();
		boolean success = false;
		
		try{
			// Write to database using hibernate
			Session sess = HibernateSession.currentSession();
			HibernateSession.beginTransaction();
			Alive alive = new Alive();
			alive.setMsg(date.toString());
			aliveId = (Long)sess.save(alive);
			HibernateSession.commitTransaction();
			
			if(aliveId != null){
				// Read from the alive table using jdbc
				SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
				Connection conn = sessionImplementor.getConnectionProvider().getConnection();
		    	String sql = "SELECT msg FROM alive WHERE alive_id = ?";
				PreparedStatement pstmt = conn.prepareStatement( sql );
				pstmt.setLong(1, aliveId);			
				ResultSet rs = pstmt.executeQuery();       
		        
		        while(rs.next()){
		        	if(date.toString().equals(rs.getString("msg"))){
		        		success = true;
		        	}
		        }
		        
		        pstmt.close();
		        
		        // Delete the row using jdbc
		        sql = "DELETE FROM alive WHERE alive_id = ?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setLong(1, aliveId);			
				pstmt.executeUpdate();
				pstmt.close();
				
				Statement stmt = conn.createStatement();
				stmt.executeUpdate("commit");
				stmt.close();
				
				conn.close();
			}
	    		
		}catch(Exception e){
			logger.error(e);
			HibernateSession.rollbackTransaction();
			success = false;
		}finally{
			HibernateSession.closeSession();
		}
		
		if(success)
			return DispatcherConstants.ALIVE_RESPONSE_VALID;
		else
			return DispatcherConstants.ALIVE_RESPONSE_INVALID;
	}
	
}
