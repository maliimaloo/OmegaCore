package net.omegagames.core.bukkit.api.listeners.pubsub;

import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import org.bukkit.Bukkit;

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
            case "omegacore:player:online_status_check":
                final String[] params1 = packet.split(":");
                final String paramServerName1 = params1[0];
                final UUID player1 = UUID.fromString(params1[1]);

                if (Bukkit.getPlayer(player1) != null && !Objects.equals(this.api.getServerName(), paramServerName1)) {
                    this.api.getPubSub().send("omegacore:player:online_status_response", player1.toString());
                }
                break;

            case "omegacore:player:online_status_response":
                final String[] params2 = packet.split(":");
                final UUID player2 = UUID.fromString(params2[1]);

                final CompletableFuture<Boolean> paramFuture = GlobalJoinListener.getOnlineStatus().get(player2);
                if (paramFuture != null) {
                    paramFuture.complete(true);
                }
                break;
        }
    }
}
