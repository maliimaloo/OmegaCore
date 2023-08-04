package net.omegagames.core.bukkit.api.commands.credit;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.bukkit.api.util.LangUtils;
import net.omegagames.core.bukkit.api.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class CreditManagerCommand extends SimpleSubCommand {

    /**
     * Constructeur de la classe `CreditManagerCommand`
     */
    public CreditManagerCommand() {
        super("give|take|show");
    }

    /**
     * Méthode principale pour traiter la commande /credit give|take|show
     */
    @Override
    protected void onCommand() {
        // Vérifier si les arguments sont corrects
        if (super.args.length == 0) {
            super.returnInvalidArgs();
            return;
        }

        // Vérifier les permissions du joueur
        if (!Utils.hasPermission(super.getSender(), super.getPermission())) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", super.getPermission())));
            return;
        }

        // Récupérer l'action demandée (give, take, show)
        final Param param = Param.find(super.getSublabel());
        if (param == null) {
            super.returnInvalidArgs();
            return;
        }

        // Récupérer le nom du joueur cible et la raison (uniquement pour give et take)
        final String targetName = super.args[0];
        final String reason = Common.joinRange(2, super.args);

        // Traiter l'action en fonction de l'action demandée
        switch (param) {
            case GIVE -> handleGiveCommand(targetName, reason);

            case TAKE -> handleTakeCommand(targetName, reason);

            case SHOW -> handleShowCommand(targetName);
        }
    }

    /**
     * Enum pour représenter les différentes actions possibles pour la commande /credit
     */
    private enum Param {
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
    private void handleGiveCommand(String targetName, String reason) {
        // Vérifier les permissions du joueur pour effectuer cette action
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.give.others") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", "arkacore.credit.give")));
            return;
        }

        // Récupérer le joueur cible et le montant spécifié
        Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        final int amount = convertAmountToInt(CreditManagerCommand.super.args);

        // Vérifier si le joueur cible est en ligne
        if (paramTargetPlayer == null) {
            // Si le joueur n'est pas en ligne, récupérer son UUID de manière asynchrone
            Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    // Si le joueur n'existe pas, afficher un message d'erreur
                    if (uniqueId == null) {
                        CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        return;
                    }

                    // Créditer le joueur cible avec le montant spécifié
                    creditPlayer(uniqueId, amount, reason);
                }

                @Override
                public void onFailure() {
                    CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
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
    private void handleTakeCommand(String targetName, String reason) {
        // Vérifier les permissions du joueur pour effectuer cette action
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take.others") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", super.getPermission())));
            return;
        }

        // Récupérer le joueur cible et le montant spécifié
        Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        int paramAmount = convertAmountToInt(CreditManagerCommand.super.args);

        // Vérifier si le joueur cible est en ligne
        if (paramTargetPlayer == null) {
            // Si le joueur n'est pas en ligne, récupérer son UUID de manière asynchrone
            Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    // Si le joueur n'existe pas, afficher un message d'erreur
                    if (uniqueId == null) {
                        CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        return;
                    }

                    // Retirer le montant spécifié du crédit du joueur cible
                    withdrawPlayer(uniqueId, paramAmount, reason);
                }

                @Override
                public void onFailure() {
                    CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
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
                        CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        return;
                    }

                    // Récupérer les données du joueur cible et afficher son crédit
                    PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(uniqueId);
                    if (!paramTargetData.isLoaded()) {
                        CreditManagerCommand.super.tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
                        return;
                    }

                    CreditManagerCommand.super.tell("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getOmegaCoins() + " &aoméga.");
                }

                @Override
                public void onFailure() {
                    CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            // Si le joueur est en ligne, récupérer directement les données du joueur et afficher son crédit
            PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(paramTargetPlayer.getUniqueId());
            if (!paramTargetData.isLoaded()) {
                CreditManagerCommand.super.tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
                return;
            }

            CreditManagerCommand.super.tell("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getOmegaCoins() + " &aoméga.");
        }
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

            this.tellSuccess("&aVous avez ajouté &f" + amount + " &acrédits à &f" + paramTargetData.getEffectiveName() + "&a.");
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

            this.tellSuccess("&aVous avez retiré &f" + amount + " &acrédits à &f" + paramTargetData.getEffectiveName() + "&a.");
        });
    }

    /**
     * Méthode pour convertir le montant de crédits (en tant que chaîne de caractères) en entier
     */
    private int convertAmountToInt(String[] args) {
        final String amountString = args[1];
        if (!Utils.isInteger(amountString)) {
            super.tellError("&cLe montant doit être un nombre entier.");
            return 0;
        }

        final int amount = Integer.parseInt(amountString);
        if (amount <= 0) {
            super.tellError("&cLe montant doit être supérieur à 0.");
            return 0;
        }

        return amount;
    }
}
