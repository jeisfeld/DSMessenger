<?php
require_once __DIR__ . '/querymessagefunctions.php';

$username = @$_POST['username'];
$password = @$_POST['password'];
$relationId = @$_POST['relationId'];
$conversationId = @$_POST['conversationId'];

header('Content-Type: text/json');
$messages = queryMessages($username, $password, $relationId, $conversationId);

foreach ($messages as &$message) {
    $message['timestamp'] = convertToJavaTimestamp($message['timestamp']);
}

printSuccess("Messages for conversationId have been retrieved.", [
    'messages' => $messages
]);

