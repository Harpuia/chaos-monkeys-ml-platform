var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* gets the list of datasets from the database */
router.get('/list', function getDatasets(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select datasets.id, datasets.name, datasets.description, datasets.format, datasets.path from datasets', function selectAllDatasets(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    }
    res.json({ datasets: rows });
  });

  //Closing connection
  connection.end();
});

/* gets the table of services */
router.get('/formats', function getFormats(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select format from formats', function getAllDatasets(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    }
    res.json({ formats: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;
