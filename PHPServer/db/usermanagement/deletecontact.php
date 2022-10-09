<?php
require_once '../../firebase/firebasefunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$userId = verifyCredentials($conn, $username, $password);

$relationId = @$_POST['relationId'];
$isSlave = @$_POST['isSlave'];
$isConnected = @$_POST['isConnected'];

if ($isConnected) {
    $token = getToken($conn, $username, $password, $relationId, $isSlave);
}

if ($isSlave) {
    $stmt = $conn->prepare("delete from dsm_relation where id = ? and master_id = ?");
}
else {
    $stmt = $conn->prepare("delete from dsm_relation where id = ? and slave_id = ?");
}

$stmt->bind_param("ii", $relationId, $userId);

if ($stmt->execute()) {
    printSuccess("Contact successfully deleted");
    
    if ($token) {
        $data = [
            'messageType' => 'ADMIN',
            'adminType' => 'CONTACT_DELETED'
        ];
        sendFirebaseMessage($token, $data);
    }
}
else {
    printError(102, "Failed to delete contact");
}

$conn->close();
