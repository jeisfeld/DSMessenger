<?php
require_once '../dbfunctions.php';

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to create table dsm_relation
$sql = "ALTER TABLE dsm_relation ADD COLUMN slave_permissions VARCHAR(50) DEFAULT '1000'";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_device updated successfully");
}
else {
    printError(102, "Error updating table: " . $conn->error);
}

$conn->close();
