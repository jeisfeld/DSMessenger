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

$deviceId = @$_POST['deviceId'];

$stmt = $conn->prepare("DELETE FROM dsm_device WHERE id = ? and user_id = ?");
$stmt->bind_param("ii", $deviceId, $userId);

if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully logged out.");
}
else {
    $stmt->close();
    printError(102, "Failed to logout.");
}

$conn->close();
