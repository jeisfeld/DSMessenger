<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/usermanagement/querycontacts.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("Contacts") ?></title>
<link rel="stylesheet" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<script src="js/jquery-3.6.2.min.js"></script>
<script src="js/contacts.js"></script>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
	<div id="contacts-container">
		<div id="header">
			<span class="left"><?= _("Username") ?>: <?= $username ?></span>
			<span class="right"><a href="change_password.php"><?= _("Change password") ?></a>&nbsp;<a href="logout.php"><?= _("Logout") ?></a></span>
		</div>
		<h1><?= _("Contacts") ?>
			<span class="right"><svg id="button-reload" onclick="location.reload()" class="icon"><use href="images/icons.svg#icon-reload"></use></svg></span>
		</h1>
		<ul id="contact-list">
            <?php
            // Assuming you have a function getContacts() that returns an array of contacts
            $contacts = queryContacts($username, $password, true);
            $primings = queryPrimings($username);
            foreach ($contacts as $contact) {
                $relationId = $contact['relationId'];
                $contactName = $contact['contactName'];
                $isSlave = $contact['isSlave'];
                $lasttimestamp = @$contact['lasttimestamp'];
                echo '<li class="contact-item"><a href="conversations.php?relationId=' . $relationId . '">' . 
                    ($isSlave ? '' : '<b>') . $contactName . ($isSlave ? '' : '</b>') . '</a><div class="icons"><span class="time">'.convertTimestamp($lasttimestamp).'</span>
                <svg class="icon editButton" data-contact="' . htmlspecialchars(json_encode($contact), ENT_QUOTES, 'UTF-8') . '"><use href="images/icons.svg#icon-edit"></use></svg></div></li>';
            }
            ?>
            <li class="contact-add-on"><svg class="icon" id="createButton"><use href="images/icons.svg#icon-circle-plus"></use></svg></li>
        </ul>
		<div id="footer">
			<span class="right"><a href="impressum.html" target="_blank"><?= _("Imprint") ?></a></span>
		</div>
	</div>
	
	<div id="modalEdit" class="modal">
		<div class="modal-content">
			<span class="close">&times;</span> 
			<h2><?= _("Edit Contact") ?></h2>
			<form action="perform_edit_contact.php" method="post">
				<input type="hidden" name="relationId" id="modalEditRelationId" value="">
				<input type="hidden" name="isSlave" id="modalEditIsSlave" value="">
				<input type="hidden" name="aiRelationId" id="modalEditAiRelationId" value="">
				<div class="form-group modalEditOnlyMaster">
    				<label for="modalEditMyName"><?= _("My Name") ?>:</label>
    				<input type="text" name="modalEditMyName" id="modalEditMyName" maxlength="30" value="">
				</div>
				<div class="form-group">
    				<label for="modalEditContactName"><?= _("Contact Name") ?>:</label>
    				<input type="text" name="modalEditContactName" id="modalEditContactName" maxlength="30" value="">
				</div>
				<div id="modalEditGroupAi">
    				<div class="form-group">
        				<label for="modalEditAiUsername"><?= _("My Name for AI") ?>:</label>
        				<input type="text" name="modalEditAiUsername" id="modalEditAiUsername" maxlength="30" value=""/>
    				</div>
    				<div class="form-group">
        				<label for="modalEditAiPolicy"><?= _("AI Behavior") ?>:</label>
	    				<select id="modalEditAiPolicy" name="modalEditAiPolicy">
	    					<option value="0"><?= _("none") ?></option>
	    					<option value="3" selected><?= _("automatic") ?></option>
	    					<option value="2"><?= _("automatic live") ?></option>
	    					<option value="1"><?= _("manual") ?></option>
	    					<option value="4"><?= _("manual live") ?></option>
	    				</select>
    				</div>
       				<div class="form-group">
        				<label for="modalEditAiPrimingId"><?= _("AI Type") ?>:</label>
	    				<select id="modalEditAiPrimingId" name="modalEditAiPrimingId">
	    				<?php 
	    				foreach ($primings as $priming) {
	    				    echo '<option value="' . htmlspecialchars($priming['id']) . '"' . ($priming['id'] == 3 ? ' selected' : '') .
	    				    '>' . htmlspecialchars($priming['name']) . '</option>';
	    				}
	    				?>
	    				</select>
    				</div>
    				<div class="form-group modalEditOnlyMaster">
        				<label for="modalEditAddPrimingText"><?= _("AI Additional Text") ?>:</label>
        				<textarea name="modalEditAddPrimingText" id="modalEditAddPrimingText" maxlength="4096"></textarea>
    				</div>
    				<div class="form-group">
        				<label for="modalEditMessageSuffix"><?= _("Message Suffix") ?>:</label>
        				<textarea name="modalEditMessageSuffix" id="modalEditMessageSuffix" maxlength="1024"></textarea>
    				</div>
    			</div>
				<div class="container">
					<span class="left"><input type="button" name="cancel" class="modal-button" value="<?= _("Cancel") ?>" onclick="$('#modalEdit').hide();"></span>
					<span class="right"> <input type="submit" name="submit" class="modal-button" value="<?= _("Save Contact") ?>"></span>
				</div>
			</form>
		</div>
	</div>

	<div id="modalCreate" class="modal">
		<div class="modal-content">
			<span class="close">&times;</span> 
			<h2><?= _("Create Contact") ?></h2>
			<form action="perform_create_contact.php" method="post">
				<div class="form-group">
    				<label for="modalCreateMyName"><?= _("My Name") ?>:</label>
    				<input type="text" name="modalCreateMyName" id="modalCreateMyName" maxlength="30" value="<?= $username ?>">
				</div>
				<div class="form-group">
    				<label for="modalCreateContactName"><?= _("Contact Name") ?>:</label>
    				<input type="text" name="modalCreateContactName" id="modalCreateContactName" maxlength="30" value="">
				</div>
				<div id="modalCreateGroupAi">
    				<div class="form-group">
        				<label for="modalCreateAiPolicy"><?= _("AI Behavior") ?>:</label>
	    				<select id="modalCreateAiPolicy" name="modalCreateAiPolicy">
	    					<option value="0"><?= _("none") ?></option>
	    					<option value="3" selected><?= _("automatic") ?></option>
	    					<option value="2"><?= _("automatic live") ?></option>
	    					<option value="1"><?= _("manual") ?></option>
	    					<option value="4"><?= _("manual live") ?></option>
	    				</select>
    				</div>
       				<div class="form-group">
        				<label for="modalCreateAiPrimingId"><?= _("AI Type") ?>:</label>
	    				<select id="modalCreateAiPrimingId" name="modalCreateAiPrimingId">
	    				<?php 
	    				foreach ($primings as $priming) {
	    				    echo '<option value="' . htmlspecialchars($priming['id']) . '"' . ($priming['id'] == 3 ? ' selected' : '') .
	    				    '>' . htmlspecialchars($priming['name']) . '</option>';
	    				}
	    				?>
	    				</select>
    				</div>
   				</div>
				<div class="container">
					<span class="left"><input type="button" name="cancel" class="modal-button" value="<?= _("Cancel") ?>" onclick="$('#modalCreate').hide();"></span>
					<span class="right"> <input type="submit" name="submit" class="modal-button" value="<?= _("Save Contact") ?>"></span>
				</div>
			</form>
		</div>
	</div>
</body>
</html>