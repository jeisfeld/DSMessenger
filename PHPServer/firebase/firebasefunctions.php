<?php
require_once dirname(__FILE__) . '/../vendor/autoload.php';
require_once dirname(__FILE__) . '/token.php';
require_once dirname(__FILE__) . '/../db/dbfunctions.php';

use Ramsey\Uuid\Uuid;
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\AndroidConfig;
use Kreait\Firebase\Exception\Messaging\InvalidMessage;
use Kreait\Firebase\Exception\Messaging\NotFound;

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

    try {
        $messaging->send($message);
    }
    catch (NotFound $e) {
        error_log($e->getMessage() . " Stack trace:");
        error_log($e->getTraceAsString());
        muteDeviceByToken($token, 2);
    }
    catch (InvalidMessage $e) {
        error_log($e->getMessage());
        error_log($e->getTraceAsString());
        if (strpos($e->getMessage(), 'too big') !== false) {
            if (isset($data['messageType']) && $data['messageType'] === 'TEXT') {
                $newData = array_merge($data, ['messageText' => 'Please refresh to see the message.']);
                $message = CloudMessage::withTarget('token', $token)->withData($newData)
                ->withAndroidConfig($androidConfig)
                ->withHighestPossiblePriority();
                try {
                    $messaging->send($message);
                }
                catch (Exception $e) {
                    error_log($e->getMessage());
                    error_log($e->getTraceAsString());
                }
            }
        } 
        else {
            muteDeviceByToken($token, 2);
        }
    }
}

function getTextData($relationId, $messageType, $messageText, $conversationId, $mysqlTimestamp, $messageId, $preparedMessage = "", $isHighPrio = FALSE)
{
    $currentDateTime = new DateTime();
    return [
        "messageType" => $messageType,
        "messageText" => $messageText,
        "priority" => $isHighPrio ? "HIGH" : "NORMAL",
        "conversationId" => $conversationId,
        "timestamp" => convertToJavaTimestamp($mysqlTimestamp),
        "messageId" => $messageId,
        "relationId" => $relationId,
        "preparedMessage" => $preparedMessage,
        "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
    ];
}

function getAdminData($relationId, $adminType, $data = [], $isHighPrio = FALSE)
{
    $currentDateTime = new DateTime();
    $result = [
        "messageType" => "ADMIN",
        "adminType" => $adminType,
        "priority" => $isHighPrio ? "HIGH" : "NORMAL",
        "messageId" => Uuid::uuid4()->toString(),
        "relationId" => $relationId,
        "messageTime" => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
    ];
    foreach ($data as $key => $value) {
        $result[$key] = $value;
    }
    return $result;
}

function sendAdminMessage($conn, $username, $password, $relationId, $adminType, $data = [], $deviceId = -1, $isHighPrio = FALSE)
{
    $data = getAdminData($relationId, $adminType, $data, $isHighPrio);
    
    $tokens = getUnmutedTokens($conn, $username, $password, $relationId);
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
    
    $tokens = getSelfTokens($conn, $username, $password, $deviceId);
    foreach ($tokens as $token) {
        sendFirebaseMessage($token, $data, null);
    }
}

?>
