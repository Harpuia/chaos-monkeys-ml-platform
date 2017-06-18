var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* List of active services. */
router.get('/', function (req, res) {
  res.sendFile(path.join(__dirname + '/../views/monitoring.html'));
});

/* Table storing all connected services */
table = [];

/* gets the table of services */
router.get('/table', function (req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select * from connected_services', function (err, rows, fields) {
    res.json(rows);
  });

  //Closing connection
  connection.end();
});

/* registers a service in the table */
router.post('/registerService', function (req, res) {
  var currentdate = new Date();
  var newService = req.body;
  var exists = false;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Find whether service already exists
  var results = connection.query('select * from connected_services where ip_address = "' + newService['ip'] + '"', function (err, rows, fields) {
    if (rows && rows.length > 0) {
      exists = true;
    }
  });

  //If the service is not registered, it gets registered
  if (!exists) {
    newService['lastcontacted'] = toMysqlFormat(currentdate);
    var sql = 'insert into connected_services (ip_address, type, name, description, status, last_updated) values ("' + newService['ip'] + '","' + newService['type'] + '","' + newService['name'] + '","' + newService['description'] + '","' + newService['status'] + '","' + newService['lastcontacted'] + '")';
    connection.query(sql, function (err, result) {
      if (err) {
        console.log(err);
      } else {
        console.log(result);
      }
    });
    connection.end();
    res.json({ response: "OK" });
  } else {
    res.json({ response: "OK", message: "Service already exists." });
    connection.end();
  }
});

/* sets status */
router.post('/setStatus', function (req, res) {
  var currentdate = new Date();
  var serviceStatus = req.body;
  var exists = false;
  var existingIndex;
  for (i = 0; i < table.length && !exists; i++) {
    if (table[i]['ip'] === serviceStatus['ip']) {
      exists = true;
      existingIndex = i;
    }
  }
  if (exists) {
    var dateTime = dateToText(currentdate);
    table[existingIndex]['lastcontacted'] = dateTime;
    table[existingIndex]['status'] = serviceStatus['status'];
    res.json({ response: "OK" });
  } else {
    res.json({ response: "ERROR" });
  }
});

/* Utility function */
var dateToText = function (currentdate) {
  return currentdate.getDate() + "/"
    + (currentdate.getMonth() + 1) + "/"
    + currentdate.getFullYear() + " @ "
    + currentdate.getHours() + ":"
    + currentdate.getMinutes() + ":"
    + currentdate.getSeconds();
}

module.exports = router;