<?php
require_once '../../firebase/firebasefunctions.php';

// Create connection
$conn = getDbConnection();
header('Content-Type: text/json');

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$userId = verifyCredentialsAndRelation($conn, $_POST);
$messageText = @$_POST['messageText'];
$messageId = @$_POST['messageId'];
$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];
$conversationFlags = @$_POST['conversationFlags'];
$timestamp = @$_POST['timestamp'];
$mysqltimestamp = convertJavaTimestamp($timestamp);
$messageIds = @$_POST['messageIds'];

$stmt = $conn->prepare("SELECT id FROM dsm_conversation WHERE id = ?");
$stmt->bind_param("s", $conversationId);
$stmt->execute();

if ($stmt->get_result()->num_rows) {
    $stmt->close();
    $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ? where id = ?");
    $stmt->bind_param("ss", $mysqltimestamp, $conversationId);
    $stmt->execute();
    $stmt->close();
}
else {
    $stmt->close();
    $stmt = $conn->prepare("INSERT INTO dsm_conversation (id, relation_id, subject, flags, lasttimestamp) values (?, ?, ?, ?, ?)");
    $stmt->bind_param("sisss", $conversationId, $relationId, $messageText, $conversationFlags, $mysqltimestamp);
    $stmt->execute();
    $stmt->close();
}

if ($messageText) {
    $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
    $stmt->bind_param("ssiss", $messageId, $conversationId, $userId, $messageText, $mysqltimestamp);
    $stmt->execute();
    $stmt->close();
}

if ($messageIds) {
   $messageIdArr = explode(",", $messageIds);
   foreach ($messageIdArr as $msgId) {
       $stmt = $conn->prepare("UPDATE dsm_message SET status = 2 where id = ? and conversation_id = ?");
       $stmt->bind_param("ss", $msgId, $conversationId);
       $stmt->execute();
       $stmt->close();
   }
}

$tokens = getVerifiedTokensFromRequestData();

$data = [];
$ttl = null;

foreach ($_POST as $key => $value) {
    if ($key == 'ttl') {
        $ttl = $value;
    }
    elseif ($key !== 'username' && $key !== 'password' && $key !== 'contactId' && $key !== 'isSlave' && $key !== 'isConnected') {
        $data[$key] = $value;
    }
}

foreach ($tokens as $token) {
    sendFirebaseMessage($token, $data, $ttl);
}

printSuccess("Message successfully sent");
$conn->close();

?>
