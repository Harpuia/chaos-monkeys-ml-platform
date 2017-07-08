var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* gets the list of tasks */
router.get('/list', function getTasks(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return tasks information
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
/* Get the tasks types */
router.get('/type', function getTypes(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Get type column of task_types
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

/* Get the tasks names */
router.get('/names', function getNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Get name column of tasks table
  var results = connection.query('select id, name from tasks', function getAllNames(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ tasksnames: rows });
  });
  //Closing connection
  connection.end();
});

/* Get the datasets names */
router.get('/datasetsnames', function getDatasetsNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

//Get the id and name column of datasets table
  var results = connection.query('select id, name from datasets', function getAllDatasetsNames(err, rows, fields) {
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

/* Get the algorithms names */
router.get('/algorithmsnames', function getAlgorithmsNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Get the id and name column of algorithms table
  var results = connection.query('select id, name from algorithms', function getAllAlgorithmsNames(err, rows, fields) {
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

/* Get the models names */
router.get('/modelsnames', function getModelsNames(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Get the id and name column of models table
  var results = connection.query('select id,name from models', function getAllModelsNames(err, rows, fields) {
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

/* Create a new task in training type */
router.post('/createTrainingTask', function insertNewTask(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  console.log(req.body);

  //Insert tasks information into tasks table
  var results = connection.query('insert into tasks SET ?', req.body, function insertTask(err, result) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      //next(err);
      
    }
    res.json({newtaskinfo: req.body});
  });
  //Closing connection
  connection.end();
});

/* Create a new task in exectution type */
router.post('/createExecutionTask', function insertNewTask(req, res) {
  //Connect to DB
  var connection = createDbConnection();
  connection.connect();
  console.log(req.body);

  //Insert tasks information into tasks table
  var results = connection.query('insert into tasks SET ?', req.body, function insertTask(err, result) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      //next(err);
      
    }
    res.json({newtaskinfo: req.body});
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

/* List the tasks from model page */
router.get('/listByModel/:model_id', function listByModel(req, res) {
  var model_id = req.params.model_id;

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Return all tasks
  var results = connection.query('select name, description from tasks where model_id = ' + model_id, function listTask(err, rows, fields) {
    if (err) {  // pass the err to error handler
      err.source = 'mysql'; // add error source for tracing
      err.status = 500;
      next(err);
    }
    res.json({ tasks: rows });
  });
});

module.exports = router;