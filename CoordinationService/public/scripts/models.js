//All model data
var modelsData;

//Selected model id
var selectedModelId;

//Initialization
$(document).ready(function () {
  //Reset task modal
  resetModal('createTaskModal');

  //Showing the loading text
  $('#modelsTableBody').text('Loading...');

  //Load upload types
  $.get("models/list", function (data) {
    modelsData = data['models'];
    var modelsList = '';
    if (!modelsData || modelsData.length === 0) {
      $('#modelsTableBody').html('<h3>This list is empty!</h3>');
    } else {
      for (i = 0; i < data['models'].length; i++) {
        modelsList += '<tr><td><span class="fa fa-cog" aria-hidden="true"></span>&nbsp;&nbsp;' + data['models'][i]['name'] + '</td><td>' + data['models'][i]['description'] + '<br><span class="label label-success">' + data['models'][i]['path'] + '</span>&nbsp;&nbsp;<span class="label label-success">' + data['models'][i]['project_id'] + '</span>&nbsp;&nbsp;<span class="label label-success">' + data['models'][i]['experiment_id'] + '</span></td><td><button type="button" onclick="displayDetails(' + i + ')" class="btn btn-primary">Details</button></td><td><button type="button" onclick="createTaskFromModel(' + i + ')" class="btn btn-primary">Create Task</button></td></tr>';
      }
      $('#modelsTableBody').html(modelsList);
    }
  });

  //Load datasets names
  $.get("tasks/datasetsnames", function (data) {
    var datasetsnames = '';
    for (i = 0; i < data['datasetsnames'].length; i++) {
      datasetsnames += '<option value="' + data['datasetsnames'][i]['id'] + '">' + data['datasetsnames'][i]['name'] + '</option>\n';
    }
    $('#taskDatasetsNames').html(datasetsnames);
  });
});

//Displays details for a selected dataset
function displayDetails(modelIndex) {
  $('#modelName').text(modelsData[modelIndex]['name']);
  $('#modelDescription').text(modelsData[modelIndex]['description']);
  $('#modelPath').text(modelsData[modelIndex]['path']);

  //Load tasks types
  $.get("tasks/listByModel/" + modelsData[modelIndex]['id'], function (data) {
    if (typeof data != "undefined") {
      var tasksList = '';
      if (!data['tasks'] || data['tasks'].length === 0) {
        $('#tasksTableBody').text('This list is empty!');
      } else {
        for (i = 0; i < data['tasks'].length; i++) {
          tasksList += '<tr><td><span class="fa fa-check-square-o" aria-hidden="true"></span>&nbsp;&nbsp;' + data['tasks'][i]['name'] + '</td><td>' + data['tasks'][i]['description'] + '</td></tr>';
        }
        $('#tasksTableBody').html(tasksList);
      }
    }
  });
  // Uncomment the following two lines after the table field is filled.
  //$('#datasetProject').text(datasetsData[datasetId]['project_name']);
  //$('#datasetTask').text(datasetsData[datasetId]['task_name']);

  $('#detailsModal').modal('show');
}

//Creates a task definition from a model
function createTaskFromModel(index) {
  selectedModelId = index;
  $('#taskModel').val(modelsData[index]['name']);
  $('#createTaskModal').modal('show');
}

//Submit the task form
function submitTaskForm() {
  var success = $('#formTaskSuccess')[0];
  var successText = $('#formTaskSuccessText')[0];
  var alert = $('#formTaskError')[0];
  var alertText = $('#formTaskErrorText')[0];
  var tasksInfoForExecution = {
    "project_id": $("#taskProjectId").val(),
    "dataset_id": $("#taskDatasetName").val(),
    // TODO: Add algorithm id in the future if needed
    "algorithm_id": null,
    "model_id": $("#taskModel").val(),
    "name": $("#taskName").val(),
    "description": $("#taskDescription").val(),
    "type": $("#taskType").val()
  }

  var objectToSend;
  var url;
  if (checkRequiredTaskFields()) {
    //Disabling submit button
    $('#submitTaskButton').prop("disabled", true);

    objectToSend = tasksInfoForExecution;
    url = 'http://127.0.0.1:3000/tasks/createExecutionTask';
    $.ajax({
      url: url,
      type: "POST",
      dataType: "json",
      contentType: 'application/json',
      data: JSON.stringify(objectToSend),
      error: function (request, status, error) {
        showSubmissionResult("Oops! An error occurs when creating the task. Please check the error log in log path for possible reasons: " + status + error, alert, alertText);
      },
      success: function (data) {
        showSubmissionResult("Task: " + data.newtaskinfo.name + " has been created successfully!", success, successText);
      }
    });
  }
}

//Checks required fields
function checkRequiredTaskFields() {
  var taskName = $("#taskName").val();
  var projectId = $("#taskProjectId").val();
  var selectedTaskType = $("#taskType").val();
  var selectedModelName = $("#taskModel").val();
  var alert = $('#formTaskError')[0];
  var alertText = $('#formTaskErrorText')[0];

  if (selectedTaskType.length == 0) {
    showSubmissionResult('Please select a task type.', alert, alertText);
    return false;
  }
  else if (taskName.length == 0) {
    showSubmissionResult('Please input a task name.', alert, alertText);
    return false;
  }
  else if (projectId.length == 0) {
    showSubmissionResult('Please input the Project ID.', alert, alertText);
    return false;
  }
  else if (selectedModelName.length == 0) {
    showSubmissionResult('Please select an algorithm.', alert, alertText);
    return false;
  }
  else {
    return true;
  }
}

//Show submission result after clicking Submit button
function showSubmissionResult(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}