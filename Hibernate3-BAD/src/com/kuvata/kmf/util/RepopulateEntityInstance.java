package com.kuvata.kmf.util;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;

import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Entity;
import com.kuvata.kmf.EntityClass;
import com.kuvata.kmf.EntityInstance;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class RepopulateEntityInstance {
	
	private static String formattedDate;
	private static Connection conn;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
		Date now = new Date();
		formattedDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE).format(now);
		SchemaDirectory.initialize("kuvata", "RepopulateEntityInstance", null, false, true);
		Session session = HibernateSession.currentSession();
		
		// Get a JDBC connection
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		conn = sessionImplementor.getConnectionProvider().getConnection();
		
		List<EntityClass> ecs = session.createQuery("FROM EntityClass").list();
		for(EntityClass ec : ecs){
			try {
				Class c = Class.forName(ec.getClassName());
				if(Entity.class.isAssignableFrom(c)){
					
					String tableName = ec.getClassName().substring(ec.getClassName().lastIndexOf(".") + 1);
					String identifierColumn = tableName.substring(0, 1).toLowerCase() + tableName.substring(1) + "Id";
					
					System.out.println("Querying " + tableName);
					String hql = "SELECT " + identifierColumn + " FROM " + tableName + " WHERE " + identifierColumn + " NOT IN (SELECT entityId FROM EntityInstance)";
					List<Long> ids = session.createQuery(hql).list();
					for(Long id : ids){
						EntityInstance ei = new EntityInstance();
						ei.setEntityId(id);
						ei.setEntityClass(ec);
						ei.setLastModified(now);
						ei.setAppUserId(1l);
						save(ei);
						System.out.println("Created entityInstance for: " + id);
					}
				}
			} catch (Exception e) {
				System.out.println("Could not query for: " + ec.getClassName());
				if(!(e instanceof java.lang.IllegalStateException) && !(e instanceof org.hibernate.hql.ast.QuerySyntaxException) && !(e instanceof java.lang.ClassNotFoundException)){
					e.printStackTrace();
				}
			}
			conn.commit();
		}
	}
	
	public static void save(EntityInstance ei) throws Exception{
		String sql = "INSERT INTO entity_instance VALUES (" + ei.getEntityId() + ", " + ei.getEntityClass().getEntityClassId() + ", 1, to_date('" + formattedDate + "', 'mm/dd/yyyy HH:MI:SS AM'))";
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		stmt.close();
	}
}
