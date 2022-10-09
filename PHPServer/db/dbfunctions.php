<?php
require_once 'dbcredentials.php';

function getDbConnection() {
    $dbConnection = new mysqli(getDbServer(), getDbUser(), getDbPassword(), getDbName());
    setDbSchema($dbConnection);
    header('Content-Type: text/json');
    return $dbConnection;
}

function printError($errorCode, $errorMessage, $data = null) {
    $result = [
        'status' => 'error',
        'errorcode' => $errorCode,
        'errormessage' => $errorMessage
    ];
    if ($data) {
        foreach ($data as $key => $value) {
            $result[$key] = $value;
        }
    }
    die (json_encode($result, JSON_PRETTY_PRINT));
}

function printSuccess($message, $data = null) {
    $result = [ 'status' => 'success', 'message' => $message ];
    if ($data) {
        foreach ($data as $key => $value) {
            $result[$key] = $value;
        }
    }
    echo json_encode($result, JSON_PRETTY_PRINT);
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
    if (!sizeof($tokens)) {
        printError(106, "Failed to retrieve token");
    }
    return $tokens;
}

function getVerifiedTokensFromRequestData() {
    // Create connection
    $conn = getDbConnection();
    
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $username = @$_POST['username'];
    $password = @$_POST['password'];
    $relationId = @$_POST['relationId'];
    $isSlave = @$_POST['isSlave'];
    
    return getTokens($conn, $username, $password, $relationId, $isSlave);
}

