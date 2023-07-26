package net.omegagames.core.api.listeners.general;

import net.omegagames.core.ApiImplementation;
import net.omegagames.core.PluginCore;
import net.omegagames.core.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mineacademy.fo.Common;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class GlobalJoinListener extends APIListener {
    public GlobalJoinListener(PluginCore plugin, ApiImplementation api) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(AsyncPlayerPreLoginEvent paramEvent) {
        try {
            long startTime = System.currentTimeMillis();
            UUID player = paramEvent.getUniqueId();

            //Chargement des data principal
            this.api.getPlayerManager().loadPlayer(player);
            Common.log("AsyncPrelogin Time: " + (System.currentTimeMillis() - startTime) + "ms.");

            Common.log(this.api.getPlayerManager().getPlayerData(player).getPlayerBean().toStringList());
        } catch (Exception exception) {
            Common.error(exception);
            paramEvent.setKickMessage("Erreur lors du chargement de votre profil.");
            paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent paramEvent) {
        long startTime = System.currentTimeMillis();
        Player paramPlayer = paramEvent.getPlayer();

        paramEvent.setJoinMessage("");
        try {
            PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
            paramPlayerData.getPlayerBean().setName(paramPlayer.getName());
            paramPlayerData.getPlayerBean().setLastLogin(Timestamp.valueOf(LocalDateTime.now().plusHours(2)));
            paramPlayerData.getPlayerBean().setLastIP(paramPlayer.getAddress().getAddress().getHostAddress());

            Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (Exception exception) {
            Common.error(exception);
            paramPlayer.kickPlayer("Erreur lors du chargement de votre profil.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent paramEvent) {
        this.api.getPlayerManager().getPlayerData(paramEvent.getPlayer().getUniqueId()).updateData();
    }
}
