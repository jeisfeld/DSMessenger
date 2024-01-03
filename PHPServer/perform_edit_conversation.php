<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $conversationId = $_POST['conversationId'];
    $relationId = $_POST['relationId'];
    $subject = $_POST['subject'];
    
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }

    $stmt = $conn->prepare("update dsm_conversation set subject = ? where id = ? and relation_id = ? and relation_id in
               (select id from dsm_relation where master_id = ? or slave_id = ?)");
    $stmt->bind_param("ssiii", $subject, $conversationId, $relationId, $userId, $userId);

    $stmt->execute();

    
    $data = getAdminData($relationId, "CONVERSATION_EDITED", [
        'conversationId' => $conversationId,
        'subject' => $subject
    ]);
    
    $tokens = getUnmutedTokens($conn, $username, $password, $relationId);
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
    
    $tokens = getSelfTokens($conn, $username, $password, - 1);
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
    
    
    header("Location: conversations.php?relationId=" . $relationId);
}
?>