<?php
require_once '../../firebase/firebasefunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

verifyCredentialsAndRelation($conn, $_POST);
$adminType = @$_POST['adminType'];
$conversationId = @$_POST['conversationId'];
$conversationFlags = @$_POST['conversationFlags'];
$subject = @$_POST['subject'];
$preparedMessage = @$_POST['preparedMessage'];
$relationId = @$_POST['relationId'];
$archived = intval(@$_POST['archived'] == 'true');

// There are two variants: either set prepared_message, or set subject and flags
if ($adminType === "MESSAGE_PREPARED") {
    $stmt = $conn->prepare("UPDATE dsm_conversation SET prepared_message = ? WHERE id = ? and relation_id = ?");
    $stmt->bind_param("ssi", $preparedMessage, $conversationId, $relationId);
    $stmt->execute();
    $stmt->close();
}
else {
    $stmt = $conn->prepare("UPDATE dsm_conversation SET subject = ?, flags = ?, archived = ? WHERE id = ? and relation_id = ?");
    $stmt->bind_param("ssisi", $subject, $conversationFlags, $archived, $conversationId, $relationId);
    $stmt->execute();
    $stmt->close();
    
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
    
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, $ttl);
    }
    
    printSuccess("Message successfully sent");
}
$conn->close();


?>
