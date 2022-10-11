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

$deviceId = @$_POST['deviceId'];
$deviceName = @$_POST['deviceName'];
$clientDeviceId = @$_POST['clientDeviceId'];

$stmt = $conn->prepare("update dsm_device set name = ? where id = ? and user_id = ?");
$stmt->bind_param("sii", $deviceName, $deviceId, $userId);

if ($stmt->execute()) {
    printSuccess("Device successfully updated");

    $tokens = getSelfTokens($conn, $username, $password, $clientDeviceId);
    $data = [
        'messageType' => 'ADMIN',
        'adminType' => 'DEVICE_UPDATED',
        'messageTime' => $messageTime
    ];
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data);
    }
}
else {
    printError(102, "Failed to update device");
}

$conn->close();
