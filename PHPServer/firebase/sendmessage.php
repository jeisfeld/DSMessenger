<?php
require_once 'firebasefunctions.php';
header('Content-Type: text/json');

$tokens = getVerifiedTokensFromRequestData();

$data = [];
$ttl = null;

foreach ($_POST as $key => $value) {
    if ($key == 'ttl') {
        $ttl = $value;
    }
    elseif ($key !== 'username' && $key !== 'password' && $key !== 'contactId' && $key !== 'isSlave' && $key !== 'isConnected') {
        $data[$key] = $value;
    }
}

foreach ($tokens as $token) {
    sendFirebaseMessage($token, $data, $ttl);
}

printSuccess("Message successfully sent");

?>
