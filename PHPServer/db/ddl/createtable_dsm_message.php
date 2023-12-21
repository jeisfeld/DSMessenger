<?php
require_once '../dbfunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to drop table dsm_message
// $sql = "DROP TABLE dsm_message";

// if ($conn->query($sql) !== TRUE) {
//     printError(103, "Error dropping table: " . $conn->error);
// }

// sql to create table dsm_message
$sql = "CREATE TABLE dsm_message (
id CHAR(36) PRIMARY KEY,
conversation_id CHAR(36),
user_id INT(8) UNSIGNED,
text VARCHAR(65535),
timestamp TIMESTAMP,
status INT(1) UNSIGNED,
FOREIGN KEY (conversation_id) REFERENCES dsm_conversation(id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES dsm_user(id) ON DELETE CASCADE
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_message created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
