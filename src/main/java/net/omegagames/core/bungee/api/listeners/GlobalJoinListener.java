package net.omegagames.core.bungee.api.listeners;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.bungee.BungeeCore;
import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class GlobalJoinListener implements Listener {
    private final BungeeCore plugin;

    public GlobalJoinListener(BungeeCore bungeeCore) {
        this.plugin = bungeeCore;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent paramPlayerDisconnectEvent) {
        UUID player = paramPlayerDisconnectEvent.getPlayer().getUniqueId();
        try (Jedis jedis = this.plugin.getDatabaseConnector().getBungeeResource()) {
            if (jedis.exists(PlayerData.getKey() + player)) {
                PlayerBean playerBean = SerializeUtil.deserialize(SerializeUtil.Mode.JSON, PlayerBean.class, jedis.hget(PlayerData.getKey() + player, "data"));
                if (playerBean != null) {
                    Common.log("Saving player data for " + playerBean.getName() + " to database.");
                    this.plugin.getServerServiceManager().updatePlayer(playerBean);
                }
            }
        }
    }
}
