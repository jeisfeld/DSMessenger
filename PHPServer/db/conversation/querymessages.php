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
    
    getRelationData($userId, $relationId, $conn);
    
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

function deleteLastMessage($userId, $conversationId) {
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $messageId = null;
    $stmt = $conn->prepare("SELECT id FROM dsm_message WHERE conversation_id = ? AND user_id != ? order by timestamp desc");
    $stmt->bind_param("si", $conversationId, $userId);
    $stmt->execute();
    $stmt->bind_result($messageId);
    if ($stmt -> fetch()) {
        $stmt -> close();
        $stmt = $conn->prepare("DELETE FROM dsm_message WHERE id = ?");
        $stmt->bind_param("s", $messageId);
        $stmt->execute();
        $stmt -> close();
        $conn -> close();
        return $messageId;
    }
    else {
        $stmt -> close();
        $conn -> close();
        return '';
    }

}

