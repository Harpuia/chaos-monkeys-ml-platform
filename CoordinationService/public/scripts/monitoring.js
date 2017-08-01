//Update view with table values
setInterval(function () {
  $.getJSON('http://127.0.0.1:3000/table', function (data) {
    var tr = '<tr class="table-active"><th>IP</th><th>Type</th><th>Name</th><th>Description</th><th>Status</th><th>Last Contacted</th></tr>';
    for (i = 0; i < data['table'].length; i++) {
      var item = data['table'][i];
      tr += '<tr class="table-success"><td>' + item['ip_address'] + '</td><td>' + item['type'] + '</td><td>' + item['name'] + '</td><td>' + item['description'] + '</td><td>' + item['status'] + '</td><td>' + displayDateTime(item['last_updated']) + '</td></tr>';
    }
    $('table').html(tr);
  })
}, 2000);