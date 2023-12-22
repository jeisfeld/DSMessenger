<?php
require_once 'firebasefunctions.php';
header('Content-Type: text/json');

$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$deviceId = @$_POST['deviceId'];

$tokens = getSelfTokens($conn, $username, $password, $deviceId);

$data = [];

foreach ($_POST as $key => $value) {
    if ($key !== 'username' && $key !== 'password' && $key !== 'deviceId') {
        $data[$key] = $value;
    }
}

$data2 = [];
$i = 1;

foreach ($tokens as $token) {
    sendFirebaseMessage($token, $data);
    $data2[$i++] = $token;
}


printSuccess("Message successfully sent", $data2);

?>
