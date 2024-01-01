<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to drop table dsm_user
// $sql = "DROP TABLE dsm_user";

// if ($conn->query($sql) !== TRUE) {
//     printError(103, "Error dropping table: " . $conn->error);
// }

// sql to create table dsm_user
$sql = "CREATE TABLE dsm_user (
id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(30) NOT NULL,
password VARCHAR(60) NOT NULL,
usertype INT(1) UNSIGNED DEFAULT 0,
CONSTRAINT unique_username UNIQUE(username)
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_user created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
