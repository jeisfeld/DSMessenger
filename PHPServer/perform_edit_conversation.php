<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once __DIR__ . '/db/dbfunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $conversationId = $_POST['conversationId'];
    $relationId = $_POST['relationId'];
    $subject = $_POST['modalEditSubject'];
    $archived = intval($_POST['modalEditArchived'] == 'true');
    $fromMessages = @$_POST['fromMessages'];
    $moveToRelationId = @$_POST['modalMoveToRelationId'];

    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }

    $targetRelationId = $relationId;
    if ($moveToRelationId && $moveToRelationId != $relationId) {
        $sourceRelationData = getRelationData($userId, $relationId, $conn);
        $targetRelationData = getRelationData($userId, $moveToRelationId, $conn);
        if (! hasManageConversationsPermission($sourceRelationData) || ! hasManageConversationsPermission($targetRelationData)) {
            printError(107, "Insufficient privileges");
        }
        $targetRelationId = $moveToRelationId;
    }

    $stmt = $conn->prepare("update dsm_conversation set subject = ?, archived = ?, relation_id = ? where id = ? and relation_id = ? and relation_id in
               (select id from dsm_relation where master_id = ? or slave_id = ?)");
    $stmt->bind_param("sisiiii", $subject, $archived, $targetRelationId, $conversationId, $relationId, $userId, $userId);

    $stmt->execute();
    $stmt->close();
    
    sendAdminMessage($conn, $username, $password, $targetRelationId, "CONVERSATION_EDITED", [
        'conversationId' => $conversationId,
        'subject' => $subject,
        'archived' => $archived ? 'true' : 'false'
    ]);
    
    if ($fromMessages) {
        header("Location: messages.php?relationId=" . $targetRelationId . "&conversationId=" . $conversationId);
    }
    else {
        header("Location: conversations.php?relationId=" . $targetRelationId);        
    }
    
    $conn->close();
}
?>