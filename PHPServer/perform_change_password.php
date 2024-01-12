<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $old_password = $_POST['old_password'];
    $new_password = $_POST['new_password'];
    $confirm_new_password = $_POST['confirm_new_password'];

    if ($password != $old_password) {
        header("Location: change_password.php?error=Wrong_old_password");
        exit;
    }

    if ($new_password != $confirm_new_password) {
        header("Location: change_password.php?error=Passwords_nomatch");
        exit;
    }

    if (strlen($new_password) < 8) {
        header("Location: change_password.php?error=Password_8_characters");
        exit;
    }
    $hashedpassword = password_hash($new_password, PASSWORD_BCRYPT);
    
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }

    $tokens = getSelfTokens($conn, $username, $password, -1);

    $stmt = $conn->prepare("UPDATE dsm_user SET password = ? WHERE username = ?");
    $stmt->bind_param("ss", $hashedpassword, $username);

    if ($stmt->execute()) {
        $stmt -> close();
        $_SESSION = array();
        session_destroy();
        
        $stmt = $conn->prepare("delete from dsm_device WHERE user_id = ?");
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $stmt -> close();
        
        $currentDateTime = new DateTime();
        foreach ($tokens as $token) {
            sendFirebaseMessage($token, [
                'messageType' => 'ADMIN',
                'adminType' => 'PASSWORD_CHANGED',
                'messageTime' => $currentDateTime->format("Y-m-d\TH:i:s.v") . 'Z'
            ]);
        }
        
        header("Location: login.php");
    }
    else {
        $stmt -> close();
        header("Location: change_password.php?error=Change_password_failed");
    }

    $conn->close();
}
?>