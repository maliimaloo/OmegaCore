package net.omegagames.core.bukkit.api.listeners.general;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.BukkitCore;
import net.omegagames.core.bukkit.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.exception.FoException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalJoinListener extends APIListener {
    private static final Map<UUID, Boolean> onlineStatus = new HashMap<>();

    public GlobalJoinListener(BukkitCore plugin) {
        super(plugin);
    }

    public static Map<UUID, Boolean> getOnlineStatus() {
        return onlineStatus;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(AsyncPlayerPreLoginEvent paramEvent) {
        try {
            long startTime = System.currentTimeMillis();
            UUID player = paramEvent.getUniqueId();

            //Chargement des data principal
            this.api.getPlayerManager().loadPlayer(player);
            Common.log("AsyncPrelogin Time: " + (System.currentTimeMillis() - startTime) + "ms.");
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

            paramPlayerData.refreshIfNeeded();
            paramPlayerData.getPlayerBean().setName(paramPlayer.getName());
            paramPlayerData.getPlayerBean().setLastLogin(Timestamp.valueOf(LocalDateTime.now().plusHours(2)));
            paramPlayerData.getPlayerBean().setLastIP(paramPlayer.getAddress().getAddress().getHostAddress());
            paramPlayerData.updateData();

            Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (Throwable throwable) {
            paramPlayer.kickPlayer("Erreur lors du chargement de votre profil.");
            throw new FoException(throwable, "Erreur lors du chargement du profil de " + paramPlayer.getName() + ".");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent paramEvent) {
        UUID player = paramEvent.getPlayer().getUniqueId();
        PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(player);
        onlineStatus.put(player, false);

        Common.runLaterAsync(60, () -> {
            this.api.getPubSub().send("online_status_check", player.toString());

            if (!onlineStatus.get(player)) {
                Common.log("Le joueur " + player + " n'est pas connecté sur le réseau.");
                onlineStatus.remove(player);
                this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean());
            }
        });
    }
}
