package net.omegagames.core.bukkit.api.player;

import net.omegagames.api.player.IPlayerDataManager;
import net.omegagames.core.bukkit.ApiImplementation;

import java.util.UUID;

public record PlayerDataManager (ApiImplementation api) implements IPlayerDataManager {
    public PlayerDataManager(ApiImplementation api) {
        this.api = api;
    }

    @Override
    public PlayerData getPlayerData(UUID playerId) {
        return new PlayerData(this.api, playerId);
    }

    @Override
    public void connectToServer(UUID uuid, String s) {

    }
}
