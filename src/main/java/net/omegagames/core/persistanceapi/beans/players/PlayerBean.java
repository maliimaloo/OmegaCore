package net.omegagames.core.persistanceapi.beans.players;

import org.mineacademy.fo.Common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
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

    public static String tableName = "t_lobby_players";
    public static String fieldUniqueId = "uuid";
    public static String fieldName = "name";
    public static String fieldNickname = "nickname";
    public static String fieldOmega = "omega";
    public static String fieldLastLogin = "last_login";
    public static String fieldFirstLogin = "first_login";
    public static String fieldLastIp = "last_ip";
    public static String fieldGroupId = "group_id";

    // Define attributes
    private UUID uniqueId;
    private String name;
    private String nickName;
    private int omega;
    private Timestamp lastLogin;
    private Timestamp firstLogin;
    private String lastIP;
    private long groupId;

    //Empty constructor because we fill it when needed
    public PlayerBean() {
        super();
    }

    // Constructor
    public PlayerBean(UUID uniqueId, String name, String nickName, int omega, Timestamp lastLogin, Timestamp firstLogin, String lastIP, long groupId) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.nickName = nickName;
        this.omega = omega;
        this.lastLogin = lastLogin;
        this.firstLogin = firstLogin;
        this.lastIP = lastIP;
        this.groupId = groupId;
    }

    // Getters
    public UUID getUniqueId() {
        return this.uniqueId;
    }
    public String getName() {
        return this.name;
    }
    public String getNickName() { return this.nickName; }
    public int getOmega() {
        return this.omega;
    }
    public Timestamp getLastLogin() {
        return this.lastLogin;
    }
    public Timestamp getFirstLogin() { return this.firstLogin; }
    public String getLastIP() { return this.lastIP; }
    public long getGroupId() { return this.groupId; }

    // Setters
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public void setOmega(int coins) {
        this.omega = coins;
    }
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
    public void setFirstLogin(Timestamp firstLogin) {
        this.firstLogin = firstLogin;
    }
    public void setLastIP(String lastIP) { this.lastIP = lastIP; }
    public void setGroupId(long groupId) { this.groupId = groupId; }

    public List<String> toStringList() {
        final ArrayList<String> paramStringList = new ArrayList<>();
        paramStringList.add("&6" + Common.chatLine());
        paramStringList.add("PlayerBean: " + this.name);
        paramStringList.add("");
        paramStringList.add("unique_id: " + this.getUniqueId().toString());
        paramStringList.add("Name: " + this.name);
        paramStringList.add("nick_name: " + this.nickName);
        paramStringList.add("omega: " + this.omega);
        paramStringList.add("last_login: " + this.lastLogin.toString());
        paramStringList.add("first_login: " + this.firstLogin.toString());
        paramStringList.add("last_ip: " + this.lastIP);
        paramStringList.add("group_id: " + this.groupId);
        paramStringList.add(Common.chatLine());
        return paramStringList;
    }
}
