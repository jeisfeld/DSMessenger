<?php
require_once __DIR__ . '/../dbfunctions.php';
header('Content-Type: text/json');

function getPrimingPrefixes($conn, $username)
{
    if ($username !== 'AI-JE') {
        return ['@@' . $username . '-'];
    }

    $prefixes = [];
    $contactUsername = null;
    $stmt = $conn->prepare("SELECT DISTINCT u.username
            FROM dsm_relation r
            JOIN dsm_user u ON (u.id = r.master_id OR u.id = r.slave_id)
            JOIN dsm_user me ON me.username = ?
            WHERE (r.master_id = me.id OR r.slave_id = me.id)
              AND u.id <> me.id");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($contactUsername);
    while ($stmt->fetch()) {
        $prefixes[] = '@@' . $contactUsername . '-';
    }
    $stmt->close();

    if (count($prefixes) === 0) {
        $prefixes[] = '@@' . $username . '-';
    }

    return $prefixes;
}

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

    $userprefixes = getPrimingPrefixes($conn, $username);
    if ($usertype === 1) {
        $stmt = $conn->prepare("SELECT id, name
         FROM dsm_ai_priming
         WHERE (name NOT LIKE '@@%' OR name LIKE " . implode(" OR name LIKE ", array_fill(0, count($userprefixes), "?")) . ")
         ORDER BY name, id");
    }
    else {
        $stmt = $conn->prepare("SELECT id, name
         FROM dsm_ai_priming
         WHERE name NOT LIKE 'Dominia%'
           AND (name NOT LIKE '@@%' OR name LIKE " . implode(" OR name LIKE ", array_fill(0, count($userprefixes), "?")) . ")
         ORDER BY name, id");
    }

    $prefixParams = array_map(fn($prefix) => $prefix . '%', $userprefixes);
    $paramTypes = str_repeat("s", count($prefixParams));
    $stmt->bind_param($paramTypes, ...$prefixParams);
    
    $stmt->execute();
    $stmt->bind_result($id, $name);
    while ($stmt->fetch()) {
        foreach ($userprefixes as $userprefix) {
            if (str_starts_with($name, $userprefix)) {
                $name = substr($name, strlen($userprefix));
                break;
            }
        }
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
