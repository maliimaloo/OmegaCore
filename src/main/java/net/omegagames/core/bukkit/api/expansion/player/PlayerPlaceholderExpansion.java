package net.omegagames.core.bukkit.api.expansion.player;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.model.HookManager;

/**
 * Cette classe représente une extension de placeholders de joueur.
 * Elle permet de gérer et fournir diverses informations sur les joueurs en tant que placeholders.
 *
 * @version 1.0
 * @since 2023-07-21
 */
public class PlayerPlaceholderExpansion {

    /**
     * Enregistre-les placeholders liés aux joueurs.
     * Associe chaque placeholder à sa méthode correspondante.
     */
    public void register() {
        HookManager.addPlaceholder("player_effective_name", this::getPlayerEffectiveName);
        HookManager.addPlaceholder("player_custom_name", this::getCustomName);
        HookManager.addPlaceholder("player_display_name", this::getDisplayName);
        HookManager.addPlaceholder("player_last_connection", this::getLastConnection);
        HookManager.addPlaceholder("player_first_connection", this::getFirstConnection);
        HookManager.addPlaceholder("player_last_ip", this::getLastIp);
        HookManager.addPlaceholder("player_group_prefix", this::getGroupPrefix);
        HookManager.addPlaceholder("player_omega_coins", this::getOmegaCoins);
    }

    /**
     * Récupère le nom effectif du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return Le nom effectif du joueur ou un message d'erreur si non défini
     */
    private String getPlayerEffectiveName(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getEffectiveName() != null) {
            return paramPlayerData.getEffectiveName();
        }

        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère le nom personnalisé du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return Le nom personnalisé du joueur ou un message d'erreur si non défini
     */
    private String getCustomName(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getCustomName() != null) {
            return paramPlayerData.getCustomName();
        }

        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère le nom d'affichage du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return Le nom d'affichage du joueur ou un message d'erreur si non défini
     */
    private String getDisplayName(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getDisplayName() != null) {
            return paramPlayerData.getDisplayName();
        }

        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère la dernière connexion du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return La date de la dernière connexion du joueur ou un message d'erreur si non définie
     */
    private String getLastConnection(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded()) {
            paramPlayerData.getLastLogin();
            return paramPlayerData.getLastLogin().toLocalDateTime().toString();
        }

        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère la première connexion du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return La date de la première connexion du joueur ou un message d'erreur si non définie
     */
    private String getFirstConnection(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded()) {
            paramPlayerData.getFirstLogin();
            return paramPlayerData.getFirstLogin().toLocalDateTime().toString();
        }

        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère la dernière adresse IP du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return La dernière adresse IP du joueur ou un message d'erreur si non définie
     */
    private String getLastIp(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getLastIp() != null) {
            return paramPlayerData.getLastIp();
        }

        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère le préfixe de groupe du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return Le préfixe de groupe du joueur ou un message d'erreur si non défini
     */
    private String getGroupPrefix(Player paramPlayer) {
        return Messenger.getErrorPrefix();
    }

    /**
     * Récupère le nombre de Omega Coins du joueur.
     *
     * @param paramPlayer Le joueur pour lequel le placeholder est évalué
     * @return Le nombre de Omega Coins du joueur ou un message d'erreur si non défini
     */
    private String getOmegaCoins(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded()) {
            return String.valueOf(paramPlayerData.getOmegaCoins());
        }

        return Messenger.getErrorPrefix();
    }
}
