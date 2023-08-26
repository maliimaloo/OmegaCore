package net.arkamc.arkacore.bukkit.pubsub;

import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.settings.Settings;
import net.arkamc.arkacore.bukkit.util.pubsub.IPacketsReceiver;
import net.arkamc.arkacore.bukkit.util.pubsub.IPatternReceiver;
import net.arkamc.arkacore.bukkit.util.pubsub.ISender;
import net.arkamc.arkacore.bukkit.util.pubsub.PendingMessage;
import org.mineacademy.fo.Common;
import redis.clients.jedis.Jedis;

public class PubSubAPI {
    private final Subscriber subscriberPattern;
    private final Subscriber subscriberChannel;

    private final Sender sender;
    private final ApiImplementation api;

    boolean working = true;

    private final Thread senderThread;
    private Thread patternThread;
    private Thread channelThread;

    // Avoid to init Threads before the subclass constructor is started (Fix possible atomicity violation)
    public PubSubAPI(ApiImplementation api) {
        this.api = api;
        this.subscriberPattern = new Subscriber();
        this.subscriberChannel = new Subscriber();

        this.sender = new Sender(api);
        this.senderThread = new Thread(sender, "SenderThread");
        this.senderThread.start();

        this.startThread();
    }

    private void startThread() {
        Common.runAsync(() -> {
            try (Jedis jedis = this.api.getBungeeResource()) {
                String[] patternsSuscribed = this.subscriberPattern.getPatternsSuscribed();
                if (patternsSuscribed.length > 0) {
                    jedis.psubscribe(this.subscriberPattern, patternsSuscribed);
                }
            } catch (Exception e) {
                Common.throwError(e, "Error while subscribing to patterns");
            }

            try (Jedis jedis = this.api.getBungeeResource()) {
                String[] channelsSuscribed = this.subscriberChannel.getChannelsSuscribed();
                if (channelsSuscribed.length > 0) {
                    jedis.subscribe(this.subscriberChannel, channelsSuscribed);
                }
            } catch (Exception e) {
                Common.throwError(e, "Error while subscribing to channels");
            }
        });
    }

    public void subscribe(String channel, IPacketsReceiver receiver) {
        this.subscriberChannel.registerReceiver(channel, receiver);
        if(this.subscriberChannel.isSubscribed())
            this.subscriberChannel.unsubscribe();
    }

    public void subscribe(String pattern, IPatternReceiver receiver) {
        this.subscriberPattern.registerPattern(pattern, receiver);
        if(this.subscriberPattern.isSubscribed())
            this.subscriberPattern.punsubscribe();
    }

    public void send(String channel, String message) {
        Common.log(Settings.Jedis.PREFIX + "[SEND] Channel: &f" + channel + "&9, Message: &f" + message + "&f.");

        final String paramServerName = this.api.getServerName();
        this.sender.publish(new PendingMessage(channel, paramServerName + ":" + message));
    }

    public void send(PendingMessage message) {
        this.sender.publish(message);
    }

    public ISender getSender() {
        return this.sender;
    }

    public void disable() {
        this.working = false;
        this.subscriberChannel.unsubscribe();
        this.subscriberPattern.punsubscribe();
        try {
            Thread.sleep(500);
        } catch (Exception ignored) {
        }

        this.senderThread.interrupt();
        this.patternThread.interrupt();
        this.channelThread.interrupt();
    }
}
