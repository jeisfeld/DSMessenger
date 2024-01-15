$(document).ready(function() {
	var formSubmitted = false;

	// Open the modal and set data
	$('.deleteButton').click(function() {
		var conversationId = $(this).data('conversation-id');
		var relationId = $(this).data('relation-id');
		var subject = $(this).data('subject');
		$('#dataSubjectDelete').text(subject);
		$('#modalDeleteConversationId').val(conversationId);
		$('#modalDeleteRelationId').val(relationId);
		$('#modalDelete').show();
	});
	$('.editButton').click(function() {
		var conversationId = $(this).data('conversation-id');
		var relationId = $(this).data('relation-id');
		var subject = $(this).data('subject');
		var archived = $(this).data('archived');
		$('#modalEditConversationId').val(conversationId);
		$('#modalEditRelationId').val(relationId);
		$('#modalEditSubject').val(subject);
		$('#modalEditArchived').prop('checked', archived);
		$('#modalEdit').show();
	});

    $('#button-archive').click(function() {
        $('#button-archive').toggleClass('active');
        $('.conversation-item.archived').toggleClass('hidden');
    });

	// Close the modal on clicking 'x'
	$('#modalDelete .close').click(function() {
		$('#modalDelete').hide();
	});
	$('#modalEdit .close').click(function() {
		$('#modalEdit').hide();
	});
	
			
	$('#buttonSubmitMessage').click(function() {
		if (formSubmitted) {
			return false;
		}
		formSubmitted = true;
        $(this).prop('disabled', true);
        $('#formSubmitMessage').submit();
    });
	
	// submit via Ctrl-Return
	$('#messageText').keydown(function(event) {
        if (event.ctrlKey && event.which === 13) { // 13 is the Enter key
			if (formSubmitted) {
				return false;
			}
			formSubmitted = true;
			$('#buttonSubmitMessage').prop('disabled', true);
			$('#formSubmitMessage').submit();
        }
    });

});


