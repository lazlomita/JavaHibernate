CREATE DATABASE IF NOT EXISTS hibernate3_5;

USE hibernate3_5;

DROP TABLE IF EXISTS student;

CREATE TABLE IF NOT EXISTS student (
  student_id int(100) NOT NULL AUTO_INCREMENT,
  student_name varchar(50) DEFAULT NULL,
  roll_number varchar(50) DEFAULT NULL,
  course varchar(50) DEFAULT NULL,  
  PRIMARY KEY (student_id)
);

SELECT * FROM student;