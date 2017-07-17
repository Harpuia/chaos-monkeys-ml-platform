var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var request = require('request');

router.get("/list", function getExperiments(req, res, next) {
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select * from experiments', function getAllExperiments(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ experimentsData: rows });
  });

  //Closing connection
  connection.end();
});

/* Create a new experiment */
router.post('/create', function insertNewExperiment(req, res, next) {
  console.log('create');
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  //Insert tasks information into tasks table
  var results = connection.query('insert into experiments SET ?', req.body, function insertExperiment(err, result) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    //Closing connection
    connection.end();
    next(); //send to backend
  });

}, function submitExperimentToRun(req, res, next){
  // options used for submitting the experiment to Jersey backend
  var contents = { experiment_name: req.body.experiment_name };
  console.log(req.body);
  console.log(contents);
  var options = {
    url: 'http://127.0.0.1:8080/services/exp/start',
    method: 'POST',
    json: contents
  };
  request(options, function(error, response, body){
    if(!error && response.statusCode == 200){
      console.log(body);
    }
    //TODO: put this response in previous middleware when implementing the new design
    // we return a success message to the frontend for easy use now ...
    res.json({newexperimentinfo: req.body});
  });
});



module.exports = router;
