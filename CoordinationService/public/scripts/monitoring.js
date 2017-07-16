//Update view with table values
setInterval(function () {
  $('table').find("tr:not(:first)").remove();
  $.getJSON('http://127.0.0.1:3000/table', function (data) {
    $.each(data['table'], function (i, item) {
      console.log('Works inside too!');
      var tr = $('<tr>');
      tr.append('<td>' + item['ip_address'] + '</td>');
      tr.append('<td>' + item['type'] + '</td>');
      tr.append('<td>' + item['name'] + '</td>');
      tr.append('<td>' + item['description'] + '</td>');
      //TODO: cleanup display later
      tr.append('<td>' + item['status'] + '</td>');
      tr.append('<td>' + displayDateTime(item['last_updated']) + '</td>');
      tr.append('</tr>');
      $('table').append(tr);
      console.log($('table'));
    })
  })
}, 2000);