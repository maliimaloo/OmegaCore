package net.omegagames.core.bukkit.api.commands;

import lombok.AccessLevel;
import lombok.Getter;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
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
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@AutoRegister
public final class CreditCommand extends SimpleCommand {
    private final String prefix = Settings.PLUGIN_PREFIX + " ";

    @Getter (value = AccessLevel.PRIVATE)
    private static final CreditCommand instance = new CreditCommand();

    public CreditCommand() {
        super("acredit");
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

        final String targetName = super.args.length > 1 ? super.args[1] : "";
        final String amount = super.args.length > 2 ? super.args[2] : "";
        final String reason = super.args.length > 3 ? Common.joinRange(3, super.args) : "";

        switch (param) {
            case HELP -> this.handleHelpCommand();

            case GIVE -> this.handleGiveCommand(targetName, reason, amount);

            case TAKE -> this.handleTakeCommand(targetName, reason, amount);

            case SHOW -> this.handleShowCommand(targetName);
        }
    }

    /**
     * Enum pour représenter les différentes actions possibles pour la commande /credit
     */
    private enum Param {
        HELP("help", "?"),
        GIVE("give", "g"),
        TAKE("take", "t"),
        SHOW("show", "s");

        private final String label;
        private final String[] aliases;

        Param(final String paramLabel, final String... paramAliases) {
            this.label = paramLabel;
            this.aliases = paramAliases;
        }

        /**
         * Trouver l'action correspondant au paramètre donné
         */
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

    /**
     * Méthode pour traiter la commande /credit give
     */
    private void handleGiveCommand(String targetName, String reason, String amountArgs) {
        // Vérifier les permissions du joueur pour effectuer cette action
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.give") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("permission", "arkacore.credit.give")));
            return;
        }

        // Récupérer le joueur cible et le montant spécifié
        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        final int amount = this.convertAmountToInt(amountArgs);
        if (paramTargetPlayer == null) {
            // Si le joueur n'est pas en ligne, récupérer son UUID de manière asynchrone
            Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    // Si le joueur n'existe pas, afficher un message d'erreur
                    if (uniqueId == null) {
                        tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        return;
                    }

                    // Créditer le joueur cible avec le montant spécifié
                    creditPlayer(uniqueId, amount, reason);
                }

                @Override
                public void onFailure() {
                    tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            // Si le joueur est en ligne, créditer directement le joueur
            creditPlayer(paramTargetPlayer.getUniqueId(), amount, reason);
        }
    }

    /**
     * Méthode pour traiter la commande /credit take
     */
    private void handleTakeCommand(String targetName, String reason, String amountArgs) {
        // Vérifier les permissions du joueur pour effectuer cette action
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", super.getPermission())));
            return;
        }

        // Récupérer le joueur cible et le montant spécifié
        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        final int paramAmount = convertAmountToInt(amountArgs);

        // Vérifier si le joueur cible est en ligne
        if (paramTargetPlayer == null) {
            // Si le joueur n'est pas en ligne, récupérer son UUID de manière asynchrone
            Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    // Si le joueur n'existe pas, afficher un message d'erreur
                    if (uniqueId == null) {
                        tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        return;
                    }

                    // Retirer le montant spécifié du crédit du joueur cible
                    withdrawPlayer(uniqueId, paramAmount, reason);
                }

