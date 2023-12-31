<?php 
include __DIR__ . '/check_language.php';
$error=$_GET['error'];
$errorText = $error ? _($error) : "";
?>
<!DOCTYPE html>
<html>
<head>
<title>Coachat - <?= _("login") ?></title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
<link rel="icon" type="image/x-icon" href="images/favicon.ico">
</head>
<body>
    <div id="login-container">
        <h1><?= _("login") ?></h1>
        <form action="perform_login.php" method="post">
            <div class="form-group">
                <label for="username"><?= _("username") ?>:</label>
                <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
                <label for="password"><?= _("password") ?>:</label>
                <input type="password" id="password" name="password" required>
            </div>
			<div class="errormessage"><?= $errorText ?></div>
            <button type="submit" class="login-button"><?= _("login") ?></button>
        </form>
    </div>
</body>
</html>
