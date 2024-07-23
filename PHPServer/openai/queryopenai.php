<?php
require_once 'apikey.php';
require_once __DIR__ .'/../util/utilityfunctions.php';

function queryOpenAi($messages, $temperature = 1, $presencePenalty = 0, $frequencyPenalty = 0, $model = 'gpt-4o-mini-2024-07-18')
{
    $data = [
        'model' => $model,
        'temperature' => $temperature,
        'presence_penalty' => $presencePenalty,
        'frequency_penalty' => $frequencyPenalty,
        'messages' => $messages
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://api.openai.com/v1/chat/completions');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Authorization: Bearer ' . getApiKey()
    ]);

    $response = curl_exec($ch);
    if (curl_errno($ch)) {
        echo 'Error:' . curl_error($ch);
    }
    curl_close($ch);

    $response_data = json_decode($response, true);

    if (@$response_data['error']) {
        $error = $response_data['error'];
        return [
            'success' => FALSE,
            'error' => $error
        ];
    }
    if (@$response_data['choices'][0]) {
        $message = $response_data['choices'][0]['message'];
        return [
            'success' => TRUE,
            'message' => $message
        ];
    }
    return [
        'success' => FALSE
    ];
}

function createOpenAiMessage($role, $content)
{
    return [
        'role' => $role,
        'content' => $content
    ];
}

$systemmessage = @$_POST['systemmessage'];
$usermessage = @$_POST['usermessage'];

if ($usermessage) {
    $messages = [];
    if ($systemmessage) {
        $messages[] = createOpenAiMessage('system', $systemmessage);
    }
    $messages[] = createOpenAiMessage('user', $usermessage);
    $result = queryOpenAi($messages);
    if ($result['success']) {
        printSuccess($result['message']['content']);
    }
    else {
        printError(222, "Failed to query OpenAI");
    }
}

?>