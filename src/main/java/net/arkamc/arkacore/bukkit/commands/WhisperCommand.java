package net.arkamc.arkacore.bukkit.commands;

import lombok.Getter;
import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.data.PlayerData;
import net.arkamc.arkacore.bukkit.data.WhisperData;
import net.arkamc.arkacore.bukkit.settings.Settings;
import net.arkamc.arkacore.bukkit.util.CommandUtils;
import net.arkamc.arkacore.bukkit.util.LangUtils;
import net.arkamc.arkacore.bukkit.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

public final class WhisperCommand extends SimpleCommand {
    @Getter
    private ApiImplementation api;
    @Getter
    private Player targetPlayer;

    public WhisperCommand(ApiImplementation api) {
        super("chuchoter|repondre");
        this.api = api;
    }

    @Override
    protected void onCommand() {
        super.checkConsole();
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
                this.handleChuchoterCommand(targetName, Common.joinRange(2, super.args));
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

    private void handleChuchoterCommand(String receiverPlayerName, String message) {
        if (!Utils.hasPermission(super.getSender(), "arkacore.msg.chuchoter") && !Utils.hasPermission(super.getSender(), "arkacore.msg.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.msg.chuchoter")));
            return;
        }

        final boolean isEnabled = Settings.Whisper.ENABLED;
        if (!isEnabled) {
            super.tellError("Cette commande est actuellement désactivée.");
            return;
        }

        final UUID senderUniqueId = super.getPlayer().getUniqueId();
        final PlayerData senderData = this.api.getPlayerManager().getPlayerData(senderUniqueId);
        if (!senderData.isWhisper()) {
            super.tellError("&cVous avez actuellement la messagerie privée de désactiver.");
            return;
        }

        final Player senderPlayer = super.getPlayer();
        final Player receiverPlayer = Bukkit.getPlayer(receiverPlayerName);
        if (receiverPlayer == null) {

        } else {
            WhisperData whisperData = new WhisperData(senderPlayer.getName(), receiverPlayer.getName(), message);
            whisperData.sendWhisper(senderPlayer);
            whisperData.sendWhisper(receiverPlayer);
            //this.chat(senderPlayer, Settings.Whisper.FORMAT_SENDER, message);
            //this.chat(receiverPlayer, Settings.Whisper.FORMAT_RECEIVING, message);
        }
    }
}
