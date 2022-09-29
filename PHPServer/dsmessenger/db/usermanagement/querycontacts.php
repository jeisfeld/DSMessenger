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

$userid = getUserId($conn, $username, $password);

$relationId = null;
$connectionCode = null;
$contactName = null;
$isSlave = null;
$stmt = $conn->prepare("SELECT id, connection_code, master_name, false as is_slave FROM dsmessenger.dsm_relation WHERE slave_id = ?
UNION
SELECT id, connection_code, slave_name, true as is_slave FROM dsmessenger.dsm_relation WHERE master_id = ?");

$stmt->bind_param("ss", $userid, $userid);
$stmt->execute();
$stmt->bind_result($relationId, $connectionCode, $contactName, $isSlave);

$contacts = array();
while ($stmt->fetch()) {
    $contacts[] = ['relationId' => $relationId, 'connectionCode' => $connectionCode, 'contactName' => $contactName, 'isSlave' => $isSlave ? true : false ];
}
printSuccess("Contacts of user ".$username." have been retrieved.", ['contacts' => $contacts]);

$conn->close();
