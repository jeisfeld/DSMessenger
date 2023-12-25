<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
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
    $isNewConversation = !$conversationId;
    
    $conn = getDbConnection();
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    $userId = verifyCredentials($conn, $username, $password);
    
    $currentDateTime = new DateTime();
    $mysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);
    
    if ($isNewConversation) {
        $conversationId = Uuid::uuid4()->toString();
        $subject = $message;
        $stmt = $conn->prepare("INSERT INTO dsm_conversation (id, relation_id, subject, flags, lasttimestamp) values (?, ?, ?, '000', ?)");
        $stmt->bind_param("siss", $conversationId, $relationId, $subject, $mysqlTimestamp);
        $stmt->execute();
        $stmt->close();
    }
    else {
        $stmt = $conn->prepare("UPDATE dsm_conversation SET lasttimestamp = ? where id = ?");
        $stmt->bind_param("ss", $mysqlTimestamp, $conversationId);
        $stmt->execute();
        $stmt->close();
    }

    $messageId = Uuid::uuid4()->toString();
    $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, ?, 0)");
    $stmt->bind_param("ssiss", $messageId, $conversationId, $userId, $message, $mysqlTimestamp);
    $stmt->execute();
    $stmt->close();
    
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
        "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
    ];
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
    $data["messageType"] = "TEXT_OWN";
    $tokens = getSelfTokens($conn, $username, $password, -1);
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
    $conn->close();
    

    // Redirect back to the chat
    header("Location: messages.php?conversationId=" . $conversationId . "&relationId=" . $relationId . 
        "&isSlave=" . $isSlave . "&subject=" . $subject . "&contactName=" . $contactName);
}
?>
