<?php
require_once 'firebasefunctions.php';

$token = getVerifiedTokenFromRequestData();

$data = [
    'messageType' => $_POST['messageType'],
    'randomImageOrigin' => $_POST['randomImageOrigin'],
    'notificationName' => $_POST['notificationName'],
    'widgetName' => $_POST['widgetName']
];

sendFirebaseMessage($token, $data);

printSuccess("Message successfully sent");

?>
