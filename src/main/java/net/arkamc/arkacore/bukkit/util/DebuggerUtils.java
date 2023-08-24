package net.arkamc.arkacore.bukkit.util;

import net.arkamc.arkacore.bukkit.settings.Settings;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;

public class DebuggerUtils {
    public static void printStackTrace(String message) {
        if (Settings.IS_DEBUG) {
            StackTraceElement[] trace = (new Exception()).getStackTrace();
            print("!----------------------------------------------------------------------------------------------------------!");
            print(message);
            print("!----------------------------------------------------------------------------------------------------------!");

            for (int i = 1; i < trace.length; ++i) {
                String line = trace[i].toString();
                if (canPrint(line)) {
                    print("\tat " + line);
                }
            }

            print("--------------------------------------------------------------------------------------------------------end-");
        }
    }

    private static void print(String message) {
        Bukkit.getConsoleSender();
        Bukkit.getConsoleSender().sendMessage(Common.colorize(message));

    }

    private static boolean canPrint(String message) {
        return !message.contains("net.minecraft") && !message.contains("org.bukkit.craftbukkit") && !message.contains("org.github.paperspigot.ServerScheduler") && !message.contains("nashorn") && !message.contains("javax.script") && !message.contains("org.yaml.snakeyaml") && !message.contains("sun.reflect") && !message.contains("sun.misc") && !message.contains("java.lang.Thread.run") && !message.contains("java.util.concurrent.ThreadPoolExecutor");
    }
}
