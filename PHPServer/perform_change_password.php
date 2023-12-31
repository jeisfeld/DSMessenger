<?php
include __DIR__ . '/check_session.php';
require_once 'db/dbfunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];

if (isset($_POST['submit'])) {
    $old_password = $_POST['old_password'];
    $new_password = $_POST['new_password'];
    $confirm_new_password = $_POST['confirm_new_password'];

    if ($password != $old_password) {
        header("Location: change_password.php?error=wrong_old_password");
        exit;
    }

    if ($new_password != $confirm_new_password) {
        header("Location: change_password.php?error=passwords_nomatch");
        exit;
    }

    if (strlen($new_password) < 8) {
        header("Location: change_password.php?error=password_8_characters");
        exit;
    }
    $hashedpassword = password_hash($new_password, PASSWORD_BCRYPT);
    
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }


    $stmt = $conn->prepare("UPDATE dsm_user SET password = ? WHERE username = ?");
    $stmt->bind_param("ss", $hashedpassword, $username);

    if ($stmt->execute()) {
        $_SESSION = array();
        session_destroy();
        header("Location: login.php");
    }
    else {
        header("Location: change_password.php?error=change_password_failed");
    }

    $conn->close();
}
?>