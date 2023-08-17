package net.arkamc.core.bukkit.player;

import lombok.AllArgsConstructor;
import net.arkamc.api.player.AbstractPlayerData;
import net.arkamc.api.player.IFinancialCallback;
import net.arkamc.core.bukkit.ApiImplementation;
import net.arkamc.core.persistanceapi.beans.credit.CreditBean;
import net.arkamc.core.persistanceapi.beans.players.PlayerBean;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.Replacer;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente les données d'un joueur dans le plugin OmegaCore.
 */
@AllArgsConstructor
public final class DataPlayer extends AbstractPlayerData {

    private final ApiImplementation api;
    private final UUID uniqueId;

    private String jedisAccountKey = "omegacore:account:{unique_id}";
    private String jedisLogsKey = "omegacore:credit_logs:{unique_id}";

    /**
     * Initialise un nouvel objet PlayerData.
     *
     * @param api      L'implémentation de l'API.
     * @param uniqueId L'UUID du joueur.
     */
    public DataPlayer(ApiImplementation api, UUID uniqueId) {
        this.api = api;
        this.uniqueId = uniqueId;

        this.jedisAccountKey = Replacer.replaceVariables(this.jedisAccountKey, SerializedMap.ofArray("{unique_id}", uniqueId));
        this.jedisLogsKey = Replacer.replaceVariables(this.jedisLogsKey, SerializedMap.ofArray("{unique_id}", uniqueId));
    }

