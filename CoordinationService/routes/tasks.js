var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* gets the table of services */
router.get('/list', function getTaks(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select tasks.id, projects.name as project_name, datasets.name as dataset_name, algorithms.name as algorithm_name, tasks.name, tasks.description, tasks.type from tasks join projects on tasks.project_id = projects.id join datasets on tasks.dataset_id = datasets.id join algorithms on tasks.algorithm_id = algorithms.id', function getAllTasks(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err)
    }
    res.json({ tasks: rows });
  });

  //Closing connection
  connection.end();
});

module.exports = router;
