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

$stmt = $conn->prepare("DELETE FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);

if ($stmt->execute()) {
    echo "Success: User " . $username . " successfully deleted.";
}
else {
    $stmt->close();
    die("Error: Failed to delete user.");
}

$conn->close();
