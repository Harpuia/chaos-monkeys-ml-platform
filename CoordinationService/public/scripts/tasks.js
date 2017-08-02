//All task data
var tasksData;

//Initialization
$(document).ready(function () {
  loadPage();
});

//Page initialization method
function loadPage() {
  //Reset a task modal 
  resetModal('createTaskModal');
  
  //Reset an experiment modal 
  resetModal('createNewExperimentModal');

  //Showing the loading text
  $('#tasksTableBody').text('Loading...');

  //Load upload types
  $.get("tasks/list", function (data) {
    tasksData = data['tasks'];
    var tasksList = '';
    if (!tasksData || tasksData.length === 0) {
      $('#tasksTableBody').html('<h3>This list is empty!</h3>');
    } else {
      for (i = 0; i < data['tasks'].length; i++) {
        tasksList += '<tr><td><span class="fa fa-check-square-o" aria-hidden="true"></span>&nbsp;&nbsp;' + data['tasks'][i]['name'] + '</td><td>' + data['tasks'][i]['description'] + '<br><span class="label label-success">' + data['tasks'][i]['type'] + '</span>&nbsp;&nbsp;<span class="label label-success">' + data['tasks'][i]['algorithm_name'] + '</span></td><td><button type="button" onclick="displayDetails(' + i + ')" class="btn btn-primary">Details</button></td><td><button type="button" onclick="createNewExperimentFromTask(' + i + ')" class="btn btn-primary">Run an experiment</button></td></tr>';
      }
      $('#tasksTableBody').html(tasksList);
    }
  });

  //Load task types
  $.get("tasks/type", function (data) {
    var types = '';
    for (i = 0; i < data['types'].length; i++) {
      types += '<option>' + data['types'][i]['type'] + '</option>\n';
    }
    $('#type').html(types);
  });

  //Load datasets names
  $.get("tasks/datasetsnames", function (data) {
    var datasetsnames = '';
    for (i = 0; i < data['datasetsnames'].length; i++) {
      datasetsnames += '<option value="' + data['datasetsnames'][i]['id'] + '">' + data['datasetsnames'][i]['name'] + '</option>\n';
    }
    $('#datasetsNames').html(datasetsnames);
  });

  //Load algorithms names
  $.get("tasks/algorithmsnames", function (data) {
    var algorithmsnames = '';
    for (i = 0; i < data['algorithmsnames'].length; i++) {
      algorithmsnames += '<option value="' + data['algorithmsnames'][i]['id'] + '">' + data['algorithmsnames'][i]['name'] + '</option>\n';
    }
    $('#algorithmsNames').html(algorithmsnames);
  });

  //Load models names
  $.get("tasks/modelsnames", function (data) {
    var modelsnames = '';
    for (i = 0; i < data['modelsnames'].length; i++) {
      modelsnames += '<option value="' + data['modelsnames'][i]['id'] + '">' + data['modelsnames'][i]['name'] + '</option>\n';
    }
    $('#modelsNames').html(modelsnames);
  });
}

//Shows task type
function showTaskType() {
  var e = $("#type")[0];
  var selectedValue = e.options[e.selectedIndex].text;
  var modelDropdown = $("#models")[0];
  if (selectedValue.toLowerCase() == "training") {
    hideDropdown(modelDropdown);
  }
  else if (selectedValue.toLowerCase() == "execution") {
    showDropdown(modelDropdown);
  }
}

//Hide dropdown list and the associated label
function hideDropdown(dropdown) {
  dropdown.style.display = "none";
}

//Show dropdown list and the associated label
function showDropdown(dropdown) {
  dropdown.style.display = "block";
}

//Submit the task form
function submitTaskForm() {
  var success = $('#formSuccess')[0];
  var successText = $('#formSuccessText')[0];
  var alert = $('#formError')[0];
  var alertText = $('#formErrorText')[0];
  var tasksInfoForTrain = {
    "dataset_id": $("#datasetsNames").val(),
    "algorithm_id": $("#algorithmsNames").val(),
    "model_id": null,
    "name": $("#name").val(),
    "description": $("#description").val(),
    "type": $("#type")[0].options[$("#type")[0].selectedIndex].text
  }
  var tasksInfoForExe = {
    "dataset_id": $("#datasetsNames").val(),
    "algorithm_id": null,
    "model_id": $("#modelsNames").val(),
    "name": $("#name").val(),
    "description": $("#description").val(),
    "type": $("#type")[0].options[$("#type")[0].selectedIndex].text
  }
  var objectToSend;
  var serviceUrl;
  if (checkRequiredFields()) {
    //Choosing object to send and url
    if (tasksInfoForTrain.type.toLowerCase() === "training") {
      objectToSend = JSON.stringify(tasksInfoForTrain);
      serviceUrl = 'http://127.0.0.1:3000/tasks/createTrainingTask';
    }
    else if (tasksInfoForExe.type.toLowerCase() === "execution") {
      objectToSend = JSON.stringify(tasksInfoForExe);
      serviceUrl = 'http://127.0.0.1:3000/tasks/createExecutionTask';
    }

    //Disabling submit button
    $('#submitTaskButton').prop("disabled", true);

    //Sending request
    $.ajax({
      url: serviceUrl,
      type: "POST",
      dataType: "json",
      contentType: 'application/json',
      data: objectToSend,
      error: function (request, status, error) {
        showSubmissionResult("Oops! An error occurs when creating the task. Please check the error log in log path for possible reasons: " + status + error, alert, alertText);
        
        //Enabling submit button
        $('#submitTaskButton').prop("disabled", false);
      },
      success: function (data) {
        showSubmissionResult("Task: " + data.newtaskinfo.name + " has been created successfully!", success, successText);
        loadPage();
      }
    });
  }
}

