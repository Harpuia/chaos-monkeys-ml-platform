//All experiments data
var experimentsData;
//Initialization
$(document).ready(function () {
  loadPage();
  loadTaskNames();
});

//Load the page
function loadPage() {
  //Reset a specific modal 
  resetModal('createExperimentModal');
  //Load upload types
  $.get("experiments/list", function (data) {
    var experimentsList = '';
    experimentsData = data['experimentsData'];
    if (!experimentsData || experimentsData.length === 0) {
      $('#experimentsTableBody').html('<h3>This list is empty!</h3>');
    } else {
      for (i = 0; i < data['experimentsData'].length; i++) {
        var cssLabel;
        if (data['experimentsData'][i]['last_status'] === 'SUCCESS' || data['experimentsData'][i]['last_status'] === 'CANCELLED')
          cssLabel = 'label-success';
        else if (data['experimentsData'][i]['last_status'] === 'ERROR')
          cssLabel = 'label-danger';
        else if(data['experimentsData'][i]['last_status'] === 'IDLE')
          cssLabel = 'label-default';
        else
          cssLabel = 'label-warning';
        experimentsList += '<tr><td><span class="fa fa-flask" aria-hidden="true"></span>&nbsp;&nbsp;' + data['experimentsData'][i]['experiment_name'] + '</td><td>' + data['experimentsData'][i]['description'] + '<br><span class="label ' + cssLabel + '">' + data['experimentsData'][i]['last_status'] + '</span></td><td><button type="button" onclick="displayDetails(' + i + ')" class="btn btn-primary">Details</button></td></tr>';
      }
      $('#experimentsTableBody').html(experimentsList);
    }
  });

}

function loadTaskNames() {
  //Load task names
  $.get("tasks/names", function (data) {
    var tasksnames = '';
    for (i = 0; i < data['tasksnames'].length; i++) {
      tasksnames += '<option value="' + data['tasksnames'][i]['id'] + '">' + data['tasksnames'][i]['name'] + '</option>\n';
    }
    $('#tasksNames').html(tasksnames);
  });
}

/*Submit the form to create a new experiment */
function submitForm() {
  var success = $('#formSuccess')[0];
  var successText = $('#formSuccessText')[0];
  var alert = $('#formError')[0];
  var alertText = $('#formErrorText')[0];
  var experimentInfo = {
    "project_id": $("#projectId").val(),
    "task_id": $("#tasksNames").val(),
    "experiment_name": $("#experimentName").val(),
    "start": null,
    "end": null,
    "last_status": "IDLE",
    "last_updated": null,
    "description": $("#description").val()
  };

  if (checkRequiredFields()) {
    //Disabling submit button
    $('#submitButton').prop("disabled", true);

    $.ajax({
      url: "http://127.0.0.1:3000/experiments/create",
      type: "POST",
      dataType: "json",
      contentType: 'application/json',
      data: JSON.stringify(experimentInfo),
      success: function (data) {
        showSubmissionResult("The experiment " + experimentInfo.experiment_name + " has been created successfully.", success, successText);
        loadPage();
        loadTaskNames();
      },
      error: function (request, status, error) {
        //Disabling submit button
        $('#submitButton').prop("disabled", false);
        showSubmissionResult("Oops! An error occurs when creating the task. Please check the error log in log path for possible reasons: " + status + error, alert, alertText);
      }
    });
  }
}



/*Check if the required field are filled */
function checkRequiredFields() {
  var experimentName = $("#experimentName").val();
  var t = $("#tasksNames")[0];
  var selectedTaskName = t.options[t.selectedIndex].text;
  var projectId = $("#projectId").val();
  var alert = $('#formError')[0];
  var alertText = $('#formErrorText')[0];
  if (experimentName.length == 0) {
    showSubmissionResult('Please input an experiment name.', alert, alertText);
    return false;
  }
  else if (projectId.length == 0) {
    showSubmissionResult('Please input the Project ID.', alert, alertText);
    return false;
  }
  else if (selectedTaskName.length == 0) {
    showSubmissionResult('Please select a task.', alert, alertText);
    return false;
  }
  else
    return true;

}

//Show submission result after clicking Submit button
function showSubmissionResult(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}

/*Displays details for a selected experiment */
function displayDetails(experimentIndex) {
  $('#experimentNameDetail').text(experimentsData[experimentIndex]['experiment_name']);
  $('#experimentStartTime').text(displayDateTime(experimentsData[experimentIndex]['start']));
  $('#experimentEndTime').text(displayDateTime(experimentsData[experimentIndex]['end']));
  $('#experimentLastStatus').text(experimentsData[experimentIndex]['last_status']);
  $('#experimentLastUpdated').text(displayDateTime(experimentsData[experimentIndex]['last_updated']));
  $('#experimentDescription').text(experimentsData[experimentIndex]['description']);

  //Show the duration for each experiment in "xx days xx hours xx minutes xx seconds" format
  if (experimentsData[experimentIndex]['last_status'] === 'SUCCESS' || experimentsData[experimentIndex]['last_status'] === 'ERROR' || experimentsData[experimentIndex]['last_status'] === 'CANCELLED' || experimentsData[experimentIndex]['last_status'] === 'IDLE') {
    var duration = stringToDate(displayDateTime(experimentsData[experimentIndex]['end'])) - stringToDate(displayDateTime(experimentsData[experimentIndex]['start']));
    $('#experimentDuration').text(msToTime(duration));
  }
  else {
    var currentTime = new Date();
    var formatedCurTime = formatDate(currentTime);
    var duration = stringToDate(formatedCurTime) - stringToDate(displayDateTime(experimentsData[experimentIndex]['start']));
    $('#experimentDuration').text(msToTime(duration));
  }

  //Showing the stop experiment button dynamically
  if (experimentsData[experimentIndex]['last_status'] !== 'SUCCESS' && experimentsData[experimentIndex]['last_status'] !== 'ERROR' && experimentsData[experimentIndex]['last_status'] !== 'CANCELLED') {

    $('#stopExperimentButton').html('<button class="btn btn-primary" onclick="alert(\'Call Stop Service for - ' + experimentsData[experimentIndex]['experiment_name'] + '\')">Stop Experiment</button>');
  } else {
    $('#stopExperimentButton').html('');
  }
  $('#detailsModal').modal('show');
}

setInterval(function () {
  loadPage();
}, 2000);