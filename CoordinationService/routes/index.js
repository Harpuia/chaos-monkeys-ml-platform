var express = require('express');
var router = express.Router();
var path = require('path');

/* List of active services. */
router.get('/', function (req, res) {
  res.sendFile(path.join(__dirname + '/../views/monitoring.html'));
});

/* Table storing all connected services */
table = [];

/* gets the table of services */
router.get('/table', function (req, res) {
  res.json({ 'table': table });
});

/* registers a service in the table */
router.post('/registerService', function (req, res) {
  var currentdate = new Date();
  var newService = req.body;
  var exists = false;
  for (i = 0; i < table.length && !exists; i++) {
    if (table[i]['ip'] === newService['ip']) {
      exists = true;
    }
  }
  if (!exists) {
    var dateTime = dateToText(currentdate);
    newService['lastcontacted'] = dateTime;
    table.push(newService);
    res.json({ response: "OK" });
  } else {
    res.json({ response: "OK" });
  }
});

/* sets status */
router.post('/setStatus', function (req, res) {
  var currentdate = new Date();
  var serviceStatus = req.body;
  var exists = false;
  var existingIndex;
  for (i = 0; i < table.length && !exists; i++) {
    if (table[i]['ip'] === serviceStatus['ip']) {
      exists = true;
      existingIndex = i;
    }
  }
  if (exists) {
    var dateTime = dateToText(currentdate);
    table[existingIndex]['lastcontacted'] = dateTime;
    table[existingIndex]['status'] = serviceStatus['status'];
    res.json({ response: "OK" });
  } else {
    res.json({ response: "ERROR" });
  }
});

/* Utility function */
var dateToText = function(currentdate){
  return currentdate.getDate() + "/"
      + (currentdate.getMonth() + 1) + "/"
      + currentdate.getFullYear() + " @ "
      + currentdate.getHours() + ":"
      + currentdate.getMinutes() + ":"
      + currentdate.getSeconds();
}

module.exports = router;