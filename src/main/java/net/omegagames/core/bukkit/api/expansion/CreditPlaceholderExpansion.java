package net.omegagames.core.bukkit.api.expansion;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.model.HookManager;

public class CreditPlaceholderExpansion {
    /**
     * Enregistre les placeholders
     */
    public void register() {
        HookManager.addPlaceholder("player_omega", this::getPlayerOmega);
    }

    /**
     * Récupère le nombre de crédits d'un joueur
     *
     * @param paramPlayer Le joueur bukkit
     * @return Le nombre de crédits du joueur.
     */
    private String getPlayerOmega(Player paramPlayer) {
        PlayerData paramPlayerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramPlayer.getUniqueId());
        if (paramPlayerData.isLoaded()) {
            return String.valueOf(paramPlayerData.getOmegaCoins());
        }

        return Messenger.getErrorPrefix();
    }
}
