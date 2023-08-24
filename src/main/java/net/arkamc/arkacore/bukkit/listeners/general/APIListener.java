package net.arkamc.arkacore.bukkit.listeners.general;

import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.BukkitCore;
import org.bukkit.event.Listener;

abstract class APIListener implements Listener
{
    final BukkitCore plugin;
    final ApiImplementation api;

    APIListener(BukkitCore plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();
    }
}
