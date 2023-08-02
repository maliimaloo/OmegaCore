package net.omegagames.core.bukkit;

import lombok.Getter;
import net.omegagames.api.OmegaGamesAPI;
import net.omegagames.core.bukkit.api.listeners.pubsub.GlobalUpdateListener;
import net.omegagames.core.bukkit.api.player.PlayerDataManager;
import net.omegagames.core.bukkit.api.pubsub.PubSubAPI;
import net.omegagames.core.persistanceapi.ServerServiceManager;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ApiImplementation extends OmegaGamesAPI {
    @Getter
    private static ApiImplementation instance;

    private final BukkitCore plugin;
    private final PlayerDataManager playerDataManager;
    private final PubSubAPI pubSub;

    public ApiImplementation(BukkitCore plugin) {
        super(plugin);
        instance = this;
        this.plugin = plugin;

        this.pubSub = new PubSubAPI(this);
        GlobalUpdateListener listener = new GlobalUpdateListener(this);
        this.pubSub.subscribe("omegacore:player:online_status_check", listener);
        this.pubSub.subscribe("omegacore:player:online_status_response", listener);
        this.pubSub.subscribe("__keyevent@0__:expired", listener);

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
