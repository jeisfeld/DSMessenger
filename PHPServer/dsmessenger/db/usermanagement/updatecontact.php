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
$myName= @$_POST['myName'];
$contactName = @$_POST['contactName'];
$isConnected = @$_POST['isConnected'];
$isSlave = @$_POST['isSlave'];

if ($isSlave) {
    $stmt = $conn->prepare("update dsm_relation set master_name = ?, slave_name = ? where id = ? and master_id = ?");
}
else {
    $stmt = $conn->prepare("update dsm_relation set slave_name = ?, master_name = ? where id = ? and slave_id = ?");
}

$stmt->bind_param("ssii", $myName, $contactName, $relationId, $userId);

if ($stmt->execute()) {
    printSuccess("Contact successfully updated");
    
    if ($isConnected) {
        $token = getToken($conn, $username, $password, $relationId);
        $data = [
            'messageType' => 'ADMIN',
            'adminType' => 'CONTACT_UPDATED'
        ];
        sendFirebaseMessage($token, $data);
    }
}
else {
    printError(102, "Failed to accept invitation");
}

$conn->close();
