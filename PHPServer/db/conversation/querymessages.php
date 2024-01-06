<?php
require_once __DIR__.'/../dbfunctions.php';
require_once __DIR__ . '/../../openai/queryopenai.php';

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

function deleteLastMessage($userId, $conversationId, $givenMessageId = null) {
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
        if ($givenMessageId && ($givenMessageId != $messageId)) {
            return '';
        }
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

function getRandomizedMessageCount($baseCount, $variance)
{
    switch ($variance) {
        case 1:
            // equal distribution at some distance from now
            return round((2* + mt_rand() / mt_getrandmax()) * $baseCount);
        case 2:
            // exponential distribution
            return round(- 1 * $baseCount * log(mt_rand() / mt_getrandmax()));
        case 0:
        default:
            return $baseCount;
    }
}

function queryMessagesForOpenai($username, $password, $relationId, $conversationId, $promptmessage, $oldMessageCount = 20, $oldMessageCountVariation = 0, $maxCharacters = 40000)
{
    $totalcharacters = strlen($promptmessage['content']);
    
    // Create connection
    $conn = getDbConnection();
    $conn2 = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userId = verifyCredentials($conn, $username, $password);
    getRelationData($userId, $relationId, $conn);
    
    // first add messages of current conversation.
    
    $messageUser = null;
    $text = null;
    $stmt = $conn->prepare("SELECT user_id, text from dsm_message WHERE conversation_id = ? order by timestamp desc");
    $stmt->bind_param("s", $conversationId);
    $stmt->execute();
    $stmt->bind_result($messageUser, $text);
    
    $messages = [];
    while ($totalcharacters < $maxCharacters && $stmt->fetch()) {
        array_unshift($messages, [
            'role' => $messageUser == $userId ? 'user' : 'assistant',
            'content' => $text
        ]);
        $totalcharacters += strlen($text);
    }
    $stmt->close();
    
    // Then add messages of other conversations of same relation.
    
    $totalMessages = 0;
    $plannedMessages = getRandomizedMessageCount($oldMessageCount, $oldMessageCountVariation);
    
    $otherConversationId = null;
    $text = null;
    $timestamp = null;
    $messageId = null;
    $stmt = $conn->prepare("SELECT conversation_id, text, timestamp, id from dsm_message WHERE conversation_id != ? AND user_id != ?
                            AND conversation_id in (SELECT id from dsm_conversation WHERE relation_id = ?)
                            order by timestamp desc");
    $stmt->bind_param("sii", $conversationId, $userId, $relationId);
    $stmt->execute();
    $stmt->bind_result($otherConversationId, $text, $timestamp, $messageId);
    while ($totalcharacters < $maxCharacters && $totalMessages < $plannedMessages && $stmt->fetch()) {
        $totalMessages ++;
        array_unshift($messages, [
            'role' => 'assistant',
            'content' => $text
        ]);
        $totalcharacters += strlen($text);
        
        $messageUser = null;
        $text2 = null;
        $messageId2 = null;
        $stmt2 = $conn2->prepare("SELECT user_id, text, id from dsm_message WHERE conversation_id = ? order by timestamp desc");
        $stmt2->bind_param("s", $otherConversationId);
        $stmt2->execute();
        $stmt2->bind_result($messageUser, $text2, $messageId2);
        $status = 0;
        
        while ($status < 2 && $stmt2->fetch()) {
            if ($status == 0) {
                if ($messageId2 == $messageId) {
                    $status = 1;
                }
                continue;
            }
            else if ($status == 1) {
                if ($messageUser == $userId) {
                    array_unshift($messages, [
                        'role' => 'user',
                        'content' => $text2
                    ]);
                    $totalcharacters += strlen($text);
                }
                else {
                    $status = 2;
                    break;
                }
            }
        }
        $stmt2->close();
    }
    $stmt->close();
    
    array_unshift($messages, $promptmessage);
    
    $conn2->close();
    $conn->close();
    return $messages;
}
