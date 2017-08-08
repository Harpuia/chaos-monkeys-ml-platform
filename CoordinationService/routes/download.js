var express = require('express');
var router = express.Router();
var path = require('path');
var utilities = require('./dbUtilities');
var request = require('request');
var zipFolder = require('zip-folder');
var fs = require('fs');

/* Downloads a document stored in one of the services */
router.get('/:entity/:id', function routeRoot(req, res, next) {
  //The url should be called as follows: <baseURL>/download/dataset/5, for example
  var entityName = req.params['entity'];
  var entityId = req.params['id'];
  var sql;
  var filePath;
  var tableName;
  switch (entityName) {
    case ('model'):
      tableName = 'models';
      break;
    case ('dataset'):
      tableName = 'datasets';
      break;
    case ('algorithm'):
      tableName = 'algorithms';
      break;
    case ('prediction'):
      tableName = 'predictions';
      break;
    default:
      res.send({ error: 'Incorrect parameters.' });
      break;
  }

  //Creating sql
  sql = 'select path from ' + tableName + ' where id = ' + entityId;
  console.log(sql);

  //Connect to DB
  var connection = createDbConnection();
  connection.connect();

  //Reading path
  connection.query(sql, function getModelPath(err, result) {
    if (err) {
      console.log(err);
      res.send({ error: err });
    }
    else {
      if (result && result.length > 0) {
        filePath = result[0]['path'];
        if (tableName === 'models' || tableName === 'algorithms' || tableName === 'predictions') {
          var fullPath = path.join(filePath, 'archive.zip');
          if (!fs.existsSync(path)) {
            zipFolder(filePath, fullPath, function (err) {
              if (err) {
                console.log('oh no!', err);
              } else {
                console.log('EXCELLENT');
                res.download(fullPath);
              }
            });
          } else{
            res.download(fullPath);
          }
        } else {
          res.download(filePath);
        }
      }
    }
  });

  //Closing connection
  connection.end();
});

module.exports = router;