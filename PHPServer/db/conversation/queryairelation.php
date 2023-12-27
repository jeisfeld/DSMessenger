<?php
require_once __DIR__ . '/../dbfunctions.php';
require_once __DIR__ . '/../../openai/queryopenai.php';

function queryAiRelation($username, $password, $relationId, $isSlave)
{
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }

    verifyCredentials($conn, $username, $password);

    $aiUserName = null;
    $addPrimingText = null;
    $aiPolicy = null;
    $primingText = null;

    $stmt = $conn->prepare("SELECT ai.user_name, ai.add_priming_text, ai.ai_policy, p.priming_text  
            from dsm_ai_relation ai, dsm_ai_priming p 
            where ai.relation_id = ?
            and ai.priming_id = p.id");

    $stmt->bind_param("i", $relationId);
    $stmt->execute();
    $stmt->bind_result($aiUserName, $addPrimingText, $aiPolicy, $primingText);
    
    if ($stmt->fetch()) {
        $primingText2 = str_replace("[EXTRA_TEXT]", $addPrimingText, $primingText);
        $primingText3 = str_replace("[NAME]", $aiUserName, $primingText2);
        
        
        $aiRelation = [
            'aiUserName' => $aiUserName,
            'aiPolicy' => $aiPolicy,
            'promptmessage' => createOpenAiMessage('system', $primingText3)
        ];
    }
    else {
        $aiRelation = null;
    }

    $stmt->close();
    $conn->close();

    return $aiRelation;
}
