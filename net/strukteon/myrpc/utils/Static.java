package net.strukteon.myrpc.utils;

import java.io.*;

public class Static
{
    public static final String VERSION = "3.1.3";
    public static final String DEFAULT_APP_ID = "401775087440756737";
    public static final File SETTINGS_FOLDER;
    public static final int WS_PORT = 50403;
    public static final String JFX_VERSION = "11.0.1";
    public static final String JFX_SDK_FOLDER;
    public static final String[] CSS_FILES;
    public static final String[] PRESENCE_MODES;
    public static final String EXTENSION_FIREFOX = "https://addons.mozilla.org/firefox/addon/my-rich-presence/";
    public static final String EXTENSION_CHROME = "https://chrome.google.com/webstore/detail/my-rich-presence/cgeaniepnccnmoogokikkplihmgmhocl";
    public static final String UPDATE_LOG;
    public static final String DONATE_PAGE = "http://strukteon.net/donate";
    public static final String VERSION_URL = "https://strukteon.net/projects/myrichpresence/newestversion.php";
    public static final String DOWNLOAD_URL = "https://strukteon.net/projects/myrichpresence/";
    public static final String FILE_EXTENSION = "mrpc";
    
    static {
        SETTINGS_FOLDER = new File("files");
        JFX_SDK_FOLDER = String.format("javafx-sdk-%s", "11.0.1");
        CSS_FILES = new String[] { "style/default/discord_style/check-box.css", "style/default/discord_style/choicebox.css", "style/default/discord_style/tabpane.css", "style/default/discord_style/textarea.css", "style/default/discord_style/text-field.css", "style/default/discord_style/button.css", "style/default/discord_style/progress-bar.css" };
        PRESENCE_MODES = new String[] { "Custom Presence", "Browser Extension" };
        UPDATE_LOG = String.format("http://strukteon.net/projects/myrichpresence/updatelog/?v=%s", "3.1.3");
    }
}
