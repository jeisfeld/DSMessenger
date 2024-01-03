<?php
require_once __DIR__ . '/db/dbfunctions.php';

session_start(); // Start the session

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $username = $_POST['username'];
    $password = $_POST['password'];

    // Create connection
    $conn = getDbConnection();
 
    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
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
        header("Location: login.php?error=wrong_credentials");
        exit;
    }
    if (! password_verify($password, $hashedpassword)) {
        header("Location: login.php?error=wrong_credentials");
        exit;
    }
    
    if ($userid) {
        // Credentials are correct
        $_SESSION['loggedin'] = true;
        $_SESSION['username'] = $username;
        $_SESSION['password'] = $password;
        $_SESSION['userId'] = $userid;
        
        // Redirect to user dashboard or another protected page
        header("Location: contacts.php");
    } 
    
    $conn -> close();
    
}
?>
