<?php
require_once __DIR__.'/../dbfunctions.php';
require_once __DIR__ . '/../../openai/queryopenai.php';

function queryMessages($username, $password, $relationId, $conversationId)
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
    $msgUserId = null;
    $text = null;
    $timestamp = null;
    $status = null;
    $stmt = $conn->prepare("SELECT id, user_id, text, timestamp, status from dsm_message WHERE conversation_id = ? order by timestamp");
    
    $stmt->bind_param("s", $conversationId);
    $stmt->execute();
    $stmt->bind_result($messageId, $msgUserId, $text, $timestamp, $status);
    
    $messages = array();
    while ($stmt->fetch()) {
        $messages[] = [
            'messageId' => $messageId,
            'userId' => $msgUserId,
            'isOwn' => $msgUserId == $userId ? 1 : 0,
            'text' => $text,
            'timestamp' => $timestamp,
            'status' => $status,
            'conversationId' => $conversationId
        ];
    }
    $stmt -> close();
    $conn -> close();
    
    return $messages;
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

function queryMessagesForOpenai($username, $password, $relationId, $conversationId, $promptmessage, $messageSuffix, $oldMessageCount = 20, $oldMessageCountVariation = 0, $maxCharacters = 40000)
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
                            AND conversation_id in (SELECT id from dsm_conversation WHERE relation_id = ? and archived = 0)
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
    
    if ($messageSuffix) {
        $messageSuffixJson = json_decode($messageSuffix, true);
        if ($messageSuffixJson) {
            $lastMessage = end($messages);
            $input = $lastMessage['content'];
            
            $pattern = '/^(.*)\s*\[([a-z]+)\]$/i';
            $matches = [];
            
            if (preg_match($pattern, $input, $matches)) {
                $modifiedMessage = $matches[1];
                $letters = str_split($matches[2]);
                $modified = false;
                
                foreach ($letters as $letter) {
                    $suffix = $messageSuffixJson[$letter];
                    if ($suffix) {
                        $modifiedMessage .= ("\n" . $suffix);
                        $modified = true;
                    }
                }
                
                if ($modified) {
                    $messages[key($messages)]['content'] = $modifiedMessage;
                }
            }
        }
        else {
            end($messages);
            $messages[key($messages)]['content'] .= ("\n" . $messageSuffix);
        }
    }    
    
    array_unshift($messages, $promptmessage);
    
    $conn2->close();
    $conn->close();
    return $messages;
}

function queryAiRelation($username, $password, $relationId, $isSlave)
{
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    verifyCredentials($conn, $username, $password);
    
    $aiUserName = null;
    $addPrimingText = null;
    $messageSuffix = null;
    $aiPolicy = null;
    $primingText = null;
    $temperature = null;
    $presencePenalty = null;
    $frequencyPenalty = null;
    $oldMessageCount = null;
    $oldMessageCountVariation = null;
    $maxCharacters = null;
    $model = null;
    
    $stmt = $conn->prepare("SELECT ai.user_name, ai.add_priming_text, ai.message_suffix, ai.ai_policy, p.priming_text, p.temperature,
            p.presence_penalty, p.frequency_penalty, p.old_message_count, p.old_message_count_variation, p.max_characters, p.model
            from dsm_ai_relation ai, dsm_ai_priming p
            where ai.relation_id = ?
            and ai.priming_id = p.id");
    
    $stmt->bind_param("i", $relationId);
    $stmt->execute();
    $stmt->bind_result($aiUserName, $addPrimingText, $messageSuffix, $aiPolicy, $primingText,
        $temperature, $presencePenalty, $frequencyPenalty, $oldMessageCount, $oldMessageCountVariation, $maxCharacters, $model);
    
    if ($stmt->fetch()) {
        $primingText2 = str_replace("[EXTRA_TEXT]", $addPrimingText, $primingText);
        if (!$aiUserName || $aiUserName == "unbekannt") {
            $primingText2 = str_replace("Dein Gegenüber heißt [NAME].", "", $primingText2);
        }
        $primingText3 = str_replace("[NAME]", $aiUserName, $primingText2);
        
        $aiRelation = [
            'aiUserName' => $aiUserName,
            'aiPolicy' => $aiPolicy,
            'promptmessage' => createOpenAiMessage('system', $primingText3),
            'messageSuffix' => $messageSuffix,
            'temperature' => $temperature,
            'presencePenalty' => $presencePenalty,
            'frequencyPenalty' => $frequencyPenalty,
            'oldMessageCount' => $oldMessageCount,
            'oldMessageCountVariation' => $oldMessageCountVariation,
            'maxCharacters' => $maxCharacters,
            'model' => $model
        ];
    }
    else {
        $aiRelation = null;
    }
    
    $stmt->close();
    $conn->close();
    
    return $aiRelation;
}

