
function resetModal(modalId){
    $('#'+modalId).on('hidden.bs.modal', function () {
        $('form').get(0).reset();
        $('#formAlert')[0].style.display = "none";
    });
}
