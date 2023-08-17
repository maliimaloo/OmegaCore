package net.arkamc.core.persistanceapi.beans.players;

import lombok.Getter;
import lombok.Setter;
import net.arkamc.core.persistanceapi.beans.credit.CreditBean;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class PlayerBean {

    /* Database Structure
    Table : t_lobby_players
    +-------------+--------------+------+-----+---------------------+-------+
    | Field       | Type         | Null | Key | Default             | Extra |
    +-------------+--------------+------+-----+---------------------+-------+
    | uuid        | binary(16)   | NO   | PRI | NULL                |       |
    | name        | varchar(255) | NO   |     | NULL                |       |
    | nickname    | varchar(45)  | YES  |     | NULL                |       |
    | omega       | int(11)      | YES  |     | NULL                |       |
    | last_login  | timestamp    | NO   |     | 0000-00-00 00:00:00 |       |
    | first_login | timestamp    | NO   |     | 0000-00-00 00:00:00 |       |
    | last_ip     | varchar(15)  | YES  |     | NULL                |       |
    | group_id    | bigint(20)   | NO   |     | NULL                |       |
    +-------------+--------------+------+-----+---------------------+-------+
    */

    @Getter
    private static String tableName = "t_lobby_players";
    @Getter
    private static String fieldUniqueId = "uuid";
    @Getter
    private static String fieldName = "name";
    @Getter
    private static String fieldNickname = "nickname";
    @Getter
    private static String fieldCredit = "credit";
    @Getter
    private static String fieldLastLogin = "last_login";
    @Getter
    private static String fieldFirstLogin = "first_login";
    @Getter
    private static String fieldLastIp = "last_ip";
    @Getter
    private static String fieldGroupId = "group_id";

    @Getter @Setter
    UUID uniqueId;

    @Getter @Setter
    String name;

    @Getter @Setter
    String nickname;
    @Getter @Setter
    long credit;

    @Getter @Setter
    Timestamp lastLogin;

    @Getter @Setter
    Timestamp firstLogin;

    @Getter @Setter
    String lastIp;

    @Getter @Setter
    long groupId;

    @Getter @Setter
    List<CreditBean> creditLogs;

    public PlayerBean() {
        super();
    }

    // Constructor
    public PlayerBean(UUID uniqueId, String name, String nickName, int credit, Timestamp lastLogin, Timestamp firstLogin, String lastIP, long groupId, List<CreditBean> creditLogs) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.nickname = nickName;
        this.credit = credit;
        this.lastLogin = lastLogin;
        this.firstLogin = firstLogin;
        this.lastIp = lastIP;
        this.groupId = groupId;
        this.creditLogs = creditLogs;
    }
}