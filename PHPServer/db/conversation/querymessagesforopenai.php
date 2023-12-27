<?php
require_once __DIR__.'/../dbfunctions.php';
require_once __DIR__ . '/../../openai/queryopenai.php';

function queryMessagesForOpenai($username, $password, $relationId, $conversationId, $promptmessage)
{
    $maxCharacters = 20000; // The number of characters when the prompt is stopped
    
    $totalcharacters = strlen($promptmessage['content']);
    
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userId = verifyCredentials($conn, $username, $password);
    verifyRelation($conn, $relationId, $userId, FALSE);
    
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
    $stmt -> close();
    
    array_unshift($messages, $promptmessage);
    
    // TODO: add messages from other conversations, paired as user/assistant, by timestamp. Always add latest assistent message and prior user messages.
    

    $conn -> close();
    return $messages;
}
