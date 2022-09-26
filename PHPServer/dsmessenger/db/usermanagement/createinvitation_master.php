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

$verificationcode = uniqid('id', true);
$slaveid = getUserSlaveId($conn, $username, $password);

$stmt = $conn->prepare("INSERT INTO dsm_user_master (slave_id, verificationcode) VALUES (?, ?)");
$stmt->bind_param("is", $slaveid, $verificationcode);
if ($stmt->execute()) {
    printSuccess("Invitation for user " . $username . " successfully created.", array("verificationcode" => $verificationcode));
}
else {
    $stmt->close();
    printError(102, "Failed to create invitation");
}

$conn->close();
