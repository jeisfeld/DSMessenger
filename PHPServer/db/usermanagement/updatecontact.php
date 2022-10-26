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
$messageTime = @$_POST['messageTime'];

$relationId = @$_POST['relationId'];
$myName= @$_POST['myName'];
$contactName = @$_POST['contactName'];
$isConnected = @$_POST['isConnected'];
$isSlave = @$_POST['isSlave'];
$slavePermissions = @$_POST['slavePermissions'];

if ($isSlave) {
    $stmt = $conn->prepare("update dsm_relation set master_name = ?, slave_name = ?, slave_permissions = ? where id = ? and master_id = ?");
}
else {
    $stmt = $conn->prepare("update dsm_relation set slave_name = ?, master_name = ?, slave_permissions = ? where id = ? and slave_id = ?");
}

$stmt->bind_param("sssii", $myName, $contactName, $slavePermissions, $relationId, $userId);

if ($stmt->execute()) {
    printSuccess("Contact successfully updated");
    
    if ($isConnected) {
        $tokens = getTokens($conn, $username, $password, $relationId, $isSlave);
        $data = [
            'messageType' => 'ADMIN',
            'adminType' => 'CONTACT_UPDATED',
            'messageTime' => $messageTime
        ];
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, $data);
        }
    }
}
else {
    printError(102, "Failed to update contact");
}

$conn->close();
