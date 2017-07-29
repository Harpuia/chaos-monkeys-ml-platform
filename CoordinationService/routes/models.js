var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');
/* gets the models list */
router.get('/list', function getModels(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'select * from models';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all services
  var results = connection.query(query, function getAllModels(err, rows, fields) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the models list");
    }
    res.json({ models: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;
