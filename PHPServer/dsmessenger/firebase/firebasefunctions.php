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

?>
