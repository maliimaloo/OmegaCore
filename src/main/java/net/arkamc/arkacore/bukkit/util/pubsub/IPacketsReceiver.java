package net.arkamc.arkacore.bukkit.util.pubsub;

public interface IPacketsReceiver {
    /**
     * Déclenché lorsqu message Redis PubSub est reçu.
     *
     * @param channel Le canal du message PubSub.
     * @param packet Le contenu du message PubSub.
     */
    void receive(String channel, String packet);
}
