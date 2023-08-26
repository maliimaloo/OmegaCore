package net.arkamc.arkacore.bukkit.commands;

import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.data.PlayerData;
import net.arkamc.arkacore.bukkit.menu.MenuCreditLog;
import net.arkamc.arkacore.bukkit.settings.Settings;
import net.arkamc.arkacore.bukkit.util.CommandUtils;
import net.arkamc.arkacore.bukkit.util.LangUtils;
import net.arkamc.arkacore.bukkit.util.Utils;
import net.arkamc.arkacore.bukkit.util.model.Callback;
import net.arkamc.arkacore.persistanceapi.beans.credit.CreditBean;
import net.arkamc.arkacore.persistanceapi.beans.players.PlayerBean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Une commande personnalisée pour gérer les crédits des joueurs.
 */
public final class CreditCommand extends SimpleCommand {
    private final ApiImplementation api;

    /**
     * Constructeur pour la commande des crédits.
     *
     * @param api L'instance de l'API.
     */
    public CreditCommand(ApiImplementation api) {
        super("acredit");
        super.setAutoHandleHelp(false);

        this.api = api;
    }

    /**
     * Méthode appelée lors de l'exécution de la commande.
     * Gère les différentes sous-commandes et leurs paramètres.
     */
    @Override
    protected void onCommand() {
        super.checkArgs(super.args.length > 0);

        final Param param = Param.find(super.args[0]);
        if (param == null) {
            super.returnInvalidArgs();
            return;
        }

        final String targetName = super.args.length > 1 ? super.args[1] : "";
        final String amountArgs = super.args.length > 2 ? super.args[2] : "";

        switch (param) {
            case HELP:
                this.handleHelpCommand();
                break;

            case GIVE:
                this.handleGiveCommand(targetName, Common.joinRange(3, super.args), amountArgs);
                break;

            case TAKE:
                this.handleTakeCommand(targetName, Common.joinRange(3, super.args), amountArgs);
                break;

            case RESET:
                this.handleResetCommand(targetName, Common.joinRange(2, super.args));
                break;

            case SHOW:
                this.handleShowCommand(targetName);
                break;

            case LOGS:
                this.handleLogCommand(targetName);
                break;
        }
    }

    /**
     * Enumeration pour les différentes sous-commandes de la commande /acredit.
     */
    private enum Param {
        HELP("help", "?"),
        GIVE("give", "g"),
        TAKE("take", "t"),
        RESET("reset", "r"),
        SHOW("show", "s"),
        LOGS("logs", "l");

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

    /**
     * Gère les commandes liées aux crédits, telles que donner, retirer, afficher et journaliser les crédits des joueurs.
     * Contient des méthodes pour chaque action de commande spécifique.
     */
    private void handleGiveCommand(String paramTargetName, String paramReason, String paramAmountArgs) {
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.give") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            final String noPermissionMsg = LangUtils.of(SimpleLocalization.NO_PERMISSION, "permission", "arkacore.credit.give");
            super.tellError(noPermissionMsg);
            return;
        }

        final Player targetBukkitPlayer = Bukkit.getPlayer(paramTargetName);
        final long amount = parseAmount(paramAmountArgs);
        final String reason = Valid.isNullOrEmpty(paramReason) ? "" : paramReason;

