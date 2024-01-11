<?php
include __DIR__ . '/check_language.php';
$error = $_GET['error'];
$errorText = $error ? _($error) : "";
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("Change password") ?></title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="js/jquery-3.6.2.min.js"></script>
</head>
<body>
	<div id="changepassword-container">
		<h1><?= _("Change password") ?></h1>
		<form action="perform_change_password.php" method="post">
			<div class="form-group password-container">
				<label for="old_password"><?= _("Old password") ?>:</label>
				<input type="password" id="old_password" class="password" name="old_password" required/>
				<span class="toggle-password" onclick="togglePassword()">
					<svg id="eyeIcon"><use href="images/icons.svg#icon-eye"/></svg>
					<svg id="eyeOffIcon" style="display: none;"><use href="images/icons.svg#icon-eye-off"/></svg>
				</span>
			</div>
			<div class="form-group password-container">
				<label for="new_password"><?= _("New password") ?>:</label>
				<input type="password" id="new_password" class="password" name="new_password" required/>
				<span class="toggle-password" onclick="togglePassword()">
					<svg id="eyeIcon"><use href="images/icons.svg#icon-eye"/></svg>
					<svg id="eyeOffIcon" style="display: none;"><use href="images/icons.svg#icon-eye-off"/></svg>
				</span>
			</div>
			<div class="form-group password-container">
				<label for="confirm_new_password"><?= _("Confirm new password") ?>:</label>
				<input type="password" class="password" id="confirm_new_password" name="confirm_new_password" required/>
				<span class="toggle-password" onclick="togglePassword()">
					<svg id="eyeIcon"><use href="images/icons.svg#icon-eye"/></svg>
					<svg id="eyeOffIcon" style="display: none;"><use href="images/icons.svg#icon-eye-off"/></svg>
				</span>
			</div>
			<div class="errormessage"><?= $errorText ?></div>
			<div class="container">
				<span class="left"> <input type="button" name="cancel" class="change-password-button" value="<?= _("Cancel") ?>"
					onclick="window.location='http://coachat.de/contacts.php'">
				</span> <span class="right"> <input type="submit" name="submit" class="change-password-button"
					value="<?= _("Change password") ?>"></span>
			</div>
		</form>
	</div>

    <script>
        $(document).ready(function() {
            $('.toggle-password').click(function() {
                var passwordInput = $('.password');
                var eyeIcon = $('.eyeIcon');
                var eyeOffIcon = $('.eyeOffIcon');
    
                if (passwordInput.attr('type') === 'password') {
                    passwordInput.attr('type', 'text');
                    eyeIcon.hide();
                    eyeOffIcon.show();
                } else {
                    passwordInput.attr('type', 'password');
                    eyeIcon.show();
                    eyeOffIcon.hide();
                }
            });
        });
    </script>
</body>
</html>
