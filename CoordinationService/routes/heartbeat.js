var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

const SERVICE_EXISTS_ERRORCODE = 1062;

/* gets the table of services */
router.get('/table', function getServiceTable(req, res) {
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
router.post('/registerService', function registerService(req, res) {
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
      res.json({ response: 'Ok.' });
    }
  });
  connection.end();
});

/* sets status */
router.post('/setStatus', function handleHeartbeatMsg(req, res) {
  var currentdate = new Date();
  var serviceStatus = req.body;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  serviceStatus['lastcontacted'] = toMysqlFormat(currentdate);
  var sql = 'update connected_services set status = \'' + serviceStatus['status'] + '\', last_updated = \'' + serviceStatus['lastcontacted'] + '\' where ip_address =\'' + serviceStatus['ip'] + '\'';
  console.log(sql);
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

module.exports = router;