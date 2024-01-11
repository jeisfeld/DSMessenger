<?php
include __DIR__ . '/check_language.php';
$error = $_GET['error'];
$errorText = $error ? _($error) : "";
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("create_account") ?></title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="js/jquery-3.6.2.min.js"></script>
</head>
<body>
	<div id="createaccount-container">
		<h1><?= _("Create account") ?></h1>
		<form action="perform_create_account.php" method="post">
			<div class="form-group">
				<label for="username"><?= _("Username") ?>:</label>
				<input type="text" id="username" name="username" required/>
			</div>
			<div class="form-group password-container">
				<label for="password"><?= _("Password") ?>:</label>
				<input type="password" id="password" class="password" name="password" required/>
				<span class="toggle-password" onclick="togglePassword()">
					<svg id="eyeIcon"><use href="images/icons.svg#icon-eye"/></svg>
					<svg id="eyeOffIcon" style="display: none;"><use href="images/icons.svg#icon-eye-off"/></svg>
				</span>
			</div>
			<div class="form-group password-container">
				<label for="confirm_password"><?= _("Confirm password") ?>:</label>
				<input type="password" class="password" id="confirm_password" name="confirm_password" required/>
				<span class="toggle-password" onclick="togglePassword()">
					<svg id="eyeIcon"><use href="images/icons.svg#icon-eye"/></svg>
					<svg id="eyeOffIcon" style="display: none;"><use href="images/icons.svg#icon-eye-off"/></svg>
				</span>
			</div>
			<div class="errormessage"><?= $errorText ?></div>
			<div class="container">
				<span class="left"> <input type="button" name="cancel" class="change-password-button" value="<?= _("Cancel") ?>"
					onclick="window.location='http://coachat.de/contacts.php'">
				</span> 
				<span class="right"> <input type="submit" name="submit" class="change-password-button" value="<?= _("Create account") ?>"></span>
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
