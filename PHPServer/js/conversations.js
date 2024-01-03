$(document).ready(function() {
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
		$('#dataSubjectEdit').text(subject);
		$('#modalEditConversationId').val(conversationId);
		$('#modalEditRelationId').val(relationId);
		$('#modalEditSubject').val(subject);
		$('#modalEdit').show();
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