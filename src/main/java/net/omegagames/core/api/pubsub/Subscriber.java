package net.omegagames.core.api.pubsub;

import net.omegagames.api.pubsub.IPacketsReceiver;
import net.omegagames.api.pubsub.IPatternReceiver;
import net.omegagames.core.PluginCore;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.plugin.SimplePlugin;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Subscriber extends JedisPubSub {

    private final HashMap<String, HashSet<IPacketsReceiver>> packetsReceivers = new HashMap<>();
    private final HashMap<String, HashSet<IPatternReceiver>> patternsReceivers = new HashMap<>();

    public void registerReceiver(String channel, IPacketsReceiver receiver) {
        HashSet<IPacketsReceiver> receivers = this.packetsReceivers.get(channel);
        if (receivers == null)
            receivers = new HashSet<>();
        receivers.add(receiver);
        this.packetsReceivers.put(channel, receivers);
    }

    public void registerPattern(String pattern, IPatternReceiver receiver) {
        HashSet<IPatternReceiver> receivers = this.patternsReceivers.get(pattern);
        if (receivers == null)
            receivers = new HashSet<>();
        receivers.add(receiver);
        this.patternsReceivers.put(pattern, receivers);
    }

    @Override
    public void onMessage(String channel, String message) {
        try
        {
            HashSet<IPacketsReceiver> receivers = packetsReceivers.get(channel);
            if (receivers != null) {
                receivers.forEach((IPacketsReceiver receiver) -> receiver.receive(channel, message));
            } else {
                Debugger.printStackTrace("{PubSub} Message reçu sur un canal, mais aucun packetsReceiver n'a été trouvé. (canal : " + channel + ", message : " + message + ")");
            }

            ((PluginCore) SimplePlugin.getInstance()).getDebugListener().receive("onlychannel", channel, message);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        try {
            HashSet<IPatternReceiver> receivers = patternsReceivers.get(pattern);
            if (receivers != null) {
                receivers.forEach((IPatternReceiver receiver) -> receiver.receive(pattern, channel, message));
            } else {
                Debugger.printStackTrace("{PubSub} Message reçu sur un canal, mais aucun packetsReceiver n'a été trouvé. (canal : " + channel + ", message : " + message + ")");
            }

            ((PluginCore) SimplePlugin.getInstance()).getDebugListener().receive(pattern, channel, message);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String[] getChannelsSuscribed() {
        Set<String> strings = this.packetsReceivers.keySet();
        return strings.toArray(new String[0]);
    }

    public String[] getPatternsSuscribed() {
        Set<String> strings = this.patternsReceivers.keySet();
        return strings.toArray(new String[0]);
    }
}
