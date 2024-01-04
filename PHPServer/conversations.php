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
		<h1><?= sprintf(_("conversations_with"), $contactName) ?></h1>
		<ul id="conversation-list">
            <?php
            // Retrieve conversations            
            foreach ($conversations as $conversation) {
                $conversationId = $conversation['conversationId'];
                $subject = $conversation['subject'];
                echo "<li class='conversation-item'><a href='messages.php?relationId=" . $relationId . "&conversationId=" . $conversationId . "'>" . $subject . '</a><div class="icons">
                <svg class="icon editButton" data-conversation-id="' . $conversationId . '" data-relation-id="' . $relationId . '" data-subject="' . $subject . '" width="120" height="120" viewBox="0 0 120 120"><path d="M96.84,2.22l22.42,22.42c2.96,2.96,2.96,7.8,0,10.76l-12.4,12.4L73.68,14.62l12.4-12.4 C89.04-0.74,93.88-0.74,96.84,2.22L96.84,2.22z M70.18,52.19L70.18,52.19l0,0.01c0.92,0.92,1.38,2.14,1.38,3.34 c0,1.2-0.46,2.41-1.38,3.34v0.01l-0.01,0.01L40.09,88.99l0,0h-0.01c-0.26,0.26-0.55,0.48-0.84,0.67h-0.01 c-0.3,0.19-0.61,0.34-0.93,0.45c-1.66,0.58-3.59,0.2-4.91-1.12h-0.01l0,0v-0.01c-0.26-0.26-0.48-0.55-0.67-0.84v-0.01 c-0.19-0.3-0.34-0.61-0.45-0.93c-0.58-1.66-0.2-3.59,1.11-4.91v-0.01l30.09-30.09l0,0h0.01c0.92-0.92,2.14-1.38,3.34-1.38 c1.2,0,2.41,0.46,3.34,1.38L70.18,52.19L70.18,52.19L70.18,52.19z M45.48,109.11c-8.98,2.78-17.95,5.55-26.93,8.33 C-2.55,123.97-2.46,128.32,3.3,108l9.07-32v0l-0.03-0.03L67.4,20.9l33.18,33.18l-55.07,55.07L45.48,109.11L45.48,109.11z M18.03,81.66l21.79,21.79c-5.9,1.82-11.8,3.64-17.69,5.45c-13.86,4.27-13.8,7.13-10.03-6.22L18.03,81.66L18.03,81.66z" fill="currentColor"></path></svg>
                <svg class="icon deleteButton" data-conversation-id="' . $conversationId . '" data-relation-id="' . $relationId . '" data-subject="' . $subject . '" width="110" height="120" viewBox="0 0 110 120"><path d="M2.347,9.633h38.297V3.76c0-2.068,1.689-3.76,3.76-3.76h21.144 c2.07,0,3.76,1.691,3.76,3.76v5.874h37.83c1.293,0,2.347,1.057,2.347,2.349v11.514H0V11.982C0,10.69,1.055,9.633,2.347,9.633 L2.347,9.633z M8.69,29.605h92.921c1.937,0,3.696,1.599,3.521,3.524l-7.864,86.229c-0.174,1.926-1.59,3.521-3.523,3.521h-77.3 c-1.934,0-3.352-1.592-3.524-3.521L5.166,33.129C4.994,31.197,6.751,29.605,8.69,29.605L8.69,29.605z M69.077,42.998h9.866v65.314 h-9.866V42.998L69.077,42.998z M30.072,42.998h9.867v65.314h-9.867V42.998L30.072,42.998z M49.572,42.998h9.869v65.314h-9.869 V42.998L49.572,42.998z" fill="currentColor"></path></svg></div></li>';
            }
            ?>
            
        </ul>
		<div id="message-input">
			<form action="sendmessage.php" method="post" class="message-form">
				<input type="hidden" name="relationId" value="<?= $relationId ?>">
				<input type="hidden" name="conversationId" value="">
				<input type="hidden" name="subject" value="">
				<textarea autofocus name="message" maxlength="40000" placeholder="<?= _("start_new_conversation") ?>" class="message-textarea"></textarea>
				<button type="submit" class="send-button"><?= _("send") ?></button>
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

