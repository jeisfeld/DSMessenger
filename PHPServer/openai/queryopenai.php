<?php
require_once 'apikey.php';
require_once __DIR__ . '/../util/utilityfunctions.php';

function queryOpenAi($messages, $temperature = 1, $presencePenalty = 0, $frequencyPenalty = 0, $model = 'gpt-4o-mini-2024-07-18')
{
    $isclaude = str_starts_with($model, 'claude');
    $isgemini = str_starts_with($model, 'gemini');
    $isllama = str_starts_with($model, 'llama') || str_starts_with($model, 'mixtral') || str_starts_with($model, 'Qwen') || str_starts_with($model, 'Nous');
    $isdeepseek = str_starts_with($model, 'deepseek');
    $isxai = str_starts_with($model, 'grok');

    if (str_starts_with($model, 'o1-')) {
        foreach ($messages as &$message) {
            if ($message['role'] === 'system') {
                $message['role'] = 'user'; // TOTO: possibly change if o1 allows system message.
            }
        }
        unset($message);
    }
    if (str_starts_with($model, 'deepseek-reasoner')) { // deepseek-reasoner expects possible system message followed by interleaving user/assistant messages.
        $newmessages = [];
        $lastrole = "";
        foreach ($messages as &$message) {
            if ($message['role'] === 'system' && $lastrole === "") {
                $newmessages[] = $message;
                continue;
            }
            if ($lastrole === "" && $message['role'] === 'assistant') {
                $newmessages[] = createOpenAiMessage('user', 'Please start with first message.');
                $lastrole = 'user';
            }
            if ($message['role'] === $lastrole) {
                $newmessages[count($newmessages) - 1] .= "\n" . $message['content'];
            }
            else {
                $newmessages[] = $message;
                $lastrole = $message['role'];
            }
        }
        unset($message);
        $messages = $newmessages;
    }

    if ($isclaude || $isgemini) {
        $system = "";
        foreach ($messages as $index => $message) {
            if (isset($message['role']) && $message['role'] === 'system') {
                // Extract content of the system message
                $system = $message['content'];
                // Remove the system message from $messages
                unset($messages[$index]);
                // Break after finding the first "system" entry
                break;
            }
        }
        // Reindex the array to remove gaps in numeric keys
        $messages = array_values($messages);
    }

    if ($isclaude) {
        $data = [
            'model' => $model,
            'temperature' => $temperature,
            'max_tokens' => 8192,
            'messages' => $messages
        ];
        if ($system) {
            $data['system'] = $system;
        }
    }
    else if ($isgemini) {
        $data = [
            "safetySettings" => [
                [
                    'category' => "HARM_CATEGORY_HARASSMENT",
                    'threshold' => "BLOCK_NONE"
                ],
                [
                    'category' => "HARM_CATEGORY_HATE_SPEECH",
                    'threshold' => "BLOCK_NONE"
                ],
                [
                    'category' => "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    'threshold' => "BLOCK_NONE"
                ],
                [
                    'category' => "HARM_CATEGORY_DANGEROUS_CONTENT",
                    'threshold' => "BLOCK_NONE"
                ],
                [
                    'category' => "HARM_CATEGORY_CIVIC_INTEGRITY",
                    'threshold' => "BLOCK_NONE"
                ]
            ],
            "generationConfig" => [
                'temperature' => $temperature,
                'response_modalities' => 'TEXT'
            ],
            'contents' => array_map(function ($item) {
                return [
                    'role' => $item['role'],
                    'parts' => [
                        'text' => $item['content']
                    ]
                ];
            }, $messages),
            'tools' => [
                [
                    'google_search_retrieval' => [
                        'dynamic_retrieval_config' => [
                            'mode' => 'MODE_DYNAMIC',
                            'dynamic_threshold' => 0.5
                        ]
                    ]
                ]
            ]
        ];
        if (str_starts_with($model, 'gemini-2')) {
            $data['tools'] = [
                [
                    'google_search' => new stdClass()
                ]
            ];
        }
        if ($system) {
            $data['system_instruction'] = [
                'parts' => [
                    'text' => $system
                ]
            ];
        }
    }
    else if ($isllama) {
        $data = [
            'model' => $model,
            'temperature' => $temperature,
            'frequency_penalty' => $frequencyPenalty,
            'messages' => $messages,
            'max_tokens' => 8192
        ];
    }
    else {
        $data = [
            'model' => $model,
            'temperature' => $temperature,
            'presence_penalty' => $presencePenalty,
            'frequency_penalty' => $frequencyPenalty,
            'messages' => $messages
        ];
    }

    $ch = curl_init();

    if ($isclaude) {
        curl_setopt($ch, CURLOPT_URL, 'https://api.anthropic.com/v1/messages');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'x-api-key: ' . getApiKey(1),
            'anthropic-version: 2023-06-01'
        ]);
    }
    else if ($isgemini) {
        curl_setopt($ch, CURLOPT_URL, 'https://generativelanguage.googleapis.com/v1beta/models/' . $model . ':generateContent?key=' . getApiKey(2));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json'
        ]);
    }
    else if ($isllama) {
        curl_setopt($ch, CURLOPT_URL, 'https://api.llama-api.com/chat/completions');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'Authorization: Bearer ' . getApiKey(3)
        ]);
    }
    else if ($isdeepseek) {
        curl_setopt($ch, CURLOPT_URL, 'https://api.deepseek.com/chat/completions');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'Authorization: Bearer ' . getApiKey(4)
        ]);
    }
    else if ($isxai) {
        curl_setopt($ch, CURLOPT_URL, 'https://api.x.ai/v1/chat/completions');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'Authorization: Bearer ' . getApiKey(5)
        ]);
    }
    else {
        curl_setopt($ch, CURLOPT_URL, 'https://api.openai.com/v1/chat/completions');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'Authorization: Bearer ' . getApiKey(0)
        ]);
    }

    $response = curl_exec($ch);
    if (curl_errno($ch)) {
        echo 'Error:' . curl_error($ch);
    }
    curl_close($ch);

    $response_data = json_decode($response, true);

    if ($isclaude) {
        if (isset($response_data['content'][0]['text'])) {
            $message = $response_data['content'][0]['text'];
            return [
                'success' => TRUE,
                'message' => [
                    'role' => $response_data['role'],
                    'content' => $message
                ]
            ];
        }
        else if (isset($response_data['error']['message'])) {
            $message = $response_data['error'];
            return [
                'success' => FALSE,
                'error' => [
                    'code' => $message['type'],
                    'message' => $message['message']
                ]
            ];
        }
        else { // for analyzing unexpected behavior
            return [
                'success' => FALSE,
                'error' => [
                    'code' => 'UNEXPECTED',
                    'message' => $response
                ]
            ];
        }
    }
    else if ($isgemini) {
        if (isset($response_data['candidates'][0]['content'])) {
            $message = $response_data['candidates'][0]['content']['parts'][0]['text'];
            return [
                'success' => TRUE,
                'message' => [
                    'role' => 'assistant',
                    'content' => $message
                ]
            ];
        }
        else if (isset($response_data['error']['message'])) {
            $message = $response_data['error'];
            return [
                'success' => FALSE,
                'error' => [
                    'code' => $message['code'],
                    'message' => $message['message']
                ]
            ];
        }
        else { // for analyzing unexpected behavior
            return [
                'success' => FALSE,
                'error' => [
                    'code' => 'UNEXPECTED',
                    'message' => $response
                ]
            ];
        }
    }
    else {
        if (@$response_data['choices'][0]) {
            $message = $response_data['choices'][0]['message'];
            return [
                'success' => TRUE,
                'message' => $message
            ];
        }
        else if (@$response_data['error']) {
            $error = $response_data['error'];
            if (is_array($error)) {
                // openai
                return [
                    'success' => FALSE,
                    'error' => [
                        'code' => $error['code'],
                        'message' => $error['message']
                    ]
                ];
            }
            else {
                // x-ai
                return [
                    'success' => FALSE,
                    'error' => [
                        'code' => $response_data['code'],
                        'message' => $error
                    ]
                ];
            }
        }
        else { // for analyzing unexpected behavior
            return [
                'success' => FALSE,
                'error' => [
                    'code' => 'UNEXPECTED',
                    'message' => $response
                ]
            ];
        }
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