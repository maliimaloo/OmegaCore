package net.omegagames.core.bukkit;

// ... (importations)

import net.omegagames.core.bukkit.api.expansion.CreditPlaceholderExpansion;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.api.settings.Settings;
import net.omegagames.core.jedis.DatabaseConnector;
import net.omegagames.core.jedis.RedisServer;
import net.omegagames.core.persistanceapi.ServerServiceManager;
import org.bukkit.Bukkit;
import org.mineacademy.fo.MinecraftVersion;
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
    private ApiImplementation api;
    private ServerServiceManager serverServiceManager;
    private DatabaseConnector databaseConnector;
    private DebugListener debugListener;
    private ScheduledExecutorService executor;
    private String serverName;

    public BukkitCore() {
    }

    // Méthodes pour obtenir différentes composantes

    /**
     * Récupère l'instance de l'implémentation de l'API.
     *
     * @return L'implémentation de l'API.
     */
    public ApiImplementation getAPI() {
        return api;
    }

    /**
     * Récupère l'instance du gestionnaire de services du serveur.
     *
     * @return Le gestionnaire de services du serveur.
     */
    public ServerServiceManager getServerServiceManager() {
        return serverServiceManager;
    }

    /**
     * Récupère l'instance du connecteur de la base de données.
     *
     * @return Le connecteur de la base de données.
     */
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    /**
     * Récupère l'instance de l'écouteur de débogage.
     *
     * @return L'écouteur de débogage.
     */
    public DebugListener getDebugListener() {
        return debugListener;
    }

    /**
     * Récupère l'instance du service d'exécution planifiée.
     *
     * @return Le service d'exécution planifiée.
     */
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Récupère le nom du serveur associé à cette instance de BukkitCore.
     *
     * @return Le nom du serveur.
     */
    public String getServerName() {
        return serverName;
    }

    @Override
    protected void onPluginLoad() {
        // Vérifie si la version de Minecraft est prise en charge avant d'activer le plugin
        if (!MinecraftVersion.atLeast(super.getMinimumVersion())) {
            Debugger.saveError(new FoException(), "Impossible d'activer le plugin: Version de Minecraft non supportée !");
            super.setEnabled(false);
            Bukkit.getServer().shutdown();
        }
    }

    @Override
    protected void onPluginStart() {
        // Configuration du plugin lors de son démarrage
        setupPlugin();
    }

    @Override
    protected void onPluginStop() {
        // Actions de nettoyage lorsque le plugin s'arrête
        this.databaseConnector.killConnection();
        this.serverServiceManager.getDatabaseManager().close();
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

        final String paramUrl = Settings.Database.URL;
        final String paramUsername = Settings.Database.USERNAME;
        final String paramPassword = Settings.Database.PASSWORD;
        this.serverServiceManager = new ServerServiceManager(paramUrl, paramUsername, paramPassword);
        this.databaseConnector = new DatabaseConnector(this, redisServer());

        this.api = new ApiImplementation(this);
        super.registerEvents(new GlobalJoinListener(this));

        new CreditPlaceholderExpansion().register();
    }

    @Override
    public boolean suggestPaper() {
        // Cette méthode est surchargée pour suggérer Paper pour une meilleure performance, mais renvoie false pour le moment.
        return false;
    }

    /**
     * Obtient la version minimale de Minecraft requise par ce plugin.
     *
     * @return La version minimale de Minecraft requise.
     */
    public MinecraftVersion.V getMinimumVersion() {
        return MinecraftVersion.V.v1_17;
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
     * Crée une nouvelle instance de RedisServer en fonction des paramètres configurés dans les paramètres.
     *
     * @return L'instance de RedisServer.
     */
    private RedisServer redisServer() {
        final String paramBungeeIp = Settings.Jedis.BUNGEE_IP;
        final String paramBungeePassword = Settings.Jedis.BUNGEE_PASSWORD;
        final Integer paramBungeePort = Settings.Jedis.BUNGEE_PORT;
        return new RedisServer(paramBungeeIp, paramBungeePort, paramBungeePassword);
    }
}
