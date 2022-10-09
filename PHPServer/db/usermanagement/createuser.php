<?php
require_once '../dbfunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
if (! $username) {
    printError(111, "Missing username");
}
$password = @$_POST['password'];
if (! $password || strlen($password) < 8) {
    printError(112, "Password must have length at least 8");
}

$hashedpassword = password_hash($password, PASSWORD_BCRYPT);

$email = @$_POST['email'];
$token = @$_POST['token'];

$stmt = $conn->prepare("SELECT id FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();

if ($stmt->get_result()->num_rows) {
    printError(113, "Username " . $username . " already exists", [
        "username" => $username
    ]);
}
$stmt->close();

$stmt = $conn->prepare("INSERT INTO dsm_user (username, password, email, token) VALUES (?, ?, ?, ?)");
$stmt->bind_param("ssss", $username, $hashedpassword, $email, $token);
if ($stmt->execute()) {
    printSuccess("User " . $username . " successfully created.");
}
else {
    $stmt->close();
    printError(102, "Failed to create user");
}

$conn->close();