//Checks if all required fields are filled correctly
function checkRequiredFields() {
  var t = $("#type")[0];
  var taskName = $("#name").val();
  var d = $("#datasetsNames")[0];
  var a = $("#algorithmsNames")[0];
  var m = $("#modelsNames")[0];
  var selectedTaskType = t.options[t.selectedIndex].text;
  var selectedDatasetName = d.options[d.selectedIndex] ===undefined ?"":d.options[d.selectedIndex].text;
  var selectedAlgorithmName = a.options[a.selectedIndex]===undefined ? "": a.options[a.selectedIndex].text;
  var selectedModelName = m.options[m.selectedIndex]===undefined ? "": m.options[m.selectedIndex].text;
  var alert = $('#formError')[0];
  var alertText = $('#formErrorText')[0];
  
  if (selectedTaskType.length == 0) {
    showSubmissionResult('Please select a task type.', alert, alertText);
    return false;
  }
  else if (taskName.length == 0) {
    showSubmissionResult('Please input a task name.', alert, alertText);
    return false;
  }
  else if (selectedDatasetName.length == 0) {
    showSubmissionResult('Please select a dataset.', alert, alertText);
    return false;
  }
  else if (selectedAlgorithmName.length == 0) {
    showSubmissionResult('Please select an algorithm.', alert, alertText);
    return false;
  }
  else if (selectedTaskType.toLowerCase() == "execution" && selectedModelName.length == 0) {
    showSubmissionResult('Please select a model.', alert, alertText);
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

//Displays details for a selected task
function displayDetails(taskIndex) {
  $('#taskName').text(tasksData[taskIndex]['name']);
  $('#taskDescription').text(tasksData[taskIndex]['description']);
  $('#taskType').text(tasksData[taskIndex]['type']);
  $('#taskAlgorithm').text(tasksData[taskIndex]['algorithm_name']);
  $('#taskDataset').text(tasksData[taskIndex]['dataset_name']);
  $('#detailsModal').modal('show');;
}

var selectedTaskId;

//Creates an experiment from the task
function createNewExperimentFromTask(index) {
  selectedTaskId=tasksData[index]['id'];
  $('#experimentTaskName').val(tasksData[index]['name']);
  $('#createNewExperimentModal').modal('show');
}

/*Check if the required field are filled */
function checkExperimentRequiredFields() {
  var experimentName = $("#experimentName").val();
  var taskName = $("#experimentTaskName").val();
  var alert = $('#formExperimentError')[0];
  var alertText = $('#formExperimentErrorText')[0];
  if (experimentName.length == 0) {
    showSubmissionResult('Please input an experiment name.', alert, alertText);
    return false;
  }
  else if (taskName.length == 0) {
    showSubmissionResult('Please select a task.', alert, alertText);
    return false;
  }
  else
    return true;

}

/*Submit the form to create a new experiment */
function submitExperimentForm() {
  var success = $('#formExperimentSuccess')[0];
  var successText = $('#formExperimentSuccessText')[0];
  var alert = $('#formExperimentError')[0];
  var alertText = $('#formExperimentErrorText')[0];
  var experimentInfo = {
    "task_id": selectedTaskId,
    "experiment_name": $("#experimentName").val(),
    "start": null,
    "end": null,
    "last_status": "IDLE",
    "last_updated": null,
    "description": $("#experimentDescription").val()
  }

  if (checkExperimentRequiredFields()) {
    $.ajax({
      url: "http://127.0.0.1:3000/experiments/create",
      type: "POST",
      dataType: "json",
      contentType: 'application/json',
      data: JSON.stringify(experimentInfo),
      success: function (data) {
        showSubmissionResult("The experiment " + experimentInfo.experiment_name + " has been created successfully.", success, successText);
        loadPage();
      },
      error: function (request, status, error) {
        showSubmissionResult("Oops! An error occurs when creating the task. Please check the error log in log path for possible reasons: " + status + error, alert, alertText);
      }
    });
  }
}