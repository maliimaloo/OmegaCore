package net.omegagames.core.bukkit.api.scoreboard;

import lombok.Getter;

public class Scoreboard implements Runnable {
    @Getter
    private final static Scoreboard instance = new Scoreboard();

    private Scoreboard() {

    }

    @Override
    public void run() {

    }
}
