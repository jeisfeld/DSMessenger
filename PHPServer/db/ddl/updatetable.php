<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to create table dsm_relation
$sql = "ALTER TABLE dsm_message MODIFY COLUMN timestamp TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_message updated successfully");
}
else {
    printError(102, "Error updating table: " . $conn->error);
}

$conn->close();
