<?php
require_once 'firebasefunctions.php';

$token = getVerifiedTokenFromRequestData();

$data = [
    'messageTime' => $_POST['messageTime'],
    'messageId' => $_POST['messageId'],
    'messageType' => $_POST['messageType'],
    'messageText' => $_POST['messageText'],
    'vibrate' => $_POST['vibrate'],
    'vibrationRepeated' => $_POST['vibrationRepeated'],
    'vibrationPattern' => $_POST['vibrationPattern'],
    'displayOnLockScreen' => $_POST['displayOnLockScreen'],
    'lockMessage' => $_POST['lockMessage'],
    'keepScreenOn' => $_POST['keepScreenOn']
];

sendFirebaseMessage($token, $data);

printSuccess("Message successfully sent");

?>
