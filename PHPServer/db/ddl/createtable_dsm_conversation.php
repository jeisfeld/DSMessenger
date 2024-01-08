<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

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
lasttimestamp TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
prepared_message VARCHAR(65535),
archived BOOL,
FOREIGN KEY (relation_id) REFERENCES dsm_relation(id) ON DELETE CASCADE
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_conversation created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
