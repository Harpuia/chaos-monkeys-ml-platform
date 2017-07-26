var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var request = require('request');

/* Downloads a document stored in one of the services */
router.get('/:entity/:id', function routeRoot(req, res) {
  //The url should be called as follows: <baseURL>/download/dataset/5, for example
  var entityName = req.params['entity'];
  var entityId = req.params['id'];
  var sql;
  var path;
  switch (entityName) {
    case ('model'):
      //Connect to DB
      var connection = createDbConnection();
      connection.connect();

      //Creating sql
      sql = 'select path from models where id = ' + entityId;

      //Reading path
      connection.query(sql, function getModelPath(err, result) {
        if (err) {
          console.log(err);
          res.send({ error: err });
        }
        else {
          if(result && result.length > 0){
            path = result[0]['path'];
            res.download(path);
          }
        }
      });

      //Closing connection
      connection.end();
      break;
    default:
      res.send({ error: 'Incorrect parameters.' });
      break;
  }
});

module.exports = router;