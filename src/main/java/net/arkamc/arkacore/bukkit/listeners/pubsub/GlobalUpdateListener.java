package net.arkamc.arkacore.bukkit.listeners.pubsub;

import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.BukkitCore;
import net.arkamc.arkacore.bukkit.listeners.general.GlobalJoinListener;
import net.arkamc.arkacore.bukkit.util.pubsub.IPacketsReceiver;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GlobalUpdateListener implements IPacketsReceiver {
    private final ApiImplementation api = BukkitCore.getInstance().getApi();

    @Override
    public void receive(String channel, String packet) {
        final String[] parts = packet.split(":");
        final String serverName = parts[0];
        final UUID uniqueId = UUID.fromString(parts[1]);

        switch (channel) {
            case "arkacore:player:online_status_check":
                if (Bukkit.getPlayer(uniqueId) != null && !Objects.equals(this.api.getServerName(), serverName)) {
                    this.api.getPubSub().send("arkacore:player:online_status_response", uniqueId.toString());
                }
                break;

            case "arkacore:player:online_status_response":
                final CompletableFuture<Boolean> paramFuture = GlobalJoinListener.getOnlineStatus().get(uniqueId);
                if (paramFuture != null) {
                    paramFuture.complete(true);
                }
                break;
        }
    }
}
