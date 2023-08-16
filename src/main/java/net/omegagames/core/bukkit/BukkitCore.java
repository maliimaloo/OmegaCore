package net.omegagames.core.bukkit;

import lombok.Getter;
import net.omegagames.core.bukkit.api.commands.CreditCommand;
import net.omegagames.core.bukkit.api.expansion.player.PlayerPlaceholderExpansion;
import net.omegagames.core.bukkit.api.expansion.server.ServerPlaceholderExpansion;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.api.listeners.pubsub.GlobalUpdateListener;
import net.omegagames.core.bukkit.api.player.PlayerDataManager;
import net.omegagames.core.bukkit.api.pubsub.PubSubAPI;
import net.omegagames.core.bukkit.api.settings.Settings;
import net.omegagames.core.jedis.DatabaseConnector;
import net.omegagames.core.jedis.RedisServer;
import net.omegagames.core.persistanceapi.SqlServiceManager;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
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
    private PlayerDataManager playerDataManager;
    @Getter
    private PubSubAPI pubSubAPI;
    @Getter
    private DebugListener debugListener;
    @Getter
    private ScheduledExecutorService executor;
    @Getter
    private String serverName;

    /* -------------------------------------------------------
     * Méthodes d'allumage et d'extinction du plugin.
     * ------------------------------------------------------- */

    @Override
    protected void onPluginStart() {
        setupPlugin();
    }

    @Override
    protected void onPluginStop() {
        cleanupPlugin();
    }

    /**
     * Configuration du plugin en initialisant différentes composantes et services.
     */
    private void setupPlugin() {
        initializeFields();

        if (Valid.isNullOrEmpty(this.serverName)) {
            handleInvalidServerName();
            return;
        }

        configureLogging();
    }

    /**
     * Nettoie les composantes du plugin lorsque celui-ci se ferme.
     */
    private void cleanupPlugin() {
        if (this.databaseConnector != null) {
            this.databaseConnector.killConnection();
        }

        if (this.sqlServiceManager != null && this.sqlServiceManager.getDatabaseManager() != null) {
            this.sqlServiceManager.getDatabaseManager().close();
        }
    }

    /**
     * Initialise les champs et les composantes nécessaires.
     */
    private void initializeFields() {
        this.executor = Executors.newScheduledThreadPool(32);
        this.serverName = Settings.SERVER_NAME;
        this.debugListener = new DebugListener();
        this.sqlServiceManager = configureServerServiceManager();
        this.databaseConnector = configureDatabaseConnector();

        this.api = new ApiImplementation(this);
        this.playerDataManager = new PlayerDataManager(this.api);

        configurePubSub();
        initListeners();
        initCommands();
        initPlaceholder();
    }

    /**
     * Traite le cas où le nom du serveur est vide.
     */
    private void handleInvalidServerName() {
        Debugger.saveError(new FoException(), "Impossible d'activer le plugin: Le nom du serveur est vide !");
        super.setEnabled(false);
        Bukkit.getServer().shutdown();
    }

    /**
     * Configure les préfixes pour les logs et les messages.
     */
    private void configureLogging() {
        Common.setTellPrefix(Settings.PLUGIN_PREFIX);
        Common.setLogPrefix(Settings.LOG_PREFIX);
    }

    /**
     * Initialise le gestionnaire de services du serveur.
     *
     * @return Le gestionnaire de services du serveur.
     */
    private SqlServiceManager configureServerServiceManager() {
        final String paramDatabaseUrl = Settings.Database.URL;
        final String paramDatabaseUsername = Settings.Database.USERNAME;
        final String paramDatabasePassword = Settings.Database.PASSWORD;

        return new SqlServiceManager(paramDatabaseUrl, paramDatabaseUsername, paramDatabasePassword);
    }

    /**
     * Initialise le connecteur de la base de données pour Jedis.
     *
     * @return Le connecteur de la base de données.
     */
    private DatabaseConnector configureDatabaseConnector() {
        final String paramBungeeIp = Settings.Jedis.BUNGEE_IP;
        final String paramBungeePassword = Settings.Jedis.BUNGEE_PASSWORD;
        final Integer paramBungeePort = Settings.Jedis.BUNGEE_PORT;
        final RedisServer paramRedisServer = new RedisServer(paramBungeeIp, paramBungeePort, paramBungeePassword);

        return new DatabaseConnector(this, paramRedisServer);
    }

    /**
     * Initialise l'API de publication/abonnement (PubSub).
     */
    private void configurePubSub() {
        this.pubSubAPI = new PubSubAPI(this.api);

        GlobalUpdateListener listener = new GlobalUpdateListener(this.api);
        this.pubSubAPI.subscribe("omegacore:player:online_status_check", listener);
        this.pubSubAPI.subscribe("omegacore:player:online_status_response", listener);
    }

    /**
     * Initialise les écouteurs (listeners).
     */
    private void initListeners() {
        super.registerEvents(new GlobalJoinListener(this));
    }

    /**
     * Initialise les commandes.
     */
    private void initCommands() {
        super.registerCommand(new CreditCommand(this.getApi()));
    }

    /**
     * Initialise les placeholders.
     */
    private void initPlaceholder() {
        new PlayerPlaceholderExpansion().register();
        new ServerPlaceholderExpansion(this.api).register();
    }

    @Override
    public boolean suggestPaper() {
        return false; // Pour le moment, ne suggère pas Paper pour une meilleure performance.
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
}