<?php
namespace dsmessenger\db;

include 'dbcredentials.php';
include 'functions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    die("Error: Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
verifyCredentials($conn, $username, $password);

$newpassword = @$_POST['newpassword'];
if (! $newpassword || strlen($newpassword) < 8) {
    die("Error: New password must have length at least 8");
}
$hashedpassword = password_hash($newpassword, PASSWORD_BCRYPT);

$stmt = $conn->prepare("UPDATE dsm_user SET password = ? WHERE username = ?");
$stmt->bind_param("ss", $hashedpassword, $username);

if ($stmt->execute()) {
    echo "Success: Password of user " . $username . " successfully changed.";
}
else {
    $stmt->close();
    die("Error: Failed to change password.");
}

$conn->close();
