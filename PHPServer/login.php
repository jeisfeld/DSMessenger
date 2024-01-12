<?php
include __DIR__ . '/check_language.php';
@$error = $_GET['error'];
@$errorText = $error ? _($error) : "";
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("Login") ?></title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="js/jquery-3.6.2.min.js"></script>
</head>
<body>
	<div id="login-container">
		<h1><?= _("Login") ?></h1>
		<form action="perform_login.php" method="post">
			<div class="form-group">
				<label for="username"><?= _("Username") ?>:</label>
				<input type="text" id="username" name="username" required/>
			</div>
			<div class="form-group password-container">
				<label for="password"><?= _("Password") ?>:</label>
				<input type="password" id="password" name="password"/>
				<span class="toggle-password" onclick="togglePassword()">
					<svg id="eyeIcon"><use href="images/icons.svg#icon-eye"/></svg>
					<svg id="eyeOffIcon" style="display: none;"><use href="images/icons.svg#icon-eye-off"/></svg>
				</span>
			</div>
			<div class="errormessage"><?= $errorText ?></div>
			<button type="submit" class="login-button"><?= _("Login") ?></button>
		</form>
		<div id="footer">
			<span class="left"></span> <span class="right"><a href="impressum.html" target="_blank"><?= _("Imprint") ?></a></span>
		</div>
	</div>

    <script>
        $(document).ready(function() {
            $('.toggle-password').click(function() {
                var passwordInput = $('#password');
                var eyeIcon = $('#eyeIcon');
                var eyeOffIcon = $('#eyeOffIcon');
    
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
