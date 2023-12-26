<?php 
include __DIR__ . '/check_language.php';
?>
<!DOCTYPE html>
<html>
<head>
<title>DS Messenger - Login</title>
<link rel="stylesheet" type="text/css" href="styles.css">
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
            <button type="submit" class="login-button"><?= _("login") ?></button>
        </form>
    </div>
</body>
</html>
