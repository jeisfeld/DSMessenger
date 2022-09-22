<?php
require 'vendor/autoload.php';
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

include 'token.php';

function sendTextMessage($messageText, $vibrate, $vibrationRepeated, $vibrationPattern, $onlockscreen, $lockMessage, $keepScreenOn)
{
    $factory = (new Factory())->withServiceAccount(getKeyFileName());
    $messaging = $factory->createMessaging();

    $data = [
        'messageType' => 'TEXT',
        'messageText' => $messageText,
        'vibrate' => $vibrate,
        'vibrationRepeated' => $vibrationRepeated,
        'vibrationPattern' => $vibrationPattern,
        'displayOnLockScreen' => $onlockscreen,
        'lockMessage' => $lockMessage,
        'keepScreenOn' => $keepScreenOn
    ];

    $message = CloudMessage::withTarget('token', getDeviceToken())->withData($data)->withHighestPossiblePriority();
    $messaging->send($message);
}

if (isset($_POST['messagetext'])) {
    sendTextMessage($_POST['messagetext'], $_POST['vibrate'], $_POST['vibrationrepeated'], $_POST['vibrationpattern'], $_POST['onlockscreen'], $_POST['lockmessage'], $_POST['keepscreenon']);
}

function sendRandomImageMessage($randomImageOrigin, $notificationName, $widgetName)
{
    $factory = (new Factory())->withServiceAccount(getKeyFileName());
    $messaging = $factory->createMessaging();
    
    $data = [
        'messageType' => 'RANDOMIMAGE',
        'randomImageOrigin' => $randomImageOrigin,
        'notificationName' => $notificationName,
        'widgetName' => $widgetName
    ];
    
    $message = CloudMessage::withTarget('token', getDeviceToken())->withData($data)->withHighestPossiblePriority();
    $messaging->send($message);
}


if (isset($_POST['randomimageorigin'])) {
    sendRandomImageMessage($_POST['randomimageorigin'], $_POST['notificationname'], $_POST['widgetname']);
}

?>


<!DOCTYPE html>
<html lang="en">
<head>
<title>Send message to Jörg</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Language" content="en">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<script type="text/javascript">
function toggleCheckboxVisibility(elementId, checkboxId) {
  var element = document.getElementById(elementId);
  var checkbox = document.getElementById(checkboxId);
  if(checkbox.checked==true) {
	element.style.visibility = "visible";
  }
  else {
	element.style.visibility = "hidden";
  }
}
</script>

</head>

<body id="overview">

	<h1>Send message to Jörg</h1>

	<form method="post" action="">
		<table>
			<tr>
				<td><label for="messagetext">Message Text</label></td>
				<td colspan="3"><textarea rows="5" cols="50" id="messagetext" name="messagetext"></textarea></td>
			</tr>
			<tr>
				<td><label for="vibrate">Vibrate</label></td>
				<td><input type="checkbox" id="vibrate" name="vibrate" value="true"
					onclick="toggleCheckboxVisibility('vibratedetails','vibrate')"></td>
				<td id="vibratedetails" style="visibility: hidden;"><label for="vibrationrepeated">Repeat</label> <input
					type="checkbox" id="vibrationrepeated" name="vibrationrepeated" value="true"> <label for="vibrationpattern">Pattern</label>
					<select name="vibrationpattern" id="vibrationpattern">
						<option value="0">Default</option>
						<option value="1">Decrease</option>
						<option value="2">Wave</option>
						<option value="3">Slow increase</option>
				</select></td>
			</tr>
			<tr>
				<td><label for="onlockscreen">Display on Lock Screen</label></td>
				<td><input type="checkbox" id="onlockscreen" name="onlockscreen" value="true"></td>
			</tr>
			<tr>
				<td><label for="lockmessage">Lock Message</label></td>
				<td><input type="checkbox" id="lockmessage" name="lockmessage" value="true"></td>
			</tr>
			<tr>
				<td><label for="keepscreenon">Keep screen on</label></td>
				<td><input type="checkbox" id="keepscreenon" name="keepscreenon" value="true"></td>
			</tr>
		</table>
		<input type="submit" value="Submit">
	</form>

	<h1>Trigger RandomImage at Jörg</h1>

	<form method="post" action="">
		<table>
			<tr>
				<td><label for="randomimageorigin">Origin</label></td>
				<td>
					<fieldset style="border: 0; padding:0;">
						<label for="randomimageorigin">Notification</label><input type="radio" id="notificationType" name="randomimageorigin"
							value="NOTIFICATION" checked><label for="widgetType">Widget</label><input type="radio" id="widgetType"
							name="randomimageorigin" value="WIDGET">
					</fieldset>
				</td>
			</tr>
			<tr id="notificationrow">
				<td><label for="notificationname">Notification Name</label></td>
				<td><select name="notificationname" id="notificationname">
						<option value="Special">Special</option>
				</select></td>
			</tr>
			<tr id="widgetrow">
				<td><label for="widgetname">Widget Name</label></td>
				<td><select name="widgetname" id="widgetname">
						<option value="SchnurpSy">SchnurpSy</option>
				</select></td>
			</tr>
		</table>
		<input type="submit" value="Submit">
	</form>

</body>
</html>