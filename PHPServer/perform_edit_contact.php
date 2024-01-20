<?php
include __DIR__ . '/check_session.php';
require_once 'firebase/firebasefunctions.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
$userId = $_SESSION['userId'];

if (isset($_POST['submit'])) {
    $relationId = $_POST['relationId'];
    $isSlave = $_POST['isSlave'];
    $aiRelationId = $_POST['aiRelationId'];
    $myName = $_POST['modalEditMyName'];
    $contactName = $_POST['modalEditContactName'];
    $aiUsername = $_POST['modalEditAiUsername'];
    $aiPolicy = $_POST['modalEditAiPolicy'];
    $aiPrimingId = $_POST['modalEditAiPrimingId'];
    $aiAddPrimingText = $_POST['modalEditAddPrimingText'];
    $aiMessageSuffix = $_POST['modalEditMessageSuffix'];
    
    // Create connection
    $conn = getDbConnection();

    // Check connection
    if ($conn->connect_error) {
        printError(101, "Connection failed: " . $conn->connect_error);
    }
    
    if ($isSlave) {
        $stmt = $conn->prepare("update dsm_relation set slave_name = ?, master_name = ? where id = ? and master_id = ?");
    }
    else {
        $stmt = $conn->prepare("update dsm_relation set master_name = ?, slave_name = ? where id = ? and slave_id = ?");
    }
    $stmt->bind_param("ssii", $contactName, $myName, $relationId, $userId);

    $stmt->execute();
    $stmt->close();
    
    sendAdminMessage($conn, $username, $password, $relationId, "CONTACT_UPDATED");
    
    if ($aiRelationId) {
        $stmt = $conn->prepare("update dsm_ai_relation set user_name = ?, priming_id = ?, add_priming_text = ?, message_suffix = ?, ai_policy = ? where id = ? and relation_id = ? and relation_id in 
(SELECT id from dsm_relation where slave_id = ? or master_id = ?)");
        $stmt->bind_param("sissiiiii", $aiUsername, $aiPrimingId, $aiAddPrimingText, $aiMessageSuffix, $aiPolicy, $aiRelationId, $relationId, $userId, $userId);
        $stmt->execute();
        $stmt->close();
    }
    
    header("Location: contacts.php");

    $conn->close();
}
?>