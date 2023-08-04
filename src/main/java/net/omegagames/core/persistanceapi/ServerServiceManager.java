package net.omegagames.core.persistanceapi;

import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import net.omegagames.core.persistanceapi.database.DatabaseManager;
import net.omegagames.core.persistanceapi.datamanager.PlayerManager;

import java.util.UUID;

@SuppressWarnings("unused")
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
    public synchronized PlayerBean getPlayer(UUID uuid) {
        // Get the PlayerBean
        return this.playerManager.getPlayer(uuid);
    }

    // Get the player by username
    public synchronized PlayerBean getPlayer(String username) {
        // Get the PlayerBean
        return this.playerManager.getPlayerByName(username);
    }

    // Update the player
    public synchronized void updatePlayer(PlayerBean player) {
        this.playerManager.updatePlayer(player);
    }

    // Create the player
    public synchronized boolean createPlayer(PlayerBean player) {
        // Create the player
        return this.playerManager.createPlayer(player);
    }
}