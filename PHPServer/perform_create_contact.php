<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $contactId = 4;
    $myName = $_POST['modalCreateMyName'];
    $contactName = $_POST['modalCreateContactName'];
    $aiPolicy = $_POST['modalCreateAiPolicy'];
    $aiPrimingId = $_POST['modalCreateAiPrimingId'];
    
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    $stmt = $conn->prepare("INSERT INTO dsm_relation (slave_id, master_id, slave_name, master_name) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("iiss", $userId, $contactId, $myName, $contactName);

    $stmt->execute();
    $success = $stmt -> affected_rows;
    $stmt->close();
    
    if ($success) {
        $relationId = null;
        $stmt = $conn->prepare("SELECT MAX(id) FROM dsm_relation WHERE slave_id=? AND master_id=?");
        $stmt->bind_param("ii", $userId, $contactId);
        $stmt->execute();
        $stmt->bind_result($relationId);
        $stmt->fetch();
        $stmt->close();
        
        if ($relationId) {
            $stmt = $conn->prepare("INSERT INTO dsm_ai_relation (relation_id, user_name, priming_id, add_priming_text, ai_policy) values (?, ?, ?, '', ?)");
            $stmt->bind_param("isii", $relationId, $myName, $aiPrimingId, $aiPolicy);
            $stmt->execute();
            $stmt->close();
        }
        sendAdminMessage($conn, $username, $password, $relationId, "INVITATION_ACCEPTED");
    }
    
    header("Location: contacts.php");

    $conn->close();
}
?>