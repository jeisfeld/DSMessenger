<?php
require_once '../dbfunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
verifyCredentials($conn, $username, $password);

$connectionCode = @$_POST['connectioncode'];
if (! $connectionCode) {
    printError(104, "Missing connection code");
}
$typeString = substr($connectionCode, 0, 1);
if ($typeString == "m") {
    $isSlave = false;
}
elseif ($typeString = "s") {
    $isSlave = true;
}
else {
    printError(106, "Invalid connection code");
}

$relationId = null;
$myName = null;
$contactName = null;
$contactId = null;
$stmt = $conn->prepare($isSlave ? "SELECT id, slave_name, master_name, master_id FROM dsm_relation WHERE connection_code = ?" : "SELECT id, master_name, slave_name, slave_id FROM dsm_relation WHERE connection_code = ?");
$stmt->bind_param("s", $connectionCode);
$stmt->execute();
$stmt->bind_result($relationId, $myName, $contactName, $contactId);
if (! $stmt->fetch()) {
    printError(106, "Invalid Connection Code");
}

printSuccess("Connection data retrieved", [
    'relationId' => $relationId,
    'myname' => $myName,
    'contactname' => $contactName,
    'contactId' => $contactId,
    'isSlave' => $isSlave ? true : false
]);

$conn->close();
