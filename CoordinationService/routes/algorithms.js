var express = require('express');
var utilities = require('./dbUtilities');
var router = express.Router();

/* gets the supported languages */
router.get('/languages', function getFormats(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all supported languages
  var results = connection.query('select language from algorithm_languages', function getSupportedLanguages(err, rows, fields) {
    if (err) { // pass the err to error handler
      connection.end();
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({
      languages: rows
    }); // rows: [ cols:[]  ]
  });
  //Closing connection
  connection.end();
});

module.exports = router;
