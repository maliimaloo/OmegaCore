package net.arkamc.core.bukkit;

import net.md_5.bungee.api.ChatColor;
import net.arkamc.api.pubsub.IPatternReceiver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings("unused")
public class DebugListener implements IPatternReceiver {
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
