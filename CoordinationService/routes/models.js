var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* gets the table of services */
router.get('/list', function getModels(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select * from models', function getAllModels(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ models: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;
