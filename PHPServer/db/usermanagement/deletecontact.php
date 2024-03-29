<?php
require_once '../../firebase/firebasefunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$userId = verifyCredentials($conn, $username, $password);
$messageTime = @$_POST['messageTime'];

$relationId = @$_POST['relationId'];
$isSlave = @$_POST['isSlave'];
$isConnected = @$_POST['isConnected'];

if ($isSlave) {
    $stmt = $conn->prepare("delete from dsm_relation where id = ? and master_id = ?");
}
else {
    $stmt = $conn->prepare("delete from dsm_relation where id = ? and slave_id = ?");
}

$stmt->bind_param("ii", $relationId, $userId);

if ($stmt->execute()) {
    printSuccess("Contact successfully deleted");

    if ($isConnected) {
        $tokens = getTokens($conn, $username, $password, $relationId, $isSlave);
    }
    if (sizeOf($tokens)) {
        $data = [
            'messageType' => 'ADMIN',
            'adminType' => 'CONTACT_DELETED',
            'messageTime' => $messageTime
        ];
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data);
        }
    }
}
else {
    printError(102, "Failed to delete contact");
}

$conn->close();
