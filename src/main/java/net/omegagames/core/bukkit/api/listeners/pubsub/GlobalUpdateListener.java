package net.omegagames.core.bukkit.api.listeners.pubsub;

import io.netty.util.concurrent.CompleteFuture;
import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.api.pubsub.PendingMessage;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import org.bukkit.Bukkit;
import org.mineacademy.fo.SerializeUtil;

import java.util.UUID;

public class GlobalUpdateListener implements IPacketsReceiver {
    private final ApiImplementation api;

    public GlobalUpdateListener(ApiImplementation api) {
        this.api = api;
    }

    @Override
    public void receive(String channel, String packet) {
        if (channel.equals("online_status_check")) {
            final UUID player = UUID.fromString(SerializeUtil.deserialize(SerializeUtil.Mode.JSON, String.class, packet));
            if (Bukkit.getPlayer(player) != null) {
                this.api.getPubSub().send(new PendingMessage("omegacore:player:online_status_response", player.toString()));
            }
        }

        if (channel.equals("omegacore:player:online_status_response")) {
            final UUID player = UUID.fromString(packet);
            GlobalJoinListener.getOnlineStatus().get(player).complete(true);
        }
    }
}
