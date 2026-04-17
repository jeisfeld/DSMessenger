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

    $userprefix = '@@' . $username . '-';
    $prefixParam = $userprefix . '%';
    if ($username === "AI-JE") {
        $stmt = $conn->prepare("SELECT id, name
         FROM dsm_ai_priming
         ORDER BY name, id");
    }
    elseif ($usertype === 1) {
        $stmt = $conn->prepare("SELECT id,
                CASE
                    WHEN name LIKE ? THEN SUBSTRING(name, LENGTH(?) + 1)
                    ELSE name
                END AS name
         FROM dsm_ai_priming
         WHERE (name NOT LIKE '@@%' OR name LIKE ?)
         ORDER BY name, id");
        $stmt->bind_param("sss", $prefixParam, $userprefix, $prefixParam);
    }
    else {
        $stmt = $conn->prepare("SELECT id,
                CASE
                    WHEN name LIKE ? THEN SUBSTRING(name, LENGTH(?) + 1)
                    ELSE name
                END AS name
         FROM dsm_ai_priming
         WHERE name NOT LIKE 'Dominia%'
           AND (name NOT LIKE '@@%' OR name LIKE ?)
         ORDER BY name, id");
        $stmt->bind_param("sss", $prefixParam, $userprefix, $prefixParam);
    }

    $stmt->execute();
    $stmt->bind_result($id, $name);
    if (str_starts_with($name, $userprefix)) {
        $name = substr($name, strlen($userprefix));
    }

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

if (! $username) {
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
