package net.arkamc.core.bukkit.util;

import net.arkamc.core.bukkit.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.SimpleComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public final class CommandUtils {
    private static final String prefix = Settings.PLUGIN_PREFIX + " ";

    /**
     * Crée un composant de commande avec une sous-commande et un message d'information au survol.
     *
     * @param paramCommandComponent le composant de commande à créer
     * @param paramCurrentLabel     le label actuel de la commande
     * @param paramPlayer           le joueur concerné
     * @param paramSubcommand       la sous-commande à ajouter
     * @param paramHoverMessage     le message d'information au survol
     * @param paramPermission       la permission requise pour la commande
     * @param paramAction           l'action à effectuer lors du clic
     */
    public static void createCommandComponent(SimpleComponent paramCommandComponent, String paramCurrentLabel, Player paramPlayer, String paramSubcommand, String paramHoverMessage, String paramPermission, String paramAction) {
        paramCommandComponent
                .append("\n" + prefix + "/" +  paramCurrentLabel + " &c&n" + paramSubcommand)
                .onHover(generateHover(paramPlayer, paramHoverMessage, paramPermission, paramAction));

        String paramCommandString = "/" + paramCurrentLabel + " " + paramSubcommand;
        getAction(paramCommandComponent, paramAction, paramCommandString);
    }

    /**
     * Crée un composant de commande avec une sous-commande et une valeur de sous-commande, ainsi qu'une liste de messages d'information au survol.
     *
     * @param paramCommandComponent le composant de commande à créer
     * @param paramCurrentLabel     le label actuel de la commande
     * @param paramPlayer           le joueur concerné
     * @param paramSubcommand       la sous-commande à ajouter
     * @param paramValueSubcommand  la valeur de la sous-commande à afficher
     * @param paramHoverList        la liste de messages d'information au survol
     * @param paramPermission       la permission requise pour la commande
     * @param paramAction           l'action à effectuer lors du clic
     */
    public static void createCommandComponent(SimpleComponent paramCommandComponent, String paramCurrentLabel, Player paramPlayer, String paramSubcommand, String paramValueSubcommand, List<String> paramHoverList, String paramPermission, String paramAction) {
        paramSubcommand = (Valid.isNullOrEmpty(paramSubcommand) ? "" : " &c&n" + paramSubcommand);
        paramCommandComponent
                .append("\n" + prefix + "/" + paramCurrentLabel + paramSubcommand + "&r " + paramValueSubcommand)
                .onHover(generateHover(paramPlayer, paramHoverList, paramPermission, paramAction));

        String paramCommandString = "/" + paramCurrentLabel + paramSubcommand;
        getAction(paramCommandComponent, paramAction, paramCommandString);
    }

    /**
     * Obtient l'action à effectuer lors du clic sur le composant de commande.
     *
     * @param paramCommandComponent le composant de commande
     * @param paramString1         l'action spécifiée
     * @param paramString2         la commande à exécuter ou à suggérer
     */
    public static void getAction(SimpleComponent paramCommandComponent, String paramString1, String paramString2) {
        paramString2 = Common.stripColors(paramString2);
        if (Objects.equals(paramString1, Action.RUN_COMMAND.getHover())) {
            paramCommandComponent.onClickRunCmd(paramString2);
        } else {
            paramCommandComponent.onClickSuggestCmd(paramString2);
        }
    }

    /**
     * Génère une liste de messages d'information au survol avec une description et un pied de page.
     *
     * @param paramPlayer   le joueur concerné
     * @param paramString1  la description du survol
     * @param paramString2  la permission requise pour la commande
     * @param paramString3  l'action à effectuer lors du clic
     * @return la liste de messages d'information au survol
     */
    public static List<String> generateHover(Player paramPlayer, String paramString1, String paramString2, String paramString3) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("&7Description: &f" + paramString1);
        addHoverFooterToList(paramPlayer, arrayList, paramString2, paramString3);
        return arrayList;
    }

    /**
     * Génère une liste de messages d'information au survol avec une description, une liste de messages et un pied de page.
     *
     * @param paramPlayer  le joueur concerné
     * @param paramList    la liste de messages à afficher dans le survol
     * @param paramString1 la permission requise pour la commande
     * @param paramString2 l'action à effectuer lors du clic
     * @return la liste de messages d'information au survol
     */
    public static List<String> generateHover(Player paramPlayer, List<String> paramList, String paramString1, String paramString2) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("&7Description:");
        arrayList.addAll(paramList);
        addHoverFooterToList(paramPlayer, arrayList, paramString1, paramString2);
        return arrayList;
    }

    /**
     * Ajoute un pied de page au survol à une liste de messages d'information.
     *
     * @param paramPlayer le joueur concerné
     * @param paramList   la liste de messages d'information
     * @param paramString1 la permission requise pour la commande
     * @param paramString2 l'action à effectuer lors du clic
     */
    public static void addHoverFooterToList(Player paramPlayer, List<String> paramList, String paramString1, String paramString2) {
        paramList.add("&7Permission: &f" + generatePermissionLine(paramPlayer, paramString1));
        paramList.add(" ");
        paramList.add("&f" + paramString2);
    }

    /**
     * Génère la ligne de permission avec un symbole de vérification pour le joueur donné.
     *
     * @param paramPlayer le joueur concerné
     * @param paramString la permission à afficher
     * @return la ligne de permission avec le symbole de vérification
     */
    public static String generatePermissionLine(Player paramPlayer, String paramString) {
        boolean bool = Utils.hasPermission(paramPlayer, paramString);
        return paramString + " &7(" + (bool ? "&a✔&7" : "&c✘&7") + "&7)";
    }


    /**
     * Les actions disponibles pour le clic sur le composant de commande.
     */
    public enum Action {
        RUN_COMMAND("&a&nClique pour exécuter cette commande"),
        SUGGEST_COMMAND("&a&nClique pour suggérer cette commande");

        Action(String param1String1) {
            this.hover = param1String1;
        }

        private final String hover;

        public String getHover() {
            return this.hover;
        }
    }
}
