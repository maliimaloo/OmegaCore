package net.omegagames.core.bukkit.api.util.model;

import lombok.Getter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.omegagames.core.bukkit.api.settings.SettingsCreditLogMenu;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.model.Replacer;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.nbt.NBTCompound;
import org.mineacademy.fo.remain.nbt.NBTItem;

import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public final class MenuItemCreator {
    @Getter
    private static final String keyCustomData = "item_data";
    @Getter
    private static final String keyUniqueId = "unique_id";

    @Getter
    private UUID uniqueId;
    @Getter
    private String key, name, material;
    @Getter
    private boolean glow;
    @Getter
    private List<String> lore;
    @Getter
    private List<Integer> slots;

    private MenuItemCreator(Builder builder) {
        this.uniqueId = UUID.randomUUID();
        this.key = builder.key;
        this.name = builder.name;
        this.material = builder.material;
        this.slots = builder.slots;
        this.glow = builder.glow;
        this.lore = builder.lore;
    }

    public ItemStack toItemStack(SerializedMap replaceVariables) {
        ItemCreator itemCreator;
        if (replaceVariables == null) {
            replaceVariables = new SerializedMap();
        }

        if (this.material.contains("-")) {
            String[] materialData = this.material.split("-");
            switch (materialData[0]) {
                case "SKULL":
                    ItemStack skullItem = SkullCreator.itemFromName(materialData[1]);
                    if (skullItem == null) {
                        throw new FoException("Tête introuvable: " + materialData[1]);
                    }

                    itemCreator = ItemCreator.of(SkullCreator.itemFromName(materialData[1]));
                    break;

                case "HDB":
                    ItemStack hdbItem = new HeadDatabaseAPI().getItemHead(materialData[1]);
                    if (hdbItem == null) {
                        throw new FoException("Tête introuvable: " + materialData[1]);
                    }

                    itemCreator = ItemCreator.of(hdbItem);
                    break;
                default:
                    throw new FoException("Invalid material type: " + this.material);
            }
        } else {
            itemCreator = ItemCreator.of(CompMaterial.fromString(this.material));
        }

        if (!Valid.isNullOrEmpty(this.name)) {
            final String finalName = Replacer.replaceVariables(this.name, replaceVariables);
            itemCreator.name(finalName);
        }

        if (!Valid.isNullOrEmpty(this.lore)) {
            final List<String> finalLore = Replacer.replaceVariables(this.lore, replaceVariables);
            itemCreator.lore(finalLore);
        }

        itemCreator.glow(this.isGlow());

        NBTItem nbtItem = new NBTItem(itemCreator.make());
        NBTCompound nbtCompound = nbtItem.addCompound(MenuItemCreator.getKeyCustomData());
        nbtCompound.setString(MenuItemCreator.getKeyUniqueId(), this.getUniqueId().toString());

        return nbtItem.getItem();
    }

    public static MenuItemCreator fromItemStack(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound(MenuItemCreator.getKeyCustomData());
        if (nbtCompound == null) {
            throw new FoException("L'item n'a pas de données personnalisées");
        }

        String strUniqueId = nbtCompound.getString(MenuItemCreator.getKeyUniqueId());
        if (strUniqueId == null) {
            throw new FoException("L'item n'a pas d'identifiant unique");
        }

        final UUID uniqueId = UUID.fromString(strUniqueId);
        if (SettingsCreditLogMenu.ImmutableContent.getCache().containsKey(uniqueId)) {
            return SettingsCreditLogMenu.ImmutableContent.getCache().get(uniqueId);
        } else if (SettingsCreditLogMenu.Content.getCache().containsKey(uniqueId)) {
            return SettingsCreditLogMenu.Content.getCache().get(uniqueId);
        }

        throw new FoException("L'item n'a pas été trouvé dans les caches.");
    }

    public static MenuItemCreator fromKey(String key) {
        return Stream.concat(SettingsCreditLogMenu.ImmutableContent.getCache().values().stream(),
                        SettingsCreditLogMenu.Content.getCache().values().stream())
                .filter(itemCreator -> itemCreator.getKey().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    public static Builder of() {
        return new Builder().title("Default").material(CompMaterial.BARRIER.toMaterial().toString()).slots(new ArrayList<>());
    }

    // Other methods...

    public static class Builder {
        private String key, name, material;
        private boolean glow;
        private List<String> lore;
        private List<Integer> slots;

        public Builder title(String title) {
            this.name = title;
            return this;
        }

        public Builder material(String material) {
            this.material = material;
            return this;
        }

        public Builder slots(List<Integer> slots) {
            this.slots = slots;
            return this;
        }

        public void glow(boolean glow) {
            this.glow = glow;
        }

        public void lore(List<String> lore) {
            this.lore = lore;
        }

        public void key(String key) {
            this.key = key;
        }

        public MenuItemCreator build() {
            return new MenuItemCreator(this);
        }
    }
}
