package net.omegagames.core.bungee.api.jedis;

import net.omegagames.core.bukkit.BukkitCore;
import net.omegagames.core.bungee.BungeeCore;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

public class DatabaseConnector {
    private final BungeeCore plugin;
    private JedisPool cachePool;
    private final RedisServer bungee;

    public DatabaseConnector(BungeeCore plugin, RedisServer bungee) {
        this.plugin = plugin;
        this.bungee = bungee;

        this.initiateConnection();
    }

    public Jedis getBungeeResource() {
        return this.cachePool.getResource();
    }

    public void killConnection() {
        this.cachePool.close();
        this.cachePool.destroy();
    }

    private void initiateConnection() {
        this.connect();
        this.plugin.getExecutor().scheduleAtFixedRate(() -> {
            try {
                cachePool.getResource().close();
            } catch (FoException exception) {
                Debugger.saveError(exception, "Error redis connection, Try to reconnect!");
                this.connect();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void connect() {
        JedisPoolConfig paramJedisConfig = new JedisPoolConfig();
        paramJedisConfig.setMaxTotal(-1);
        paramJedisConfig.setJmxEnabled(false);

        try {
            this.cachePool = new JedisPool(paramJedisConfig, this.bungee.getIp(), this.bungee.getPort(), 0, this.bungee.getPassword());
            this.cachePool.getResource().close();

            Common.logNoPrefix("&7[&9Jedis&7] &aConnection à la database.");
        } catch (FoException exception) {
            Debugger.saveError(exception, "Impossible de se connecter à la database.");
            Bukkit.shutdown();
        }
    }
}
