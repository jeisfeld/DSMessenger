<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $conversationId = $_POST['conversationId'];
    $relationId = $_POST['relationId'];
    
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }

    $stmt = $conn->prepare("delete from dsm_conversation where id = ? and relation_id = ? and relation_id in
               (select id from dsm_relation where master_id = ? or slave_id = ?)");
    $stmt->bind_param("siii", $conversationId, $relationId, $userId, $userId);

    $stmt->execute();

    sendAdminMessage($conn, $username, $password, $relationId, "CONVERSATION_DELETED", [
        'conversationId' => $conversationId
    ]);
    
    header("Location: conversations.php?relationId=" . $relationId);
}
?>