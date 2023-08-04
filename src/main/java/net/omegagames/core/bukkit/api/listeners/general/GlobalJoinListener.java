package net.omegagames.core.bukkit.api.listeners.general;

import net.omegagames.core.bukkit.BukkitCore;
import net.omegagames.core.bukkit.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent paramEvent) {
        try {
            long startTime = System.currentTimeMillis();
            UUID playerId = paramEvent.getUniqueId();

            PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(playerId);
            if (!paramPlayerData.isLoaded()) {
                if (!paramPlayerData.create()) {
                    paramEvent.setKickMessage("Erreur lors du chargement de votre profil.");
                    paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                }
            }

            Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (Throwable throwable) {
            Common.throwError(throwable);
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

            final String paramName = paramPlayer.getName();
            paramPlayerData.setEffectiveName(paramName);

            Timestamp paramTimestamp = Timestamp.valueOf(ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime());
            paramPlayerData.setLastLogin(paramTimestamp);

            String paramHostAddress = Objects.requireNonNull(paramPlayer.getAddress()).getHostString();
            paramPlayerData.setLastIp(paramHostAddress);

            this.plugin.getMain_scoreboard().show(paramPlayer);
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

        final CompletableFuture<Boolean> paramFuture = new CompletableFuture<>();
        GlobalJoinListener.getOnlineStatus().put(player, paramFuture);

        this.api.getPubSub().send("omegacore:player:online_status_check", player.toString());
        CompletableFuture.runAsync(() -> {
            try {
                final Boolean paramOnlineStatus = paramFuture.get(5, TimeUnit.SECONDS);
                if (!paramOnlineStatus) {
                    this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean());
                    paramPlayerData.expire();
                }
            } catch (Throwable throwable) {
                this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean());
                paramPlayerData.expire();
            }
        }).thenRun(() -> GlobalJoinListener.getOnlineStatus().remove(player)).exceptionally(throwable -> {
            Common.throwError(throwable);
            return null;
        });
    }
}
