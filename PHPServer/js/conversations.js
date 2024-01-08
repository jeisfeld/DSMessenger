$(document).ready(function() {
	$('#buttonSubmitMessage').click(function() {
        $(this).prop('disabled', true);
        $(this).css('color', 'transparent');
        $('#formSubmitMessage').submit();
    });
	
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

	// Close the modal if user clicks outside of it
	$(window).click(function(event) {
		if ($(event.target).is('#modalDelete')) {
			$('#modalDelete').hide();
		}
		if ($(event.target).is('#modalEdit')) {
			$('#modalEdit').hide();
		}
	});
});


