package net.omegagames.core.bukkit;

import net.omegagames.api.OmegaGamesAPI;
import net.omegagames.core.bukkit.api.player.PlayerDataManager;
import net.omegagames.core.bukkit.api.pubsub.PubSubAPI;
import net.omegagames.core.persistanceapi.ServerServiceManager;
import redis.clients.jedis.Jedis;

@SuppressWarnings("unused")
public class ApiImplementation extends OmegaGamesAPI {
    private final BukkitCore plugin;
    private final PlayerDataManager playerDataManager;
    private final PubSubAPI pubSub;

    public ApiImplementation(BukkitCore plugin) {
        super(plugin);
        this.plugin = plugin;

        this.pubSub = new PubSubAPI(this);
        this.playerDataManager = new PlayerDataManager(this);
    }

    @Override
    public String getServerName() {
        return this.plugin.getServerName();
    }

    @Override
    public Jedis getBungeeResource() {
        return this.plugin.getDatabaseConnector().getBungeeResource();
    }

    @Override
    public PlayerDataManager getPlayerManager() {
        return this.playerDataManager;
    }

    @Override
    public PubSubAPI getPubSub() {
        return this.pubSub;
    }

    public ServerServiceManager getServerServiceManager() {
        return this.plugin.getServerServiceManager();
    }

    @Override
    public BukkitCore getPlugin() {
        return this.plugin;
    }
}
