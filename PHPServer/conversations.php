<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/dbfunctions.php';
$username = $_SESSION['username'];
$userId = $_SESSION['userId'];
$relationId = $_GET['relationId'];

if (! $relationId) {
    header("Location: contacts.php");
}

// Retrieve data for current contact

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$relationData = getRelationData($userId, $relationId);
$contactName = $relationData['contactName'];

// Retrieve data for conversations

$conversationId = null;
$subject = null;
$flags = null;
$lasttimestamp = null;
$preparedMessage = null;
$stmt = $conn->prepare("SELECT id, subject, flags, lasttimestamp, prepared_message from dsm_conversation WHERE relation_id = ? order by lasttimestamp desc");

$stmt->bind_param("s", $relationId);
$stmt->execute();
$stmt->bind_result($conversationId, $subject, $flags, $lasttimestamp, $preparedMessage);

$conversations = array();
while ($stmt->fetch()) {
    $conversations[] = [
        'conversationId' => $conversationId,
        'subject' => $subject,
        'flags' => $flags,
        'lasttimestamp' => $lasttimestamp,
        'preparedMessage' => $preparedMessage
    ];
}
$stmt -> close();
$conn -> close();

?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= sprintf(_("conversations_with"), $contactName) ?></title>
<link rel="stylesheet" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="js/jquery-3.6.2.min.js"></script>
<script src="js/conversations.js"></script>
</head>
<body>
	<div id="conversations-container">
		<div id="header">
			<span class="left"><?= _("username") ?>: <?= $username ?></span> <span class="right"> <a href="contacts.php"><?= _("contacts") ?></a>&nbsp;<a
				href="logout.php"><?= _("logout") ?></a></span>
		</div>
		<h1><?= sprintf(_("conversations_with"), $contactName) ?>
			<span class="right"><svg id="button-reload" onclick="location.reload()" class="icon"><use xlink:href="images/icons.svg#icon-reload"></use></svg></span>
		</h1>
		<ul id="conversation-list">
            <?php
            // Retrieve conversations            
            foreach ($conversations as $conversation) {
                $conversationId = $conversation['conversationId'];
                $subject = $conversation['subject'];
                echo "<li class='conversation-item'><a href='messages.php?relationId=" . $relationId . "&conversationId=" . $conversationId . "'>" . $subject . '</a><div class="icons">
                <svg class="icon editButton" data-conversation-id="' . $conversationId . '" data-relation-id="' . $relationId . '" data-subject="' . $subject . '"><use xlink:href="images/icons.svg#icon-edit"></use></svg>
                <svg class="icon deleteButton" data-conversation-id="' . $conversationId . '" data-relation-id="' . $relationId . '" data-subject="' . $subject . '"><use xlink:href="images/icons.svg#icon-delete"></use></svg></div></li>';
            }
            ?>
            
        </ul>
		<div id="message-input">
			<form action="sendmessage.php" method="post" class="message-form" id="formSubmitMessage">
				<input type="hidden" name="relationId" value="<?= $relationId ?>">
				<input type="hidden" name="conversationId" value="">
				<input type="hidden" name="subject" value="">
				<textarea autofocus name="message" maxlength="40000" placeholder="<?= _("start_new_conversation") ?>" class="message-textarea"></textarea>
				<button type="submit" class="send-button" id="buttonSubmitMessage"><?= _("send") ?></button>
			</form>
		</div>
		<div id="footer">
			<span class="left"></span> <span class="right"><a href="impressum.html" target="_blank"><?= _("imprint") ?></a></span>
		</div>
	</div>

	<div id="modalDelete" class="modal">
		<div class="modal-content">
			<span class="close">&times;</span>
			<span><?= sprintf(_("modal_do_you_want_to_delete"), '<span id="dataSubjectDelete"></span>') ?></span>
			<form action="perform_delete_conversation.php" method="post">
    			<input type="hidden" name="conversationId" id="modalDeleteConversationId" value="">
    			<input type="hidden" name="relationId" id="modalDeleteRelationId" value="">
    			<div class="container">
    				<span class="left"> <input type="button" name="cancel" class="modal-button" value="<?= _("cancel") ?>" onclick="$('#modalDelete').hide();"></span> 
    				<span class="right"> <input type="submit" name="submit" class="modal-button" value="<?= _("delete") ?>"></span>
    			</div>
			</form>
		</div>
	</div>
	<div id="modalEdit" class="modal">
		<div class="modal-content">
			<span class="close">&times;</span> <span><?= sprintf(_("modal_edit_subject"), '<span id="dataSubjectEdit"></span>') ?></span>
			<form action="perform_edit_conversation.php" method="post">
				<input type="hidden" name="conversationId" id="modalEditConversationId" value="">
				<input type="hidden" name="relationId" id="modalEditRelationId" value="">
				<div class="form-group">
    				<input type="text" name="subject" id="modalEditSubject" maxlength="100" value="">
				</div>
				<div class="container">
					<span class="left"> <input type="button" name="cancel" class="modal-button" value="<?= _("cancel") ?>"
						onclick="$('#modalEdit').hide();"></span> <span class="right"> <input type="submit" name="submit"
						class="modal-button" value="<?= _("change_subject") ?>"></span>
				</div>
			</form>
		</div>
	</div>
</body>
</html>

