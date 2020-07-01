package net.strukteon.myrpc.settings;

import java.util.*;
import java.nio.charset.*;
import net.strukteon.myrpc.utils.*;
import com.google.gson.*;
import java.io.*;

public class Settings
{
    private static final File SETTINGS_FILE;
    public String mrpVersion;
    public boolean minimizeToTaskbar;
    public boolean isResizable;
    public double windowHeight;
    public double windowWidth;
    public List<Application> applications;
    
    public Settings() {
        this.mrpVersion = "3.1.3";
        this.minimizeToTaskbar = false;
        this.isResizable = false;
        this.windowHeight = 0.0;
        this.windowWidth = 0.0;
        this.applications = new ArrayList<Application>(Collections.singletonList(new Application()));
    }
    
    public static boolean initFolder() {
        return Static.SETTINGS_FOLDER.exists() || Static.SETTINGS_FOLDER.mkdirs();
    }
    
    public void saveSettings() {
        this.mrpVersion = "3.1.3";
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(Settings.SETTINGS_FILE), StandardCharsets.UTF_8);
            gson.toJson(this, os);
            os.close();
        }
        catch (IOException e) {
            Logger.LOG("An error occurred while trying to save settings", new Object[0]);
        }
    }
    
    public static Settings loadSettings() throws IOException {
        if (!Settings.SETTINGS_FILE.exists()) {
            Logger.LOG("Settings file does not exist, and will be therefore created", new Object[0]);
            if (Settings.SETTINGS_FILE.createNewFile()) {
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                final OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(Settings.SETTINGS_FILE), StandardCharsets.UTF_8);
                gson.toJson(new Settings(), os);
                os.close();
                Logger.LOG("Settings file was successfully created", new Object[0]);
            }
            else {
                Logger.LOG("Error: Could not create the settings file", new Object[0]);
                Logger.LOG("You will not be able to save any settings changed.", new Object[0]);
            }
            return new Settings();
        }
        final Settings settings = new Gson().fromJson(new FileReader(Settings.SETTINGS_FILE), Settings.class);
        Logger.LOG("Settings successfully loaded", new Object[0]);
        return settings;
    }
    
    static {
        SETTINGS_FILE = new File(Static.SETTINGS_FOLDER, "settings.json");
    }
}
