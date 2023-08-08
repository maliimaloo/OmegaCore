package net.omegagames.core.bukkit.api.listeners.general;

import net.omegagames.core.bukkit.BukkitCore;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.bukkit.api.scoreboard.ScoreboardData;
import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Écouteur pour les événements globaux de connexion/déconnexion des joueurs.
 */
public class GlobalJoinListener extends APIListener {
    // Map pour stocker les statuts de connexion en cours pour chaque joueur
    private static final Map<UUID, CompletableFuture<Boolean>> onlineStatus = new HashMap<>();

    public GlobalJoinListener(BukkitCore plugin) {
        super(plugin);
    }

    /**
     * Obtient la liste des statuts de connexion en cours pour chaque joueur.
     *
     * @return Une map associant l'UUID du joueur à son statut de connexion en cours.
     */
    public static Map<UUID, CompletableFuture<Boolean>> getOnlineStatus() {
        return onlineStatus;
    }

    // Gestionnaire d'événements pour le pré-connexion d'un joueur
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent paramEvent) {
        try {
            this.handlePlayerPreJoin(paramEvent);
        } catch (Throwable throwable) {
            this.handlePreJoinException(paramEvent, throwable);
        }
    }

    // Gestionnaire d'événements pour la connexion d'un joueur
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent paramEvent) {
        try {
            this.handlePlayerJoin(paramEvent);
        } catch (Throwable throwable) {
            this.handleJoinException(paramEvent.getPlayer(), throwable);
        }
    }

    // Gestionnaire d'événements pour la déconnexion d'un joueur
    @EventHandler
    public void onQuit(PlayerQuitEvent paramEvent) {
        try {
            this.handlePlayerQuit(paramEvent.getPlayer());
        } catch (Throwable throwable) {
            Common.throwError(throwable);
        }
    }

    // Méthode pour gérer l'événement de pré-connexion d'un joueur
    private void handlePlayerPreJoin(AsyncPlayerPreLoginEvent paramEvent) {
        long startTime = System.currentTimeMillis();
        UUID playerUniqueId = paramEvent.getUniqueId();
        PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(playerUniqueId);
        if (!paramPlayerData.isLoaded()) {
            this.handlePlayerPreJoinLoaded(paramEvent, paramPlayerData);
        }

        Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    // Méthode pour gérer le chargement du joueur lors de l'événement de pré-connexion
    private void handlePlayerPreJoinLoaded(AsyncPlayerPreLoginEvent paramEvent, PlayerData paramPlayerData) {
        UUID playerUniqueId = paramEvent.getUniqueId();
        String playerName = paramEvent.getName();
        String playerIp = paramEvent.getAddress().getHostAddress();
        LocalDateTime paramLocalDateTime = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime();

        PlayerBean playerBean = this.api.getServerServiceManager().getPlayer(paramPlayerData.getUniqueId());
        if (playerBean == null) {
            playerBean = new PlayerBean(playerUniqueId, playerName, "", 0, Timestamp.valueOf(paramLocalDateTime), Timestamp.valueOf(paramLocalDateTime), playerIp, 0, new ArrayList<>());
            boolean isCreate = this.api.getServerServiceManager().createPlayer(playerBean);
            if (!isCreate) {
                paramEvent.setKickMessage("Erreur lors de la création de votre profil à partir de la BDD.");
                paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                return;
            }
        }

        boolean isLoadedToJedis = paramPlayerData.loadToJedis(playerBean);
        if (!isLoadedToJedis) {
            paramEvent.setKickMessage("Erreur lors de l'enregistrement de vos données dans Jedis.");
            paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    // Méthode pour gérer les exceptions lors de l'événement de pré-connexion
    private void handlePreJoinException(AsyncPlayerPreLoginEvent paramEvent, Throwable throwable) {
        Common.throwError(throwable, "Erreur lors de la pré-connexion du joueur " + paramEvent.getName() + " (" + paramEvent.getUniqueId() + ").");
        paramEvent.setKickMessage("Erreur lors du chargement de votre profil.");
        paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    }

    // Méthode pour gérer l'événement de connexion d'un joueur
    private void handlePlayerJoin(PlayerJoinEvent paramEvent) {
        long startTime = System.currentTimeMillis();
        Player paramPlayer = paramEvent.getPlayer();

        paramEvent.setJoinMessage("");

        try {
            PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(paramPlayer.getUniqueId());

            String playerName = paramPlayer.getName();
            paramPlayerData.setEffectiveName(playerName);

            Timestamp paramTimestamp = Timestamp.valueOf(ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime());
            paramPlayerData.setLastLogin(paramTimestamp);

            String paramHostAddress = Objects.requireNonNull(paramPlayer.getAddress()).getHostString();
            paramPlayerData.setLastIp(paramHostAddress);

            for (ScoreboardData scoreboard : this.api.getPlugin().getScoreboardManager().getCache()) {
                if (scoreboard.getIsDefault()) {
                    scoreboard.showScoreboard(paramPlayer);
                    break;
                }
            }

            Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (Throwable throwable) {
            this.handleJoinException(paramPlayer, throwable);
        }
    }

    // Méthode pour gérer l'événement de connexion lorsque l'exception est lancée
    private void handleJoinException(Player paramPlayer, Throwable throwable) {
        paramPlayer.kickPlayer("Erreur lors du chargement de votre profil.");
        throw new FoException(throwable, "Erreur lors de la connexion du joueur " + paramPlayer.getName() + " (" + paramPlayer.getUniqueId() + ").");
    }

    // Méthode pour gérer l'événement de déconnexion d'un joueur
    private void handlePlayerQuit(Player paramPlayer) {
        UUID playerUuid = paramPlayer.getUniqueId();
        PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(playerUuid);

        CompletableFuture<Boolean> paramFuture = new CompletableFuture<>();
        GlobalJoinListener.getOnlineStatus().put(playerUuid, paramFuture);

        this.api.getPubSub().send("omegacore:player:online_status_check", playerUuid.toString());
        CompletableFuture.runAsync(() -> {
            try {
                Boolean paramOnlineStatus = paramFuture.get(5, TimeUnit.SECONDS);
                if (!paramOnlineStatus) {
                    Common.log("Le joueur " + paramPlayerData.getEffectiveName() + " (" + playerUuid + ") n'est pas en ligne.");
                    this.handlePlayerQuitOnlineStatusFalse(paramPlayerData);
                }
            } catch (Throwable throwable) {
                Common.log("Le joueur " + paramPlayerData.getEffectiveName() + " (" + playerUuid + ") n'est pas en ligne.");
                this.handlePlayerQuitOnlineStatusException(paramPlayerData);
            }
        }).thenRun(() -> GlobalJoinListener.getOnlineStatus().remove(playerUuid)).exceptionally(throwable -> {
            Common.throwError(throwable);
            return null;
        });
    }

    // Méthode pour gérer l'événement de déconnexion lorsque le statut en ligne est false
    private void handlePlayerQuitOnlineStatusFalse(PlayerData paramPlayerData) {
        this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean(), (response) -> {
            if (response == 0) {
                Common.throwError(new FoException("Erreur lors de la mise à jour du profil de " + paramPlayerData.getEffectiveName() + "."));
                return;
            }

            Common.log("Debug_1: Le profil de " + paramPlayerData.getEffectiveName() + " a été mis à jour avec succès.");
            paramPlayerData.expire();
        });
    }

    // Méthode pour gérer les exceptions lors de l'événement de déconnexion lorsque le statut en ligne est false
    private void handlePlayerQuitOnlineStatusException(PlayerData paramPlayerData) {
        this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean(), (response) -> {
            if (response == 0) {
                Common.throwError(new FoException("Erreur lors de la mise à jour du profil de " + paramPlayerData.getEffectiveName() + "."));
                return;
            }

            Common.log("Debug_2: Le profil de " + paramPlayerData.getEffectiveName() + " a été mis à jour avec succès.");
            paramPlayerData.expire();
        });
    }
}
