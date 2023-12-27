<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once 'db/conversation/queryairelation.php';
require_once 'db/conversation/querymessagesforopenai.php';
require_once 'openai/queryopenai.php';
use Ramsey\Uuid\Uuid;
$username = $_SESSION['username'];
$password = $_SESSION['password'];

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
    $userId = verifyCredentials($conn, $username, $password);
    verifyRelation($conn, $relationId, $userId, $isSlave);

    $currentDateTime = new DateTime();
    $mysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);

    $messageId = Uuid::uuid4()->toString();
    if ($isNewConversation) {
        $conversationId = Uuid::uuid4()->toString();
    }
    if ($message) {
        if ($isNewConversation) {
            $subject = $message;
            $conversationFlags = $replyPolicy ? $replyPolicy . "00" : "000";
            $stmt = $conn->prepare("INSERT INTO dsm_conversation (id, relation_id, subject, flags, lasttimestamp) values (?, ?, ?, ?, ?)");
            $stmt->bind_param("sisss", $conversationId, $relationId, $subject, $conversationFlags, $mysqlTimestamp);
            $stmt->execute();
            $stmt->close();
        }
        else {
            $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ?, prepared_message = null where id = ?");
            $stmt->bind_param("ss", $mysqlTimestamp, $conversationId);
            $stmt->execute();
            $stmt->close();
        }
        $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
        $stmt->bind_param("ssiss", $messageId, $conversationId, $userId, $message, $mysqlTimestamp);
        $stmt->execute();
        $stmt->close();
    }

    session_write_close();
    ob_start();
    // Redirect back to the chat
    echo "Dummy response";
    if ($isNewConversation && ! $message) {
        header("Location: conversations.php?relationId=" . $relationId . "&contactName=" . $contactName . "&contactId=" . $contactId . "&isSlave=" . $isSlave . "&replyPolicy=" . $replyPolicy);
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
    $responseMessage = null;
    $aiPolicy = 0;

    if (! $isSlave) {
        $aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);
        if ($aiRelation) {
            $aiPolicy = $aiRelation['aiPolicy'];
        }
        if ($aiPolicy > 0) {
            $messages = queryMessagesForOpenai($username, $password, $relationId, $conversationId, $aiRelation['promptmessage']);

            $result = queryOpenAi($messages);
            if ($result['success']) {
                $responseMessage = $result['message']['content'];
            }
            else {
                $aiPolicy = 0;
            }
        }
    }

    if ($aiPolicy == 1) {
        $preparedMessage = $responseMessage;
        $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? where id = ?");
        $stmt->bind_param("ss", $responseMessage, $conversationId);
        $stmt->execute();
        $stmt->close();
    }
    
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
        
    if ($aiPolicy == 2) {
        $responseMessageId = Uuid::uuid4()->toString();
        $currentDateTime = new DateTime();
        $responseMysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);

        $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
        $stmt->bind_param("ssiss", $responseMessageId, $conversationId, $contactId, $responseMessage, $responseMysqlTimestamp);
        $stmt->execute();
        $stmt->close();
        $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ?, prepared_message = null where id = ?");
        $stmt->bind_param("ss", $responseMysqlTimestamp, $conversationId);
        $stmt->execute();
        $stmt->close();

        $tokens = getSelfTokens($conn, $username, $password, - 1);
        $currentDateTime = new DateTime();
        $data = [
            "messageType" => "TEXT",
            "messageText" => $responseMessage,
            "priority" => "NORMAL",
            "conversationId" => $conversationId,
            "timestamp" => convertToJavaTimestamp($responseMysqlTimestamp),
            "messageId" => $responseMessageId,
            "relationId" => $relationId,
            "preparedMessage" => "",
            "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
        ];
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data, null);
        }
        
        $tokens = getUnmutedTokens($conn, $username, $password, $relationId, $isSlave);
        $currentDateTime = new DateTime();
        $data = [
            "messageType" => "TEXT_OWN",
            "messageText" => $responseMessage,
            "priority" => "NORMAL",
            "conversationId" => $conversationId,
            "timestamp" => convertToJavaTimestamp($responseMysqlTimestamp),
            "messageId" => $responseMessageId,
            "relationId" => $relationId,
            "preparedMessage" => "",
            "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
        ];
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data, null);
        }
    }


    $conn->close();
}
?>
