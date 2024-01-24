<?php
require_once '../../firebase/firebasefunctions.php';
require_once 'querymessagefunctions.php';
require_once '../../openai/queryopenai.php';

// Create connection
$conn = getDbConnection();
header('Content-Type: text/json');

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = $_POST['username'];
$password = $_POST['password'];
verifyCredentialsAndRelation($conn, $_POST);
$isSlave = @$_POST['isSlave'];
$conversationId = @$_POST['conversationId'];
$relationId = @$_POST['relationId'];

$aiRelation = queryAiRelation($username, $password, $relationId, $isSlave);

if ($isSlave && ($aiRelation['aiPolicy'] == 1 || $aiRelation['aiPolicy'] == 4)) {
    $result = handleOpenAi($username, $password, $relationId, $conversationId, $aiRelation);
    $preparedMessage = $result['success'] ? $result['message']['content'] : "";

    printSuccess("Message successfully sent", [
        "preparedMessage" => $preparedMessage
    ]);
    
    $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? WHERE id = ? and relation_id = ?");
    $stmt->bind_param("ssi", $preparedMessage, $conversationId, $relationId);
    $stmt->execute();
    $stmt->close();
    sendAdminMessage($conn, $username, $password, $relationId, "CONVERSATION_EDITED", [
        'conversationId' => $conversationId,
        'preparedMessage' => $preparedMessage
    ]);
}

$conn->close();

?>
