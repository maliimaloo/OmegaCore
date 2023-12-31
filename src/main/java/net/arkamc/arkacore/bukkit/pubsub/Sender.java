package net.arkamc.arkacore.bukkit.pubsub;

import net.arkamc.arkacore.bukkit.ApiImplementation;
import net.arkamc.arkacore.bukkit.BukkitCore;
import net.arkamc.arkacore.bukkit.util.pubsub.ISender;
import net.arkamc.arkacore.bukkit.util.pubsub.PendingMessage;
import redis.clients.jedis.Jedis;

import java.util.concurrent.LinkedBlockingQueue;

class Sender implements Runnable, ISender
{

    private final LinkedBlockingQueue<PendingMessage> pendingMessages = new LinkedBlockingQueue<>();
    private final ApiImplementation connector;
    private Jedis jedis;

    public Sender(ApiImplementation connector)
    {
        this.connector = connector;
    }

    public void publish(PendingMessage message)
    {
        this.pendingMessages.add(message);
    }

    @Override
    public void run()
    {
        fixDatabase();
        while (true) {
            PendingMessage message;
            try {
                message = this.pendingMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.jedis.close();
                return;
            }

            boolean published = false;
            while (!published) {
                try {
                    this.jedis.publish(message.getChannel(), message.getMessage());
                    message.runAfter();
                    published = true;
                } catch (Exception e) {
                    fixDatabase();
                }
            }
        }
    }

    private void fixDatabase() {
        try {
            this.jedis = this.connector.getBungeeResource();
        } catch (Exception e) {
            BukkitCore.getInstance().getLogger().severe("[Publisher] Cannot connect to redis server : " + e.getMessage() + ". Retrying in 5 seconds.");
            try {
                Thread.sleep(5000);
                fixDatabase();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
