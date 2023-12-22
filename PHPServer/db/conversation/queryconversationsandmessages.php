<?php
require_once __DIR__.'/../dbfunctions.php';

function queryConversationsAndMessages($username, $password, $relationId, $isSlave)
{
    // Create connection
    $conn = getDbConnection();
    $conn2 = getDbConnection();
    
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
        $messageId = null;
        $msgUserId = null;
        $text = null;
        $timestamp = null;
        $status = null;
        $stmt2 = $conn2->prepare("SELECT id, user_id, text, timestamp, status from dsm_message WHERE conversation_id = ? order by timestamp");
        $stmt2->bind_param("s", $conversationId);
        $stmt2->execute();
        $stmt2->bind_result($messageId, $msgUserId, $text, $timestamp, $status);
        $messages = array();
        while ($stmt2->fetch()) {
            $messages[] = [
                'messageId' => $messageId,
                'isOwn' => $msgUserId == $userId ? 1 : 0, 
                'text' => $text,
                'timestamp' => $timestamp,
                'status' => $status
            ];
        }
        $stmt2->close();
        $conversations[] = [
            'conversationId' => $conversationId,
            'subject' => $subject,
            'flags' => $flags,
            'lasttimestamp' => $lasttimestamp,
            'messages' => $messages
        ];
    }
    $conn -> close();
    $conn2 -> close();
    
    return $conversations;
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$relationId = @$_POST['relationId'];
$isSlave = @$_POST['isSlave'];

$username = "jeisfeld";
$password = "45Karte45";
$relationId = 1;
$isSlave = 1;

if ($username) {
    header('Content-Type: text/json');
    $conversations = queryConversationsAndMessages($username, $password, $relationId, $isSlave);
    
    printSuccess("Conversations for relationId " . $relationId . " have been retrieved.", [
        'conversations' => $conversations
    ]);
}
