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
verifyCredentials($conn, $username, $password);

$newpassword = @$_POST['newpassword'];
if (! $newpassword || strlen($newpassword) < 8) {
    printError(112, "New password must have length at least 8");
}
$hashedpassword = password_hash($newpassword, PASSWORD_BCRYPT);

$stmt = $conn->prepare("UPDATE dsm_user SET password = ? WHERE username = ?");
$stmt->bind_param("ss", $hashedpassword, $username);

if ($stmt->execute()) {
    printSuccess("Password of user " . $username . " successfully changed.");
}
else {
    $stmt->close();
    printError(102, "Failed to change password.");
}

$conn->close();
