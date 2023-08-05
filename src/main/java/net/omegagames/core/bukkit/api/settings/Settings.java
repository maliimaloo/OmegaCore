package net.omegagames.core.bukkit.api.settings;

import net.omegagames.core.bukkit.api.scoreboard.ScoreboardData;
import org.mineacademy.fo.settings.SimpleSettings;

import java.util.Set;

public final class Settings extends SimpleSettings {
    public static String SERVER_NAME;

    private static void init() {
        Settings.setPathPrefix(null);
        SERVER_NAME = Settings.getString("Server_Name");
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

    public final static class Scoreboard {
        public static Set<ScoreboardData> SCOREBOARD_DATA;

        private static void init() {
            Settings.setPathPrefix(null);
            SCOREBOARD_DATA = Settings.getSet("Scoreboard", ScoreboardData.class);
        }
    }
}
