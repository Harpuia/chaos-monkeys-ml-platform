var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');

const SERVICE_EXISTS_ERRORCODE = 1062;

/* gets the table of services */
router.get('/table', function getServiceTable(req, res, next) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var sql = 'select * from connected_services';
  logMessage(false, log.operationType.QueryData, new Date(), sql);
  var results = connection.query(sql, function getAllConnectedServices(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    } else {
      var result;
      var duration;
      for (var i = 0; i < rows.length; i++) {
        duration = Date.now() - sqlDateToEpoch(rows[i]['last_updated']);
      }
      res.json({ table: rows });
    }
  });

  //Closing connection
  connection.end();
});

/* registers a service in the table */
router.post('/registerService', function registerService(req, res, next) {
  var currentdate = new Date();
  var newService = req.body;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  newService['lastcontacted'] = toMysqlFormat(currentdate);
  var sql = 'insert into connected_services (ip_address, type, name, description, status, last_updated) values (\'' + newService['ip'] + '\',\'' + newService['type'] + '\',\'' + newService['name'] + '\',\'' + newService['description'] + '\',\'' + newService['status'] + '\',\'' + newService['lastcontacted'] + '\')';
  connection.query(sql, function checkInsertOperationStatus(err, result) {
    if (err) {
      if (err.errno === SERVICE_EXISTS_ERRORCODE)
        res.json({ response: 'The service already exists.' });
      else
        res.json({ response: err });
    } else {
      res.json({ response: 'OK' });
    }
  });
  connection.end();
});

/* sets status */
router.post('/setStatus', function handleHeartbeatMsg(req, res, next) {
  var currentdate = new Date();
  var serviceStatus = req.body;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  serviceStatus['lastcontacted'] = toMysqlFormat(currentdate);
  var sql = 'update connected_services set status = \'' + JSON.stringify(serviceStatus['status']) + '\', last_updated = \'' + serviceStatus['lastcontacted'] + '\' where ip_address =\'' + serviceStatus['ip'] + '\'';
  connection.query(sql, function checkUpdateOperationStatus(err, result) {
    if (err) {
      res.json({ response: err });
    } else {
      if (result.affectedRows === 0)
        res.json({ response: 'The service is not registered.' });
      else
        res.json({ response: 'Ok' });
    }
  });
  connection.end();
});

//Converts an SQL Date to epoch
function sqlDateToEpoch(dateString) {
  if (dateString) {
    var parts = dateString.toString().match(/([a-zA-Z]{3}) (\d{2}) (\d{4}) (\d{2}):(\d{2}):(\d{2})/);
    var value = Date.UTC(parts[3], getMonthFromString(parts[1]), parts[2], parts[4], parts[5], parts[6]);
    return value;
  } else
    return null;
}

//Gets the month number from a month string
function getMonthFromString(month) {
  if (month) {
    return new Date(Date.parse(month + " 1, 2012")).getMonth() + 1;
  }
  else
    return null;
}

module.exports = router;
