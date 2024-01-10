function scrollToBottom() {
	const messages = document.getElementById('messages');
	messages.scrollTop = messages.scrollHeight;
}

window.onload = scrollToBottom;

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


$('#editButton').click(function() {
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

$('#modalEdit .close').click(function() {
	$('#modalEdit').hide();
});

$(window).click(function(event) {
	if ($(event.target).is('#modalEdit')) {
		$('#modalEdit').hide();
	}
});

$(document).ready(function() {
	$('#buttonSubmitMessage').click(function() {
		$(this).prop('disabled', true);
		$(this).css('color', 'transparent');
		$('#formSubmitMessage').submit();
	});
});

