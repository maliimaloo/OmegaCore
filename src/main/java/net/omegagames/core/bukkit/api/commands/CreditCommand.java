package net.omegagames.core.bukkit.api.commands;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.player.PlayerData;
import net.omegagames.core.bukkit.api.settings.Settings;
import net.omegagames.core.bukkit.api.util.CommandUtils;
import net.omegagames.core.bukkit.api.util.LangUtils;
import net.omegagames.core.bukkit.api.util.Utils;
import net.omegagames.core.persistanceapi.beans.credit.CreditBean;
import net.omegagames.core.persistanceapi.beans.players.PlayerBean;
import net.omegagames.core.bukkit.api.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

public final class CreditCommand extends SimpleCommand {
    private final ApiImplementation api;

    public CreditCommand(ApiImplementation api) {
        super("acredit");
        super.setAutoHandleHelp(false);

        this.api = api;
    }

    @Override
    protected void onCommand() {
        if (super.args.length == 0) {
            super.returnInvalidArgs();
            return;
        }

        final Param param = Param.find(super.args[0]);
        if (param == null) {
            super.returnInvalidArgs();
            return;
        }

        final String targetName = super.args.length > 1 ? super.args[1] : "";
        final String amountArgs = super.args.length > 2 ? super.args[2] : "";
        final String reason = super.args.length > 3 ? Common.joinRange(3, super.args) : "";

        switch (param) {
            case HELP:
                handleHelpCommand();
                break;

            case GIVE:
                handleGiveCommand(targetName, reason, amountArgs);
                break;

            case TAKE:
                handleTakeCommand(targetName, reason, amountArgs);
                break;

            case SHOW:
                handleShowCommand(targetName);
                break;
        }
    }

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

    private void handleGiveCommand(String targetName, String reason, String amountArgs) {
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.give") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("permission", "arkacore.credit.give")));
            return;
        }

        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        final long amount = parseAmount(amountArgs);

        if (paramTargetPlayer == null) {
            Utils.getUUIDFromUsernameAsync(targetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    creditPlayer(uniqueId, amount, reason, null);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            this.creditPlayer(paramTargetPlayer.getUniqueId(), amount, reason, paramTargetPlayer);
        }
    }

    private void handleTakeCommand(String targetName, String reason, String amountArgs) {
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", super.getPermission())));
            return;
        }

        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        final long amount = this.parseAmount(amountArgs);

        if (paramTargetPlayer == null) {
            Utils.getUUIDFromUsernameAsync(targetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID var1) {
                    withdrawPlayer(var1, amount, reason, null);
                }

                @Override
                public void onFailure(Throwable var1) {
                    tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            withdrawPlayer(paramTargetPlayer.getUniqueId(), amount, reason, paramTargetPlayer);
        }
    }

