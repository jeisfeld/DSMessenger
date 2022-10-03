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

$isSlave = @$_POST['isSlave'];
$myName = @$_POST['myname'];
$contactName = @$_POST['contactname'];
$connectionCode = @$_POST['connectioncode'];
$relationId = @$_POST['relationId'];

if ($isSlave) {
    $stmt = $conn->prepare("UPDATE dsm_relation
SET master_id = ?, master_name = ?, slave_name = ?, connection_code = null
WHERE connection_code = ? and id = ?");
    $stmt->bind_param("isssi", $userId, $myName, $contactName, $connectionCode, $relationId);
}
else {
    $stmt = $conn->prepare("UPDATE dsm_relation
SET slave_id = ?, master_name = ?, connection_code = null
WHERE connection_code = ? and id = ?");
    $stmt->bind_param("issi", $userId, $contactName, $connectionCode, $relationId);
}

if ($stmt->execute()) {
    printSuccess("Invitation successfully accepted");
    
    $token = getToken($conn, $username, $password, $relationId, $isSlave);
    $data = [
        'messageType' => 'ADMIN',
        'adminType' => 'INVITATION_ACCEPTED'
    ];
    sendFirebaseMessage($token, $data);    
}
else {
    printError(102, "Failed to accept invitation");
}

$conn->close();
