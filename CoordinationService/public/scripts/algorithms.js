//All algorithm data
var algorithmsData;

//Selected algorithm id
var selectedAlgorithmId;

//Initialization
$(document).ready(function () {
  loadPage();
  loadDropdowns();
});

function loadDropdowns() {
  //fulfill #language select
  $.get("algorithms/languages", function (data) {
    var languages = '';
    for (i = 0; i < data['languages'].length; i++) {
      languages += '<option>' + data['languages'][i]['language'] + '</option>\n';
    }
    $('#language').html(languages);
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
    $('#taskDatasetName').html(datasetsnames);
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

//Loads the page
function loadPage() {
  //Reset upload modal 
  resetModal('uploadModal');

  //Reset task modal
  resetModal('createTaskModal');

  //Activate the popover in the upload modal
  $('[data-toggle="popover"]').popover();

  //Showing the loading text
  $('#algorithmsTableBody').text('Loading...');

  //Load upload types
  $.get("algorithms/list", function (data) {
    algorithmsData = data['algorithms'];
    var algorithmsList = '';
    if (!algorithmsData || algorithmsData.length === 0) {
      $('#algorithmsTableBody').html('<h3>This list is empty!</h3>');
    } else {
      for (i = 0; i < data['algorithms'].length; i++) {
        algorithmsList += '<tr><td><span class="fa fa-file-code-o" aria-hidden="true"></span>&nbsp;&nbsp;' + data['algorithms'][i]['name'] + '</td><td>' + data['algorithms'][i]['description'] + '<br><span class="label label-success">' + data['algorithms'][i]['language'] + '</span></td><td><button type="button" onclick="displayDetails(' + i + ')" class="btn btn-primary">Details</button></td><td><button type="button" onclick="createTaskFromAlgorithm(' + i + ')" class="btn btn-primary">Create Task</button></td></tr>';
      }
      $('#algorithmsTableBody').html(algorithmsList);
    }
  });
}
//Checks if all required fields are filled correctly in the upload form
function checkAlgorithmUploadFormRequiredFields() {
  var fileName = $("#algorithm_file")[0].value;
  var algorithmName = $("#algorithm_name").val();
  var language = $("#language")[0];
  var selectedLanguage = language.options[language.selectedIndex].text;
  var alert = $('#formUploadError')[0];
  var alertText = $('#formUploadErrorText')[0];

  if (algorithmName.length == 0) {
    showSubmissionResult('Please input an algorithm name.', alert, alertText);
    return false;
  }
  else if (selectedLanguage.length == 0) {
    showSubmissionResult('Please select an algorithm language.', alert, alertText);
    return false;
  }
  else if (fileName.length == 0) {
    showSubmissionResult('Please choose an algorithm file.', alert, alertText);
    return false;
  }
  else {
    hideFormError(alert);
    return true;
  }
}

//Checks form fields for correctness
function checkAlgFileExtension() {
  var result;
  var fileName = $("#algorithm_file")[0].value;
  var extension = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length);
  var alert = $('#formUploadError')[0];
  var alertText = $('#formUploadErrorText')[0];
  if (fileName.length == 0) {
    showSubmissionResult('Please choose an algorithm file.', alert, alertText);
    result = false;
  } else {
    if (extension.toLowerCase() != "zip") {
      showSubmissionResult('The choosen algorithm file must be in ZIP format.', alert, alertText);
      result = false;
    }
    else {
      hideFormError(alert);
      result = true;
    }
  }
  return (result);
}

//Hide form error
function hideFormError(alert) {
  alert.style.display = "none";
}

//Handles the submit action
function submitForm() {
  var success = $('#formUploadSuccess')[0];
  var successText = $('#formUploadSuccessText')[0];
  var form = new FormData($('form#algorithmForm')[0]);
  if (checkAlgorithmUploadFormRequiredFields()) {
    //Sending post request
    showUploadingSpinner();
    var serviceIp = getServiceIp('AlgInput-' + $('#language option:selected').text());
    $.ajax({
      url: "http://" + serviceIp + "/services/algr/upload",
      type: "POST",
      dataType: 'json',
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
          displayAlertByType(resObject);
        } else {
          var alert = $('#formUploadError')[0];
          var alertText = $('#formUploadErrorText')[0];
          var networkErr = "Upload request failed, please check your network connection";
          showSubmissionResult(networkErr, alert, alertText);
        }
      },
      complete: function () {
        $("#uploadingAlgorithm")[0].style.display = "none";
        $(".descriptionAlgorithm").css('visibility', 'visible');
      }
    });
  }

}