        if (targetBukkitPlayer == null) {
            Utils.getUUIDFromUsernameAsync(paramTargetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    creditPlayer(uniqueId, amount, reason, null);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    tellError(throwable.getMessage());
                }
            });
        } else {
            this.creditPlayer(targetBukkitPlayer.getUniqueId(), amount, reason, targetBukkitPlayer);
        }
    }

    /**
     * Gère la commande pour retirer des crédits à un joueur.
     */
    private void handleTakeCommand(String paramTargetName, String paramReason, String paramAmountArgs) {
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.credit.take")));
            return;
        }

        final Player targetBukkitPlayer = Bukkit.getPlayer(paramTargetName);
        final long amount = this.parseAmount(paramAmountArgs);
        final String reason = Valid.isNullOrEmpty(paramReason) ? "" : paramReason;

        if (targetBukkitPlayer == null) {
            Utils.getUUIDFromUsernameAsync(paramTargetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID targetUniqueId) {
                    withdrawPlayer(targetUniqueId, amount, reason, null);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    tellError(throwable.getMessage());
                }
            });
        } else {
            withdrawPlayer(targetBukkitPlayer.getUniqueId(), amount, reason, targetBukkitPlayer);
        }
    }

    /**
     * Gère la commande "reset" pour réinitialiser les crédits et les journaux d'un joueur.
     */
    private void handleResetCommand(String targetName, String resetReason) {
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.reset") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.credit.reset")));
            return;
        }

        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        final String reason = Valid.isNullOrEmpty(resetReason) ? "" : resetReason;

        if (paramTargetPlayer == null) {
            Utils.getUUIDFromUsernameAsync(targetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID targetUniqueId) {
                    CreditCommand.this.resetPlayer(targetUniqueId, reason, null);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    CreditCommand.super.tellError(throwable.getMessage());
                }
            });
        } else {
            this.resetPlayer(paramTargetPlayer.getUniqueId(), reason, paramTargetPlayer);
        }
    }

    /**
     * Gère la commande pour journaliser les informations liées aux crédits pour un joueur.
     */
    private void handleLogCommand(String targetName) {
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.log") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.credit.log")));
            return;
        }

        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        if (paramTargetPlayer == null) {
            Utils.getUUIDFromUsernameAsync(targetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    PlayerData paramTargetData = api.getPlayerManager().getPlayerData(uniqueId);
                    if (!paramTargetData.isLoaded()) {
                        PlayerBean playerBean = api.getSQLServiceManager().getPlayer(uniqueId);
                        if (playerBean == null) {
                            tellError("&cJoueur introuvable !");
                            return;
                        }

                        new MenuCreditLog(playerBean.getCreditLogs()).displayTo(CreditCommand.super.getPlayer());
                    }

                    new MenuCreditLog(paramTargetData.getLogs()).displayTo(CreditCommand.super.getPlayer());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    tellError(throwable.getMessage());
                }
            });
        } else {
            PlayerData targetData = this.api.getPlayerManager().getPlayerData(paramTargetPlayer.getUniqueId());
            if (!targetData.isLoaded()) {
                tellError("&cUne erreur est survenue lors de la récupération des données du joueur en ligne.");
                return;
            }

            new MenuCreditLog(targetData.getLogs()).displayTo(super.getPlayer());
        }
    }

    /**
     * Gère la commande pour afficher le montant de crédits d'un joueur.
     */
    private void handleShowCommand(String targetName) {
        if (Utils.hasPermission(super.getSender(), "arkacore.credit.show") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.credit.show")));
            return;
        }

        final Player paramTargetPlayer = Bukkit.getPlayer(targetName);
        if (paramTargetPlayer == null || !paramTargetPlayer.isOnline()) {
            Utils.getUUIDFromUsernameAsync(targetName, new Callback<UUID>() {
                @Override
                public void onSuccess(UUID uniqueId) {
                    PlayerData paramTargetData = CreditCommand.this.api.getPlayerManager().getPlayerData(uniqueId);
                    if (!paramTargetData.isLoaded()) {
                        PlayerBean playerBean = CreditCommand.this.api.getSQLServiceManager().getPlayer(uniqueId);
                        if (playerBean == null) {
                            tellError("&cJoueur introuvable !");
                            return;
                        }

                        CreditCommand.super.tellSuccess("&fLe joueur &a" + playerBean.getName() + " &fà &a" + playerBean.getCredit() + " crédits&f.");
                        return;
                    }

                    CreditCommand.super.tellSuccess("&fLe joueur &a" + paramTargetData.getEffectiveName() + " &fà &a" + paramTargetData.getCredit() + " crédits&f.");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    CreditCommand.super.tellError("Impossible de récupérer l'uuid du joueur.");
                }
            });
        } else {
            PlayerData paramTargetData = this.api.getPlayerManager().getPlayerData(paramTargetPlayer.getUniqueId());
            if (!paramTargetData.isLoaded()) {
                tellError("&cUne erreur est survenue lors de la récupération des données du joueur en ligne.");
                return;
            }

            tellSuccess("&aLe joueur &f" + paramTargetData.getEffectiveName() + " &aà &f" + paramTargetData.getCredit() + " &acrédits.");
        }
    }

    /**
     * Gère la commande pour fournir des informations d'aide concernant les commandes liées aux crédits.
     */
    private void handleHelpCommand() {
        if (!Utils.hasPermission(super.getSender(), "arkacore.credit.help") && !Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            super.tellError(LangUtils.of(SimpleLocalization.NO_PERMISSION, Collections.singletonMap("{permission}", "arkacore.credit.help")));
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
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "give", "<joueur> <montant> <raison>", Collections.singletonList("&f- &cDonner &fdes crédits"), "arkacore.credit.give", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.take") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "take", "<joueur> <montant> <raison>", Collections.singletonList("&f- &cRetirer &fdes crédits"), "arkacore.credit.take", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.reset") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "reset", "<joueur> <raison>", Collections.singletonList("&f- &cRéinitialiser &fles crédits et logs"), "arkacore.credit.reset", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.show") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "show", "<joueur>", Collections.singletonList("&f- &cAfficher &fles crédits"), "arkacore.credit.show", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        if (Utils.hasPermission(super.getSender(), "arkacore.credit.logs") || Utils.hasPermission(super.getSender(), "arkacore.credit.admin")) {
            CommandUtils.createCommandComponent(commandComponent, "&f" + super.getCurrentLabel(), super.getPlayer(), "logs", "<joueur>", Collections.singletonList("&f- &cAfficher &fles logs du joueur"), "arkacore.credit.logs", CommandUtils.Action.SUGGEST_COMMAND.getHover());
        }

        commandComponent
                .append("\n" + Settings.PLUGIN_PREFIX)
                .append("\n" + Settings.PLUGIN_PREFIX + "&f&nSurvolez la commande pour plus d'informations.")
                .append("\n&f" + Common.chatLine())
                .send(super.getSender());
    }

    /**
     * Réinitialise les crédits et les journaux d'un joueur en fonction de son UUID.
     *
     * @param playerUniqueId L'UUID du joueur à réinitialiser.
     * @param reason         La raison de la réinitialisation.
     * @param targetPlayer   Le joueur ayant déclenché la réinitialisation (null s'il est hors ligne).
     */
    private void resetPlayer(UUID playerUniqueId, String reason, Player targetPlayer) {
        PlayerData playerData = this.api.getPlayerManager().getPlayerData(playerUniqueId);
        final String resetReason = Valid.isNullOrEmpty(reason) ? "" : reason;

        if (!playerData.isLoaded()) {
            this.resetOfflinePlayer(playerUniqueId, resetReason);
        } else {
            this.resetOnlinePlayer(playerData, resetReason, targetPlayer);
        }
    }

    /**
     * Réinitialise les crédits et les journaux d'un joueur hors ligne.
     *
     * @param playerUniqueId L'UUID du joueur à réinitialiser.
     * @param resetReason    La raison de la réinitialisation.
     */
    private void resetOfflinePlayer(UUID playerUniqueId, String resetReason) {
        this.api.getSQLServiceManager().getPlayer(playerUniqueId, (playerBean) -> {
            long currentCredit = playerBean.getCredit();
            playerBean.setCredit(0);
            playerBean.setCreditLogs(new ArrayList<>());

            this.saveTransactionOffline(playerBean, currentCredit, resetReason, "reset");

            super.tellSuccess("&fVous avez réinitialisé les crédits et logs du joueur &a" + playerBean.getName() + "&f.");
            this.api.getSQLServiceManager().updatePlayer(playerBean);
        });
    }

    /**
     * Réinitialise les crédits et les journaux d'un joueur en ligne.
     *
     * @param playerData    L'objet de données du joueur.
     * @param resetReason   La raison de la réinitialisation.
     * @param targetPlayer  Le joueur ayant déclenché la réinitialisation.
     */
    private void resetOnlinePlayer(PlayerData playerData, String resetReason, Player targetPlayer) {
        playerData.withdrawCredit(playerData.getCredit(), (result, amountFinal, throwable) -> {
            if (throwable != null) {
                this.tellError("&cUne erreur est survenue lors de la réinitialisation des crédits! Cause: " + throwable.getMessage());
                return;
            }

            playerData.resetLogs();
            this.saveTransactionOnline(playerData, amountFinal, resetReason, "reset");

            this.tellSuccess("&fVous avez réinitialisé les crédits et logs du joueur&f. &7(&f" + resetReason + "&7)");
            if (targetPlayer != null) {
                Messenger.success(targetPlayer, "&fVos crédits et logs ont été réinitialisés par &a" + CreditCommand.super.getSender().getName() + "&f. &7(&f" + resetReason + "&7)");
                Messenger.success(targetPlayer, "&fNouveau solde: &a" + result + " crédits&f.");
            }
        });
    }

    /**
     * Crédite un joueur avec un montant spécifié et enregistre la transaction.
     *
     * @param uniqueId      L'UUID du joueur à créditer.
     * @param amount        Le montant de crédits à donner.
     * @param reason        La raison de donner des crédits.
     * @param targetPlayer  Le joueur qui reçoit les crédits.
     */
    private void creditPlayer(UUID uniqueId, long amount, String reason, Player targetPlayer) {
        PlayerData paramTargetData = this.api.getPlayerManager().getPlayerData(uniqueId);
        if (!paramTargetData.isLoaded()) {
            this.api.getSQLServiceManager().getPlayer(uniqueId, new Callback<PlayerBean>() {
                @Override
                public void onSuccess(PlayerBean playerBean) {
                    final long newAmount = playerBean.getCredit() + amount;
                    playerBean.setCredit(newAmount);

                    saveTransactionOffline(playerBean, amount, reason, "give");

                    tellSuccess("&fVous avez ajouté &a" + amount + " crédits &fà &a" + playerBean.getName() + "&a.");
                    api.getSQLServiceManager().updatePlayer(playerBean, null);
                }

                @Override
                public void onFailure(Throwable cause) {
                    tellError(cause.getMessage());
                }
            });
        } else {
            paramTargetData.creditCredit(amount, (result, amountFinal, throwable) -> {
                if (throwable != null) {
                    tellError("&cUne erreur est survenue lors de l'ajout des crédits! Cause: " + throwable.getMessage());
                    return;
                }

                saveTransactionOnline(paramTargetData, amountFinal, reason, "give");

                this.tellSuccess("&fVous avez ajouté &a" + amount + " crédits &fà &a" + paramTargetData.getEffectiveName() + "&a. &7(&f" + reason + "&7)");
                if (targetPlayer != null) {
                    Messenger.success(targetPlayer, "&fVous avez reçu &a" + amountFinal + " crédits &fde &a" + super.getSender().getName() + "&a. &7(&f" + reason + "&7)");
                    Messenger.success(targetPlayer, "&fNouveau solde: &a" + result + " crédits&f.");
                }
            });
        }
    }

    /**
     * Retire des crédits du compte d'un joueur et enregistre la transaction.
     *
     * @param playerId      L'UUID du joueur à retirer les crédits.
     * @param amount        Le montant de crédits à retirer.
     * @param reason        La raison de retirer des crédits.
     * @param bukkitPlayer  Le joueur qui perd les crédits.
     */
    private void withdrawPlayer(UUID playerId, long amount, String reason, Player bukkitPlayer) {
        PlayerData playerData = api.getPlayerManager().getPlayerData(playerId);
        if (!playerData.isLoaded()) {
            this.withdrawPlayerOffline(playerId, amount, reason);
        } else {
            this.withdrawPlayerOnline(playerData, amount, reason, bukkitPlayer);
        }
    }

    /**
     * Retire les crédits d'un joueur hors-ligne.
     *
     * @param uniqueId L'uniqueId' du joueur
     * @param amount   La quantité a retiré
     * @param reason   La raison du retrait
     */
    private void withdrawPlayerOffline (UUID uniqueId, long amount, String reason) {
        this.api.getSQLServiceManager().getPlayer(uniqueId, new Callback<PlayerBean>() {
            @Override
            public void onSuccess(PlayerBean playerBean) {
                long newAmount = Math.max(playerBean.getCredit() - amount, 0);
                playerBean.setCredit(newAmount);

                CreditCommand.this.saveTransactionOffline(playerBean, amount, reason, "take");

                CreditCommand.super.tellSuccess("&fVous avez retiré &a" + amount + " crédits &fà &a" + playerBean.getName() + "&a.");
                CreditCommand.this.api.getSQLServiceManager().updatePlayer(playerBean, null);
            }

            @Override
            public void onFailure(Throwable cause) {
                CreditCommand.super.tellError(cause.getMessage());
            }
        });
    }

    /**
     * Retire les crédits d'un joueur hors-ligne.
     *
     * @param playerData   Les data du joueur
     * @param amount       La quantité a retiré
     * @param reason       La raison du retrait
     * @param bukkitPlayer (Nullable) Le joueur bukkit
     */
    private void withdrawPlayerOnline (PlayerData playerData, long amount, String reason, Player bukkitPlayer) {
        playerData.withdrawCredit(amount, (result, amountFinal, throwable) -> {
            if (throwable != null) {
                this.tellError("&cUne erreur est survenue lors du retrait des crédits! Cause: " + throwable.getMessage());
                return;
            }

            this.saveTransactionOnline(playerData, amountFinal, reason, "take");

            super.tellSuccess("&fVous avez retiré &a" + amount + " crédits &fà &a" + playerData.getEffectiveName() + "&f. &7(&f" + reason + "&7)");
            if (bukkitPlayer != null) {
                Messenger.success(bukkitPlayer, "&fVous avez perdu &a" + amount + " crédits &fde &a" + super.getSender().getName() + "&f. &7(&f" + reason + "&7)");
                Messenger.success(bukkitPlayer, "&fNouveau solde: &a" + result + " crédits&f.");
            }
        });
    }

    /**
     * Gère l'enregistrement d'une transaction de crédit pour un joueur hors ligne.
     *
     * @param playerBean    La structure de données du joueur.
     * @param amount        Le montant de crédits impliqué.
     * @param reason        La raison de la transaction.
     * @param param         Le type de transaction (donner ou retirer).
     */
    private void saveTransactionOffline(PlayerBean playerBean, long amount, String reason, String param) {
        if (reason == null || reason.isEmpty()) {
            reason = "Aucune raison spécifiée.";
        }

        LocalDateTime currentTime = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(currentTime);
        CreditBean creditBean = new CreditBean(playerBean.getUniqueId(), timestamp, param, super.getSender().getName(), playerBean.getName(), (int) amount, reason);
        playerBean.getCreditLogs().add(creditBean);
    }

    /**
     * Gère l'enregistrement d'une transaction de crédit pour un joueur en ligne.
     *
     * @param playerData    L'objet de données du joueur.
     * @param amount        Le montant de crédits impliqué.
     * @param reason        La raison de la transaction.
     * @param param         Le type de transaction (donner ou retirer).
     */
    private void saveTransactionOnline(PlayerData playerData, long amount, String reason, String param) {
        if (reason == null || reason.isEmpty()) {
            reason = "Aucune raison spécifiée.";
        }

        LocalDateTime currentTime = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(currentTime);
        CreditBean creditBean = new CreditBean(playerData.getUniqueId(), timestamp, param, super.getSender().getName(), playerData.getEffectiveName(), (int) amount, reason);
        playerData.addLog(creditBean);
    }

    /**
     * Analyse l'argument de chaîne donné en un montant de crédits valide.
     *
     * @param args  L'argument contenant le montant de crédits.
     * @return      Le montant de crédits analysé.
     */
    private long parseAmount(String args) {
        if (!Valid.isInteger(args)) {
            super.tellError("&cLe montant doit être un nombre entier.");
            return 0;
        }

        return Math.max(Long.parseLong(args), 0);
    }
}
