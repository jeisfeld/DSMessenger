<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/conversation/querymessagefunctions.php';
require_once 'openai/queryopenai.php';

$username = $_SESSION['username'];
$password = $_SESSION['password'];
$relationId = $_GET['relationId'];
$conversationId = $_GET['conversationId'];

$messages = queryMessages($username, $password, $relationId, $conversationId);
foreach ($messages as $message) {
    $prefix = $message['isOwn'] ? 'User: ' : 'AI: ';
    echo $prefix . $message['text'] . "\n\n";
}
?>
