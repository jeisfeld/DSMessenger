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
$userid = verifyCredentials($conn, $username, $password);

$clientToken = @$_POST['clientToken'];

$deviceId = null;
$deviceName = null;
$token = null;
$muted = null;
$displayStrategyNormal = null;
$displayStrategyUrgent = null;
$stmt = $conn->prepare("SELECT id, name, token, muted, displaystrategy_normal, displaystrategy_urgent FROM dsm_device WHERE user_id = ?");

$stmt->bind_param("i", $userid);
$stmt->execute();
$stmt->bind_result($deviceId, $deviceName, $token, $muted, $displayStrategyNormal, $displayStrategyUrgent);

$devices = array();
while ($stmt->fetch()) {
    $devices[] = [
        'deviceId' => $deviceId,
        'deviceName' => $deviceName,
        'muted' => $muted ? true : false,
        'displayStrategyNormal' => $displayStrategyNormal,
        'displayStrategyUrgent' => $displayStrategyUrgent,
        'isClient' => $clientToken === $token ? true : false
    ];
}
printSuccess("Devices of user " . $username . " have been retrieved.", [
    'devices' => $devices
]);

$conn->close();
