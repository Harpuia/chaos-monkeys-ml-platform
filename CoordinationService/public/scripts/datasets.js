//All dataset data
var datasetsData;

//Initialization
$(document).ready(function () {
  loadPage();
});

//Loads the page
function loadPage() {
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
    if (!datasetsData || datasetsData.length === 0) {
      $('#datasetsTableBody').html('<h3>This list is empty!</h3>');
    } else {
      for (i = 0; i < data['datasets'].length; i++) {
        datasetsList += '<tr><td><span class="fa fa-file-text-o" aria-hidden="true"></span>&nbsp;&nbsp;' + data['datasets'][i]['name'] + '</td><td>' + data['datasets'][i]['description'] + '<br><span class="label label-success">' + data['datasets'][i]['path'] + '</span>&nbsp;&nbsp;<span class="label label-success">' + data['datasets'][i]['format'] + '</span></td><td><button type="button" onclick="displayDetails(' + i + ')" class="btn btn-primary">Details</button></td><td><button type="button" onclick="createTaskFromDataset(' + i + ')" class="btn btn-primary">Create Task</button></td></tr>';
      }
      $('#datasetsTableBody').html(datasetsList);
    }
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
}

//Shows upload spinner
function showUploadingSpinner() {
  $(".description").css('visibility', 'hidden');
  $("#uploading")[0].style.display = "block";
}

//Checks if all required fields are filled correctly in the upload form
function checkUploadFormRequiredFields() {
  var fileName = $("#file")[0].value;
  var datasetName = $("#dataset_name").val();
  var userId = $("#user_id").val();
  var projectId = $("#project_id").val();
  var dataFormat = $("#format")[0];
  var selectedDataFormat = dataFormat.options[dataFormat.selectedIndex].text;
  var alert = $('#formUploadError')[0];
  var alertText = $('#formUploadErrorText')[0];

  if (fileName.length == 0) {
    showSubmissionResult('Please choose a file.', alert, alertText);
    return false;
  }
  else if (datasetName.length == 0) {
    showSubmissionResult('Please input a dataset name.', alert, alertText);
    return false;
  }
  else if (userId.length == 0) {
    showSubmissionResult('Please input the User ID.', alert, alertText);
    return false;
  }
  else if (projectId.length == 0) {
    showSubmissionResult('Please input the Project ID.', alert, alertText);
    return false;
  }
  else if (selectedDataFormat.length == 0) {
    showSubmissionResult('Please select a dataset format.', alert, alertText);
    return false;
  }
  else {
    hideFormError(alert);
    return true;
  }
}

//Checks form fields for correctness
function checkExtension() {
  var result;
  var e = $("#format")[0];
  var selectedValue = e.options[e.selectedIndex].text;
  var filename = $("#file")[0].value;
  var extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length);
  var alert = $('#formUploadError')[0];
  var alertText = $('#formUploadErrorText')[0];
  if (filename.length == 0) {
    showSubmissionResult('Select a file to upload.', alert, alertText);
    result = false;
  } else {
    if (selectedValue.toLowerCase() != extension.toLowerCase()) {
      showSubmissionResult('The selected data format doesn\'t match the input data format.', alert, alertText);
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
  var success = $('#formUploadSuccess')[0];
  var successText = $('#formUploadSuccessText')[0];
  var alert = $('#formUploadError')[0];
  var alertText = $('#formUploadErrorText')[0];
  var form = new FormData($('form#dataForm')[0]);
  //Sending post request
  if (checkUploadFormRequiredFields()) {
    showUploadingSpinner();
    $.ajax({
      url: "http://127.0.0.1:8080/services/datasets/upload",
      type: "POST",
      data: form,
      processData: false,
      contentType: false,
      success: function (data) {
        showSubmissionResult("Success!", success, successText);
        loadPage();
      },
      error: function (jqXHR, status, error) {
        if (jqXHR.responseJSON) {
          var resObject = jqXHR.responseJSON;
          displayAlertByType(resObject, alert, alertText);
        } else {
          var networkErr = "Upload request failed, please check your network connection or contact the server administrator";
          showSubmissionResult(networkErr, alert, alertText);
        }
      },
      complete: function () {
        document.getElementById("uploading").style.display = "none";
        $(".description").css('visibility', 'visible');
      }
    });
  }
}

//Show submission result after clicking Submit button
function showSubmissionResult(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}

// response is object which contains code: int, msg: string, and success: boolean
function displayAlertByType(response, alert, alertText) {
  var errorCode = response.code;
  var errorMsg = response.msg;
  showSubmissionResult(errorMsg, alert, alertText);
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
  var success = $('#formTaskSuccess')[0];
  var successText = $('#formTaskSuccessText')[0];
  var alert = $('#formTaskError')[0];
  var alertText = $('#formTaskErrorText')[0];
  var tasksInfoForTrain = {
    "project_id": $("#projectId").val(),
    "dataset_id": datasetsData[selectedDatasetId]['id'],
    "algorithm_id": $("#algorithmsNames").val(),
    "model_id": null,
    "name": $("#name").val(),
    "description": $("#taskDescription").val(),
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
    //Disabling submit button
    $('#submitTaskButton').prop("disabled", true);

    if (tasksInfoForTrain.type.toLowerCase() == "training") {
      objectToSend = tasksInfoForTrain;
      url = 'http://127.0.0.1:3000/tasks/createTrainingTask';
    }
    else if (tasksInfoForExe.type.toLowerCase() == "execution") {
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
        showSubmissionResult("Oops! An error occurs when creating the task. Please check the error log in log path for possible reasons: " + status + error, alert, alertText);
      },
      success: function (data) {
        showSubmissionResult("Task: " + data.newtaskinfo.name + " has been created successfully!", success, successText);
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
  else if (selectedTaskType.toLowerCase() == "training" && selectedAlgorithmName.length == 0) {
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


//Show submission result after clicking Submit button
function showSubmissionResult(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}
