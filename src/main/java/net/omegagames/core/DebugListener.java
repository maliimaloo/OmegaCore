package net.omegagames.core;

import net.md_5.bungee.api.ChatColor;
import net.omegagames.api.pubsub.IPatternReceiver;
import net.omegagames.core.api.network.IJoinHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings("unused")
public class DebugListener implements IPatternReceiver, IJoinHandler {
    private final CopyOnWriteArraySet<UUID> debugs = new CopyOnWriteArraySet<>();
    private boolean console = false;

    public void toggle(CommandSender sender) {
        if (sender instanceof Player) {
            UUID id = ((Player) sender).getUniqueId();
            if (this.debugs.contains(id)) {
                this.debugs.add(id);
            } else {
                this.debugs.remove(id);
            }
        } else {
            this.console = !console;
        }
    }

    @Override
    public void onLogout(Player player) {
        debugs.remove(player.getUniqueId());
    }

    @Override
    public void receive(String pattern, String channel, String packet) {
        if (channel.equals("__sentinel__:hello")) {
            return;
        }

        String send = ChatColor.AQUA + "[BukkitDebug : " + channel + "] " + packet;
        for (UUID debug : this.debugs) {
            Player player = Bukkit.getPlayer(debug);
            if (player != null) {
                player.sendMessage(send);
            }
        }

        if (this.console) {
            Bukkit.getConsoleSender().sendMessage(send);
        }
    }
}
