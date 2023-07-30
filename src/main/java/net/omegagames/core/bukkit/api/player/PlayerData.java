package net.omegagames.core.bukkit.api.player;

import net.omegagames.api.player.AbstractPlayerData;
import net.omegagames.api.player.IFinancialCallback;
import net.omegagames.core.bukkit.persistanceapi.beans.players.PlayerBean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerData extends AbstractPlayerData {
    private final Jedis jedis;
    private final String key;

    public PlayerData(Jedis jedis, UUID uniqueId) {
        this.jedis = jedis;
        this.key = "omegacore:account:" + uniqueId.toString();
    }

    @Override
    public UUID getUniqueId() {
        return UUID.fromString(this.getHashValue("uniqueId"));
    }

    /**
     * ========================
     * > DisplayName management
     * ========================
     */
    @Override
    public String getEffectiveName() {
        return this.getHashValue("name");
    }

    @Override
    public String getCustomName() {
        return this.getHashValue("customName");
    }

    @Override
    public String getDisplayName() {
        return this.getCustomName() != null ? this.getCustomName() : this.getEffectiveName();
    }

    @Override
    public void setEffectiveName(String name) {
        this.setHashValue("name", name);
    }

    @Override
    public void setCustomName(String name) {
        this.setHashValue("customName", name);
    }

    @Override
    public boolean hasNickname() {
        return this.getCustomName() != null;
    }

    /**
     * ========================
     * > State management
     * ========================
     */
    @Override
    public Timestamp getLastLogin() {
        return Timestamp.valueOf(this.getHashValue("lastLogin"));
    }

    @Override
    public Timestamp getFirstLogin() {
        return Timestamp.valueOf(this.getHashValue("firstLogin"));
    }

    @Override
    public String getLastIp() {
        return this.getHashValue("lastIp");
    }

    @Override
    public void setLastLogin(Timestamp lastLogin) {
        this.setHashValue("lastLogin", lastLogin.toString());
    }

    @Override
    public void setFirstLogin(Timestamp firstLogin) {
        this.setHashValue("firstLogin", firstLogin.toString());
    }

    @Override
    public void setLastIp(String lastIp) {
        this.setHashValue("lastIp", lastIp);
    }

    /**
     * ========================
     * > Groupe management
     * ========================
     */
    @Override
    public Long getGroupId() {
        return Long.parseLong(this.getHashValue("groupId"));
    }

    @Override
    public void setGroupId(Long groupId) {
        this.setHashValue("groupId", groupId.toString());
    }

    /**
     * ========================
     * > Coins management
     * ========================
     */
    @Override
    public long getOmegaCoins() {
        return Integer.parseInt(this.getHashValue("omegaCoins"));
    }

    @Override
    public void creditOmegaCoins(long amount, String reason, boolean applyMultiplier, IFinancialCallback financialCallback) {
        this.creditEconomy(0, amount, reason, applyMultiplier, financialCallback);
    }

    @Override
    public void withdrawOmegaCoins(long amount, String reason, IFinancialCallback financialCallback) {
        this.creditEconomy(0, -amount, reason, false, financialCallback);
    }

    @Override
    public long increaseOmegaCoins(long increaseBy) {
        int result = (int) (this.getOmegaCoins() + (int) increaseBy);
        this.setHashValue("omegaCoins", Integer.toString(result));
        return result;
    }

    @Override
    public long decreaseOmegaCoins(long decreaseBy) {
        return this.increaseOmegaCoins(-decreaseBy);
    }

    private void creditEconomy(int type, long amountFinal, String reason, boolean applyMultiplier, IFinancialCallback financialCallback) {
        Common.runAsync(() ->  {
            try {
                String message = "Aucune raison spécifiée.";
                if (reason != null) {
                    message = reason;
                }

                if (this.getUniqueId() != null) {
                    Player paramReceiver = Bukkit.getPlayer(this.getUniqueId());
                    if (amountFinal > 0) {
                        Messenger.success(paramReceiver, "Vous venez de recevoir " + amountFinal + " omega. [" + message + "]");
                    } else {
                        Messenger.success(paramReceiver, "Vous venez de perdre " + amountFinal + " omega. [" + message + "]");
                    }
                }

                long result = (type == 0) ? this.increaseOmegaCoins(amountFinal) : 0;

                if (financialCallback != null) {
                    financialCallback.done(result, amountFinal, null);
                }
            } catch (Throwable throwable) {
                Common.throwError(throwable);
            }
        });
    }

    /**
     * ========================
     * > PlayerBean management
     * ========================
     */
    public PlayerBean getPlayerBean() {
        return new PlayerBean(
                this.getUniqueId(),
                this.getEffectiveName(),
                this.getCustomName(),
                (int) this.getOmegaCoins(),
                this.getLastLogin(),
                this.getFirstLogin(),
                this.getLastIp(),
                this.getGroupId()
        );
    }

    /**
     * ========================
     * > Jedis management
     * ========================
     */
    private String getHashValue(String hash) {
        try (Jedis jedis = this.jedis) {
            return jedis.hget(this.key, hash);
        }
    }

    private void setHashValue(String hash, String value) {
        try (Jedis jedis = this.jedis) {
            jedis.hset(this.key, hash, value);
        }
    }
}
