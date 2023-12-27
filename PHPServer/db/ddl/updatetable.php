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
$sql = "ALTER TABLE dsm_conversation ADD COLUMN prepared_message VARCHAR(65535);";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_conversation updated successfully");
}
else {
    printError(102, "Error updating table: " . $conn->error);
}

$conn->close();
