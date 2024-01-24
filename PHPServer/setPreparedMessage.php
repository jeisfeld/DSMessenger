<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$preparedMessage = @$_POST['preparedMessage'];
$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];
$isSlave = @$_POST['isSlave'];

if ($isSlave) {
    $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? WHERE id = ? and relation_id = ?");
    $stmt->bind_param("ssi", $preparedMessage, $conversationId, $relationId);
    $stmt->execute();
    $stmt->close();
    sendAdminMessage($conn, $username, $password, $relationId, "CONVERSATION_EDITED", [
        'conversationId' => $conversationId,
        'preparedMessage' => $preparedMessage
    ]);
}
$conn->close();

?>
