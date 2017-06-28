router.get("/listExperiments", function getExperiments(req, res) {
  var connection = createDbConnection();
  connection.connect();
  
});