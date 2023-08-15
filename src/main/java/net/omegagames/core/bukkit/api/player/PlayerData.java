package net.omegagames.core.bukkit.api.player;

import net.omegagames.api.player.AbstractPlayerData;
import net.omegagames.api.player.IFinancialCallback;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.persistanceapi.beans.credit.CreditBean;
import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import org.mineacademy.fo.Common;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData extends AbstractPlayerData {
    private final ApiImplementation api;
    private final String jedisAccountKey;
    private final String jedisCreditLogsKey;

    private final UUID uniqueId;

    public PlayerData(ApiImplementation api, UUID uniqueId) {
        this.api = api;
        this.jedisAccountKey = "omegacore:account:" + uniqueId;
        this.jedisCreditLogsKey = "omegacore:credit_logs:" + uniqueId;
        this.uniqueId = uniqueId;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public boolean isLoaded() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            return jedis.exists(this.jedisAccountKey);
        }
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


    /* ========================
     * > State management
     * ======================== */

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


    /* ========================
     * > Groupe management
     * ======================== */
    @Override
    public Long getGroupId() {
        return Long.parseLong(this.getHashValue("groupId"));
    }

    @Override
    public void setGroupId(Long groupId) {
        this.setHashValue("groupId", groupId.toString());
    }

    /* ========================
     * > Logs management
     * ======================== */
    public List<CreditBean> getLogs() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            List<CreditBean> logs = new ArrayList<>();
            for (String log : jedis.lrange(this.jedisCreditLogsKey, 0, -1)) {
                logs.add(CreditBean.fromString(this.getUniqueId(), log));
            }

            return logs;
        }
    }

    public boolean addLog(CreditBean log) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.rpush(this.jedisCreditLogsKey, log.toString());
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    /* ========================
     * > Coins management
     * ======================== */
    @Override
    public long getOmegaCoins() {
        return Integer.parseInt(this.getHashValue("omega"));
    }

    @Override
    public void creditOmegaCoins(long amount, IFinancialCallback financialCallback) {
        this.creditEconomy(amount, financialCallback);
    }

    @Override
    public void withdrawOmegaCoins(long amount, IFinancialCallback financialCallback) {
        this.creditEconomy(-amount, financialCallback);
    }

    @Override
    public boolean hasEnoughOmegaCoins(long amount) {
        return this.getOmegaCoins() >= amount;
    }

    private void creditEconomy(long amount, IFinancialCallback financialCallback) {
        Common.runAsync(() ->  {
            long newAmount = Math.max(this.getOmegaCoins() + amount, 0);

            try {
                this.setHashValue("omega", Long.toString(newAmount));
                if (financialCallback != null) {
                    financialCallback.done(newAmount, amount, null);
                }
            } catch (Throwable throwable) {
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
                (int) this.getOmegaCoins(),
                this.getLastLogin(),
                this.getFirstLogin(),
                this.getLastIp(),
                this.getGroupId(),
                this.getLogs()
        );
    }

    /**
     * ========================
     * > Jedis management
     * ========================
     */
    @Override
    public void expire() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.expire(this.jedisAccountKey, 3/*((60 * 60) * 3)*/);
            jedis.expire(this.jedisCreditLogsKey, 3);
        }
    }

    @Override
    public void persist() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.persist(this.jedisAccountKey);
            jedis.persist(this.jedisCreditLogsKey);
        }
    }

    @Override
    public void delete() {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.del(this.jedisAccountKey);
            jedis.del(this.jedisCreditLogsKey);
        }
    }

    public boolean loadToJedis(PlayerBean paramPlayerBean) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.hsetnx(this.jedisAccountKey, "uniqueId", paramPlayerBean.getUniqueId().toString());
            jedis.hsetnx(this.jedisAccountKey, "name", paramPlayerBean.getName());
            jedis.hsetnx(this.jedisAccountKey, "customName", paramPlayerBean.getNickname());
            jedis.hsetnx(this.jedisAccountKey, "omega", Integer.toString((int) paramPlayerBean.getOmega()));
            jedis.hsetnx(this.jedisAccountKey, "lastLogin", paramPlayerBean.getLastLogin().toString());
            jedis.hsetnx(this.jedisAccountKey, "firstLogin", paramPlayerBean.getFirstLogin().toString());
            jedis.hsetnx(this.jedisAccountKey, "lastIp", paramPlayerBean.getLastIp());
            jedis.hsetnx(this.jedisAccountKey, "groupId", Long.toString(paramPlayerBean.getGroupId()));
            if (!paramPlayerBean.getCreditLogs().isEmpty()) {
                for (int i = 0; i < paramPlayerBean.getCreditLogs().size(); i++) {
                    jedis.rpush(this.jedisCreditLogsKey, paramPlayerBean.getCreditLogs().get(i).toString());
                }
            }
            return true;
        } catch (Throwable throwable) {
            Common.throwError(throwable, "Impossible de charger le joueur " + paramPlayerBean.getName() + " dans Redis.");
            return false;
        }
    }

    private String getHashValue(String hash) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            return jedis.hget(this.jedisAccountKey, hash);
        }
    }

    private void setHashValue(String hash, String value) {
        try (Jedis jedis = this.api.getBungeeResource()) {
            jedis.hset(this.jedisAccountKey, hash, value == null ? "" : value);
        }
    }
}
