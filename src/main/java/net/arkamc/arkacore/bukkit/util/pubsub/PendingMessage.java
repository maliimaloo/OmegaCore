package net.arkamc.arkacore.bukkit.util.pubsub;

public class PendingMessage {
    private final String channel;
    private final String message;
    private final Runnable callback;

    /**
     * Constructeur
     *
     * @param channel Canal du message
     * @param message Contenu du message
     * @param callback Callback déclenché après l'opération
     */
    public PendingMessage(String channel, String message, Runnable callback) {
        this.channel = channel;
        this.message = message;
        this.callback = callback;
    }

    /**
     * Constructeur
     *
     * @param channel Canal du message
     * @param message Contenu du message
     */
    public PendingMessage(String channel, String message) {
        this(channel, message, null);
    }

    /**
     * Déclenche le callback
     */
    public void runAfter() {
        try {
            if (this.callback != null)
                this.callback.run();
        } catch (Exception ignored) {
        }
    }

    /**
     * Obtient le canal du message
     *
     * @return Canal
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Obtient le contenu du message
     *
     * @return Message
     */
    public String getMessage() {
        return this.message;
    }
}
