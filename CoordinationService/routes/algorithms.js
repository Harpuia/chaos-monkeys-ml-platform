var express = require('express');
var utilities = require('./dbUtilities');
var router = express.Router();
var log = require('./logUtilities');

/* gets the supported languages */
router.get('/languages', function getFormats(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  //console.log(log.operationType.Upload);
  var query = 'select language from algorithm_languages';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all supported languages
  var results = connection.query(query, function getSupportedLanguages(err, rows, fields) {
    if (err) { // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      connection.end();
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the supported languages");
    }
    res.json({
      languages: rows
    }); // rows: [ cols:[]  ]
  });
  //Closing connection
  connection.end();
});

/* gets the list of algorithms from the database */
router.get('/list', function getAlgorithms(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'select * from algorithms';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all services
  var results = connection.query(query, function selectAllAlgorithms(err, rows, fields) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
     else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the algorithms list");
    }
    res.json({ algorithms: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;
