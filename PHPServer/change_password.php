<?php
include __DIR__ . '/check_language.php';
$error = $_GET['error'];
$errorText = $error ? _($error) : "";
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - Change Password</title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
	<div id="changepassword-container">
		<h1><?= _("change_password") ?></h1>
		<form action="perform_change_password.php" method="post">
			<div class="form-group">
				<label for="old_password"><?= _("old_password") ?>:</label> <input type="password" id="old_password"
					name="old_password" required>
			</div>
			<div class="form-group">
				<label for="new_password"><?= _("new_password") ?>:</label> <input type="password" id="new_password"
					name="new_password" required>
			</div>
			<div class="form-group">
				<label for="confirm_new_password"><?= _("confirm_new_password") ?>:</label> <input type="password"
					id="confirm_new_password" name="confirm_new_password" required>
			</div>
			<div class="errormessage"><?= $errorText ?></div>
			<div class="container">
				<span class="left"> <input type="button" name="cancel" class="change-password-button" value="<?= _("cancel") ?>"
					onclick="window.location='http://coachat.de/contacts.php'">
				</span> <span class="right"> <input type="submit" name="submit" class="change-password-button"
					value="<?= _("change_password") ?>"></span>
			</div>
		</form>
	</div>
</body>
</html>
