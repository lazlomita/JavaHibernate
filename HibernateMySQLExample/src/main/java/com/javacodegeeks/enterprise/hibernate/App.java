package com.javacodegeeks.enterprise.hibernate;

import org.hibernate.Session;

import com.javacodegeeks.enterprise.hibernate.utils.HibernateUtil;
 
public class App 
{
    public static void main( String[] args )
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
 
        session.beginTransaction();
        Student student = new Student();
 
        student.setStudentName("JavaFun");
        student.setStudentAge("19");
 
        session.save(student);
        session.getTransaction().commit();
        System.out.println("Great! Student was saved");
    }
}