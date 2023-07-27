package net.omegagames.core.bukkit.api.listeners.pubsub;

import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.BukkitCore;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.bukkit.settings.Settings;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class GlobalUpdateListener implements IPacketsReceiver {
    private final ApiImplementation api;

    public GlobalUpdateListener(ApiImplementation api) {
        this.api = api;
    }

    @Override
    public void receive(String channel, String packet) {
        if (channel.equals("online_status_check")) {
            final UUID player = UUID.fromString(SerializeUtil.deserialize(SerializeUtil.Mode.JSON, String.class, packet));
            final String paramKey = PlayerData.getKey() + player;
            final String paramFieldOnline = "online";

            Common.logNoPrefix(Settings.Jedis.PREFIX + " Reception du packet de mise à jour de l'état de connexion de " + player + " !");
            if (Bukkit.getPlayer(player) != null) {
                try (Jedis jedis = this.api.getBungeeResource()) {
                    Common.logNoPrefix(Settings.Jedis.PREFIX + " Mise à jour de l'état de connexion de " + player + " !");
                    jedis.hset(paramKey, paramFieldOnline, Boolean.toString(true));
                } catch (Throwable throwable) {
                    Common.throwError(throwable);
                }
            }
        }
    }
}
