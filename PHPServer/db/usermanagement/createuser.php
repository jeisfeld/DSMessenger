<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

$username = @$_POST['username'];
if (! $username) {
    printError(111, "Missing username");
}
$password = @$_POST['password'];
if (! $password || strlen($password) < 8) {
    printError(112, "Password must have length at least 8");
}

$hashedpassword = password_hash($password, PASSWORD_BCRYPT);

$stmt = $conn->prepare("SELECT id FROM dsm_user WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();

if ($stmt->get_result()->num_rows) {
    printError(113, "Username " . $username . " already exists", [
        "username" => $username
    ]);
}
$stmt->close();

$stmt = $conn->prepare("INSERT INTO dsm_user (username, password) VALUES (?, ?)");
$stmt->bind_param("ss", $username, $hashedpassword);

if ($stmt->execute()) {
    $token = @$_POST['token'];
    $userId = verifyCredentials($conn, $username, $password);

    if (! $userId) {
        printError(102, "Failed to retrieve userId");
    }
    if ($token) {
        $stmt = $conn->prepare("INSERT INTO dsm_device (user_id, name, token) values (?, 'Device 1', ?)");
        $stmt->bind_param("is", $userId, $token);
        $stmt->execute();
        $stmt->close();
    }
    else {
        $stmt = $conn->prepare("INSERT INTO dsm_device (user_id, name) values (?, 'Device 1')");
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $stmt->close();
    }
    $deviceId = null;
    $displayStrategyNormal = null;
    $displayStrategyUrgent = null;
    $stmt = $conn->prepare("SELECT id, displaystrategy_normal, displaystrategy_urgent FROM dsm_device WHERE user_id=? AND NAME='Device 1'");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $stmt->bind_result($deviceId, $displayStrategyNormal, $displayStrategyUrgent);
    $stmt->fetch();
    $stmt->close();
    if (! $deviceId) {
        printError(102, "Failed to retrieve deviceId");
    }
    
    // By default, create connection to user jeisfeld, named "Coach", with automatic coaching.
    $masterId = 3;
    
    $stmt = $conn->prepare("INSERT INTO dsm_relation (slave_id, master_id, slave_name, master_name) VALUES (?, ?, ?, 'Coach')");
    $stmt->bind_param("iis", $userId, $masterId, $username);
    $stmt->execute();
    $stmt->close();
    $relationId = null;
    $stmt = $conn->prepare("SELECT MAX(id) FROM dsm_relation WHERE slave_id=? AND master_id=?");
    $stmt->bind_param("ii", $userId, $masterId);
    $stmt->execute();
    $stmt->bind_result($relationId);
    $stmt->fetch();
    $stmt->close();
    if ($relationId) {
        $stmt = $conn->prepare("INSERT INTO dsm_ai_relation (relation_id, user_name, priming_id, add_priming_text, ai_policy) values (?, ?, 3, '', 2)");
        $stmt->bind_param("is", $relationId, $username);
        $stmt->execute();
        $stmt->close();
    }

    printSuccess("User " . $username . " successfully created.", [
        'userId' => $userId,
        'deviceId' => $deviceId,
        'deviceName' => 'Device 1',
        'muted' => false,
        'displayStrategyNormal' => $displayStrategyNormal,
        'displayStrategyUrgent' => $displayStrategyUrgent
    ]);
}
else {
    $stmt->close();
    printError(102, "Failed to create user");
}

$conn->close();
