var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* gets the table of services */
router.get('/list', function getTasks(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select tasks.id, projects.name as project_name, datasets.name as dataset_name, algorithms.name as algorithm_name, tasks.name, tasks.description, tasks.type from tasks join projects on tasks.project_id = projects.id join datasets on tasks.dataset_id = datasets.id join algorithms on tasks.algorithm_id = algorithms.id', function getAllTasks(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ tasks: rows });
  });

  //Closing connection
  connection.end();
});
/* gets the table of services */
router.get('/type', function getTypes(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select type from task_types', function getAllTypes(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ types: rows });
  });
  //Closing connection
  connection.end();
});

/* gets the table of services */
router.get('/datasetsnames', function getDatasetsNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select name from datasets', function getAllDatasetsNames(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ datasetsnames: rows });
  });
  //Closing connection
  connection.end();
});

/* gets the table of services */
router.get('/algorithmsnames', function getAlgorithmsNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select name from algorithms', function getAllAlgorithmsNames(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ algorithmsnames: rows });
  });
  //Closing connection
  connection.end();
});

/* gets the table of services */
router.get('/modelsnames', function getModelsNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('select name from models', function getAllModelsNames(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ modelsnames: rows });
  });
  //Closing connection
  connection.end();
});

/* gets the table of services */
router.post('/create', function insertNewTask(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all services
  var results = connection.query('insert into tasks ', function insertTask(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ modelsnames: rows });
  });
  //Closing connection
  connection.end();
});

/* List the tasks from dataset page */
router.get('/listByDataset/:dataset_id', function listByDataset(req, res) {
  var dataset_id = req.params.dataset_id;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all tasks
  var results = connection.query('select name, description from tasks where dataset_id = ' + dataset_id, function listTask(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ tasks: rows });
  });
});

module.exports = router;
