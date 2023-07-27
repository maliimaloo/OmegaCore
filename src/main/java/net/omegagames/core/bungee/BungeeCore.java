package net.omegagames.core.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.omegagames.core.bungee.api.jedis.DatabaseConnector;
import net.omegagames.core.bungee.api.jedis.RedisServer;
import net.omegagames.core.bungee.settings.BungeeSettings;
import net.omegagames.core.persistanceapi.ServerServiceManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BungeeCore extends Plugin {
    private static BungeeCore instance;

    private ServerServiceManager serverServiceManager;
    private DatabaseConnector databaseConnector;
    private ScheduledExecutorService executor;

    public BungeeCore() {}

    public static BungeeCore getInstance() {
        return instance;
    }

    public ServerServiceManager getServerServiceManager() {
        return this.serverServiceManager;
    }
    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }
    public ScheduledExecutorService getExecutor() {
        return this.executor;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.setupPlugin();
    }

    private void setupPlugin() {
        this.executor = Executors.newScheduledThreadPool(32);

        final String paramUrl = BungeeSettings.DATABASE_URL;
        final String paramUsername = BungeeSettings.DATABASE_USERNAME;
        final String paramPassword = BungeeSettings.DATABASE_PASSWORD;
        this.serverServiceManager = new ServerServiceManager(paramUrl, paramUsername, paramPassword);
        this.databaseConnector = new DatabaseConnector(this, this.redisServer());
    }

    private RedisServer redisServer() {
        final String paramBungeeIp = BungeeSettings.BUNGEE_IP;
        final String paramBungeePassword = BungeeSettings.BUNGEE_PASSWORD;
        final Integer paramBungeePort = BungeeSettings.BUNGEE_PORT;
        return new RedisServer(paramBungeeIp, paramBungeePort, paramBungeePassword);
    }
}
