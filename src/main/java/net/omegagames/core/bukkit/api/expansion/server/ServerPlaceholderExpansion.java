package net.omegagames.core.bukkit.api.expansion.server;

import net.omegagames.core.bukkit.ApiImplementation;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.remain.Remain;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ServerPlaceholderExpansion {
    protected ApiImplementation api;

    public ServerPlaceholderExpansion(ApiImplementation api) {
        this.api = api;
    }

    public void register() {
        HookManager.addPlaceholder("server_name", this::getServerName);
        HookManager.addPlaceholder("server_date", this::getCurrentDate);
        HookManager.addPlaceholder("server_player_online", this::getPlayerOnline);
    }

    private String getServerName(Player player) {
        final String paramServerName = this.api.getServerName();
        if (Valid.isNullOrEmpty(paramServerName)) {
            return Messenger.getErrorPrefix();
        }

        return paramServerName;
    }

    private String getCurrentDate(Player player) {
        final String paramDateFormat = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDate().toString();
        if (Valid.isNullOrEmpty(paramDateFormat)) {
            return Messenger.getErrorPrefix();
        }

        return paramDateFormat;
    }

    private String getPlayerOnline(Player player) {
        final String paramPlayerCount = String.valueOf(Remain.getOnlinePlayers().size());
        if (Valid.isNullOrEmpty(paramPlayerCount)) {
            return Messenger.getErrorPrefix();
        }

        return paramPlayerCount;
    }
}
