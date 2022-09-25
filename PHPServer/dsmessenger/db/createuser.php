<?php
namespace dsmessenger\db;

include 'dbcredentials.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    die("Error: Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
if (! $username) {
    die("Error: Missing username");
}
$password = @$_POST['password'];
if (! $password || strlen($password) < 8) {
    die("Error: Password must have length at least 8");
}

$hashedpassword = password_hash($password, PASSWORD_BCRYPT);

$email = @$_POST['email'];
$token = @$_POST['token'];

$stmt = $conn->prepare("SELECT id FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();

if ($stmt->get_result()->num_rows) {
    die("Error: Username " . $username . " already exists ");
}
$stmt->close();

$stmt = $conn->prepare("INSERT INTO dsm_user (username, password, email, token) VALUES (?, ?, ?, ?)");
$stmt->bind_param("ssss", $username, $hashedpassword, $email, $token);
if ($stmt->execute()) {
    echo "Success: User " . $username . " successfully created.";
}
else {
    $stmt->close();
    die("Error: Failed to create user");
}

$conn->close();
