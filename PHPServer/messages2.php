<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once __DIR__ . '/db/conversation/querymessages.php';
require_once 'db/conversation/queryairelation.php';
require_once 'db/conversation/querymessagesforopenai.php';
require_once 'openai/queryopenai.php';
use Ramsey\Uuid\Uuid;
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];
$subject = $_GET['subject'];
$contactName = $_GET['contactName'];
$contactId = $_GET['contactId'];
$relationId = $_GET['relationId'];
$isSlave = $_GET['isSlave'];
$replyPolicy = $_GET['replyPolicy'];

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
<title>Coachat - <?= sprintf(_("conversation_with"), $subject, $contactName) ?></title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<script src="js/jquery-3.6.2.min.js"></script>
</head>
<body>

	<div id="chat-container">
		<div id="header">
			<span class="left"><?= _("username") ?>: <?= $username ?></span> <span class="right">
			<a id="conversations-link" href="conversations.php?relationId=<?= $relationId ?>"><?= sprintf(_("conversations_with"), $contactName) ?></a>
			&nbsp;<a href="logout.php"><?= _("logout") ?></a></span>
		</div>
		<h1><?= sprintf(_("conversation_with"),  substr($subject, 0, 30), $contactName) ?></h1>

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
	</div>

    <script>
	const messages = document.getElementById('messages');
	messages.scrollTop = messages.scrollHeight;
    </script>
    
<?php 
$conn = getDbConnection();
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}
getRelationData($conn, $userId, $relationId);

$responseMessage = null;
$aiPolicy = 0;

$aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);
if ($aiRelation) {
    $aiPolicy = $aiRelation['aiPolicy'];
}

if ($message && $aiPolicy > 1) {
    $messages = queryMessagesForOpenai($username, $password, $relationId, $conversationId, $aiRelation['promptmessage'], $aiRelation['oldMessageCount'], $aiRelation['oldMessageCountVariation'], $aiRelation['maxCharacters']);
    
    $result = queryOpenAi($messages, $aiRelation['temperature'], $aiRelation['presencePenalty'], $aiRelation['frequencyPenalty']);
    if ($result['success']) {
        $responseMessage = $result['message']['content'];
    }
    else {
        $aiPolicy = 0;
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
    
    $tokens = getSelfTokens($conn, $username, $password, - 1);
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
    $tokens = getUnmutedTokens($conn, $username, $password, $relationId, $isSlave);
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

echo '<script type="text/javascript">';
echo 'window.location.href = "https://coachat.de/messages.php?conversationId=' . $conversationId . '&relationId=' . $relationId .
    '&isSlave=' . $isSlave . '&subject=' . $subject . '&contactName=' . $contactName . '&contactId=' . $contactId . '&replyPolicy=' . $replyPolicy. '";';
echo '</script>';

?>


</body>
</html>
