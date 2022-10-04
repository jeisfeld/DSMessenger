<?php
require_once 'dbcredentials.php';

function printError($errorCode, $errorMessage) {
    $result = [
        'status' => 'error',
        'errorcode' => $errorCode,
        'errormessage' => $errorMessage
    ];
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
        printError(105, "Missing username");
    }
    if (! $password) {
        printError(105, "Missing password");
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


function getToken($conn, $username, $password, $relationId, $isSlave) {
    $userId = verifyCredentials($conn, $username, $password);
    
    $token = null;
    
    if ($isSlave) {
        $stmt = $conn->prepare("SELECT u.token
FROM dsm_user u, dsm_relation r
WHERE u.id = r.slave_id AND r.master_id = ? AND r.id = ?");
    }
    else {
        $stmt = $conn->prepare("SELECT u.token
FROM dsm_user u, dsm_relation r
WHERE u.id = r.master_id AND r.slave_id = ? AND r.id = ?");
    }

    $stmt->bind_param("ii", $userId, $relationId);
    $stmt->execute();
    $stmt->bind_result($token);
    $stmt->fetch();
    $stmt->close();
    if (!$token) {
        printError(102, "Failed to retrieve token");
    }
    return $token;
}


function getVerifiedTokenFromRequestData() {
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
    
    return getToken($conn, $username, $password, $relationId, $isSlave);
}

