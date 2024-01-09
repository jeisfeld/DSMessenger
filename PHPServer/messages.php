<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/conversation/querymessages.php';
require_once 'openai/queryopenai.php';
include __DIR__ . '/vendor/erusev/parsedown/Parsedown.php';

$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];
$relationId = $_GET['relationId'];
$conversationId = $_GET['conversationId'];

$conversationData = getConversationData($userId, $relationId, $conversationId);
$contactName = $conversationData['contactName'];
$contactId = $conversationData['contactId'];
$isSlave = $conversationData['isSlave'];
$preparedMessage = $isSlave ? $conversationData['preparedMessage'] : "";
$subject = $conversationData['subject'];
$archived = $conversationData['archived'];
$aiPolicy = 0;
if (! $isSlave) {
    $aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);
    if ($aiRelation) {
        $aiPolicy = $aiRelation['aiPolicy'];
    }
}

function convertTimestamp($mysqlTimestamp) {
    $timestampDateTime = DateTime::createFromFormat('Y-m-d H:i:s.u', $mysqlTimestamp);
    $todayDateTime = new DateTime();
    $todayDateTime->setTime(0, 0, 0); // Reset time part to 00:00:00 for accurate comparison
    if ($timestampDateTime->format('Y-m-d') === $todayDateTime->format('Y-m-d')) {
        return $timestampDateTime->format('H:i:s');
    } else {
        return $timestampDateTime->format('Y-m-d H:i:s');
    }
}

?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= sprintf(_("Conversation with"), $subject, $contactName) ?></title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="js/jquery-3.6.2.min.js"></script>
</head>
<body>

	<div id="chat-container">
		<div id="header">
			<span class="left"><?= _("Username") ?>: <?= $username ?></span> <span class="right">
			<a id="conversations-link" href="conversations.php?relationId=<?= $relationId ?>"><?= sprintf(_("Conversations with"), $contactName) ?></a>
			&nbsp;<a href="logout.php"><?= _("Logout") ?></a></span>
		</div>
		<h1><?= sprintf(_("Conversation with"), substr($subject, 0, 30), $contactName) ?>
			<span class="right">
			<svg id="editButton" class="icon" data-conversation-id="<?= $conversationId ?>" data-relation-id="<?= $relationId ?>" data-subject="<?= $subject ?>" data-archived="<?= $archived ?>"><use xlink:href="images/icons.svg#icon-edit"></use></svg>
			<svg id="button-reload" onclick="location.reload()" class="icon"><use xlink:href="images/icons.svg#icon-reload"></use></svg>
			</span>
		</h1>

		<div id="messages">
            <?php
            $messages = queryMessages($username, $password, $relationId, $isSlave, $conversationId);
            $lastMessageKey = array_key_last($messages);
            foreach ($messages as $key => $message) {
                $class = $message['userId'] == $userId ? 'own-message' : 'other-message';
                $parsedown = new Parsedown();
                $messageText = $parsedown->text($message['text']);
                
                echo "<div class='message $class'>";
                echo "<div class='text'>" . $messageText . "</div>";
                echo "<span class='time'>" . htmlspecialchars(convertTimestamp($message['timestamp'])) . "</span>";
                if ($key === $lastMessageKey && $message['userId'] != $userId && ($aiPolicy == 2 || $aiPolicy == 3) && !$isSlave) {
                    echo '<svg id="icon-retry" class="icon" onclick="window.location.href = \'messages2.php?relationId=' . $relationId . '&conversationId=' . $conversationId . '&deleteLast=true\';"><use xlink:href="images/icons.svg#icon-reload"></use></svg>';
                }
                echo "</div>";
            }
            ?>
	    </div>
		<div id="message-input">
			<form action="sendmessage.php" method="post" class="message-form" id="formSubmitMessage">
				<input type="hidden" name="conversationId" id="conversationId" value="<?= $conversationId ?>">
				<input type="hidden" name="relationId" id="relationId" value="<?= $relationId ?>"> 
				<input type="hidden" name="contactId" value="<?= $contactId ?>">
				<input type="hidden" name="isSlave" id="isSlave" value="<?= $isSlave ?>">
				<input type="hidden" name="subject" value="<?= $subject ?>">
				<input type="hidden" name="replyPolicy" value="">
				<input type="hidden" name="contactName" value="<?= $contactName ?>">
				<textarea autofocus name="message" id="message" maxlength="40000" placeholder="<?= _("Type your message here...") ?>" class="message-textarea"><?= $preparedMessage ?></textarea>
				<button type="submit" class="send-button" id="buttonSubmitMessage"><?= _("Send") ?></button>
			</form>
		</div>

		<div id="footer">
			<span class="left"></span> <span class="right"><a href="impressum.html" target="_blank"><?= _("Imprint") ?></a></span>
		</div>
	</div>

	<div id="modalEdit" class="modal">
		<div class="modal-content">
			<h2><?= _("Edit Conversation") ?></h2>
			<span class="close">&times;</span> 
			<form action="perform_edit_conversation.php" method="post">
				<input type="hidden" name="conversationId" id="modalEditConversationId" value="">
				<input type="hidden" name="relationId" id="modalEditRelationId" value="">
				<div class="form-group">
    				<label for="modalEditSubject"><?= _("Subject") ?>:</label><input type="text" name="modalEditSubject" id="modalEditSubject" maxlength="100" value="">
				</div>
				<div class="form-group">
    				<label for="modalEditArchived"><?= _("Archived") ?>:</label><input type="checkbox" name="modalEditArchived" value="true" id="modalEditArchived">
				</div>
				<div class="container">
					<span class="left"> <input type="button" name="cancel" class="modal-button" value="<?= _("Cancel") ?>"
						onclick="$('#modalEdit').hide();"></span> <span class="right"> <input type="submit" name="submit"
						class="modal-button" value="<?= _("Save Conversation") ?>"></span>
				</div>
			</form>
		</div>
	</div>

    <script src="js/messages.js"></script>

</body>
</html>
