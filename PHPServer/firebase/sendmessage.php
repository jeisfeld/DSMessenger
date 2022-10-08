<?php
require_once 'firebasefunctions.php';

$token = getVerifiedTokenFromRequestData();

$data = [];

foreach ($_POST as $key => $value) {
    if ($key !== 'username' && $key !== 'password' && $key !== 'contactId' && $key !== 'isSlave' && $key !== 'isConnected') {
        $data[$key] = $value;
    }
}

sendFirebaseMessage($token, $data);

printSuccess("Message successfully sent");

?>
