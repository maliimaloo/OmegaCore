package net.omegagames.core.bukkit.api.scoreboard;

import org.mineacademy.fo.model.SimpleScoreboard;
import org.mineacademy.fo.remain.Remain;

public class Scoreboard extends SimpleScoreboard {
    public Scoreboard() {
        super("§6§lOmegaGames");

        super.addRows("§7§m------------------", "§7Joueurs: §f%player_online%", "§7§m------------------");
    }

    @Override
    protected void onUpdate() {
        super.setRow(1, "§7Joueurs: §f" + Remain.getOnlinePlayers().size());
    }
}
