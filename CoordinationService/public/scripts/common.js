/* Reset the datasets/algorithms upload and tasks/experiments creation modals. */
function resetModal(modalId) {
    $('#' + modalId).on('hidden.bs.modal', function () {
        for (i = 0; i < $('form').length; i++)
            $('form').get(i).reset();
        var formerror = $('#formError')[0];
        var formsuccess = $('#formSuccess')[0];
        var formuploaderror = $('#formUploadError')[0];
        var formuploadsuccess = $('#formUploadSuccess')[0];
        var formtasksuccess = $('#formTaskSuccess')[0];
        var formtaskerror = $('#formTaskError')[0];
        var formexperimenterror = $('#formExperimentError')[0];
        var formexperimentsuccess = $('#formExperimentSuccess')[0];
        if (typeof formerror != 'undefined')
            formerror.style.display = "none";
        if (typeof formsuccess != 'undefined')
            formsuccess.style.display = "none";
        if (typeof formuploaderror != 'undefined')
            formuploaderror.style.display = "none";
        if (typeof formuploadsuccess != 'undefined')
            formuploadsuccess.style.display = "none";
        if (typeof formtaskerror != 'undefined')
            formtaskerror.style.display = "none";
        if (typeof formtasksuccess != 'undefined')
            formtasksuccess.style.display = "none";
        if (typeof formexperimenterror != 'undefined')
            formexperimenterror.style.display = "none";
        if (typeof formexperimentsuccess != 'undefined')
            formexperimentsuccess.style.display = "none";
    });
    $('#' + modalId).on('shown.bs.modal', function () {
        enableDisabledButtons(modalId);
    });
}

//Formats MYSQL Datetime for display
function displayDateTime(time) {
    if (time)
        return time.replace('T', ' ').replace('.000Z', '');
    else
        return time;
}

//Enables disabled buttons under a div
function enableDisabledButtons(divId) {
    $('#' + divId + ' :button').attr('disabled', false);
}

//Parse a String format date into a Date format
function stringToDate(s) {
    if (s)
        return new Date(Date.parse(s.replace('-', '/', 'g')));
    else
        return s;
}

//Transform milliseconds to HH:MM:SS format 
function msToTime(duration) {
    var seconds = parseInt((duration / 1000) % 60)
        , minutes = parseInt((duration / (1000 * 60)) % 60)
        , hours = parseInt((duration / (1000 * 60 * 60)) % 24)
        , days = parseInt((duration / (1000 * 60 * 60 * 24)));

    days = (days < 10) ? "0" + days : days;
    hours = (hours < 10) ? "0" + hours : hours;
    minutes = (minutes < 10) ? "0" + minutes : minutes;
    seconds = (seconds < 10) ? "0" + seconds : seconds;

    return days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds";
}

//Transform a javascript date format to YYYY-MM-DD HH:MM:SS format 
function formatDate(datetime) {
    var year = datetime.getFullYear();
    var month = datetime.getMonth() + 1;
    var day = datetime.getDate();
    var hour = datetime.getHours();
    var minute = datetime.getMinutes();
    var second = datetime.getSeconds();
    return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
}

//Dynamically loads the IP of a service
serviceIp = '';
function getServiceIp(serviceType, callback) {
    var coordinationUrl = 'http://' + location.host + '/dynamicIp/get/' + serviceType;
    $.getJSON(coordinationUrl, function (data) {
        if (data['message'] === 'success')
            callback(data['ip']);
    });
}