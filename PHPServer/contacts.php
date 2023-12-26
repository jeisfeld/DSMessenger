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
			<span class="left"><?= _("username") ?>: <?= $username ?></span> <span class="right"><a href="logout.php"><?= _("logout") ?></a></span>
		</div>
		<h1><?= _("contacts") ?></h1>
		<ul id="contact-list">
            <?php
            // Assuming you have a function getContacts() that returns an array of contacts
            $contacts = queryContacts($username, $password);
            foreach ($contacts as $contact) {
                $replyPolicy = substr($contact["slavePermissions"], 3, 1);
                echo "<li><a class='contact-item' href='conversations.php?relationId=" . $contact['relationId'] . "&contactName=" . $contact['contactName'] 
                . "&isSlave=" . $contact['isSlave'] . "&replyPolicy=" . $replyPolicy . "'>" . $contact['contactName'] . "</a></li>";
            }
            ?>
        </ul>
	</div>
</body>
</html>