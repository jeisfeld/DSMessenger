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
$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];
$username = $_POST['username'];
$password = $_POST['password'];

$stmt = $conn->prepare("DELETE FROM dsm_conversation WHERE id = ? and relation_id = ?");
$stmt->bind_param("si", $conversationId, $relationId);
$stmt->execute();
$stmt->close();

sendAdminMessage($conn, $username, $password, $relationId, "CONVERSATION_DELETED", [
    'conversationId' => $conversationId
]);
$conn->close();

printSuccess("Message successfully sent");

?>
