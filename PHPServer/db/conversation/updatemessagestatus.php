<?php
require_once '../../firebase/firebasefunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

verifyCredentialsAndRelation($conn, $_POST);
$relationId = @$_POST['relationId'];
$messageId = @$_POST['messageId'];
$conversationId = @$_POST['conversationId'];
$adminType = @$_POST['adminType'];
$messageIds = @$_POST['messageIds'];

if (! $conversationId) {
    printError(121, "Missing conversationId");
}
if (! $adminType) {
    printError(122, "Missing adminType");
}

if ($adminType == "MESSAGE_RECEIVED") {
    $status = 1;
} elseif ($adminType == "MESSAGE_ACKNOWLEDGED") {
    $status = 2;
} else {
    $status = 0;
}

$stmt = $conn->prepare("SELECT id from dsm_conversation where id = ? and relation_id = ?");
$stmt->bind_param("si", $conversationId, $relationId);
$stmt->execute();
if (! $stmt->get_result()->num_rows) {
    printError(107, "Insufficient privileges");
}
$stmt->close();

$stmt = $conn->prepare("UPDATE dsm_message SET status = GREATEST(status, ?) where id = ? and conversation_id = ?");
$stmt->bind_param("iss", $status, $messageId, $conversationId);
$stmt->execute();
$stmt->close();

if ($messageIds) {
    $messageIdArr = explode(",", $messageIds);
    foreach ($messageIdArr as $msgId) {
        $stmt = $conn->prepare("UPDATE dsm_message SET status = GREATEST(status, ?) where id = ? and conversation_id = ?");
        $stmt->bind_param("iss", $status, $msgId, $conversationId);
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

?>
