var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var request = require('request');
var log = require('./logUtilities');
router.get("/list", function getExperiments(req, res, next) {
  var connection = createDbConnection();
  connection.connect();
  var query = 'select * from experiments';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Return all services
  var results = connection.query(query, function getAllExperiments(err, rows, fields) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received the experiments list");
    }
    res.json({ experimentsData: rows });
  });

  //Closing connection
  connection.end();
});

/* Create a new experiment */
router.post('/create', function insertNewExperiment(req, res, next) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  var query = 'insert into experiments SET ?';
  logMessage(false, log.operationType.QueryData, new Date(), query);
  //Insert tasks information into tasks table
  var results = connection.query(query, req.body, function insertExperiment(err, result) {
    if (err) {  // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Created a new experiment");
    }
    //Closing connection
    connection.end();
    next(); //send to backend
  });

}, function submitExperimentToRun(req, res, next) {
  // options used for submitting the experiment to Jersey backend
  var contents = { experiment_name: req.body.experiment_name };
  logMessage(false, log.operationType.SubmitExperimentToRun, new Date(), "Submitting the experiment to Jersey backend");
  var options = {
    url: 'http://127.0.0.1:8080/services/exp/start',
    method: 'POST',
    json: contents
  };
  request(options, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Received response from Jeysey backend");
    }else {
      logMessage(true, log.errorType.JerseyError, new Date(), error);
    }
    //TODO: put this response in previous middleware when implementing the new design
    // we return a success message to the frontend for easy use now ...
    res.json({ newexperimentinfo: req.body });
  });
});



module.exports = router;
