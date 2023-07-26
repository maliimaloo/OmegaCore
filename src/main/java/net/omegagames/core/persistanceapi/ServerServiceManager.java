package net.omegagames.core.persistanceapi;

import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import net.omegagames.core.persistanceapi.database.DatabaseManager;
import net.omegagames.core.persistanceapi.datamanager.PlayerManager;

import java.util.UUID;

public class ServerServiceManager {
    private final DatabaseManager databaseManager;
    private final PlayerManager playerManager;

    public ServerServiceManager(String paramUrl, String paramUsername, String paramPassword) {
        this.databaseManager = DatabaseManager.getInstance(paramUrl, paramUsername, paramPassword);
        this.playerManager = new PlayerManager();
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    /*============================================
      Part of player manager
    ============================================*/

    // Get the player by UUID
    public synchronized PlayerBean getPlayer(UUID uuid, PlayerBean player) {
        // Get the PlayerBean
        return this.playerManager.getPlayer(uuid, player);
    }

    // Update the player
    public synchronized void updatePlayer(PlayerBean player) {
        this.playerManager.updatePlayer(player);
    }

    // Create the player
    public synchronized void createPlayer(PlayerBean player) {
        // Create the player
        this.playerManager.createPlayer(player);
    }
}
