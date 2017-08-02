var express = require('express');
var mysql = require('mysql');
var config = require('../config/dbConfig.json');

/* Create DB connection */
createDbConnection = function () {
  var connection = mysql.createConnection({
    host: config.dbhost,
    user: config.dbuser,
    password: config.dbpassword,
    database: config.dbname
  });
  return connection;
}

/* Truncates/pads numbers to two digits */
var twoDigits = function twoDigits(d) {
  if (d) {
    if (0 <= d && d < 10) return "0" + d.toString();
    if (-10 < d && d < 0) return "-0" + (-1 * d).toString();
    return d.toString();
  } else
    return null;
}