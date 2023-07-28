package net.omegagames.core.bukkit.api.listeners.general;

import net.omegagames.api.pubsub.PendingMessage;
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
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GlobalJoinListener extends APIListener {
    private static final Map<UUID, CompletableFuture<Boolean>> onlineStatus = new HashMap<>();

    public GlobalJoinListener(BukkitCore plugin) {
        super(plugin);
    }

    public static Map<UUID, CompletableFuture<Boolean>> getOnlineStatus() {
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
        final UUID player = paramEvent.getPlayer().getUniqueId();
        final PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(player);
        if (paramPlayerData == null) {
            Common.log("Le joueur " + player + " n'a pas de PlayerData.");
            return;
        }

        paramPlayerData.updateData();

        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        GlobalJoinListener.getOnlineStatus().put(player, future);

        this.api.getPubSub().send(new PendingMessage("omegacore:player:online_status_check", player.toString()));
        CompletableFuture.runAsync(() -> {
            try {
                boolean paramOnlineStatus = future.get(5, TimeUnit.SECONDS);
                if (!paramOnlineStatus) {
                    Common.log("Le joueur " + player + " n'est pas connecté sur le réseau.");
                    this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean());
                }
            } catch (Throwable throwable) {
                Common.throwError(throwable);
            }
        }).thenRun(() -> GlobalJoinListener.getOnlineStatus().remove(player)).exceptionally(throwable -> {
            Common.throwError(throwable);
            return null;
        });
    }
}
