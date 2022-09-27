<?php
function printError($errorCode, $errorMessage) {
    die ("{\n  ".'"status": "error",'."\n  ".'"errorcode": "'.$errorCode.'",'."\n  ".'"errormessage": "'.$errorMessage.'"'."\n}");
}

function printSuccess($message, $data = null) {
    echo "{\n  ".'"status": "success",'."\n";
    if ($data) {
        foreach ($data as $key => $value) {
            echo '  "'.$key.'": "'.$value.'",'."\n";
        }
    }
    echo '  "message": "'.$message.'"'."\n";
    echo '}';
}


function verifyCredentials($conn, $username, $password)
{
    if (! $username) {
        die("Error: Missing username");
    }
    if (! $password) {
        die("Error: Missing password");
    }

    $hashedpassword = null;
    $stmt = $conn->prepare("SELECT password FROM dsm_user WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($hashedpassword);
    $stmt->fetch();
    $stmt->close();

    if (! $hashedpassword) {
        printError(105, "Error: Wrong username or password");
    }
    if (! password_verify($password, $hashedpassword)) {
        printError(105, "Error: Wrong username or password");
    }
}

function getUserId($conn, $username, $password) {
    if (! $username) {
        die("Error: Missing username");
    }
    if (! $password) {
        die("Error: Missing password");
    }
    
    $id = null;
    $stmt = $conn->prepare("SELECT id FROM dsm_user WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($id);
    $stmt->fetch();
    $stmt->close();
    return $id;
}

$_POST = $_GET;
