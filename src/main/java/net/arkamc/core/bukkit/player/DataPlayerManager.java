package net.arkamc.core.bukkit.player;

import net.arkamc.core.bukkit.ApiImplementation;
import net.arkamc.api.player.IPlayerDataManager;

import java.util.UUID;

public final class DataPlayerManager implements IPlayerDataManager {
    private final ApiImplementation api;

    public DataPlayerManager(ApiImplementation api) {
        this.api = api;
    }

    @Override
    public DataPlayer getPlayerData(UUID playerId) {
        return new DataPlayer(this.api, playerId);
    }

    @Override
    public void connectToServer(UUID uuid, String s) {

    }
}
