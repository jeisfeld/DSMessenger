<?php
namespace dsmessenger;
require 'vendor/autoload.php';
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

include 'token.php';

function sendTextMessage($device, $messageText, $vibrate, $vibrationRepeated, $vibrationPattern, $displayOnLockScreen, $lockMessage, $keepScreenOn)
{
    $factory = (new Factory())->withServiceAccount(getKeyFileName());
    $messaging = $factory->createMessaging();

    $data = [
        'messageType' => 'TEXT',
        'messageText' => $messageText,
        'vibrate' => $vibrate,
        'vibrationRepeated' => $vibrationRepeated,
        'vibrationPattern' => $vibrationPattern,
        'displayOnLockScreen' => $displayOnLockScreen,
        'lockMessage' => $lockMessage,
        'keepScreenOn' => $keepScreenOn
    ];

    $token = ($device == 'tablet' ? getDeviceTokenTablet() : getDeviceToken());
    $message = CloudMessage::withTarget('token', $token)->withData($data)->withHighestPossiblePriority();
    $messaging->send($message);
}

if (isset($_POST['messageText'])) {
    sendTextMessage($_POST['device'], $_POST['messageText'], $_POST['vibrate'], $_POST['vibrationRepeated'], $_POST['vibrationPattern'], $_POST['displayOnLockScreen'], $_POST['lockMessage'], $_POST['keepScreenOn']);
}

function sendRandomImageMessage($device, $randomImageOrigin, $notificationName, $widgetName)
{
    $factory = (new Factory())->withServiceAccount(getKeyFileName());
    $messaging = $factory->createMessaging();

    $data = [
        'messageType' => 'RANDOMIMAGE',
        'randomImageOrigin' => $randomImageOrigin,
        'notificationName' => $notificationName,
        'widgetName' => $widgetName
    ];

    $token = ($device == 'tablet' ? getDeviceTokenTablet() : getDeviceToken());
    $message = CloudMessage::withTarget('token', $token)->withData($data)->withHighestPossiblePriority();
    $messaging->send($message);
}

if (isset($_POST['randomImageOrigin'])) {
    sendRandomImageMessage($_POST['device'], $_POST['randomImageOrigin'], $_POST['notificationName'], $_POST['widgetName']);
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

	<h2>Text message</h2>

	<form method="post" action="">
		<table>
			<tr>
				<td><label for="device">Device</label></td>
				<td>
					<fieldset style="border: 0; padding: 0;">
						<label for="handy">Handy</label><input type="radio" id="handy" name="device" value="handy" checked><label
							for="tablet">Tablet</label><input type="radio" id="tablet" name="device" value="tablet">
					</fieldset>
				</td>
			</tr>
			<tr>
				<td><label for="messageText">Message Text</label></td>
				<td colspan="3"><textarea rows="5" cols="50" id="messageText" name="messageText"></textarea></td>
			</tr>
			<tr>
				<td><label for="vibrate">Vibrate</label></td>
				<td><input type="checkbox" id="vibrate" name="vibrate" value="true"
					onclick="toggleCheckboxVisibility('vibrateDetails','vibrate')"></td>
				<td id="vibrateDetails" style="visibility: hidden;"><label for="vibrationRepeated">Repeat</label> <input
					type="checkbox" id="vibrationRepeated" name="vibrationRepeated" value="true"> <label for="vibrationPattern">Pattern</label>
					<select name="vibrationPattern" id="vibrationPattern">
						<option value="0">Default</option>
						<option value="1">Decrease</option>
						<option value="2">Wave</option>
						<option value="3">Slow increase</option>
				</select></td>
			</tr>
			<tr>
				<td><label for="displayOnLockScreen">Display on Lock Screen</label></td>
				<td><input type="checkbox" id="displayOnLockScreen" name="displayOnLockScreen" value="true"></td>
			</tr>
			<tr>
				<td><label for="lockMessage">Lock Message</label></td>
				<td><input type="checkbox" id="lockMessage" name="lockMessage" value="true"></td>
			</tr>
			<tr>
				<td><label for="keepScreenOn">Keep screen on</label></td>
				<td><input type="checkbox" id="keepScreenOn" name="keepScreenOn" value="true"></td>
			</tr>
		</table>
		<input type="submit" value="Submit">
	</form>

	<h2>Randomimage trigger</h2>

	<form method="post" action="">
		<table>
			<tr>
				<td><label for="device">Device</label></td>
				<td>
					<fieldset style="border: 0; padding: 0;">
						<label for="handy">Handy</label><input type="radio" id="handy" name="device" value="handy" checked><label
							for="tablet">Tablet</label><input type="radio" id="tablet" name="device" value="tablet">
					</fieldset>
				</td>
			</tr>
			<tr>
				<td><label for="randomImageOrigin">Origin</label></td>
				<td>
					<fieldset style="border: 0; padding: 0;">
						<label for="notificationType">Notification</label><input type="radio" id="notificationType"
							name="randomImageOrigin" value="NOTIFICATION" checked><label for="widgetType">Widget</label><input type="radio"
							id="widgetType" name="randomImageOrigin" value="WIDGET">
					</fieldset>
				</td>
			</tr>
			<tr id="notificationrow">
				<td><label for="notificationName">Notification Name</label></td>
				<td><select name="notificationName" id="notificationName">
						<option value="Special">Special</option>
				</select></td>
			</tr>
			<tr id="widgetrow">
				<td><label for="widgetName">Widget Name</label></td>
				<td><select name="widgetName" id="widgetName">
						<option value="SchnurpSy">SchnurpSy</option>
				</select></td>
			</tr>
		</table>
		<input type="submit" value="Submit">
	</form>

</body>
</html>