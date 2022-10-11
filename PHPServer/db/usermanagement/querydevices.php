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

$deviceId = null;
$deviceName = null;
$stmt = $conn->prepare("SELECT id, name FROM dsm_device WHERE user_id = ?");

$stmt->bind_param("i", $userid);
$stmt->execute();
$stmt->bind_result($deviceId, $deviceName);

$devices = array();
while ($stmt->fetch()) {
    $devices[] = [
        'deviceId' => $deviceId,
        'deviceName' => $deviceName,
    ];
}
printSuccess("Devices of user " . $username . " have been retrieved.", [
    'devices' => $devices
]);

$conn->close();
