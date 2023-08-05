package net.omegagames.core.bukkit.api.commands;

import lombok.AccessLevel;
import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

@AutoRegister
public final class TestCommand extends SimpleCommand {
    @Getter (value = AccessLevel.PUBLIC)
    private static Boolean paramValue = false;

    @Getter (value = AccessLevel.PRIVATE)
    private static TestCommand instance = new TestCommand();

    public TestCommand() {
        super("test");
    }

    @Override
    protected void onCommand() {
        paramValue = !paramValue;
        super.tellSuccess("Param value is now " + paramValue);
    }
}
