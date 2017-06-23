var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');

/* List of active services. */
router.get('/', function routeRoot(req, res) {
  res.sendFile(path.join(__dirname + '/../views/monitoring.html'));
});

module.exports = router;