var express = require('express');
var router = express.Router();
var dbUtilities = require('./routes/dbUtilities');
var log = require('./routes/logUtilities');

//Connect to DB
var connection = createDbConnection();
connection.connect();
var sql = 'delete from connected_services';
logMessage(false, log.operationType.QueryData, new Date(), sql);
//Return all services
connection.query(sql, function deleteAllConnectedServices(err, result) {
  if (err) {
    logMessage(true, log.errorType.DBError, new Date(), err);
  } else {
    logMessage(true, log.operationType.ResponseReceived, new Date(), 'Query successful: connected_services table cleared.');
  }
});

module.exports = router;