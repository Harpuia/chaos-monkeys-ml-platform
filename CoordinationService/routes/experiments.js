var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

router.get("/list", function getExperiments(req, res) {
  var connection = createDbConnection();
  connection.connect();
  
  //Return all services
  var results = connection.query('select * from experiments', function getAllExperiments(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    }
    res.json({ experimentsData: rows });
  });

  //Closing connection
  connection.end();
});

/* Create a new experiment */
router.post('/createNewExperiment', function insertNewExperiment(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Insert tasks information into tasks table
  var results = connection.query('insert into experiments SET ?', req.body, function insertExperiment(err, result) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      //next(err);
      
    }
    res.json({newexperimentinfo: req.body});
  });
  //Closing connection
  connection.end();
});

module.exports = router;