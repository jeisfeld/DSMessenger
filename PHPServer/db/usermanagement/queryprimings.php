<?php
require_once __DIR__ . '/../dbfunctions.php';
header('Content-Type: text/json');

function queryPrimingsForUser($conn, $username)
{
    $usertype = 0;
    $stmt = $conn->prepare("SELECT usertype from dsm_user WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($usertype);
    $stmt->fetch();
    $stmt->close();

    $id = null;
    $name = null;
    $primings = array();

    if ($usertype === 1) {
        $stmt = $conn->prepare("SELECT id, name from dsm_ai_priming order by name, id");
    }
    else {
        $stmt = $conn->prepare("SELECT id, name from dsm_ai_priming where name not like 'Dominia%' and name not like 'Veit%' order by name, id");
    }

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

if (!$username) {
    printError(111, "Missing username");
}

$conn = getDbConnection();

if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

verifyCredentials($conn, $username, $password);
$primings = queryPrimingsForUser($conn, $username);
$conn->close();

printSuccess("Primings of user " . $username . " have been retrieved.", [
    'primings' => $primings
]);
?>
