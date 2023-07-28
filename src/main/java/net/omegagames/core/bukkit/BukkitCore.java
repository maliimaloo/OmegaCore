package net.omegagames.core.bukkit;

import net.omegagames.core.bukkit.api.jedis.DatabaseConnector;
import net.omegagames.core.bukkit.api.jedis.RedisServer;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.persistanceapi.ServerServiceManager;
import org.bukkit.Bukkit;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.plugin.SimplePlugin;
import net.omegagames.core.bukkit.settings.Settings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("unused")
public class BukkitCore extends SimplePlugin {
    private ApiImplementation api;
    private ServerServiceManager serverServiceManager;
    private DatabaseConnector databaseConnector;
    private DebugListener debugListener;
    private ScheduledExecutorService executor;

    private String serverName;

    public BukkitCore() {}

    public ApiImplementation getAPI() {
        return this.api;
    }

    public ServerServiceManager getServerServiceManager() {
        return this.serverServiceManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public DebugListener getDebugListener() {
        return this.debugListener;
    }

    public ScheduledExecutorService getExecutor() {
        return this.executor;
    }

    public String getServerName() {
        return this.serverName;
    }

    @Override
    protected void onPluginStart() {
        this.setupPlugin();
    }

    @Override
    protected void onPluginStop() {
        this.getDatabaseConnector().killConnection();
    }

    private void setupPlugin() {
        this.executor = Executors.newScheduledThreadPool(32);

        this.serverName = Settings.SERVER_NAME;
        if (Valid.isNullOrEmpty(this.serverName)) {
            Debugger.saveError(new FoException(), "Impossible d'activer le plugin: ServerName est vide !");
            super.setEnabled(false);
            Bukkit.getServer().shutdown();
            return;
        }

        this.debugListener = new DebugListener();

        final String paramUrl = Settings.Database.URL;
        final String paramUsername = Settings.Database.USERNAME;
        final String paramPassword = Settings.Database.PASSWORD;
        this.serverServiceManager = new ServerServiceManager(paramUrl, paramUsername, paramPassword);
        this.databaseConnector = new DatabaseConnector(this, this.redisServer());

        this.api = new ApiImplementation(this);

        this.api.getBungeeResource().flushAll();
        super.registerEvents(new GlobalJoinListener(this));
    }

    private RedisServer redisServer() {
        final String paramBungeeIp = Settings.Jedis.BUNGEE_IP;
        final String paramBungeePassword = Settings.Jedis.BUNGEE_PASSWORD;
        final Integer paramBungeePort = Settings.Jedis.BUNGEE_PORT;
        return new RedisServer(paramBungeeIp, paramBungeePort, paramBungeePassword);
    }

    @Override
    public boolean suggestPaper() {
        return false;
    }

    public MinecraftVersion.V getMinimumVersion() {
        return MinecraftVersion.V.v1_8;
    }

    public static String getAuthor() {
        return "Maliimaloo";
    }

    public static BukkitCore getInstance() {
        return (BukkitCore) SimplePlugin.getInstance();
    }
}