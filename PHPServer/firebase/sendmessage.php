<?php
require_once 'firebasefunctions.php';

$tokens = getVerifiedTokensFromRequestData();

$data = [];

foreach ($_POST as $key => $value) {
    if ($key !== 'username' && $key !== 'password' && $key !== 'contactId' && $key !== 'isSlave' && $key !== 'isConnected') {
        $data[$key] = $value;
    }
}

foreach ($tokens as $token) {
    sendFirebaseMessage($token, $data);
}

printSuccess("Message successfully sent");

?>
