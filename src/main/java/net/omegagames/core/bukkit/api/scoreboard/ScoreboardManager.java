package net.omegagames.core.bukkit.api.scoreboard;

import lombok.Getter;
import net.omegagames.core.bukkit.ApiImplementation;

import net.omegagames.core.bukkit.api.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.remain.Remain;

import java.util.Objects;
import java.util.Set;

public record ScoreboardManager (@Getter Set<ScoreboardData> scoreboard_cache, @Getter ApiImplementation api) {
    public ScoreboardManager(ApiImplementation api) {
        this(Settings.Scoreboard.SCOREBOARD_DATA, api);

        Common.runTimerAsync(10, () -> {
            for (ScoreboardData scoreboard : this.getScoreboard_cache()) {
                if (Valid.isNullOrEmpty(scoreboard.getDisplayCondition()) || Remain.getOnlinePlayers().isEmpty()) {
                    continue;
                }

                final String displayCondition = scoreboard.getDisplayCondition();
                final String paramPlaceholder = displayCondition.split("=")[0];
                final Boolean paramConditionValue = Boolean.valueOf(displayCondition.split("=")[1]);

                for (Player paramPlayer : Remain.getOnlinePlayers()) {
                    final String paramReplaceholderValue = HookManager.replacePlaceholders(paramPlayer, paramPlaceholder);
                    final Boolean paramValue = Boolean.valueOf(paramReplaceholderValue);

                    if (Objects.equals(paramConditionValue, paramValue)) {
                        if (scoreboard.isViewing(paramPlayer)) {
                            continue;
                        }

                        Bukkit.getScheduler().runTask(this.api.getPlugin(), () -> scoreboard.showScoreboard(paramPlayer));
                    } else {
                        ScoreboardData scoreboardData = this.getApi().getScoreboardManager().getScoreboard(scoreboard.getIfConditionNotMet());
                        if (scoreboardData != null && !scoreboardData.isViewing(paramPlayer)) {
                            Bukkit.getScheduler().runTask(this.api.getPlugin(), () -> scoreboardData.showScoreboard(paramPlayer));
                        }
                    }
                }
            }
        });
    }

    public ScoreboardData getScoreboard(String uniqueId) {
        return this.scoreboard_cache.stream().filter((scoreboard) -> scoreboard.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public ScoreboardData getScoreboard(Player player) {
        return this.scoreboard_cache.stream().filter((scoreboard) -> scoreboard.isViewing(player)).findFirst().orElse(null);
    }
}
