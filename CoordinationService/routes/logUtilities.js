var mysql = require('mysql');
var config = require('../config/logDbConfig.json');

/* Create logdatabase connection */
createLogDbConnection = function () {
    var connection = mysql.createConnection({
        host: config.dbhost,
        user: config.dbuser,
        password: config.dbpassword,
        database: config.dbname
    });
    return connection;
}
//Error type object
var errorType = {
    'DBError': 'DBError',
    'JerseyError': 'JerseyError'
}
//Operation type object
var operationType = {
    'ResponseReceived': 'ResponseReceived',
    'QueryData': 'QueryData',
    'SubmitExperimentToRun': 'SubmitExperimentToRun'
}
//Insert any error/operation into logdatabase
logMessage = function (ifError, type, timestamp, message) {
    var logInfo = {
        "timestamp": timestamp,
        "type": type,
        "message": message
    }
    if (ifError === true) {
        {
            console.log(timestamp + " " + type + ": " + message);
            var connection = createLogDbConnection();
            connection.connect();
            //Insert errors information into errors_log table
            var query = 'insert into errors_log (timestamp,type,message) values(?,?,?)';
            var results = connection.query(query, [timestamp, type, message], function insertLog(err, result) {
                if (err) {  // pass the err to error handler
                    console.log(query);
                    err.source = 'mysql'; // add error source for tracing
                    err.status = 500;
                }
                connection.end();
            });
        }
    }
    else {
        console.log(timestamp + " " + type + ": " + message);
        var connection = createLogDbConnection();
        connection.connect();
        //Insert operations information into operations_log table
        var query = 'insert into operations_log (timestamp,type,message) values(?,?,?)';
        var results = connection.query(query, [timestamp, type, message], function insertLog(err, result) {
            if (err) {  // pass the err to error handler
                console.log("eoor occurred", err);
                err.source = 'mysql'; // add error source for tracing
                err.status = 500;
            }
            connection.end();
        });
    }
}

module.exports = {
    errorType: errorType,
    operationType: operationType
};
