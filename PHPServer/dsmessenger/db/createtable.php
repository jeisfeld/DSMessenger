<?php
namespace dsmessenger\db;
include 'dbcredentials.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// sql to drop table
$sql = "DROP TABLE dsm_user";

if ($conn->query($sql) === TRUE) {
    echo "Table dsm_user dropped successfully";
}
else {
    echo "Error dropping table: " . $conn->error;
}

// sql to create table
$sql = "CREATE TABLE dsm_user (
id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(30) NOT NULL,
password VARCHAR(60) NOT NULL,
email VARCHAR(60),
token VARCHAR(200)
)";

if ($conn->query($sql) === TRUE) {
    echo "Table dsm_user created successfully";
}
else {
    echo "Error creating table: " . $conn->error;
}

$conn->close();
