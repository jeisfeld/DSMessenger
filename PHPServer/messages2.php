<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once __DIR__ . '/db/conversation/querymessagefunctions.php';
require_once 'openai/queryopenai.php';
include __DIR__ . '/vendor/erusev/parsedown/Parsedown.php';
use Ramsey\Uuid\Uuid;

$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];
$relationId = $_GET['relationId'];
$conversationId = $_GET['conversationId'];

$conversationData = getConversationData($userId, $relationId, $conversationId);
$contactName = $conversationData['contactName'];
$contactId = $conversationData['contactId'];
$isSlave = $conversationData['isSlave'];
$subject = $conversationData['subject'];

$conn = getDbConnection();
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
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
		<h1 class="contains-heading-text"><span class="heading-text"><?= sprintf(_("Conversation with"), $subject, $contactName) ?></span>
			<span class="right"><svg id="button-reload" onclick="window.location.href = 'messages.php?relationId=<?= $relationId ?>&conversationId=<?= $conversationId ?>';" class="icon"><use href="images/icons.svg#icon-reload"/></svg></span>
		</h1>

		<div id="messages">
            <?php
            $messages = queryMessages($username, $password, $relationId, $conversationId);
            foreach ($messages as $message) {
                $class = $message['userId'] == $userId ? 'own-message' : 'other-message';
                $parsedown = new Parsedown();
                $messageText = $parsedown->text($message['text']);
                $messageText = preg_replace("/\r\n|\n/", "<br>\n", $messageText);
                $messageText = preg_replace("/(<\/?(p|li|ul|ol|h1|h2|h3|h4|h5)>)<br>/", "$1", $messageText);
                
                echo '<div class="message '. $class. '">';
                echo '<div class="text">' . $messageText . '</div>';
                echo '<span class="time">' . htmlspecialchars(convertTimestamp($message['timestamp'])) . '</span>'; // Format time as needed
                echo '</div>';
            }
            ?>
	        <div class="message" id="waiting-for-response"><?= _("(Waiting for response...)") ?></div>
        </div>
        
		<div id="footer">
			<span class="left"></span> <span class="right"><a href="impressum.html" target="_blank"><?= _("Imprint") ?></a></span>
		</div>
	</div>

    <script>
	const messages = document.getElementById('messages');
	messages.scrollTop = messages.scrollHeight;
    </script>
    
<?php 
$responseMessage = null;
$aiPolicy = 0;

$aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);
if ($aiRelation) {
    $aiPolicy = $aiRelation['aiPolicy'];
}

if ($message && ($aiPolicy == 2 || $aiPolicy == 3)) {
    $result = handleOpenAi($username, $password, $relationId, $conversationId, $aiRelation);
    if ($result['success']) {
        $responseMessage = $result['message']['content'];
    }
    else {
        $responseMessage = "System error - please retry later - " . $result['error']['message'];
    }
}

if ($message && ($aiPolicy == 2 || $aiPolicy == 3)) {
    $responseMessageId = Uuid::uuid4()->toString();
    $currentDateTime = new DateTime();
    $responseMysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);
    
    $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
    $stmt->bind_param("ssiss", $responseMessageId, $conversationId, $contactId, $responseMessage, $responseMysqlTimestamp);
    $stmt->execute();
    $stmt->close();
    $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ?, prepared_message = '' where id = ?");
    $stmt->bind_param("ss", $responseMysqlTimestamp, $conversationId);
    $stmt->execute();
    $stmt->close();
    
    $tokens = getUnmutedSelfTokens($conn, $username, $password, - 1);
    $currentDateTime = new DateTime();
    $data = [
        "messageType" => "TEXT",
        "messageText" => $responseMessage,
        "priority" => "NORMAL",
        "conversationId" => $conversationId,
        "timestamp" => convertToJavaTimestamp($responseMysqlTimestamp),
        "messageId" => $responseMessageId,
        "relationId" => $relationId,
        "preparedMessage" => "",
        "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
    ];
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
}

if ($message && $aiPolicy == 2) {
    $tokens = getUnmutedTokens($conn, $username, $password, $relationId);
    $currentDateTime = new DateTime();
    $data = [
        "messageType" => "TEXT_OWN",
        "messageText" => $responseMessage,
        "priority" => "NORMAL",
        "conversationId" => $conversationId,
        "timestamp" => convertToJavaTimestamp($responseMysqlTimestamp),
        "messageId" => $responseMessageId,
        "relationId" => $relationId,
        "preparedMessage" => "",
        "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
    ];
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
}

if ($message && $aiPolicy == 4) {
    $lastMessage = end($messages);
    if ($lastMessage['userId'] == $userId) {
        $lastTimestamp = $lastMessage['timestamp'];
        
        $result = null;
        $timeout = 180; // Timeout after 120 seconds (2 minutes) to avoid infinite loop
        $start_time = time();

        $stmt = $conn->prepare("SELECT id, user_id, text, timestamp, status from dsm_message WHERE conversation_id = ? AND timestamp > ? ORDER BY timestamp");
        $stmt->bind_param("ss", $conversationId, $lastTimestamp);
        $found = false;
        
        while (!$found && (time() - $start_time) < $timeout) {
            $stmt->execute();
            
            $messageId = null;
            $msgUserId = null;
            $text = null;
            $timestamp = null;
            $status = null;
            $stmt->bind_result($messageId, $msgUserId, $text, $timestamp, $status);
            
            if ($stmt->fetch()) {
                $found = true;
            }
            else {
                $expiredTime = time() - $start_time;
                sleep($expiredTime < 15 ? 2 : ($expiredTime < 40 ? 5 : 10)); 
            }
            $stmt->reset();
        }

        $stmt->close();
    }
}

echo '<script type="text/javascript">';
echo 'window.location.href = "messages.php?relationId=' . $relationId . '&conversationId=' . $conversationId . '";';
echo '</script>';

$conn->close();

?>


</body>
</html>
