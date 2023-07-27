package net.omegagames.core.bukkit.api.listeners.general;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.BukkitCore;
import org.bukkit.event.Listener;

abstract class APIListener implements Listener
{
    final BukkitCore plugin;
    final ApiImplementation api;

    APIListener(BukkitCore plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
    }
}
