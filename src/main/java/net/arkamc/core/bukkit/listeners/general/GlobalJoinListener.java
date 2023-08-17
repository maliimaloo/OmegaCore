package net.arkamc.core.bukkit.listeners.general;

import lombok.Getter;
import net.arkamc.core.bukkit.BukkitCore;
import net.arkamc.core.bukkit.player.DataPlayer;
import net.arkamc.core.persistanceapi.beans.players.PlayerBean;
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
 * Cette classe gère les événements liés à la connexion et à la déconnexion des joueurs.
 *
 * @version 1.0
 * @since 2023-07-21
 */
public class GlobalJoinListener extends APIListener {

    @Getter
    private static final Map<UUID, CompletableFuture<Boolean>> onlineStatus = new HashMap<>();

    /**
     * Crée une nouvelle instance de l'écouteur avec le plugin BukkitCore.
     *
     * @param plugin Le plugin BukkitCore auquel l'écouteur est associé.
     */
    public GlobalJoinListener(BukkitCore plugin) {
        super(plugin);
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

    /**
     * Gère les étapes pré-connexion du joueur.
     * Cette méthode est appelée lorsqu'un joueur tente de se connecter au serveur.
     * Elle vérifie si les données du joueur sont déjà chargées, sinon elle appelle
     * la méthode `handlePlayerPreJoinLoaded` pour diriger le chargement initial.
     *
     * @param paramEvent L'événement de pré-connexion du joueur.
     */
    private void handlePlayerPreJoin(AsyncPlayerPreLoginEvent paramEvent) {
        long startTime = System.currentTimeMillis();
        UUID playerUniqueId = paramEvent.getUniqueId();
        DataPlayer paramDataPlayer = this.api.getPlayerManager().getPlayerData(playerUniqueId);
        if (!paramDataPlayer.isLoaded()) {
            this.handlePlayerPreJoinLoaded(paramEvent, paramDataPlayer);
        }

        Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    /**
     * Gère le chargement initial des données du joueur lors de la pré-connexion.
     * Cette méthode est appelée lorsque les données du joueur ne sont pas encore chargées.
     * Elle récupère les informations de pré-connexion telles que l'UUID, le nom, l'adresse IP,
     * puis crée un enregistrement de joueur s'il n'existe pas déjà dans la base de données.
     * Ensuite, elle tente de charger les données du joueur dans Jedis pour une utilisation ultérieure.
     *
     * @param paramEvent     L'événement de pré-connexion du joueur.
     * @param paramDataPlayer Les données du joueur.
     */
    private void handlePlayerPreJoinLoaded(AsyncPlayerPreLoginEvent paramEvent, DataPlayer paramDataPlayer) {
        UUID playerUniqueId = paramEvent.getUniqueId();
        String playerName = paramEvent.getName();
        String playerIp = paramEvent.getAddress().getHostAddress();
        LocalDateTime paramLocalDateTime = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime();

        PlayerBean playerBean = this.api.getSQLServiceManager().getPlayer(paramDataPlayer.getUniqueId());
        if (playerBean == null) {
            playerBean = new PlayerBean(playerUniqueId, playerName, "", 0, Timestamp.valueOf(paramLocalDateTime), Timestamp.valueOf(paramLocalDateTime), playerIp, 0, new ArrayList<>());
            boolean isCreate = this.api.getSQLServiceManager().createPlayer(playerBean);
            if (!isCreate) {
                paramEvent.setKickMessage("Erreur lors de la création de votre profil à partir de la BDD.");
                paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                return;
            }
        }

        boolean isLoadedToJedis = paramDataPlayer.loadToJedis(playerBean);
        if (!isLoadedToJedis) {
            paramEvent.setKickMessage("Erreur lors de l'enregistrement de vos données dans Jedis.");
            paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    /**
     * Gère les exceptions survenues lors de la pré-connexion du joueur.
     * Cette méthode est appelée en cas d'erreur lors du traitement de la pré-connexion.
     * Elle génère un message d'erreur pour le joueur et déclenche une exception
     * pour informer les administrateurs ou les développeurs du problème.
     *
     * @param paramEvent  L'événement de pré-connexion du joueur.
     * @param throwable L'exception survenue.
     */
    private void handlePreJoinException(AsyncPlayerPreLoginEvent paramEvent, Throwable throwable) {
        Common.throwError(throwable, "Erreur lors de la pré-connexion du joueur " + paramEvent.getName() + " (" + paramEvent.getUniqueId() + ").");
        paramEvent.setKickMessage("Erreur lors du chargement de votre profil.");
        paramEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    }

    /**
     * Gère l'événement de connexion d'un joueur.
     * Cette méthode est appelée lorsqu'un joueur se connecte au serveur.
     * Elle met à jour les informations du joueur, telles que son nom effectif,
     * son heure de dernière connexion et son adresse IP.
     *
     * @param paramEvent L'événement de connexion du joueur.
     */
    private void handlePlayerJoin(PlayerJoinEvent paramEvent) {
        long startTime = System.currentTimeMillis();
        Player paramPlayer = paramEvent.getPlayer();

        paramEvent.setJoinMessage("");

        try {
            DataPlayer paramDataPlayer = this.api.getPlayerManager().getPlayerData(paramPlayer.getUniqueId());

            String playerName = paramPlayer.getName();
            paramDataPlayer.setEffectiveName(playerName);

            Timestamp paramTimestamp = Timestamp.valueOf(ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime());
            paramDataPlayer.setLastLogin(paramTimestamp);

            String paramHostAddress = Objects.requireNonNull(paramPlayer.getAddress()).getHostString();
            paramDataPlayer.setLastIp(paramHostAddress);

            Common.log("Join Time: " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (Throwable throwable) {
            this.handleJoinException(paramPlayer, throwable);
        }
    }


    /**
     * Gère les exceptions survenues lors de la connexion d'un joueur.
     * Cette méthode est appelée en cas d'erreur lors du traitement de la connexion.
     * Elle génère un message d'erreur pour le joueur, le déconnecte et déclenche
     * une exception pour informer les administrateurs ou les développeurs du problème.
     *
     * @param paramPlayer Le joueur qui a tenté de se connecter.
     * @param throwable   L'exception survenue.
     */
    private void handleJoinException(Player paramPlayer, Throwable throwable) {
        paramPlayer.kickPlayer("Erreur lors du chargement de votre profil.");
        throw new FoException(throwable, "Erreur lors de la connexion du joueur " + paramPlayer.getName() + " (" + paramPlayer.getUniqueId() + ").");
    }

    /**
     * Gère l'événement de déconnexion d'un joueur.
     * Cette méthode est appelée lorsqu'un joueur se déconnecte du serveur.
     * Elle vérifie si le joueur est encore en ligne en interrogeant Jedis
     * pour obtenir son statut. Si le joueur n'est plus en ligne, elle
     * met à jour les données du joueur et les enregistre dans la base de données.
     *
     * @param paramPlayer Le joueur qui s'est déconnecté.
     */
    private void handlePlayerQuit(Player paramPlayer) {
        UUID playerUuid = paramPlayer.getUniqueId();
        DataPlayer paramDataPlayer = this.api.getPlayerManager().getPlayerData(playerUuid);

        CompletableFuture<Boolean> paramFuture = new CompletableFuture<>();
        GlobalJoinListener.getOnlineStatus().put(playerUuid, paramFuture);

        this.api.getPubSub().send("omegacore:player:online_status_check", playerUuid.toString());
        CompletableFuture.runAsync(() -> {
            try {
                Boolean paramOnlineStatus = paramFuture.get(5, TimeUnit.SECONDS);
                if (!paramOnlineStatus) {
                    Common.log("Le joueur " + paramDataPlayer.getEffectiveName() + " (" + playerUuid + ") n'est pas en ligne.");
                    this.handlePlayerQuitOnlineStatusFalse(paramDataPlayer);
                }
            } catch (Throwable throwable) {
                Common.log("Le joueur " + paramDataPlayer.getEffectiveName() + " (" + playerUuid + ") n'est pas en ligne.");
                this.handlePlayerQuitOnlineStatusException(paramDataPlayer);
            }
        }).thenRun(() -> GlobalJoinListener.getOnlineStatus().remove(playerUuid)).exceptionally(throwable -> {
            Common.throwError(throwable);
            return null;
        });
    }

    /**
     * Gère la mise à jour du profil du joueur lorsqu'il se déconnecte et est considéré hors ligne.
     * Cette méthode est appelée lorsque le statut de connexion en ligne du joueur est faux,
     * indiquant qu'il s'est déconnecté. Elle met à jour les informations du joueur dans la base
     * de données et marque les données du joueur comme expirées.
     *
     * @param paramDataPlayer Les données du joueur qui s'est déconnecté.
     */
    private void handlePlayerQuitOnlineStatusFalse(DataPlayer paramDataPlayer) {
        this.api.getSQLServiceManager().updatePlayer(paramDataPlayer.getPlayerBean(), (response) -> {
            if (response == 0) {
                Common.throwError(new FoException("Erreur lors de la mise à jour du profil de " + paramDataPlayer.getEffectiveName() + "."));
                return;
            }

            Common.log("Debug_1: Le profil de " + paramDataPlayer.getEffectiveName() + " a été mis à jour avec succès.");
            paramDataPlayer.expire();
        });
    }

    /**
     * Gère les exceptions survenues lors de la mise à jour du profil du joueur
     * lorsqu'il se déconnecte et est considéré hors ligne.
     * Cette méthode est appelée en cas d'erreur lors de la mise à jour des informations du joueur.
     * Elle génère un message d'erreur, mais tente quand même de marquer les données du joueur comme expirées.
     *
     * @param paramDataPlayer Les données du joueur qui s'est déconnecté.
     */
    private void handlePlayerQuitOnlineStatusException(DataPlayer paramDataPlayer) {
        this.api.getSQLServiceManager().updatePlayer(paramDataPlayer.getPlayerBean(), (response) -> {
            if (response == 0) {
                Common.throwError(new FoException("Erreur lors de la mise à jour du profil de " + paramDataPlayer.getEffectiveName() + "."));
                return;
            }

            Common.log("Debug_2: Le profil de " + paramDataPlayer.getEffectiveName() + " a été mis à jour avec succès.");
            paramDataPlayer.expire();
        });
    }
}
