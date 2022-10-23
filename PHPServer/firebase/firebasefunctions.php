<?php
require_once dirname(__FILE__) . '/../vendor/autoload.php';
require_once dirname(__FILE__) . '/token.php';
require_once dirname(__FILE__) . '/../db/dbfunctions.php';

use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\AndroidConfig;

function sendFirebaseMessage($token, $data, $ttl = null)
{
    $factory = (new Factory())->withServiceAccount(dirname(__FILE__) . "/" . getKeyFileName());
    $messaging = $factory->createMessaging();

    $androidConfig = null;
    if ($ttl == null) {
        $androidConfig = AndroidConfig::fromArray([
            'priority' => 'high'
        ]);
    }
    else {
        $androidConfig = AndroidConfig::fromArray([
            'priority' => 'high',
            'ttl' => $ttl . 's'
        ]);
    }

    $message = CloudMessage::withTarget('token', $token)->withData($data)
        ->withAndroidConfig($androidConfig)
        ->withHighestPossiblePriority();
    $messaging->send($message);
}

?>
