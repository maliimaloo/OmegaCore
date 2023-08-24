package net.arkamc.arkacore.bukkit.util.pubsub;

public interface IPatternReceiver {
    /**
     * Déclenché lorsqu'un message Redis PubSub est reçu.
     *
     * @param pattern Le modèle du message PubSub.
     * @param channel Le canal du message PubSub.
     * @param packet Le contenu du message PubSub.
     */
    void receive(String pattern, String channel, String packet);
}
