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
verifyCredentials($conn, $username, $password);

$stmt = $conn->prepare("DELETE FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);

if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully deleted.");
}
else {
    $stmt->close();
    printError(102, "Failed to delete user.");
}

$conn->close();
