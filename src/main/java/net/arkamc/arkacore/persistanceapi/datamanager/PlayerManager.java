package net.arkamc.arkacore.persistanceapi.datamanager;

import net.arkamc.arkacore.bukkit.util.model.Callback;
import net.arkamc.arkacore.persistanceapi.SqlServiceManager;
import net.arkamc.arkacore.persistanceapi.beans.credit.CreditBean;
import net.arkamc.arkacore.persistanceapi.beans.players.PlayerBean;
import net.arkamc.arkacore.persistanceapi.database.DatabaseManager;
import net.arkamc.arkacore.persistanceapi.database.MResultSet;
import net.arkamc.arkacore.persistanceapi.utils.Transcoder;
import org.mineacademy.fo.exception.FoException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Gestionnaire de joueurs pour interagir avec la base de données des joueurs.
 */
public class PlayerManager {
    private final DatabaseManager databaseManager;

    public PlayerManager(SqlServiceManager manager) {
        this.databaseManager = manager.getDatabaseManager();
    }

    /**
     * Récupère un joueur à partir de son UUID.
     *
     * @param uuid     L'UUID du joueur à récupérer.
     * @param callback Le callback appelé lorsqu'un joueur est récupéré ou lorsque l'opération échoue.
     * @return Le bean du joueur si trouvé, sinon null.
     */
    public PlayerBean getPlayer(UUID uuid, Callback<PlayerBean> callback) {
        String query = "SELECT * FROM " + PlayerBean.getTableName() + " " +
                "LEFT JOIN " + CreditBean.getTableName() + " " +
                "ON " + PlayerBean.getTableName() + "." + PlayerBean.getFieldUniqueId() + " = " + CreditBean.getTableName() + "." + CreditBean.getFieldUniqueId() + " " +
                "WHERE " + PlayerBean.getTableName() + "." + PlayerBean.getFieldUniqueId() + " = ?;";

        MResultSet resultSet = this.databaseManager.request(query, Transcoder.encode(uuid.toString()));
        if (resultSet.next()) {
            PlayerBean playerBean = convertToPlayerBean(resultSet);
            if (callback != null) {
                callback.onSuccess(playerBean);
            }

            return playerBean;
        }

        if (callback != null) {
            callback.onFailure(new FoException("Impossible de récupérer le joueur dans la base de données."));
        }

        return null;
    }

    /**
     * Récupère un joueur à partir de son DisplayName.
     *
     * @param displayName Le nickname du joueur
     * @param callback Le callback appelé lorsqu'un joueur est récupéré ou lorsque l'opération échoue.
     * @return Le bean du joueur si trouvé, sinon null.
     */
    public PlayerBean getPlayer(String displayName, Callback<PlayerBean> callback) {
        String query = "SELECT * FROM " + PlayerBean.getTableName() +
                " INNER JOIN " + CreditBean.getTableName() +
                " ON " + PlayerBean.getTableName() + "." + PlayerBean.getFieldUniqueId() + " = " + CreditBean.getTableName() + "." + CreditBean.getFieldUniqueId() +
                " WHERE " + PlayerBean.getFieldName() + " = ?";

        MResultSet resultSet = this.databaseManager.request(query, displayName);
        if (resultSet.first()) {
            PlayerBean playerBean = convertToPlayerBean(resultSet);
            if (callback != null) {
                callback.onSuccess(playerBean);
            }

            return playerBean;
        }

        if (callback != null) {
            callback.onFailure(new FoException("Impossible de récupérer le joueur dans la base de données."));
        }

        return null;
    }


    /**
     * Crée un nouveau joueur dans la base de données.
     *
     * @param player Le bean du joueur à créer.
     * @return true si le joueur a été créé avec succès, sinon false.
     */
    public boolean createPlayer(PlayerBean player) {
        String query = "INSERT INTO " + PlayerBean.getTableName() + " (" +
                PlayerBean.getFieldUniqueId() + ", " +
                PlayerBean.getFieldName() + ", " +
                PlayerBean.getFieldNickname() + ", " +
                PlayerBean.getFieldCredit() + ", " +
                PlayerBean.getFieldLastLogin() + ", " +
                PlayerBean.getFieldFirstLogin() + ", " +
                PlayerBean.getFieldLastIp() + ", " +
                PlayerBean.getFieldGroupId() + ")";
        query += " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return this.databaseManager.transmission(query, Transcoder.encode(player.getUniqueId().toString()), player.getName(), player.getNickname(), player.getCredit(), player.getFirstLogin(), player.getLastLogin(), player.getLastIp(), player.getGroupId());
    }

