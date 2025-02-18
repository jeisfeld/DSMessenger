<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once 'db/conversation/querymessagefunctions.php';
require_once 'openai/queryopenai.php';
use Ramsey\Uuid\Uuid;
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $relationId = $_POST['relationId'];
    $conversationId = $_POST['conversationId'];
    $messageText = $_POST['messageText'];
    @$lastOwnMessageId = $_POST['lastOwnMessageId'];
    @$lastAiMessageId = $_POST['lastAiMessageId'];
    $isNewConversation = ! $conversationId;
    
    if ($messageText) {
        $messageText = trim($messageText);
    }

    if ($isNewConversation) {
        $conversationId = Uuid::uuid4()->toString();
        $relationData = getRelationData($userId, $relationId);
        $isSlave = $relationData['isSlave'];
        $subject = substr($messageText, 0, 100);
        $replyPolicy = substr($relationData['slavePermissions'], 3, 1);
    }
    else {
        $conversationData = getConversationData($userId, $relationId, $conversationId);
        $isSlave = $conversationData['isSlave'];
        $subject = $conversationData['subject'];
        $replyPolicy = substr($conversationData['slavePermissions'], 3, 1);
    }
    
    $conn = getDbConnection();
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    if ($lastOwnMessageId) {
        $stmt = $conn->prepare("DELETE FROM dsm_message WHERE conversation_id = ? and (id = ? or id = ?)");
        $stmt->bind_param("sss", $conversationId, $lastOwnMessageId, $lastAiMessageId);
        $stmt->execute();
        $stmt -> close();
        sendAdminMessage($conn, $username, $password, $relationId, "MESSAGE_DELETED", [
            'conversationId' => $conversationId,
            'messageId' => $lastAiMessageId
        ]);
        sendAdminMessage($conn, $username, $password, $relationId, "MESSAGE_DELETED", [
            'conversationId' => $conversationId,
            'messageId' => $lastOwnMessageId
        ]);
    }

    $currentDateTime = new DateTime();
    $mysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);

    $messageId = Uuid::uuid4()->toString();
    if ($messageText) {
        if ($isNewConversation) {
            $conversationFlags = $replyPolicy ? $replyPolicy . "00" : "000";
            $stmt = $conn->prepare("INSERT INTO dsm_conversation (id, relation_id, subject, flags, lasttimestamp, prepared_message) values (?, ?, ?, ?, ?, '')");
            $stmt->bind_param("sisss", $conversationId, $relationId, $subject, $conversationFlags, $mysqlTimestamp);
            $stmt->execute();
            $stmt->close();
        }
        else {
            $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ?, prepared_message = '' where id = ?");
            $stmt->bind_param("ss", $mysqlTimestamp, $conversationId);
            $stmt->execute();
            $stmt->close();
        }
        $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
        $stmt->bind_param("ssiss", $messageId, $conversationId, $userId, $messageText, $mysqlTimestamp);
        $stmt->execute();
        $stmt->close();
    }
    
    $aiPolicy = 0;
    if (! $isSlave) {
        $aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);
        if ($aiRelation) {
            $aiPolicy = $aiRelation['aiPolicy'];
        }
    }

    session_write_close();
    ob_start();
    // Redirect back to the chat
    echo "Dummy response";
    if ($isNewConversation && ! $messageText) {
        header("Location: conversations.php?relationId=" . $relationId);
    }
    else if (($aiPolicy == 2 || $aiPolicy == 3 || $aiPolicy == 4) && $messageText) {
        header("Location: messages2.php?relationId=" . $relationId . "&conversationId=" . $conversationId);
    }
    else {
        header("Location: messages.php?relationId=" . $relationId . "&conversationId=" . $conversationId);
    }
    header('Connection: close');
    header('Content-Length: ' . ob_get_length());
    ob_end_flush();
    @ob_flush();
    flush();
    if (function_exists('fastcgi_finish_request')) {
        // Finish the request and flush all response data to the client
        fastcgi_finish_request();
    }

    $responseMessage = "";
    
    if ($messageText && ($aiPolicy == 1 || $aiPolicy == 4)) {
        $result = handleOpenAi($username, $password, $relationId, $conversationId, $aiRelation);
        if ($result['success']) {
            $responseMessage = $result['message']['content'];
        }
        else {
            $responseMessage = "System error - please retry later - " . $result['error']['message'];
        }
        
        $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? where id = ?");
        $stmt->bind_param("ss", $responseMessage, $conversationId);
        $stmt->execute();
        $stmt->close();
    }

    if ($aiPolicy != 3) {
        $tokens = getUnmutedTokens($conn, $username, $password, $relationId);
        
        $data = getTextData($relationId, "TEXT", $messageText, $conversationId, $mysqlTimestamp, $messageId, $responseMessage);
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data);
        }
        $data = getTextData($relationId, "TEXT_OWN", $messageText, $conversationId, $mysqlTimestamp, $messageId, $responseMessage);
        $tokens = getUnmutedSelfTokens($conn, $username, $password, - 1);
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data, null);
        }
    }
 
    $conn->close();
}
?>
