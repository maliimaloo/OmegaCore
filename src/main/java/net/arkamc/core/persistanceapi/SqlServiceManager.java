package net.arkamc.core.persistanceapi;

import net.arkamc.core.bukkit.util.model.Callback;
import net.arkamc.core.persistanceapi.beans.players.PlayerBean;
import net.arkamc.core.persistanceapi.database.DatabaseManager;
import net.arkamc.core.persistanceapi.datamanager.PlayerManager;

import java.util.UUID;

@SuppressWarnings("unused")
public class SqlServiceManager {
    private final DatabaseManager databaseManager;
    private final PlayerManager playerManager;

    public SqlServiceManager(String paramUrl, String paramUsername, String paramPassword) {
        this.databaseManager = DatabaseManager.getInstance(paramUrl, paramUsername, paramPassword);

        this.playerManager = new PlayerManager(this);
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    /*============================================
      Part of player manager
    ============================================*/

    /**
     * Récupère le joueur par son UUID
     *
     * @param uuid L'UUID du joueur
     * @return Le PlayerBean du joueur
     */
    public synchronized PlayerBean getPlayer(UUID uuid) {
        // Get the PlayerBean
        return this.playerManager.getPlayer(uuid, null);
    }

    /**
     * Récupère le joueur par son UUID dans un callback
     *
     * @param uuid L'UUID du joueur
     * @param callback Le callback de PlayerBean
     */
    public synchronized void getPlayer(UUID uuid, Callback<PlayerBean> callback) {
        // Get the PlayerBean
        this.playerManager.getPlayer(uuid, callback);
    }

    /**
     * Update les données d'un joueur à partir de son PlayerBean
     *
     * @param playerBean Le PlayerBean du joueur
     */
    public synchronized void updatePlayer(PlayerBean playerBean) {
        this.updatePlayer(playerBean, null);
    }

    /**
     * Update les données d'un joueur à partir de son PlayerBean
     *
     * @param playerBean Le PlayerBean du joueur
     * @param callback La réponse de l'update (1 si réussi, 0 sinon)
     */
    public synchronized void updatePlayer(PlayerBean playerBean, Callback<Integer> callback) {
        this.playerManager.updatePlayer(playerBean, callback);
    }

    /**
     * Réinitialisation des logs de transaction d'un joueur à partir de son PlayerBean
     *
     * @param playerBean Le PlayerBean du joueur
     */
    public synchronized void resetPlayerLogs(PlayerBean playerBean) {
        this.resetPlayerLogs(playerBean, null);
    }

    /**
     * Réinitialisation des logs de transaction d'un joueur à partir de son PlayerBean
     *
     * @param playerBean Le PlayerBean du joueur
     * @param callback   La réponse de l'update (return Nombre de lignes affecter)
     */
    public synchronized void resetPlayerLogs(PlayerBean playerBean, Callback<Integer> callback) {
        this.playerManager.resetPlayerLogs(playerBean, callback);
    }

    /**
     * Crée les données d'un joueur dans la base de données
     *
     * @param playerBean Le PlayerBean du joueur
     * @return Si la création a réussi {@code true} sinon {@code false
     */
    public synchronized boolean createPlayer(PlayerBean playerBean) {
        return this.playerManager.createPlayer(playerBean);
    }
}
