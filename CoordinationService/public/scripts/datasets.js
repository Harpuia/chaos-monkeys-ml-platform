//All dataset data
var datasetsData;

//Initialization
$(document).ready(function () {
  //Reset a specific modal
  resetModal('uploadModal');

  //Reset the create task modal
  resetModal('createTaskModal');

  //Showing the loading text
  $('#datasetsTableBody').text('Loading...');

  //Activate the popover in the upload modal
  $('[data-toggle="popover"]').popover();

  //Load datasets list
  $.get("datasets/list", function (data) {
    datasetsData = data['datasets'];
    var datasetsList = '';
    for (i = 0; i < data['datasets'].length; i++) {
      datasetsList += '<tr><td><span class="fa fa-file-text-o" aria-hidden="true"></span>&nbsp;&nbsp;' + data['datasets'][i]['name'] + '</td><td>' + data['datasets'][i]['description'] + '<br><span class="label label-success">' + data['datasets'][i]['path'] + '</span>&nbsp;&nbsp;<span class="label label-success">' + data['datasets'][i]['format'] + '</span></td><td><button type="button" onclick="displayDetails(' + i + ')" class="btn btn-primary">Details</button></td><td><button type="button" onclick="createTaskFromDataset(' + i + ')" class="btn btn-primary">Create Task</button></td></tr>';
    }
    $('#datasetsTableBody').html(datasetsList);
  });

  //Load upload types
  $.get("datasets/formats", function (data) {
    var formats = '';
    for (i = 0; i < data['formats'].length; i++) {
      formats += '<option>' + data['formats'][i]['format'] + '</option>\n';
    }
    $('#format').html(formats);
  });

  //Load task types
  $.get("tasks/type", function (data) {
    var types = '';
    for (i = 0; i < data['types'].length; i++) {
      types += '<option>' + data['types'][i]['type'] + '</option>\n';
    }
    $('#type').html(types);
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
});

//Shows upload spinner
function showUploadingSpinner() {
  $(".description").css('visibility', 'hidden');
  $("#uploading")[0].style.display = "block";
}

//Checks form fields for correctness
function checkExtension() {
  var result;
  var e = $("#format")[0];
  var selectedValue = e.options[e.selectedIndex].text;
  var filename = $("#file")[0].value;
  var extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length);
  var alert = $('#formAlert')[0];
  var alertText = $('#formAlertText')[0];
  if (filename.length == 0) {
    showFormError('Select a file to upload.', alert, alertText);
    result = false;
  } else {
    if (selectedValue.toLowerCase() != extension.toLowerCase()) {
      showFormError('The selected data format doesn\'t match the input data format.', alert, alertText);
      result = false;
    }
    else {
      hideFormError(alert);
      result = true;
    }
  }
  return (result);
}

//Handles the submit action
function submitForm() {
  var form = new FormData($('form#dataForm')[0]);
  //Sending post request
  if (checkExtension()) {
    showUploadingSpinner();
    $.ajax({
      url: "http://127.0.0.1:8080/services/upload",
      type: "POST",
      dataType: 'text',
      data: form,
      processData: false,
      contentType: false,
      success: function (data) {
        console.log("Submitted");
        console.log('Data: ' + data);
        alert("Success!");
      },
      error: function (request, status, error) {
        console.log(status + error);
        alert("Oops! An error occurs when uploading the data. Please check the error log in log path for possible reasons: " + status + error);
      },
      complete: function () {
        document.getElementById("uploading").style.display = "none";
        $(".description").css('visibility', 'visible');
      }
    });
  }
}

//Show error
function showFormError(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}

//Hide form error
function hideFormError(alert) {
  alert.style.display = "none";
}

//Displays details for a selected dataset
function displayDetails(datasetIndex) {
  $('#datasetName').text(datasetsData[datasetIndex]['name']);
  $('#datasetDescription').text(datasetsData[datasetIndex]['description']);
  $('#datasetFormat').text(datasetsData[datasetIndex]['format']);
  $('#datasetPath').text(datasetsData[datasetIndex]['path']);

  //Load tasks types
  $.get("tasks/listByDataset/" + datasetsData[datasetIndex]['id'], function (data) {
    if (typeof data != "undefined") {
      var tasksList = '';
      for (i = 0; i < data['tasks'].length; i++) {
        tasksList += '<tr><td><span class="fa fa-check-square-o" aria-hidden="true"></span>&nbsp;&nbsp;' + data['tasks'][i]['name'] + '</td><td>' + data['tasks'][i]['description'] + '</td></tr>';
      }
      $('#tasksTableBody').html(tasksList);
    }
  });
  // Uncomment the following two lines after the table field is filled.
  //$('#datasetProject').text(datasetsData[datasetIndex]['project_name']);
  //$('#datasetTask').text(datasetsData[datasetIndex]['task_name']);

  $('#detailsModal').modal('show');
}

//Selected Dataset ID (when creating task from Dataset)
var selectedDatasetId;

//Creates a task definition from a dataset
function createTaskFromDataset(index) {
  selectedDatasetId = index;
  $('#taskDatasetName').val(datasetsData[index]['name']);
  $('#createTaskModal').modal('show');
}

//Submit the task form
function submitTaskForm() {
  var tasksInfoForTrain = {
    "project_id": $("#projectId").val(),
    "dataset_id": selectedDatasetId,
    "algorithm_id": $("#algorithmsNames").val(),
    "model_id": null,
    "name": $("#name").val(),
    "description": $("#description").val(),
    "type": $("#type")[0].options[$("#type")[0].selectedIndex].text
  }
  var tasksInfoForExe = {
    "project_id": $("#projectId").val(),
    "dataset_id": selectedDatasetId,
    "algorithm_id": null,
    "model_id": $("#modelsNames").val(),
    "name": $("#name").val(),
    "description": $("#description").val(),
    "type": $("#type")[0].options[$("#type")[0].selectedIndex].text
  }
  var objectToSend;
  var url;
  if (checkRequiredTaskFields()) {
    if (tasksInfoForTrain.type.toLowerCase() == "training") {
      console.log(tasksInfoForExe);
      objectToSend = tasksInfoForTrain;
      url = 'http://127.0.0.1:3000/tasks/createTrainingTask';
    }
    else if (tasksInfoForExe.type.toLowerCase() == "execution") {
      console.log(tasksInfoForExe);
      objectToSend = tasksInfoForTrain;
      url = 'http://127.0.0.1:3000/tasks/createExecutionTask';
    }
    $.ajax({
      url: url,
      type: "POST",
      dataType: "json",
      contentType: 'application/json',
      data: JSON.stringify(objectToSend),
      error: function (request, status, error) {
        console.log(status + error);
        alert("Oops! An error occurs when creating the task. Please check the error log in log path for possible reasons: " + status + error);
      },
      success: function (data) {
        alert("Task: " + data.newtaskinfo.name + " was created successfully!");
      }
    });
  }
}

function checkRequiredTaskFields() {
  var t = $("#type")[0];
  var taskName = $("#name").val();
  var projectId = $("#projectId").val();
  var a = $("#algorithmsNames")[0];
  var m = $("#modelsNames")[0];
  var selectedTaskType = t.options[t.selectedIndex].text;
  var selectedAlgorithmName = a.options[a.selectedIndex].text;
  var selectedModelName = m.options[m.selectedIndex].text;
  var alert = $('#formAlert')[0];
  var alertText = $('#formAlertText')[0];
  //console.log(taskName);
  if (selectedTaskType.length == 0) {
    showFormError('Please select a task type.', alert, alertText);
    return false;
  }
  else if (taskName.length == 0) {
    showFormError('Please input a task name.', alert, alertText);
    return false;
  }
  else if (projectId.length == 0) {
    showFormError('Please input the Project ID.', alert, alertText);
    return false;
  }
  else if (selectedTaskType.toLowerCase() == "training" && selectedAlgorithmName.length == 0) {
    showFormError('Please select an algorithm.', alert, alertText);
    return false;
  }
  else if (selectedTaskType.toLowerCase() == "execution" && selectedModelName.length == 0) {
    showFormError('Please select a model.', alert, alertText);
    return false;
  }
  else
    return true;
}

//Shows task type
function showTaskType() {
  var e = $("#type")[0];
  var selectedValue = e.options[e.selectedIndex].text;
  var modelDropdown = $("#models")[0];
  var algorithmDropdown = $("#algorithms")[0];
  if (selectedValue.toLowerCase() == "training") {
    hideDropdown(modelDropdown);
    showDropdown(algorithmDropdown);
  }
  else if (selectedValue.toLowerCase() == "execution") {
    hideDropdown(algorithmDropdown);
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