<?php
session_start();

if (!isset($_SESSION['loggedin']) || $_SESSION['loggedin'] !== true) {
    // The user is not logged in, redirect to login page
    header("Location: login.php");
    exit;
}
?>
