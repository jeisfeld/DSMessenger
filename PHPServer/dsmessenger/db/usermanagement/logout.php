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

$stmt = $conn->prepare("UPDATE dsm_user SET token=null WHERE username = ?");
$stmt->bind_param("s", $username);

if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully logged out.");
}
else {
    $stmt->close();
    printError(102, "Failed to logout.");
}

$conn->close();
