<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to create table dsm_ai_priming
$sql = "ALTER TABLE dsm_ai_priming
ADD COLUMN model VARCHAR(100) DEFAULT 'gpt-4-1106-preview'";

// variation: 0 no, 1 linear, 2 exponential

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_ai_priming updated successfully");
}
else {
    printError(102, "Error updating table: " . $conn->error);
}

$conn->close();
