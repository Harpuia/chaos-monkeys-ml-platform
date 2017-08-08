var express = require('express');
var router = express.Router();
var mysql = require('mysql');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');

/* Convention: serviceType must be in the format specified by "ServiceTypes.xlsx",  i.e. "DataInput-<data_format>", "AlgInput-<language>", "Train-<language>" or "Exec-<language>" */
router.get('/get/:serviceType', function loadIp(req, res, next) {
  //Getting the elements of the serviceType value
  var serviceType = req.params['serviceType'];
  loadServiceIp(serviceType, function (value) {
    res.send(value);
  });
});

//Allows to get experiment IP from experiment id
router.get('/getExperimentIp/:experimentTaskId', function loadIp(req, res, next) {
  //Getting the elements of the serviceType value
  var taskId = req.params['experimentTaskId'];

  //Querying for experiment type
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'select tasks.`type` as `type`, algorithms.`language` as `language` from tasks inner join algorithms on tasks.algorithm_id=algorithms.id where tasks.id=' + taskId;
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all services
  var results = connection.query(query, function getTaskType(err, rows, fields) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the task type");
      if (rows.length > 0) {
        //Deciding which service to call
        var taskType = rows[0]['type'];
        var taskLanguage = rows[0]['language'];
        var serviceType = '-' + taskLanguage;
        if (taskType === 'Training')
          serviceType = 'Train' + serviceType;
        else
          serviceType = 'Exec' + serviceType;

        //Getting service IP
        loadServiceIp(serviceType, function (value) {
          res.send(value);
        });
      } else {
        res.send({ message: 'No service of type ' + serviceType + ' found.', ip: undefined })
      }
    }
  });
  //Closing connection
  connection.end();
});

loadServiceIp = function (serviceType, callback) {
  if (serviceType) {
    var typeElements = serviceType.split('-');
    if (typeElements.length === 2 && (typeElements[0] === 'DataInput' || typeElements[0] === 'AlgInput' || typeElements[0] === 'Train' || typeElements[0] === 'Exec')) {
      //Connect to DB
      var connection = createDbConnection();
      connection.connect();
      var query = 'select type, ip_address from connected_services where `type`="' + serviceType + '"';
      logMessage(false, log.operationType.QueryData, new Date(), query);
      //Return all services
      connection.query(query, function selectServiceTypes(err, rows, fields) {
        if (err) {  // pass the err to error handler
          logMessage(true, log.errorType.DBError, new Date(), err);
          callback(null);
        }
        else {
          if (rows.length > 0) {
            logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the service types list");
            callback({ message: 'success', ip: rows[0]['ip_address'] });
          }
          else {
            logMessage(true, log.errorType.LogicError, new Date(), 'No service of type ' + serviceType + ' found.');
            callback({ message: 'No service of type ' + serviceType + ' found.', ip: undefined });
          }
        }
      });
      connection.end();
    } else {
      logMessage(true, log.errorType.LogicError, new Date(), 'Unknown service type.');
      callback({ message: 'Unknown service type.', ip: undefined });
    }
  } else {
    logMessage(true, log.errorType.LogicError, new Date(), 'Unknown service type.');
    callback({ message: 'Unknown service type.', ip: undefined });
  }
}

module.exports = router;