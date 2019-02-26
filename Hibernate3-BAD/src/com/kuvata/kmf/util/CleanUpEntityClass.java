package com.kuvata.kmf.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import com.kuvata.kmf.EntityClass;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class CleanUpEntityClass {

	public static void main(String[] args) throws Exception{
		SchemaDirectory.initialize("kuvata", "CleanUpEntityClass", null, false, true);
		Session session = HibernateSession.currentSession();
		
		HashMap<String, LinkedList<Long>> entityClasses = new HashMap<String, LinkedList<Long>>();
		List<EntityClass> ecs = session.createQuery("FROM EntityClass ec ORDER BY ec.entityClassId").list();
		for(EntityClass ec : ecs){
			if(entityClasses.containsKey(ec.getClassName())){
				entityClasses.get(ec.getClassName()).add(ec.getEntityClassId());
			}else{
				LinkedList<Long> ecList = new LinkedList<Long>();
				ecList.add(ec.getEntityClassId());
				entityClasses.put(ec.getClassName(), ecList);
			}
		}
		
		// Get a JDBC connection
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();
		
		for(Iterator<Entry<String,LinkedList<Long>>> i = entityClasses.entrySet().iterator(); i.hasNext();){
			Entry<String, LinkedList<Long>> e = i.next();
			LinkedList<Long> ecList = e.getValue();
			if(ecList.size() > 1){
				Long entityClassId = ecList.get(0);
				for(int j=1; j<ecList.size(); j++){
					String sql = "UPDATE entity_instance SET entity_class_id = " + entityClassId + " WHERE entity_class_id = " + ecList.get(j);
					System.out.println("Executing statement: " + sql);
					Statement stmt = conn.createStatement();
					stmt.execute(sql);
					stmt.close();
					
					sql = "DELETE FROM entity_class WHERE entity_class_id = " + ecList.get(j);
					System.out.println("Executing statement: " + sql);
					stmt = conn.createStatement();
					stmt.execute(sql);
				}
			}
			
			conn.commit();
		}
	}
}
