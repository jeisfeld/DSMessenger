<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/conversation/queryconversations.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>DS Messenger - Conversations</title>
<link rel="stylesheet" type="text/css" href="styles.css">
<!-- Add CSS for styling -->
</head>
<body>
	<div class="container">
		<span class="left">Username: <?= $username ?></span>
		<span class="right"><a href="logout.php">Logout</a></span>
	</div>
	
	<h1>Conversations with <?= $_GET['contactName'] ?></h1>
	<ul>
    <?php
    $username = $_SESSION['username'];
    $password = $_SESSION['password'];
    $relationId = $_GET['relationId'];
    $isSlave = $_GET['isSlave'];
    
    $conversations = queryConversations($username, $password, $relationId, $isSlave);
    
    foreach ($conversations as $conversation) {
        echo "<li><a href='messages.php?conversationId=" . $conversation['conversationId'] . 
        "&relationId=" . $relationId . "&isSlave=" . $isSlave . "&subject=" . $conversation['subject'] .
        "&contactName=" . $_GET['contactName'] . "'>" . $conversation['subject'] . "</a></li>";
    }
    ?>
</ul>
</body>
</html>
