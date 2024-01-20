<?php
require_once __DIR__.'/../dbfunctions.php';

function queryContacts($username, $password)
{
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userid = verifyCredentials($conn, $username, $password);
    
    $relationId = null;
    $connectionCode = null;
    $contactName = null;
    $contactId = null;
    $myName = null;
    $isSlave = null;
    $slavePermissions = null;
    $aiPolicy = null;
    $aiRelationId = null;
    $aiUsername = null;
    $aiPrimingId = null;
    $aiAddPrimingText = null;
    $aiMessageSuffix = null;
    $stmt = $conn->prepare("SELECT r.id, connection_code, master_name as contact_name, master_id as contact_id, slave_name as my_name, false as is_slave, slave_permissions, ai_policy, a.id as ai_relation_id, a.user_name as ai_user_name, priming_id, add_priming_text, message_suffix FROM dsm_relation r LEFT JOIN dsm_ai_relation a ON r.id = a.relation_id WHERE slave_id = ?
UNION
SELECT r.id, connection_code, slave_name as contact_name, slave_id as contact_id, master_name as my_name, true as is_slave, slave_permissions, ai_policy, a.id as ai_relation_id, a.user_name as ai_user_name, priming_id, add_priming_text, message_suffix FROM dsm_relation r LEFT JOIN dsm_ai_relation a ON r.id = a.relation_id WHERE master_id = ?
ORDER BY contact_name");
    
    $stmt->bind_param("ii", $userid, $userid);
    $stmt->execute();
    $stmt->bind_result($relationId, $connectionCode, $contactName, $contactId, $myName, $isSlave, $slavePermissions, $aiPolicy, $aiRelationId, $aiUsername, $aiPrimingId, $aiAddPrimingText, $aiMessageSuffix);
    
    $contacts = array();
    while ($stmt->fetch()) {
        $contacts[] = [
            'relationId' => $relationId,
            'connectionCode' => $connectionCode,
            'contactName' => $contactName,
            'contactId' => $contactId == null ? -1 : $contactId,
            'myName' => $myName,
            'isSlave' => $isSlave ? true : false,
            'isConfirmed' => $contactId ? true : false,
            'slavePermissions' => $slavePermissions,
            'aiPolicy' => $aiPolicy ?? 0,
            'aiRelationId' => $aiRelationId ?? '',
            'aiUsername' => $aiUsername ?? '',
            'aiPrimingId' => $aiPrimingId,
            'aiAddPrimingText' => $aiAddPrimingText,
            'aiMessageSuffix' => $aiMessageSuffix
        ];
    }
    $stmt->close();
    $conn -> close();
    
    return $contacts;
}

function queryUsertype($username, $password)
{
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $userid = verifyCredentials($conn, $username, $password);
    
    $usertype = null;
    $stmt = $conn->prepare("SELECT usertype from dsm_user WHERE id = ?");
    
    $stmt->bind_param("i", $userid);
    $stmt->execute();
    $stmt->bind_result($usertype);
    $stmt->fetch();    
    $stmt->close();
    
    return $usertype;
}

function queryPrimings()
{
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $id = null;
    $name = null;
    $stmt = $conn->prepare("SELECT id, name from dsm_ai_priming");

    $stmt->execute();
    $stmt->bind_result($id, $name);

    while ($stmt->fetch()) {
        $primings[] = [
            'id' => $id,
            'name' => $name
        ];
    }
    
    $stmt->close();
    
    return $primings;
}

$username = @$_POST['username'];
$password = @$_POST['password'];
$messageTime = @$_POST['messageTime'];

if ($username && $messageTime) {
    header('Content-Type: text/json');
    $contacts = queryContacts($username, $password);
    $usertype = queryUsertype($username, $password);
    
    printSuccess("Contacts of user " . $username . " have been retrieved.", [
        'contacts' => $contacts,
        'usertype' => $usertype
    ]);
}
