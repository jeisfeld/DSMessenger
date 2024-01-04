<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/usermanagement/querycontacts.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("contacts") ?></title>
<link rel="stylesheet" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
	<div id="contacts-container">
		<div id="header">
			<span class="left"><?= _("username") ?>: <?= $username ?></span>
			<span class="right"><a href="change_password.php"><?= _("change_password") ?></a>&nbsp;<a href="logout.php"><?= _("logout") ?></a></span>
		</div>
		<h1><?= _("contacts") ?></h1>
		<ul id="contact-list">
            <?php
            // Assuming you have a function getContacts() that returns an array of contacts
            $contacts = queryContacts($username, $password);
            foreach ($contacts as $contact) {
                echo "<li class='contact-item'><a href='conversations.php?relationId=" . $contact['relationId'] . "'>" . 
                ($contact['isSlave'] ? "" : "<b>") . $contact['contactName'] . ($contact['isSlave'] ? "" : "</b>") . "</a></li>";
            }
            ?>
        </ul>
		<div id="footer">
			<span class="left"></span> <span class="right"><a href="impressum.html" target="_blank"><?= _("imprint") ?></a></span>
		</div>
	</div>
</body>
</html>