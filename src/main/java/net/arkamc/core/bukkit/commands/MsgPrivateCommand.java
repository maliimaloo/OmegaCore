package net.arkamc.core.bukkit.commands;

import lombok.Getter;
import net.arkamc.core.bukkit.ApiImplementation;
import net.arkamc.core.bukkit.settings.Settings;
import net.arkamc.core.bukkit.util.CommandUtils;
import net.arkamc.core.bukkit.util.LangUtils;
import net.arkamc.core.bukkit.util.Utils;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.Collections;

public final class MsgPrivateCommand extends SimpleCommand {
    @Getter
    private ApiImplementation api;
    @Getter
    private Player targetPlayer;

    public MsgPrivateCommand(ApiImplementation api) {
        super("chuchoter|repondre");
        this.api = api;
    }

    @Override
    protected void onCommand() {
        super.checkArgs(super.args.length > 0);

        final Param param = Param.find(super.args[0]);
        if (param == null) {
            super.returnInvalidArgs();
            return;
        }

        final String targetName = super.args.length > 1 ? super.args[1] : "";

        switch (param) {
            case HELP:
                this.handleHelpCommand();
                break;
            case CHUCHOTER:

                break;
            case REPONDRE:

                break;
        }
    }

    /**
     * Enumeration pour les différentes sous-commandes de la commande /acredit.
     */
    private enum Param {
        CHUCHOTER ("chuchoter", "whisper", "whisp", "msg", "m"),
        REPONDRE ("repondre", "reply", "r"),
        HELP ("help", "h");

        private final String label;
        private final String[] aliases;

        Param(final String paramLabel, final String... paramAliases) {
            this.label = paramLabel;
            this.aliases = paramAliases;
        }

        /**
         * Trouve un paramètre à partir de l'argument donné.
         *
         * @param paramArgument L'argument de la sous-commande.
         * @return Le Param correspondant ou null si non trouvé.
         */
        @Nullable
        private static Param find(String paramArgument) {
            String finalParamArgument = paramArgument.toLowerCase();

            return java.util.Arrays.stream(values())
                    .filter(param -> param.label.equals(finalParamArgument) || (param.aliases != null && java.util.Arrays.asList(param.aliases).contains(finalParamArgument)))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void handleHelpCommand() {
        if (!Utils.hasPermission(super.getSender(), "arkacore.msg.help") && !Utils.hasPermission(super.getSender(), "arkacore.msg.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.msg.help")));
            return;
        }

        final SimpleComponent commandComponent = SimpleComponent.empty();
        commandComponent
                .append(Common.chatLine())
                .append("\n" + Settings.PLUGIN_PREFIX + " &ccommandes manager disponible")
                .append("\n" + Settings.PLUGIN_PREFIX)
                .append("\n" + Settings.PLUGIN_PREFIX + "&6[] &f- Arguments Requis")
                .append("\n" + Settings.PLUGIN_PREFIX + "&6<> &f- Arguments Optionnels")
                .append("\n" + Settings.PLUGIN_PREFIX);

        if (Utils.hasPermission(super.getSender(), "arkacore.msg.chuchoter") || Utils.hasPermission(super.getSender(), "arkacore.msg.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "chuchoter", "<joueur> <message>", Collections.singletonList("&f- &cEnvoyer &fun message privée"), "arkacore.msg.chuchoter", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.msg.repondre") || Utils.hasPermission(super.getSender(), "arkacore.msg.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "repondre", "<message>", Collections.singletonList("&f- &cRépondre &fà un message privée"), "arkacore.msg.repondre", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        commandComponent
                .append("\n" + Settings.PLUGIN_PREFIX)
                .append("\n" + Settings.PLUGIN_PREFIX + "&f&nSurvolez la commande pour plus d'informations.")
                .append("\n&f" + Common.chatLine())
                .send(super.getSender());
    }

    private void handleChuchoterCommand(String targetName, String message) {
        if (!Utils.hasPermission(super.getSender(), "arkacore.msg.chuchoter") && !Utils.hasPermission(super.getSender(), "arkacore.msg.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.msg.chuchoter")));
            return;
        }

        final boolean isEnabled = Settings.Whisper.ENABLED;
        if (!isEnabled) {
            super.tellError("Cette commande est actuellement désactivée.");
            return;
        }


    }
}
