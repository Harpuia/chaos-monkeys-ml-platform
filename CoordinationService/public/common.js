
function resetModal(modalId){
    $('#'+modalId).on('hidden.bs.modal', function () {
        $('form').get(0).reset();
        $('#formError')[0].style.display = "none";
        $('#formSuccess')[0].style.display = "none";
    });
}
