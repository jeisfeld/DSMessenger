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

$clientToken = @$_POST['clientToken'];

$deviceId = null;
$deviceName = null;
$token = null;
$stmt = $conn->prepare("SELECT id, name, token FROM dsm_device WHERE user_id = ?");

$stmt->bind_param("i", $userid);
$stmt->execute();
$stmt->bind_result($deviceId, $deviceName, $token);

$devices = array();
while ($stmt->fetch()) {
    $devices[] = [
        'deviceId' => $deviceId,
        'deviceName' => $deviceName,
        'isClient' => $clientToken === $token ? true : false
    ];
}
printSuccess("Devices of user " . $username . " have been retrieved.", [
    'devices' => $devices
]);

$conn->close();