                @Override
                public void onFailure() {
                    tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            // Si le joueur est en ligne, retirer directement le montant du crédit du joueur
            withdrawPlayer(paramTargetPlayer.getUniqueId(), paramAmount, reason);
        }
    }

    /**
     * Méthode pour traiter la commande /credit show
     */
    private void handleShowCommand(String targetName) {
        // Vérifier les permissions du joueur pour effectuer cette action
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.show.others") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", super.getPermission())));
            return;
        }

        // Récupérer le joueur cible
        Player paramTargetPlayer = Bukkit.getPlayer(targetName);

        // Vérifier si le joueur cible est en ligne
        if (paramTargetPlayer == null) {
            // Si le joueur n'est pas en ligne, récupérer son UUID de manière asynchrone
            Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    // Si le joueur n'existe pas, afficher un message d'erreur
                    if (uniqueId == null) {
                        tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        return;
                    }

                    // Récupérer les données du joueur cible et afficher son crédit
                    PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(uniqueId);
                    if (!paramTargetData.isLoaded()) {
                        tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
                        return;
                    }

                    tellSuccess("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getOmegaCoins() + " &aoméga.");
                }

                @Override
                public void onFailure() {
                    tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            // Si le joueur est en ligne, récupérer directement les données du joueur et afficher son crédit
            PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramTargetPlayer.getUniqueId());
            if (!paramTargetData.isLoaded()) {
                tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
                return;
            }

            tellSuccess("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getOmegaCoins() + " &aoméga.");
        }
    }

    private void handleHelpCommand() {
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.help") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", "arkacore.scoreboard.help")));
            return;
        }

        final SimpleComponent commandComponent = SimpleComponent.empty();
        commandComponent
                .append(Common.chatLine())
                .append("\n" + this.prefix + "&ccommandes manager disponible")
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&6[] &f- Arguments Requis")
                .append("\n" + this.prefix + "&6<> &f- Arguments Optionnels")
                .append("\n" + this.prefix);

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.give") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "give", "<joueur> <montant>", Collections.singletonList("\n &f- &cDonner &fdes crédits"), "arkacore.credit.give|arkacore.credit.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "take", "<joueur> <montant>", Collections.singletonList("\n &f- &cRetirer &fdes crédits"), "arkacore.credit.take|arkacore.credit.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.show") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "show", "<joueur>", Collections.singletonList("\n &f- &cVoir &fles crédits"), "arkacore.credit.show|arkacore.credit.admin", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        commandComponent
                .append("\n" + this.prefix)
                .append("\n" + this.prefix + "&f&nSurvolez la commande pour plus d'informations.")
                .append("\n&f" + Common.chatLine())
                .send(super.getSender());
    }

    /**
     * Méthode pour créditer un joueur avec un montant donné
     */
    private void creditPlayer(UUID uniqueId, long amount, String reason) {
        PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(uniqueId);
        if (!paramTargetData.isLoaded()) {
            super.tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
            return;
        }

        // Créditer le joueur avec le montant spécifié
        paramTargetData.creditOmegaCoins(amount, reason, false, (l, l1, throwable) -> {
            if (throwable != null) {
                super.tellError("&cUne erreur est survenue! Le joueur n'a pas pu être trouvé.");
                throw new FoException(throwable);
            }

            this.tellSuccess("&fVous avez ajouté &a" + amount + " omegas &fà &a" + paramTargetData.getEffectiveName() + "&a.");
        });
    }

    /**
     * Méthode pour retirer un montant de crédits à un joueur
     */
    private void withdrawPlayer(UUID uniqueId, long amount, String reason) {
        PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(uniqueId);
        if (!paramTargetData.isLoaded()) {
            super.tellError("&cUne erreur est survenue! Le joueur n'a pas pu être trouvé.");
            return;
        }

        // Retirer le montant spécifié du crédit du joueur
        paramTargetData.withdrawOmegaCoins(amount, reason, (l, l1, throwable) -> {
            if (throwable != null) {
                this.tellError("&cUne erreur est survenue lors de l'ajout des crédits.");
                throw new FoException(throwable);
            }

            this.tellSuccess("&fVous avez retiré &a" + amount + " omegas &fà &a" + paramTargetData.getEffectiveName() + "&f.");
        });
    }

    /**
     * Méthode pour convertir le montant de crédits (en tant que chaîne de caractères) en entier
     */
    private int convertAmountToInt(String args) {
        if (!Utils.isInteger(args)) {
            super.tellError("&cLe montant doit être un nombre entier.");
            return 0;
        }

        final int amount = Integer.parseInt(args);
        if (amount <= 0) {
            super.tellError("&cLe montant doit être supérieur à 0.");
            return 0;
        }

        return amount;
    }
}