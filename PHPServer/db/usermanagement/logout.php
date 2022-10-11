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
$clientDeviceId = @$_POST['clientDeviceId'];
$deviceToken = getDeviceToken($conn, $username, $password, $deviceId);

$stmt = $conn->prepare("DELETE FROM dsm_device WHERE id = ? and user_id = ?");
$stmt->bind_param("ii", $deviceId, $userId);

if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully logged out.");

    $tokens = getSelfTokens($conn, $username, $password, $clientDeviceId);
    $data = [
        'messageType' => 'ADMIN',
        'adminType' => 'DEVICE_DELETED',
        'messageTime' => $messageTime
    ];

    foreach ($tokens as $token) {
        if ($token != $deviceToken) {
            sendFirebaseMessage($token, $data);
        }
    }

    if ($deviceToken && $deviceId != $clientDeviceId) {
        sendFirebaseMessage($deviceToken, [
            'messageType' => 'ADMIN',
            'adminType' => 'DEVICE_LOGGED_OUT',
            'messageTime' => $messageTime
        ]);
    }
}
else {
    $stmt->close();
    printError(102, "Failed to logout.");
}

$conn->close();
