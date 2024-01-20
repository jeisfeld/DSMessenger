<?php
require_once '../../firebase/firebasefunctions.php';
require_once 'querymessagefunctions.php';
require_once '../../openai/queryopenai.php';
use Ramsey\Uuid\Uuid;

// Create connection
$conn = getDbConnection();
header('Content-Type: text/json');

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = $_POST['username'];
$password = $_POST['password'];
$userId = verifyCredentialsAndRelation($conn, $_POST);
$messageText = @$_POST['messageText'];
$messageId = @$_POST['messageId'];
$isSlave = @$_POST['isSlave'];
$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];
$contactId = @$_POST['contactId'];
$conversationFlags = @$_POST['conversationFlags'];
$timestamp = @$_POST['timestamp'];
$mysqltimestamp = convertJavaTimestamp($timestamp);
$messageIds = @$_POST['messageIds'];
$lastAiMessageId = @$_POST['lastAiMessageId'];
$lastOwnMessageId = @$_POST['lastOwnMessageId'];


$stmt = $conn->prepare("SELECT id FROM dsm_conversation WHERE id = ?");
$stmt->bind_param("s", $conversationId);
$stmt->execute();
$isNewConversation = ! ($stmt->get_result()->num_rows);
$stmt->close();

if ($messageText) {
    if ($isNewConversation) {
        $subject = substr($messageText, 0, 100);
        $stmt = $conn->prepare("INSERT INTO dsm_conversation (id, relation_id, subject, flags, lasttimestamp, prepared_message) values (?, ?, ?, ?, ?, '')");
        $stmt->bind_param("sisss", $conversationId, $relationId, $subject, $conversationFlags, $mysqltimestamp);
        $stmt->execute();
        $stmt->close();
    }
    else {
        $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ?, prepared_message = '' where id = ?");
        $stmt->bind_param("ss", $mysqltimestamp, $conversationId);
        $stmt->execute();
        $stmt->close();
    }
    $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
    $stmt->bind_param("ssiss", $messageId, $conversationId, $userId, $messageText, $mysqltimestamp);
    $stmt->execute();
    $stmt->close();
}

if ($messageIds) {
    $messageIdArr = explode(",", $messageIds);
    foreach ($messageIdArr as $msgId) {
        $stmt = $conn->prepare("UPDATE dsm_message SET status = 2 where id = ? and conversation_id = ?");
        $stmt->bind_param("ss", $msgId, $conversationId);
        $stmt->execute();
        $stmt->close();
    }
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

session_write_close();
ob_start();
printSuccess("Message successfully sent");
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
$aiPolicy = 0;

if (! $isSlave) {
    $aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);
    if ($aiRelation) {
        $aiPolicy = $aiRelation['aiPolicy'];
    }
    if ($messageText && $aiPolicy > 0) {
        $result = handleOpenAi($username, $password, $relationId, $conversationId, $aiRelation);
        if ($result['success']) {
            $responseMessage = $result['message']['content'];
        }
        else {
            $aiPolicy = 0;
        }
    }
}

if ($responseMessage && $aiPolicy == 1) {
    $preparedMessage = $responseMessage;
    $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? where id = ?");
    $stmt->bind_param("ss", $responseMessage, $conversationId);
    $stmt->execute();
    $stmt->close();
}

if ($aiPolicy != 3) {
    $tokens = getVerifiedTokensFromRequestData();
    $data = [];
    $ttl = null;
    foreach ($_POST as $key => $value) {
        if ($key == 'ttl') {
            $ttl = $value;
        }
        elseif ($key !== 'username' && $key !== 'password' && $key !== 'contactId' && $key !== 'isSlave' && $key !== 'isConnected') {
            $data[$key] = $value;
        }
    }
    $data['preparedMessage'] = $preparedMessage;
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, $ttl);
    }
}

if ($responseMessage && ($aiPolicy == 2 || $aiPolicy == 3)) {
    $responseMessageId = Uuid::uuid4()->toString();
    $currentDateTime = new DateTime();
    $responseMysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);
    $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
    $stmt->bind_param("ssiss", $responseMessageId, $conversationId, $contactId, $responseMessage, $responseMysqlTimestamp);
    $stmt->execute();
    $stmt->close();
    $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ?, prepared_message = '' where id = ?");
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
}

if ($responseMessage && $aiPolicy == 2) {
    $tokens = getVerifiedTokensFromRequestData();
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

?>
