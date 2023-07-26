package net.omegagames.core.bukkit.api.network;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface IJoinManager {
    /**
     * Enregistre un JoinHandler qui sera appelé par le gestionnaire
     *
     * @param handler Le handler
     * @param priority La priorité du handler (0 = la plus basse, veuillez ne pas utiliser de priorités inférieures à 10)
     */
    void registerHandler(IJoinHandler handler, int priority);

    /**
     * Compte le nombre de joueurs connectés
     *
     * @return Le nombre de joueurs
     */
    int countExpectedPlayers();

    /**
     * Récupère les joueurs connectés sous forme de liste d'UUID
     *
     * @return Liste d'UUID
     */
    List<UUID> getExpectedPlayers();

    /**
     * Récupère les modérateurs connectés sous forme de liste d'UUID
     *
     * @return Liste d'UUID
     */
    List<UUID> getModeratorsExpected();
}