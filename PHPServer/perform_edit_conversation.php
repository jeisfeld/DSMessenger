<?php
include __DIR__ . '/check_session.php';
require_once 'db/dbfunctions.php';
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $conversationId = $_POST['conversationId'];
    $relationId = $_POST['relationId'];
    $params = $_POST['params'];
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

    header("Location: conversations.php?" . $params);
}
?>