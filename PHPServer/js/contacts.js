$(document).ready(function() {
	$('.editButton').click(function() {
		var contact = $(this).data('contact');
		var relationId = contact.relationId;
		var isSlave = contact.isSlave;
		var myName = contact.myName;
		var contactName = contact.contactName;
		var aiPolicy = contact.aiPolicy;
		var aiRelationId = contact.aiRelationId;
		var aiUsername = contact.aiUsername;
		var aiPrimingId = contact.aiPrimingId;
		var aiAddPrimingText = contact.aiAddPrimingText;
		if (isSlave) {
			$('.modalEditOnlyMaster').show();					
		}
		else {
			$('.modalEditOnlyMaster').hide();					
		}
		if (aiRelationId) {
			$('#modalEditGroupAi').show();					
		}
		else {
			$('#modalEditGroupAi').hide();					
		}
		$('#modalEditContactName').val(contactName);
		$('#modalEditRelationId').val(relationId);
		$('#modalEditIsSlave').val(isSlave ? 1 : 0);
		$('#modalEditAiRelationId').val(aiRelationId);
		$('#modalEditMyName').val(myName);
		$('#modalEditAiUsername').val(aiUsername);
		$('#modalEditAiPolicy').val(aiPolicy >=2 ? 3 : 1);
		$('#modalEditAiPrimingId').val(aiPrimingId);
		$('#modalEditAddPrimingText').val(aiAddPrimingText);
		$('#modalEdit').show();
	});

	$('#createButton').click(function() {
		$('#modalCreateContactName').val('');
		$('#modalCreateAiPolicy').val(3);
		$('#modalCreateAiPrimingId').val(3);
		$('#modalCreate').show();
	});

	// Close the modal on clicking 'x'
	$('#modalEdit .close').click(function() {
		$('#modalEdit').hide();
	});
	
	$('#modalCreate .close').click(function() {
		$('#modalCreate').hide();
	});	

});
