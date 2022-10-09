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

$stmt = $conn->prepare("SELECT id FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();

if ($stmt->get_result()->num_rows) {
    printError(113, "Username " . $username . " already exists", [
        "username" => $username
    ]);
}
$stmt->close();

$stmt = $conn->prepare("INSERT INTO dsm_user (username, password) VALUES (?, ?)");
$stmt->bind_param("ss", $username, $hashedpassword);

if ($stmt->execute()) {
    $token = @$_POST['token'];
    $userId = verifyCredentials($conn, $username, $password);
    
    if (!$userId) {
        printError(102, "Failed to retrieve userId");
    }
    if ($token) {
        $stmt = $conn->prepare("INSERT INTO dsm_device (user_id, name, token) values (?, 'Device 1', ?)");
        $stmt->bind_param("is", $userId, $token);
        $stmt->execute();
        $stmt->close();
    }
    else {
        $stmt = $conn->prepare("INSERT INTO dsm_device (user_id, name) values (?, 'Device 1')");
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $stmt->close();
    }
    $deviceId = null;
    $stmt = $conn->prepare("SELECT id FROM dsm_device WHERE user_id=? AND NAME='Device 1'");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $stmt->bind_result($deviceId);
    $stmt->fetch();
    $stmt->close();
    if (!$deviceId) {
        printError(102, "Failed to retrieve deviceId");
    }
    
    printSuccess("User " . $username . " successfully created.", ['userId' => $userId, 'deviceId' => $deviceId]);
}
else {
    $stmt->close();
    printError(102, "Failed to create user");
}

$conn->close();
