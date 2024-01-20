<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
require_once 'db/conversation/querymessagefunctions.php';
require_once 'openai/queryopenai.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];
$isSlave = @$_POST['isSlave'];
$aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);

if ($aiRelation['aiPolicy'] == 1 && $isSlave) {
    $result = handleOpenAi($username, $password, $relationId, $conversationId, $aiRelation);
    $preparedMessage = $result['success'] ? $result['message']['content'] : "";
    echo $preparedMessage;
    
    $currentDateTime = new DateTime();
    $mysqlTimestamp = substr($currentDateTime->format("Y-m-d H:i:s.u"), 0, 23);
    
    $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ?, lasttimestamp = ? WHERE id = ? and relation_id = ?");
    $stmt->bind_param("sssi", $preparedMessage, $mysqlTimestamp, $conversationId, $relationId);
    $stmt->execute();
    $stmt->close();
    sendAdminMessage($conn, $username, $password, $relationId, "CONVERSATION_EDITED", [
        'conversationId' => $conversationId,
        'preparedMessage' => $preparedMessage
    ]);
}

$conn -> close();

?>
