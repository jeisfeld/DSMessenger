<?php
require_once 'dbcredentials.php';
require_once __DIR__ .'/../util/utilityfunctions.php';

function getDbConnection() {
    $dbConnection = new mysqli(getDbServer(), getDbUser(), getDbPassword(), getDbName());
    setDbSchema($dbConnection);
    return $dbConnection;
}

function verifyCredentials($conn, $username, $password)
{
    if (! $username) {
        printError(111, "Missing username");
    }
    if (! $password) {
        printError(112, "Password must have length at least 8");
    }

    $hashedpassword = null;
    $userid = null;
    $stmt = $conn->prepare("SELECT password, id FROM dsm_user WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($hashedpassword, $userid);
    $stmt->fetch();
    $stmt->close();

    if (! $hashedpassword) {
        printError(105, "Error: Wrong username or password");
    }
    if (! password_verify($password, $hashedpassword)) {
        printError(105, "Error: Wrong username or password");
    }
    
    return $userid;
}

function verifyCredentialsAndRelation($conn, $post) {
    $username = @$post['username'];
    $password = @$post['password'];
    $userId = verifyCredentials($conn, $username, $password);
    
    $relationId = @$post['relationId'];
    $isSlave = @$post['isSlave'];
    
    if (! $relationId) {
        printError(111, "Missing relationId");
    }
    if ($isSlave) {
        $stmt = $conn->prepare("SELECT id from dsm_relation where id = ? and master_id = ?");
    }
    else {
        $stmt = $conn->prepare("SELECT id from dsm_relation where id = ? and slave_id = ?");
    }
    $stmt->bind_param("ii", $relationId, $userId);
    $stmt->execute();
    if (! $stmt->get_result()->num_rows) {
        printError(107, "Insufficient privileges");
    }
    $stmt->close();
    return $userId;
}

function getRelationData($userId, $relationId, $givenConn = null) {
    if ($givenConn) {
        $conn = $givenConn;
    }
    else {
        $conn = getDbConnection();
        
        if ($conn->connect_error) {
            printError(101, "Connection failed: " . $conn->connect_error);
        }
    }
    $connectionCode = null;
    $contactName = null;
    $contactId = null;
    $myName = null;
    $isSlave = null;
    $slavePermissions = null;
    $stmt = $conn->prepare("SELECT connection_code, master_name as contact_name, master_id as contact_id, slave_name as my_name, false as is_slave, slave_permissions FROM dsm_relation WHERE id = ? and slave_id = ?
UNION
SELECT connection_code, slave_name as contact_name, slave_id as contact_id, master_name as my_name, true as is_slave, slave_permissions FROM dsm_relation WHERE id = ? and master_id = ?");
    $stmt->bind_param("iiii", $relationId, $userId, $relationId, $userId);
    $stmt->execute();
    $stmt->bind_result($connectionCode, $contactName, $contactId, $myName, $isSlave, $slavePermissions);
    if (! $stmt->fetch()) {
        // relation does not belong to user
        if ($_SESSION['username']) {
            header("Location: logout.php");
        }
        else {
            printError(107, "Insufficient privileges - " . $userId  . " - " . $relationId);
        }
        exit(0);
    }
    $stmt->close();
    if (!$givenConn) {
        $conn -> close();
    }
    return [
        'connectionCode' => $connectionCode,
        'contactName' => $contactName,
        'contactId' => $contactId,
        'myName' => $myName,
        'isSlave' => $isSlave,
        'slavePermissions' => $slavePermissions
    ];
}

function hasManageConversationsPermission($relationData) {
    if (! $relationData || ! array_key_exists('isSlave', $relationData)) {
        return false;
    }
    if (! $relationData['isSlave']) {
        return true;
    }
    $slavePermissions = @$relationData['slavePermissions'];
    return strlen($slavePermissions) > 2 && substr($slavePermissions, 2, 1) === '1';
}

function getManageConversationRelations($userId, $givenConn = null) {
    if ($givenConn) {
        $conn = $givenConn;
    }
    else {
        $conn = getDbConnection();

        if ($conn->connect_error) {
            printError(101, "Connection failed: " . $conn->connect_error);
        }
    }

    $relationId = null;
    $contactName = null;
    $isSlave = null;
    $slavePermissions = null;
    $stmt = $conn->prepare("SELECT id, master_name as contact_name, false as is_slave, slave_permissions FROM dsm_relation WHERE slave_id = ?
UNION
SELECT id, slave_name as contact_name, true as is_slave, slave_permissions FROM dsm_relation WHERE master_id = ?");
    $stmt->bind_param("ii", $userId, $userId);
    $stmt->execute();
    $stmt->bind_result($relationId, $contactName, $isSlave, $slavePermissions);

    $relations = array();
    while ($stmt->fetch()) {
        $relationData = [
            'relationId' => $relationId,
            'contactName' => $contactName,
            'isSlave' => $isSlave,
            'slavePermissions' => $slavePermissions
        ];
        if (hasManageConversationsPermission($relationData)) {
            $relations[] = $relationData;
        }
    }
    $stmt->close();

    if (! $givenConn) {
        $conn->close();
    }

    usort($relations, function ($relation1, $relation2) {
        return strcmp($relation1['contactName'], $relation2['contactName']);
    });

    return $relations;
}

function getConversationData($userId, $relationId, $conversationId) {
    $conn = getDbConnection();
    
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $data = getRelationData($userId, $relationId, $conn);
    
    $subject = null;
    $flags = null;
    $lasttimestamp = null;
    $preparedMessage = null;
    $archived = null;
    $stmt = $conn->prepare("SELECT subject, flags, lasttimestamp, prepared_message, archived from dsm_conversation WHERE relation_id = ? AND id = ? order by lasttimestamp desc");
    
    $stmt->bind_param("ss", $relationId, $conversationId);
    $stmt->execute();
    $stmt->bind_result($subject, $flags, $lasttimestamp, $preparedMessage, $archived);
    if (! $stmt->fetch()) {
        // conversation does not belong to relation - logout
        header("Location: logout.php");
        exit(0);
    }
    $stmt->close();
    $conn->close();
    $data['subject'] = $subject;
    $data['flags'] = $flags;
    $data['lasttimestamp'] = $lasttimestamp;
    $data['preparedMessage'] = $preparedMessage;
    $data['archived'] = $archived;
    return $data;
}

function getTokens($conn, $username, $password, $relationId, $isSlave) {
    $userId = verifyCredentials($conn, $username, $password);
    
    if ($isSlave) {
        $stmt = $conn->prepare("SELECT d.token
FROM dsm_device d, dsm_relation r
WHERE d.user_id = r.slave_id AND r.master_id = ? AND r.id = ?");
    }
    else {
        $stmt = $conn->prepare("SELECT d.token
FROM dsm_device d, dsm_relation r
WHERE d.user_id = r.master_id AND r.slave_id = ? AND r.id = ?");
    }

    $token = null;
    $stmt->bind_param("ii", $userId, $relationId);
    $stmt->execute();
    $stmt->bind_result($token);

    $tokens = array();
    while ($stmt->fetch()) {
        $tokens[] = $token;
    }
    
    $stmt->close();
    return $tokens;
}

function getUnmutedTokens($conn, $username, $password, $relationId) {
    $userId = verifyCredentials($conn, $username, $password);
    
        $stmt = $conn->prepare("SELECT d.token
FROM dsm_device d, dsm_relation r
WHERE d.user_id = r.slave_id AND r.master_id = ? AND r.id = ? AND d.muted = 0
UNION
SELECT d.token
FROM dsm_device d, dsm_relation r
WHERE d.user_id = r.master_id AND r.slave_id = ? AND r.id = ? AND d.muted = 0");
    
    $token = null;
    $stmt->bind_param("iiii", $userId, $relationId, $userId, $relationId);
    $stmt->execute();
    $stmt->bind_result($token);
    
    $tokens = array();
    while ($stmt->fetch()) {
        if ($token) {
            $tokens[] = $token;
        }
    }
    
    $stmt->close();
    return $tokens;
}

function getSelfTokens($conn, $username, $password, $deviceId) {
    $userId = verifyCredentials($conn, $username, $password);
    
        $stmt = $conn->prepare("SELECT token FROM dsm_device
WHERE user_id = ? AND id <> ?");
    
    $token = null;
    $stmt->bind_param("ii", $userId, $deviceId);
    $stmt->execute();
    $stmt->bind_result($token);
    
    $tokens = array();
    while ($stmt->fetch()) {
        if ($token) {
            $tokens[] = $token;
        }
    }
    
    return $tokens;
}

function getUnmutedSelfTokens($conn, $username, $password, $deviceId) {
    $userId = verifyCredentials($conn, $username, $password);
    
    $stmt = $conn->prepare("SELECT token FROM dsm_device
WHERE user_id = ? AND id <> ? AND muted = 0");
    
    $token = null;
    $stmt->bind_param("ii", $userId, $deviceId);
    $stmt->execute();
    $stmt->bind_result($token);
    
    $tokens = array();
    while ($stmt->fetch()) {
        if ($token) {
            $tokens[] = $token;
        }
    }
    
    return $tokens;
}


function getDeviceToken($conn, $username, $password, $deviceId) {
    $userId = verifyCredentials($conn, $username, $password);
    
    $stmt = $conn->prepare("SELECT token FROM dsm_device WHERE user_id = ? AND id = ?");
    
    $token = null;
    $stmt->bind_param("ii", $userId, $deviceId);
    $stmt->execute();
    $stmt->bind_result($token);
    $stmt->fetch();
    return $token;
}

function muteDeviceByToken($token, $muted) {
    if ($token) {
        $conn = getDbConnection();
        
        // Check connection
        if ($conn->connect_error) {
            printError(101, "Connection failed: " . $conn->connect_error);
        }
        
        $stmt = $conn->prepare("update dsm_device set muted = ? where token = ?");
        $stmt->bind_param("is", $muted, $token);
        $stmt->execute();
        $stmt->close();
        $conn->close();
    }
}

function getVerifiedTokensFromRequestData() {
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $username = @$_POST['username'];
    $password = @$_POST['password'];
    $relationId = @$_POST['relationId'];
    
    return getUnmutedTokens($conn, $username, $password, $relationId);
}
