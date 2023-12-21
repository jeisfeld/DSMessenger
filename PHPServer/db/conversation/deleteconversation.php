<?php
require_once '../../firebase/firebasefunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

verifyCredentialsAndRelation($conn, $_POST);
$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];

$stmt = $conn->prepare("DELETE FROM dsm_conversation WHERE id = ? and relation_id = ?");
$stmt->bind_param("si", $conversationId, $relationId);
$stmt->execute();
$stmt->close();

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