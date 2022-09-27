<?php
require_once '../dbcredentials.php';
require_once '../functions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
verifyCredentials($conn, $username, $password);

$token = @$_POST['token'];

$oldtoken = null;
$stmt = $conn->prepare("SELECT token FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$stmt->bind_result($oldtoken);
$stmt->fetch();
$stmt->close();

if ($oldtoken) {
    printError(106, "User " . $username . " is logged in on another device.");
}

$stmt = $conn->prepare("UPDATE dsm_user SET token=? WHERE username = ?");
$stmt->bind_param("ss", $token, $username);

if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully logged in.");
}
else {
    $stmt->close();
    printError(102, "Failed to login user.");
}

$conn->close();
