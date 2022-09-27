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

$slaveid = getUserId($conn, $username, $password);
$isSlave = @$_POST['isSlave'];

if ($isSlave) {
    $connectioncode = uniqid('m', true);
    $stmt = $conn->prepare("INSERT INTO dsm_relation (slave_id, connection_code, wait_verification_by) VALUES (?, ?, false)");
}
else {
    $connectioncode = uniqid('s', true);
    $stmt = $conn->prepare("INSERT INTO dsm_relation (master_id, connection_code, wait_verification_by) VALUES (?, ?, true)");
}

$stmt->bind_param("is", $slaveid, $connectioncode);
if ($stmt->execute()) {
    printSuccess("Invitation for user " . $username . " successfully created.", array("connection_code" => $connectioncode));
}
else {
    $stmt->close();
    printError(102, "Failed to create invitation");
}

$conn->close();
