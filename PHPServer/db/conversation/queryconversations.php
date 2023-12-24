<?php
require_once __DIR__.'/../dbfunctions.php';

function queryConversations($username, $password, $relationId, $isSlave)
{
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userId = verifyCredentials($conn, $username, $password);
    
    verifyRelation($conn, $relationId, $userId, $isSlave);
    
    $conversationId = null;
    $subject = null;
    $flags = null;
    $lasttimestamp = null;
    $stmt = $conn->prepare("SELECT id, subject, flags, lasttimestamp from dsm_conversation WHERE relation_id = ? order by lasttimestamp desc");
    
    $stmt->bind_param("s", $relationId);
    $stmt->execute();
    $stmt->bind_result($conversationId, $subject, $flags, $lasttimestamp);
    
    $conversations = array();
    while ($stmt->fetch()) {
        $conversations[] = [
            'conversationId' => $conversationId,
            'subject' => $subject,
            'flags' => $flags,
            'lasttimestamp' => $lasttimestamp
        ];
    }
    $stmt -> close();
    $conn -> close();
    
    return $conversations;
}
