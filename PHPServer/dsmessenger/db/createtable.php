<?php
namespace dsmessenger\db;
include 'dbcredentials.php';
use mysqli;

// Create connection

$conn = new mysqli(getDbServer(), getDbUser(), getDbPassword(), getDbName());
// Check connection
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}

// sql to create table
$sql = "CREATE TABLE " . getSchemaPrefix() . "dsm_user (
id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(30) NOT NULL,
name VARCHAR(30) NOT NULL,
token VARCHAR(255)
)";

if ($conn->query($sql) === TRUE) {
  echo "Table created successfully";
} else {
  echo "Error creating table: " . $conn->error;
}

$conn->close();
