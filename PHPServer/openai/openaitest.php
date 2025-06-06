<?php
require_once 'queryopenai.php';

$messageText = createOpenAiMessage(
    'user',
    'Translate the following English text to an arbitrary language: Hello, how are you? Tell which language you selected.'
);

$result = queryOpenAi([$messageText], 1, 0, 0, 'claude-3-5-sonnet-20241022');

if ($result['success']) {
    $messageText = $result['message'];
    echo "<i>[".$messageText['role']."]:</i> ".$messageText['content'];
}
else {
    $error = $result['error'];
    echo "<b>ERROR: ".$error['code']."</b><br>";
    echo $error['message']."<br>";
}

?>