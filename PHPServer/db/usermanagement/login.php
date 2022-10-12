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

$token = @$_POST['token'];
$deviceName = @$_POST['deviceName'];
$messageTime = @$_POST['messageTime'];

if ($deviceName) {
    $deviceId = null;
    $stmt = $conn->prepare("SELECT id FROM dsm_device WHERE user_id = ? and name = ?");
    $stmt->bind_param("is", $userId, $deviceName);
    $stmt->execute();
    $stmt->bind_result($deviceId);
    $stmt->fetch();
    $stmt->close();
    if ($deviceId) {
        printError(116, "Device " . $deviceName . " already exists.", [
            "deviceName" => $deviceName
        ]);
    }
}
else {
    $deviceId = null;
    $stmt = $conn->prepare("SELECT id FROM dsm_device WHERE user_id = ?");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $stmt->bind_result($deviceId);
    $stmt->fetch();
    $stmt->close();
    if ($deviceId) {
        printError(115, "User " . $username . " is logged in on another device.", [
            "username" => $username
        ]);
    }
}

if (! $deviceName) {
    $deviceName = "Device 1";
}

$stmt = $conn->prepare("INSERT INTO dsm_device (user_id, name, token) values (?, ?, ?)");
$stmt->bind_param("iss", $userId, $deviceName, $token);

if ($stmt->execute()) {
    $displayStrategyNormal = null;
    $displayStrategyUrgent = null;
    $stmt = $conn->prepare("SELECT id, displaystrategy_normal, displaystrategy_urgent FROM dsm_device WHERE user_id=? AND name=?");
    $stmt->bind_param("is", $userId, $deviceName);
    $stmt->execute();
    $stmt->bind_result($deviceId, $displayStrategyNormal, $displayStrategyUrgent);
    $stmt->fetch();
    $stmt->close();
    if (! $deviceId) {
        printError(102, "Failed to retrieve deviceId");
    }

    printSuccess("User " . $username . " successfully logged in.", [
        'userId' => $userId,
        'deviceId' => $deviceId,
        'deviceName' => $deviceName,
        'muted' => false,
        'displayStrategyNormal' => $displayStrategyNormal,
        'displayStrategyUrgent' => $displayStrategyUrgent
    ]);

    $tokens = getSelfTokens($conn, $username, $password, $deviceId);
    $data = [
        'messageType' => 'ADMIN',
        'adminType' => 'DEVICE_ADDED',
        'messageTime' => $messageTime
    ];
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data);
    }
}
else {
    $stmt->close();
    printError(102, "Failed to login user.");
}

$conn->close();
