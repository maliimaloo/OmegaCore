package net.omegagames.core.bukkit.api.commands;

import lombok.AccessLevel;
import lombok.Getter;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.model.CreditLogMenu;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.persistanceapi.beans.credit.CreditBean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@AutoRegister
public final class TestCommand extends SimpleCommand {
    @Getter
    private static Boolean paramValue = false;

    @Getter (value = AccessLevel.PRIVATE)
    private static TestCommand instance = new TestCommand();

    public TestCommand() {
        super("test");
    }

    @Override
    protected void onCommand() {
        paramValue = !paramValue;
        super.tellSuccess("Param value is now " + paramValue);

        if (args.length > 1) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                super.tell("&cPlayer not found.");
                return;
            }

            PlayerData playerData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(player.getUniqueId());
            if (!playerData.isLoaded()) {
                super.tellError("Player data not loaded.");
                return;
            }

            new CreditLogMenu(ApiImplementation.getInstance(), playerData.getLogs()).displayTo(super.getPlayer());
        }
    }
}
