var mysql = require('mysql');
var config = require('../config/dbConfig.json');

/* Create DB connection */
createDbConnection = function () {
    var connection = mysql.createConnection({
        host: config.dbhost,
        user: config.dbuser,
        password: config.dbpassword,
        database: config.dbname
    });
    return connection;
}

 var errorType = {
    'DBError':'DBError',
    'JerseyError':'JerseyError'
 }

 var operationType = {
    'ResponseReceived':'ResponseReceived',
    'QueryData':'QueryData',
    'SubmitExperimentToRun': 'SubmitExperimentToRun'
 }

logMessage = function (ifError, type, timestamp, message) {
    if(ifError===true){
        console.log(timestamp + " " + type + ": " + message);
    }
    else 
        console.log(timestamp + " " + type + ": " + message);
}

module.exports={
    errorType:errorType,
    operationType:operationType
};
