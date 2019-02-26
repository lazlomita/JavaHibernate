package com.kuvata.kmf;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class PreDeleteThread extends Thread{

	private static Logger logger = Logger.getLogger(PreDeleteThread.class);
	private List ids;
	private String schemaName;
	
	public void run(){
		// Initialize the schema
		SchemaDirectory.initialize( this.getSchemaName(), "PreDeleteThread", "Background Remove", false, false );
		
		// Start the delete process
		delete(this.ids);
	}

	private static void delete(List ids){
	
		// Get the current session and start the transaction
		HibernateSession.beginTransaction();

		try {
			// Delete from the entity_instance table
			String hql = "Delete from EntityInstance as e where e.entityId in (:ids)";
			HibernateSession.executeBulkUpdate( hql, ids );
			
			// Delete from attr table
			hql = "DELETE FROM Attr WHERE owner_id in (:ids)";
			HibernateSession.executeBulkUpdate( hql, ids );
			
			// Delete from the permission_entry table
			hql = "DELETE FROM PermissionEntry WHERE permission_entity_id in (:ids)";
			HibernateSession.executeBulkUpdate( hql, ids );
			
			// Delete from the dirty table
			hql = "DELETE FROM Dirty WHERE dirty_entity_id in (:ids)";
			HibernateSession.executeBulkUpdate( hql, ids );
			
			HibernateSession.commitTransaction();
			
		} catch (Exception e) {
			HibernateSession.rollbackTransaction();
			logger.error(e);
		}
	}
	
	public void setIds(List ids) {
		this.ids = ids;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getSchemaName() {
		return schemaName;
	}
}
