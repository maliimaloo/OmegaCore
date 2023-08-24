package net.arkamc.arkacore.bukkit.manager;

import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.data.PlayerData;

import java.util.UUID;

public final class PlayerDataManager{
    private final ApiImplementation api;

    public PlayerDataManager(ApiImplementation api) {
        this.api = api;
    }

    public PlayerData getPlayerData(UUID playerId) {
        return new PlayerData(this.api, playerId);
    }
}
