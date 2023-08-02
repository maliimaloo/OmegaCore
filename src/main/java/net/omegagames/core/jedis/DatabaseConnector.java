package net.omegagames.core.jedis;

import net.omegagames.core.bukkit.BukkitCore;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

public class DatabaseConnector {
    private final BukkitCore pluginBukkit;

    private JedisPool cachePool;
    private final RedisServer bungee;

    public DatabaseConnector(BukkitCore pluginBukkit, RedisServer bungee) {
        this.pluginBukkit = pluginBukkit;
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

        this.pluginBukkit.getExecutor().scheduleAtFixedRate(() -> {
            try {
                this.cachePool.getResource().close();
            } catch (FoException exception) {
                Common.throwError(exception, "Error redis connection, Try to reconnect!");
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
            this.cachePool.getResource().configSet("notify-keyspace-events", "Ex");
            this.cachePool.getResource().close();

            Common.logNoPrefix("&7[&9Jedis&7] &aConnection à la database.");
        } catch (Throwable throwable) {
            Common.throwError(throwable, "Impossible de se connecter à la database, désactivation du plugin.");
            Bukkit.shutdown();
        }
    }
}
