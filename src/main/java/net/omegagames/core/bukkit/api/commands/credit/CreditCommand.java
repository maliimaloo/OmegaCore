package net.omegagames.core.bukkit.api.commands.credit;

import lombok.AccessLevel;
import lombok.Getter;
import net.omegagames.core.bukkit.BukkitCore;
import net.omegagames.core.bukkit.api.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.model.SimpleComponent;

import java.util.Collections;
import java.util.List;

@AutoRegister
public final class CreditCommand extends SimpleCommandGroup {
    private final String prefix = Settings.PLUGIN_PREFIX + " ";

    @Getter(value = AccessLevel.PRIVATE)
    private static final CreditCommand instance = new CreditCommand();

    public CreditCommand() {
        super("credit");
    }

    @Override
    protected List<SimpleComponent> getNoParamsHeader() {
        final String paramAuthor = BukkitCore.getAuthor();
        final String paramVersion = BukkitCore.getVersion();

        SimpleComponent helpComponent = SimpleComponent.empty();
        helpComponent
                .append(Common.chatLine())
                .append("\n" + this.prefix + "En cours d'exécution &cArkaCore &f" + paramVersion)
                .append("\n" + this.prefix + "Crée par &c&n" + paramAuthor + "&c")
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&f&nClique pour voir les sous_commandes disponible.")
                .onHover("&a&nClique pour voir les sous commandes.").onClickRunCmd("/" + this.getLabel() + " ?")
                .append("\n&f" + Common.chatLine());

        return Collections.singletonList(helpComponent);

    }

    @Override
    protected void registerSubcommands() {
        super.registerSubcommand(new CreditHelpCommand());
        super.registerSubcommand(new CreditManagerCommand());
    }
}
