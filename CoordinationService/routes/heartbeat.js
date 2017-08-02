var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');

const SERVICE_EXISTS_ERRORCODE = 1062;

/* gets the table of services, controls liveness of remote services and deregisters non responsive ones after 3 minutes */
router.get('/table', function getServiceTable(req, res, next) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  ip = '';
  result = [];

  //Return all services
  var sql = 'select * from connected_services';
  logMessage(false, log.operationType.QueryData, new Date(), sql);
  connection.query(sql, function getAllConnectedServices(err, rows, fields) {
    if (err) {
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql';
      err.status = 500;
      next(err)
    } else {
      result = [];
      var duration;
      for (var i = 0; i < rows.length; i++) {
        duration = Date.now() - rows[i]['last_updated'];
        rows[i]['last_updated'] = duration / 1000;
        ip = rows[i]['ip'];
        if (duration > 180000) {
          //Remove service from table
          var sqlDelete = 'delete from connected_services where id ="' + rows[i]['id'] + '"';
          logMessage(false, log.operationType.QueryData, new Date(), sqlDelete);
          var results = connection.query(sqlDelete, function deleteInactiveServer(err, resp) {
            if (err) {
              logMessage(true, log.errorType.DBError, new Date(), err);
              err.source = 'mysql';
              err.status = 500;
              next(err)
            } else {
              logMessage(false, log.operationType.ResponseReceived, new Date(), 'Successfully deleted inactive server with IP: ' + ip);
            }
          });
        } else {
          result.push(rows[i]);
        }
      }
      res.json({ table: result });
    }
  });
  //Closing connection
  connection.end();
});

/* registers a service in the table */
router.post('/registerService', function registerService(req, res, next) {
  var currentdate = Date.now();
  var newService = req.body;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var sql = 'insert into connected_services (ip_address, type, name, description, status, last_updated) values (\'' + newService['ip'] + '\',\'' + newService['type'] + '\',\'' + newService['name'] + '\',\'' + newService['description'] + '\',\'' + newService['status'] + '\',' + currentdate + ')';
  logMessage(false, log.operationType.QueryData, new Date(), sql);
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
  var currentdate = Date.now();
  var serviceStatus = req.body;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var sql = 'update connected_services set status = \'' + JSON.stringify(serviceStatus['status']) + '\', last_updated = ' + currentdate + ' where ip_address =\'' + serviceStatus['ip'] + '\'';
  logMessage(false, log.operationType.QueryData, new Date(), sql);
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
