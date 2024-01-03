<?php
require_once __DIR__ . '/db/dbfunctions.php';

session_start(); // Start the session

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $username = $_POST['username'];
    $password = $_POST['password'];
    $confirmPassword = $_POST['confirm_password'];
    
    if ($password != $confirmPassword) {
        header("Location: create_account.php?error=passwords_nomatch");
        exit;
    }
    
    if (strlen($password) < 8) {
        header("Location: create_account.php?error=password_8_characters");
        exit;
    }
    $hashedpassword = password_hash($password, PASSWORD_BCRYPT);
    
    // Create connection
    $conn = getDbConnection();
 
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $stmt = $conn->prepare("SELECT username FROM dsm_user WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    if ($stmt->get_result()->num_rows) {
        $stmt->close();
        header("Location: create_account.php?error=username_already_exists");
        exit;
    }
    else {
        $stmt->close();
    }
    
    $stmt = $conn->prepare("INSERT INTO dsm_user (username, password) VALUES (?, ?)");
    $stmt->bind_param("ss", $username, $hashedpassword);
    if ($stmt->execute()) {
        $stmt->close();
        header("Location: login.php");
    }
    else {
        $stmt->close();
        header("Location: create_account.php");
    }
        
    $conn -> close();        
}
?>
