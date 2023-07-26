package net.omegagames.core.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.omegagames.core.bungee.settings.BungeeSettings;
import net.omegagames.core.persistanceapi.ServerServiceManager;

public class BungeeCore extends Plugin {
    private ServerServiceManager serverServiceManager;

    public BungeeCore() {}

    public ServerServiceManager getServerServiceManager() {
        return this.serverServiceManager;
    }

    @Override
    public void onEnable() {
        final String paramUrl = BungeeSettings.DATABASE_URL;
        final String paramUsername = BungeeSettings.DATABASE_USERNAME;
        final String paramPassword = BungeeSettings.DATABASE_PASSWORD;

        this.serverServiceManager = new ServerServiceManager(paramUrl, paramUsername, paramPassword);
    }
}
