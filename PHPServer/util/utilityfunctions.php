<?php
function printError($errorCode, $errorMessage, $data = null) {
    $result = [
        'status' => 'error',
        'errorcode' => $errorCode,
        'errormessage' => $errorMessage
    ];
    if ($data) {
        foreach ($data as $key => $value) {
            $result[$key] = $value;
        }
    }
    die (json_encode($result, JSON_PRETTY_PRINT));
}

function printSuccess($message, $data = null) {
    $result = [ 'status' => 'success', 'message' => $message ];
    if ($data) {
        foreach ($data as $key => $value) {
            $result[$key] = $value;
        }
    }
    echo json_encode($result, JSON_PRETTY_PRINT);
}


function convertJavaTimestamp($javaTimestamp) {
    $seconds = floor($javaTimestamp / 1000);
    $milliseconds = $javaTimestamp % 1000;
    $dateTime = new DateTime();
    $dateTime->setTimestamp($seconds);
    $mysqlTimestamp = $dateTime->format("Y-m-d H:i:s") . '.' . sprintf("%03d", $milliseconds);
    return $mysqlTimestamp;
}

function convertToJavaTimestamp($mysqlTimestamp) {
    $dateTime = DateTime::createFromFormat('Y-m-d H:i:s.u', $mysqlTimestamp);
    $seconds = $dateTime->getTimestamp();
    $milliseconds = intval($dateTime->format("u"));
    $javaTimestamp = ($seconds * 1000) + ($milliseconds / 1000);
    return $javaTimestamp; 
}

function convertTimestamp($mysqlTimestamp) {
    if (!$mysqlTimestamp) {
        return "";
    }
    $timestampDateTime = DateTime::createFromFormat('Y-m-d H:i:s.u', $mysqlTimestamp);
    $todayDateTime = new DateTime();
    $todayDateTime->setTime(0, 0, 0); // Reset time part to 00:00:00 for accurate comparison
    if ($timestampDateTime->format('Y-m-d') === $todayDateTime->format('Y-m-d')) {
        return $timestampDateTime->format('H:i');
    }
    else if ($timestampDateTime->format('Y') === $todayDateTime->format('Y')) {
        return $timestampDateTime->format('d.m. H:i');
    }
    else {
        return $timestampDateTime->format('d.m.Y H:i');
    }
}

function consoleLog($debugData) {
    ob_start(); // Start output buffering
    print_r($debugData); // Print the object
    $debugDataAsString = ob_get_clean();
    echo "<script>console.log('Debug Data: " . json_encode($debugDataAsString) . "');</script>";
}

