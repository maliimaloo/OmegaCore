package net.arkamc.arkacore.bukkit.util.pubsub;

public interface IPubSubAPI {
    /**
     * Souscrit un {@link IPacketsReceiver} donné à un canal donné.
     *
     * @param channel Canal à écouter.
     * @param receiver Récepteur.
     */
    void subscribe(String channel, IPacketsReceiver receiver);

    /**
     * Souscrit un {@link IPatternReceiver} donné à un motif donné.
     *
     * @param pattern Motif à écouter.
     * @param receiver Récepteur.
     */
    void subscribe(String pattern, IPatternReceiver receiver);

    /**
     * Envoie un message donné sur le canal donné.
     *
     * @param channel Canal.
     * @param message Message.
     */
    void send(String channel, String message);

    /**
     * Envoie un message PubSub {@link PendingMessage}.
     *
     * @param message Message.
     */
    void send(PendingMessage message);

    /**
     * Obtient l'éditeur de messages.
     *
     * @return Instance.
     */
    ISender getSender();
}
