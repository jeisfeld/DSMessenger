<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once 'db/conversation/queryairelation.php';
require_once 'db/conversation/querymessagesforopenai.php';
require_once 'openai/queryopenai.php';
use Ramsey\Uuid\Uuid;
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $conversationId = $_POST['conversationId'];
    $relationId = $_POST['relationId'];
    $isSlave = $_POST['isSlave'];
    $subject = $_POST['subject'];
    $contactName = $_POST['contactName'];
    $message = $_POST['message'];
    $replyPolicy = $_POST['replyPolicy'];
    $contactId = $_POST['contactId'];
    $isNewConversation = ! $conversationId;

    $conn = getDbConnection();
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    getRelationData($conn, $userId, $relationId);

    $currentDateTime = new DateTime();
    $mysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);

    $messageId = Uuid::uuid4()->toString();
    if ($isNewConversation) {
        $conversationId = Uuid::uuid4()->toString();
    }
    if ($message) {
        if ($isNewConversation) {
            $subject = substr($message, 0, 100);
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
        $stmt->bind_param("ssiss", $messageId, $conversationId, $userId, $message, $mysqlTimestamp);
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
    if ($isNewConversation && ! $message) {
        header("Location: conversations.php?relationId=" . $relationId);
    }
    else if ($aiPolicy == 2 || $aiPolicy == 3) {
        header("Location: messages2.php?conversationId=" . $conversationId . "&relationId=" . $relationId . "&isSlave=" . $isSlave . "&subject=" . $subject . "&contactName=" . $contactName . "&contactId=" . $contactId . "&replyPolicy=" . $replyPolicy);
    }
    else {
        header("Location: messages.php?conversationId=" . $conversationId . "&relationId=" . $relationId . "&isSlave=" . $isSlave . "&subject=" . $subject . "&contactName=" . $contactName . "&contactId=" . $contactId . "&replyPolicy=" . $replyPolicy);
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

    $preparedMessage = "";
    $responseMessage = "";

    
    if ($message && $aiPolicy == 1) {
        $messages = queryMessagesForOpenai($username, $password, $relationId, $conversationId, $aiRelation['promptmessage'], $aiRelation['oldMessageCount'], $aiRelation['oldMessageCountVariation'], $aiRelation['maxCharacters']);

        $result = queryOpenAi($messages, $aiRelation['temperature'], $aiRelation['presencePenalty'], $aiRelation['frequencyPenalty']);
        if ($result['success']) {
            $responseMessage = $result['message']['content'];
        }
        else {
            $aiPolicy = 0;
        }
        
        $preparedMessage = $responseMessage;
        $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? where id = ?");
        $stmt->bind_param("ss", $responseMessage, $conversationId);
        $stmt->execute();
        $stmt->close();
    }

    if ($aiPolicy != 3) {
        $tokens = getUnmutedTokens($conn, $username, $password, $relationId, $isSlave);
        $currentDateTime = new DateTime();
        $data = [
            "messageType" => "TEXT",
            "messageText" => $message,
            "priority" => "NORMAL",
            "conversationId" => $conversationId,
            "timestamp" => convertToJavaTimestamp($mysqlTimestamp),
            "messageId" => $messageId,
            "username" => $username,
            "password" => $password,
            "relationId" => $relationId,
            "isSlave" => $isSlave,
            "preparedMessage" => $preparedMessage,
            "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
        ];
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data, null);
        }
        $data["messageType"] = "TEXT_OWN";
        $tokens = getSelfTokens($conn, $username, $password, - 1);
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data, null);
        }
    }
 
    $conn->close();
}
?>
