package avantica.hibernate.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import avantica.hibernate.entities.Message;
import avantica.hibernate.entities.MessageSec;
import avantica.hibernate.entities.Student;

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
		System.out.println("**** Start Hello test: No hibernate ******");
		
		Message messageNoHibernate = new Message("Hello World");
		System.out.println( messageNoHibernate.getText() );
		
		
		
		System.out.println("**** Start Hello test: Message ******");
		
		Message message1 = new Message("Hello World");
		message1.setId(new Long(123));

		Session session1 = getSessionFactory().openSession();
		Transaction tx1 = session1.beginTransaction();
		session1.save(message1);
		tx1.commit();
		session1.close();
				
		
		
		System.out.println("**** Start Hello test: MessageSec ******");
				
		MessageSec message2 = new MessageSec("Hello World");
		message2.setId(new Long(123));

		Session session2 = getSessionFactory().openSession();
		Transaction tx2 = session2.beginTransaction();
		session2.save(message2);
		tx2.commit();
		session2.close();
				
		

		System.out.println("**** Start Hello test: Student ******");
		
		Student student1 = new Student();
		student1.setId(10);
		student1.setName("Lazlo");
		student1.setRoll("101");
		student1.setPhone("8888");
		student1.setDegree("B.E");
		
		Student student2 = new Student();
		student2.setId(11);
		student2.setName("Carolina");
		student2.setRoll("101");
		student2.setPhone("8888");
		student2.setDegree("B.E");

		Session session3 = getSessionFactory().openSession();
		Transaction tx3 = session3.beginTransaction();
		
		session3.save(student1);
		System.out.println("Student Object saved successfully.....!!");
		session3.save(student2);
		System.out.println("Student Object saved successfully.....!!");
		
		tx3.commit();		
		session3.close();
		
		
		
		getSessionFactory().close();
	}
}