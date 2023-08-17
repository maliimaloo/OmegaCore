package net.arkamc.core.bukkit;

import lombok.Getter;
import net.arkamc.api.ArkaAPI;
import net.arkamc.core.bukkit.player.DataPlayerManager;
import net.arkamc.core.bukkit.pubsub.PubSubAPI;
import net.arkamc.core.persistanceapi.SqlServiceManager;
import redis.clients.jedis.Jedis;

public class ApiImplementation extends ArkaAPI {
    @Getter
    private static ApiImplementation instance;

    private final BukkitCore plugin;

    /**
     * Constructeur de la classe ApiImplementation.
     *
     * @param plugin L'instance principale du plugin BukkitCore.
     */
    public ApiImplementation(BukkitCore plugin) {
        super(plugin);
        instance = this;
        this.plugin = plugin;
    }

    /* -------------------------------------------
     * Méthodes surchargées de l'API OmegaGames
     * ------------------------------------------- */

    /**
     * Récupère le nom du serveur associé à cette instance de BukkitCore.
     *
     * @return Le nom du serveur.
     */
    @Override
    public String getServerName() {
        return this.plugin.getServerName();
    }

    /**
     * Récupère la ressource Jedis pour la communication avec BungeeCord.
     *
     * @return L'instance Jedis pour la communication avec BungeeCord.
     */
    @Override
    public Jedis getBungeeResource() {
        return this.plugin.getDatabaseConnector().getBungeeResource();
    }

    /**
     * Récupère le gestionnaire de données des joueurs.
     *
     * @return Le gestionnaire de données des joueurs.
     */
    @Override
    public DataPlayerManager getPlayerManager() {
        return this.plugin.getDataPlayerManager();
    }

    /**
     * Récupère l'API de messagerie publique-privée pour la communication entre serveurs.
     *
     * @return L'API de messagerie publique-privée.
     */
    @Override
    public PubSubAPI getPubSub() {
        return this.plugin.getPubSubAPI();
    }

    /* -------------------------------------------
     * Méthodes spécifiques à OmegaGames
     * ------------------------------------------- */

    /**
     * Récupère le gestionnaire de services du serveur.
     *
     * @return Le gestionnaire de services du serveur.
     */
    public SqlServiceManager getSQLServiceManager() {
        return this.plugin.getSqlServiceManager();
    }

    /**
     * Récupère l'instance principale du plugin BukkitCore.
     *
     * @return L'instance principale du plugin BukkitCore.
     */
    @Override
    public BukkitCore getPlugin() {
        return this.plugin;
    }
}
