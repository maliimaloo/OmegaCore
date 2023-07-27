package net.omegagames.core.bukkit.api.listeners.pubsub;

import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.settings.Settings;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;

import java.util.UUID;

public class GlobalUpdateListener implements IPacketsReceiver {
    public GlobalUpdateListener() {}

    @Override
    public void receive(String channel, String packet) {
        if (channel.equals("online_status_check")) {
            UUID player = UUID.fromString(SerializeUtil.deserialize(SerializeUtil.Mode.JSON, String.class, packet));
            Common.logNoPrefix(Settings.Jedis.PREFIX + " Reception du packet de mise à jour de l'état de connexion de " + player + " !");
            if (Bukkit.getPlayer(player) != null) {
                Common.logNoPrefix(Settings.Jedis.PREFIX + " Mise à jour de l'état de connexion de " + player + " !");
                GlobalJoinListener.getOnlineStatus().put(player, true);
            }
        }
    }
}
