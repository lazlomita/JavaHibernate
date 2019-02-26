package avantica.hibernate.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class SimpleTest {
	
	private static SessionFactory mySessionFactory;
	
	private static void initSessionFactory() {
		Configuration cfg = new Configuration();
		cfg.configure("hibernate.cfg.xml");
		mySessionFactory = cfg.buildSessionFactory();
	}
	

	private static SessionFactory getSessionFactory() {
		return mySessionFactory;
	}
	
	

	public static void main(String[] args) {
		
		initSessionFactory();
		
		System.out.println("**** Start Hello test ******");
		
		//Message message = new Message("Hello World");
		//System.out.println( message.getText() );
		
		Session session = getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		Message message = new Message("Hello World");
		message.setId(new Long(123));
		session.save(message);
		tx.commit();
		session.close();
		
		System.out.println("**** End Hello test ******");
		

		Session session2 = getSessionFactory().openSession();

		Student student = new Student();
		student.setId(10);
		student.setName("Lazlo");
		student.setRoll("101");
		student.setPhone("8888");
		student.setDegree("B.E");
		
		Student student2 = new Student();
		student2.setId(11);
		student2.setName("Carolina");
		student2.setRoll("101");
		student2.setPhone("8888");
		student2.setDegree("B.E");

		Transaction tx1 = session2.beginTransaction();
		session2.save(student);
		System.out.println("Object saved successfully.....!!");
		tx1.commit();
		session2.clear();
		

		Transaction tx2 = session2.beginTransaction();
		session2.save(student2);
		System.out.println("Object saved successfully.....!!");
		tx2.commit();
		
		
		session2.close();
		getSessionFactory().close();
	}
}