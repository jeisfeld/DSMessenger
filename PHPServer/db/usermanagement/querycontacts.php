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
$userid = verifyCredentials($conn, $username, $password);

$relationId = null;
$connectionCode = null;
$contactName = null;
$contactId = null;
$myName = null;
$isSlave = null;
$stmt = $conn->prepare("SELECT id, connection_code, master_name, master_id, slave_name, false as is_slave FROM dsm_relation WHERE slave_id = ?
UNION
SELECT id, connection_code, slave_name, slave_id, master_name, true as is_slave FROM dsm_relation WHERE master_id = ?");

$stmt->bind_param("ii", $userid, $userid);
$stmt->execute();
$stmt->bind_result($relationId, $connectionCode, $contactName, $contactId, $myName, $isSlave);

$contacts = array();
while ($stmt->fetch()) {
    $contacts[] = [
        'relationId' => $relationId,
        'connectionCode' => $connectionCode,
        'contactName' => $contactName,
        'contactId' => $contactId == null ? -1 : $contactId,
        'myName' => $myName,
        'isSlave' => $isSlave ? true : false,
        'isConfirmed' => $contactId ? true : false
    ];
}
printSuccess("Contacts of user " . $username . " have been retrieved.", [
    'contacts' => $contacts
]);

$conn->close();
