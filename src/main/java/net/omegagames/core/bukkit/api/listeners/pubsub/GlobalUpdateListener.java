package net.omegagames.core.bukkit.api.listeners.pubsub;

import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.listeners.general.GlobalJoinListener;
import net.omegagames.core.bukkit.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;

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

            case "__keyevent@0__:expired" -> {
                if (packet.startsWith("omegacore:account:")) {
                    UUID playerId = UUID.fromString(packet.split(":")[2]);
                    PlayerData paramPlayerData = this.api.getPlayerManager().getPlayerData(playerId);
                    this.api.getServerServiceManager().updatePlayer(paramPlayerData.getPlayerBean());
                }
            }
        }
    }
}
