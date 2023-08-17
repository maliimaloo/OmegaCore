package net.arkamc.core.persistanceapi.beans.credit;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CreditBean {
    /* Database Structure
    Table : t_lobby_credit_logs
    +-------------+--------------+------+-----+---------------------+-------+
    | Field       | Type         | Null | Key | Default             | Extra |
    +-------------+--------------+------+-----+---------------------+-------+
    | players_uuid| binary(16)   | NO   |     | NULL                |       |
    | timestamp   | timestamp    | NO   | PRI | 0000-00-00 00:00:00 |       |
    | type        | varchar(45)  | NO   |     | NULL                |       |
    | sender      | varchar(45)  | NO   |     | NULL                |       |
    | receiver    | varchar(45)  | NO   |     | NULL                |       |
    | amount      | int(11)      | NO   |     | NULL                |       |
    | reason      | varchar(255) | NO   |     | NULL                |       |
    +-------------+--------------+------+-----+---------------------+-------+
    */

    @Getter
    private static String tableName = "t_lobby_credit_logs";
    @Getter
    private static String fieldUniqueId = "players_uuid";
    @Getter
    private static String fieldTimestamp = "timestamp";
    @Getter
    private static String fieldType = "type";
    @Getter
    private static String fieldSender = "sender";
    @Getter
    private static String fieldReceiver = "receiver";
    @Getter
    private static String fieldAmount = "amount";
    @Getter
    private static String fieldReason = "reason";

    @Getter @Setter
    private UUID uniqueId;
    @Getter @Setter
    private Timestamp timestamp;
    @Getter @Setter
    private String type;
    @Getter @Setter
    private String sender;
    @Getter @Setter
    private String receiver;
    @Getter @Setter
    private int amount;
    @Getter @Setter
    private String reason;

    public CreditBean(UUID uniqueId, Timestamp timestamp, String type, String sender, String receiver, int amount, String reason) {
        this.uniqueId = uniqueId;
        this.timestamp = timestamp;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.reason = reason;
    }

    public String getTimestampToDate() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm:ss");
        return this.getTimestamp().toLocalDateTime().format(formatter);
    }

    @Override
    public String toString() {
        return this.getTimestampToDate() + "," + this.getType() + "," + this.getSender() + "," + this.getReceiver() + "," + this.getAmount() + "," + this.getReason();
    }

    public static CreditBean fromString(UUID uniqueId, String creditString) {
        String[] creditStringArray = creditString.split(",");
        return new CreditBean(uniqueId, Timestamp.valueOf(creditStringArray[0]), creditStringArray[1], creditStringArray[2], creditStringArray[3], Integer.parseInt(creditStringArray[4]), creditStringArray[5] == null ? "" : creditStringArray[5]);
    }
}
