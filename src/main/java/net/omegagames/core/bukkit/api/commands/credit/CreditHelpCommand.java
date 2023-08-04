package net.omegagames.core.bukkit.api.commands.credit;

import net.omegagames.core.bukkit.api.settings.Settings;
import net.omegagames.core.bukkit.api.util.CommandUtils;
import net.omegagames.core.bukkit.api.util.Utils;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.model.SimpleComponent;

import java.util.Collections;

public class CreditHelpCommand extends SimpleSubCommand {
    private final String prefix = Settings.PLUGIN_PREFIX + " ";

    protected CreditHelpCommand() {
        super("?|help");
        super.setPermission("arkacore.credit.help");
    }

    @Override
    protected void onCommand() {
        final SimpleComponent commandComponent = SimpleComponent.empty();
        commandComponent
                .append(Common.chatLine())
                .append("\n" + this.prefix + "&ccommandes manager disponible")
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&6[] &f- Arguments Requis")
                .append("\n" + this.prefix + "&6<> &f- Arguments Optionnels")
                .append("\n" + this.prefix);

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.give") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, super.getCurrentLabel(), super.getPlayer(), "give", "<joueur> <montant>", Collections.singletonList("\n &f- &cDonner &fdes crédits"), "arkacore.credit.give|arkacore.credit.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, super.getCurrentLabel(), super.getPlayer(), "take", "<joueur> <montant>", Collections.singletonList("\n &f- &cRetirer &fdes crédits"), "arkacore.credit.take|arkacore.credit.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.show") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, super.getCurrentLabel(), super.getPlayer(), "show", "<joueur>", Collections.singletonList("\n &f- &cVoir &fles crédits"), "arkacore.credit.show|arkacore.credit.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        commandComponent
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&f&nSurvolez la commande pour plus d'informations.")
                .append("\n&f" + Common.chatLine())
                .send(super.getSender());
    }
}
