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
			'preparedMessage': $('#message').val(),
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

$(document).ready(function() {
	$('#buttonSubmitMessage').click(function() {
        $(this).prop('disabled', true);
        $(this).css('color', 'transparent');
        $('#formSubmitMessage').submit();
    });
});

