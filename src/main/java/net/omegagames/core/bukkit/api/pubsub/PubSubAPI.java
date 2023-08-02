package net.omegagames.core.bukkit.api.pubsub;

import net.omegagames.api.pubsub.*;
import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.bukkit.api.settings.Settings;
import org.mineacademy.fo.Common;
import redis.clients.jedis.Jedis;

public class PubSubAPI implements IPubSubAPI {
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
        this.patternThread = new Thread(() -> {
            while (this.working) {
                Jedis jedis = this.api.getBungeeResource();
                try {
                    String[] patternsSuscribed = this.subscriberPattern.getPatternsSuscribed();
                    if(patternsSuscribed.length > 0)
                        jedis.psubscribe(this.subscriberPattern, patternsSuscribed);
                } catch (Exception e) {
                    Common.throwError(e, "Error while subscribing to patterns");
                }
                jedis.close();
            }
        });
        this.patternThread.start();

        this.channelThread = new Thread(() -> {
            while (this.working) {
                Jedis jedis = this.api.getBungeeResource();
                try {
                    String[] channelsSuscribed = this.subscriberChannel.getChannelsSuscribed();
                    if (channelsSuscribed.length > 0)
                        jedis.subscribe(this.subscriberChannel, channelsSuscribed);
                } catch (Exception e) {
                    Common.throwError(e, "Error while subscribing to channels");
                }
                jedis.close();
            }
        });
        this.channelThread.start();
    }

    @Override
    public void subscribe(String channel, IPacketsReceiver receiver) {
        this.subscriberChannel.registerReceiver(channel, receiver);
        if(this.subscriberChannel.isSubscribed())
            this.subscriberChannel.unsubscribe();
    }

    @Override
    public void subscribe(String pattern, IPatternReceiver receiver) {
        this.subscriberPattern.registerPattern(pattern, receiver);
        if(this.subscriberPattern.isSubscribed())
            this.subscriberPattern.punsubscribe();
    }

    @Override
    public void send(String channel, String message) {
        Common.log(Settings.Jedis.PREFIX + "[SEND] Channel: &f" + channel + "&9, Message: &f" + message + "&f.");

        final String paramServerName = this.api.getServerName();
        this.sender.publish(new PendingMessage(channel, paramServerName + ":" + message));
    }

    @Override
    public void send(PendingMessage message) {
        this.sender.publish(message);
    }

    @Override
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
