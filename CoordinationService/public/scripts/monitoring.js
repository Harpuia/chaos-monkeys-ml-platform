$(document).ready(function () {
  $('#serverIP').text(location.host);
});

//Update view with table values
setInterval(function () {
  $.getJSON('http://' + location.host + '/table', function (data) {
    var tr = '<tr class="table-active"><th>IP</th><th>Type</th><th>Name</th><th>Description</th><th>Status</th><th>Last Contacted (seconds ago)</th></tr>';
    var color;
    var displayStatus;
    var jsonStatus;
    for (i = 0; i < data['table'].length; i++) {
      var item = data['table'][i];
      if (item['last_updated'] > 60 && item['last_updated'] < 120)
        color = 'class="bg-warning"';
      else if (item['last_updated'] > 120)
        color = 'class="bg-danger"';
      else
        color = 'class="bg-success"';
      displayStatus = '';
      if (item['status']) {
        jsonStatus = JSON.parse(item['status']);
        for (var name in jsonStatus) {
          displayStatus += '<p><strong>' + name + '</strong> :' + jsonStatus[name] + '</p>';
        }
      } else {
        displayStatus = 'N/A';
      }
      tr += '<tr><td>' + item['ip_address'] + '</td><td>' + item['type'] + '</td><td>' + item['name'] + '</td><td>' + item['description'] + '</td><td style="width:30%">' + displayStatus + '</td><td ' + color + '>' + item['last_updated'] + '</td></tr>';
    }
    $('#monitoring_table').html(tr);
  })
}, 2000);
/*Download the errors_log table as a CSV file */
function exportErrLogToCSV() {
  $.get("/getErrLog", function (data) {
    if (data['errorItems'].length > 0)
      window.open('/exportErrLogToCSV');
    else {
      var alert = $('#formNoExist')[0];
      var alertText = $('#formNoExistText')[0];
      showSubmissionResult("There is no data in error log!", alert, alertText);

    }
  });
}
/*Download the operations_log table as a CSV file */
function exportOpLogToCSV() {
  $.get("/getOpLog", function (data) {
    if (data['operationItems'].length > 0)
      window.open('/exportOpLogToCSV');
    else {
      var alert = $('#formNoExist')[0];
      var alertText = $('#formNoExistText')[0];
      showSubmissionResult("There is no data in operation log!", alert, alertText);
    }
  });
}
/*Show bootstrap alert after clicking a button */
function showSubmissionResult(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}