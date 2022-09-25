<?php
namespace dsmessenger\db;

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

    if (! $hashedpassword) {
        die("Error: Wrong username or password");
    }
    if (! password_verify($password, $hashedpassword)) {
        die("Error: Wrong username or password");
    }
}