var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var stylus = require('stylus');
var request = require('request');
var index = require('./routes/index');
var heartbeat = require('./routes/heartbeat');
var datasets = require('./routes/datasets');
var tasks = require('./routes/tasks');
var users = require('./routes/users');
var experiments = require('./routes/experiments');
var models = require('./routes/models');
var algorithms = require('./routes/algorithms');
var download = require('./routes/download');
var dynamicIp = require('./routes/dynamicIp');
var startup = require('./startup');

var app = express();

//View engine setup (for errors)
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

//Modules
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(stylus.middleware(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, 'public')));

//Mounting middleware
app.use('/', index);
app.use('/', heartbeat);
app.use('/datasets', datasets);
app.use('/tasks', tasks);
app.use('/users', users);
app.use('/experiments', experiments);
app.use('/models', models);
app.use('/algorithms', algorithms);
app.use('/download', download);
app.use('/dynamicIp', dynamicIp);
app.use('/public', express.static(__dirname + '/public'));
app.use('/views', express.static(__dirname + '/views'));

//Catch 404 and forward to error handler
app.use(function handleNotFoundError(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

//Error handler
app.use(function handleError(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
