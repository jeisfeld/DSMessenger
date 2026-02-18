function scrollToBottom() {
	const messages = document.getElementById('messages');
	messages.scrollTop = messages.scrollHeight;
}

function retryMessage() {
	$("#lastai").hide();
	$("#lastown").hide();
	$("#messageText").val($("#lastMessage").val());
	$("#lastOwnMessageId").val($("#lastown").data("messageid"));
	$("#lastAiMessageId").val($("#lastai").data("messageid"));
}

function recreatePreparedMessage() {
	$("#messageText").prop('disabled', true);
	$.ajax({
		url: 'recreatePreparedMessage.php',
		type: 'POST',
		data: {
			'relationId': $('#relationId').val(),
			'conversationId': $('#conversationId').val(),
			'isSlave': $('#isSlave').val()
		},
		success: function(response) {
			$("#messageText").val(response);
			$("#messageText").prop('disabled', false);
		},
		error: function(xhr, status, error) {
			$("#messageText").prop('disabled', false);
		}
	});
}


$(document).ready(function() {
	var formSubmitted = false;
	
	$('#conversations-link').click(function(event) {
		// Trigger AJAX request but don't prevent default behavior
		$.ajax({
			url: 'setPreparedMessage.php',
			type: 'POST',
			data: {
				'preparedMessage': $('#messageText').val(),
				'relationId': $('#relationId').val(),
				'conversationId': $('#conversationId').val(),
				'isSlave': $('#isSlave').val()
			},
			success: function(response) {
				// This function is called if the request is successful.
				// 'response' contains the data returned from the server.
				console.log(response);
			},
			error: function(xhr, status, error) {
				// This function is called in case of an error.
				console.error("Error occurred: " + error);
			}
		});
	});

	$('#editButton').click(function() {
		var conversationId = $(this).data('conversation-id');
		var relationId = $(this).data('relation-id');
		var subject = $(this).data('subject');
		var archived = $(this).data('archived');
		$('#modalEditConversationId').val(conversationId);
		$('#modalEditRelationId').val(relationId);
		$('#modalEditSubject').val(subject);
		$('#modalEditArchived').prop('checked', archived);
		$('#modalMoveToRelationId').val('');
		$('#modalMoveContainer').hide();
		$('#modalEdit').show();
	});

	$('#modalShowMoveButton').click(function() {
		$('#modalMoveContainer').toggle();
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
	
	scrollToBottom();
});