    /**
     * Met à jour les informations du joueur dans la base de données.
     *
     * @param playerBean   Le bean du joueur à mettre à jour.
     * @param callback     Le callback appelé lorsque la mise à jour est effectuée ou lorsque l'opération échoue.
     */
    public void updatePlayer(PlayerBean playerBean, Callback<Integer> callback) {
        final String updateQuery = "UPDATE " + PlayerBean.getTableName() + " SET " +
                PlayerBean.getFieldName() + " = ?, " +
                PlayerBean.getFieldCredit() + " = ?, " +
                PlayerBean.getFieldLastLogin() + " = ?, " +
                PlayerBean.getFieldLastIp() + " = ?, " +
                PlayerBean.getFieldGroupId() + " = ?, " +
                PlayerBean.getFieldNickname() + " = ?, " +
                PlayerBean.getFieldWhisper() + " = ? " +
                "WHERE " + PlayerBean.getFieldUniqueId() + " = ?";

        this.databaseManager.asyncTransmission(updateQuery, (response) -> {
            if (callback != null) {
                callback.onSuccess(response);
            }

            if (playerBean.getCreditLogs() != null && !playerBean.getCreditLogs().isEmpty()) {
                String insertLogsQuery = "INSERT IGNORE INTO " + CreditBean.getTableName() + " (" +
                        CreditBean.getFieldUniqueId() + ", " +
                        CreditBean.getFieldTimestamp() + ", " +
                        CreditBean.getFieldType() + ", " +
                        CreditBean.getFieldSender() + ", " +
                        CreditBean.getFieldReceiver() + ", " +
                        CreditBean.getFieldAmount() + ", " +
                        CreditBean.getFieldReason() + ") VALUES ";

                StringJoiner valuesJoiner = new StringJoiner(", ");
                for (CreditBean creditLog : playerBean.getCreditLogs()) {
                    String values = String.format("('%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                            Transcoder.encode(playerBean.getUniqueId().toString()),
                            creditLog.getTimestamp().toString(),
                            creditLog.getType(),
                            creditLog.getSender(),
                            creditLog.getReceiver(),
                            creditLog.getAmount(),
                            creditLog.getReason());
                    valuesJoiner.add(values);
                }

                insertLogsQuery += valuesJoiner.toString();
                this.databaseManager.asyncTransmission(insertLogsQuery);
            }
        }, playerBean.getName(), playerBean.getCredit(), playerBean.getLastLogin().toString(), playerBean.getLastIp(), playerBean.getGroupId(), playerBean.getNickname(), playerBean.isWhisper() ? 1 : 0, Transcoder.encode(playerBean.getUniqueId().toString()));
    }

    /**
     * Réinitialiser les logs des transactions de crédit d'un joueur
     *
     * @param paramPlayerBean Le bean du joueur à mettre à jour.
     * @param callback        Le callback appelé lorsque la mise à jour est effectuée ou lorsque l'opération échoue.
     */
    public void resetPlayerLogs(PlayerBean paramPlayerBean, Callback<Integer> callback) {
        final String query = "DELETE FROM " + CreditBean.getTableName() + " " +
                "WHERE " + CreditBean.getFieldUniqueId() + " = ?";

        this.databaseManager.asyncTransmission(query, paramRow -> {
            if (callback != null) {
                callback.onSuccess(paramRow);
            }
        }, Transcoder.encode(paramPlayerBean.getUniqueId().toString()));
    }

    // Méthode privée pour convertir le résultat de la requête en PlayerBean
    private PlayerBean convertToPlayerBean(MResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }

        final String uniqueId = Transcoder.decode(resultSet.getString(PlayerBean.getFieldUniqueId()));
        final String name = resultSet.getString(PlayerBean.getFieldName());
        final String nickname = resultSet.getString(PlayerBean.getFieldNickname());
        final int omega = resultSet.getInt(PlayerBean.getFieldCredit());
        final Timestamp lastLogin = Timestamp.valueOf(resultSet.getString(PlayerBean.getFieldLastLogin()));
        final Timestamp firstLogin = Timestamp.valueOf(resultSet.getString(PlayerBean.getFieldFirstLogin()));
        final String lastIP = resultSet.getString(PlayerBean.getFieldLastIp());
        final long groupId = resultSet.getLong(PlayerBean.getFieldGroupId());
        final boolean whisper = resultSet.getInt(PlayerBean.getFieldWhisper()) != 0;
        final List<CreditBean> creditLogs = new ArrayList<>();
        do {
            if (resultSet.getString(CreditBean.getFieldUniqueId()) == null) {
                break;
            }

            creditLogs.add(convertToCreditBean(resultSet));
        } while (resultSet.next());

        return new PlayerBean(UUID.fromString(uniqueId), name, nickname, omega, lastLogin, firstLogin, lastIP, groupId, whisper, creditLogs);
    }

    private CreditBean convertToCreditBean(MResultSet resultSet) {
        final String uniqueId = Transcoder.decode(resultSet.getString(CreditBean.getFieldUniqueId()));
        final Timestamp timestamp = Timestamp.valueOf(resultSet.getString(CreditBean.getFieldTimestamp()));
        final String type = resultSet.getString(CreditBean.getFieldType());
        final String sender = resultSet.getString(CreditBean.getFieldSender());
        final String receiver = resultSet.getString(CreditBean.getFieldReceiver());
        final int amount = resultSet.getInt(CreditBean.getFieldAmount());
        final String reason = resultSet.getString(CreditBean.getFieldReason());

        return new CreditBean(UUID.fromString(uniqueId), timestamp, type, sender, receiver, amount, reason);
    }
}
