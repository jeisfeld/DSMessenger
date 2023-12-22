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
// $sql = "DROP TABLE dsm_device";

// if ($conn->query($sql) !== TRUE) {
//     printError(103, "Error dropping table: " . $conn->error);
// }

// sql to create table dsm_user
$sql = "CREATE TABLE dsm_device (
id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
user_id INT(8) UNSIGNED NOT NULL,
name VARCHAR(30) NOT NULL,
token VARCHAR(200),
muted BOOLEAN DEFAULT false,
displaystrategy_normal VARCHAR(50) DEFAULT '0000000',
displaystrategy_urgent VARCHAR(50) DEFAULT '0100100',
FOREIGN KEY (user_id) REFERENCES dsm_user(id) ON DELETE CASCADE,
CONSTRAINT unique_devicename UNIQUE(user_id, name)
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_device created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
