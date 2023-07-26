package net.omegagames.core.bukkit.api.network;

import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("unused")
public enum ResponseType {
    ALLOW(),
    DENY_OTHER(),
    DENY_CANT_RECEIVE(ChatColor.RED + "La partie ne peut pas vous recevoir."),
    DENY_FULL(ChatColor.RED + "La partie est pleine."),
    DENY_VIPONLY(ChatColor.RED + "La partie est pleine. Devenez " + ChatColor.GREEN + "VIP" + ChatColor.RED + " pour rejoindre."),
    DENY_NOT_READY(ChatColor.RED + "Cette arène n'est pas prête. Merci de patienter."),
    DENY_IN_GAME(ChatColor.RED + "La partie est déjà en cours.");

    private String message = null;

    /**
     * Constructeur vide
     */
    ResponseType() {}

    /**
     * Constructeur
     *
     * @param message Raison de refus d'une demande de connexion
     */
    ResponseType(String message) {
        this.message = message;
    }

    /**
     * Obtient la raison du refus d'une demande de connexion
     *
     * @return Raison
     */
    public String getMessage() {
        return message;
    }
}
