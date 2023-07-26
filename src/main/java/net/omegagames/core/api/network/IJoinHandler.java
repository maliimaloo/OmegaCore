package net.omegagames.core.api.network;

import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("unused")
public interface IJoinHandler {
    /**
     * Appelé lorsque qu'un utilisateur souhaite se connecter, par exemple en cliquant avec le bouton droit sur un panneau de jeu.
     * (Avant que le joueur ne soit sur le serveur)
     *
     * @param player Joueur demandeur
     * @param response Objet de réponse {@link JoinResponse}
     *
     * @return Réponse remplie
     */
    default JoinResponse requestJoin(UUID player, JoinResponse response) {
        return response;
    }

    /**
     * Appelé lorsqu'une équipe souhaite se connecter, par exemple en cliquant avec le bouton droit sur un panneau de jeu.
     * (Avant que le joueur ne soit sur le serveur)
     *
     * @param party UUID de l'équipe
     * @param player Le joueur qui rejoint
     * @param response Objet de réponse {@link JoinResponse}
     *
     * @return Réponse remplie
     */
    default JoinResponse requestPartyJoin(UUID party, UUID player, JoinResponse response) {
        return response;
    }

    /**
     * Événement déclenché lorsqu'un joueur se connecte.
     *
     * @param player UUID du joueur qui se connecte
     * @param username Nom d'utilisateur du joueur qui se connecte
     */
    default void onLogin(UUID player, String username) {}

    /**
     * Événement déclenché lorsque qu'un joueur a terminé la connexion.
     *
     * @param player Joueur qui s'est connecté
     */
    default void finishJoin(Player player) {}

    /**
     * Événement déclenché lorsqu'un modérateur se connecte.
     * <b>Remplacez {@link IJoinHandler#onLogin(UUID, String)} et {@link IJoinHandler#finishJoin(Player)}</b>
     *
     * @param player Modérateur qui s'est connecté
     */
    default void onModerationJoin(Player player) {}

    /**
     * Événement déclenché lorsqu'un joueur se déconnecte.
     *
     * @param player Joueur qui s'est déconnecté
     */
    default void onLogout(Player player) {}

}
