var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var request = require('request');
var log = require('./logUtilities');
var dynamicIp = require('./dynamicIp');

//Lists all experiments
router.get("/list", function getExperiments(req, res, next) {
  var connection = createDbConnection();
  connection.connect();
  var query = 'select experiments.id as id, experiments.task_id as task_id, experiments.experiment_name as experiment_name, experiments.start as start, experiments.end as end, experiments.last_status as last_status, experiments.last_updated as last_updated, experiments.description as description, experiments.error_message as error_message, predictions.id as prediction_id from experiments left join predictions on experiments.id = predictions.experiment_id';
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
  connection.query(query, req.body, function insertExperiment(err, result) {
    //Closing connection
    connection.end();
    if (err) { // pass the err to error handler
      logMessage(true, log.errorType.DBError, new Date(), err);
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    } else {
      logMessage(false, log.operationType.ResponseReceived, new Date(), "Created a new experiment");
      next();
    }
  });

}, function submitExperimentToRun(req, res, next) {
  //Options used for submitting the experiment to Jersey backend
  var contents = { experiment_name: req.body.experiment_name };
  logMessage(false, log.operationType.SubmitExperimentToRun, new Date(), "Submitting the experiment to Jersey backend");
  var taskId = req.body.task_id;

  //Getting experiment type
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
          if (value.message === 'success') {
            var address = 'http://' + value.ip + '/services/exp/start';
            var options = {
              url: address,
              method: 'POST',
              json: contents
            };
            request(options, function (error, response, body) {
              if (!error && response.statusCode === 200) {
                logMessage(false, log.operationType.ResponseReceived, new Date(), "Received response from Jeysey backend");
                //TODO: put this response in previous middleware when implementing the new design
                // we return a success message to the frontend for easy use now ...
                res.json({ newexperimentinfo: req.body });
              } else if (!error && response.statusCode === 400) {
                logMessage(false, log.operationType.ResponseReceived, new Date(), "Jeysey backend refused this request due to bad request");
                //TODO: delete the experiment record just created
                res.status(400).send({ code: body.code, msg: body.msg });
              } else {
                logMessage(true, log.errorType.JerseyError, new Date(), error);
                res.status(500).send({ code: 399, msg: "Server may not work now, please try later or contact admin" });
              }
            });
          } else {
            logMessage(true, log.errorType.JerseyError, new Date(), 'Training/Execution service not found.');
            res.status(500).send({ code: 399, msg: "Server may not work now, please try later or contact admin" });
          }
        });
      }
    }
  });
  //Closing connection
  connection.end();
});


module.exports = router;
