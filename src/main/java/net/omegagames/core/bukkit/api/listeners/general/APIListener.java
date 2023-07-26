package net.omegagames.core.bukkit.api.listeners.general;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.PluginCore;
import org.bukkit.event.Listener;

abstract class APIListener implements Listener
{
    final PluginCore plugin;
    final ApiImplementation api;

    APIListener(PluginCore plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
    }
}
