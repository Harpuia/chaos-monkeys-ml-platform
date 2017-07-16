/* Reset the datasets/algorithms upload and tasks/experiments creation modals. */
function resetModal(modalId) {
    $('#' + modalId).on('hidden.bs.modal', function () {
        $('form').get(0).reset();
        var formerror = $('#formError')[0];
        var formsuccess = $('#formSuccess')[0];
        var formuploaderror = $('#formUploadError')[0];
        var formuploadsuccess = $('#formUploadSuccess')[0];
        var formtasksuccess = $('#formTaskSuccess')[0];
        var formtaskerror = $('#formTaskError')[0];
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
    });
}

//Formats time for display
function displayDateTime(time) {
    if (time)
        return time.replace('T', ' ').replace('.000Z', '');
    else
        return time;
}