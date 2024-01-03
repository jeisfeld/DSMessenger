<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$messageTime = @$_POST['messageTime'];
$clientDeviceId = @$_POST['clientDeviceId'];
$userId = verifyCredentials($conn, $username, $password);

$newPassword = @$_POST['newPassword'];
if (! $newPassword || strlen($newPassword) < 8) {
    printError(112, "New password must have length at least 8");
}
$hashedpassword = password_hash($newPassword, PASSWORD_BCRYPT);

$stmt = $conn->prepare("UPDATE dsm_user SET password = ? WHERE username = ?");
$stmt->bind_param("ss", $hashedpassword, $username);

if ($stmt->execute()) {
    $stmt -> close();
    
    $stmt = $conn->prepare("delete from dsm_device WHERE user_id = ? and id != ?");
    $stmt->bind_param("ii", $userId, $clientDeviceId);
    $stmt->execute();
    $stmt -> close();
    
    $tokens = getSelfTokens($conn, $username, $password, $clientDeviceId);
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, [
            'messageType' => 'ADMIN',
            'adminType' => 'PASSWORD_CHANGED',
            'messageTime' => $messageTime
        ]);
    }
    
    printSuccess("Password of user " . $username . " successfully changed.");
}
else {
    $stmt->close();
    printError(102, "Failed to change password.");
}

$conn->close();
