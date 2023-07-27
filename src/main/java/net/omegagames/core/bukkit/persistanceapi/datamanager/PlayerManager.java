package net.omegagames.core.bukkit.persistanceapi.datamanager;

import net.omegagames.core.bukkit.persistanceapi.beans.players.PlayerBean;
import net.omegagames.core.bukkit.persistanceapi.database.DatabaseManager;
import net.omegagames.core.bukkit.persistanceapi.database.MResultSet;
import net.omegagames.core.bukkit.persistanceapi.utils.Transcoder;
import org.mineacademy.fo.exception.FoException;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerManager {
    private final DatabaseManager databaseManager;

    public PlayerManager() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public PlayerBean getPlayer(UUID uuid, PlayerBean player) {
        final String paramQuery = "select HEX(" + PlayerBean.fieldUniqueId + ") as " + PlayerBean.fieldUniqueId + ", " +
                PlayerBean.fieldName + ", " +
                PlayerBean.fieldNickname + ", " +
                PlayerBean.fieldOmega + ", " +
                PlayerBean.fieldLastLogin + ", " +
                PlayerBean.fieldFirstLogin + ", " +
                PlayerBean.fieldLastIp + ", " +
                PlayerBean.fieldGroupId + " from " +
                PlayerBean.tableName + " where " +
                PlayerBean.fieldUniqueId + " = UNHEX(?)";

        MResultSet paramResultSet = this.databaseManager.request(paramQuery, Transcoder.encode(uuid.toString()));
        if (paramResultSet.next()) {
            String playerUuid = Transcoder.decode(paramResultSet.getString(PlayerBean.fieldUniqueId));
            String name = paramResultSet.getString(PlayerBean.fieldName);
            String nickName = paramResultSet.getString(PlayerBean.fieldNickname);
            int omega = paramResultSet.getInt(PlayerBean.fieldOmega);
            Timestamp lastLogin = Timestamp.valueOf(paramResultSet.getString(PlayerBean.fieldLastLogin));
            Timestamp firsLogin = Timestamp.valueOf(paramResultSet.getString(PlayerBean.fieldFirstLogin));
            String lastIP = paramResultSet.getString(PlayerBean.fieldLastIp);
            long groupId = paramResultSet.getLong(PlayerBean.fieldGroupId);
            return new PlayerBean(UUID.fromString(playerUuid), name, nickName, omega , lastLogin, firsLogin, lastIP, groupId);
        } else {
            if (this.createPlayer(player)) {
                return this.getPlayer(uuid, player);
            } else {
                throw new FoException("Impossible d'enregistrer les données du joueur en base de donnée.");
            }
        }
    }

    public boolean createPlayer(PlayerBean player) {
        String paramQuery = "insert into " + PlayerBean.tableName + " (" +
                PlayerBean.fieldUniqueId + ", " +
                PlayerBean.fieldName + ", " +
                PlayerBean.fieldNickname + ", " +
                PlayerBean.fieldOmega + ", " +
                PlayerBean.fieldLastLogin + ", " +
                PlayerBean.fieldFirstLogin + ", " +
                PlayerBean.fieldLastIp + ", " +
                PlayerBean.fieldGroupId + ")";

        paramQuery += " values (UNHEX(?), ?, ?, ?, now(), now(), ?, ?)";
        return this.databaseManager.transmission(paramQuery, Transcoder.encode(player.getUniqueId().toString()), player.getName(), player.getNickName(), player.getOmega(), player.getLastIP(), player.getGroupId());
    }

    // Update the player
    public void updatePlayer(PlayerBean player) {
        String paramQuery = "update " + PlayerBean.tableName + " set " +
                PlayerBean.fieldName + " = ?, " +
                PlayerBean.fieldOmega + " = ?, " +
                PlayerBean.fieldLastLogin + " = ?, " +
                PlayerBean.fieldLastIp + " = ?, " +
                PlayerBean.fieldGroupId + " = ?, " +
                PlayerBean.fieldNickname + " = ?";

        paramQuery += " where uuid = UNHEX(?)";
        this.databaseManager.transmission(paramQuery, player.getName(), player.getOmega(), player.getLastLogin().toString(), player.getLastIP(), player.getGroupId(), player.getNickName(), Transcoder.encode(player.getUniqueId().toString()));
    }
}
