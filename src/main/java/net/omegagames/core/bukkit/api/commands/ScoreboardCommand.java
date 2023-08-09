package net.omegagames.core.bukkit.api.commands;

import lombok.AccessLevel;
import lombok.Getter;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.scoreboard.ScoreboardData;
import net.omegagames.core.bukkit.api.settings.Settings;
import net.omegagames.core.bukkit.api.util.CommandUtils;
import net.omegagames.core.bukkit.api.util.LangUtils;
import net.omegagames.core.bukkit.api.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

//AutoRegister
public final class ScoreboardCommand extends SimpleCommand {
    private ScoreboardCommand(String label) {
        super(label);
    }

    @Override
    protected void onCommand() {

    }
    /*private final String prefix = Settings.PLUGIN_PREFIX + " ";

    @Getter (value = AccessLevel.PRIVATE)
    private static final ScoreboardCommand instance = new ScoreboardCommand();

    /**
     * Constructeur de la classe `ScoreboardManagerCommand`
     *
    public ScoreboardCommand() {
        super("ascoreboard");
        super.setAutoHandleHelp(false);
    }

    @Override
    protected void onCommand() {
        // Vérifier si les arguments sont corrects
        if (super.args.length == 0) {
            super.returnInvalidArgs();
            return;
        }

        // Récupérer l'action demandée (give, take, show)
        final Param param = Param.find(super.args[0]);
        if (param == null) {
            super.returnInvalidArgs();
            return;
        }

        // Récupérer le nom du joueur cible et l'uniqueId du scoreboard
        final String targetName = super.args.length > 1 ? super.args[1] : "";
        final String scoreboardUniqueId = super.args.length > 2 ? super.args[2] : "";

        switch (param) {
            case HELP:
                this.handleHelpCommand();
                break;

            case GIVE:
                this.handleShowCommand(targetName, scoreboardUniqueId);
                break;

            case TAKE:
                this.handleHideCommand(targetName, scoreboardUniqueId);
                break;
        }
    }

    /**
     * Enum pour représenter les différentes actions possibles pour la commande /credit
     *
    private enum Param {
        HELP("help", "?"),
        GIVE("show", "s"),
        TAKE("hide", "h");

        private final String label;
        private final String[] aliases;

        Param(final String paramLabel, final String... paramAliases) {
            this.label = paramLabel;
            this.aliases = paramAliases;
        }

        /**
         * Trouver l'action correspondant au paramètre donné
         *
        @Nullable
        private static Param find(String paramArgument) {
            String finalParamArgument = paramArgument.toLowerCase();

            return Arrays.stream(values())
                    .filter(param -> param.label.toLowerCase().equals(finalParamArgument) || param.aliases != null && Arrays.asList(param.aliases).contains(finalParamArgument))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private void handleShowCommand(String targetName, String scoreboardUniqueId) {
        if (super.args.length < 3) {
            super.returnInvalidArgs();
            return;
        }

        if (!Utils.hasPermission(super.getSender(), "arkacore.scoreboard.show") && !Utils.hasPermission(super.getSender(), "arkacore.scoreboard.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("permission", "arkacore.scoreboard.show")));
            return;
        }

        Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        if (Objects.isNull(paramTargetPlayer)) {
            super.tellError("&cLe joueur &a" + targetName + " &cn'est pas connecté.");
            return;
        }

        ScoreboardData paramScoreboardData = ApiImplementation.getInstance().getScoreboardManager().getScoreboard(scoreboardUniqueId);
        if (Objects.isNull(paramScoreboardData)) {
            super.tellError("&cLe scoreboard avec l'uniqueId &a" + scoreboardUniqueId + " &cn'existe pas.");
            return;
        }

        if (paramScoreboardData.isViewing(paramTargetPlayer)) {
            super.tellError("&cLe joueur &a" + targetName + " &ca déjà le scoreboard &a" + scoreboardUniqueId + " &cd'affiché.");
            return;
        }

        paramScoreboardData.show(paramTargetPlayer);
        super.tellSuccess("&fLe scoreboard &a" + scoreboardUniqueId + " &fa bien été affiché au joueur &a" + targetName + ".");
    }

    private void handleHideCommand(String targetName, String scoreboardUniqueId) {
        if (super.args.length < 3) {
            super.returnInvalidArgs();
            return;
        }

        if (!Utils.hasPermission(super.getSender(), "arkacore.scoreboard.hide") && !Utils.hasPermission(super.getSender(), "arkacore.scoreboard.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("permission", "arkacore.scoreboard.hide")));
            return;
        }

        Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        if (Objects.isNull(paramTargetPlayer)) {
            super.tellError("&cLe joueur &a" + targetName + " &cn'est pas connecté.");
            return;
        }

        ScoreboardData paramScoreboardData = ApiImplementation.getInstance().getScoreboardManager().getScoreboard(scoreboardUniqueId);
        if (Objects.isNull(paramScoreboardData)) {
            super.tellError("&cLe scoreboard avec l'uniqueId &a" + scoreboardUniqueId + " &cn'existe pas.");
            return;
        }

        if (!paramScoreboardData.isViewing(paramTargetPlayer)) {
            super.tellError("&cLe joueur &a" + targetName + " &cn'a pas le scoreboard &a" + scoreboardUniqueId + " &cd'affiché.");
            return;
        }

        paramScoreboardData.hide(paramTargetPlayer);
        super.tellSuccess("&fLe scoreboard &a" + scoreboardUniqueId + " &fa bien été caché au joueur &a" + targetName + "&f.");
    }

    private void handleHelpCommand() {
        if (!Utils.hasPermission(super.getSender(), "arkacore.scoreboard.help") && !Utils.hasPermission(super.getSender(), "arkacore.scoreboard.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", "arkacore.scoreboard.help")));
            return;
        }

        final SimpleComponent commandComponent = SimpleComponent.empty();
        commandComponent
                .append(Common.chatLine())
                .append("\n" + this.prefix + "&ccommandes scoreboard disponible")
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&6[] &f- Arguments Requis")
                .append("\n" + this.prefix + "&6<> &f- Arguments Optionnels")
                .append("\n" + this.prefix);

        if (Utils.hasPermission(super.getSender(), "arkacore.scoreboard.show") || Utils.hasPermission(super.getSender(), "arkacore.scoreboard.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "show", "<joueur> <scoreboard_unique_id>", Collections.singletonList("\n &f- &cAfficher &fun scoreboard à un joueur"), "arkacore.scoreboard.show|arkacore.scoreboard.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.scoreboard.hide") || Utils.hasPermission(super.getSender(), "arkacore.scoreboard.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "hide", "<joueur> <scoreboard_unique_id>", Collections.singletonList("\n &f- &cCacher &fun scoreboard à un joueur"), "arkacore.scoreboard.show|arkacore.scoreboard.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        commandComponent
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&f&nSurvolez la commande pour plus d'informations.")
                .append("\n&f" + Common.chatLine())
                .send(super.getSender());
    }*/
}
