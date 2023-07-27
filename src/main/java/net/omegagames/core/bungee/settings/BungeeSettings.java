package net.omegagames.core.bungee.settings;

import lombok.Getter;
import net.omegagames.core.bukkit.settings.Settings;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.settings.YamlConfig;

@Getter
@AutoRegister
public final class BungeeSettings extends YamlConfig {
    @Getter
    private final static BungeeSettings instance = new BungeeSettings();

    public static String DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD;

    public static String BUNGEE_IP, BUNGEE_PASSWORD;
    public static Integer BUNGEE_PORT;

    private BungeeSettings() {
        this.loadConfiguration(NO_DEFAULT, "bungee_settings.yml");
    }

    @Override
    protected void onLoad() {
        super.setPathPrefix("Database");
        DATABASE_URL = super.getString("Url");
        DATABASE_USERNAME = super.getString("Username");
        DATABASE_PASSWORD = super.getString("Password");

        super.setPathPrefix("Jedis");
        BUNGEE_IP = super.getString("Bungee_Ip");
        BUNGEE_PASSWORD = super.getString("Bungee_Password");
        BUNGEE_PORT = super.getInteger("Bungee_Port", 4242);
    }
}
