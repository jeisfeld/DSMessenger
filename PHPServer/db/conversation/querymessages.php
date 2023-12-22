<?php
require_once __DIR__.'/../dbfunctions.php';

function queryMessages($username, $password, $relationId, $isSlave, $conversationId)
{
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userId = verifyCredentials($conn, $username, $password);
    
    verifyRelation($conn, $relationId, $userId, $isSlave);
    
    $messageId = null;
    $userId = null;
    $text = null;
    $timestamp = null;
    $status = null;
    $stmt = $conn->prepare("SELECT id, user_id, text, timestamp, status from dsm_message WHERE conversation_id = ? order by timestamp");
    
    $stmt->bind_param("s", $conversationId);
    $stmt->execute();
    $stmt->bind_result($messageId, $userId, $text, $timestamp, $status);
    
    $messages = array();
    while ($stmt->fetch()) {
        $messages[] = [
            'messageId' => $messageId,
            'userId' => $userId,
            'text' => $text,
            'timestamp' => $timestamp,
            'status' => $status
        ];
    }
    $stmt -> close();
    $conn -> close();
    
    return $messages;
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$relationId = @$_POST['relationId'];
$isSlave = @$_POST['isSlave'];
$conversationId = @$_POST['conversationId'];
if ($username) {
    header('Content-Type: text/json');
    $messages = queryMessages($username, $password, $relationId, $isSlave, $conversationId);
    
    printSuccess("Messages for conversationId " . $conversationId . " have been retrieved.", [
        'messages' => $messages
    ]);
}
