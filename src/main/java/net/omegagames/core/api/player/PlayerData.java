package net.omegagames.core.api.player;

import net.omegagames.api.player.AbstractPlayerData;
import net.omegagames.api.player.IFinancialCallback;
import net.omegagames.core.ApiImplementation;
import net.omegagames.core.PluginCore;
import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.exception.FoException;
import redis.clients.jedis.Jedis;

import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerData extends AbstractPlayerData {
    protected final ApiImplementation api;
    protected final PlayerDataManager manager;

    private PlayerBean playerBean;

    private long lastRefresh;
    private final UUID playerUniqueID;

    private final static String key = "playerdata:";
    private boolean loaded = false;

    protected PlayerData(UUID playerUniqueID, ApiImplementation api, PlayerDataManager manager) {
        this.playerUniqueID = playerUniqueID;
        this.api = api;
        this.manager = manager;

        this.playerBean = new PlayerBean(this.playerUniqueID, "", null, 0, null, null, null, 0);
        this.refreshData();
    }

    public boolean refreshData() {
        this.lastRefresh = System.currentTimeMillis();

        //Load from redis
        try (Jedis jedis = this.api.getBungeeResource()) {
            if (jedis.exists(key + this.playerUniqueID)) {
                this.playerBean = SerializeUtil.deserialize(SerializeUtil.Mode.JSON, PlayerBean.class, jedis.get(key + this.playerUniqueID));
            } else {
                this.playerBean = this.api.getServerServiceManager().getPlayer(this.playerUniqueID, this.playerBean);
            }

            this.loaded = true;
            return true;
        } catch (Exception exception) {
            Common.error(exception);
        }

        return false;
    }

    /**
     *  Doit être appelé avant de modifier les données.
     */
    public void refreshIfNeeded() {
        if (this.lastRefresh + 1000 * 60 < System.currentTimeMillis()) {
            this.refreshData();
        }
    }


    public void updateData() {
        if (this.playerBean != null && this.loaded) {
            //Save in redisResource
            //Jedis jedis = api.getBungee();
            //Generated class so FUCK IT I made it static
            //CacheLoader.send(jedis, key + playerUUID, playerBean);

            try (Jedis jedis = this.api.getBungeeResource()){
                if (jedis != null) {
                    jedis.set(key + this.playerUniqueID, SerializeUtil.serialize(SerializeUtil.Mode.JSON, this.playerBean).toString());
                } else {
                    this.api.getServerServiceManager().updatePlayer(this.playerBean);
                }
            } catch (Throwable throwable) {
                throw new FoException(throwable);
            }
        }
    }

    @Override
    public UUID getPlayerID() {
        return this.playerUniqueID;
    }

    /**
     * ========================
     * > DisplayName management
     * ========================
     */
    @Override
    public String getDisplayName() {
        return this.hasNickname() ? this.getCustomName() : this.getEffectiveName();
    }

    @Override
    public String getEffectiveName() {
        return this.playerBean.getName();
    }

    @Override
    public String getCustomName() {
        return this.playerBean.getNickName();
    }

    @Override
    public boolean hasNickname() {
        return this.getCustomName() != null && !this.getCustomName().equals("null");
    }

    /**
     * ========================
     * > Coins management
     * ========================
     */
    @Override
    public void creditCoins(long amount, String reason, boolean applyMultiplier, IFinancialCallback iFinancialCallback) {
        this.creditEconomy(0, amount, reason, applyMultiplier, iFinancialCallback);
    }

    @Override
    public void withdrawCoins(long amount, IFinancialCallback iFinancialCallback) {
        PluginCore.getInstance().getExecutor().execute(() -> {
            long result = this.decreaseCoins(amount);
            if (iFinancialCallback != null) {
                iFinancialCallback.done(result, -amount, null);
            }
        });
    }

    @Override
    public long increaseCoins(long incrBy) {
        this.refreshData();
        int result = (int) (this.playerBean.getOmega() + incrBy);
        this.playerBean.setOmega(result);
        this.updateData();
        return result;
    }

    @Override
    public long decreaseCoins(long decrBy) {
        return this.increaseCoins(-decrBy);
    }

    @Override
    public long getOmega() {
        this.refreshIfNeeded();
        return this.playerBean.getOmega();
    }

    public PlayerBean getPlayerBean() {
        return this.playerBean;
    }



    private void creditEconomy(int type, long amountFinal, String reason, boolean applyMultiplier, IFinancialCallback financialCallback) {
        PluginCore.getInstance().getExecutor().execute(() -> {
            try {
                String message;
                if (reason != null) {
                    message = reason;
                    if (this.getPlayerID() != null) {
                        Player paramReceiver = Bukkit.getPlayer(this.getPlayerID());
                        Messenger.success(paramReceiver, "Vous venez de recevoir " + amountFinal + " omega. [" + message + "]" );
                    }
                }

                long result = (type == 0) ? this.increaseCoins(amountFinal) : 0;

                if (financialCallback != null) {
                    financialCallback.done(result, amountFinal, null);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static String getKey() {
        return key;
    }
}
