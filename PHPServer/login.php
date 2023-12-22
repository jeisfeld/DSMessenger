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
    
    $userId = verifyCredentials($conn, $username, $password);
    if ($userId) {
        // Credentials are correct
        $_SESSION['loggedin'] = true;
        $_SESSION['username'] = $username;
        $_SESSION['password'] = $password;
        $_SESSION['userId'] = $userId;
        
        // Redirect to user dashboard or another protected page
        header("Location: contacts.php");
    } 
}
?>
