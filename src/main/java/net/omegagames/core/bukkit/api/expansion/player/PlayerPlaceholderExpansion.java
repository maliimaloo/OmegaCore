package net.omegagames.core.bukkit.api.expansion.player;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.model.HookManager;

public class PlayerPlaceholderExpansion {
    /**
     * Enregistrement des placeholders
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

    /* ========================
     * > DisplayName management
     * ======================== */
    private String getPlayerEffectiveName(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getEffectiveName() != null) {
            return paramPlayerData.getEffectiveName();
        }

        return Messenger.getErrorPrefix();
    }

    private String getCustomName(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getCustomName() != null) {
            return paramPlayerData.getCustomName();
        }

        return Messenger.getErrorPrefix();
    }

    private String getDisplayName(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getDisplayName() != null) {
            return paramPlayerData.getDisplayName();
        }

        return Messenger.getErrorPrefix();
    }


    /* ========================
     * > State management
     * ======================== */
    private String getLastConnection(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getLastLogin() != null) {
            return paramPlayerData.getLastLogin().toLocalDateTime().toString();
        }

        return Messenger.getErrorPrefix();
    }

    private String getFirstConnection(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getFirstLogin() != null) {
            return paramPlayerData.getFirstLogin().toLocalDateTime().toString();
        }

        return Messenger.getErrorPrefix();
    }

    private String getLastIp(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded() && paramPlayerData.getLastIp() != null) {
            return paramPlayerData.getLastIp();
        }

        return Messenger.getErrorPrefix();
    }


    /* ========================
     * > Groupe management
     * ======================== */
    private String getGroupPrefix(Player paramPlayer) {
        return Messenger.getErrorPrefix();
    }

    /* ========================
     * > Coins management
     * ======================== */
    private String getOmegaCoins(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded()) {
            return String.valueOf(paramPlayerData.getOmegaCoins());
        }

        return Messenger.getErrorPrefix();
    }
}
