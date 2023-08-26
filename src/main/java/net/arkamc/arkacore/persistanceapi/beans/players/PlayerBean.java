package net.arkamc.arkacore.persistanceapi.beans.players;

import lombok.Getter;
import lombok.Setter;
import net.arkamc.arkacore.persistanceapi.beans.credit.CreditBean;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class PlayerBean {

    /* Database Structure
    Table : t_lobby_players
    +-------------+--------------+------+-----+---------------------+-------+
    | Field       | Type         | Null | Key | Default             | Extra |
    +-------------+--------------+------+-----+---------------------+-------+
    | uuid        | binary(16)   | NO   | PRI |                     |       |
    | name        | varchar(255) | NO   |     |                     |       |
    | nickname    | varchar(45)  | YES  |     | NULL                |       |
    | credit      | int(11)      | YES  |     | 0                   |       |
    | last_login  | timestamp    | NO   |     | current_timestamp() |       |
    | first_login | timestamp    | NO   |     | current_timestamp() |       |
    | last_ip     | varchar(15)  | YES  |     | NULL                |       |
    | group_id    | bigint(20)   | YES  |     | NULL                |       |
    | whisper     | varchar(5)   | NO   |     | FALSE               |       |
    +-------------+--------------+------+-----+---------------------+-------+
    */

    @Getter
    private final static String tableName = "t_lobby_players";
    @Getter
    private final static String fieldUniqueId = "uuid";
    @Getter
    private final static String fieldName = "name";
    @Getter
    private final static String fieldNickname = "nickname";
    @Getter
    private final static String fieldCredit = "credit";
    @Getter
    private final static String fieldLastLogin = "last_login";
    @Getter
    private final static String fieldFirstLogin = "first_login";
    @Getter
    private final static String fieldLastIp = "last_ip";
    @Getter
    private final static String fieldGroupId = "group_id";
    @Getter
    private final static String fieldWhisper = "whisper";

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
    boolean whisper;

    @Getter @Setter
    List<CreditBean> creditLogs;

    public PlayerBean() {
        super();
    }

    // Constructor
    public PlayerBean(UUID uniqueId, String name, String nickName, long credit, Timestamp lastLogin, Timestamp firstLogin, String lastIP, long groupId, boolean whisper, List<CreditBean> creditLogs) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.nickname = nickName;
        this.credit = credit;
        this.lastLogin = lastLogin;
        this.firstLogin = firstLogin;
        this.lastIp = lastIP;
        this.groupId = groupId;
        this.whisper = whisper;
        this.creditLogs = creditLogs;
    }
}