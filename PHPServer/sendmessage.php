<?php
require_once 'firebase/firebasefunctions.php';
use Ramsey\Uuid\Uuid;

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $conversationId = $_POST['conversationId'];
    $relationId = $_POST['relationId'];
    $isSlave = $_POST['isSlave'];
    $subject = $_POST['subject'];
    $contactName = $_POST['contactName'];
    $message = $_POST['message'];
    $userId = $_POST['userId'];
    
    $conn = getDbConnection();
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    $stmt = $conn->prepare("INSERT INTO dsm_message (id, conversation_id, user_id, text, timestamp, status) values (?, ?, ?, ?, now(), 0)");
    $stmt->bind_param("ssis", Uuid::uuid4()->toString(), $conversationId, $userId, $message);
    $stmt->execute();
    $stmt->close();
    $conn->close();

    // Redirect back to the chat
    header("Location: messages.php?conversationId=" . $conversationId . "&relationId=" . $relationId . 
        "&isSlave=" . $isSlave . "&subject=" . $subject . "&contactName=" . $contactName);
}
?>
