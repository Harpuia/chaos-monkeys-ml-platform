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
    res.json({ experiments: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;