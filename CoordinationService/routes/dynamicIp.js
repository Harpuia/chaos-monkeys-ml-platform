var express = require('express');
var router = express.Router();
var mysql = require('mysql');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');

/* Convention: serviceType must be in the format specified by "ServiceTypes.xlsx",  i.e. "DataInput-<data_format>", "AlgInput-<language>", "Train-<language>" or "Exec-<language>" */
router.get('/get/:serviceType', function routeRoot(req, res, next) {
  //Getting the elements of the serviceType value
  var serviceType = req.params['serviceType'];
  if (serviceType) {
    var typeElements = serviceType.split('-');
    if (typeElements.length === 2 && (typeElements[0] === 'DataInput' || typeElements[0] === 'AlgInput' || typeElements[0] === 'Train' || typeElements[0] === 'Exec')) {
      //Connect to DB
      var connection = createDbConnection();
      connection.connect();
      var query = 'select type, ip_address from connected_services where type ="' + serviceType + '"';
      logMessage(false, log.operationType.QueryData, new Date(), query);
      //Return all services
      connection.query(query, function selectServiceTypes(err, rows, fields) {
        if (err) {  // pass the err to error handler
          logMessage(true, log.errorType.DBError, new Date(), err);
          err.source = 'mysql'; // add error source for tracing
          err.status = 500;
          next(err)
        }
        else {
          if (rows.length > 0) {
            logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the service types list");
            res.send({ message: 'success', ip: rows[0]['ip_address'] });
          }
          else {
            logMessage(true, log.errorType.LogicError, new Date(), 'No service of type ' + serviceType + ' found.');
            res.send({ message: 'No service of type ' + serviceType + ' found.', ip: undefined });
          }
        }
      });
      connection.end();
    } else {
      logMessage(true, log.errorType.LogicError, new Date(), 'Unknown service type.');
      res.send({ message: 'Unknown service type.', ip: undefined });
    }
  } else {
    logMessage(true, log.errorType.LogicError, new Date(), 'Unknown service type.');
    res.send({ message: 'Unknown service type.', ip: undefined });
  }
});

module.exports = router;