package com.jcg.hibernate.crud.operations;

import java.util.List;

import org.apache.log4j.Logger;

public class AppMain {

	public final static Logger logger = Logger.getLogger(AppMain.class);

	public static void main(String[] args) {
		int testID = 0;
		
		logger.info(".......Hibernate Crud Operations Example.......\n");

		logger.info("\n=======CREATE RECORDS=======\n");
		DbOperations.createRecord();

		logger.info("\n=======READ RECORDS=======\n");
		List<Student>viewStudents = DbOperations.displayRecords();
		if(viewStudents != null & viewStudents.size() > 0) {
			for(Student studentObj : viewStudents) {
				logger.info(studentObj.toString());
				testID = studentObj.getId();
			}
		}

		logger.info("\n=======UPDATE RECORDS=======\n");
		int updateId = testID;
		DbOperations.updateRecord(updateId);
		logger.info("\n=======READ RECORDS AFTER UPDATION=======\n");
		List<Student> updateStudent = DbOperations.displayRecords();
		if(updateStudent != null & updateStudent.size() > 0) {
			for(Student studentObj : updateStudent) {
				logger.info(studentObj.toString());
			}
		}

		logger.info("\n=======DELETE RECORD=======\n");
		int deleteId = testID;
		DbOperations.deleteRecord(deleteId);
		logger.info("\n=======READ RECORDS AFTER DELETION=======\n");
		List<Student> deleteStudentRecord = DbOperations.displayRecords();
		for(Student studentObj : deleteStudentRecord) {
			logger.info(studentObj.toString());
		}

		logger.info("\n=======DELETE ALL RECORDS=======\n");
		DbOperations.deleteAllRecords();
		logger.info("\n=======READ RECORDS AFTER ALL RECORDS DELETION=======");
		List<Student> deleteAll = DbOperations.displayRecords();
		if(deleteAll.size() == 0) {
			logger.info("\nNo Records Are Present In The Database Table!\n");
		}		
		System.exit(0);
	} 
}