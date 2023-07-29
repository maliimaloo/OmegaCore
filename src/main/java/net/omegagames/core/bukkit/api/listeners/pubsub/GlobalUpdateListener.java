package net.omegagames.core.bukkit.api.listeners.pubsub;

import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.api.pubsub.PendingMessage;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.persistanceapi.database.Callback;
import net.omegagames.core.bukkit.persistanceapi.database.MResultSet;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GlobalUpdateListener implements IPacketsReceiver {
    private final ApiImplementation api;

    public GlobalUpdateListener(ApiImplementation api) {
        this.api = api;
    }

    @Override
    public void receive(String channel, String packet) {
        switch (channel) {
            case "omegacore:player:online_status_check" -> {
                final String[] params = packet.split(":");
                final String paramServerName = params[0];
                final UUID player = UUID.fromString(params[1]);

                if (Bukkit.getPlayer(player) != null && !Objects.equals(this.api.getServerName(), paramServerName)) {
                    this.api.getPubSub().send("omegacore:player:online_status_response", player.toString());
                }
            }

            case "omegacore:player:online_status_response" -> {
                final String[] params = packet.split(":");
                final UUID player = UUID.fromString(params[1]);

                final CompletableFuture<Boolean> paramFuture = GlobalJoinListener.getOnlineStatus().get(player);
                if (paramFuture != null) {
                    paramFuture.complete(true);
                }
            }
        }
    }
}