function showUploadingSpinner() {
  $(".descriptionAlgorithm").css('visibility', 'hidden');
  $("#uploadingAlgorithm")[0].style.display = "block";
};

// response is object which contains code: int, msg: string, and success: boolean
function displayAlertByType(response) {
  var errorCode = response.code;
  var errorMsg = response.msg;
  var alert = $('#formUploadError')[0];
  var alertText = $('#formUploadErrorText')[0];
  showSubmissionResult(errorMsg, alert, alertText);
}

//Show submission result after clicking Submit button
function showSubmissionResult(message, alert, alertText) {
  alertText.innerText = message;
  alert.style.display = "block";
}

//Displays details for a selected algorithm
function displayDetails(algorithmIndex) {
  $('#algorithmName').text(algorithmsData[algorithmIndex]['name']);
  $('#algorithmDescription').text(algorithmsData[algorithmIndex]['description']);
  $('#algorithmLanguage').text(algorithmsData[algorithmIndex]['language']);
  $('#algorithmPath').html('<a href="http://' + location.host + '/download/algorithm/' + algorithmsData[algorithmIndex]['id'] + '" target="_blank">Download</a>');

  //Load algorithms types
  $.get("tasks/listByAlgorithm/" + algorithmsData[algorithmIndex]['id'], function (data) {
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

  $('#detailsModal').modal('show');
}

//Creates a task definition from an algorithm
function createTaskFromAlgorithm(index) {
  selectedAlgorithmId = index;
  $('#taskAlgorithmsNames').val(algorithmsData[index]['name']);
  $('#createTaskModal').modal('show');
}

//Submit the task form
function submitTaskForm() {
  var success = $('#formTaskSuccess')[0];
  var successText = $('#formTaskSuccessText')[0];
  var alert = $('#formTaskError')[0];
  var alertText = $('#formTaskErrorText')[0];
  var tasksInfoForTrain = {
    "dataset_id": $("#taskDatasetName").val(),
    "algorithm_id": algorithmsData[selectedAlgorithmId]['id'],
    "model_id": null,
    "name": $("#taskName").val(),
    "description": $("#taskDescription").val(),
    "type": $("#type")[0].options[$("#type")[0].selectedIndex].text
  }
  var tasksInfoForExe = {
    "dataset_id": $("#taskDatasetName").val(),
    "algorithm_id": algorithmsData[selectedAlgorithmId]['id'],
    "model_id": $("#modelsNames").val(),
    "name": $("#taskName").val(),
    "description": $("#taskDescription").val(),
    "type": $("#type")[0].options[$("#type")[0].selectedIndex].text
  }
  var objectToSend;
  var url;
  if (checkRequiredTaskFields()) {
    if (tasksInfoForTrain.type.toLowerCase() === "training") {
      objectToSend = JSON.stringify(tasksInfoForTrain);
      url = 'http://' + location.host + '/tasks/createTrainingTask';
    } else if (tasksInfoForExe.type.toLowerCase() === "execution") {
      objectToSend = JSON.stringify(tasksInfoForExe);
      url = 'http://' + location.host + '/tasks/createExecutionTask';
    }

    //Disabling submit button
    $('#submitTaskButton').prop("disabled", true);

    $.ajax({
      url: url,
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

function checkRequiredTaskFields() {
  var t = $("#type")[0];
  var taskName = $("#taskName").val();
  var d = $("#taskDatasetName")[0];
  var m = $("#modelsNames")[0];
  var selectedTaskType = t.options[t.selectedIndex].text;
  var selectedAlgorithmName = $("#taskAlgorithmsNames").val();
  var selectedDatasetName = d.options[d.selectedIndex] === undefined ? "" : d.options[d.selectedIndex].text;
  var selectedModelName = m.options[m.selectedIndex] === undefined ? "" : m.options[m.selectedIndex].text;
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
  else {
    return true;
  }
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