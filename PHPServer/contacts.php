<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/usermanagement/querycontacts.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>DS Messenger - Contacts</title>
<link rel="stylesheet" type="text/css" href="styles.css">
<!-- Add CSS for styling -->
</head>
<body>
	<div class="container">
		<span class="left">Username: <?= $username ?></span>
		<span class="right"><a href="logout.php">Logout</a></span>
	</div>

	<h1>Contacts</h1>

	<ul>
    <?php
    $contacts = queryContacts($username, $password);
    foreach ($contacts as $contact) {
        echo "<li><a href='conversations.php?contact_id=" . $contact['contactId'] . "&relationId=" . $contact['relationId'] . "&contactName=" . $contact['contactName'] . "&isSlave=" . $contact['isSlave'] . "'>" . $contact['contactName'] . "</a></li>";
    }
    ?>
</ul>
</body>
</html>
