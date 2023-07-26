package net.omegagames.core.bungee.settings;

import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.settings.YamlConfig;

@Getter
@AutoRegister
public final class BungeeSettings extends YamlConfig {
    @Getter
    private final static BungeeSettings instance = new BungeeSettings();

    public static String DATABASE_URL;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;

    private BungeeSettings() {
        this.loadConfiguration(NO_DEFAULT, "bungee_settings.yml");
    }

    @Override
    protected void onLoad() {
        super.setPathPrefix("Database");
        DATABASE_URL = getString("Url");
        DATABASE_USERNAME = getString("Username");
        DATABASE_PASSWORD = getString("Password");
    }
}
