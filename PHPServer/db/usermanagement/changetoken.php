<?php
require_once '../dbfunctions.php';

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
$deviceId = @$_POST['deviceId'];

$stmt = $conn->prepare("UPDATE dsm_device SET token = ? WHERE id = ? AND user_id = ?");
$stmt->bind_param("sii", $token, $deviceId, $userId);

if ($stmt->execute()) {
    printSuccess("Token from user " . $username . " successfully updated.");
}
else {
    $stmt->close();
    printError(102, "Failed to update user token.");
}

$conn->close();
