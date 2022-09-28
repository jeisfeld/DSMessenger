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
$isSlave = @$_POST['is_slave'];
$myName = @$_POST['myname'];
$contactName = @$_POST['contactname'];

if ($isSlave) {
    $connectioncode = uniqid('m', true);
    $stmt = $conn->prepare("INSERT INTO dsm_relation (slave_id, connection_code, slave_name, master_name) VALUES (?, ?, ?, ?)");
}
else {
    $connectioncode = uniqid('s', true);
    $stmt = $conn->prepare("INSERT INTO dsm_relation (master_id, connection_code, master_name, slave_name) VALUES (?, ?, ?, ?)");
}

$stmt->bind_param("isss", $slaveid, $connectioncode, $myName, $contactName);
if ($stmt->execute()) {
    printSuccess("Invitation for user " . $username . " successfully created.", array("connectioncode" => $connectioncode));
}
else {
    $stmt->close();
    printError(102, "Failed to create invitation");
}

$conn->close();
