package net.omegagames.core.bukkit.api.expansion.server;

import net.omegagames.core.bukkit.ApiImplementation;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.remain.Remain;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Cette classe représente une extension de placeholders de serveur.
 * Elle permet de fournir des informations sur le serveur en tant que placeholders.
 *
 * @version 1.0
 * @since 2023-07-21
 */
public class ServerPlaceholderExpansion {

    protected ApiImplementation api;

    /**
     * Constructeur de la classe.
     *
     * @param api L'instance de l'API du serveur
     */
    public ServerPlaceholderExpansion(ApiImplementation api) {
        this.api = api;
    }

    /**
     * Enregistre-les placeholders liés au serveur.
     * Associe chaque placeholder à sa méthode correspondante.
     */
    public void register() {
        HookManager.addPlaceholder("server_name", this::getServerName);
        HookManager.addPlaceholder("server_date", this::getCurrentDate);
        HookManager.addPlaceholder("server_player_online", this::getPlayerOnline);
    }

    /**
     * Récupère le nom du serveur.
     *
     * @param player Le joueur pour lequel le placeholder est évalué (non utilisé ici)
     * @return Le nom du serveur ou un message d'erreur si non défini
     */
    private String getServerName(Player player) {
        final String paramServerName = this.api.getServerName();
        if (Valid.isNullOrEmpty(paramServerName)) {
            return Messenger.getErrorPrefix();
        }

        return paramServerName;
    }

    /**
     * Récupère la date actuelle.
     *
     * @param player Le joueur pour lequel le placeholder est évalué (non utilisé ici)
     * @return La date actuelle au format chaîne ou un message d'erreur si non définie
     */
    private String getCurrentDate(Player player) {
        final String paramDateFormat = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDate().toString();
        if (Valid.isNullOrEmpty(paramDateFormat)) {
            return Messenger.getErrorPrefix();
        }

        return paramDateFormat;
    }

    /**
     * Récupère le nombre de joueurs en ligne.
     *
     * @param player Le joueur pour lequel le placeholder est évalué (non utilisé ici)
     * @return Le nombre de joueurs en ligne au format chaîne ou un message d'erreur si non défini
     */
    private String getPlayerOnline(Player player) {
        final String paramPlayerCount = String.valueOf(Remain.getOnlinePlayers().size());
        if (Valid.isNullOrEmpty(paramPlayerCount)) {
            return Messenger.getErrorPrefix();
        }

        return paramPlayerCount;
    }
}
