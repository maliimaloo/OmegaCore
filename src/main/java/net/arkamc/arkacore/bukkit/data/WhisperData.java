package net.arkamc.arkacore.bukkit.data;

import lombok.Getter;
import net.arkamc.arkacore.bukkit.settings.Settings;
import net.arkamc.arkacore.bukkit.util.Utils;
import org.bukkit.entity.Player;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Variables;

import java.util.HashMap;
import java.util.Map;

public class WhisperData implements ConfigSerializable {
    @Getter
    private static Map<String, String> whisperCache = new HashMap<>();

    @Getter
    private final String senderName;
    @Getter
    private final String receiverName;

    @Getter
    private final String message;
    @Getter
    private final String senderFormat;
    @Getter
    private final String receiverFormat;
    @Getter
    private final String colorMessage;

    public WhisperData(String senderName, String receiverName, String message) {
        this.senderName = senderName;
        this.receiverName = receiverName;

        this.senderFormat = Settings.Whisper.FORMAT_SENDER;
        this.receiverFormat = Settings.Whisper.FORMAT_RECEIVING;
        this.colorMessage = Settings.Whisper.MESSAGE_COLOR;

        this.message = message;
    }

    public void sendWhisper(Player toPlayer) {
        if (!Utils.startWithColor(this.colorMessage)) {
            throw new CommandException("Aucune couleur de message n'a était trouvée dans le fichier de configuration.");
        }

        final String whisperMessage = Variables.replace(this.receiverFormat, toPlayer, SerializedMap.ofArray(
                "message", this.message,
                "sender", this.senderName,
                "receiver", this.receiverName
        ).asMap());

        toPlayer.sendMessage(whisperMessage);
    }

    @Override
    public SerializedMap serialize() {
        return SerializedMap.ofArray(
            "sender_name", this.senderName,
            "receiver_name", this.receiverName,
            "message", this.message
        );

    }

    public static WhisperData deserialize(SerializedMap serializedMap) {
        final String senderName = serializedMap.getString("sender_name");
        final String receiverName = serializedMap.getString("receiver_name");
        final String message = serializedMap.getString("message");

        return new WhisperData(senderName, receiverName, message);
    }
}
