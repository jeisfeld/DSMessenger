<?php
function getPreferredLanguage($default = 'en_US') {
    if (isset($_SERVER['HTTP_ACCEPT_LANGUAGE'])) {
        $langs = explode(',', $_SERVER['HTTP_ACCEPT_LANGUAGE']);
        if (empty($langs)) {
            return $default;
        }
        
        foreach ($langs as $lang) {
            // Extract the primary language part (e.g., "en" from "en-US")
            $primaryLang = explode('-', $lang)[0];
            switch ($primaryLang) {
                case 'de':
                    return 'de_DE';
                case 'en':
                    return 'en_US';
                    // Add more languages here as needed
                default:
                    return $default;
            }
        }
    }
    
    return $default; // Return default language if no match is found
}

$preferredLang = getPreferredLanguage();
putenv("LC_ALL=$preferredLang");
setlocale(LC_ALL, $preferredLang);
bindtextdomain("messages", "./locales");
textdomain("messages");
?>
