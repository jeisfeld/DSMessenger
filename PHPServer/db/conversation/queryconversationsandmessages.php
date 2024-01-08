<?php
require_once __DIR__.'/../dbfunctions.php';

function queryConversationsAndMessages($username, $password, $startTime)
{
    // Create connection
    $conn = getDbConnection();
    $conn2 = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userId = verifyCredentials($conn, $username, $password);
    
    $conversationId = null;
    $relationId = null;
    $subject = null;
    $flags = null;
    $lasttimestamp = null;
    $preparedMessage = null;
    $archived = null;
    $mysqltimestamp = convertJavaTimestamp($startTime);
    
    $stmt = $conn->prepare("SELECT id, relation_id, subject, flags, lasttimestamp, prepared_message, archived from dsm_conversation WHERE relation_id in (select id from dsm_relation where slave_id = ? or master_id = ? ) order by lasttimestamp desc");
    
    $stmt->bind_param("ii", $userId, $userId);
    $stmt->execute();
    $stmt->bind_result($conversationId, $relationId, $subject, $flags, $lasttimestamp, $preparedMessage, $archived);
    
    $conversations = array();
    while ($stmt->fetch()) {
        $messageId = null;
        $msgUserId = null;
        $text = null;
        $timestamp = null;
        $status = null;
        $stmt2 = $conn2->prepare("SELECT id, user_id, text, timestamp, status from dsm_message WHERE conversation_id = ? and timestamp > ? order by timestamp");
        $stmt2->bind_param("ss", $conversationId, $mysqltimestamp);
        $stmt2->execute();
        $stmt2->bind_result($messageId, $msgUserId, $text, $timestamp, $status);
        $messages = array();
        while ($stmt2->fetch()) {
            $messages[] = [
                'messageId' => $messageId,
                'isOwn' => $msgUserId == $userId ? 1 : 0, 
                'text' => $text,
                'timestamp' => convertToJavaTimestamp($timestamp),
                'status' => $status
            ];
        }
        $stmt2->close();
        $conversations[] = [
            'relationId' => $relationId,
            'conversationId' => $conversationId,
            'subject' => $subject,
            'flags' => $flags,
            'lasttimestamp' => convertToJavaTimestamp($lasttimestamp),
            'preparedMessage' => $preparedMessage,
            'archived' => $archived ? "true" : "false",
            'messages' => $messages
        ];
    }
    $conn -> close();
    $conn2 -> close();
    
    return $conversations;
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$startTime = @$_POST['startTime'];

if ($username) {
    header('Content-Type: text/json');
    $conversations = queryConversationsAndMessages($username, $password, $startTime);
    
    printSuccess("Conversations for relationId have been retrieved.", [
        'conversations' => $conversations
    ]);
}
