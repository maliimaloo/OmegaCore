package net.omegagames.core.persistanceapi.database;

import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import net.omegagames.core.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.database.SimpleDatabase;

import java.sql.*;

@SuppressWarnings("unused")
public class DatabaseManager extends SimpleDatabase {
    public static volatile DatabaseManager instance;

    private final String prefix = Settings.Database.PREFIX;

    public DatabaseManager(String paramUrl, String paramUsername, String paramPassword) {
        super.connect(paramUrl, paramUsername, paramPassword);
    }

    // Singleton generator
    public static DatabaseManager getInstance(String url, String name, String password) {
        if (DatabaseManager.instance == null) {
            DatabaseManager.instance = new DatabaseManager(url, name, password);
        }

        return DatabaseManager.getInstance();
    }

    public static DatabaseManager getInstance() {
        return DatabaseManager.instance;
    }


    @Override
    protected void onConnected() {
        Common.logNoPrefix(this.prefix + " Base de données connecter !");
        super.createTable(this.createTablePlayerBean());
    }

    public MResultSet request(String query, Object... objects) {
        PreparedStatement req_sql = null;
        ResultSet req = null;

        try {
            Connection connection = super.getConnection();
            Throwable var1 = null;

            req_sql = super.prepareStatement(query);
            int index = 1;
            int var9 = objects.length;

            for (Object obj : objects) {
                if (obj == null) {
                    req_sql.setNull(index, Types.NULL);
                } else {
                    req_sql.setObject(index, obj.toString());
                }
                ++index;
            }

            req = req_sql.executeQuery();
            MResultSet result_req = new MResultSet(req);
            req.close();
            req_sql.close();
            return result_req;
        } catch (SQLException var36) {
            Common.error(var36);
        } finally {
            try {
                if (req != null) {
                    req.close();
                }

                if (req_sql != null) {
                    req_sql.close();
                }
            } catch (SQLException var32) {
                Common.error(var32);
            }

        }

        return null;
    }

    public boolean transmission(String query, Object... objects) {
        try {
            Connection connection = super.getConnection();
            Throwable var1 = null;

            PreparedStatement req_sql = super.prepareStatement(query);
            int index = 1;
            int var8 = objects.length;

            for (Object obj : objects) {
                if (obj == null) {
                    req_sql.setNull(index, Types.NULL);
                } else {
                    req_sql.setObject(index, obj.toString());
                }
                ++index;
            }

            req_sql.executeUpdate();
            req_sql.close();
            return true;
        } catch (Throwable var21) {
            Common.error(var21);
            return false;
        }
    }

    public void asyncRequest(String query, Object... objects) {
        this.asyncRequest(query, null, objects);
    }

    public void asyncRequest(String query, Callback<MResultSet> callback, Object... objects) {
        Common.runAsync(() -> {
            PreparedStatement req_sql = null;
            ResultSet req = null;

            try {
                Connection connection = super.getConnection();
                Throwable var1 = null;

                req_sql = super.prepareStatement(query);
                int index = 1;
                int var10 = objects.length;

                for (Object obj : objects) {
                    req_sql.setObject(index, obj.toString());
                    ++index;
                }

                req = req_sql.executeQuery();
                MResultSet result_req = new MResultSet(req);
                req.close();
                req_sql.close();
                if (callback != null) {
                    Common.runLater(() -> callback.onSuccess(result_req));
                }
            } catch (SQLException var37) {
                Common.error(var37);
                if (callback != null) {
                    Common.runLater(() -> callback.onFailure(var37));
                }
            } finally {
                try {
                    if (req != null) {
                        req.close();
                    }

                    if (req_sql != null) {
                        req_sql.close();
                    }
                } catch (SQLException var33) {
                    Common.error(var33);
                }
            }
        });
    }

    public void asyncTransmission(String query, Object... objects) {
        this.asyncTransmission(query, null, objects);
    }

    public void asyncTransmission(String query, Callback<Integer> callback, Object... objects) {
        Common.runAsync(() -> {
            try {
                Connection connection = super.getConnection();
                Throwable var1 = null;

                PreparedStatement req_sql = super.prepareStatement(query);
                int index = 1;
                int var9 = objects.length;

                for (Object obj : objects) {
                    if (obj == null) {
                        req_sql.setNull(index, Types.NULL);
                    } else {
                        req_sql.setObject(index, obj.toString());
                    }
                    ++index;
                }

                int result = req_sql.executeUpdate();
                req_sql.close();
                if (callback != null) {
                    Common.runLater(() -> callback.onSuccess(result));
                }
            } catch (SQLException var22) {
                Common.error(var22);
                if (callback != null) {
                    Common.runLater(() -> callback.onFailure(var22));
                }
            }
        });
    }

    private TableCreator createTablePlayerBean() {
        return new TableCreator(PlayerBean.tableName)
                .addDefault(PlayerBean.fieldUniqueId, "binary(16)", null)
                .addDefault(PlayerBean.fieldName, "varchar(255)", null)
                .addDefault(PlayerBean.fieldNickname, "varchar(45)", null)
                .addDefault(PlayerBean.fieldOmega, "int(16)", "0")
                .addDefault(PlayerBean.fieldLastLogin, "timestamp", "CURRENT_TIMESTAMP")
                .addDefault(PlayerBean.fieldFirstLogin, "timestamp", "CURRENT_TIMESTAMP")
                .addDefault(PlayerBean.fieldLastIp, "varchar(15)", null)
                .setPrimaryColumn(PlayerBean.fieldUniqueId);
    }
}
