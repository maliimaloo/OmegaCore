package net.arkamc.arkacore.jedis;

import net.arkamc.arkacore.bukkit.BukkitCore;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

public class DatabaseConnector {
    private final BukkitCore pluginBukkit;

    private JedisPool cachePool;
    private final RedisServer bungee;

    private int reconnectAttempts = 0;
    private final int MAX_RECONNECT_ATTEMPTS = 3;

    public DatabaseConnector(BukkitCore pluginBukkit, RedisServer bungee) {
        this.pluginBukkit = pluginBukkit;
        this.bungee = bungee;

        this.connect();
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
                // Test the connection by trying to get a resource from the pool
                try (Jedis jedis = this.cachePool.getResource()) {
                    // Reset reconnectAttempts on successful connection
                    this.reconnectAttempts = 0;
                }

            } catch (Exception exception) {
                if (this.reconnectAttempts < this.MAX_RECONNECT_ATTEMPTS) {
                    this.reconnectAttempts++;
                    this.connect();
                } else {
                    Common.log("&cReached maximum reconnection attempts. Disabling the plugin.");
                    Bukkit.shutdown();
                }
            }

        }, 0, 10, TimeUnit.SECONDS);
    }

    private void connect() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(-1);
        jedisPoolConfig.setJmxEnabled(false);

        try {
            this.cachePool = new JedisPool(jedisPoolConfig, this.bungee.getIp(), this.bungee.getPort(), 0, this.bungee.getPassword());
            this.cachePool.getResource().configSet("notify-keyspace-events", "Ex");
            this.cachePool.getResource().close();

            Common.logNoPrefix("&7[&9Jedis&7] &aConnection à la database.");
        } catch (Throwable throwable) {
            Common.throwError(throwable, "Error redis connection, trying to reconnect!");
        }
    }
}
