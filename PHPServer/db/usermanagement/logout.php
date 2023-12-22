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

$deviceId = @$_POST['deviceId'];
$clientDeviceId = @$_POST['clientDeviceId'];
$deviceToken = getDeviceToken($conn, $username, $password, $deviceId);

$stmt = $conn->prepare("DELETE FROM dsm_device WHERE id = ? and user_id = ?");
$stmt->bind_param("ii", $deviceId, $userId);

if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully logged out.");
    $stmt->close();
    
    // If there are devices left and all are muted, then unmute one of them.
    $deviceCount = null;
    $stmt = $conn->prepare("select count(*) from dsm_device where user_id = ?");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $stmt->bind_result($deviceCount);
    $stmt->fetch();
    $stmt->close();
    if ($deviceCount) {
        $unmutedCount = null;
        $stmt = $conn->prepare("select count(*) from dsm_device where user_id = ? and muted = 0");
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $stmt->bind_result($unmutedCount);
        $stmt->fetch();
        $stmt->close();
        if (!$unmutedCount) {
            $newDeviceId = null;
            $stmt = $conn->prepare("select min(id) from dsm_device where user_id = ?");
            $stmt->bind_param("i", $userId);
            $stmt->execute();
            $stmt->bind_result($newDeviceId);
            $stmt->fetch();
            $stmt->close();
            if ($newDeviceId) {
                $stmt = $conn->prepare("update dsm_device set muted = 0 where user_id = ? and id = ?");
                $stmt->bind_param("ii", $userId, $newDeviceId);
                $stmt->execute();
            }
        }
    }

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
