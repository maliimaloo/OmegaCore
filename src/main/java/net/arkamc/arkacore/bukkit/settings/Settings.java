package net.arkamc.arkacore.bukkit.settings;

import org.mineacademy.fo.settings.SimpleSettings;

import java.util.List;

public final class Settings extends SimpleSettings {
    public static String SERVER_NAME;
    public static String LOG_PREFIX;
    public static List<String> ALIASES;

    private static void init() {
        Settings.setPathPrefix(null);
        SERVER_NAME = Settings.getString("Server_Name");
        LOG_PREFIX = getString("Log_Prefix");
        ALIASES = getStringList("Aliases");
    }

    public final static class Database {
        public static String PREFIX;

        public static String URL, USERNAME, PASSWORD;

        private static void init() {
            Settings.setPathPrefix("Database");
            PREFIX = Settings.getString("Prefix");
            URL = Settings.getString("Url");
            USERNAME = Settings.getString("Username");
            PASSWORD = Settings.getString("Password");
        }
    }

    public final static class Jedis {
        public static String PREFIX;

        public static String BUNGEE_IP, BUNGEE_PASSWORD;

        public static Integer BUNGEE_PORT;

        private static void init() {
            Settings.setPathPrefix("Jedis");
            PREFIX = Settings.getString("Prefix");
            BUNGEE_IP = Settings.getString("Bungee_Ip");
            BUNGEE_PASSWORD = Settings.getString("Bungee_Password");
            BUNGEE_PORT = Settings.getInteger("Bungee_Port", 4242);
        }
    }

    public static class Whisper {
        public static Boolean ENABLED;

        public static String FORMAT_SENDER, FORMAT_RECEIVING, MESSAGE_COLOR;

        private static void init() {
            Settings.setPathPrefix("Chat.Whisper");
            ENABLED = Settings.getBoolean("Enabled");
            FORMAT_SENDER = Settings.getString("Format_Sender");
            FORMAT_RECEIVING = Settings.getString("Format_Receiving");
            MESSAGE_COLOR = Settings.getString("Message_Color");
        }
    }

    public static class Socialspy {
        public static Boolean ENABLED;

        public static String FORMAT, MESSAGE_COLOR;

        private static void init() {
            Settings.setPathPrefix("Chat.Socialspy");
            ENABLED = Settings.getBoolean("Enabled");
            FORMAT = Settings.getString("Format");
            MESSAGE_COLOR = Settings.getString("Message_Color");
        }
    }
}
