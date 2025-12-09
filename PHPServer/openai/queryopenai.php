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
    $needsresponsesapi = str_starts_with($model, 'gpt-5-pro');

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
        foreach ($messages as $index => &$message) {
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
        foreach ($messages as &$message) {
            if (isset($message['role']) && $message['role'] === 'assistant') {
                $message['role'] = 'model';
            }
        }
        unset($message);
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
        if (str_starts_with($model, 'gemini-2') || str_starts_with($model, 'gemini-3')) {
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
    else if ($isxai) {
        $data = [
            'model' => $model,
            'temperature' => $temperature,
            'messages' => $messages
        ];
    }
    else if ($needsresponsesapi) {
        $input = mapMessagesToResponsesInput($messages);
        
        $data = [
            'model' => $model,
            'temperature' => $temperature,
            'input' => $messages
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
    else if ($needsresponsesapi) {
        curl_setopt($ch, CURLOPT_URL, 'https://api.openai.com/v1/responses');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'Authorization: Bearer ' . getApiKey(0)
        ]);
    }else {
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
    else if ($needsresponsesapi) {
        if (!empty($response_data['output']) && is_array($response_data['output'])) {
            // filter items with status === "completed"
            $completed = array_values(array_filter(
                $response_data['output'],
                fn($item) => ($item['type'] ?? null) === 'message'
                ));
            
            // take the last such item
            if ($completed) {
                $last = end($completed);
                // flatten any text parts
                $texts = array_map(
                    fn($c) => $c['text'] ?? '',
                    array_filter($last['content'] ?? [], fn($c) => ($c['type'] ?? null) === 'output_text')
                    );
                $message = trim(implode("\n", $texts));
                return [
                    'success' => TRUE,
                    'message' => $message
                ];
            }
        }
        if (@$response_data['error']) {
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

function mapMessagesToResponsesInput(array $messages): array {
    $out = [];
    
    foreach ($messages as $m) {
        $role = $m['role'];
        $content = $m['content'];
        
        // Case A: content was a simple string
        if (is_string($content)) {
            $out[] = [
                'role' => $role,
                'content' => [
                    [ 'type' => 'input_text', 'text' => $content ]
                ],
            ];
            continue;
        }
        
        // Case B: Chat Completions "vision" style: array of {type:text|image_url,...}
        if (is_array($content)) {
            $parts = [];
            foreach ($content as $part) {
                if (($part['type'] ?? null) === 'text') {
                    $parts[] = [ 'type' => 'input_text', 'text' => $part['text'] ?? '' ];
                } elseif (($part['type'] ?? null) === 'image_url') {
                    // Accept either ['image_url' => 'https://...'] or ['image_url' => ['url'=>'...']]
                    $img = $part['image_url'] ?? null;
                    $url = is_array($img) ? ($img['url'] ?? null) : $img;
                    if ($url) {
                        $parts[] = [ 'type' => 'input_image', 'image_url' => $url ];
                    }
                }
                // You can add other part types (files, audio) here if you use them.
            }
            $out[] = [ 'role' => $role, 'content' => $parts ];
            continue;
        }
        
        // Fallback: coerce anything else to text
        $out[] = [
            'role' => $role,
            'content' => [
                [ 'type' => 'input_text', 'text' => strval($content) ]
            ],
        ];
    }
    
    return $out;
}

?>