<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/usermanagement/querycontacts.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("Contacts") ?></title>
<link rel="stylesheet" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
	<div id="contacts-container">
		<div id="header">
			<span class="left"><?= _("Username") ?>: <?= $username ?></span>
			<span class="right"><a href="change_password.php"><?= _("Change password") ?></a>&nbsp;<a href="logout.php"><?= _("Logout") ?></a></span>
		</div>
		<h1><?= _("Contacts") ?>
			<span class="right"><svg id="button-reload" onclick="location.reload()" class="icon"><use href="images/icons.svg#icon-reload"></use></svg></span>
		</h1>
		<ul id="contact-list">
            <?php
            // Assuming you have a function getContacts() that returns an array of contacts
            $contacts = queryContacts($username, $password);
            foreach ($contacts as $contact) {
                echo '<li class="contact-item"><a href="conversations.php?relationId=' . $contact['relationId'] . '">' . 
                ($contact['isSlave'] ? '' : '<b>') . $contact['contactName'] . ($contact['isSlave'] ? '' : '</b>') . '</a></li>';
            }
            ?>
        </ul>
		<div id="footer">
			<span class="left"></span> <span class="right"><a href="impressum.html" target="_blank"><?= _("Imprint") ?></a></span>
		</div>
	</div>
</body>
</html>