    private void handleShowCommand(String targetName) {
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.show.others") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", super.getPermission())));
            return;
        }

        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        if (paramTargetPlayer == null) {
            Utils.getUUIDFromUsernameAsync(targetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    PlayerData paramTargetData = api.getPlayerManager().getPlayerData(uniqueId);
                    if (!paramTargetData.isLoaded()) {
                        PlayerBean playerBean = api.getServerServiceManager().getPlayer(uniqueId);
                        if (playerBean == null) {
                            tellError("&cUne erreur est survenue lors de la récupération des données du joueur hors-ligne.");
                            return;
                        }

                        tellSuccess("&aLe joueur &f" + playerBean.getName() + " &aà &f" + playerBean.getOmega() + " &aoméga.");
                        return;
                    }

                    tellSuccess("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getOmegaCoins() + " &aoméga.");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    tellError("&cLe joueur &f" + targetName + " &cn'existe pas.");
                }
            });
        } else {
            PlayerData paramTargetData = api.getPlayerManager().getPlayerData(paramTargetPlayer.getUniqueId());
            if (!paramTargetData.isLoaded()) {
                tellError("&cUne erreur est survenue lors de la récupération des données du joueur en ligne.");
                return;
            }

            tellSuccess("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getOmegaCoins() + " &aoméga.");
        }
    }

    private void handleHelpCommand() {
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.help") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.scoreboard.help")));
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
                .append("\n" + Settings.PLUGIN_PREFIX)
                .append("\n" + Settings.PLUGIN_PREFIX + "&f&nSurvolez la commande pour plus d'informations.")
                .append("\n&f" + Common.chatLine())
                .send(super.getSender());
    }

    private void creditPlayer(UUID uniqueId, long amount, String reason, Player targetPlayer) {
        PlayerData paramTargetData = this.api.getPlayerManager().getPlayerData(uniqueId);
        if (!paramTargetData.isLoaded()) {
            this.api.getServerServiceManager().getPlayer(uniqueId, new Callback<PlayerBean>() {
                @Override
                public void onSuccess(PlayerBean playerBean) {
                    final long newAmount = playerBean.getOmega() + amount;
                    playerBean.setOmega(newAmount);

                    handleSaveTransactionOffline(playerBean, amount, reason, "give");

                    tellSuccess("&fVous avez ajouté &a" + amount + " omegas &fà &a" + playerBean.getName() + "&a.");
                    api.getServerServiceManager().updatePlayer(playerBean, null);
                }

                @Override
                public void onFailure(Throwable cause) {
                    tellError(cause.getMessage());
                }
            });
        } else {
            paramTargetData.creditOmegaCoins(amount, reason, false, (result, amountFinal, throwable) -> {
                if (throwable != null) {
                    this.tellError("&cUne erreur est survenue lors de l'ajout des crédits! Cause: " + throwable.getMessage());
                    return;
                }

                this.handleSaveTransactionOnline(paramTargetData, amount, reason, "give");

                this.tellSuccess("&fVous avez ajouté &a" + amount + " omegas &fà &a" + paramTargetData.getEffectiveName() + "&a. &7(&f" + reason + "&7)");
                if (targetPlayer != null) {
                    Messenger.success(targetPlayer, "&fVous avez reçu &a" + amountFinal + " omegas &fde &a" + super.getSender().getName() + "&a. &7(&f" + reason + "&7)");
                }
            });
        }
    }

    private void withdrawPlayer(UUID playerId, long amount, String reason, Player targetPlayer) {
        PlayerData playerData = api.getPlayerManager().getPlayerData(playerId);
        if (!playerData.isLoaded()) {
            this.api.getServerServiceManager().getPlayer(playerId, new Callback<PlayerBean>() {
                @Override
                public void onSuccess(PlayerBean playerBean) {
                    long newAmount = Math.max(playerBean.getOmega() - amount, 0);
                    playerBean.setOmega(newAmount);

                    handleSaveTransactionOffline(playerBean, amount, reason, "take");

                    tellSuccess("&fVous avez retiré &a" + amount + " omegas &fà &a" + playerBean.getName() + "&a.");
                    api.getServerServiceManager().updatePlayer(playerBean, null);
                }

                @Override
                public void onFailure(Throwable cause) {
                    tellError(cause.getMessage());
                }
            });
        } else {
            playerData.withdrawOmegaCoins(amount, reason, (updatedAmount, newBalance, throwable) -> {
                if (throwable != null) {
                    this.tellError("&cUne erreur est survenue lors du retrait des crédits! Cause: " + throwable.getMessage());
                    return;
                }

                this.handleSaveTransactionOnline(playerData, amount, reason, "take");

                this.tellSuccess("&fVous avez retiré &a" + amount + " omegas &fà &a" + playerData.getEffectiveName() + "&f. &7(&f" + reason + "&7)");
                if (targetPlayer != null) {
                    Messenger.success(targetPlayer, "&fVous avez perdu &a" + amount + " omegas &fde &a" + super.getSender().getName() + "&f. &7(&f" + reason + "&7)");
                }
            });
        }
    }

    private long parseAmount(String args) {
        if (!Utils.isInteger(args)) {
            super.tellError("&cLe montant doit être un nombre entier.");
            return 0;
        }

        return Math.max(Long.parseLong(args), 0);
    }

    private void handleSaveTransactionOffline(PlayerBean playerBean, long amount, String reason, String param) {
        LocalDateTime currentTime = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(currentTime);
        CreditBean creditBean = new CreditBean(playerBean.getUniqueId(), timestamp, param, super.getSender().getName(), playerBean.getName(), (int) amount, reason);
        playerBean.getCreditLogs().add(creditBean);
    }

    private void handleSaveTransactionOnline(PlayerData playerData, long amount, String reason, String param) {
        LocalDateTime currentTime = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(currentTime);
        CreditBean creditBean = new CreditBean(playerData.getUniqueId(), timestamp, param, super.getSender().getName(), playerData.getEffectiveName(), (int) amount, reason);
        playerData.addLog(creditBean);
    }
}