    /**
     * Récupère l'UUID unique du joueur.
     *
     * @return L'UUID du joueur.
     */
    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    /**
     * Vérifie si les données du joueur sont chargées en mémoire.
     *
     * @return Vrai si les données sont chargées, sinon faux.
     */
    public boolean isLoaded() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            return jedis.exists(this.jedisAccountKey);
        }
    }


    /* ========================
     * > DisplayName management
     * ======================== */

    /**
     * Récupère le nom effectif du joueur.
     *
     * @return Le nom effectif du joueur.
     */
    @Override
    public String getEffectiveName() {
        return this.getHashValue("name");
    }

    /**
     * Récupère le nom personnalisé du joueur.
     *
     * @return Le nom personnalisé du joueur.
     */
    @Override
    public String getCustomName() {
        return this.getHashValue("customName");
    }

    /**
     * Récupère le nom d'affichage du joueur, qui est soit le nom personnalisé s'il existe,
     * sinon le nom effectif.
     *
     * @return Le nom d'affichage du joueur.
     */
    @Override
    public String getDisplayName() {
        return this.getCustomName() != null ? this.getCustomName() : this.getEffectiveName();
    }

    /**
     * Définit le nom effectif du joueur.
     *
     * @param name Le nom effectif à définir.
     */
    @Override
    public void setEffectiveName(String name) {
        this.setHashValue("name", name);
    }

    /**
     * Définit le nom personnalisé du joueur.
     *
     * @param name Le nom personnalisé à définir.
     */
    @Override
    public void setCustomName(String name) {
        this.setHashValue("customName", name);
    }

    /**
     * Vérifie si le joueur a un surnom (nom personnalisé) défini.
     *
     * @return true si le joueur a un surnom, sinon false.
     */
    @Override
    public boolean hasNickname() {
        return this.getCustomName() != null;
    }


    /* ========================
     * > State management
     * ======================== */

    /**
     * Récupère la date et l'heure de la dernière connexion du joueur.
     *
     * @return La date et l'heure de la dernière connexion du joueur.
     */
    @Override
    public Timestamp getLastLogin() {
        return Timestamp.valueOf(this.getHashValue("lastLogin"));
    }

    /**
     * Récupère la date et l'heure de la première connexion du joueur.
     *
     * @return La date et l'heure de la première connexion du joueur.
     */
    @Override
    public Timestamp getFirstLogin() {
        return Timestamp.valueOf(this.getHashValue("firstLogin"));
    }

    /**
     * Récupère l'adresse IP de la dernière connexion du joueur.
     *
     * @return L'adresse IP de la dernière connexion du joueur.
     */
    @Override
    public String getLastIp() {
        return this.getHashValue("lastIp");
    }

    /**
     * Définit la date et l'heure de la dernière connexion du joueur.
     *
     * @param lastLogin La date et l'heure de la dernière connexion à définir.
     */
    @Override
    public void setLastLogin(Timestamp lastLogin) {
        this.setHashValue("lastLogin", lastLogin.toString());
    }

    /**
     * Définit la date et l'heure de la première connexion du joueur.
     *
     * @param firstLogin La date et l'heure de la première connexion à définir.
     */
    @Override
    public void setFirstLogin(Timestamp firstLogin) {
        this.setHashValue("firstLogin", firstLogin.toString());
    }

    /**
     * Définit l'adresse IP de la dernière connexion du joueur.
     *
     * @param lastIp L'adresse IP de la dernière connexion à définir.
     */
    @Override
    public void setLastIp(String lastIp) {
        this.setHashValue("lastIp", lastIp);
    }


    /* ========================
     * > Groupe management
     * ======================== */

    /**
     * Récupère l'ID du groupe auquel le joueur appartient.
     *
     * @return L'ID du groupe du joueur.
     */
    @Override
    public Long getGroupId() {
        return Long.parseLong(this.getHashValue("groupId"));
    }

    /**
     * Définit l'ID du groupe auquel le joueur appartient.
     *
     * @param groupId L'ID du groupe à définir.
     */
    @Override
    public void setGroupId(Long groupId) {
        this.setHashValue("groupId", groupId.toString());
    }


    /* ========================
     * > Logs management
     * ======================== */

    /**
     * Récupère la liste des journaux de crédit du joueur.
     *
     * @return Une liste contenant les journaux de crédit du joueur.
     */
    public List<CreditBean> getLogs() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            List<CreditBean> logs = new ArrayList<>();

            // Parcours de la liste des journaux dans Redis et création des objets CreditBean correspondants
            for (String log : jedis.lrange(this.jedisLogsKey, 0, -1)) {
                logs.add(CreditBean.fromString(this.getUniqueId(), log));
            }

            return logs;
        }
    }

    /**
     * Ajoute un nouveau journal de crédit à la liste.
     *
     * @param log Le journal de crédit à ajouter.
     */
    public void addLog(CreditBean log) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            // Ajout du journal de crédit à la liste dans Redis
            jedis.rpush(this.jedisLogsKey, log.toString());
        }
    }

    /**
     * Réinitialiser les logs de crédit.
     */
    public void resetLogs() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.ltrim(this.jedisLogsKey, jedis.llen(this.jedisLogsKey), -1);
        }
    }


    /* ========================
     * > Coins management
     * ======================== */

    /**
     * Récupère le nombre d'Omega Coins du joueur.
     *
     * @return Le nombre d'Omega Coins du joueur.
     */
    @Override
    public long getCredit() {
        return Integer.parseInt(this.getHashValue("omega"));
    }

    /**
     * Crédite le compte du joueur avec une certaine quantité d'Omega Coins.
     *
     * @param amount            Le montant a crédité
     * @param financialCallback Le rappel à appeler une fois l'opération effectuée.
     */
    @Override
    public void creditCredit(long amount, IFinancialCallback financialCallback) {
        this.creditEconomy(amount, financialCallback);
    }

    /**
     * Retire une certaine quantité d'Omega Coins du compte du joueur.
     *
     * @param amount            Le montant a retiré
     * @param financialCallback Le rappel à appeler une fois l'opération effectuée.
     */
    @Override
    public void withdrawCredit(long amount, IFinancialCallback financialCallback) {
        this.creditEconomy(-amount, financialCallback);
    }

    /**
     * Vérifie si le joueur possède suffisamment d'Omega Coins.
     *
     * @param amount Le montant a vérifié
     * @return Vrai si le joueur a suffisamment d'Omega Coins, sinon faux.
     */
    @Override
    public boolean hasEnoughCredit(long amount) {
        return this.getCredit() >= amount;
    }

    /**
     * Effectue une opération de crédit/débit sur les Omega Coins du joueur de manière asynchrone.
     *
     * @param amount            Le montant à créditer/débiter.
     * @param financialCallback Le rappel à appeler une fois l'opération effectuée.
     */
    private void creditEconomy(long amount, IFinancialCallback financialCallback) {
        Common.runAsync(() ->  {
            long newAmount = Math.max(this.getCredit() + amount, 0);

            try {
                // Met à jour le nombre d'Omega Coins dans Redis
                this.setHashValue("omega", Long.toString(newAmount));

                // Appelle le callback si fourni
                if (financialCallback != null) {
                    financialCallback.done(newAmount, amount, null);
                }
            } catch (Throwable throwable) {
                // En cas d'erreur, gère l'exception et appelle le callback si fourni
                Common.throwError(throwable);
                if (financialCallback != null) {
                    financialCallback.done(newAmount, amount, throwable);
                }
            }
        });
    }

    /* ========================
     * > PlayerBean management
     * ======================== */
    public PlayerBean getPlayerBean() {
        return new PlayerBean(
                this.getUniqueId(),
                this.getEffectiveName(),
                this.getCustomName(),
                (int) this.getCredit(),
                this.getLastLogin(),
                this.getFirstLogin(),
                this.getLastIp(),
                this.getGroupId(),
                this.getLogs()
        );
    }

    /* ========================
     * > Jedis management
     * ======================== */

    /**
     * Active une expiration pour les données du joueur dans Redis.
     * Les données seront automatiquement supprimées après la durée spécifiée.
     */
    public void expire() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            // Active l'expiration des données du joueur dans Redis
            jedis.expire(this.jedisAccountKey, 3/*((60 * 60) * 3)*/);
            jedis.expire(this.jedisLogsKey, 3);
        }
    }

    /**
     * Désactive l'expiration pour les données du joueur dans Redis.
     * Les données ne seront pas supprimées automatiquement.
     */
    public void persist() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            // Désactive l'expiration des données du joueur dans Redis
            jedis.persist(this.jedisAccountKey);
            jedis.persist(this.jedisLogsKey);
        }
    }

    /**
     * Supprime les données du joueur de Redis.
     * Les données seront irrémédiablement supprimées.
     */
    public void delete() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            // Supprime les données du joueur de Redis
            jedis.del(this.jedisAccountKey);
            jedis.del(this.jedisLogsKey);
        }
    }

    /**
     * Charge les données d'un joueur depuis l'objet PlayerBean dans Redis.
     * Les données sont stockées sous forme de champs hachés dans Redis.
     *
     * @param paramPlayerBean L'objet PlayerBean contenant les données du joueur.
     * @return Vrai si les données ont été chargées avec succès dans Redis, sinon faux.
     */
    public boolean loadToJedis(PlayerBean paramPlayerBean) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            // Charge les données du joueur dans Redis en tant que champs hachés
            jedis.hsetnx(this.jedisAccountKey, "uniqueId", paramPlayerBean.getUniqueId().toString());
            jedis.hsetnx(this.jedisAccountKey, "name", paramPlayerBean.getName());
            jedis.hsetnx(this.jedisAccountKey, "customName", paramPlayerBean.getNickname());
            jedis.hsetnx(this.jedisAccountKey, "omega", Integer.toString((int) paramPlayerBean.getCredit()));
            jedis.hsetnx(this.jedisAccountKey, "lastLogin", paramPlayerBean.getLastLogin().toString());
            jedis.hsetnx(this.jedisAccountKey, "firstLogin", paramPlayerBean.getFirstLogin().toString());
            jedis.hsetnx(this.jedisAccountKey, "lastIp", paramPlayerBean.getLastIp());
            jedis.hsetnx(this.jedisAccountKey, "groupId", Long.toString(paramPlayerBean.getGroupId()));

            // Charge les logs de crédit du joueur s'il y en a
            if (!paramPlayerBean.getCreditLogs().isEmpty()) {
                for (int i = 0; i < paramPlayerBean.getCreditLogs().size(); i++) {
                    jedis.rpush(this.jedisLogsKey, paramPlayerBean.getCreditLogs().get(i).toString());
                }
            }

            return true;
        } catch (Throwable throwable) {
            // En cas d'erreur, gère l'exception et renvoie false
            Common.throwError(throwable, "Impossible de charger le joueur " + paramPlayerBean.getName() + " dans Redis.");
            return false;
        }
    }

    /**
     * Récupère la valeur d'un champ haché spécifique associé au joueur dans Redis.
     *
     * @param hash Le nom du champ haché à récupérer.
     * @return La valeur du champ haché ou une chaîne vide si le champ n'existe pas.
     */
    private String getHashValue(String hash) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            return jedis.hget(this.jedisAccountKey, hash);
        }
    }

    /**
     * Définit la valeur d'un champ haché associé au joueur dans Redis.
     * Si la valeur est nulle, le champ haché est défini comme une chaîne vide.
     *
     * @param hash  Le nom du champ haché à définir.
     * @param value La valeur à associer au champ haché.
     */
    private void setHashValue(String hash, String value) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.hset(this.jedisAccountKey, hash, value == null ? "" : value);
        }
    }

}
