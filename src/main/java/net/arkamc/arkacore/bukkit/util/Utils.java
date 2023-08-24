package net.arkamc.arkacore.bukkit.util;

import net.arkamc.arkacore.bukkit.util.model.Callback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.jsonsimple.JSONArray;
import org.mineacademy.fo.jsonsimple.JSONObject;
import org.mineacademy.fo.jsonsimple.JSONParser;
import org.mineacademy.fo.model.SimpleComponent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings("unused")
public final class Utils {
    /**
     * Vérifie si un joueur a une certaine permission.
     *
     * @param paramSender Le joueur a vérifié
     * @param paramString La permission a vérifié
     * @return true si le joueur a la permission, sinon false.
     */
    public static boolean hasPermission(CommandSender paramSender, String paramString) {
        if (paramString == null || paramString.isEmpty() || paramString.equals("none"))
            return true;
        return !(paramSender instanceof Player) || paramSender.hasPermission(paramString) || paramSender.isOp();
    }

    /**
     * Vérifie si une chaîne commence par une couleur valide.
     *
     * @param paramString La chaîne a vérifié
     * @return true si la chaîne commence par une couleur valide, sinon false.
     */
    public static boolean startWithColor(String paramString) {
        boolean bool = paramString.startsWith("&") || paramString.startsWith("§") || paramString.startsWith("{#") || paramString.startsWith("#");
        if (!bool) {
            Common.log( "Settings: la couleur du message '" + paramString + "' ne commence pas par une couleur. (&, §, {#, #)");
            return false;
        }

        return true;
    }

    public static SimpleComponent itemHoverComponent(Player paramPlayer) {
        SimpleComponent itemComponent = SimpleComponent.of(true, "[i] ");

        ItemStack item;
        try {
            item = paramPlayer.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError error) {
            item = paramPlayer.getInventory().getItemInHand();
        }

        if (item.getType() == Material.AIR) {
            return itemComponent;
        }

        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
            itemComponent = SimpleComponent.of(true, item.getType().name() + " ");
        } else {
            itemComponent = SimpleComponent.of(true, item.getItemMeta().getDisplayName() + " ");
        }

        itemComponent.onHover(item);
        return itemComponent;
    }

    public static void getUUIDFromUsernameAsync(String username, Callback<UUID> callback) {
        Common.runAsync(() -> {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    JSONArray jsonArray = JSONParser.deserializeMany(reader);
                    reader.close();

                    String uuidString = ((JSONObject) jsonArray.get(0)).get("id").toString();
                    UUID uuid = UUID.fromString(uuidString.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    callback.onSuccess(uuid);
                } else {
                    Common.throwError(null, "Impossible de récupérer l'UUID de " + username + ". Code de réponse: " + connection.getResponseCode() + ".");
                    callback.onFailure(new FoException("Impossible de récupérer l'UUID de " + username + ". Code de réponse: " + connection.getResponseCode() + "."));
                }
            } catch (Throwable throwable) {
                Common.throwError(throwable, "Impossible de récupérer l'UUID de " + username);
                callback.onFailure(new FoException("Impossible de récupérer l'UUID de " + username));
            }
        });
    }
}
