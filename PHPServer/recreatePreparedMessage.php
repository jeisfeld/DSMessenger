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

if ($isSlave && ($aiRelation['aiPolicy'] == 1 || $aiRelation['aiPolicy'] == 4)) {
    $result = handleOpenAi($username, $password, $relationId, $conversationId, $aiRelation);
    $preparedMessage = $result['success'] ? $result['message']['content'] : "";
    echo $preparedMessage;
    
    $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? WHERE id = ? and relation_id = ?");
    $stmt->bind_param("ssi", $preparedMessage, $conversationId, $relationId);
    $stmt->execute();
    $stmt->close();
    sendAdminMessage($conn, $username, $password, $relationId, "CONVERSATION_EDITED", [
        'conversationId' => $conversationId,
        'preparedMessage' => $preparedMessage
    ]);
}

$conn -> close();

?>
