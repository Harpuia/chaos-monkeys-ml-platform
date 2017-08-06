package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;
/**
 * class used to manipulate log database using ActiveJDBC ORM
 */
@DbName("logdatabase")
@Table("`errors_log`")
public class ErrorLog extends Model{
}
