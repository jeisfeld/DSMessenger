<?php
require_once '../dbfunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to drop table dsm_conversation
// $sql = "DROP TABLE dsm_conversation";

// if ($conn->query($sql) !== TRUE) {
//     printError(103, "Error dropping table: " . $conn->error);
// }

// sql to create table dsm_conversation
$sql = "CREATE TABLE dsm_conversation (
id CHAR(36) PRIMARY KEY,
relation_id INT(8) UNSIGNED,
subject VARCHAR(100),
flags VARCHAR(20),
lasttimestamp TIMESTAMP,
FOREIGN KEY (relation_id) REFERENCES dsm_relation(id) ON DELETE CASCADE
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_conversation created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
