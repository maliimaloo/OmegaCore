package net.omegagames.core.bukkit.api.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import net.omegagames.core.bukkit.ApiImplementation;
import org.bukkit.entity.Player;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.SimpleScoreboard;
import org.mineacademy.fo.model.Variables;

import java.util.List;

public class ScoreboardData extends SimpleScoreboard implements ConfigSerializable {
    @Getter
    private final String stripTitle;

    @Getter
    private final String uniqueId;
    @Getter
    private final List<String> contents;
    @Getter
    private final Boolean isDefault;

    @Getter
    private final String displayCondition;
    @Getter
    private final String ifConditionNotMet;

    /**
     * Constructeur de la classe ScoreboardData.
     *
     * @param unique_id l'uniqueId du scoreboard
     * @param title le titre du scoreboard
     * @param update_interval l'intervalle de mise à jour du scoreboard
     * @param contents la liste des lignes du scoreboard
     * @param isDefault si le scoreboard est le scoreboard par défaut
     * @param display_condition la condition d'affichage du scoreboard
     * @param if_condition_not_met le message à afficher si la condition d'affichage n'est pas respectée
     */
    public ScoreboardData(String unique_id, String title, Integer update_interval, List<String> contents, Boolean isDefault, String display_condition, String if_condition_not_met) {
        super(title);
        super.setUpdateDelayTicks(update_interval);

        this.stripTitle = title;
        this.uniqueId = unique_id;
        this.contents = contents;
        this.isDefault = isDefault;
        this.displayCondition = display_condition;
        this.ifConditionNotMet = if_condition_not_met;

        for (Object content : this.getContents()) {
            super.addRows(content);
        }
    }

    public void showScoreboard(Player player) {
        do {
            ScoreboardData playerScoreboard = ApiImplementation.getInstance().getScoreboardManager().getScoreboard(player);
            if (playerScoreboard == null) {
                break;
            }

            if (playerScoreboard.isViewing(player)) {
                playerScoreboard.hide(player);
            }
        } while (true);

        super.show(player);
    }

    @Override
    protected void onUpdate() {
        for (int i = 0; i < this.getContents().size(); i++) {
            super.setRow(i, this.getContents().get(i));
        }
    }

    @Override
    protected String replaceVariables(@NonNull Player player, @NonNull String message) {
        final String finalTitle = Variables.replace(this.getStripTitle(), player);
        super.setTitle(finalTitle);

        return Variables.replace(message, player);
    }

    /**
     * Sérialise les données du scoreboard en une instance de SerializedMap.
     *
     * @return une instance de SerializedMap contenant les données du scoreboard
     */
    @Override
    public SerializedMap serialize() {
        return SerializedMap.ofArray(
                "UniqueId", this.getUniqueId(),
                "display_condition", this.getDisplayCondition(),
                "if_condition_not_met", this.getIfConditionNotMet(),
                "title", super.getTitle(),
                "update_interval", super.getUpdateDelayTicks(),
                "contents", this.getContents(),
                "default", this.getIsDefault()
        );
    }

    /**
     * Désérialise les données du scoreboard à partir d'une instance de SerializedMap.
     *
     * @param serializedMap l'instance de SerializedMap contenant les données du scoreboard
     * @return une instance de ScoreboardData contenant les données du scoreboard
     */
    public static ScoreboardData deserialize(SerializedMap serializedMap) {
        return new ScoreboardData(
                serializedMap.getString("UniqueId"),
                serializedMap.getString("title"),
                serializedMap.getInteger("update_interval"),
                serializedMap.getStringList("contents"),
                serializedMap.getBoolean("default"),
                serializedMap.getString("display_condition"),
                serializedMap.getString("if_condition_not_met")
        );
    }
}
