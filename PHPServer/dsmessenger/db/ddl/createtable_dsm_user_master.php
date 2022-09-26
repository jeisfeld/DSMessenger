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
$sql = "DROP TABLE dsm_user_master";

if ($conn->query($sql) !== TRUE) {
    printError(103, "Error dropping table: " . $conn->error);
}

// sql to create table dsm_user_slave
$sql = "CREATE TABLE dsm_user_master (
id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
slave_id INT(6) UNSIGNED NOT NULL,
username VARCHAR(30),
password VARCHAR(60),
verificationcode VARCHAR(25) NOT NULL,
email VARCHAR(60),
token VARCHAR(200),
FOREIGN KEY (slave_id) REFERENCES dsm_user_slave(id)
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_user_master created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
