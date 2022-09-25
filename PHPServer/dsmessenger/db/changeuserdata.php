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

$email = @$_POST['email'];
$token = @$_POST['token'];

$oldemail = null;
$oldtoken = null;
$stmt = $conn->prepare("SELECT email, token FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$stmt->bind_result($oldemail, $oldtoken);
$stmt->fetch();
$stmt->close();

if (! $email) {
    $email = $oldemail;
}
if (! $token) {
    $token = $oldtoken;
}

$stmt = $conn->prepare("UPDATE dsm_user SET email=?, token=? WHERE username = ?");
$stmt->bind_param("sss", $email, $token, $username);

if ($stmt->execute()) {
    echo "Success: Data from user " . $username . " successfully updated.";
}
else {
    $stmt->close();
    die("Error: Failed to update user data.");
}

$conn->close();
