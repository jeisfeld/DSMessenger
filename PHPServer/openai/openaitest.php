<?php
require_once 'queryopenai.php';

$message = createOpenAiMessage(
    'user',
    'Translate the following English text to an arbitrary language: Hello, how are you? Tell which language you selected.'
);

$result = queryOpenAi([$message], 1, 0, 0);

if ($result['success']) {
    $message = $result['message'];
    echo "<i>[".$message['role']."]:</i> ".$message['content'];
}
else {
    $error = $result['error'];
    echo "<b>ERROR: ".$error['code']."</b><br>";
    echo $error['message']."<br>";
}

?>