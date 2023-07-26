package net.omegagames.core.api.player;

import net.omegagames.api.player.IPlayerDataManager;
import net.omegagames.core.ApiImplementation;
import org.bukkit.Bukkit;
import org.mineacademy.fo.exception.FoException;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager implements IPlayerDataManager {
    protected final ApiImplementation api;
    protected final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(ApiImplementation api) {
        this.api = api;
    }

    @Override
    public PlayerData getPlayerData(UUID player) {
        return this.getPlayerData(player, false);
    }

    @Override
    public PlayerData getPlayerData(UUID player, boolean forceRefresh) {
        if (player == null) {
            throw new FoException("Les paramètres du joueur est null !");
        }

        PlayerData paramData = this.cache.get(player);
        if (paramData == null) {
            Bukkit.getLogger().severe(player + " ne se trouve pas en cache !");
        }

        return paramData;
    }

    public PlayerData getPlayerDataByName(String name) {
        for (PlayerData data : this.cache.values()) {
            if (data.getEffectiveName().equals(name))
                return data;
        }

        return null;
    }

    public void loadPlayer(UUID player) {
        try{
            PlayerData playerData = new PlayerData(player, this.api, this);
            this.cache.put(player, playerData);
        } catch (Exception e) {
            throw new FoException(e);
        }
    }

    @Override
    public void connectToServer(UUID uuid, String s) {


    }

    @Override
    public void sendMessage(UUID uuid, TextComponent textComponent) {

    }
}
