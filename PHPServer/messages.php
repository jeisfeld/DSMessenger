<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/conversation/querymessages.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];
$subject = $_GET['subject'];
$contactName = $_GET['contactName'];
$contactId = $_GET['contactId'];
$relationId = $_GET['relationId'];
$isSlave = $_GET['isSlave'];
$replyPolicy = $_GET['replyPolicy'];
$preparedMessage = $isSlave ? $_GET['preparedMessage'] : "";

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
<title>DS Messenger - Messages</title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<script src="js/jquery-3.6.2.min.js"></script>
</head>
<body>

	<div id="chat-container">
		<div id="header">
			<span class="left"><?= _("username") ?>: <?= $username ?></span> <span class="right">
			<a id="conversations-link" href="conversations.php?relationId=<?= $relationId ?>&contactName=<?= $contactName ?>&contactId=<?= $contactId ?>&isSlave=<?= $isSlave ?>&replyPolicy=<?= $replyPolicy ?>"><?= sprintf(_("conversations_with"), $_GET['contactName']) ?></a>
			&nbsp;<a href="logout.php"><?= _("logout") ?></a></span>
		</div>
		<h1><?= sprintf(_("conversation_with"), $subject, $contactName) ?></h1>

		<div id="messages">
            <?php
            $conversationId = $_GET['conversationId'];
            $messages = queryMessages($username, $password, $relationId, $isSlave, $conversationId);
            foreach ($messages as $message) {
                // Assume $message['is_own'] is true if it's the user's message
                $class = $message['userId'] == $userId ? 'own-message' : 'other-message';
                echo "<div class='message $class'>";
                echo "<p class='text'>" . nl2br(htmlspecialchars($message['text'])) . "</p>";
                echo "<span class='time'>" . htmlspecialchars(convertTimestamp($message['timestamp'])) . "</span>"; // Format time as needed
                echo "</div>";
            }
            ?>
	    </div>
		<div id="message-input">
			<form action="sendmessage.php" method="post" class="message-form">
				<input type="hidden" name="conversationId" id="conversationId" value="<?= $conversationId ?>">
				<input type="hidden" name="relationId" id="relationId" value="<?= $relationId ?>"> 
				<input type="hidden" name="contactId" value="<?= $contactId ?>">
				<input type="hidden" name="isSlave" id="isSlave" value="<?= $isSlave ?>">
				<input type="hidden" name="subject" value="<?= $subject ?>">
				<input type="hidden" name="replyPolicy" value="">
				<input type="hidden" name="contactName" value="<?= $contactName ?>">
				<textarea autofocus name="message" id="message" placeholder="<?= _("type_message") ?>" class="message-textarea"><?= $preparedMessage ?></textarea>
				<button type="submit" class="send-button"><?= _("send") ?></button>
			</form>
		</div>

	</div>

    <script src="js/messages.js"></script>
</body>
</html>
