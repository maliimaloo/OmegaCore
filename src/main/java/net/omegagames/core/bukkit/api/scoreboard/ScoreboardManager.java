package net.omegagames.core.bukkit.api.scoreboard;

public class ScoreboardManager {
    /*@Getter
    private final Set<ScoreboardData> cache;

    @Getter
    private final ApiImplementation api;

    public ScoreboardManager(ApiImplementation api) {
        this.cache = Settings.Scoreboard.SCOREBOARD_DATA;
        this.api = api;

        Common.runTimer(10, () -> {
            for (ScoreboardData scoreboard : this.getCache()) {
                if (Valid.isNullOrEmpty(scoreboard.getDisplayCondition()) || Remain.getOnlinePlayers().isEmpty()) {
                    continue;
                }

                final String[] displayCondition = scoreboard.getDisplayCondition().split("=");
                final String paramPlaceholder = displayCondition[0];
                final Boolean paramConditionValue = Boolean.valueOf(displayCondition[1]);

                for (Player paramPlayer : Remain.getOnlinePlayers()) {
                    final String paramReplaceholderValue = HookManager.replacePlaceholders(paramPlayer, paramPlaceholder);
                    final Boolean paramValue = Boolean.valueOf(paramReplaceholderValue);

                    if (Objects.equals(paramConditionValue, paramValue)) {
                        if (!scoreboard.isViewing(paramPlayer)) {
                            scoreboard.showScoreboard(paramPlayer);
                        }
                    } else {
                        ScoreboardData scoreboardData = this.api.getScoreboardManager().getScoreboard(scoreboard.getIfConditionNotMet());
                        if (scoreboardData != null && !scoreboardData.isViewing(paramPlayer)) {
                            scoreboardData.showScoreboard(paramPlayer);
                        }
                    }
                }
            }
        });
    }

    public ScoreboardData getScoreboard(String uniqueId) {
        return this.getCache().stream().filter((scoreboard) -> scoreboard.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public ScoreboardData getScoreboard(Player player) {
        return this.getCache().stream().filter((scoreboard) -> scoreboard.isViewing(player)).findFirst().orElse(null);
    }*/
}
