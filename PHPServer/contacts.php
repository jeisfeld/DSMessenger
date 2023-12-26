<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/usermanagement/querycontacts.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>Contacts</title>
<link rel="stylesheet" href="styles.css">
</head>
<body>
	<div id="contacts-container">
		<div id="header">
			<span class="left">Username: <?= $username ?></span> <span class="right"><a href="logout.php">Logout</a></span>
		</div>
		<h1>Contacts</h1>
		<ul id="contact-list">
            <?php
            // Assuming you have a function getContacts() that returns an array of contacts
            $contacts = queryContacts($username, $password);
            foreach ($contacts as $contact) {
                echo "<li><a class='contact-item' href='conversations.php?relationId=" . $contact['relationId'] . "&contactName=" . $contact['contactName'] . "&isSlave=" . $contact['isSlave'] . "'>" . $contact['contactName'] . "</a></li>";
            }
            ?>
        </ul>
	</div>
</body>
</html>