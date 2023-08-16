package net.omegagames.core.bukkit.api.settings;

import lombok.Getter;
import net.omegagames.core.bukkit.api.util.model.MenuItemCreator;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettingsCreditLogMenu extends YamlStaticConfig {
    public static String TITLE;
    public static Integer ROW;
    public static Integer PAGE_SIZE;

    private static void init() {
        SettingsCreditLogMenu.setPathPrefix(null);

        TITLE = SettingsCreditLogMenu.getString("Title");
        ROW = SettingsCreditLogMenu.getInteger("Row");
        PAGE_SIZE = SettingsCreditLogMenu.getInteger("Page_Size");
    }

    public final static class Content  {
        @Getter
        private static Map<UUID, MenuItemCreator> cache = new HashMap<>();

        private static void init() {
            SettingsCreditLogMenu.setPathPrefix(null);

            SerializedMap sectionContent = SettingsCreditLogMenu.getMap("Contents.Content");
            sectionContent.forEach((key, value) -> {
                SerializedMap content = sectionContent.getMap(key);

                MenuItemCreator.Builder itemCreatorBuilder = MenuItemCreator.of();
                if (content.containsKey("Title")) {
                    itemCreatorBuilder.title(content.getString("Title"));
                }

                if (content.containsKey("Material")) {
                    itemCreatorBuilder.material(content.getString("Material"));
                }

                if (content.containsKey("Slots")) {
                    List<Integer> slots = content.getList("Slots", Integer.class);
                    itemCreatorBuilder.slots(slots);
                }

                if (content.containsKey("Glow")) {
                    itemCreatorBuilder.glow(content.getBoolean("Glow"));
                }

                if (content.containsKey("Lore")) {
                    itemCreatorBuilder.lore(content.getStringList("Lore"));
                }

                itemCreatorBuilder.key(key);

                MenuItemCreator finalItemCreator = itemCreatorBuilder.build();
                cache.put(finalItemCreator.getUniqueId(), finalItemCreator);
            });
        }
    }

    public final static class ImmutableContent {
        @Getter
        private static Map<UUID, MenuItemCreator> cache = new HashMap<>();

        private static void init() {
            SettingsCreditLogMenu.setPathPrefix(null);

            SerializedMap sectionImmutableContent = SettingsCreditLogMenu.getMap("Contents.Immutable_Content");
            sectionImmutableContent.forEach((key, value) -> {
                SerializedMap content = sectionImmutableContent.getMap(key);

                MenuItemCreator.Builder itemCreatorBuilder = MenuItemCreator.of();
                if (content.containsKey("Title")) {
                    itemCreatorBuilder.title(content.getString("Title"));
                }

                if (content.containsKey("Material")) {
                    itemCreatorBuilder.material(content.getString("Material"));
                }

                if (content.containsKey("Slots")) {
                    List<Integer> slots = content.getList("Slots", Integer.class);
                    slots.forEach((slot) -> Common.log("Key: " + key + ", Slot: " + slot));

                    itemCreatorBuilder.slots(slots);
                }

                if (content.containsKey("Glow")) {
                    itemCreatorBuilder.glow(content.getBoolean("Glow"));
                }

                if (content.containsKey("Lore")) {
                    itemCreatorBuilder.lore(content.getStringList("Lore"));
                }

                itemCreatorBuilder.key(key);

                MenuItemCreator finalItemCreator = itemCreatorBuilder.build();
                cache.put(finalItemCreator.getUniqueId(), finalItemCreator);
            });
        }
    }

    @Override
    protected void onLoad() {
        super.loadConfiguration("menu/credit_log.yml");
    }
}
