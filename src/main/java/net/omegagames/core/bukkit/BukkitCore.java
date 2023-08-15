package net.omegagames.core.bukkit;

import lombok.Getter;
import net.omegagames.core.bukkit.api.commands.CreditCommand;
import net.omegagames.core.bukkit.api.expansion.player.PlayerPlaceholderExpansion;
import net.omegagames.core.bukkit.api.expansion.server.ServerPlaceholderExpansion;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.api.settings.Settings;
import net.omegagames.core.jedis.DatabaseConnector;
import net.omegagames.core.jedis.RedisServer;
import net.omegagames.core.persistanceapi.SqlServiceManager;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Classe principale du plugin.
 */
public class BukkitCore extends SimplePlugin {
    @Getter
    private ApiImplementation api;
    @Getter
    private SqlServiceManager sqlServiceManager;
    @Getter
    private DatabaseConnector databaseConnector;
    @Getter
    private DebugListener debugListener;

    @Getter
    private ScheduledExecutorService executor;
    @Getter
    private String serverName;

    public BukkitCore() {
    }

    /* -------------------------------------------------------
     * Méthodes d'allumage et d'extinction du plugin.
     * ------------------------------------------------------- */
    @Override
    protected void onPluginStart() {
        // Configuration du plugin lors de son démarrage
        setupPlugin();
    }

    @Override
    protected void onPluginStop() {
        // Actions de nettoyage lorsque le plugin s'arrête
        if (this.databaseConnector != null) {
            this.databaseConnector.killConnection();
        }

        if (this.sqlServiceManager != null && this.sqlServiceManager.getDatabaseManager() != null) {
            this.sqlServiceManager.getDatabaseManager().close();
        }
    }

    /**
     * Configuration du plugin en initialisant différentes composantes et services.
     */
    private void setupPlugin() {
        this.executor = Executors.newScheduledThreadPool(32);

        this.serverName = Settings.SERVER_NAME;
        if (Valid.isNullOrEmpty(this.serverName)) {
            // Vérifie si le nom du serveur est vide ou nul avant d'activer le plugin
            Debugger.saveError(new FoException(), "Impossible d'activer le plugin: Le nom du serveur est vide !");
            super.setEnabled(false);
            Bukkit.getServer().shutdown();
            return;
        }

        this.debugListener = new DebugListener();

        this.sqlServiceManager = this.initServerServiceManager();
        this.databaseConnector = this.initDatabaseconnector();

        this.api = new ApiImplementation(this);

        this.initListeners();
        this.initCommands();
        this.initPlaceholder();
    }

    @Override
    public boolean suggestPaper() {
        // Cette méthode est surchargée pour suggérer Paper pour une meilleure performance, mais renvoie false pour le moment.
        return false;
    }

    /**
     * Obtient l'auteur de ce plugin.
     *
     * @return Le nom de l'auteur.
     */
    public static String getAuthor() {
        return "Maliimaloo";
    }

    /**
     * Obtient l'instance unique de la classe BukkitCore.
     *
     * @return L'instance de BukkitCore.
     */
    public static BukkitCore getInstance() {
        return (BukkitCore) SimplePlugin.getInstance();
    }


    /**
     * Initialise le connecteur de la base de données pour Jedis.
     *
     * @return Le connecteur de la base de données.
     */
    private DatabaseConnector initDatabaseconnector() {
        final String paramBungeeIp = Settings.Jedis.BUNGEE_IP;
        final String paramBungeePassword = Settings.Jedis.BUNGEE_PASSWORD;
        final Integer paramBungeePort = Settings.Jedis.BUNGEE_PORT;
        final RedisServer paramRedisServer = new RedisServer(paramBungeeIp, paramBungeePort, paramBungeePassword);

        return new DatabaseConnector(this, paramRedisServer);
    }

    /**
     * Initialise le gestionnaire de services du serveur.
     *
     * @return Le gestionnaire de services du serveur.
     */
    private SqlServiceManager initServerServiceManager() {
        final String paramDatabaseUrl = Settings.Database.URL;
        final String paramDatabaseUsername = Settings.Database.USERNAME;
        final String paramDatabasePassword = Settings.Database.PASSWORD;

        return new SqlServiceManager(paramDatabaseUrl, paramDatabaseUsername, paramDatabasePassword);
    }

    /**
     * Initialise les écouteurs d'événements.
     */
    private void initListeners() {
        super.registerEvents(new GlobalJoinListener(this));
    }

    private void initCommands() {
        super.registerCommand(new CreditCommand(this.getApi()));
    }

    /**
     * Initialise les placeholders.
     */
    private void initPlaceholder() {
        new PlayerPlaceholderExpansion().register();
        new ServerPlaceholderExpansion(this.getApi()).register();
    }
}
