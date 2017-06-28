var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* List of active services. */
router.get('/', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/monitoring.html'));
});

router.get('/monitoring', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/monitoring.html'));
});

router.get('/algorithms', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/algorithms.html'));
});

router.get('/datasets', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/datasets.html'));
});

router.get('/experiments', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/experiments.html'));
});

router.get('/models', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/models.html'));
});

router.get('/tasks', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/tasks.html'));
});

module.exports = router;