var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');

/* gets the list of datasets from the database */
router.get('/list', function getDatasets(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'select datasets.id, datasets.name, datasets.description, datasets.format, datasets.path from datasets';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all services
  var results = connection.query(query, function selectAllDatasets(err, rows, fields) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the datasets list");
    }
    res.json({ datasets: rows });
  });

  //Closing connection
  connection.end();
});

/* gets the supported formats */
router.get('/formats', function getFormats(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'select format from formats';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all services
  var results = connection.query(query, function getAllDatasets(err, rows, fields) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the supported formats list");
    }
    res.json({ formats: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;
