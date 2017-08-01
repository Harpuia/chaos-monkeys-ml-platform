var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var log = require('./logUtilities');
/* gets the models list */
router.get('/list', function getModels(req, res, next) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'SELECT models.id as id, models.name as name, models.description as description, models.path as path, experiments.experiment_name as experiment_name FROM models join experiments on models.experiment_id = experiments.id';
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