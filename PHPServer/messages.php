<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/conversation/querymessages.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];
$subject = $_GET['subject'];
$contactName = $_GET['contactName'];
$relationId = $_GET['relationId'];
$isSlave = $_GET['isSlave'];

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
<link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>

	<div id="chat-container">
		<div id="header">
			<span class="left">Username: <?= $username ?></span> <span class="right">
			<a href="conversations.php?relationId=<?= $relationId ?>&contactName=<?= $contactName ?>&isSlave=<?= $isSlave ?>">Conversations with <?= $contactName ?></a>
			&nbsp;<a href="logout.php">Logout</a></span>
		</div>
		<h1>Conversation "<?= $subject ?>" with <?= $contactName ?></h1>

		<div id="messages">
            <?php
            $conversationId = $_GET['conversationId'];
            $messages = queryMessages($username, $password, $relationId, $isSlave, $conversationId);
            foreach ($messages as $message) {
                // Assume $message['is_own'] is true if it's the user's message
                $class = $message['userId'] == $userId ? 'own-message' : 'other-message';
                echo "<div class='message $class'>";
                echo "<p class='text'>" . htmlspecialchars($message['text']) . "</p>";
                echo "<span class='time'>" . htmlspecialchars(convertTimestamp($message['timestamp'])) . "</span>"; // Format time as needed
                echo "</div>";
            }
            ?>
	        </div>
		<div id="message-input">
			<form action="sendmessage.php" method="post" class="message-form">
				<input type="hidden" name="conversationId" value="<?= $conversationId ?>">
				<input type="hidden" name="relationId" value="<?= $relationId ?>"> 
				<input type="hidden" name="isSlave" value="<?= $isSlave ?>">
				<input type="hidden" name="subject" value="<?= $subject ?>">
				<input type="hidden" name="contactName" value="<?= $contactName ?>">
				<textarea name="message" placeholder="Type your message here..." class="message-textarea"></textarea>
				<button type="submit" class="send-button">Send</button>
			</form>
		</div>

	</div>
    <script>
        function scrollToBottom() {
            const messages = document.getElementById('messages');
            messages.scrollTop = messages.scrollHeight;
        }
    
        // Call scrollToBottom when the page loads and whenever new messages are loaded or sent
        window.onload = scrollToBottom;
    </script>
</body>
</html>
