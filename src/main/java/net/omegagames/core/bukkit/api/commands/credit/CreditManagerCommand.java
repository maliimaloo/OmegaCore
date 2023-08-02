package net.omegagames.core.bukkit.api.commands.credit;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.bukkit.api.util.LangUtils;
import net.omegagames.core.bukkit.api.util.Utils;
import net.omegagames.core.bukkit.api.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class CreditManagerCommand extends SimpleSubCommand {
    private final String prefix = Settings.PLUGIN_PREFIX + " ";

    public CreditManagerCommand() {
        super("give|take");
    }

    @Override
    protected void onCommand() {
        if (super.args.length == 0) {
            super.returnInvalidArgs();
            return;
        }

        final Param param = Param.find(super.getSublabel());
        if (param == null) {
            super.tellError("&cArgument invalide. Veuillez utiliser &6/give&c ou &6/take&c.");
            return;
        }

        switch (param) {
            case GIVE -> {
                if (!Utils.hasPermission(super.getSender(), "arkacore.credit.give")) {
                    super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", "arkacore.credit.give")));
                    return;
                }

                if (super.args.length < 2) {
                    super.returnInvalidArgs();
                    return;
                }

                final String targetName = super.args[0];
                final String amountString = super.args[1];
                if (!Utils.isInteger(amountString)) {
                    super.tellError("&cLe montant doit être un nombre entier.");
                    return;
                }

                final int amount = Integer.parseInt(amountString);
                if (amount <= 0) {
                    super.tellError("&cLe montant doit être supérieur à 0.");
                    return;
                }

                Player paramTargetPlayer = Bukkit.getPlayer(targetName);
                if (paramTargetPlayer == null) {
                    Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                        @Override
                        public void onSuccess(UUID uniqueId) {
                            if (uniqueId == null) {
                                CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                                return;
                            }

                            CreditManagerCommand.this.creditPlayer(uniqueId, amount);
                        }

                        @Override
                        public void onFailure() {
                            CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        }
                    });
                } else {
                    this.creditPlayer(paramTargetPlayer.getUniqueId(), amount);
                }
            }
            
            case TAKE -> {
                if (!Utils.hasPermission(super.getSender(), "arkacore.credit.take")) {
                    super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, SerializedMap.ofArray("{permission}", "arkacore.credit.take")));
                    return;
                }
                if (super.args.length < 2) {
                    super.returnInvalidArgs();
                    return;
                }

                final String targetName = super.args[0];
                final String amountString = super.args[1];
                if (!Utils.isInteger(amountString)) {
                    super.tellError("&cLe montant doit être un nombre entier.");
                    return;
                }

                final int amount = Integer.parseInt(amountString);
                if (amount <= 0) {
                    super.tellError("&cLe montant doit être supérieur à 0.");
                    return;
                }

                Player paramTargetPlayer = Bukkit.getPlayer(targetName);
                if (paramTargetPlayer == null) {
                    Utils.getUUIDFromUsernameAsync(targetName, new Utils.UUIDCallback() {
                        @Override
                        public void onSuccess(UUID uniqueId) {
                            if (uniqueId == null) {
                                CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                                return;
                            }

                            CreditManagerCommand.this.withdrawPlayer(uniqueId, amount);
                        }

                        @Override
                        public void onFailure() {
                            CreditManagerCommand.super.tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                        }
                    });
                } else {
                    this.withdrawPlayer(paramTargetPlayer.getUniqueId(), amount);
                }
            }
        }
    }


    private enum Param {
        /**
         * Donner des crédits à un joueur.
         */
        GIVE("give", "g"),

        /**
         * Retirer des crédits à un joueur.
         */
        TAKE("take", "t");

        private final String label;

        private final String[] aliases;

        Param(final String paramLabel, final String... paramAliases) {
            this.label = paramLabel;
            this.aliases = paramAliases;
        }

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

    private void creditPlayer(UUID uniqueId, long amount) {
        PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(uniqueId);
        if (paramTargetData == null) {
            super.tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
            return;
        }

        paramTargetData.creditOmegaCoins(amount, null, false, (l, l1, throwable) -> {
            if (throwable != null) {
                this.tellError("&cUne erreur est survenue lors de l'ajout des crédits.");
                throw new FoException(throwable);
            }

            this.tellSuccess("&aVous avez ajouté &f" + amount + " &acrédits à &f" + paramTargetData.getEffectiveName() + "&a.");
        });
    }

    private void withdrawPlayer(UUID uniqueId, long amount) {
        PlayerData paramTargetData = ApiImplementation.getInstance().getPlayerManager().getPlayerData(uniqueId);
        if (paramTargetData == null) {
            super.tellError("&cUne erreur est survenue lors de la récupération des données du joueur.");
            return;
        }

        paramTargetData.withdrawOmegaCoins(amount, null, (l, l1, throwable) -> {
            if (throwable != null) {
                this.tellError("&cUne erreur est survenue lors de l'ajout des crédits.");
                throw new FoException(throwable);
            }

            this.tellSuccess("&aVous avez retiré &f" + amount + " &acrédits à &f" + paramTargetData.getEffectiveName() + "&a.");
        });
    }
}
