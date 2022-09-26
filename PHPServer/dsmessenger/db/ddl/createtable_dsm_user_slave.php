<?php
require_once '../dbcredentials.php';
require_once '../functions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to drop table dsm_user_slave
$sql = "DROP TABLE dsm_user_slave";

if ($conn->query($sql) !== TRUE) {
    printError(103, "Error dropping table: " . $conn->error);
}

// sql to create table dsm_user_slave
$sql = "CREATE TABLE dsm_user_slave (
id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(30) NOT NULL,
password VARCHAR(60) NOT NULL,
email VARCHAR(60),
token VARCHAR(200)
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_user_slave created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
