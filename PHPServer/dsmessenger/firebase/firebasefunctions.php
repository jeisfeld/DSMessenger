<?php
require_once dirname(__FILE__) . '/../vendor/autoload.php';
require_once dirname(__FILE__) . '/token.php';
require_once dirname(__FILE__) . '/../db/dbfunctions.php';

use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

function sendFirebaseMessage($token, $data) {
    $factory = (new Factory())->withServiceAccount(dirname(__FILE__) . "/" . getKeyFileName());
    $messaging = $factory->createMessaging();
    $message = CloudMessage::withTarget('token', $token)->withData($data)->withHighestPossiblePriority();
    $messaging->send($message);
}

function sendTextMessage($device, $messageText, $vibrate, $vibrationRepeated, $vibrationPattern, $displayOnLockScreen, $lockMessage, $keepScreenOn)
{
    $data = [
        'messageType' => 'TEXT',
        'messageText' => $messageText,
        'vibrate' => $vibrate,
        'vibrationRepeated' => $vibrationRepeated,
        'vibrationPattern' => $vibrationPattern,
        'displayOnLockScreen' => $displayOnLockScreen,
        'lockMessage' => $lockMessage,
        'keepScreenOn' => $keepScreenOn
    ];

    $token = ($device == 'tablet' ? getDeviceTokenTablet() : getDeviceToken());
    sendFirebaseMessage($token, $data);
}

function sendRandomImageMessage($device, $randomImageOrigin, $notificationName, $widgetName)
{
    $data = [
        'messageType' => 'RANDOMIMAGE',
        'randomImageOrigin' => $randomImageOrigin,
        'notificationName' => $notificationName,
        'widgetName' => $widgetName
    ];

    $token = ($device == 'tablet' ? getDeviceTokenTablet() : getDeviceToken());
    sendFirebaseMessage($token, $data);
}

?>
