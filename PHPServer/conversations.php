<?php
include __DIR__ . '/check_session.php';
require_once __DIR__ . '/db/conversation/queryconversations.php';
$username = $_SESSION['username'];
$password = $_SESSION['password'];
?>
<!DOCTYPE html>
<html>
<head>
<title>DS Messenger - Conversations</title>
<link rel="stylesheet" href="styles.css">
</head>
<body>
	<div id="conversations-container">
		<div id="header">
			<span class="left"><?= _("username") ?>: <?= $username ?></span> <span class="right">
			<a href="contacts.php"><?= _("contacts") ?></a>&nbsp;<a href="logout.php"><?= _("logout") ?></a></span>
		</div>
		<h1><?= sprintf(_("conversations_with"), $_GET['contactName']) ?></h1>
		<ul id="conversation-list">
            <?php
            $username = $_SESSION['username'];
            $password = $_SESSION['password'];
            $relationId = $_GET['relationId'];
            $isSlave = $_GET['isSlave'];
            $contactName = $_GET['contactName'];
            $replyPolicy = $_GET['replyPolicy'];
            
            $conversations = queryConversations($username, $password, $relationId, $isSlave);
            
            foreach ($conversations as $conversation) {
                echo "<li><a class='conversation-item' href='messages.php?conversationId=" . $conversation['conversationId'] .
                "&relationId=" . $relationId . "&isSlave=" . $isSlave . "&replyPolicy=" . $replyPolicy . "&subject=" . $conversation['subject'] .
                "&contactName=" . $contactName . "'>" . $conversation['subject'] . "</a></li>";
            }
            ?>
            
        </ul>
		<div id="message-input">
			<form action="sendmessage.php" method="post" class="message-form">
				<input type="hidden" name="conversationId" value="">
				<input type="hidden" name="relationId" value="<?= $relationId ?>"> 
				<input type="hidden" name="isSlave" value="<?= $isSlave ?>">
				<input type="hidden" name="subject" value="">
				<input type="hidden" name="replyPolicy" value="<?= $replyPolicy ?>">
				<input type="hidden" name="contactName" value="<?= $contactName ?>">
				<textarea name="message" placeholder="<?= _("start_new_conversation") ?>" class="message-textarea"></textarea>
				<button type="submit" class="send-button"><?= _("send") ?></button>
			</form>
		</div>
	</div>
</body>
</html>

