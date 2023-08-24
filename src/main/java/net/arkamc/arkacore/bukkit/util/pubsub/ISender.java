package net.arkamc.arkacore.bukkit.util.pubsub;

public interface ISender {
    /**
     * Publie un message donné.
     *
     * @param message Message.
     */
    void publish(PendingMessage message);
}

