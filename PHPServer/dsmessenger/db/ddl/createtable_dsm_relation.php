<?php
require_once '../dbcredentials.php';
require_once '../functions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to drop table dsm_relation
$sql = "DROP TABLE dsm_relation";

if ($conn->query($sql) !== TRUE) {
    printError(103, "Error dropping table: " . $conn->error);
}

// sql to create table dsm_relation
$sql = "CREATE TABLE dsm_relation (
id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
slave_id INT(6) UNSIGNED,
master_id INT(6) UNSIGNED,
connection_code VARCHAR(24),
wait_verification_by BOOLEAN,
FOREIGN KEY (slave_id) REFERENCES dsm_user(id),
FOREIGN KEY (master_id) REFERENCES dsm_user(id)
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_relation created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

$conn->close();
