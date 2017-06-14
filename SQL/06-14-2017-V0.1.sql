/* Create a database named ConfigurationDatabase. */
CREATE DATABASE IF NOT EXISTS ConfigurationDatabase;
USE ConfigurationDatabase;

/*Create a table named Formats which stores all system-supported data formats. */
DROP TABLE IF EXISTS Formats;
CREATE TABLE Formats (
  ID int NOT NULL AUTO_INCREMENT,
  format varchar(255) NOT NULL,
  PRIMARY KEY (ID)
  );
INSERT INTO Formats (Format)
VALUES ('JSON');
INSERT INTO Formats (Format)
VALUES ('CSV');

/* Create a table named  Dataset which contains the information for each uploaded data set.*/
DROP TABLE IF EXISTS Dataset;
CREATE TABLE Dataset (
  ID int NOT NULL AUTO_INCREMENT,
  project_id varchar(255) NOT NULL,
  path varchar(255) NOT NULL,
  PRIMARY KEY (ID)
  );


  