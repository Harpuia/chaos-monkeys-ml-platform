# CMU Chaos Monkeys Experimentation Platform

## Introduction

This archive includes the sources for this project. The system is tested under Ubuntu 16.04 and Windows 10 with the following packages installed.

* JDK 1.8
* RScript 3.2.3
* NodeJS 8
* MySQL 5.7  
* Maven 3.3.9

## Installation
* Open the terminal / CMD.
* Make sure the MySQL server has started. If the server is not on, please follow the instruction below.

For UNIX-based system, type the following commands in the terminal: 

```
$ mysql.server start
```

For Windows users, type the following commands in CMD:

```
C:\ start mysqlId -u root
```

* Run the command line MySQL interface to connect to the MySQL server

**Windows**

```
C:\ mysql --user=root
```

**UNIX**

```
$ mysql -u root
```

* You need to create a database by

```
mysql> create database configurationdatabase;
mysql> create database logdatabase
```

* At this point exit from MySQL by typing `exit` at the MySQL prompt. In your OS shell (**not the MySQL command line window**), navigate to folder `SQL` and import the sample data by typing the following commands (*root pwd* is the password you set for the MySQL root administrator):

**Windows**

```
C:\<workspace> cd SQL
C:\<workspace>\SQL mysql -u root -p configurationdatabase < 08-08-2017-V1.4.sql 
C:\<workspace>\SQL mysql -u root -p logdatabase < logdb-08-06-2017-V0.2.sql
```
**UNIX**

```
$ cd SQL
$ mysql -u root -p configurationdatabase < 08-08-2017-V1.4.sql
$ mysql -u root -p logdatabase < logdb-08-06-2017-V0.2.sql
```

* Next, you will need to configure the parameters of MySQL in both Coordination Service and the Jersey Services to your customized settings. You can do this by editing the database config files in the path `CoordinationService/config/dbConfig.json` and `JerseySerices/dbConfig.ini`.

## To Build and Run the Coordination Service

```
$ cd CoordinationServices
$ npm install
$ npm start
```

## To Build Jersey Services

```
$ bash JerseyServices/build.sh
```

## To Run All Jersey Services

```
$ cd JerseyServices/launch_scripts/
$ bash run_jersey_all.sh
```

## Or, Run Jersey Services Separately

### To Run Algorithm Input Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_algr_input.sh
```

### To Run Data Input Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_data_input.sh
```

### To Run Training Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_training.sh
```

### To Run Execution Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_algr_input.sh
```