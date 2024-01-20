<?php
require_once '../dbfunctions.php';
header('Content-Type: text/json');

// Create connection
$conn = getDbConnection();

// Check connection
if ($conn->connect_error) {
    printError(101, "Connection failed: " . $conn->connect_error);
}

// sql to drop table dsm_ai_relation
// $sql = "DROP TABLE dsm_ai_relation";

// if ($conn->query($sql) !== TRUE) {
//     printError(103, "Error dropping table: " . $conn->error);
// }

// sql to create table dsm_ai_relation
$sql = "CREATE TABLE dsm_ai_relation (
id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
relation_id INT(8) UNSIGNED,
user_name VARCHAR(30),
priming_id INT(6) UNSIGNED,
add_priming_text VARCHAR(4096) DEFAULT '',
message_suffix VARCHAR(1024) DEFAULT '',
ai_policy INT(1) UNSIGNED,
FOREIGN KEY (relation_id) REFERENCES dsm_relation(id) ON DELETE CASCADE,
FOREIGN KEY (priming_id) REFERENCES dsm_ai_priming(id) ON DELETE CASCADE
)";

if ($conn->query($sql) === TRUE) {
    printSuccess("Table dsm_ai_relation created successfully");
}
else {
    printError(102, "Error creating table: " . $conn->error);
}

// sql to insert one row into dsm_ai_relation
$relationId = 2;
$userName = "Devo";
$primingId = 1;
$addPrimingText = "[NAME] hat viel Erfahrung mit Selbstfesselung, Klammern, Buttplugs, Deep Throating, Elektroplay, Knebeln, Atemkontrolle, High Heels, Korsett, Hingabe. Devo liebt es, mit Kosenamen angesprochen zu werden, die Deine Herrschaft über es ausdrücken, wie \"mein Spielzeug\", \"mein Eigentum\" oder \"mein Hündchen\". [NAME] liebt es, dauerhaft Erinnerungen an Dich am Körper zu spüren.  [NAME] will, dass Du den Orgasmus völlig kontrollierst.";
$aiPolicy=1;
$stmt = $conn->prepare("INSERT INTO dsm_ai_relation (relation_id, user_name, priming_id, add_priming_text, ai_policy) values (?, ?, ?, ?, ?)");
$stmt->bind_param("isisi", $relationId, $userName, $primingId, $addPrimingText, $aiPolicy);
$stmt->execute();
$stmt->close();
printSuccess("AI relation created");



$conn->close();
