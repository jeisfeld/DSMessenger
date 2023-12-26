<!DOCTYPE html>
<html>
<head>
<title>DS Messenger - Login</title>
<link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
    <div id="login-container">
        <h1>Login</h1>
        <form action="perform_login.php" method="post">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit" class="login-button">Login</button>
        </form>
    </div>
</body>
</html>
