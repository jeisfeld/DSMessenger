<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$userId = verifyCredentials($conn, $username, $password);

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

$stmt->bind_param("isss", $userId, $connectioncode, $myName, $contactName);
if ($stmt->execute()) {
    $stmt->close();
    $relationId = null;
    $stmt = $conn->prepare("SELECT id FROM dsm_relation WHERE connection_code = ?");
    $stmt->bind_param("s", $connectioncode);
    $stmt->execute();
    $stmt->bind_result($relationId);
    $stmt->fetch();
    $stmt->close();
    if (!$relationId) {
        printError(102, "Failed to retrieve id of invitation");
    }
    printSuccess("Invitation for user " . $username . " successfully created.", 
        array("connectionCode" => $connectioncode, "relationId" => $relationId));
}
else {
    $stmt->close();
    printError(102, "Failed to create invitation");
}

$conn->close();
