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
$muted = @$_POST['muted'] ? 1 : 0;
$displayStrategyNormal = @$_POST['displayStrategyNormal'];
$displayStrategyUrgent = @$_POST['displayStrategyUrgent'];
$deviceName = @$_POST['deviceName'];
$clientDeviceId = @$_POST['clientDeviceId'];

if ($muted) {
    $deviceCount = null;
    $stmt = $conn->prepare("select count(*) from dsm_device where id <> ? and user_id = ? and muted = 0");
    $stmt->bind_param("ii", $deviceId, $userId);
    $stmt->execute();
    $stmt->bind_result($deviceCount);
    $stmt->fetch();
    if (!$deviceCount) {
        printError(102, "Cannot mute device - no other device is unmuted");
    }
    $stmt->close();
}

$stmt = $conn->prepare("update dsm_device set name = ?, muted = ?, displaystrategy_normal = ?, displaystrategy_urgent = ? where id = ? and user_id = ?");
$stmt->bind_param("sissii", $deviceName, $muted, $displayStrategyNormal, $displayStrategyUrgent, $deviceId, $userId);

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
