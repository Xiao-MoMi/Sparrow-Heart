package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.argument.HandSlot;
import net.momirealms.sparrow.heart.exception.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;

public abstract class SparrowHeart {

    public static SparrowHeart getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final SparrowHeart INSTANCE = getHeart();

        public static SparrowHeart getHeart() {
            String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String className;
            if (isMojMap()) {
                switch (bukkitVersion) {
                    case "1.20.5", "1.20.6" -> className = "Mojmap_R1";
                    default -> throw new UnsupportedVersionException();
                }
            } else {
                switch (bukkitVersion) {
                    case "1.20.5", "1.20.6" -> className = "Reobf_1_20_R4";
                    case "1.20.3", "1.20.4" -> className = "Reobf_1_20_R3";
                    case "1.20.2" -> className = "Reobf_1_20_R2";
                    case "1.20", "1.20.1" -> className = "Reobf_1_20_R1";
                    case "1.19.4" -> className = "Reobf_1_19_R3";
                    case "1.19.3" -> className = "Reobf_1_19_R2";
                    case "1.19.2", "1.19.1" -> className = "Reobf_1_19_R1";
                    case "1.18.2" -> className = "Reobf_1_18_R2";
                    case "1.18.1", "1.18" -> className = "Reobf_1_18_R1";
                    default -> throw new UnsupportedVersionException();
                }
            }
            try {
                Class<?> clazz = Class.forName("net.momirealms.sparrow.heart.impl." + className);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (SparrowHeart) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize sparrow heart", e);
            }
        }

        private static boolean isMojMap() {
            try {
                Class.forName("net.minecraft.world.level.biome.Biome");
                return true;
            } catch (ClassNotFoundException ignore) {
                return false;
            }
        }
    }

    public abstract void sendActionBar(Player player, String json);

    public abstract void sendTitle(Player player, String titleJson, String subTitleJson, int fadeInTicks, int stayTicks, int fadeOutTicks);

    public abstract void sendToast(Player player, ItemStack icon, String titleJson, String advancementType);

    public abstract void sendDemo(Player player);

    public abstract void sendCredits(Player player);

    public abstract void sendTotemAnimation(Player player, ItemStack totem);

    public abstract void openCustomInventory(Player player, Inventory inventory, String jsonTitle);

    public abstract void updateInventoryTitle(Player player, String jsonTitle);

    public abstract void swingHand(Player player, HandSlot slot);
}
